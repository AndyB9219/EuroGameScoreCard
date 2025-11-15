package com.eurogame.scorecard.data.xml

data class ScorecardTemplate(
    val game: GameTemplate,
    val categories: List<CategoryTemplate>
)

data class GameTemplate(
    val name: String,
    val subtitle: String? = null,
    val designer: String? = null,
    val studio: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val backgroundImageUrl: String? = null,
    val publishYear: Int? = null
)

data class CategoryTemplate(
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val scoringRuleType: String? = null,
    val scoreIndex: Int = 0,
    val isOptional: Boolean = false
)
