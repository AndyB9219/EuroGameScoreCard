package com.eurogame.scorecard.domain.model

data class Game(
    val id: Long = 0,
    val name: String,
    val description: String,
    val subtitle: String? = null,
    val designer: String? = null,
    val studio: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val backgroundImageUrl: String? = null,
    val publishYear: Int? = null,
    val templateId: String? = null,
    val players: List<Player> = emptyList(),
    val categories: List<ScoreCategory> = emptyList(),
    val scores: Map<Pair<Long, Long>, Int> = emptyMap(), // (playerId, categoryId) -> score
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
