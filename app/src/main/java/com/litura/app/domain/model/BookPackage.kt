package com.litura.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BookPackage(
    val bookId: String,
    val version: String,
    val source: BookSource,
    val identity: BookIdentity,
    val classification: BookClassification,
    val structure: BookStructure,
    val learningFocus: LearningFocus,
    val social: SocialTags,
    val badges: BookBadges,
    val economics: BookEconomics,
    val assets: BookAssets,
    val analytics: BookAnalytics
)

@Serializable
data class BookSource(
    val type: String,
    val publisher: String,
    val license: String
)

@Serializable
data class BookIdentity(
    val title: String,
    val author: String,
    val series: String? = null,
    val publicationYear: Int,
    val language: String
)

@Serializable
data class BookClassification(
    val genres: List<String>,
    val readerLevel: ReaderLevel,
    val difficultyTier: String
)

@Serializable
data class ReaderLevel(
    val lexile: Int,
    val label: String
)

@Serializable
data class BookStructure(
    val totalBites: Int,
    val chapters: List<Chapter>
)

@Serializable
data class Chapter(
    val chapterId: String,
    val title: String,
    val biteRange: List<Int>
)

@Serializable
data class LearningFocus(
    val primarySkills: List<String>,
    val secondarySkills: List<String>
)

@Serializable
data class SocialTags(
    val topicTags: List<String>,
    val interestTags: List<String>,
    val shareableAchievements: Boolean = true
)

@Serializable
data class BookBadges(
    val completionBadge: String,
    val specialtyBadges: List<String>,
    val hiddenBadges: List<HiddenBadge>
)

@Serializable
data class HiddenBadge(
    val id: String,
    val condition: String
)

@Serializable
data class BookEconomics(
    val price: Price,
    val purchaseType: String,
    val includedInPlans: List<String>
)

@Serializable
data class Price(
    val currency: String,
    val amount: Double
)

@Serializable
data class BookAssets(
    val coverImage: String,
    val badgeImages: List<String>
)

@Serializable
data class BookAnalytics(
    val estimatedReadTimeMinutes: Int,
    val avgSessionMinutes: Int? = null,
    val engagementWeight: Double
)
