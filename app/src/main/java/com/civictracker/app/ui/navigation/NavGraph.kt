package com.civictracker.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.civictracker.app.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home") {
        const val routeWithArgs = "home?routingIssueId={routingIssueId}"
        fun createRoute(routingIssueId: String? = null) = if (routingIssueId != null) "home?routingIssueId=$routingIssueId" else "home"
    }
    object Report : Screen("report")
    object IssueDetail : Screen("issue_detail/{issueId}") {
        fun createRoute(issueId: String) = "issue_detail/$issueId"
    }
    object OfficerDashboard : Screen("officer_dashboard")
    object PublicScorecard : Screen("public_scorecard")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 1000 }) }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.routeWithArgs,
            arguments = listOf(navArgument("routingIssueId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val routingIssueId = backStackEntry.arguments?.getString("routingIssueId")
            HomeScreen(
                routingIssueId = routingIssueId,
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
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
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
                onBack = { navController.popBackStack() },
                onGetDirections = { id ->
                    navController.navigate(Screen.Home.createRoute(id)) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
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
