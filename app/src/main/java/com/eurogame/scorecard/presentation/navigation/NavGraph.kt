package com.eurogame.scorecard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.eurogame.scorecard.presentation.gamesetup.GameSetupScreen
import com.eurogame.scorecard.presentation.scorecard.ScorecardScreen
import com.eurogame.scorecard.presentation.templatebuilder.TemplateBuilderScreen
import com.eurogame.scorecard.presentation.templatebrowser.TemplateBrowserScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = "${Screen.GameSetup.route}?${Screen.TEMPLATE_ARG}={${Screen.TEMPLATE_ARG}}",
            arguments = listOf(
                navArgument(Screen.TEMPLATE_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val viewModel = koinViewModel<com.eurogame.scorecard.presentation.gamesetup.GameSetupViewModel>()
            val templateFileName = backStackEntry.arguments?.getString(Screen.TEMPLATE_ARG)

            LaunchedEffect(templateFileName) {
                if (templateFileName != null) {
                    viewModel.loadFromTemplate(templateFileName)
                }
            }

            GameSetupScreen(
                viewModel = viewModel,
                onGameCreated = {
                    navController.navigate(Screen.Scorecard.route) {
                        popUpTo(Screen.GameSetup.route) { inclusive = true }
                    }
                },
                onBrowseTemplates = {
                    navController.navigate(Screen.TemplateBrowser.route)
                }
            )
        }

        composable(Screen.Scorecard.route) {
            ScorecardScreen(
                onNewGame = {
                    navController.navigate(Screen.GameSetup.route) {
                        popUpTo(Screen.Scorecard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TemplateBrowser.route) {
            TemplateBrowserScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onCreateTemplate = {
                    navController.navigate(Screen.TemplateBuilder.route)
                },
                onTemplateSelected = { templateFileName ->
                    navController.navigate(Screen.gameSetupWithTemplate(templateFileName)) {
                        popUpTo(Screen.GameSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TemplateBuilder.route) {
            TemplateBuilderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTemplateSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
