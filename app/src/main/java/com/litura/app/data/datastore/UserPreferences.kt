package com.litura.app.data.datastore

data class UserPreferences(
    val userId: String = "u_local_001",
    val displayName: String = "Reader",
    val avatarId: String = "avatar_01",
    val subscriptionTier: String = "FREE",
    val currentHealth: Int = 10,
    val lastHealthRechargeTimestamp: Long = System.currentTimeMillis(),
    val reviewTextUsedToday: Int = 0,
    val currentStreakDays: Int = 0,
    val lastActiveDate: String = "",
    val longestStreakDays: Int = 0,
    val devModeEnabled: Boolean = false,
    val totalXp: Int = 0,
    val totalBitesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val isFirstLaunch: Boolean = true
)
