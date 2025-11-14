package com.eurogame.scorecard.presentation.navigation

sealed class Screen(val route: String) {
    object GameSetup : Screen("game_setup")
    object Scorecard : Screen("scorecard")
}
