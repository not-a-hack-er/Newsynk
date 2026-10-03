package com.abpvt.newsapp.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abpvt.newsapp.auth.AuthViewModel
import com.abpvt.newsapp.auth.LoginScreen
import com.abpvt.newsapp.auth.RegisterScreen
import com.abpvt.newsapp.auth.ForgotPasswordScreen
import com.abpvt.newsapp.ui.news.NewsScreen
import com.abpvt.newsapp.ui.news.SelectedArticleViewModel

sealed class Screen(val route: String) {
    object Login    : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object News     : Screen("news")
    object Explore  : Screen("explore")
    object Profile  : Screen("profile")
    object Bookmarks: Screen("bookmarks")
    object Contact  : Screen("contact")
    object CompareCoverage : Screen("compare_coverage")

    // Use query params for anything that contains a URL or arbitrary string
    // so NavController never mistakes encoded slashes for path separators.
    object Comments : Screen("comments?articleId={articleId}") {
        fun createRoute(articleId: String) = "comments?articleId=${android.net.Uri.encode(articleId)}"
    }
    object ArticleView : Screen("articleView?url={url}&title={title}") {
        fun createRoute(url: String, title: String = "Newsynk article") =
            "articleView?url=${android.net.Uri.encode(url)}&title=${android.net.Uri.encode(title)}"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    // Single shared AuthViewModel for the entire nav graph
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController, authViewModel)
        }
        composable(Screen.News.route) {
            // Scope SelectedArticleViewModel to the "news" back-stack entry so
            // ArticleView can access the same instance when it navigates forward.
            val newsEntry = remember(it) {
                navController.getBackStackEntry(Screen.News.route)
            }
            val selectedArticleVM: SelectedArticleViewModel = hiltViewModel(newsEntry)
            NewsScreen(
                navController = navController,
                authViewModel = authViewModel,
                selectedArticleViewModel = selectedArticleVM
            )
        }
        composable(Screen.Comments.route) { backStackEntry ->
            // Query param is auto-decoded by NavController
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            com.abpvt.newsapp.ui.comments.CommentScreen(
                articleId = articleId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Explore.route) { backStackEntry ->
            val newsEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.News.route)
            }
            val selectedArticleVM: SelectedArticleViewModel = hiltViewModel(newsEntry)
            com.abpvt.newsapp.ui.news.ExploreScreen(
                navController = navController,
                selectedArticleViewModel = selectedArticleVM
            )
        }
        composable(Screen.CompareCoverage.route) { backStackEntry ->
            val newsEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.News.route)
            }
            val selectedArticleVM: SelectedArticleViewModel = hiltViewModel(newsEntry)
            com.abpvt.newsapp.ui.news.CompareCoverageScreen(
                navController = navController,
                selectedArticleViewModel = selectedArticleVM
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
            val articleUrl = backStackEntry.arguments?.getString("url") ?: ""
            val articleTitle = backStackEntry.arguments?.getString("title") ?: "Newsynk article"
            // Prefer the News-scoped hand-off. A defensive fallback to the current
            // entry keeps externally-triggered navigation safe even without News.
            val viewModelOwner = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry(Screen.News.route) }
                    .getOrElse { backStackEntry }
            }
            val selectedArticleVM: SelectedArticleViewModel = hiltViewModel(viewModelOwner)
            com.abpvt.newsapp.ui.news.ArticleScreen(
                navController = navController,
                selectedArticleViewModel = selectedArticleVM,
                fallbackArticle = com.abpvt.newsapp.data.model.Article(
                    id = articleUrl,
                    title = articleTitle,
                    url = articleUrl,
                    sectionName = "Newsynk"
                ),
                onCommentClick = {
                    navController.navigate(Screen.Comments.createRoute(articleUrl))
                }
            )
        }
        composable(Screen.Bookmarks.route) { backStackEntry ->
            val newsEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.News.route)
            }
            val selectedArticleVM: SelectedArticleViewModel = hiltViewModel(newsEntry)
            com.abpvt.newsapp.ui.profile.BookmarksScreen(navController, selectedArticleVM)
        }
        composable(Screen.Contact.route) {
            com.abpvt.newsapp.ui.profile.ContactScreen(navController)
        }
    }
}
