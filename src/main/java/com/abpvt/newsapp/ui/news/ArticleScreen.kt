package com.abpvt.newsapp.ui.news

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.abpvt.newsapp.data.model.Article
import com.abpvt.newsapp.navigation.Screen

@Composable
fun ArticleScreen(
    url: String,
    navController: NavHostController,
    onCommentClick: () -> Unit,
    interactionViewModel: ArticleInteractionViewModel = viewModel(),
    articleTitle: String? = null,
    articleSource: String? = null
) {
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Get interaction state
    val interactionStates by interactionViewModel.interactionStates.collectAsState()
    val interactionState = interactionStates[url]

    // Fetch on initial load
    LaunchedEffect(url) {
        interactionViewModel.loadInteractions(url)
    }

    val isBookmarked = interactionState?.isBookmarked == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Bookmark Button
                    IconButton(
                        onClick = { 
                            // Construct a minimal dummy Article for saving if needed, though we primarily just need the URL.
                            val dummyArticle = Article(
                                source = Article.Source(id = null, name = articleSource ?: "Unknown"),
                                author = null,
                                title = articleTitle ?: "Saved Article",
                                description = null,
                                url = url,
                                urlToImage = null,
                                publishedAt = "",
                                content = null
                            )
                            interactionViewModel.toggleBookmark(dummyArticle) 
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colors.primary else MaterialTheme.colors.onPrimary
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${articleTitle ?: "Article"}\n$url")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    // Comment Button
                    IconButton(onClick = onCommentClick) {
                        Text(
                            text = "💬",
                            style = MaterialTheme.typography.body1
                        )
                    }
                },
                elevation = 4.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = true
                        loadUrl(url)
                    }
                },
                update = { webView ->
                    // Optionally handle updates here
                }
            )

            // Show a simple loading indicator while the page loads
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxSize().padding(top = 0.dp))
            }
        }
    }
}
