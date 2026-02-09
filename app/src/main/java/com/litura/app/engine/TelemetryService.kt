package com.litura.app.engine

import com.litura.app.data.local.dao.TelemetryDao
import com.litura.app.data.local.entity.TelemetryEventEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryService @Inject constructor(
    private val telemetryDao: TelemetryDao
) {
    suspend fun logSessionStart() {
        insert("SESSION_START")
    }

    suspend fun logBiteStarted(bookId: String, biteId: String) {
        insert("BITE_STARTED", bookId = bookId, biteId = biteId)
    }

    suspend fun logQuestionAttempt(
        bookId: String,
        biteId: String,
        questionId: String,
        attemptNumber: Int,
        timeToAnswerMs: Long,
        xpAwarded: Int
    ) {
        insert(
            "QUESTION_ATTEMPT",
            bookId = bookId,
            biteId = biteId,
            questionId = questionId,
            attemptNumber = attemptNumber,
            timeToAnswerMs = timeToAnswerMs,
            xpAwarded = xpAwarded
        )
    }

    suspend fun logBiteCompleted(bookId: String, biteId: String, competencyPercent: Double) {
        insert(
            "BITE_COMPLETED",
            bookId = bookId,
            biteId = biteId,
            competencyPercent = competencyPercent
        )
    }

    suspend fun logHealthChanged(healthDelta: Int) {
        insert("HEALTH_CHANGED", healthDelta = healthDelta)
    }

    suspend fun logReviewTextOpened(bookId: String, biteId: String) {
        insert(
            "REVIEW_TEXT_OPENED",
            bookId = bookId,
            biteId = biteId,
            usedReviewText = true
        )
    }

    private suspend fun insert(
        eventType: String,
        bookId: String? = null,
        biteId: String? = null,
        questionId: String? = null,
        attemptNumber: Int? = null,
        timeToAnswerMs: Long? = null,
        usedReviewText: Boolean? = null,
        xpAwarded: Int? = null,
        healthDelta: Int? = null,
        competencyPercent: Double? = null
    ) {
        telemetryDao.insertEvent(
            TelemetryEventEntity(
                eventType = eventType,
                timestamp = System.currentTimeMillis(),
                bookId = bookId,
                biteId = biteId,
                questionId = questionId,
                attemptNumber = attemptNumber,
                timeToAnswerMs = timeToAnswerMs,
                usedReviewText = usedReviewText,
                xpAwarded = xpAwarded,
                healthDelta = healthDelta,
                competencyPercent = competencyPercent
            )
        )
    }
}
