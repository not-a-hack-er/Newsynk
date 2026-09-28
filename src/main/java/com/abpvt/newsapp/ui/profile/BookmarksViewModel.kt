package com.abpvt.newsapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.repository.InteractionRepository
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _bookmarks = MutableStateFlow<List<Article>>(emptyList())
    val bookmarks: StateFlow<List<Article>> = _bookmarks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchBookmarks()
    }

    fun fetchBookmarks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            when (val result = interactionRepository.getBookmarkedArticles()) {
                is Resource.Success -> {
                    _bookmarks.value = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> { /* Loading state handled */ }
            }
            
            _isLoading.value = false
        }
    }
}
