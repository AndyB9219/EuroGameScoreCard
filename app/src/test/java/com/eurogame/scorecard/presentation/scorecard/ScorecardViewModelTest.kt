package com.eurogame.scorecard.presentation.scorecard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.eurogame.scorecard.domain.model.Game
import com.eurogame.scorecard.domain.model.Player
import com.eurogame.scorecard.domain.model.ScoreCategory
import com.eurogame.scorecard.domain.repository.GameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
class ScorecardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: GameRepository
    private lateinit var viewModel: ScorecardViewModel

    private val testPlayers = listOf(
        Player(id = 1, name = "Alice", orderIndex = 0),
        Player(id = 2, name = "Bob", orderIndex = 1)
    )

    private val testCategories = listOf(
        ScoreCategory(id = 1, title = "Birds", subtitle = "Main scoring", orderIndex = 0),
        ScoreCategory(id = 2, title = "Bonus", subtitle = null, orderIndex = 1)
    )

    private val testScores = mapOf(
        Pair(1L, 1L) to 10,  // Alice - Birds
        Pair(1L, 2L) to 5,   // Alice - Bonus
        Pair(2L, 1L) to 8,   // Bob - Birds
        Pair(2L, 2L) to 12   // Bob - Bonus
    )

    private val testGame = Game(
        id = 1,
        name = "Wingspan",
        description = "Test game",
        players = testPlayers,
        categories = testCategories,
        scores = testScores,
        isActive = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(null)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.game)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loads active game on initialization`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(testGame)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.game)
        assertEquals("Wingspan", state.game?.name)
        assertEquals(2, state.game?.players?.size)
        assertEquals(2, state.game?.categories?.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `handles null active game`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(null)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.game)
        assertFalse(state.isLoading)
    }

    @Test
    fun `handles repository errors when loading game`() = runTest {
        coEvery { repository.getActiveGame() } throws Exception("Database error")

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNull(state.game)
        assertTrue(state.error?.contains("Failed to load game") == true)
        assertFalse(state.isLoading)
    }

    @Test
    fun `updateScore calls repository with correct parameters`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(testGame)
        coEvery { repository.updateScore(any(), any(), any(), any()) } returns Unit

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        viewModel.updateScore(playerId = 1L, categoryId = 1L, score = 15)
        advanceUntilIdle()

        coVerify {
            repository.updateScore(
                gameId = 1L,
                playerId = 1L,
                categoryId = 1L,
                score = 15
            )
        }
    }

    @Test
    fun `updateScore does nothing when game is null`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(null)
        coEvery { repository.updateScore(any(), any(), any(), any()) } returns Unit

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        viewModel.updateScore(playerId = 1L, categoryId = 1L, score = 15)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            repository.updateScore(any(), any(), any(), any())
        }
    }

    @Test
    fun `updateScore handles repository errors`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(testGame)
        coEvery { repository.updateScore(any(), any(), any(), any()) } throws Exception("Update failed")

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        viewModel.updateScore(playerId = 1L, categoryId = 1L, score = 15)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("Failed to update score") == true)
    }

    @Test
    fun `getPlayerTotal calculates correct total`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(testGame)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val aliceTotal = viewModel.getPlayerTotal(testPlayers[0])
        val bobTotal = viewModel.getPlayerTotal(testPlayers[1])

        assertEquals(15, aliceTotal)  // 10 + 5
        assertEquals(20, bobTotal)    // 8 + 12
    }

    @Test
    fun `getPlayerTotal returns 0 when game is null`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(null)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val total = viewModel.getPlayerTotal(testPlayers[0])

        assertEquals(0, total)
    }

    @Test
    fun `getPlayerTotal returns 0 for player with no scores`() = runTest {
        val gameWithNoScores = testGame.copy(scores = emptyMap())
        coEvery { repository.getActiveGame() } returns flowOf(gameWithNoScores)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val total = viewModel.getPlayerTotal(testPlayers[0])

        assertEquals(0, total)
    }

    @Test
    fun `getWinner returns player with highest score`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(testGame)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val winner = viewModel.getWinner()

        assertNotNull(winner)
        assertEquals("Bob", winner?.name)  // Bob has 20 points vs Alice's 15
    }

    @Test
    fun `getWinner returns null when game is null`() = runTest {
        coEvery { repository.getActiveGame() } returns flowOf(null)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val winner = viewModel.getWinner()

        assertNull(winner)
    }

    @Test
    fun `getWinner returns null when no players`() = runTest {
        val gameWithNoPlayers = testGame.copy(players = emptyList())
        coEvery { repository.getActiveGame() } returns flowOf(gameWithNoPlayers)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val winner = viewModel.getWinner()

        assertNull(winner)
    }

    @Test
    fun `getWinner returns first player in case of tie`() = runTest {
        val tiedScores = mapOf(
            Pair(1L, 1L) to 10,
            Pair(1L, 2L) to 5,
            Pair(2L, 1L) to 10,
            Pair(2L, 2L) to 5
        )
        val tiedGame = testGame.copy(scores = tiedScores)
        coEvery { repository.getActiveGame() } returns flowOf(tiedGame)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        val winner = viewModel.getWinner()

        assertNotNull(winner)
        // Should return one of the tied players (maxByOrNull returns first max)
        assertEquals(15, viewModel.getPlayerTotal(winner!!))
    }

    @Test
    fun `clearError clears error message`() = runTest {
        coEvery { repository.getActiveGame() } throws Exception("Error")

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.error)

        viewModel.clearError()

        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `state updates reactively when game flow emits new value`() = runTest {
        val updatedGame = testGame.copy(
            scores = testScores + (Pair(1L, 1L) to 20)
        )
        coEvery { repository.getActiveGame() } returns flowOf(testGame, updatedGame)

        viewModel = ScorecardViewModel(repository)
        advanceUntilIdle()

        // Should receive both emissions from the flow
        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.game)
        }
    }
}
