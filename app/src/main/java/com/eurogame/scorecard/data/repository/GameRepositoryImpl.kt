package com.eurogame.scorecard.data.repository

import com.eurogame.scorecard.data.local.dao.GameDao
import com.eurogame.scorecard.data.local.dao.PlayerDao
import com.eurogame.scorecard.data.local.dao.PlayerScoreDao
import com.eurogame.scorecard.data.local.dao.ScoreCategoryDao
import com.eurogame.scorecard.data.local.entity.GameEntity
import com.eurogame.scorecard.data.local.entity.PlayerEntity
import com.eurogame.scorecard.data.local.entity.PlayerScoreEntity
import com.eurogame.scorecard.data.local.entity.ScoreCategoryEntity
import com.eurogame.scorecard.domain.model.Game
import com.eurogame.scorecard.domain.model.GameSetupData
import com.eurogame.scorecard.domain.model.Player
import com.eurogame.scorecard.domain.model.ScoreCategory
import com.eurogame.scorecard.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val playerDao: PlayerDao,
    private val scoreCategoryDao: ScoreCategoryDao,
    private val playerScoreDao: PlayerScoreDao
) : GameRepository {

    override suspend fun createGame(setupData: GameSetupData): Long {
        // Deactivate any existing active games
        gameDao.deactivateAllGames()

        // Create game
        val gameId = gameDao.insert(
            GameEntity(
                name = setupData.gameName,
                description = setupData.description,
                subtitle = setupData.subtitle,
                designer = setupData.designer,
                studio = setupData.studio,
                minPlayers = setupData.minPlayers,
                maxPlayers = setupData.maxPlayers,
                backgroundImageUrl = setupData.backgroundImageUrl,
                publishYear = setupData.publishYear,
                templateId = setupData.templateId,
                isActive = true
            )
        )

        // Create players
        val playerEntities = setupData.playerNames.mapIndexed { index, name ->
            PlayerEntity(
                gameId = gameId,
                name = name,
                orderIndex = index
            )
        }
        val playerIds = playerDao.insertAll(playerEntities)

        // Create categories
        val categoryEntities = setupData.categories.mapIndexed { index, category ->
            ScoreCategoryEntity(
                gameId = gameId,
                title = category.title,
                subtitle = category.subtitle,
                description = category.description,
                iconUrl = category.iconUrl,
                backgroundImageUrl = category.backgroundImageUrl,
                scoringRuleType = category.scoringRuleType,
                scoreIndex = category.scoreIndex,
                orderIndex = index,
                isOptional = category.isOptional,
                isEnabled = true  // All categories are enabled by default when game is created
            )
        }
        val categoryIds = scoreCategoryDao.insertAll(categoryEntities)

        // Initialize all scores to 0
        val scores = mutableListOf<PlayerScoreEntity>()
        for (playerId in playerIds) {
            for (categoryId in categoryIds) {
                scores.add(
                    PlayerScoreEntity(
                        playerId = playerId,
                        categoryId = categoryId,
                        score = 0
                    )
                )
            }
        }
        playerScoreDao.insertAll(scores)

        return gameId
    }

    override suspend fun updateScore(gameId: Long, playerId: Long, categoryId: Long, score: Int) {
        playerScoreDao.upsert(
            PlayerScoreEntity(
                playerId = playerId,
                categoryId = categoryId,
                score = score
            )
        )
    }

    override suspend fun deleteGame(gameId: Long) {
        gameDao.getGameById(gameId).collect { game ->
            game?.let { gameDao.delete(it) }
        }
    }

    override fun getActiveGame(): Flow<Game?> {
        return gameDao.getActiveGame().combine(Unit) { game, _ ->
            game?.let { buildGameFromEntity(it) }
        }
    }

    override fun getGameById(gameId: Long): Flow<Game?> {
        return gameDao.getGameById(gameId).combine(Unit) { game, _ ->
            game?.let { buildGameFromEntity(it) }
        }
    }

    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames().combine(Unit) { games, _ ->
            games.map { buildGameFromEntity(it) }
        }
    }

    private suspend fun buildGameFromEntity(gameEntity: GameEntity): Game {
        val players = mutableListOf<Player>()
        val categories = mutableListOf<ScoreCategory>()
        val scores = mutableMapOf<Pair<Long, Long>, Int>()

        // Get players
        playerDao.getPlayersByGameId(gameEntity.id).collect { playerEntities ->
            players.clear()
            players.addAll(playerEntities.map {
                Player(
                    id = it.id,
                    name = it.name,
                    orderIndex = it.orderIndex
                )
            })
        }

        // Get categories
        scoreCategoryDao.getCategoriesByGameId(gameEntity.id).collect { categoryEntities ->
            categories.clear()
            categories.addAll(categoryEntities.map {
                ScoreCategory(
                    id = it.id,
                    title = it.title,
                    subtitle = it.subtitle,
                    description = it.description,
                    iconUrl = it.iconUrl,
                    backgroundImageUrl = it.backgroundImageUrl,
                    scoringRuleType = it.scoringRuleType,
                    scoreIndex = it.scoreIndex,
                    orderIndex = it.orderIndex,
                    isOptional = it.isOptional,
                    isEnabled = it.isEnabled
                )
            })
        }

        // Get scores
        playerScoreDao.getScoresByGameId(gameEntity.id).collect { scoreEntities ->
            scores.clear()
            scoreEntities.forEach {
                scores[Pair(it.playerId, it.categoryId)] = it.score
            }
        }

        return Game(
            id = gameEntity.id,
            name = gameEntity.name,
            description = gameEntity.description,
            subtitle = gameEntity.subtitle,
            designer = gameEntity.designer,
            studio = gameEntity.studio,
            minPlayers = gameEntity.minPlayers,
            maxPlayers = gameEntity.maxPlayers,
            backgroundImageUrl = gameEntity.backgroundImageUrl,
            publishYear = gameEntity.publishYear,
            templateId = gameEntity.templateId,
            players = players,
            categories = categories,
            scores = scores,
            createdAt = gameEntity.createdAt,
            isActive = gameEntity.isActive
        )
    }
}
