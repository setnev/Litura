package com.litura.app.data.repository

import com.litura.app.data.local.dao.BiteDao
import com.litura.app.data.local.dao.BiteProgressDao
import com.litura.app.data.local.dao.QuestionDao
import com.litura.app.data.local.dao.ReadingProgressDao
import com.litura.app.data.local.entity.BiteEntity
import com.litura.app.data.local.entity.BiteProgressEntity
import com.litura.app.data.local.entity.QuestionEntity
import com.litura.app.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingRepositoryImpl @Inject constructor(
    private val biteDao: BiteDao,
    private val questionDao: QuestionDao,
    private val readingProgressDao: ReadingProgressDao,
    private val biteProgressDao: BiteProgressDao
) : ReadingRepository {
    override suspend fun getBite(biteId: String): BiteEntity? = biteDao.getBiteById(biteId)
    override suspend fun getBiteByIndex(bookId: String, index: Int): BiteEntity? =
        biteDao.getBiteByIndex(bookId, index)
    override suspend fun getNextBite(bookId: String, currentIndex: Int): BiteEntity? =
        biteDao.getNextBite(bookId, currentIndex)
    override fun getBitesForBook(bookId: String): Flow<List<BiteEntity>> =
        biteDao.getBitesForBook(bookId)
    override suspend fun getRandomQuestions(biteId: String, count: Int): List<QuestionEntity> =
        questionDao.getRandomQuestionsForBite(biteId, count)
    override suspend fun getProgress(userId: String, bookId: String): ReadingProgressEntity? =
        readingProgressDao.getProgress(userId, bookId)
    override fun getInProgressBooks(userId: String): Flow<List<ReadingProgressEntity>> =
        readingProgressDao.getInProgressBooks(userId)
    override suspend fun updateProgress(progress: ReadingProgressEntity) =
        readingProgressDao.upsertProgress(progress)
    override suspend fun saveBiteProgress(progress: BiteProgressEntity) =
        biteProgressDao.upsertBiteProgress(progress)
    override suspend fun getCompletedBiteCount(userId: String, bookId: String): Int =
        biteProgressDao.getCompletedBiteCount(userId, bookId)
    override suspend fun getAverageCompetency(userId: String, bookId: String): Double =
        biteProgressDao.getAverageCompetency(userId, bookId) ?: 0.0
}
