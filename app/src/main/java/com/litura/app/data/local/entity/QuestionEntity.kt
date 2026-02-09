package com.litura.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = BiteEntity::class,
            parentColumns = ["biteId"],
            childColumns = ["biteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("biteId")]
)
data class QuestionEntity(
    @PrimaryKey val questionId: String,
    val biteId: String,
    val bookId: String,
    val type: String,
    val difficulty: String,
    val prompt: String,
    val choicesJson: String,
    val correctChoiceId: String,
    val explanation: String
)
