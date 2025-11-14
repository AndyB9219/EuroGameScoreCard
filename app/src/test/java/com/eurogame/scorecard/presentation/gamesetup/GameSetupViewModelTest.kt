package com.eurogame.scorecard.presentation.gamesetup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.eurogame.scorecard.data.storage.TemplateStorageManager
import com.eurogame.scorecard.data.xml.CategoryTemplate
import com.eurogame.scorecard.data.xml.GameTemplate
import com.eurogame.scorecard.data.xml.ScorecardTemplate
import com.eurogame.scorecard.domain.model.CategoryInput
import com.eurogame.scorecard.domain.model.GameSetupData
import com.eurogame.scorecard.domain.repository.GameRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class GameSetupViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: GameRepository
    private lateinit var storageManager: TemplateStorageManager
    private lateinit var viewModel: GameSetupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        storageManager = mockk()
        viewModel = GameSetupViewModel(repository, storageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.state.value

        assertEquals("", state.gameName)
        assertEquals("", state.description)
        assertEquals(2, state.playerNames.size)
        assertEquals(1, state.categories.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.gameCreated)
    }

    @Test
    fun `onGameNameChange updates game name`() {
        viewModel.onGameNameChange("Wingspan")

        assertEquals("Wingspan", viewModel.state.value.gameName)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        viewModel.onDescriptionChange("Test game description")

        assertEquals("Test game description", viewModel.state.value.description)
    }

    @Test
    fun `onPlayerNameChange updates specific player name`() {
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")

        val playerNames = viewModel.state.value.playerNames
        assertEquals("Alice", playerNames[0])
        assertEquals("Bob", playerNames[1])
    }

    @Test
    fun `addPlayer increases player count`() {
        assertEquals(2, viewModel.state.value.playerNames.size)

        viewModel.addPlayer()

        assertEquals(3, viewModel.state.value.playerNames.size)
    }

    @Test
    fun `removePlayer decreases player count when more than 2 players`() {
        viewModel.addPlayer()
        viewModel.addPlayer()
        assertEquals(4, viewModel.state.value.playerNames.size)

        viewModel.removePlayer(2)

        assertEquals(3, viewModel.state.value.playerNames.size)
    }

    @Test
    fun `removePlayer does not remove when only 2 players remain`() {
        assertEquals(2, viewModel.state.value.playerNames.size)

        viewModel.removePlayer(0)

        assertEquals(2, viewModel.state.value.playerNames.size)
    }

    @Test
    fun `onCategoryTitleChange updates category title`() {
        viewModel.onCategoryTitleChange(0, "Birds")

        assertEquals("Birds", viewModel.state.value.categories[0].title)
    }

    @Test
    fun `onCategorySubtitleChange updates category subtitle`() {
        viewModel.onCategorySubtitleChange(0, "End of round bonuses")

        assertEquals("End of round bonuses", viewModel.state.value.categories[0].subtitle)
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
    fun `createGame fails when game name is blank`() = runTest {
        viewModel.onGameNameChange("")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.onCategoryTitleChange(0, "Points")

        viewModel.createGame()
        advanceUntilIdle()

        assertEquals("Please enter a game name", viewModel.state.value.error)
        assertFalse(viewModel.state.value.gameCreated)
    }

    @Test
    fun `createGame fails when less than 2 valid player names`() = runTest {
        viewModel.onGameNameChange("Wingspan")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "")
        viewModel.onCategoryTitleChange(0, "Points")

        viewModel.createGame()
        advanceUntilIdle()

        assertEquals("Please enter at least 2 player names", viewModel.state.value.error)
        assertFalse(viewModel.state.value.gameCreated)
    }

    @Test
    fun `createGame fails when no valid categories`() = runTest {
        viewModel.onGameNameChange("Wingspan")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.onCategoryTitleChange(0, "")

        viewModel.createGame()
        advanceUntilIdle()

        assertEquals("Please add at least one score category", viewModel.state.value.error)
        assertFalse(viewModel.state.value.gameCreated)
    }

    @Test
    fun `createGame succeeds with valid input`() = runTest {
        coEvery { repository.createGame(any()) } returns 1L

        viewModel.onGameNameChange("Wingspan")
        viewModel.onDescriptionChange("Fun game night")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.onCategoryTitleChange(0, "Birds")
        viewModel.onCategorySubtitleChange(0, "Main scoring")

        viewModel.createGame()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.gameCreated)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)

        coVerify {
            repository.createGame(
                match {
                    it.gameName == "Wingspan" &&
                    it.description == "Fun game night" &&
                    it.playerNames == listOf("Alice", "Bob") &&
                    it.categories.size == 1 &&
                    it.categories[0].title == "Birds" &&
                    it.categories[0].subtitle == "Main scoring"
                }
            )
        }
    }

    @Test
    fun `createGame trims whitespace from inputs`() = runTest {
        coEvery { repository.createGame(any()) } returns 1L

        viewModel.onGameNameChange("  Wingspan  ")
        viewModel.onDescriptionChange("  Description  ")
        viewModel.onPlayerNameChange(0, "  Alice  ")
        viewModel.onPlayerNameChange(1, "  Bob  ")
        viewModel.onCategoryTitleChange(0, "  Birds  ")
        viewModel.onCategorySubtitleChange(0, "  Subtitle  ")

        viewModel.createGame()
        advanceUntilIdle()

        coVerify {
            repository.createGame(
                match {
                    it.gameName == "Wingspan" &&
                    it.description == "Description" &&
                    it.playerNames == listOf("Alice", "Bob") &&
                    it.categories[0].title == "Birds" &&
                    it.categories[0].subtitle == "Subtitle"
                }
            )
        }
    }

    @Test
    fun `createGame filters out blank player names and categories`() = runTest {
        coEvery { repository.createGame(any()) } returns 1L

        viewModel.onGameNameChange("Wingspan")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.addPlayer()
        viewModel.onPlayerNameChange(2, "   ")  // Blank player name

        viewModel.onCategoryTitleChange(0, "Birds")
        viewModel.addCategory()
        viewModel.onCategoryTitleChange(1, "   ")  // Blank category

        viewModel.createGame()
        advanceUntilIdle()

        coVerify {
            repository.createGame(
                match {
                    it.playerNames.size == 2 &&
                    it.categories.size == 1
                }
            )
        }
    }

    @Test
    fun `createGame handles repository errors`() = runTest {
        coEvery { repository.createGame(any()) } throws Exception("Database error")

        viewModel.onGameNameChange("Wingspan")
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.onCategoryTitleChange(0, "Birds")

        viewModel.createGame()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("Failed to create game") == true)
        assertFalse(viewModel.state.value.gameCreated)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `resetGameCreated resets gameCreated flag`() {
        viewModel.onGameNameChange("Wingspan")
        viewModel.state.value.copy(gameCreated = true)

        viewModel.resetGameCreated()

        assertFalse(viewModel.state.value.gameCreated)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        viewModel.onGameNameChange("")
        viewModel.createGame()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    // Template loading tests

    @Test
    fun `loadFromTemplate populates all game fields from template`() = runTest {
        val template = ScorecardTemplate(
            game = GameTemplate(
                name = "Wingspan",
                subtitle = "European Edition",
                designer = "Elizabeth Hargrave",
                studio = "Stonemaier Games",
                minPlayers = 1,
                maxPlayers = 5,
                backgroundImageUrl = "https://example.com/bg.png",
                publishYear = 2019
            ),
            categories = listOf(
                CategoryTemplate(
                    name = "Birds",
                    description = "Points from bird cards",
                    iconUrl = "https://example.com/bird.png",
                    backgroundImageUrl = "https://example.com/bird-bg.png",
                    scoringRuleType = "sum",
                    scoreIndex = 0
                )
            )
        )

        coEvery { storageManager.loadTemplate("wingspan.xml") } returns template

        viewModel.loadFromTemplate("wingspan.xml")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Wingspan", state.gameName)
        assertEquals("European Edition", state.subtitle)
        assertEquals("Elizabeth Hargrave", state.designer)
        assertEquals("Stonemaier Games", state.studio)
        assertEquals("1", state.minPlayers)
        assertEquals("5", state.maxPlayers)
        assertEquals("https://example.com/bg.png", state.backgroundImageUrl)
        assertEquals("2019", state.publishYear)
        assertEquals("wingspan.xml", state.templateId)
    }

    @Test
    fun `loadFromTemplate populates categories from template`() = runTest {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "Test Game"),
            categories = listOf(
                CategoryTemplate(
                    name = "Category 1",
                    description = "Description 1",
                    iconUrl = "https://example.com/icon1.png",
                    backgroundImageUrl = "https://example.com/bg1.png",
                    scoringRuleType = "sum",
                    scoreIndex = 0
                ),
                CategoryTemplate(
                    name = "Category 2",
                    description = "Description 2",
                    iconUrl = "https://example.com/icon2.png",
                    backgroundImageUrl = null,
                    scoringRuleType = "max",
                    scoreIndex = 1
                )
            )
        )

        coEvery { storageManager.loadTemplate("test.xml") } returns template

        viewModel.loadFromTemplate("test.xml")
        advanceUntilIdle()

        val categories = viewModel.state.value.categories
        assertEquals(2, categories.size)

        assertEquals("Category 1", categories[0].title)
        assertEquals("Description 1", categories[0].description)
        assertEquals("https://example.com/icon1.png", categories[0].iconUrl)
        assertEquals("https://example.com/bg1.png", categories[0].backgroundImageUrl)
        assertEquals("sum", categories[0].scoringRuleType)
        assertEquals(0, categories[0].scoreIndex)

        assertEquals("Category 2", categories[1].title)
        assertEquals("Description 2", categories[1].description)
        assertEquals("https://example.com/icon2.png", categories[1].iconUrl)
        assertEquals("", categories[1].backgroundImageUrl)
        assertEquals("max", categories[1].scoringRuleType)
        assertEquals(1, categories[1].scoreIndex)
    }

    @Test
    fun `loadFromTemplate handles null optional fields`() = runTest {
        val template = ScorecardTemplate(
            game = GameTemplate(
                name = "Test Game",
                subtitle = null,
                designer = null,
                studio = null,
                minPlayers = null,
                maxPlayers = null,
                backgroundImageUrl = null,
                publishYear = null
            ),
            categories = listOf(
                CategoryTemplate(
                    name = "Category",
                    description = null,
                    iconUrl = null,
                    backgroundImageUrl = null,
                    scoringRuleType = null,
                    scoreIndex = 0
                )
            )
        )

        coEvery { storageManager.loadTemplate("test.xml") } returns template

        viewModel.loadFromTemplate("test.xml")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Test Game", state.gameName)
        assertEquals("", state.subtitle)
        assertEquals("", state.designer)
        assertEquals("", state.studio)
        assertEquals("", state.minPlayers)
        assertEquals("", state.maxPlayers)
        assertEquals("", state.backgroundImageUrl)
        assertEquals("", state.publishYear)

        assertEquals("Category", state.categories[0].title)
        assertEquals("", state.categories[0].description)
        assertEquals("", state.categories[0].iconUrl)
        assertEquals("", state.categories[0].backgroundImageUrl)
        assertEquals("", state.categories[0].scoringRuleType)
    }

    @Test
    fun `loadFromTemplate handles null template`() = runTest {
        coEvery { storageManager.loadTemplate("nonexistent.xml") } returns null

        viewModel.loadFromTemplate("nonexistent.xml")
        advanceUntilIdle()

        // State should remain unchanged when template is null
        val state = viewModel.state.value
        assertEquals("", state.gameName)
        assertNull(state.templateId)
    }

    @Test
    fun `loadFromTemplate handles storage exception`() = runTest {
        coEvery { storageManager.loadTemplate("test.xml") } throws Exception("Storage error")

        viewModel.loadFromTemplate("test.xml")
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("Failed to load template") == true)
        assertTrue(viewModel.state.value.error?.contains("Storage error") == true)
    }

    @Test
    fun `loadFromTemplate sets templateId for tracking`() = runTest {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "Test Game"),
            categories = listOf(CategoryTemplate(name = "Cat1", scoreIndex = 0))
        )

        coEvery { storageManager.loadTemplate("my_template.xml") } returns template

        viewModel.loadFromTemplate("my_template.xml")
        advanceUntilIdle()

        assertEquals("my_template.xml", viewModel.state.value.templateId)
    }

    @Test
    fun `createGame includes templateId when creating from template`() = runTest {
        val template = ScorecardTemplate(
            game = GameTemplate(name = "Wingspan"),
            categories = listOf(CategoryTemplate(name = "Birds", scoreIndex = 0))
        )

        coEvery { storageManager.loadTemplate("wingspan.xml") } returns template
        coEvery { repository.createGame(any()) } returns 1L

        viewModel.loadFromTemplate("wingspan.xml")
        advanceUntilIdle()

        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")
        viewModel.createGame()
        advanceUntilIdle()

        coVerify {
            repository.createGame(
                match {
                    it.gameName == "Wingspan" &&
                    it.templateId == "wingspan.xml"
                }
            )
        }
    }

    @Test
    fun `loadFromTemplate preserves player names if already set`() = runTest {
        viewModel.onPlayerNameChange(0, "Alice")
        viewModel.onPlayerNameChange(1, "Bob")

        val template = ScorecardTemplate(
            game = GameTemplate(name = "Test Game"),
            categories = listOf(CategoryTemplate(name = "Cat1", scoreIndex = 0))
        )

        coEvery { storageManager.loadTemplate("test.xml") } returns template

        viewModel.loadFromTemplate("test.xml")
        advanceUntilIdle()

        // Player names should be preserved (not reset by template)
        assertEquals("Alice", viewModel.state.value.playerNames[0])
        assertEquals("Bob", viewModel.state.value.playerNames[1])
    }
}
