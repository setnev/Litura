package com.litura.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "bite_progress",
    primaryKeys = ["userId", "biteId"]
)
data class BiteProgressEntity(
    val userId: String,
    val biteId: String,
    val bookId: String,
    val isCompleted: Boolean,
    val xpEarned: Int,
    val competencyPercent: Double,
    val timeSpentMs: Long,
    val completedAt: Long?
)
