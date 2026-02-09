package com.litura.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.local.dao.TelemetryDao
import com.litura.app.data.local.entity.TelemetryEventEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val displayName: String = "Reader",
    val avatarId: String = "avatar_01",
    val subscriptionTier: String = "FREE",
    val totalXp: Int = 0,
    val totalBitesCompleted: Int = 0,
    val booksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val devModeEnabled: Boolean = false,
    val showTelemetryViewer: Boolean = false,
    val telemetryEvents: List<TelemetryEventEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val telemetryDao: TelemetryDao
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userPreferencesDataStore.preferences.collect { prefs ->
                _state.update {
                    it.copy(
                        displayName = prefs.displayName,
                        avatarId = prefs.avatarId,
                        subscriptionTier = prefs.subscriptionTier,
                        totalXp = prefs.totalXp,
                        totalBitesCompleted = prefs.totalBitesCompleted,
                        booksCompleted = prefs.booksCompleted,
                        currentStreak = prefs.currentStreakDays,
                        longestStreak = prefs.longestStreakDays,
                        devModeEnabled = prefs.devModeEnabled,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleDevMode() {
        viewModelScope.launch {
            val current = _state.value.devModeEnabled
            userPreferencesDataStore.setDevMode(!current)
            _state.update { it.copy(devModeEnabled = !current) }
        }
    }

    fun toggleTelemetryViewer() {
        val show = !_state.value.showTelemetryViewer
        _state.update { it.copy(showTelemetryViewer = show) }
        if (show) {
            viewModelScope.launch {
                telemetryDao.getRecentEvents(50).collect { events ->
                    _state.update { it.copy(telemetryEvents = events) }
                }
            }
        }
    }
}
