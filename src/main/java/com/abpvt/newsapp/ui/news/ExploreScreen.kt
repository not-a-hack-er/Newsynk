package com.abpvt.newsapp.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.data.repository.PersonalizationRepository
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.components.PremiumBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavHostController,
    selectedArticleViewModel: SelectedArticleViewModel,
    viewModel: NewsViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val recent by viewModel.recentSearches.collectAsState()
    val topics by viewModel.followedTopics.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val clusters by viewModel.clusters.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = { PremiumBottomBar(navController, Screen.Explore.route) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextField(
                    value = query,
                    onValueChange = {
                        viewModel.activateSearch()
                        viewModel.onSearchQueryChanged(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search topics, sources or keywords") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.submitSearch() })
                )
            }

            if (query.isBlank() && recent.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recent searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            recent.forEach { term ->
                                AssistChip(
                                    onClick = {
                                        viewModel.activateSearch()
                                        viewModel.onSearchQueryChanged(term)
                                        viewModel.submitSearch(term)
                                    },
                                    label = { Text(term) },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your topics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Follow topics to tune the For You feed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PersonalizationRepository.AVAILABLE_TOPICS.forEach { topic ->
                            FilterChip(
                                selected = topic in topics,
                                onClick = { viewModel.toggleTopic(topic) },
                                label = { Text(topic) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    if (query.isBlank()) "Discover now" else "Search results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            items(articles, key = { it.url }) { article ->
                val cluster = clusters.firstOrNull { it.allArticles.any { item -> item.url == article.url } }
                NewsItem(
                    article = article,
                    coverageCount = cluster?.sourceCount ?: 1,
                    onCoverageClick = {
                        if (cluster != null) {
                            selectedArticleViewModel.selectCluster(cluster)
                            navController.navigate(Screen.CompareCoverage.route)
                        }
                    },
                    onArticleClick = {
                        selectedArticleViewModel.select(article)
                        navController.navigate(Screen.ArticleView.createRoute(article.url, article.title))
                    },
                    onCommentClick = { navController.navigate(Screen.Comments.createRoute(article.url)) }
                )
            }
        }
    }
}
