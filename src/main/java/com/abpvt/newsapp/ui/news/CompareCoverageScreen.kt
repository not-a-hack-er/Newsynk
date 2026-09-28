package com.abpvt.newsapp.ui.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareCoverageScreen(
    navController: NavHostController,
    selectedArticleViewModel: SelectedArticleViewModel
) {
    val cluster by selectedArticleViewModel.selectedCluster.collectAsState()
    val current = cluster

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare coverage") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (current == null) {
            Text("This story group is no longer available.", modifier = Modifier.padding(padding).padding(24.dp))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    current.primary.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "${current.sourceCount} independent sources are covering this story. Open each report to compare framing and detail.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(current.allArticles, key = { it.url }) { article ->
                NewsItem(
                    article = article,
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
