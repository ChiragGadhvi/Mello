package com.chirag.mello.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.chirag.mello.ui.screens.HomeScreen
import com.chirag.mello.ui.screens.InsightsScreen
import com.chirag.mello.ui.screens.MelloBackground
import com.chirag.mello.ui.screens.OnboardingScreen
import com.chirag.mello.ui.screens.ProfileScreen
import com.chirag.mello.ui.screens.TimelineScreen
import com.chirag.mello.ui.theme.*
import com.chirag.mello.viewmodel.JournalViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TIMELINE = "timeline"
    const val INSIGHTS = "insights"
    const val PROFILE = "profile"
}

@Composable
fun MelloNavGraph() {
    val navController = rememberNavController()
    val viewModel: JournalViewModel = viewModel()
    
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mello_prefs", Context.MODE_PRIVATE) }
    val isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)
    val startDest = if (isOnboardingComplete) Routes.HOME else Routes.ONBOARDING

    MelloBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // Only show bottom bar if we are NOT on the onboarding screen
                if (currentDestination?.route != Routes.ONBOARDING) {
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
                            icon = { Icon(Icons.Filled.BarChart, contentDescription = "Insights") },
                            label = { Text("Insights") },
                            selected = currentDestination?.hierarchy?.any { it.route == Routes.INSIGHTS } == true,
                            onClick = {
                                navController.navigate(Routes.INSIGHTS) {
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onFinish = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTimeline = {
                            navController.navigate(Routes.TIMELINE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToInsights = {
                            navController.navigate(Routes.INSIGHTS) {
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
                composable(Routes.INSIGHTS) {
                    InsightsScreen(viewModel = viewModel)
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
