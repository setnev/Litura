package com.litura.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_friends")
data class MockFriendEntity(
    @PrimaryKey val friendId: String,
    val displayName: String,
    val avatarId: String,
    val totalXp: Int,
    val totalBitesCompleted: Int,
    val avgCompetency: Double,
    val currentStreakDays: Int,
    val dailyXpRate: Int,
    val dailyBiteRate: Int,
    val seededAt: Long
)
