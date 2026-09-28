package com.abpvt.newsapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.news.NewsItem
import com.abpvt.newsapp.ui.theme.DeepBlue
import com.abpvt.newsapp.ui.components.PremiumBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavHostController,
    bookmarksViewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by bookmarksViewModel.bookmarks.collectAsState()
    val isLoading by bookmarksViewModel.isLoading.collectAsState()
    val error by bookmarksViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Saved Articles", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlue)
            )
        },
        bottomBar = { PremiumBottomBar(navController, Screen.Bookmarks.route) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                bookmarks.isEmpty() -> {
                    Text(
                        text = "No saved articles found.\nArticles you bookmark will appear here.",
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookmarks) { article ->
                            NewsItem(
                                article = article,
                                onArticleClick = {
                                    navController.navigate(Screen.ArticleView.createRoute(article.url))
                                },
                                onCommentClick = {
                                    navController.navigate(Screen.Comments.createRoute(article.url))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
