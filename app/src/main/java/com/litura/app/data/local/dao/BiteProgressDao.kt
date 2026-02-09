package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.BiteProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BiteProgressDao {
    @Query("SELECT * FROM bite_progress WHERE userId = :userId AND bookId = :bookId")
    fun getBiteProgressForBook(userId: String, bookId: String): Flow<List<BiteProgressEntity>>

    @Query("SELECT * FROM bite_progress WHERE userId = :userId AND biteId = :biteId")
    suspend fun getBiteProgress(userId: String, biteId: String): BiteProgressEntity?

    @Query("SELECT COUNT(*) FROM bite_progress WHERE userId = :userId AND bookId = :bookId AND isCompleted = 1")
    suspend fun getCompletedBiteCount(userId: String, bookId: String): Int

    @Query("SELECT AVG(competencyPercent) FROM bite_progress WHERE userId = :userId AND bookId = :bookId AND isCompleted = 1")
    suspend fun getAverageCompetency(userId: String, bookId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBiteProgress(progress: BiteProgressEntity)
}
