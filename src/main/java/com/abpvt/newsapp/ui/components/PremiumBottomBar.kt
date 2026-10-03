package com.abpvt.newsapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.abpvt.newsapp.navigation.Screen

private data class Destination(val label: String, val route: String, val icon: ImageVector)

private val destinations = listOf(
    Destination("Home", Screen.News.route, Icons.Default.Home),
    Destination("Explore", Screen.Explore.route, Icons.Default.Explore),
    Destination("Saved", Screen.Bookmarks.route, Icons.Default.Bookmark),
    Destination("Profile", Screen.Profile.route, Icons.Default.Person)
)

@Composable
fun PremiumBottomBar(navController: NavHostController, currentRoute: String) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(Screen.News.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            )
        }
    }
}
