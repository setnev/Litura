package com.litura.app.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "reading_progress",
    primaryKeys = ["userId", "bookId"]
)
data class ReadingProgressEntity(
    val userId: String,
    val bookId: String,
    val status: String,
    val currentBiteId: String?,
    val completedBites: Int,
    val totalBites: Int,
    val percentComplete: Double,
    val averageCompetency: Double,
    val averageAttempts: Double,
    val avgTimePerQuestionMs: Long,
    val totalXpEarned: Int,
    val startedAt: Long?,
    val lastReadAt: Long?,
    val completedAt: Long?
)
