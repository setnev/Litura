package com.litura.app.engine

data class QuestionResult(
    val questionId: String,
    val biteId: String,
    val isCorrect: Boolean,
    val attemptNumber: Int,
    val timeToAnswerMs: Long,
    val xpAwarded: Int
)
