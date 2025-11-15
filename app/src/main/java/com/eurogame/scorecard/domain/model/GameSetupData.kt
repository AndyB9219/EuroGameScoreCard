package com.eurogame.scorecard.domain.model

data class GameSetupData(
    val gameName: String,
    val description: String,
    val subtitle: String? = null,
    val designer: String? = null,
    val studio: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val backgroundImageUrl: String? = null,
    val publishYear: Int? = null,
    val templateId: String? = null,
    val playerNames: List<String>,
    val categories: List<CategoryInput>
)

data class CategoryInput(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val scoringRuleType: String? = null,
    val scoreIndex: Int = 0,
    val isOptional: Boolean = false
)
