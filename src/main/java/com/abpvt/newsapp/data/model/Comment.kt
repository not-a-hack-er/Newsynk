package com.abpvt.newsapp.data.model



/** Represents a user comment on an article. */
data class Comment(
    val id: String = "",
    val articleId: String = "",
    val userId: String = "",
    val username: String = "",  // Display name for the user
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val voters: Map<String, Boolean> = emptyMap(),  // userId -> upvoted(true)/downvoted(false)
    val parentCommentId: String? = null  // null for top-level comments, commentId for replies
)
