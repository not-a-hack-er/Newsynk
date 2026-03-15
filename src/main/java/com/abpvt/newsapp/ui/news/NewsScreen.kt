package com.abpvt.newsapp.ui.news

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.ScreenBackground

/**
 * Composable screen that shows a list of news articles.
 *
 * Navigation:
 * - On article click we navigate to comments screen using the article's URL encoded as id.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel = viewModel()
) {
    val articles by newsViewModel.articles.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsState()

    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { newsViewModel.refresh() }
    )

    LaunchedEffect(Unit) {
        Log.d("NewsScreen", "NewsScreen composed")
    }

    LaunchedEffect(articles, isLoading, error) {
        Log.d("NewsScreen", "State - Loading: $isLoading, Articles: ${articles.size}, Error: $error")
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                color = MaterialTheme.colors.primary,
                elevation = 4.dp
            ) {
                TopAppBar(
                    title = { Text(text = "Top Headlines") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    elevation = 0.dp
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (error != null) {
                    Text(
                        text = "Error: ${error ?: "Unknown error"}",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (!isLoading && articles.isEmpty() && error == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No articles available",
                            style = MaterialTheme.typography.h6
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(articles) { article ->
                        NewsItem(
                            article = article,
                            onArticleClick = {
                                val articleUrl = Uri.encode(article.url)
                                navController.navigate(Screen.ArticleView.createRoute(articleUrl))
                            },
                            onCommentClick = {
                                val articleId = Uri.encode(article.url)
                                navController.navigate(Screen.Comments.createRoute(articleId))
                            }
                        )
                    }
                }
            }

            // Pull refresh indicator
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
