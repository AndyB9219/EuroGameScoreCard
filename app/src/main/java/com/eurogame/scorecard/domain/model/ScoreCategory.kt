package com.eurogame.scorecard.domain.model

data class ScoreCategory(
    val id: Long = 0,
    val title: String,
    val subtitle: String? = null,
    val orderIndex: Int = 0
)
