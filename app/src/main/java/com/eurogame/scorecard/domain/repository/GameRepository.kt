package com.eurogame.scorecard.domain.repository

import com.eurogame.scorecard.domain.model.Game
import com.eurogame.scorecard.domain.model.GameSetupData
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    suspend fun createGame(setupData: GameSetupData): Long
    suspend fun updateScore(gameId: Long, playerId: Long, categoryId: Long, score: Int)
    suspend fun deleteGame(gameId: Long)
    fun getActiveGame(): Flow<Game?>
    fun getGameById(gameId: Long): Flow<Game?>
    fun getAllGames(): Flow<List<Game>>
}
