package com.eurogame.scorecard.data.repository

import app.cash.turbine.test
import com.eurogame.scorecard.data.local.dao.GameDao
import com.eurogame.scorecard.data.local.dao.PlayerDao
import com.eurogame.scorecard.data.local.dao.PlayerScoreDao
import com.eurogame.scorecard.data.local.dao.ScoreCategoryDao
import com.eurogame.scorecard.data.local.entity.GameEntity
import com.eurogame.scorecard.data.local.entity.PlayerEntity
import com.eurogame.scorecard.data.local.entity.PlayerScoreEntity
import com.eurogame.scorecard.data.local.entity.ScoreCategoryEntity
import com.eurogame.scorecard.domain.model.CategoryInput
import com.eurogame.scorecard.domain.model.GameSetupData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {

    private lateinit var gameDao: GameDao
    private lateinit var playerDao: PlayerDao
    private lateinit var scoreCategoryDao: ScoreCategoryDao
    private lateinit var playerScoreDao: PlayerScoreDao
    private lateinit var repository: GameRepositoryImpl

    @Before
    fun setup() {
        gameDao = mockk()
        playerDao = mockk()
        scoreCategoryDao = mockk()
        playerScoreDao = mockk()

        repository = GameRepositoryImpl(
            gameDao = gameDao,
            playerDao = playerDao,
            scoreCategoryDao = scoreCategoryDao,
            playerScoreDao = playerScoreDao
        )
    }

    @Test
    fun `createGame deactivates existing games before creating new one`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test game",
            playerNames = listOf("Alice", "Bob"),
            categories = listOf(
                CategoryInput("Birds", "Main scoring"),
                CategoryInput("Bonus", null)
            )
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 1L
        coEvery { playerDao.insertAll(any()) } returns listOf(1L, 2L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(1L, 2L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L, 2L, 3L, 4L)

        repository.createGame(setupData)

        coVerify { gameDao.deactivateAllGames() }
    }

    @Test
    fun `createGame inserts game with correct data`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test game",
            playerNames = listOf("Alice", "Bob"),
            categories = listOf(CategoryInput("Birds", "Main scoring"))
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 1L
        coEvery { playerDao.insertAll(any()) } returns listOf(1L, 2L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(1L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L, 2L)

        repository.createGame(setupData)

        coVerify {
            gameDao.insert(
                match {
                    it.name == "Wingspan" &&
                    it.description == "Test game" &&
                    it.isActive == true
                }
            )
        }
    }

    @Test
    fun `createGame inserts players with correct order indices`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test game",
            playerNames = listOf("Alice", "Bob", "Charlie"),
            categories = listOf(CategoryInput("Birds", null))
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 1L
        coEvery { playerDao.insertAll(any()) } returns listOf(1L, 2L, 3L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(1L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L, 2L, 3L)

        repository.createGame(setupData)

        coVerify {
            playerDao.insertAll(
                match { players ->
                    players.size == 3 &&
                    players[0].name == "Alice" && players[0].orderIndex == 0 &&
                    players[1].name == "Bob" && players[1].orderIndex == 1 &&
                    players[2].name == "Charlie" && players[2].orderIndex == 2
                }
            )
        }
    }

    @Test
    fun `createGame inserts categories with correct order indices and subtitles`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test",
            playerNames = listOf("Alice", "Bob"),
            categories = listOf(
                CategoryInput("Birds", "Main scoring"),
                CategoryInput("Bonus", null)
            )
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 1L
        coEvery { playerDao.insertAll(any()) } returns listOf(1L, 2L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(1L, 2L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L, 2L, 3L, 4L)

        repository.createGame(setupData)

        coVerify {
            scoreCategoryDao.insertAll(
                match { categories ->
                    categories.size == 2 &&
                    categories[0].title == "Birds" &&
                    categories[0].subtitle == "Main scoring" &&
                    categories[0].orderIndex == 0 &&
                    categories[1].title == "Bonus" &&
                    categories[1].subtitle == null &&
                    categories[1].orderIndex == 1
                }
            )
        }
    }

    @Test
    fun `createGame initializes all scores to 0`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test",
            playerNames = listOf("Alice", "Bob"),
            categories = listOf(
                CategoryInput("Birds", null),
                CategoryInput("Bonus", null)
            )
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 1L
        coEvery { playerDao.insertAll(any()) } returns listOf(10L, 20L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(100L, 200L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L, 2L, 3L, 4L)

        repository.createGame(setupData)

        coVerify {
            playerScoreDao.insertAll(
                match { scores ->
                    scores.size == 4 &&  // 2 players × 2 categories
                    scores.all { it.score == 0 } &&
                    scores.any { it.playerId == 10L && it.categoryId == 100L } &&
                    scores.any { it.playerId == 10L && it.categoryId == 200L } &&
                    scores.any { it.playerId == 20L && it.categoryId == 100L } &&
                    scores.any { it.playerId == 20L && it.categoryId == 200L }
                }
            )
        }
    }

    @Test
    fun `createGame returns game id`() = runTest {
        val setupData = GameSetupData(
            gameName = "Wingspan",
            description = "Test",
            playerNames = listOf("Alice"),
            categories = listOf(CategoryInput("Birds", null))
        )

        coEvery { gameDao.deactivateAllGames() } just Runs
        coEvery { gameDao.insert(any()) } returns 42L
        coEvery { playerDao.insertAll(any()) } returns listOf(1L)
        coEvery { scoreCategoryDao.insertAll(any()) } returns listOf(1L)
        coEvery { playerScoreDao.insertAll(any()) } returns listOf(1L)

        val gameId = repository.createGame(setupData)

        assertEquals(42L, gameId)
    }

    @Test
    fun `updateScore upserts score entity`() = runTest {
        coEvery { playerScoreDao.upsert(any()) } just Runs

        repository.updateScore(gameId = 1L, playerId = 10L, categoryId = 20L, score = 15)

        coVerify {
            playerScoreDao.upsert(
                match {
                    it.playerId == 10L &&
                    it.categoryId == 20L &&
                    it.score == 15
                }
            )
        }
    }

    @Test
    fun `getActiveGame returns null when no active game`() = runTest {
        coEvery { gameDao.getActiveGame() } returns flowOf(null)

        repository.getActiveGame().test {
            val game = awaitItem()
            assertNull(game)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveGame builds complete game from entities`() = runTest {
        val gameEntity = GameEntity(
            id = 1L,
            name = "Wingspan",
            description = "Test game",
            isActive = true
        )

        val playerEntities = listOf(
            PlayerEntity(id = 10L, gameId = 1L, name = "Alice", orderIndex = 0),
            PlayerEntity(id = 20L, gameId = 1L, name = "Bob", orderIndex = 1)
        )

        val categoryEntities = listOf(
            ScoreCategoryEntity(id = 100L, gameId = 1L, title = "Birds", subtitle = "Main", orderIndex = 0)
        )

        val scoreEntities = listOf(
            PlayerScoreEntity(id = 1L, playerId = 10L, categoryId = 100L, score = 15),
            PlayerScoreEntity(id = 2L, playerId = 20L, categoryId = 100L, score = 20)
        )

        coEvery { gameDao.getActiveGame() } returns flowOf(gameEntity)
        coEvery { playerDao.getPlayersByGameId(1L) } returns flowOf(playerEntities)
        coEvery { scoreCategoryDao.getCategoriesByGameId(1L) } returns flowOf(categoryEntities)
        coEvery { playerScoreDao.getScoresByGameId(1L) } returns flowOf(scoreEntities)

        repository.getActiveGame().test {
            val game = awaitItem()

            assertNotNull(game)
            assertEquals("Wingspan", game?.name)
            assertEquals("Test game", game?.description)
            assertEquals(2, game?.players?.size)
            assertEquals(1, game?.categories?.size)
            assertEquals(15, game?.scores?.get(Pair(10L, 100L)))
            assertEquals(20, game?.scores?.get(Pair(20L, 100L)))
            assertTrue(game?.isActive == true)

            awaitComplete()
        }
    }

    @Test
    fun `getGameById returns null when game not found`() = runTest {
        coEvery { gameDao.getGameById(999L) } returns flowOf(null)

        repository.getGameById(999L).test {
            val game = awaitItem()
            assertNull(game)
            awaitComplete()
        }
    }

    @Test
    fun `deleteGame deletes game entity`() = runTest {
        val gameEntity = GameEntity(id = 1L, name = "Test", description = "", isActive = true)

        coEvery { gameDao.getGameById(1L) } returns flowOf(gameEntity)
        coEvery { gameDao.delete(any()) } just Runs

        repository.deleteGame(1L)

        coVerify { gameDao.delete(gameEntity) }
    }

    @Test
    fun `getAllGames returns empty list when no games`() = runTest {
        coEvery { gameDao.getAllGames() } returns flowOf(emptyList())

        repository.getAllGames().test {
            val games = awaitItem()
            assertTrue(games.isEmpty())
            awaitComplete()
        }
    }
}
