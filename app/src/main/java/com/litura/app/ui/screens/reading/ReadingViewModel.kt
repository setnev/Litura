package com.litura.app.ui.screens.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.local.entity.BiteEntity
import com.litura.app.data.local.entity.BiteProgressEntity
import com.litura.app.data.local.entity.QuestionEntity
import com.litura.app.data.local.entity.ReadingProgressEntity
import com.litura.app.data.repository.BookRepository
import com.litura.app.data.repository.ReadingRepository
import com.litura.app.domain.model.ChoiceData
import com.litura.app.engine.BadgeEvaluator
import com.litura.app.engine.GameEngine
import com.litura.app.engine.HealthManager
import com.litura.app.engine.QuestionResult
import com.litura.app.engine.TelemetryService
import com.litura.app.util.ReadingFacts
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class ReadingPhase {
    LOADING,
    READING_FACT_SPLASH,
    BITE_TEXT,
    QUESTION,
    RECAP,
    EXIT_FACT_SPLASH,
    NO_HEALTH
}

data class BiteRecap(
    val timeSpentMs: Long,
    val xpEarned: Int,
    val competencyPercent: Double,
    val newBadges: List<String>
)

data class ReadingUiState(
    val phase: ReadingPhase = ReadingPhase.LOADING,
    val bookId: String = "",
    val bookTitle: String = "",
    val chapterTitle: String = "",
    val currentBite: BiteEntity? = null,
    val currentQuestions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val currentAttempt: Int = 1,
    val selectedChoiceId: String? = null,
    val eliminatedChoices: Set<String> = emptySet(),
    val correctChoiceRevealed: Boolean = false,
    val health: Int = 10,
    val biteRecap: BiteRecap? = null,
    val readingFact: String = "",
    val progressPercent: Float = 0f,
    val reviewTextRemaining: Int = 5,
    val reviewTextVisible: Boolean = false,
    val questionResults: List<QuestionResult> = emptyList(),
    val biteStartTimeMs: Long = 0L,
    val questionStartTimeMs: Long = 0L,
    val error: String? = null
)

@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingRepository: ReadingRepository,
    private val gameEngine: GameEngine,
    private val healthManager: HealthManager,
    private val telemetryService: TelemetryService,
    private val badgeEvaluator: BadgeEvaluator,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val json: Json
) : ViewModel() {

    private val _state = MutableStateFlow(ReadingUiState())
    val state: StateFlow<ReadingUiState> = _state.asStateFlow()

    fun startReading(bookId: String, initialBiteId: String?) {
        viewModelScope.launch {
            gameEngine.initialize()
            val health = healthManager.getCurrentHealth()

            if (health <= 0) {
                _state.update { it.copy(phase = ReadingPhase.NO_HEALTH, health = health) }
                return@launch
            }

            val book = bookRepository.getBookById(bookId) ?: return@launch
            val prefs = userPreferencesDataStore.preferences.first()
            val progress = readingRepository.getProgress(prefs.userId, bookId)

            val bite = (if (initialBiteId != null) {
                readingRepository.getBite(initialBiteId)
            } else if (progress != null && progress.currentBiteId != null) {
                readingRepository.getBite(progress.currentBiteId)
            } else {
                readingRepository.getBiteByIndex(bookId, 1)
            }) ?: return@launch

            val reviewRemaining = gameEngine.getReviewTextRemaining()

            telemetryService.logSessionStart()
            telemetryService.logBiteStarted(bookId, bite.biteId)
            healthManager.resetBiteLossCounter()

            val progressPercent = if (progress != null) {
                progress.completedBites.toFloat() / progress.totalBites
            } else {
                0f
            }

            _state.update {
                it.copy(
                    phase = ReadingPhase.READING_FACT_SPLASH,
                    bookId = bookId,
                    bookTitle = book.title,
                    chapterTitle = bite.chapterId,
                    currentBite = bite,
                    health = health,
                    readingFact = ReadingFacts.random(),
                    progressPercent = progressPercent,
                    reviewTextRemaining = reviewRemaining,
                    biteStartTimeMs = System.currentTimeMillis()
                )
            }
        }
    }

    fun dismissSplash() {
        _state.update { it.copy(phase = ReadingPhase.BITE_TEXT) }
    }

    fun finishReadingBite() {
        viewModelScope.launch {
            val bite = _state.value.currentBite ?: return@launch
            val config = gameEngine.getConfig()
            val questions = readingRepository.getRandomQuestions(
                bite.biteId,
                config.questionRules.questionsPerBite
            )

            _state.update {
                it.copy(
                    phase = ReadingPhase.QUESTION,
                    currentQuestions = questions,
                    currentQuestionIndex = 0,
                    currentAttempt = 1,
                    selectedChoiceId = null,
                    eliminatedChoices = emptySet(),
                    correctChoiceRevealed = false,
                    questionResults = emptyList(),
                    questionStartTimeMs = System.currentTimeMillis()
                )
            }
        }
    }

    fun selectChoice(choiceId: String) {
        _state.update { it.copy(selectedChoiceId = choiceId) }
    }

    fun submitAnswer() {
        viewModelScope.launch {
            val s = _state.value
            val question = s.currentQuestions.getOrNull(s.currentQuestionIndex) ?: return@launch
            val selectedId = s.selectedChoiceId ?: return@launch
            val isCorrect = selectedId == question.correctChoiceId
            val timeToAnswer = System.currentTimeMillis() - s.questionStartTimeMs

            if (isCorrect || s.currentAttempt >= 2) {
                val xp = if (isCorrect) gameEngine.computeXp(s.currentAttempt) else 0
                val result = QuestionResult(
                    questionId = question.questionId,
                    biteId = question.biteId,
                    isCorrect = isCorrect,
                    attemptNumber = s.currentAttempt,
                    timeToAnswerMs = timeToAnswer,
                    xpAwarded = xp
                )

                telemetryService.logQuestionAttempt(
                    s.bookId, question.biteId, question.questionId,
                    s.currentAttempt, timeToAnswer, xp
                )

                if (!isCorrect) {
                    val newHealth = healthManager.applyPenalty()
                    telemetryService.logHealthChanged(-1)
                    _state.update { it.copy(health = newHealth) }
                }

                val newResults = s.questionResults + result
                val newEliminated = if (!isCorrect) s.eliminatedChoices + selectedId else s.eliminatedChoices

                _state.update {
                    it.copy(
                        questionResults = newResults,
                        eliminatedChoices = newEliminated,
                        correctChoiceRevealed = true,
                        selectedChoiceId = selectedId
                    )
                }

                if (xp > 0) {
                    userPreferencesDataStore.addXp(xp)
                }
            } else {
                // First wrong attempt
                val newHealth = healthManager.applyPenalty()
                telemetryService.logHealthChanged(-1)
                _state.update {
                    it.copy(
                        currentAttempt = 2,
                        eliminatedChoices = s.eliminatedChoices + selectedId,
                        selectedChoiceId = null,
                        health = newHealth,
                        questionStartTimeMs = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun nextQuestion() {
        val s = _state.value
        val nextIndex = s.currentQuestionIndex + 1
        if (nextIndex < s.currentQuestions.size) {
            _state.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    currentAttempt = 1,
                    selectedChoiceId = null,
                    eliminatedChoices = emptySet(),
                    correctChoiceRevealed = false,
                    questionStartTimeMs = System.currentTimeMillis()
                )
            }
        } else {
            completeBite()
        }
    }

    private fun completeBite() {
        viewModelScope.launch {
            val s = _state.value
            val bite = s.currentBite ?: return@launch
            val prefs = userPreferencesDataStore.preferences.first()
            val competency = gameEngine.computeBiteCompetency(s.questionResults)
            val xpEarned = s.questionResults.sumOf { it.xpAwarded }
            val timeSpent = System.currentTimeMillis() - s.biteStartTimeMs

            readingRepository.saveBiteProgress(
                BiteProgressEntity(
                    userId = prefs.userId,
                    biteId = bite.biteId,
                    bookId = s.bookId,
                    isCompleted = true,
                    xpEarned = xpEarned,
                    competencyPercent = competency,
                    timeSpentMs = timeSpent,
                    completedAt = System.currentTimeMillis()
                )
            )

            userPreferencesDataStore.incrementBitesCompleted()

            val completedCount = readingRepository.getCompletedBiteCount(prefs.userId, s.bookId)
            val book = bookRepository.getBookById(s.bookId)
            val totalBites = book?.totalBites ?: 1
            val avgCompetency = readingRepository.getAverageCompetency(prefs.userId, s.bookId)

            val nextBite = readingRepository.getNextBite(s.bookId, bite.orderIndex)
            val status = if (completedCount >= totalBites) "COMPLETED" else "IN_PROGRESS"

            readingRepository.updateProgress(
                ReadingProgressEntity(
                    userId = prefs.userId,
                    bookId = s.bookId,
                    status = status,
                    currentBiteId = nextBite?.biteId,
                    completedBites = completedCount,
                    totalBites = totalBites,
                    percentComplete = (completedCount.toDouble() / totalBites) * 100.0,
                    averageCompetency = avgCompetency,
                    averageAttempts = s.questionResults.map { it.attemptNumber.toDouble() }.average(),
                    avgTimePerQuestionMs = s.questionResults.map { it.timeToAnswerMs }.average().toLong(),
                    totalXpEarned = xpEarned,
                    startedAt = System.currentTimeMillis(),
                    lastReadAt = System.currentTimeMillis(),
                    completedAt = if (status == "COMPLETED") System.currentTimeMillis() else null
                )
            )

            if (status == "COMPLETED") {
                userPreferencesDataStore.incrementBooksCompleted()
            }

            telemetryService.logBiteCompleted(s.bookId, bite.biteId, competency)

            val badges = badgeEvaluator.evaluateAfterBiteCompletion(prefs.userId, s.bookId)

            _state.update {
                it.copy(
                    phase = ReadingPhase.RECAP,
                    biteRecap = BiteRecap(
                        timeSpentMs = timeSpent,
                        xpEarned = xpEarned,
                        competencyPercent = competency,
                        newBadges = badges.map { b -> b.badgeId }
                    ),
                    progressPercent = completedCount.toFloat() / totalBites
                )
            }
        }
    }

    fun nextBite() {
        viewModelScope.launch {
            val s = _state.value
            val bite = s.currentBite ?: return@launch
            val nextBite = readingRepository.getNextBite(s.bookId, bite.orderIndex)

            if (nextBite == null) {
                _state.update {
                    it.copy(
                        phase = ReadingPhase.EXIT_FACT_SPLASH,
                        readingFact = ReadingFacts.random()
                    )
                }
                return@launch
            }

            val health = healthManager.getCurrentHealth()
            if (health <= 0) {
                _state.update { it.copy(phase = ReadingPhase.NO_HEALTH, health = 0) }
                return@launch
            }

            healthManager.resetBiteLossCounter()
            telemetryService.logBiteStarted(s.bookId, nextBite.biteId)

            _state.update {
                it.copy(
                    phase = ReadingPhase.BITE_TEXT,
                    currentBite = nextBite,
                    chapterTitle = nextBite.chapterId,
                    currentQuestions = emptyList(),
                    currentQuestionIndex = 0,
                    currentAttempt = 1,
                    selectedChoiceId = null,
                    eliminatedChoices = emptySet(),
                    correctChoiceRevealed = false,
                    questionResults = emptyList(),
                    biteRecap = null,
                    health = health,
                    biteStartTimeMs = System.currentTimeMillis(),
                    reviewTextVisible = false
                )
            }
        }
    }

    fun toggleReviewText() {
        viewModelScope.launch {
            val s = _state.value
            if (!s.reviewTextVisible && s.reviewTextRemaining > 0) {
                gameEngine.useReviewText()
                telemetryService.logReviewTextOpened(s.bookId, s.currentBite?.biteId ?: "")
                _state.update {
                    it.copy(
                        reviewTextVisible = true,
                        reviewTextRemaining = it.reviewTextRemaining - 1
                    )
                }
            } else {
                _state.update { it.copy(reviewTextVisible = !it.reviewTextVisible) }
            }
        }
    }

    fun getChoicesForCurrentQuestion(): List<ChoiceData> {
        val question = _state.value.currentQuestions.getOrNull(_state.value.currentQuestionIndex)
            ?: return emptyList()
        return json.decodeFromString<List<ChoiceData>>(question.choicesJson)
    }
}
