package com.litura.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameEngineConfig(
    val engineVersion: String,
    val xpRules: XpRules,
    val healthSystem: HealthSystemConfig,
    val questionRules: QuestionRulesConfig,
    val readingSession: ReadingSessionConfig,
    val reviewText: ReviewTextConfig,
    val competition: CompetitionConfig,
    val difficultyScaling: DifficultyScalingConfig? = null
)

@Serializable
data class XpRules(
    val correct: Int,
    val halfCredit: Int,
    val incorrect: Int
)

@Serializable
data class HealthSystemConfig(
    val enabled: Boolean,
    val maxSegments: Int,
    val segmentRechargeMinutes: Int,
    val penalties: HealthPenalties
)

@Serializable
data class HealthPenalties(
    val wrongAnswer: Int,
    val maxLossPerBite: Int
)

@Serializable
data class QuestionRulesConfig(
    val questionsPerBite: Int,
    val maxAttemptsPerQuestion: Int,
    val visualFeedback: VisualFeedback
)

@Serializable
data class VisualFeedback(
    val firstWrong: String,
    val secondWrong: String
)

@Serializable
data class ReadingSessionConfig(
    val lockProgressUntilComplete: Boolean,
    val allowMidBiteExit: Boolean,
    val exitWarning: Boolean
)

@Serializable
data class ReviewTextConfig(
    val enabled: Boolean,
    val dailyLimit: Int,
    val resetPolicy: String
)

@Serializable
data class CompetitionConfig(
    val enabled: Boolean,
    val comparisonMetrics: List<String>,
    val visibility: CompetitionVisibility
)

@Serializable
data class CompetitionVisibility(
    val showRankDelta: Boolean,
    val showExactRank: Boolean
)

@Serializable
data class DifficultyScalingConfig(
    val enabled: Boolean,
    val futureUse: String? = null
)
