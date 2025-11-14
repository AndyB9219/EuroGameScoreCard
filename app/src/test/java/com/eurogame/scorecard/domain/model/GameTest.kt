package com.eurogame.scorecard.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameTest {

    @Test
    fun `Game data class has correct properties`() {
        val players = listOf(
            Player(id = 1, name = "Alice", orderIndex = 0),
            Player(id = 2, name = "Bob", orderIndex = 1)
        )

        val categories = listOf(
            ScoreCategory(id = 1, title = "Birds", subtitle = "Main", orderIndex = 0)
        )

        val scores = mapOf(
            Pair(1L, 1L) to 10,
            Pair(2L, 1L) to 15
        )

        val game = Game(
            id = 42,
            name = "Wingspan",
            description = "Fun game",
            players = players,
            categories = categories,
            scores = scores,
            createdAt = 1234567890L,
            isActive = true
        )

        assertEquals(42, game.id)
        assertEquals("Wingspan", game.name)
        assertEquals("Fun game", game.description)
        assertEquals(2, game.players.size)
        assertEquals(1, game.categories.size)
        assertEquals(2, game.scores.size)
        assertEquals(1234567890L, game.createdAt)
        assertTrue(game.isActive)
    }

    @Test
    fun `Game has default values`() {
        val game = Game(
            name = "Test Game",
            description = "Test"
        )

        assertEquals(0, game.id)
        assertTrue(game.players.isEmpty())
        assertTrue(game.categories.isEmpty())
        assertTrue(game.scores.isEmpty())
        assertTrue(game.isActive)
        assertTrue(game.createdAt > 0)
    }

    @Test
    fun `Game data class supports copy`() {
        val game = Game(
            id = 1,
            name = "Original",
            description = "Original description"
        )

        val copied = game.copy(name = "Updated")

        assertEquals(1, copied.id)
        assertEquals("Updated", copied.name)
        assertEquals("Original description", copied.description)
    }

    @Test
    fun `Game scores map works correctly`() {
        val game = Game(
            name = "Test",
            description = "",
            scores = mapOf(
                Pair(1L, 10L) to 5,
                Pair(1L, 20L) to 10,
                Pair(2L, 10L) to 8
            )
        )

        assertEquals(5, game.scores[Pair(1L, 10L)])
        assertEquals(10, game.scores[Pair(1L, 20L)])
        assertEquals(8, game.scores[Pair(2L, 10L)])
        assertNull(game.scores[Pair(3L, 10L)])
    }
}
