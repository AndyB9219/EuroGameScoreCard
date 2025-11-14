package com.eurogame.scorecard.domain.model

import org.junit.Assert.*
import org.junit.Test

class PlayerTest {

    @Test
    fun `getTotalScore calculates sum of all category scores`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L, 30L)
        val scores = mapOf(
            Pair(1L, 10L) to 5,
            Pair(1L, 20L) to 10,
            Pair(1L, 30L) to 15
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(30, total)
    }

    @Test
    fun `getTotalScore returns 0 when no scores exist`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L)
        val scores = emptyMap<Pair<Long, Long>, Int>()

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(0, total)
    }

    @Test
    fun `getTotalScore returns 0 when no categories provided`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = emptyList<Long>()
        val scores = mapOf(
            Pair(1L, 10L) to 5,
            Pair(1L, 20L) to 10
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(0, total)
    }

    @Test
    fun `getTotalScore treats missing scores as 0`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L, 30L)
        val scores = mapOf(
            Pair(1L, 10L) to 5,
            // Missing score for category 20
            Pair(1L, 30L) to 15
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(20, total)  // 5 + 0 + 15
    }

    @Test
    fun `getTotalScore only sums scores for this player`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L)
        val scores = mapOf(
            Pair(1L, 10L) to 5,    // Alice
            Pair(1L, 20L) to 10,   // Alice
            Pair(2L, 10L) to 100,  // Bob (different player)
            Pair(2L, 20L) to 200   // Bob (different player)
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(15, total)  // Only Alice's scores: 5 + 10
    }

    @Test
    fun `getTotalScore handles negative scores`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L, 30L)
        val scores = mapOf(
            Pair(1L, 10L) to 10,
            Pair(1L, 20L) to -5,
            Pair(1L, 30L) to 15
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(20, total)  // 10 + (-5) + 15
    }

    @Test
    fun `getTotalScore handles zero scores`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L)
        val scores = mapOf(
            Pair(1L, 10L) to 0,
            Pair(1L, 20L) to 0
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(0, total)
    }

    @Test
    fun `getTotalScore ignores categories not in the list`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val categoryIds = listOf(10L, 20L)
        val scores = mapOf(
            Pair(1L, 10L) to 5,
            Pair(1L, 20L) to 10,
            Pair(1L, 30L) to 100,  // Not in categoryIds list
            Pair(1L, 40L) to 200   // Not in categoryIds list
        )

        val total = player.getTotalScore(scores, categoryIds)

        assertEquals(15, total)  // Only 5 + 10
    }

    @Test
    fun `Player data class has correct properties`() {
        val player = Player(
            id = 42,
            name = "Alice",
            orderIndex = 5
        )

        assertEquals(42, player.id)
        assertEquals("Alice", player.name)
        assertEquals(5, player.orderIndex)
    }

    @Test
    fun `Player data class has default id value`() {
        val player = Player(name = "Alice", orderIndex = 0)

        assertEquals(0, player.id)
    }

    @Test
    fun `Player data class has default orderIndex value`() {
        val player = Player(name = "Alice")

        assertEquals(0, player.orderIndex)
    }

    @Test
    fun `Player data class supports copy`() {
        val player = Player(id = 1, name = "Alice", orderIndex = 0)
        val copied = player.copy(name = "Alice Updated")

        assertEquals(1, copied.id)
        assertEquals("Alice Updated", copied.name)
        assertEquals(0, copied.orderIndex)
    }

    @Test
    fun `Player data class supports equality`() {
        val player1 = Player(id = 1, name = "Alice", orderIndex = 0)
        val player2 = Player(id = 1, name = "Alice", orderIndex = 0)
        val player3 = Player(id = 2, name = "Bob", orderIndex = 1)

        assertEquals(player1, player2)
        assertNotEquals(player1, player3)
    }
}
