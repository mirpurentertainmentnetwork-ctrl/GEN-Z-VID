package com.genzvid.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.genzvid.app.ui.screens.AuthScreen
import com.genzvid.app.ui.screens.CreateScreen
import com.genzvid.app.ui.screens.FeedScreen
import com.genzvid.app.ui.screens.ProfileScreen
import com.genzvid.app.ui.screens.SearchScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Feed : Screen("feed", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Create : Screen("create", "Create", Icons.Default.Add)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@Composable
fun GenZVidApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Feed, Screen.Search, Screen.Create, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") { AuthScreen(onAuthSuccess = { navController.navigate("feed") { popUpTo("auth") { inclusive = true } } }) }
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Create.route) { CreateScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLogout = { navController.navigate("auth") { popUpTo(0) } }) }
        }
    }
}
