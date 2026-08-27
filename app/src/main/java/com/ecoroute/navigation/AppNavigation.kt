package com.ecoroute.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.ecoroute.screens.ComparisonScreen
import com.ecoroute.screens.HistoryScreen
import com.ecoroute.screens.HomeScreen
import com.ecoroute.screens.InsightsScreen
import com.ecoroute.screens.RouteScreen


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
                    navController.navigate("history")
                },
                onInsights = {
                    navController.navigate("insights")
                }
            )
        }

        composable("route") {
            RouteScreen(
                onCompare = {
                    navController.navigate("comparison")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("comparison") {
            ComparisonScreen(
                onInsights = {
                    navController.navigate("insights")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("insights") {
            InsightsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}