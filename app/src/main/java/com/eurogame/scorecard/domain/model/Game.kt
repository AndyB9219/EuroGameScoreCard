package com.eurogame.scorecard.domain.model

data class Game(
    val id: Long = 0,
    val name: String,
    val description: String,
    val players: List<Player> = emptyList(),
    val categories: List<ScoreCategory> = emptyList(),
    val scores: Map<Pair<Long, Long>, Int> = emptyMap(), // (playerId, categoryId) -> score
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
