package com.chirag.mello.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.chirag.mello.ui.screens.HomeScreen
import com.chirag.mello.ui.screens.MelloBackground
import com.chirag.mello.ui.screens.ProfileScreen
import com.chirag.mello.ui.screens.TimelineScreen
import com.chirag.mello.ui.theme.*
import com.chirag.mello.viewmodel.JournalViewModel

object Routes {
    const val HOME = "home"
    const val TIMELINE = "timeline"
    const val PROFILE = "profile"
}

@Composable
fun MelloNavGraph() {
    val navController = rememberNavController()
    val viewModel: JournalViewModel = viewModel()

    MelloBackground {
    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar(containerColor = Surface.copy(alpha = 0.6f)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Create, contentDescription = "Log") },
                    label = { Text("Log") },
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.HOME } == true,
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = Lavender,
                        indicatorColor = Lavender,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Journal") },
                    label = { Text("Journal") },
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.TIMELINE } == true,
                    onClick = {
                        navController.navigate(Routes.TIMELINE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = Lavender,
                        indicatorColor = Lavender,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination?.hierarchy?.any { it.route == Routes.PROFILE } == true,
                    onClick = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Background,
                        selectedTextColor = Lavender,
                        indicatorColor = Lavender,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTimeline = { 
                        navController.navigate(Routes.TIMELINE) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.TIMELINE) {
                TimelineScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }
        }
    }
    } // end MelloBackground
}
