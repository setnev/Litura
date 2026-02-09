package com.litura.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.repository.BookImporter
import com.litura.app.data.repository.BookRepository
import com.litura.app.data.repository.ReadingRepository
import com.litura.app.engine.CompetitiveEngine
import com.litura.app.engine.CompetitiveStandings
import com.litura.app.engine.GameEngine
import com.litura.app.engine.HealthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CurrentBookCard(
    val bookId: String,
    val title: String,
    val author: String,
    val competencyPercent: Double,
    val xpEarned: Int,
    val completedBites: Int,
    val totalBites: Int,
    val progressPercent: Float
)

data class HomeUiState(
    val greeting: String = "Hello, Reader!",
    val currentlyReading: List<CurrentBookCard> = emptyList(),
    val health: Int = 10,
    val maxHealth: Int = 10,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val competitive: CompetitiveStandings? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingRepository: ReadingRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val bookImporter: BookImporter,
    private val gameEngine: GameEngine,
    private val healthManager: HealthManager,
    private val competitiveEngine: CompetitiveEngine
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            initializeApp()
            loadDashboard()
        }
    }

    private suspend fun initializeApp() {
        val prefs = userPreferencesDataStore.preferences.first()
        if (prefs.isFirstLaunch) {
            gameEngine.initialize()
            bookImporter.importAllBooks()
            competitiveEngine.seedFriendsIfNeeded()
            userPreferencesDataStore.setFirstLaunchComplete()
        } else {
            gameEngine.initialize()
        }
    }

    private suspend fun loadDashboard() {
        val prefs = userPreferencesDataStore.preferences.first()
        val health = healthManager.getCurrentHealth()

        _state.update {
            it.copy(
                greeting = "Hello, ${prefs.displayName}!",
                health = health,
                totalXp = prefs.totalXp,
                currentStreak = prefs.currentStreakDays
            )
        }

        // Load in-progress books
        viewModelScope.launch {
            readingRepository.getInProgressBooks(prefs.userId).collect { progressList ->
                val cards = progressList.mapNotNull { progress ->
                    val book = bookRepository.getBookById(progress.bookId) ?: return@mapNotNull null
                    CurrentBookCard(
                        bookId = book.bookId,
                        title = book.title,
                        author = book.author,
                        competencyPercent = progress.averageCompetency,
                        xpEarned = progress.totalXpEarned,
                        completedBites = progress.completedBites,
                        totalBites = progress.totalBites,
                        progressPercent = progress.completedBites.toFloat() / progress.totalBites
                    )
                }
                _state.update { it.copy(currentlyReading = cards, isLoading = false) }
            }
        }

        // Load competitive standings
        viewModelScope.launch {
            val standings = competitiveEngine.getComparisons()
            _state.update { it.copy(competitive = standings) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            gameEngine.initialize()
            val health = healthManager.getCurrentHealth()
            val prefs = userPreferencesDataStore.preferences.first()
            _state.update {
                it.copy(
                    health = health,
                    totalXp = prefs.totalXp,
                    currentStreak = prefs.currentStreakDays
                )
            }
        }
    }
}
