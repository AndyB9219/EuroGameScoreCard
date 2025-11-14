package com.eurogame.scorecard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val subtitle: String? = null,
    val designer: String? = null,
    val studio: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val backgroundImageUrl: String? = null,
    val publishYear: Int? = null,
    val templateId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
