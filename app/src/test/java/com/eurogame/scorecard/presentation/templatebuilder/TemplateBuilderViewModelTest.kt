package com.eurogame.scorecard.presentation.templatebuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateBuilderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var storageManager: TemplateStorageManager
    private lateinit var viewModel: TemplateBuilderViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        storageManager = mockk()
        viewModel = TemplateBuilderViewModel(storageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.state.value

        assertEquals("", state.gameName)
        assertEquals("", state.subtitle)
        assertEquals("", state.designer)
        assertEquals("", state.studio)
        assertEquals("", state.minPlayers)
        assertEquals("", state.maxPlayers)
        assertEquals("", state.backgroundImageUrl)
        assertEquals("", state.publishYear)
        assertEquals(1, state.categories.size)
        assertFalse(state.isSaving)
        assertNull(state.error)
        assertFalse(state.templateSaved)
    }

    @Test
    fun `onGameNameChange updates game name`() {
        viewModel.onGameNameChange("Wingspan")

        assertEquals("Wingspan", viewModel.state.value.gameName)
    }

    @Test
    fun `onSubtitleChange updates subtitle`() {
        viewModel.onSubtitleChange("European Edition")

        assertEquals("European Edition", viewModel.state.value.subtitle)
    }

    @Test
    fun `onDesignerChange updates designer`() {
        viewModel.onDesignerChange("Elizabeth Hargrave")

        assertEquals("Elizabeth Hargrave", viewModel.state.value.designer)
    }

    @Test
    fun `onStudioChange updates studio`() {
        viewModel.onStudioChange("Stonemaier Games")

        assertEquals("Stonemaier Games", viewModel.state.value.studio)
    }

    @Test
    fun `onMinPlayersChange updates min players`() {
        viewModel.onMinPlayersChange("1")

        assertEquals("1", viewModel.state.value.minPlayers)
    }

    @Test
    fun `onMaxPlayersChange updates max players`() {
        viewModel.onMaxPlayersChange("5")

        assertEquals("5", viewModel.state.value.maxPlayers)
    }

    @Test
    fun `onBackgroundImageUrlChange updates background image url`() {
        viewModel.onBackgroundImageUrlChange("https://example.com/image.png")

        assertEquals("https://example.com/image.png", viewModel.state.value.backgroundImageUrl)
    }

    @Test
    fun `onPublishYearChange updates publish year`() {
        viewModel.onPublishYearChange("2019")

        assertEquals("2019", viewModel.state.value.publishYear)
    }

    @Test
    fun `onCategoryNameChange updates specific category name`() {
        viewModel.onCategoryNameChange(0, "Birds")

        assertEquals("Birds", viewModel.state.value.categories[0].name)
    }

    @Test
    fun `onCategoryDescriptionChange updates specific category description`() {
        viewModel.onCategoryDescriptionChange(0, "Points from bird cards")

        assertEquals("Points from bird cards", viewModel.state.value.categories[0].description)
    }

    @Test
    fun `onCategoryIconUrlChange updates specific category icon url`() {
        viewModel.onCategoryIconUrlChange(0, "https://example.com/bird.png")

        assertEquals("https://example.com/bird.png", viewModel.state.value.categories[0].iconUrl)
    }

    @Test
    fun `onCategoryBackgroundImageUrlChange updates specific category background image`() {
        viewModel.onCategoryBackgroundImageUrlChange(0, "https://example.com/bg.png")

        assertEquals("https://example.com/bg.png", viewModel.state.value.categories[0].backgroundImageUrl)
    }

    @Test
    fun `onCategoryScoringRuleTypeChange updates specific category scoring rule`() {
        viewModel.onCategoryScoringRuleTypeChange(0, "sum")

        assertEquals("sum", viewModel.state.value.categories[0].scoringRuleType)
    }

    @Test
    fun `onCategoryScoreIndexChange updates specific category score index`() {
        viewModel.onCategoryScoreIndexChange(0, "5")

        assertEquals("5", viewModel.state.value.categories[0].scoreIndex)
    }

    @Test
    fun `addCategory increases category count`() {
        assertEquals(1, viewModel.state.value.categories.size)

        viewModel.addCategory()

        assertEquals(2, viewModel.state.value.categories.size)
    }

    @Test
    fun `removeCategory decreases category count when more than 1 category`() {
        viewModel.addCategory()
        viewModel.addCategory()
        assertEquals(3, viewModel.state.value.categories.size)

        viewModel.removeCategory(1)

        assertEquals(2, viewModel.state.value.categories.size)
    }

    @Test
    fun `removeCategory does not remove when only 1 category remains`() {
        assertEquals(1, viewModel.state.value.categories.size)

        viewModel.removeCategory(0)

        assertEquals(1, viewModel.state.value.categories.size)
    }

    @Test
    fun `saveTemplate fails when game name is blank`() = runTest {
        viewModel.onGameNameChange("")
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertEquals("Please enter a game name", viewModel.state.value.error)
        assertFalse(viewModel.state.value.templateSaved)
    }

    @Test
    fun `saveTemplate fails when no valid categories`() = runTest {
        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertEquals("Please add at least one score category", viewModel.state.value.error)
        assertFalse(viewModel.state.value.templateSaved)
    }

    @Test
    fun `saveTemplate succeeds with valid minimal input`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.templateSaved)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isSaving)

        coVerify {
            storageManager.saveTemplate(
                match {
                    it.game.name == "Wingspan" &&
                    it.categories.size == 1 &&
                    it.categories[0].name == "Birds"
                },
                "wingspan.xml"
            )
        }
    }

    @Test
    fun `saveTemplate succeeds with all fields populated`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan")
        viewModel.onSubtitleChange("European Edition")
        viewModel.onDesignerChange("Elizabeth Hargrave")
        viewModel.onStudioChange("Stonemaier Games")
        viewModel.onMinPlayersChange("1")
        viewModel.onMaxPlayersChange("5")
        viewModel.onBackgroundImageUrlChange("https://example.com/bg.png")
        viewModel.onPublishYearChange("2019")
        viewModel.onCategoryNameChange(0, "Birds")
        viewModel.onCategoryDescriptionChange(0, "Points from bird cards")
        viewModel.onCategoryIconUrlChange(0, "https://example.com/bird.png")
        viewModel.onCategoryBackgroundImageUrlChange(0, "https://example.com/bird-bg.png")
        viewModel.onCategoryScoringRuleTypeChange(0, "sum")
        viewModel.onCategoryScoreIndexChange(0, "0")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.templateSaved)

        coVerify {
            storageManager.saveTemplate(
                match {
                    it.game.name == "Wingspan" &&
                    it.game.subtitle == "European Edition" &&
                    it.game.designer == "Elizabeth Hargrave" &&
                    it.game.studio == "Stonemaier Games" &&
                    it.game.minPlayers == 1 &&
                    it.game.maxPlayers == 5 &&
                    it.game.backgroundImageUrl == "https://example.com/bg.png" &&
                    it.game.publishYear == 2019 &&
                    it.categories[0].name == "Birds" &&
                    it.categories[0].description == "Points from bird cards" &&
                    it.categories[0].iconUrl == "https://example.com/bird.png" &&
                    it.categories[0].backgroundImageUrl == "https://example.com/bird-bg.png" &&
                    it.categories[0].scoringRuleType == "sum" &&
                    it.categories[0].scoreIndex == 0
                },
                "wingspan.xml"
            )
        }
    }

    @Test
    fun `saveTemplate trims whitespace from inputs`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("  Wingspan  ")
        viewModel.onSubtitleChange("  European Edition  ")
        viewModel.onCategoryNameChange(0, "  Birds  ")
        viewModel.onCategoryDescriptionChange(0, "  Description  ")

        viewModel.saveTemplate()
        advanceUntilIdle()

        coVerify {
            storageManager.saveTemplate(
                match {
                    it.game.name == "Wingspan" &&
                    it.game.subtitle == "European Edition" &&
                    it.categories[0].name == "Birds" &&
                    it.categories[0].description == "Description"
                },
                "wingspan.xml"
            )
        }
    }

    @Test
    fun `saveTemplate filters out blank categories`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")
        viewModel.addCategory()
        viewModel.onCategoryNameChange(1, "   ")  // Blank category

        viewModel.saveTemplate()
        advanceUntilIdle()

        coVerify {
            storageManager.saveTemplate(
                match { it.categories.size == 1 },
                "wingspan.xml"
            )
        }
    }

    @Test
    fun `saveTemplate converts null optional fields correctly`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan")
        viewModel.onMinPlayersChange("invalid")  // Invalid number
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        coVerify {
            storageManager.saveTemplate(
                match {
                    it.game.minPlayers == null &&
                    it.game.subtitle == null &&
                    it.game.designer == null
                },
                "wingspan.xml"
            )
        }
    }

    @Test
    fun `saveTemplate handles storage failure`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns false

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertEquals("Failed to save template", viewModel.state.value.error)
        assertFalse(viewModel.state.value.templateSaved)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `saveTemplate handles storage exception`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } throws Exception("Storage error")

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("Error saving template") == true)
        assertTrue(viewModel.state.value.error?.contains("Storage error") == true)
        assertFalse(viewModel.state.value.templateSaved)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `saveTemplate sets isSaving during save operation`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            true
        }

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")

        assertFalse(viewModel.state.value.isSaving)

        viewModel.saveTemplate()

        assertTrue(viewModel.state.value.isSaving)

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.onGameNameChange("")
        viewModel.saveTemplate()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `resetTemplateSaved resets templateSaved flag`() = runTest {
        every { storageManager.generateUniqueFileName(any()) } returns "wingspan.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan")
        viewModel.onCategoryNameChange(0, "Birds")
        viewModel.saveTemplate()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.templateSaved)

        viewModel.resetTemplateSaved()

        assertFalse(viewModel.state.value.templateSaved)
    }

    @Test
    fun `saveTemplate uses generated unique filename`() = runTest {
        every { storageManager.generateUniqueFileName("Wingspan European") } returns "wingspan_european_1.xml"
        coEvery { storageManager.saveTemplate(any(), any()) } returns true

        viewModel.onGameNameChange("Wingspan European")
        viewModel.onCategoryNameChange(0, "Birds")

        viewModel.saveTemplate()
        advanceUntilIdle()

        coVerify {
            storageManager.generateUniqueFileName("Wingspan European")
            storageManager.saveTemplate(any(), "wingspan_european_1.xml")
        }
    }
}
