package com.eurogame.scorecard.domain.model

data class Player(
    val id: Long = 0,
    val name: String,
    val orderIndex: Int = 0
) {
    fun getTotalScore(scores: Map<Pair<Long, Long>, Int>, categoryIds: List<Long>): Int {
        return categoryIds.sumOf { categoryId ->
            scores[Pair(id, categoryId)] ?: 0
        }
    }
}
