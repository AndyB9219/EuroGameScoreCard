package com.eurogame.scorecard.domain.model

import org.junit.Assert.*
import org.junit.Test

class GameSetupDataTest {

    @Test
    fun `GameSetupData has correct properties`() {
        val categories = listOf(
            CategoryInput("Birds", "Main scoring"),
            CategoryInput("Bonus", null)
        )

        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Fun game night",
            playerNames = listOf("Alice", "Bob", "Charlie"),
            categories = categories
        )

        assertEquals("Wingspan", setupData.gameName)
        assertEquals("Fun game night", setupData.description)
        assertEquals(3, setupData.playerNames.size)
        assertEquals(listOf("Alice", "Bob", "Charlie"), setupData.playerNames)
        assertEquals(2, setupData.categories.size)
    }

    @Test
    fun `GameSetupData supports empty description`() {
        val setupData = GameSetupData(
            gameName = "Test",
            description = "",
            playerNames = listOf("Alice"),
            categories = listOf(CategoryInput("Points", null))
        )

        assertEquals("", setupData.description)
    }

    @Test
    fun `GameSetupData data class supports copy`() {
        val setupData = GameSetupData(
            gameName = "Original",
            description = "Original description",
            playerNames = listOf("Alice"),
            categories = listOf(CategoryInput("Birds", null))
        )

        val copied = setupData.copy(gameName = "Updated")

        assertEquals("Updated", copied.gameName)
        assertEquals("Original description", copied.description)
    }
}

class CategoryInputTest {

    @Test
    fun `CategoryInput has correct properties`() {
        val category = CategoryInput(
            title = "Birds",
            subtitle = "Main scoring"
        )

        assertEquals("Birds", category.title)
        assertEquals("Main scoring", category.subtitle)
    }

    @Test
    fun `CategoryInput supports null subtitle`() {
        val category = CategoryInput(
            title = "Birds",
            subtitle = null
        )

        assertEquals("Birds", category.title)
        assertNull(category.subtitle)
    }

    @Test
    fun `CategoryInput has default null subtitle`() {
        val category = CategoryInput(title = "Birds")

        assertNull(category.subtitle)
    }

    @Test
    fun `CategoryInput data class supports copy`() {
        val category = CategoryInput(
            title = "Original",
            subtitle = "Original subtitle"
        )

        val copied = category.copy(title = "Updated")

        assertEquals("Updated", copied.title)
        assertEquals("Original subtitle", copied.subtitle)
    }

    @Test
    fun `CategoryInput data class supports equality`() {
        val category1 = CategoryInput("Birds", "Main")
        val category2 = CategoryInput("Birds", "Main")
        val category3 = CategoryInput("Bonus", null)

        assertEquals(category1, category2)
        assertNotEquals(category1, category3)
    }
}
