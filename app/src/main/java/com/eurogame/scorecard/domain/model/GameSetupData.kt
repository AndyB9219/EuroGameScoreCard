package com.eurogame.scorecard.domain.model

data class GameSetupData(
    val gameName: String,
    val description: String,
    val playerNames: List<String>,
    val categories: List<CategoryInput>
)

data class CategoryInput(
    val title: String,
    val subtitle: String? = null
)
