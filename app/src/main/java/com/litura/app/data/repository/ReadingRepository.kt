package com.litura.app.data.repository

import com.litura.app.data.local.entity.BiteEntity
import com.litura.app.data.local.entity.BiteProgressEntity
import com.litura.app.data.local.entity.QuestionEntity
import com.litura.app.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
    suspend fun getBite(biteId: String): BiteEntity?
    suspend fun getBiteByIndex(bookId: String, index: Int): BiteEntity?
    suspend fun getNextBite(bookId: String, currentIndex: Int): BiteEntity?
    fun getBitesForBook(bookId: String): Flow<List<BiteEntity>>
    suspend fun getRandomQuestions(biteId: String, count: Int): List<QuestionEntity>
    suspend fun getProgress(userId: String, bookId: String): ReadingProgressEntity?
    fun getInProgressBooks(userId: String): Flow<List<ReadingProgressEntity>>
    suspend fun updateProgress(progress: ReadingProgressEntity)
    suspend fun saveBiteProgress(progress: BiteProgressEntity)
    suspend fun getCompletedBiteCount(userId: String, bookId: String): Int
    suspend fun getAverageCompetency(userId: String, bookId: String): Double
}
