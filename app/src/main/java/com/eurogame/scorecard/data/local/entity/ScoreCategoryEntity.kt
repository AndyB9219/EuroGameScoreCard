package com.eurogame.scorecard.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "score_categories",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class ScoreCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val backgroundImageUrl: String? = null,
    val scoringRuleType: String? = null,
    val scoreIndex: Int = 0,
    val orderIndex: Int,
    val isOptional: Boolean = false,
    val isEnabled: Boolean = true
)
