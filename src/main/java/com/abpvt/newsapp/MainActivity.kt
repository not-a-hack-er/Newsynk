package com.abpvt.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.abpvt.newsapp.navigation.AppNavGraph
import com.abpvt.newsapp.navigation.Screen
import com.abpvt.newsapp.ui.theme.NewsappTheme
import com.abpvt.newsapp.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsApp()
        }
    }
}

@Composable
fun NewsApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val startDestination = remember(sessionManager) {
        if (sessionManager.isSessionValid()) {
            Screen.News.route
        } else {
            Screen.Login.route
        }
    }
    
    NewsappTheme {
        Surface(color = MaterialTheme.colors.background) {
            val navController = rememberNavController()
            AppNavGraph(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}
