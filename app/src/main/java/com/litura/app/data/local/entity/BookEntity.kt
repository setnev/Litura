package com.litura.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val bookId: String,
    val title: String,
    val author: String,
    val series: String?,
    val publicationYear: Int,
    val language: String,
    val genresJson: String,
    val lexile: Int,
    val difficultyTier: String,
    val totalBites: Int,
    val chaptersJson: String,
    val primarySkillsJson: String,
    val secondarySkillsJson: String,
    val completionBadge: String,
    val specialtyBadgesJson: String,
    val hiddenBadgesJson: String,
    val priceAmount: Double,
    val priceCurrency: String,
    val purchaseType: String,
    val includedInPlansJson: String,
    val coverImagePath: String,
    val estimatedReadTimeMinutes: Int,
    val engagementWeight: Double,
    val purchaseState: String,
    val isFavorite: Boolean = false,
    val importedAt: Long
)
