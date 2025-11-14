package com.eurogame.scorecard.data.local.dao

import androidx.room.*
import com.eurogame.scorecard.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Delete
    suspend fun delete(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameById(gameId: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE isActive = 1 LIMIT 1")
    fun getActiveGame(): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("UPDATE games SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllGames()
}
