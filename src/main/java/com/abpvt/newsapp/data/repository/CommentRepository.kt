package com.abpvt.newsapp.data.repository

import android.content.Context
import com.abpvt.newsapp.data.model.Comment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.abpvt.newsapp.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class CommentRepository @Inject constructor(@ApplicationContext context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val commentsCollection = db.collection("comments")
    private val auth = FirebaseAuth.getInstance()
    private val moderationPrefs = context.getSharedPreferences("newsynk_moderation", Context.MODE_PRIVATE)

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

    /** Fetch comments for a given article ID, ordered by timestamp ascending. */
    suspend fun getCommentsForArticle(articleId: String): Resource<List<Comment>> {
        return try {
            val snapshot = commentsCollection
                .whereEqualTo("articleId", articleId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            val comments = snapshot.documents
                .mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
            val blocked = moderationPrefs.getStringSet("blocked_users", emptySet()).orEmpty()
            Resource.Success(comments.filterNot { it.userId in blocked })
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
                val voters = (snapshot.get("voters") as? Map<*, *>)
                    ?.mapNotNull { (key, value) ->
                        if (key is String && value is Boolean) key to value else null
                    }
                    ?.toMap()
                    ?.toMutableMap()
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

    suspend fun reportComment(commentId: String, reason: String): Resource<Unit> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("You must be logged in to report")
        return try {
            db.collection("comment_reports").add(
                mapOf(
                    "commentId" to commentId,
                    "reporterId" to userId,
                    "reason" to reason.take(200),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "status" to "open"
                )
            ).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to report comment")
        }
    }

    fun blockUser(userId: String) {
        if (userId.isBlank() || userId == auth.currentUser?.uid) return
        val updated = moderationPrefs.getStringSet("blocked_users", emptySet()).orEmpty().toMutableSet()
            .apply { add(userId) }
        moderationPrefs.edit().putStringSet("blocked_users", updated).apply()
    }
}
