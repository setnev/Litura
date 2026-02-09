package com.litura.app.engine

import android.content.Context
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.domain.model.GameEngineConfig
import com.litura.app.util.AssetReader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private val _config = MutableStateFlow<GameEngineConfig?>(null)
    val config: StateFlow<GameEngineConfig?> = _config.asStateFlow()

    suspend fun initialize() {
        if (_config.value != null) return
        val configJson = AssetReader.readJsonFromAssets(context, "game_engine.json")
        _config.value = json.decodeFromString<GameEngineConfig>(configJson)
    }

    fun getConfig(): GameEngineConfig = _config.value ?: error("GameEngine not initialized")

    fun computeXp(attemptNumber: Int): Int {
        val rules = getConfig().xpRules
        return when (attemptNumber) {
            1 -> rules.correct
            2 -> rules.halfCredit
            else -> rules.incorrect
        }
    }

    fun computeBiteCompetency(results: List<QuestionResult>): Double {
        if (results.isEmpty()) return 0.0
        val correct = results.count { it.isCorrect }
        return (correct.toDouble() / results.size) * 100.0
    }

    suspend fun canRead(): Boolean {
        val prefs = userPreferencesDataStore.preferences.first()
        return prefs.currentHealth > 0
    }

    suspend fun getReviewTextRemaining(): Int {
        val config = getConfig()
        val prefs = userPreferencesDataStore.preferences.first()
        return config.reviewText.dailyLimit - prefs.reviewTextUsedToday
    }

    suspend fun useReviewText() {
        val prefs = userPreferencesDataStore.preferences.first()
        userPreferencesDataStore.updateReviewTextUsed(prefs.reviewTextUsedToday + 1)
    }
}
