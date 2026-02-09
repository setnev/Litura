package com.litura.app.engine

import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.domain.model.GameEngineConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class HealthManager @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val gameEngine: GameEngine
) {
    private var healthLostThisBite: Int = 0

    suspend fun getCurrentHealth(): Int {
        val config = gameEngine.getConfig()
        val prefs = userPreferencesDataStore.preferences.first()
        val storedHealth = prefs.currentHealth
        val lastRecharge = prefs.lastHealthRechargeTimestamp
        val now = System.currentTimeMillis()

        val rechargeIntervalMs = config.healthSystem.segmentRechargeMinutes * 60 * 1000L
        val elapsed = now - lastRecharge
        val recharged = if (rechargeIntervalMs > 0) (elapsed / rechargeIntervalMs).toInt() else 0
        val currentHealth = min(storedHealth + recharged, config.healthSystem.maxSegments)

        if (recharged > 0) {
            userPreferencesDataStore.updateHealthWithRecharge(currentHealth, now)
        }

        return currentHealth
    }

    suspend fun applyPenalty(): Int {
        val config = gameEngine.getConfig()
        val maxLoss = config.healthSystem.penalties.maxLossPerBite

        if (healthLostThisBite >= maxLoss) {
            return getCurrentHealth()
        }

        val currentHealth = getCurrentHealth()
        val penalty = config.healthSystem.penalties.wrongAnswer
        val newHealth = maxOf(currentHealth + penalty, 0)
        healthLostThisBite++

        userPreferencesDataStore.updateHealth(newHealth)
        return newHealth
    }

    fun resetBiteLossCounter() {
        healthLostThisBite = 0
    }

    fun observeHealth(): Flow<Int> = flow {
        while (true) {
            emit(getCurrentHealth())
            delay(10_000)
        }
    }
}
