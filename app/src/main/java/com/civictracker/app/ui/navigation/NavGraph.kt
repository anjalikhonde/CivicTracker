package com.civictracker.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.civictracker.app.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Report : Screen("report")
    object IssueDetail : Screen("issue_detail/{issueId}") {
        fun createRoute(issueId: String) = "issue_detail/$issueId"
    }
    object OfficerDashboard : Screen("officer_dashboard")
    object PublicScorecard : Screen("public_scorecard")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 1000 })
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -1000 })
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -1000 })
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 1000 })
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onReportIssue = { navController.navigate(Screen.Report.route) },
                onIssueClick = { issueId ->
                    navController.navigate(Screen.IssueDetail.createRoute(issueId))
                },
                onNavigateToOfficerDashboard = {
                    navController.navigate(Screen.OfficerDashboard.route)
                },
                onNavigateToPublicScorecard = {
                    navController.navigate(Screen.PublicScorecard.route)
                },
                onLogout = { }
            )
        }

        composable(Screen.Report.route) {
            ReportIssueScreen(
                onBack = { navController.popBackStack(); Unit },
                onSuccess = { navController.popBackStack(); Unit }
            )
        }

        composable(
            route = Screen.IssueDetail.route,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            IssueDetailScreen(
                issueId = issueId,
                onBack = { navController.popBackStack(); Unit }
            )
        }

        composable(Screen.OfficerDashboard.route) {
            OfficerDashboardScreen(
                onBack = { navController.popBackStack(); Unit },
                onIssueClick = { issueId ->
                    navController.navigate(Screen.IssueDetail.createRoute(issueId))
                }
            )
        }

        composable(Screen.PublicScorecard.route) {
            PublicScorecardScreen(
                onBack = { navController.popBackStack(); Unit }
            )
        }
    }
}