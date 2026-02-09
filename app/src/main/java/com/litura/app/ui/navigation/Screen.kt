package com.litura.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Library : Screen
    @Serializable data object Badges : Screen
    @Serializable data object Skills : Screen
    @Serializable data object Profile : Screen
    @Serializable data class Reading(val bookId: String, val biteId: String? = null) : Screen
}
