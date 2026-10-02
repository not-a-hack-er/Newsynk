package com.abpvt.newsapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abpvt.newsapp.navigation.AppNavGraph
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.navigation.DeepLinkRouter
import com.abpvt.newsapp.notifications.NotificationsPrefs
import com.abpvt.newsapp.ui.theme.NewsappTheme
import com.abpvt.newsapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.FirebaseApp

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
    val firebaseConfigured = remember(context) { FirebaseApp.getApps(context).isNotEmpty() }

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
        DeepLinkRouter.startDestination(firebaseConfigured && sessionManager.isSessionValid())
    }

    NewsappTheme(darkTheme = isDarkMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            if (!firebaseConfigured) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Newsynk needs Firebase setup", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "This build has no Firebase configuration. Add the project's google-services.json and rebuild the app to use sign-in.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                return@Surface
            }
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
