package com.eurogame.scorecard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eurogame.scorecard.presentation.gamesetup.GameSetupScreen
import com.eurogame.scorecard.presentation.scorecard.ScorecardScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.GameSetup.route) {
            GameSetupScreen(
                onGameCreated = {
                    navController.navigate(Screen.Scorecard.route) {
                        popUpTo(Screen.GameSetup.route) { inclusive = true }
                    }
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
    }
}
