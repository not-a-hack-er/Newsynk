package com.abpvt.newsapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abpvt.newsapp.navigation.AppNavGraph
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.navigation.DeepLinkRouter
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.abpvt.newsapp.ui.theme.NewsappTheme
import com.abpvt.newsapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint

private const val PREFS_THEME    = "newsapp_theme"
private const val KEY_DARK_MODE  = "dark_mode"
private const val KEY_DARK_MODE_SET = "dark_mode_set"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read deep-link from notification intent (if any)
        val deepLinkArticleUrl = intent?.getStringExtra(NotificationsPrefs.EXTRA_ARTICLE_URL)
        val deepLinkArticleTitle = intent?.getStringExtra(NotificationsPrefs.EXTRA_ARTICLE_TITLE)

        setContent {
            NewsynkApp(
                deepLinkArticleUrl = deepLinkArticleUrl,
                deepLinkArticleTitle = deepLinkArticleTitle
            )
        }
    }
}

@Composable
fun NewsynkApp(
    deepLinkArticleUrl: String? = null,
    deepLinkArticleTitle: String? = null
) {
    val context        = LocalContext.current
    val prefs          = remember { context.getSharedPreferences(PREFS_THEME, Context.MODE_PRIVATE) }
    val sessionManager = remember { SessionManager(context.applicationContext) }

    val systemDark = isSystemInDarkTheme()
    var isDarkMode by remember {
        mutableStateOf(
            if (prefs.getBoolean(KEY_DARK_MODE_SET, false))
                prefs.getBoolean(KEY_DARK_MODE, false)
            else systemDark
        )
    }

    val toggleDarkMode = {
        isDarkMode = !isDarkMode
        prefs.edit()
            .putBoolean(KEY_DARK_MODE, isDarkMode)
            .putBoolean(KEY_DARK_MODE_SET, true)
            .apply()
    }

    // Always create the News destination before opening a notification article.
    // ArticleView shares state with the News back-stack entry, so making ArticleView
    // the initial destination would leave that owner missing and could crash.
    val startDestination = remember(sessionManager) {
        DeepLinkRouter.startDestination(sessionManager.isSessionValid())
    }

    NewsappTheme(darkTheme = isDarkMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route
            var deepLinkHandled by remember(deepLinkArticleUrl) { mutableStateOf(false) }
            AppNavGraph(
                navController      = navController,
                startDestination   = startDestination,
                isDarkMode         = isDarkMode,
                onToggleDarkMode   = toggleDarkMode
            )

            LaunchedEffect(deepLinkArticleUrl, deepLinkArticleTitle, currentRoute) {
                if (DeepLinkRouter.shouldOpenArticle(currentRoute, deepLinkArticleUrl, deepLinkHandled)) {
                    val articleUrl = deepLinkArticleUrl ?: return@LaunchedEffect
                    deepLinkHandled = true
                    navController.navigate(
                        Screen.ArticleView.createRoute(
                            url = articleUrl,
                            title = deepLinkArticleTitle ?: "Newsynk article"
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
