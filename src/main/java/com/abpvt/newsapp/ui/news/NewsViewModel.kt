package com.abpvt.newsapp.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.repository.NewsRepository
import com.abpvt.newsapp.data.repository.PersonalizationRepository
import com.abpvt.newsapp.data.model.StoryCluster
import com.abpvt.newsapp.utils.StoryClusterer
import com.abpvt.newsapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val personalizationRepository: PersonalizationRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _clusters = MutableStateFlow<List<StoryCluster>>(emptyList())
    val clusters: StateFlow<List<StoryCluster>> = _clusters

    val followedTopics = personalizationRepository.followedTopics
    val onboardingComplete = personalizationRepository.onboardingComplete
    val recentSearches = personalizationRepository.recentSearches

    private var currentPage = 1
    private var hasMorePages = true
    private var fetchJob: Job? = null

    init {
        // Observe search query with debounce
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            _searchQuery
                .debounce(500)
                .collectLatest { query ->
                    if (query.trim().isNotEmpty()) {
                        fetchNews(query = query, category = null, isRefresh = true)
                    } else if (_isSearchActive.value) {
                        fetchNews(query = null, category = _selectedCategory.value, isRefresh = true)
                    }
                }
        }

        fetchNews(category = "All", isRefresh = true)
    }

    fun selectCategory(category: String) {
        if (_selectedCategory.value == category && !_isSearchActive.value) return
        _selectedCategory.value = category
        _isSearchActive.value = false
        _searchQuery.value = ""
        fetchNews(query = null, category = category, isRefresh = true)
    }

    fun activateSearch() {
        _isSearchActive.value = true
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun submitSearch(query: String = _searchQuery.value) {
        personalizationRepository.rememberSearch(query)
        if (query.isNotBlank()) {
            _searchQuery.value = query
            fetchNews(query = query, category = null, isRefresh = true)
        }
    }

    fun clearRecentSearches() = personalizationRepository.clearRecentSearches()

    fun toggleTopic(topic: String) = personalizationRepository.toggleTopic(topic)

    fun completeOnboarding() {
        personalizationRepository.completeOnboarding()
        selectCategory("For You")
    }

    fun closeSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        fetchNews(query = null, category = _selectedCategory.value, isRefresh = true)
    }

    fun loadNextPage() {
        if (_isLoading.value || _isPaginating.value || !hasMorePages) return
        val query = if (_isSearchActive.value) _searchQuery.value else null
        val category = if (!_isSearchActive.value) _selectedCategory.value else null
        fetchNews(query = query, category = category, isRefresh = false)
    }

    fun refresh() {
        val query = if (_isSearchActive.value) _searchQuery.value else null
        val category = if (!_isSearchActive.value) _selectedCategory.value else null
        fetchNews(query = query, category = category, isRefresh = true)
    }

    private fun fetchNews(query: String? = null, category: String? = null, isRefresh: Boolean = true) {
        if (isRefresh) fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            if (isRefresh) {
                currentPage = 1
                hasMorePages = true
                _isLoading.value = true
            } else {
                _isPaginating.value = true
            }
            _error.value = null

            try {
                val res = when {
                    !query.isNullOrBlank() -> repository.getLatestNews(query.trim(), currentPage)
                    category.equals("For You", ignoreCase = true) ->
                        repository.getPersonalizedNews(followedTopics.value, currentPage)
                    !category.isNullOrBlank() -> repository.getCategoryNews(category, currentPage)
                    else -> repository.getTopHeadlines(currentPage)
                }

                when (res) {
                    is Resource.Success -> {
                        val newArticles = res.data ?: emptyList()
                        if (newArticles.isEmpty() || newArticles.size < 4) {
                            hasMorePages = false
                        }

                        if (isRefresh) {
                            _articles.value = newArticles
                        } else {
                            val existingSet = _articles.value.map { it.url }.toSet()
                            val uniqueNew = newArticles.filter { it.url !in existingSet }
                            _articles.value = _articles.value + uniqueNew
                        }
                        _clusters.value = StoryClusterer.cluster(_articles.value)
                        currentPage++
                    }
                    is Resource.Error -> {
                        val errorMsg = res.message ?: "Unknown error while fetching news"
                        if (isRefresh) _error.value = errorMsg
                        hasMorePages = false
                    }
                    is Resource.Loading -> { /* no-op */ }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (isRefresh) _error.value = e.localizedMessage ?: "An unexpected error occurred"
                hasMorePages = false
            } finally {
                if (isActive) {
                    _isLoading.value = false
                    _isPaginating.value = false
                }
            }
        }
    }
}
