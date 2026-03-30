package com.abpvt.newsapp.data.repository

import com.abpvt.newsapp.data.model.Article
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import com.abpvt.newsapp.utils.Resource
import java.util.Base64

data class ArticleInteractionState(
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: Boolean? = null, // true for upvote, false for downvote, null for no vote
    val isBookmarked: Boolean = false
)

class InteractionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Use Base64 encoding for article URLs to use them as document IDs safely
    private fun encodeUrl(url: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.toByteArray())
    }

    /** View interaction state for a specific article. */
    suspend fun getInteractionState(articleUrl: String): Resource<ArticleInteractionState> {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            return Resource.Success(ArticleInteractionState())
        }

        val encodedUrl = encodeUrl(articleUrl)
        
        return try {
            // Get votes
            val voteDoc = db.collection("article_votes").document(encodedUrl).get().await()
            val voters = voteDoc.get("voters") as? Map<String, Boolean> ?: emptyMap()
            
            var upvotes = 0
            var downvotes = 0
            for ((_, isUpvote) in voters) {
                if (isUpvote) upvotes++ else downvotes++
            }
            val userVote = voters[userId]

            // Get bookmark status
            val bookmarkDoc = db.collection("users").document(userId)
                .collection("bookmarks").document(encodedUrl).get().await()
            val isBookmarked = bookmarkDoc.exists()

            Resource.Success(
                ArticleInteractionState(
                    upvotes = upvotes,
                    downvotes = downvotes,
                    userVote = userVote,
                    isBookmarked = isBookmarked
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load interaction state")
        }
    }

    /** Vote on an article. */
    suspend fun voteArticle(articleUrl: String, isUpvote: Boolean): Resource<ArticleInteractionState> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        val encodedUrl = encodeUrl(articleUrl)
        val voteRef = db.collection("article_votes").document(encodedUrl)

        return try {
            val updatedState = db.runTransaction { transaction ->
                val snapshot = transaction.get(voteRef)
                val voters = (snapshot.get("voters") as? MutableMap<String, Boolean>) ?: mutableMapOf()
                
                val previousVote = voters[userId]
                if (previousVote == isUpvote) {
                    // Toggle off
                    voters.remove(userId)
                } else {
                    // Set/update
                    voters[userId] = isUpvote
                }
                
                transaction.set(voteRef, mapOf("voters" to voters), SetOptions.merge())

                // Calculate new counts
                var upvotes = 0
                var downvotes = 0
                for ((_, vote) in voters) {
                    if (vote) upvotes++ else downvotes++
                }

                ArticleInteractionState(
                    upvotes = upvotes,
                    downvotes = downvotes,
                    userVote = voters[userId],
                    isBookmarked = false // Needs to be fetched separately, usually combined in ViewModel
                )
            }.await()
            
            Resource.Success(updatedState)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to vote")
        }
    }

    /** Toggle bookmark for an article. */
    suspend fun toggleBookmark(articleUrl: String, article: Article? = null): Resource<Boolean> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        val encodedUrl = encodeUrl(articleUrl)
        val bookmarkRef = db.collection("users").document(userId)
            .collection("bookmarks").document(encodedUrl)

        return try {
            val exists = bookmarkRef.get().await().exists()
            if (exists) {
                bookmarkRef.delete().await()
                Resource.Success(false)
            } else {
                // Save the article data so it can be displayed in a bookmarks list later
                val bookmarkData = mutableMapOf<String, Any>(
                    "url" to articleUrl,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                
                if (article != null) {
                    bookmarkData["title"] = article.title
                    bookmarkData["source"] = article.source.name
                    if (article.publishedAt.isNotBlank()) bookmarkData["publishedAt"] = article.publishedAt
                    article.urlToImage?.let { bookmarkData["urlToImage"] = it }
                    article.description?.let { bookmarkData["description"] = it }
                }
                
                bookmarkRef.set(bookmarkData).await()
                Resource.Success(true)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to toggle bookmark")
        }
    }

    /** Fetch all bookmarked articles for current user. */
    suspend fun getBookmarkedArticles(): Resource<List<Article>> {
        val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
        val bookmarksCollection = db.collection("users").document(userId).collection("bookmarks")

        return try {
            val snapshot = bookmarksCollection
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val articles = snapshot.documents.mapNotNull { doc ->
                val url = doc.getString("url") ?: return@mapNotNull null
                val title = doc.getString("title") ?: "Saved Article"
                val sourceName = doc.getString("source") ?: "Unknown Source"
                val publishedAt = doc.getString("publishedAt") ?: ""
                val urlToImage = doc.getString("urlToImage")
                val description = doc.getString("description")

                Article(
                    id = doc.id,
                    title = title,
                    url = url,
                    publishedAt = publishedAt,
                    sectionName = sourceName,
                    fields = Article.Fields(
                        urlToImage = urlToImage,
                        description = description,
                        author = null,
                        content = null
                    )
                )
            }
            Resource.Success(articles)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get bookmarked articles")
        }
    }
}
