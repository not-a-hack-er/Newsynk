package com.abpvt.newsapp.ui.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.data.model.Comment
import com.abpvt.newsapp.data.repository.CommentRepository
import com.abpvt.newsapp.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()


    // UI state flows
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Reply state - tracks which comment is being replied to
    private val _replyingTo = MutableStateFlow<Comment?>(null)
    val replyingTo: StateFlow<Comment?> = _replyingTo

    // Keep track of which article's comments are loaded
    private var currentArticleId: String? = null
    private var lastPostAt: Long = 0L

    /** Load comments for the given article and update state flows. */
    fun loadComments(articleId: String) {
        currentArticleId = articleId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val result = commentRepository.getCommentsForArticle(articleId)) {
                    is Resource.Success -> {
                        _comments.value = result.data ?: emptyList()
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Failed to load comments"
                    }
                    is Resource.Loading -> {
                        // Already handling loading state
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Post a new comment or reply for the article, then refresh comments. */
    fun postComment(articleId: String, text: String) {
        viewModelScope.launch {
            val cleaned = text.trim()
            if (cleaned.length > 1000) {
                _error.value = "Comments are limited to 1,000 characters"
                return@launch
            }
            if (System.currentTimeMillis() - lastPostAt < 10_000L) {
                _error.value = "Please wait a few seconds before posting again"
                return@launch
            }
            _isLoading.value = true
            _error.value = null
            try {
                // Get the current authenticated user
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "You must be logged in to comment"
                    _isLoading.value = false
                    return@launch
                }
                
                // Create a Comment object (with parentCommentId if replying)
                val comment = Comment(
                    articleId = articleId,
                    userId = currentUser.uid,
                    content = cleaned,
                    timestamp = System.currentTimeMillis(),
                    parentCommentId = _replyingTo.value?.id  // null if not replying
                )
                
                when (val result = commentRepository.addComment(comment)) {
                    is Resource.Success -> {
                        lastPostAt = System.currentTimeMillis()
                        // Clear reply state and reload comments after successful post
                        _replyingTo.value = null
                        loadComments(articleId)
                    }
                    is Resource.Error -> {
                        _error.value = result.message ?: "Failed to post comment"
                    }
                    is Resource.Loading -> {
                        // Already handling loading state
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to post comment"
            } finally {
                _isLoading.value = false
            }
        }
    }


    /** Cast a vote (upvote/downvote) on a comment with optimistic UI update. */
    fun vote(commentId: String, isUpvote: Boolean) {
        viewModelScope.launch {
            try {
                // Get the current authenticated user
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _error.value = "You must be logged in to vote"
                    return@launch
                }
                
                // Optimistic update: Update the comment locally first
                val currentComments = _comments.value
                val commentIndex = currentComments.indexOfFirst { it.id == commentId }
                if (commentIndex != -1) {
                    val comment = currentComments[commentIndex]
                    val updatedVoters = comment.voters.toMutableMap()
                    val previousVote = updatedVoters[currentUser.uid]
                    
                    if (previousVote == isUpvote) {
                        updatedVoters.remove(currentUser.uid)
                    } else {
                        updatedVoters[currentUser.uid] = isUpvote
                    }
                    
                    val updatedComment = comment.copy(voters = updatedVoters)
                    val updatedList = currentComments.toMutableList()
                    updatedList[commentIndex] = updatedComment
                    _comments.value = updatedList
                }
                
                // Then update on server (no need to reload all comments)
                when (val result = commentRepository.voteComment(commentId, currentUser.uid, isUpvote)) {
                    is Resource.Success -> {
                        // Server update successful, optimistic update already applied
                    }
                    is Resource.Error -> {
                        // Revert optimistic update on error
                        currentArticleId?.let { loadComments(it) }
                        _error.value = result.message ?: "Failed to submit vote"
                    }
                    is Resource.Loading -> {
                        // No action needed
                    }
                }
            } catch (e: Exception) {
                // Revert optimistic update on error
                currentArticleId?.let { loadComments(it) }
                _error.value = e.message ?: "Failed to submit vote"
            }
        }
    }
    
    /** Set which comment is being replied to. */
    fun setReplyingTo(comment: Comment) {
        _replyingTo.value = comment
    }
    
    /** Clear the reply state. */
    fun clearReply() {
        _replyingTo.value = null
    }

    fun report(comment: Comment) {
        viewModelScope.launch {
            when (val result = commentRepository.reportComment(comment.id, "User reported inappropriate content")) {
                is Resource.Success -> _error.value = "Thanks. The comment was reported for review."
                is Resource.Error -> _error.value = result.message
                else -> Unit
            }
        }
    }

    fun block(comment: Comment) {
        commentRepository.blockUser(comment.userId)
        _comments.value = _comments.value.filterNot { it.userId == comment.userId }
    }
}
