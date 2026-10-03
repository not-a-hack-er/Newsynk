package com.abpvt.newsapp.ui.news

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.R
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NewsScreen(
    navController: NavHostController,
    newsViewModel: NewsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel? = null,
    selectedArticleViewModel: SelectedArticleViewModel = hiltViewModel()
) {
    val articles by newsViewModel.articles.collectAsState()
    val isLoading by newsViewModel.isLoading.collectAsState()
    val isPaginating by newsViewModel.isPaginating.collectAsState()
    val error by newsViewModel.error.collectAsState()
    val clusters by newsViewModel.clusters.collectAsState()
    val followedTopics by newsViewModel.followedTopics.collectAsState()
    val onboardingComplete by newsViewModel.onboardingComplete.collectAsState()
    val recentSearches by newsViewModel.recentSearches.collectAsState()

    val searchQuery by newsViewModel.searchQuery.collectAsState()
    val isSearchActive by newsViewModel.isSearchActive.collectAsState()

    val userName by (authViewModel?.currentUserName
        ?: kotlinx.coroutines.flow.MutableStateFlow("")).collectAsState()

    val greeting = if (userName.isNotBlank()) "Hi, $userName 👋" else ""

    val pullRefreshState = rememberPullToRefreshState()

    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Trigger loadNextPage when we scroll near the bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            newsViewModel.loadNextPage()
        }
    }

    val selectedCategory by newsViewModel.selectedCategory.collectAsState()

    if (!onboardingComplete) {
        com.abpvt.newsapp.ui.news.components.PersonalizationDialog(
            selectedTopics = followedTopics,
            onToggleTopic = newsViewModel::toggleTopic,
            onComplete = newsViewModel::completeOnboarding,
            onSkip = newsViewModel::completeOnboarding
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(colors = listOf(GradientStart, GradientEnd)))
            ) {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        if (isSearchActive) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { newsViewModel.onSearchQueryChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp),
                                placeholder = {
                                    Text(
                                        text = "Search news...",
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    newsViewModel.submitSearch()
                                    keyboardController?.hide()
                                }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                    cursorColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF060515)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_foreground),
                                        contentDescription = null,
                                        modifier = Modifier.size(38.dp).graphicsLayer(scaleX = 1.7f, scaleY = 1.7f)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Newsynk",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = greeting.ifEmpty { "Your world, in focus" },
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (isSearchActive) {
                            IconButton(onClick = { newsViewModel.closeSearch() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = { newsViewModel.activateSearch() }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (!isSearchActive) {
                    com.abpvt.newsapp.ui.news.components.TrendingTickerBar(
                        onTopicClick = { topic ->
                            newsViewModel.activateSearch()
                            newsViewModel.onSearchQueryChanged(topic)
                        }
                    )
                }
            }
        },
        bottomBar = {
            com.abpvt.newsapp.ui.components.PremiumBottomBar(navController, Screen.News.route)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (!isSearchActive) {
                com.abpvt.newsapp.ui.news.components.CategoryFilterBar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        newsViewModel.selectCategory(category)
                    }
                )
            }

            if (isSearchActive && searchQuery.isBlank() && recentSearches.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSearches) { term ->
                        AssistChip(
                            onClick = {
                                newsViewModel.onSearchQueryChanged(term)
                                newsViewModel.submitSearch(term)
                            },
                            label = { Text(term) }
                        )
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { newsViewModel.refresh() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                Crossfade(targetState = isLoading to articles, animationSpec = tween(400), label = "news_crossfade") { (loading, list) ->
                    Column(modifier = Modifier.fillMaxSize()) {
                    if (loading && list.isEmpty()) {
                        com.abpvt.newsapp.ui.news.components.FeedSkeleton(
                            modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                        )
                        return@Column
                    }
                    if (error != null && list.isEmpty()) {
                        EmptyNewsState(
                            title = if (isSearchActive) "No matching stories yet" else "Stories are temporarily unavailable",
                            message = if (isSearchActive)
                                "Try a broader search. Keyless mode searches recent stories from our public RSS sources."
                            else "Check your connection and try again. Recent stories will appear here when a source responds.",
                            onRetry = newsViewModel::refresh
                        )
                        return@Column
                    }

                    if (!loading && list.isEmpty() && error == null) {
                        EmptyNewsState(
                            title = if (isSearchActive) "Search for a story" else "Nothing new just yet",
                            message = if (isSearchActive) "Explore a topic, person, or publisher." else "Pull down to refresh the latest coverage.",
                            onRetry = newsViewModel::refresh
                        )
                        return@Column
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (list.isNotEmpty() && !isSearchActive) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "YOUR DAILY BRIEFING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    )
                                    Text(
                                        text = if (selectedCategory == "For You") "Selected for you" else "The latest stories",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Fresh perspectives from across the news.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        items(list, key = { it.url }) { article ->
                            val cluster = clusters.firstOrNull { story ->
                                story.allArticles.any { clustered -> clustered.url == article.url }
                            }
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
                                onCommentClick = {
                                    navController.navigate(Screen.Comments.createRoute(article.url))
                                }
                            )
                        }

                        if (isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = DeepBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun EmptyNewsState(title: String, message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(14.dp).size(26.dp))
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(message, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Button(onClick = onRetry, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try again")
                }
            }
        }
    }
}
