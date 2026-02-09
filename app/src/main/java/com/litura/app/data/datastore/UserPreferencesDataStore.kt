package com.litura.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_ID = stringPreferencesKey("avatar_id")
        val SUBSCRIPTION_TIER = stringPreferencesKey("subscription_tier")
        val CURRENT_HEALTH = intPreferencesKey("current_health")
        val LAST_HEALTH_RECHARGE = longPreferencesKey("last_health_recharge")
        val REVIEW_TEXT_USED_TODAY = intPreferencesKey("review_text_used_today")
        val CURRENT_STREAK_DAYS = intPreferencesKey("current_streak_days")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val LONGEST_STREAK_DAYS = intPreferencesKey("longest_streak_days")
        val DEV_MODE_ENABLED = booleanPreferencesKey("dev_mode_enabled")
        val TOTAL_XP = intPreferencesKey("total_xp")
        val TOTAL_BITES_COMPLETED = intPreferencesKey("total_bites_completed")
        val BOOKS_COMPLETED = intPreferencesKey("books_completed")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            userId = prefs[Keys.USER_ID] ?: "u_local_001",
            displayName = prefs[Keys.DISPLAY_NAME] ?: "Reader",
            avatarId = prefs[Keys.AVATAR_ID] ?: "avatar_01",
            subscriptionTier = prefs[Keys.SUBSCRIPTION_TIER] ?: "FREE",
            currentHealth = prefs[Keys.CURRENT_HEALTH] ?: 10,
            lastHealthRechargeTimestamp = prefs[Keys.LAST_HEALTH_RECHARGE] ?: System.currentTimeMillis(),
            reviewTextUsedToday = prefs[Keys.REVIEW_TEXT_USED_TODAY] ?: 0,
            currentStreakDays = prefs[Keys.CURRENT_STREAK_DAYS] ?: 0,
            lastActiveDate = prefs[Keys.LAST_ACTIVE_DATE] ?: "",
            longestStreakDays = prefs[Keys.LONGEST_STREAK_DAYS] ?: 0,
            devModeEnabled = prefs[Keys.DEV_MODE_ENABLED] ?: false,
            totalXp = prefs[Keys.TOTAL_XP] ?: 0,
            totalBitesCompleted = prefs[Keys.TOTAL_BITES_COMPLETED] ?: 0,
            booksCompleted = prefs[Keys.BOOKS_COMPLETED] ?: 0,
            isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true
        )
    }

    suspend fun updateHealth(health: Int) {
        context.dataStore.edit { it[Keys.CURRENT_HEALTH] = health }
    }

    suspend fun updateHealthWithRecharge(health: Int, timestamp: Long) {
        context.dataStore.edit {
            it[Keys.CURRENT_HEALTH] = health
            it[Keys.LAST_HEALTH_RECHARGE] = timestamp
        }
    }

    suspend fun updateReviewTextUsed(count: Int) {
        context.dataStore.edit { it[Keys.REVIEW_TEXT_USED_TODAY] = count }
    }

    suspend fun addXp(xp: Int) {
        context.dataStore.edit {
            it[Keys.TOTAL_XP] = (it[Keys.TOTAL_XP] ?: 0) + xp
        }
    }

    suspend fun incrementBitesCompleted() {
        context.dataStore.edit {
            it[Keys.TOTAL_BITES_COMPLETED] = (it[Keys.TOTAL_BITES_COMPLETED] ?: 0) + 1
        }
    }

    suspend fun incrementBooksCompleted() {
        context.dataStore.edit {
            it[Keys.BOOKS_COMPLETED] = (it[Keys.BOOKS_COMPLETED] ?: 0) + 1
        }
    }

    suspend fun updateStreak(current: Int, longest: Int, lastActiveDate: String) {
        context.dataStore.edit {
            it[Keys.CURRENT_STREAK_DAYS] = current
            it[Keys.LONGEST_STREAK_DAYS] = longest
            it[Keys.LAST_ACTIVE_DATE] = lastActiveDate
        }
    }

    suspend fun setDevMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DEV_MODE_ENABLED] = enabled }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { it[Keys.IS_FIRST_LAUNCH] = false }
    }

    suspend fun resetReviewTextDaily() {
        context.dataStore.edit { it[Keys.REVIEW_TEXT_USED_TODAY] = 0 }
    }
}
