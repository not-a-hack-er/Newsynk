package com.abpvt.newsapp.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.repository.ArticleInteractionState
import com.abpvt.newsapp.data.repository.InteractionRepository
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArticleInteractionViewModel(
    private val repository: InteractionRepository = InteractionRepository()
) : ViewModel() {

    // Map of article URL to its interaction state
    private val _interactionStates = MutableStateFlow<Map<String, ArticleInteractionState>>(emptyMap())
    val interactionStates: StateFlow<Map<String, ArticleInteractionState>> = _interactionStates

    fun loadInteractions(articleUrl: String) {
        viewModelScope.launch {
            val result = repository.getInteractionState(articleUrl)
            if (result is Resource.Success) {
                result.data?.let { state ->
                    _interactionStates.update { currentMap ->
                        currentMap.toMutableMap().apply {
                            put(articleUrl, state)
                        }
                    }
                }
            }
        }
    }

    fun vote(articleUrl: String, isUpvote: Boolean) {
        // Optimistic update
        val currentState = _interactionStates.value[articleUrl] ?: ArticleInteractionState()
        
        val newVote = if (currentState.userVote == isUpvote) null else isUpvote
        var newUpvotes = currentState.upvotes
        var newDownvotes = currentState.downvotes

        // Remove old vote
        if (currentState.userVote == true) newUpvotes--
        else if (currentState.userVote == false) newDownvotes--

        // Add new vote
        if (newVote == true) newUpvotes++
        else if (newVote == false) newDownvotes++

        val optimisticState = currentState.copy(
            upvotes = newUpvotes,
            downvotes = newDownvotes,
            userVote = newVote
        )

        _interactionStates.update { it.toMutableMap().apply { put(articleUrl, optimisticState) } }

        // Actual network call
        viewModelScope.launch {
            val result = repository.voteArticle(articleUrl, isUpvote)
            if (result is Resource.Success) {
                result.data?.let { serverState ->
                    _interactionStates.update { 
                        it.toMutableMap().apply { 
                            put(articleUrl, serverState.copy(isBookmarked = optimisticState.isBookmarked)) 
                        } 
                    }
                }
            } else {
                // Revert on failure
                _interactionStates.update { it.toMutableMap().apply { put(articleUrl, currentState) } }
            }
        }
    }

    fun toggleBookmark(article: Article) {
        val currentState = _interactionStates.value[article.url] ?: ArticleInteractionState()
        val optimisticState = currentState.copy(isBookmarked = !currentState.isBookmarked)

        _interactionStates.update { it.toMutableMap().apply { put(article.url, optimisticState) } }

        viewModelScope.launch {
            val result = repository.toggleBookmark(article.url, article)
            if (result is Resource.Success) {
                result.data?.let { isBookmarked ->
                    _interactionStates.update { 
                        it.toMutableMap().apply { 
                            put(article.url, optimisticState.copy(isBookmarked = isBookmarked)) 
                        } 
                    }
                }
            } else {
                 // Revert on failure
                _interactionStates.update { it.toMutableMap().apply { put(article.url, currentState) } }
            }
        }
    }
}
