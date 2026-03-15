package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.abpvt.newsapp.utils.Resource

class CommentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val commentsCollection = db.collection("comments")
    private val auth = FirebaseAuth.getInstance()

    /** Add a new comment to Firestore and return the document ID. */
    suspend fun addComment(comment: Comment): Resource<String> {
        return try {
            // Ensure username is set from current user's email if not provided
            val username = if (comment.username.isBlank()) {
                auth.currentUser?.email?.substringBefore("@") ?: "Anonymous"
            } else {
                comment.username
            }
            val commentWithUsername = comment.copy(username = username)
            val docRef = commentsCollection.add(commentWithUsername).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    /** Fetch comments for a given article ID. */
    suspend fun getCommentsForArticle(articleId: String): Resource<List<Comment>> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("articleId", articleId)
                // Removed .orderBy to avoid composite index requirement
                // TODO: Re-enable once Firestore index is fully propagated
                // .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            // Sort in memory instead and properly map document IDs
            val comments = snapshot.documents
                .mapNotNull { doc -> 
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                .sortedBy { it.timestamp }
            Resource.Success(comments)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch comments")
        }
    }


    /**
     * Cast an upvote/downvote for a comment by updating its 'voters' map.
     * Uses a Firestore transaction for atomic read-modify-write.
     * Returns the updated Comment object for optimistic UI updates.
     */
    suspend fun voteComment(commentId: String, userId: String, isUpvote: Boolean): Resource<Comment> {
        val commentRef = commentsCollection.document(commentId)
        return try {
            // Run a transaction to update the voters map atomically
            val updatedComment = db.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                // Get current voters map (userId -> Boolean)
                val voters = snapshot.get("voters") as? MutableMap<String, Boolean>
                    ?: mutableMapOf()
                val previousVote = voters[userId]
                if (previousVote == isUpvote) {
                    // If user has same vote already, remove it (toggle off)
                    voters.remove(userId)
                } else {
                    // Otherwise set/update to the new vote (true for upvote, false for downvote)
                    voters[userId] = isUpvote
                }
                transaction.update(commentRef, "voters", voters)
                
                // Return the updated comment
                snapshot.toObject(Comment::class.java)?.copy(
                    id = commentId,
                    voters = voters
                )
            }.await()
            
            if (updatedComment != null) {
                Resource.Success(updatedComment)
            } else {
                Resource.Error("Failed to retrieve updated comment")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to vote on comment")
        }
    }
}
