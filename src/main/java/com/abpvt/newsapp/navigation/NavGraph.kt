package com.abpvt.newsapp.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.auth.AuthViewModelFactory
import com.abpvt.newsapp.auth.LoginScreen
import com.abpvt.newsapp.auth.RegisterScreen
import com.abpvt.newsapp.ui.news.NewsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object News : Screen("news")
    object Comments : Screen("comments/{articleId}") {
        fun createRoute(articleId: String) = "comments/$articleId"
    }
    object Profile : Screen("profile")
    object ArticleView : Screen("articleView/{articleUrl}") {
        fun createRoute(articleUrl: String) = "articleView/$articleUrl"
    }
    object Bookmarks : Screen("bookmarks")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    // Single shared AuthViewModel for the entire nav graph
    val application = LocalContext.current.applicationContext as Application
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(application)
    )

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.News.route) {
            // Pass authViewModel so NewsScreen can show the user's name
            NewsScreen(navController, authViewModel = authViewModel)
        }
        composable(Screen.Comments.route) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            com.abpvt.newsapp.ui.comments.CommentScreen(
                articleId = articleId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            com.abpvt.newsapp.ui.profile.ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        }
        composable(Screen.ArticleView.route) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            com.abpvt.newsapp.ui.news.ArticleScreen(
                url = articleUrl,
                navController = navController,
                onCommentClick = {
                    navController.navigate(Screen.Comments.createRoute(articleUrl))
                }
            )
        }
        composable(Screen.Bookmarks.route) {
            com.abpvt.newsapp.ui.profile.BookmarksScreen(navController)
        }
    }
}
