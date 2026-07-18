package id.antasari.sumifyai.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.antasari.sumifyai.ui.screens.CreateSummaryScreen
import id.antasari.sumifyai.ui.screens.DashboardScreen
import id.antasari.sumifyai.ui.screens.DetailsScreen
import id.antasari.sumifyai.ui.screens.FavoriteSummariesScreen
import id.antasari.sumifyai.ui.screens.SettingsScreen
import id.antasari.sumifyai.ui.screens.StatusProgressScreen
import id.antasari.sumifyai.ui.screens.WelcomeScreen
import id.antasari.sumifyai.ui.viewmodel.MainViewModel

object Routes {
    const val WELCOME = "welcome"
    const val DASHBOARD = "dashboard"
    const val CREATE_SUMMARY = "create_summary"
    const val FAVORITE_SUMMARIES = "favorite_summaries"
    const val SETTINGS = "settings"
    const val STATUS_PROGRESS = "status_progress/{meetingId}"
    const val DETAILS = "details/{meetingId}"

    fun statusProgress(meetingId: String) = "status_progress/$meetingId"
    fun details(meetingId: String) = "details/$meetingId"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    startDestination: String = Routes.WELCOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToDashboard = {
                    viewModel.completeWelcome {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToCreate = {
                    navController.navigate(Routes.CREATE_SUMMARY)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToFavorites = {
                    navController.navigate(Routes.FAVORITE_SUMMARIES)
                },
                onNavigateToDetails = { meetingId, status ->
                    if (status.isFinalReportStatus()) {
                        navController.navigate(Routes.details(meetingId))
                    } else {
                        navController.navigate(Routes.statusProgress(meetingId))
                    }
                }
            )
        }

        composable(Routes.FAVORITE_SUMMARIES) {
            FavoriteSummariesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetails = { meetingId, status ->
                    if (status.isFinalReportStatus()) {
                        navController.navigate(Routes.details(meetingId))
                    } else {
                        navController.navigate(Routes.statusProgress(meetingId))
                    }
                }
            )
        }

        composable(Routes.CREATE_SUMMARY) {
            CreateSummaryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUploadSuccess = { meetingId ->
                    navController.navigate(Routes.statusProgress(meetingId)) {
                        popUpTo(Routes.DASHBOARD) // Pop the create screen so back goes to dashboard
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.STATUS_PROGRESS,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            StatusProgressScreen(
                meetingId = meetingId,
                viewModel = viewModel,
                onNavigateToDetails = {
                    navController.navigate(Routes.details(meetingId)) {
                        popUpTo(Routes.DASHBOARD) // Back goes to dashboard
                    }
                },
                onNavigateBack = {
                    navController.popBackStack(Routes.DASHBOARD, false)
                }
            )
        }

        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getString("meetingId") ?: ""
            DetailsScreen(
                meetingId = meetingId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun String.isFinalReportStatus(): Boolean {
    return equals("completed", ignoreCase = true) ||
        equals("failed", ignoreCase = true) ||
        equals("cancelled", ignoreCase = true)
}
