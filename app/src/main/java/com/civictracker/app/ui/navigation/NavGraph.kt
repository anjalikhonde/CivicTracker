package com.civictracker.app.ui.navigation

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
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onReportIssue = {
                    navController.navigate(Screen.Report.route)
                },
                onIssueClick = { issueId ->
                    navController.navigate(Screen.IssueDetail.createRoute(issueId))
                },
                onNavigateToOfficerDashboard = {
                    navController.navigate(Screen.OfficerDashboard.route)
                },
                onNavigateToPublicScorecard = {
                    navController.navigate(Screen.PublicScorecard.route)
                },
                onLogout = {
                    // Navigate to Login if implemented
                }
            )
        }
        composable(Screen.Report.route) {
            ReportIssueScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.IssueDetail.route,
            arguments = listOf(navArgument("issueId") { type = NavType.StringType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getString("issueId") ?: ""
            IssueDetailScreen(
                issueId = issueId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OfficerDashboard.route) {
            OfficerDashboardScreen(
                onBack = { navController.popBackStack() },
                onIssueClick = { issueId ->
                    navController.navigate(Screen.IssueDetail.createRoute(issueId))
                }
            )
        }
        composable(Screen.PublicScorecard.route) {
            PublicScorecardScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
