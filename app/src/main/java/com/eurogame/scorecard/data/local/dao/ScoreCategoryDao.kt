package com.eurogame.scorecard.data.local.dao

import androidx.room.*
import com.eurogame.scorecard.data.local.entity.ScoreCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ScoreCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<ScoreCategoryEntity>): List<Long>

    @Update
    suspend fun update(category: ScoreCategoryEntity)

    @Delete
    suspend fun delete(category: ScoreCategoryEntity)

    @Query("SELECT * FROM score_categories WHERE gameId = :gameId ORDER BY orderIndex ASC")
    fun getCategoriesByGameId(gameId: Long): Flow<List<ScoreCategoryEntity>>

    @Query("SELECT * FROM score_categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Flow<ScoreCategoryEntity?>
}
