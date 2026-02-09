package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.BiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BiteDao {
    @Query("SELECT * FROM bites WHERE bookId = :bookId ORDER BY orderIndex ASC")
    fun getBitesForBook(bookId: String): Flow<List<BiteEntity>>

    @Query("SELECT * FROM bites WHERE biteId = :biteId")
    suspend fun getBiteById(biteId: String): BiteEntity?

    @Query("SELECT * FROM bites WHERE bookId = :bookId AND orderIndex = :index")
    suspend fun getBiteByIndex(bookId: String, index: Int): BiteEntity?

    @Query("SELECT * FROM bites WHERE bookId = :bookId AND orderIndex > :currentIndex ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getNextBite(bookId: String, currentIndex: Int): BiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBites(bites: List<BiteEntity>)
}
