package com.eurogame.scorecard.domain.model

import org.junit.Assert.*
import org.junit.Test

class ScoreCategoryTest {

    @Test
    fun `ScoreCategory has correct properties`() {
        val category = ScoreCategory(
            id = 42,
            title = "Birds",
            subtitle = "Main scoring",
            orderIndex = 5
        )

        assertEquals(42, category.id)
        assertEquals("Birds", category.title)
        assertEquals("Main scoring", category.subtitle)
        assertEquals(5, category.orderIndex)
    }

    @Test
    fun `ScoreCategory has default values`() {
        val category = ScoreCategory(title = "Birds")

        assertEquals(0, category.id)
        assertNull(category.subtitle)
        assertEquals(0, category.orderIndex)
    }

    @Test
    fun `ScoreCategory supports null subtitle`() {
        val category = ScoreCategory(
            id = 1,
            title = "Birds",
            subtitle = null,
            orderIndex = 0
        )

        assertNull(category.subtitle)
    }

    @Test
    fun `ScoreCategory data class supports copy`() {
        val category = ScoreCategory(
            id = 1,
            title = "Original",
            subtitle = "Original subtitle",
            orderIndex = 0
        )

        val copied = category.copy(title = "Updated")

        assertEquals(1, copied.id)
        assertEquals("Updated", copied.title)
        assertEquals("Original subtitle", copied.subtitle)
    }

    @Test
    fun `ScoreCategory data class supports equality`() {
        val category1 = ScoreCategory(id = 1, title = "Birds", subtitle = "Main", orderIndex = 0)
        val category2 = ScoreCategory(id = 1, title = "Birds", subtitle = "Main", orderIndex = 0)
        val category3 = ScoreCategory(id = 2, title = "Bonus", subtitle = null, orderIndex = 1)

        assertEquals(category1, category2)
        assertNotEquals(category1, category3)
    }
}
