package com.litura.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BitesFile(
    val bookId: String,
    val bites: List<BiteData>
)

@Serializable
data class BiteData(
    val biteId: String,
    val chapterId: String,
    val orderIndex: Int,
    val text: String,
    val estimatedSeconds: Int
)
