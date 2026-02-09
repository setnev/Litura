package com.litura.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "earned_badges",
    primaryKeys = ["userId", "badgeId"]
)
data class BadgeEntity(
    val userId: String,
    val badgeId: String,
    val bookId: String,
    val badgeType: String,
    val earnedAt: Long
)
