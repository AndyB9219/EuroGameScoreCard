package com.eurogame.scorecard.presentation.navigation

sealed class Screen(val route: String) {
    object GameSetup : Screen("game_setup")
    object Scorecard : Screen("scorecard")
    object TemplateBrowser : Screen("template_browser")
    object TemplateBuilder : Screen("template_builder")

    companion object {
        const val TEMPLATE_ARG = "templateFileName"
        fun gameSetupWithTemplate(templateFileName: String) = "game_setup?$TEMPLATE_ARG=$templateFileName"
    }
}
