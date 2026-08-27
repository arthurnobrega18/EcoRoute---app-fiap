package com.ecoroute.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.ecoroute.screens.ComparisonScreen
import com.ecoroute.screens.HistoryScreen
import com.ecoroute.screens.HomeScreen
import com.ecoroute.screens.InsightsScreen
import com.ecoroute.screens.PreferencesScreen
import com.ecoroute.screens.RouteScreen


private fun androidx.navigation.NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onNewRoute = {
                    navController.navigate("route")
                },
                onHistory = {
                    navController.navigateToTab("history")
                },
                onInsights = {
                    navController.navigateToTab("insights")
                },
                onPreferences = {
                    navController.navigate("preferences")
                }
            )
        }

        composable("preferences") {
            PreferencesScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("route") {
            RouteScreen(
                onCompare = { origin, destination ->
                    val encodedOrigin = Uri.encode(origin)
                    val encodedDestination = Uri.encode(destination)
                    navController.navigate("comparison/$encodedOrigin/$encodedDestination")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "comparison/{origin}/{destination}",
            arguments = listOf(
                navArgument("origin") { type = NavType.StringType },
                navArgument("destination") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val origin = backStackEntry.arguments?.getString("origin").orEmpty()
            val destination = backStackEntry.arguments?.getString("destination").orEmpty()
            ComparisonScreen(
                origin = origin,
                destination = destination,
                onInsights = {
                    navController.navigateToTab("insights")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("insights") {
            InsightsScreen(
                onHome = {
                    navController.navigateToTab("home")
                },
                onHistory = {
                    navController.navigateToTab("history")
                },
                onNewRoute = {
                    navController.navigate("route")
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onHome = {
                    navController.navigateToTab("home")
                },
                onInsights = {
                    navController.navigateToTab("insights")
                },
                onNewRoute = {
                    navController.navigate("route")
                }
            )
        }
    }
}