package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE userId = :userId")
    fun getAllProgressForUser(userId: String): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE userId = :userId AND bookId = :bookId")
    suspend fun getProgress(userId: String, bookId: String): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE userId = :userId AND status = 'IN_PROGRESS'")
    fun getInProgressBooks(userId: String): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ReadingProgressEntity)
}
