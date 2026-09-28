package com.abpvt.newsapp.ui.news

import androidx.lifecycle.ViewModel
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.data.model.StoryCluster
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Lightweight nav-graph-scoped ViewModel that acts as a "hand-off" between
 * the news feed and the reader screen.
 *
 * When the user taps an article card, NewsScreen stores the full Article object
 * here before navigating to ArticleView. ArticleScreen then reads it back —
 * no URL encoding, no size limits, no extra network call.
 */
@HiltViewModel
class SelectedArticleViewModel @Inject constructor() : ViewModel() {

    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle: StateFlow<Article?> = _selectedArticle

    private val _selectedCluster = MutableStateFlow<StoryCluster?>(null)
    val selectedCluster: StateFlow<StoryCluster?> = _selectedCluster

    fun select(article: Article) {
        _selectedArticle.value = article
    }

    fun selectCluster(cluster: StoryCluster) {
        _selectedCluster.value = cluster
    }

    fun clear() {
        _selectedArticle.value = null
    }
}
