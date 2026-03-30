package com.abpvt.newsapp.ui.news

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.BuildConfig
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.remote.RetrofitClient
import com.abpvt.newsapp.data.repository.NewsRepository
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for loading top headlines and exposing UI state.
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
        if (BuildConfig.DEBUG) Log.d("NewsViewModel", "ViewModel initialized, fetching headlines...")
        fetchTopHeadlines()
    }

    fun fetchTopHeadlines(query: String = "india") {
        viewModelScope.launch {
            if (BuildConfig.DEBUG) Log.d("NewsViewModel", "Starting fetch for query: $query")
            _isLoading.value = true
            _error.value = null
            try {
                val res = repository.getLatestNews(query)
                when (res) {
                    is Resource.Success -> {
                        val articleList = res.data ?: emptyList()
                        if (BuildConfig.DEBUG) Log.d("NewsViewModel", "Success! ${articleList.size} articles")
                        _articles.value = articleList
                    }
                    is Resource.Error -> {
                        val errorMsg = res.message ?: "Unknown error while fetching news"
                        if (BuildConfig.DEBUG) Log.e("NewsViewModel", "Error: $errorMsg")
                        _error.value = errorMsg
                    }
                    is Resource.Loading -> { /* no-op */ }
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "An unexpected error occurred"
                if (BuildConfig.DEBUG) Log.e("NewsViewModel", "Exception: $errorMsg", e)
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        fetchTopHeadlines()
    }
}
