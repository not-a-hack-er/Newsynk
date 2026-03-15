package com.abpvt.newsapp.navigation



import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abpvt.newsapp.auth.LoginScreen
import com.abpvt.newsapp.auth.RegisterScreen
import com.abpvt.newsapp.ui.news.NewsScreen
import com.abpvt.newsapp.ui.comments.CommentScreen

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
    startDestination: String = Screen.Login.route
) {
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
            NewsScreen(navController)
        }
        composable(Screen.Comments.route) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            CommentScreen(articleId)
        }
        composable(Screen.Profile.route) {
            com.abpvt.newsapp.ui.profile.ProfileScreen(navController)
        }
        composable(Screen.ArticleView.route) { backStackEntry ->
            val articleUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            com.abpvt.newsapp.ui.news.ArticleScreen(
                url = articleUrl,
                navController = navController,
                onCommentClick = {
                    val articleId = articleUrl // Using URL as ID in Comments screen
                    navController.navigate(Screen.Comments.createRoute(articleId))
                }
            )
        }
        composable(Screen.Bookmarks.route) {
            com.abpvt.newsapp.ui.profile.BookmarksScreen(navController)
        }
    }
}
