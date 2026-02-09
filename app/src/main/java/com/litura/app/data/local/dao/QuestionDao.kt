package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.QuestionEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE biteId = :biteId")
    suspend fun getQuestionsForBite(biteId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE biteId = :biteId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsForBite(biteId: String, limit: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
}
