package com.abpvt.newsapp.ui.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.remote.RetrofitClient
import com.abpvt.newsapp.data.repository.NewsRepository
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for loading top headlines and exposing UI state.
 * Assumes NewsRepository.getTopHeadlines(country) returns Resource<List<Article>>.
 */
class NewsViewModel(
    private val repository: NewsRepository = NewsRepository(RetrofitClient.newsApiService)
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        Log.d("NewsViewModel", "ViewModel initialized, fetching headlines...")
        fetchTopHeadlines()
    }

    /** Fetch latest news for the given query/topic (default: "latest news, india"). */
    fun fetchTopHeadlines(query: String = "latest news, india") {
        viewModelScope.launch {
            Log.d("NewsViewModel", "Starting fetch for query: $query")
            _isLoading.value = true
            _error.value = null
            try {
                // Use getLatestNews to get fresher content sorted by time
                val res = repository.getLatestNews(query)
                Log.d("NewsViewModel", "Repository response: ${res.javaClass.simpleName}")
                
                when (res) {
                    is Resource.Success -> {
                        val articleList = res.data ?: emptyList()
                        Log.d("NewsViewModel", "Success! Received ${articleList.size} articles")
                        _articles.value = articleList
                        
                        if (articleList.isNotEmpty()) {
                            Log.d("NewsViewModel", "First article: ${articleList[0].title}")
                        }
                    }
                    is Resource.Error -> {
                        val errorMsg = res.message ?: "Unknown error while fetching news"
                        Log.e("NewsViewModel", "Error: $errorMsg")
                        _error.value = errorMsg
                    }
                    is Resource.Loading -> {
                        Log.d("NewsViewModel", "Loading state received")
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "An unexpected error occurred"
                Log.e("NewsViewModel", "Exception: $errorMsg", e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
                Log.d("NewsViewModel", "Fetch completed. Loading: false, Articles: ${_articles.value.size}, Error: ${_error.value}")
            }
        }
    }

    /** Manual refresh invocation (for pull-to-refresh etc.). */
    fun refresh() {
        Log.d("NewsViewModel", "Manual refresh triggered")
        fetchTopHeadlines()
    }
}
