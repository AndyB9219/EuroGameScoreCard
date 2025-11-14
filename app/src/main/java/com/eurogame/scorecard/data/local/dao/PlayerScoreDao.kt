package com.eurogame.scorecard.data.local.dao

import androidx.room.*
import com.eurogame.scorecard.data.local.entity.PlayerScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: PlayerScoreEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(scores: List<PlayerScoreEntity>): List<Long>

    @Update
    suspend fun update(score: PlayerScoreEntity)

    @Upsert
    suspend fun upsert(score: PlayerScoreEntity)

    @Delete
    suspend fun delete(score: PlayerScoreEntity)

    @Query("SELECT * FROM player_scores WHERE playerId = :playerId")
    fun getScoresByPlayerId(playerId: Long): Flow<List<PlayerScoreEntity>>

    @Query("SELECT * FROM player_scores WHERE playerId = :playerId AND categoryId = :categoryId")
    fun getScore(playerId: Long, categoryId: Long): Flow<PlayerScoreEntity?>

    @Query("""
        SELECT * FROM player_scores
        WHERE playerId IN (SELECT id FROM players WHERE gameId = :gameId)
    """)
    fun getScoresByGameId(gameId: Long): Flow<List<PlayerScoreEntity>>
}
