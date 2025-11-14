package com.eurogame.scorecard.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eurogame.scorecard.data.local.dao.GameDao
import com.eurogame.scorecard.data.local.dao.PlayerDao
import com.eurogame.scorecard.data.local.dao.PlayerScoreDao
import com.eurogame.scorecard.data.local.dao.ScoreCategoryDao
import com.eurogame.scorecard.data.local.entity.GameEntity
import com.eurogame.scorecard.data.local.entity.PlayerEntity
import com.eurogame.scorecard.data.local.entity.PlayerScoreEntity
import com.eurogame.scorecard.data.local.entity.ScoreCategoryEntity

@Database(
    entities = [
        GameEntity::class,
        PlayerEntity::class,
        ScoreCategoryEntity::class,
        PlayerScoreEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun scoreCategoryDao(): ScoreCategoryDao
    abstract fun playerScoreDao(): PlayerScoreDao
}
