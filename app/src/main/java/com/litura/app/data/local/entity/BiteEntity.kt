package com.litura.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bites",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["bookId"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId"), Index("bookId", "orderIndex")]
)
data class BiteEntity(
    @PrimaryKey val biteId: String,
    val bookId: String,
    val chapterId: String,
    val orderIndex: Int,
    val text: String,
    val estimatedSeconds: Int
)
