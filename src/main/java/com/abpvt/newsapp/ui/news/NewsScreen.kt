package com.abpvt.newsapp.ui.news

import android.net.Uri
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel = viewModel(),
    authViewModel: AuthViewModel? = null
) {
    val articles by newsViewModel.articles.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val error by newsViewModel.error.collectAsState()

    // Collect user name safely — authViewModel is optional
    val userName by (authViewModel?.currentUserName
        ?: kotlinx.coroutines.flow.MutableStateFlow("")).collectAsState()

    // Derive greeting label
    val greeting = if (userName.isNotBlank()) "Hi, $userName 👋" else ""

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { newsViewModel.refresh() }
    )

    LaunchedEffect(Unit) { Log.d("NewsScreen", "NewsScreen composed") }
    LaunchedEffect(articles, isLoading, error) {
        Log.d("NewsScreen", "State - Loading: $isLoading, Articles: ${articles.size}, Error: $error")
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd))
                    )
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Column {
                            Text(
                                text = "Newsynk",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (greeting.isNotEmpty()) {
                                Text(
                                    text = greeting,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White
                            )
                        }
                    },
                    backgroundColor = Color.Transparent,
                    contentColor = Color.White,
                    elevation = 0.dp
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            Crossfade(targetState = isLoading to articles, animationSpec = tween(400), label = "news_crossfade") { (loading, list) ->
                Column(modifier = Modifier.fillMaxSize()) {
                    if (error != null) {
                        Text(
                            text = "Error: ${error ?: "Unknown error"}",
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (!loading && list.isEmpty() && error == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No articles available", style = MaterialTheme.typography.h6)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(list) { article ->
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
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
