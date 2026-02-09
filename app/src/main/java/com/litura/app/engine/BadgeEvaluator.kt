package com.litura.app.engine

import com.litura.app.data.local.dao.BadgeDao
import com.litura.app.data.local.dao.BiteProgressDao
import com.litura.app.data.local.dao.BookDao
import com.litura.app.data.local.dao.ReadingProgressDao
import com.litura.app.data.local.entity.BadgeEntity
import com.litura.app.domain.model.HiddenBadge
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeEvaluator @Inject constructor(
    private val badgeDao: BadgeDao,
    private val readingProgressDao: ReadingProgressDao,
    private val biteProgressDao: BiteProgressDao,
    private val bookDao: BookDao,
    private val json: Json
) {
    suspend fun evaluateAfterBiteCompletion(
        userId: String,
        bookId: String
    ): List<BadgeEntity> {
        val awarded = mutableListOf<BadgeEntity>()
        val book = bookDao.getBookById(bookId) ?: return awarded
        val progress = readingProgressDao.getProgress(userId, bookId) ?: return awarded

        // Check book completion
        if (progress.completedBites >= progress.totalBites) {
            val badge = awardBadge(userId, book.completionBadge, bookId, "COMPLETION")
            if (badge != null) awarded.add(badge)
        }

        // Check hidden badges
        val hiddenBadges = json.decodeFromString<List<HiddenBadge>>(book.hiddenBadgesJson)
        for (hidden in hiddenBadges) {
            if (!badgeDao.hasBadge(userId, hidden.id)) {
                if (evaluateCondition(hidden.condition, userId, bookId, progress.completedBites, progress.totalBites)) {
                    val badge = awardBadge(userId, hidden.id, bookId, "HIDDEN")
                    if (badge != null) awarded.add(badge)
                }
            }
        }

        return awarded
    }

    private suspend fun awardBadge(
        userId: String,
        badgeId: String,
        bookId: String,
        badgeType: String
    ): BadgeEntity? {
        if (badgeDao.hasBadge(userId, badgeId)) return null
        val badge = BadgeEntity(
            userId = userId,
            badgeId = badgeId,
            bookId = bookId,
            badgeType = badgeType,
            earnedAt = System.currentTimeMillis()
        )
        badgeDao.insertBadge(badge)
        return badge
    }

    private suspend fun evaluateCondition(
        condition: String,
        userId: String,
        bookId: String,
        completedBites: Int,
        totalBites: Int
    ): Boolean {
        val lower = condition.lowercase()
        if (lower.contains("competency") && lower.contains(">=")) {
            if (completedBites < totalBites) return false
            val threshold = Regex(">=\\s*(\\d+)").find(lower)?.groupValues?.get(1)?.toDoubleOrNull() ?: return false
            val avgCompetency = biteProgressDao.getAverageCompetency(userId, bookId) ?: 0.0
            return avgCompetency >= threshold
        }
        return false
    }
}
