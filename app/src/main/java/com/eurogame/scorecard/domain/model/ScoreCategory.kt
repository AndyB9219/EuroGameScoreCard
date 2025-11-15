package com.eurogame.scorecard.domain.model

data class ScoreCategory(
    val id: Long = 0,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val scoringRuleType: String? = null,
    val scoreIndex: Int = 0,
    val orderIndex: Int = 0,
    val isOptional: Boolean = false,
    val isEnabled: Boolean = true
)
