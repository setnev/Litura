package com.litura.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_events")
data class TelemetryEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val timestamp: Long,
    val bookId: String?,
    val biteId: String?,
    val questionId: String?,
    val attemptNumber: Int?,
    val timeToAnswerMs: Long?,
    val usedReviewText: Boolean?,
    val xpAwarded: Int?,
    val healthDelta: Int?,
    val competencyPercent: Double?
)
