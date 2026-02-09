package com.litura.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionsFile(
    val bookId: String,
    val questions: List<QuestionData>
)

@Serializable
data class QuestionData(
    val questionId: String,
    val biteId: String,
    val type: String,
    val difficulty: String,
    val prompt: String,
    val choices: List<ChoiceData>,
    val correctChoiceId: String,
    val explanation: String
)

@Serializable
data class ChoiceData(
    val id: String,
    val text: String
)
