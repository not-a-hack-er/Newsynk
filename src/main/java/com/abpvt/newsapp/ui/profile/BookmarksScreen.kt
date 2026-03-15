package com.abpvt.newsapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.news.NewsItem

@Composable
fun BookmarksScreen(
    navController: NavHostController,
    bookmarksViewModel: BookmarksViewModel = viewModel()
) {
    val bookmarks by bookmarksViewModel.bookmarks.collectAsState()
    val isLoading by bookmarksViewModel.isLoading.collectAsState()
    val error by bookmarksViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Articles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 4.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                bookmarks.isEmpty() -> {
                    Text(
                        text = "No saved articles found. articles you bookmark will appear here.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
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
