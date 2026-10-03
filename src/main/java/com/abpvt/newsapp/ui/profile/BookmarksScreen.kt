package com.abpvt.newsapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.news.NewsItem
import com.abpvt.newsapp.ui.news.SelectedArticleViewModel
import com.abpvt.newsapp.ui.theme.GradientStart
import com.abpvt.newsapp.ui.components.PremiumBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    navController: NavHostController,
    selectedArticleViewModel: SelectedArticleViewModel,
    bookmarksViewModel: BookmarksViewModel = hiltViewModel()
) {
    val bookmarks by bookmarksViewModel.bookmarks.collectAsState()
    val isLoading by bookmarksViewModel.isLoading.collectAsState()
    val error by bookmarksViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Saved stories", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GradientStart)
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
                    SavedState(
                        title = "Your reading list is unavailable",
                        message = "Check your connection and try again.",
                        buttonText = "Try again",
                        onAction = bookmarksViewModel::fetchBookmarks,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                bookmarks.isEmpty() -> {
                    SavedState(
                        title = "A place for stories worth keeping",
                        message = "Bookmark an article and it will appear here whenever you need it.",
                        buttonText = "Explore stories",
                        onAction = { navController.navigate(Screen.Explore.route) },
                        modifier = Modifier.align(Alignment.Center)
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
                                    selectedArticleViewModel.select(article)
                                    navController.navigate(Screen.ArticleView.createRoute(article.url, article.title))
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

@Composable
private fun SavedState(
    title: String,
    message: String,
    buttonText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Button(onClick = onAction, shape = RoundedCornerShape(14.dp)) { Text(buttonText) }
        }
    }
}
