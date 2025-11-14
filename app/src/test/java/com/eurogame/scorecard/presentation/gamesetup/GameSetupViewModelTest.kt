package com.eurogame.scorecard.presentation.gamesetup

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
    private lateinit var viewModel: GameSetupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = GameSetupViewModel(repository)
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
}
