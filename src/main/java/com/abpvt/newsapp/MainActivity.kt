package com.abpvt.newsapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.abpvt.newsapp.navigation.AppNavGraph
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.abpvt.newsapp.ui.theme.NewsappTheme
import com.abpvt.newsapp.utils.SessionManager

private const val PREFS_THEME    = "newsapp_theme"
private const val KEY_DARK_MODE  = "dark_mode"
private const val KEY_DARK_MODE_SET = "dark_mode_set"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read deep-link from notification intent (if any)
        val deepLinkArticleUrl = intent?.getStringExtra(NotificationsPrefs.EXTRA_ARTICLE_URL)

        setContent {
            NewsynkApp(deepLinkArticleUrl = deepLinkArticleUrl)
        }
    }
}

@Composable
fun NewsynkApp(deepLinkArticleUrl: String? = null) {
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

    // If a deep-link article URL came from a notification and the user is logged in,
    // navigate straight to the article view, otherwise go to normal start.
    val startDestination = remember(sessionManager, deepLinkArticleUrl) {
        when {
            !sessionManager.isSessionValid() -> Screen.Login.route
            !deepLinkArticleUrl.isNullOrBlank() ->
                Screen.ArticleView.createRoute(Uri.encode(deepLinkArticleUrl))
            else -> Screen.News.route
        }
    }

    NewsappTheme(darkTheme = isDarkMode) {
        Surface(color = MaterialTheme.colors.background) {
            val navController = rememberNavController()
            AppNavGraph(
                navController      = navController,
                startDestination   = startDestination,
                isDarkMode         = isDarkMode,
                onToggleDarkMode   = toggleDarkMode
            )
        }
    }
}
