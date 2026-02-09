package com.litura.app.ui.screens.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.repository.BookRepository
import com.litura.app.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class SkillPath(
    val skillName: String,
    val books: List<SkillBook>,
    val overallProgress: Float
)

data class SkillBook(
    val bookId: String,
    val title: String,
    val isPrimary: Boolean,
    val progressPercent: Float
)

data class SkillsUiState(
    val skillPaths: List<SkillPath> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingRepository: ReadingRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val json: Json
) : ViewModel() {

    private val _state = MutableStateFlow(SkillsUiState())
    val state: StateFlow<SkillsUiState> = _state.asStateFlow()

    init {
        loadSkills()
    }

    private fun loadSkills() {
        viewModelScope.launch {
            val prefs = userPreferencesDataStore.preferences.first()
            val books = bookRepository.getAllBooks().first()
            val skillMap = mutableMapOf<String, MutableList<SkillBook>>()

            for (book in books) {
                val primary = json.decodeFromString<List<String>>(book.primarySkillsJson)
                val secondary = json.decodeFromString<List<String>>(book.secondarySkillsJson)
                val progress = readingRepository.getProgress(prefs.userId, book.bookId)
                val progressPercent = if (progress != null) {
                    progress.completedBites.toFloat() / progress.totalBites
                } else 0f

                for (skill in primary) {
                    skillMap.getOrPut(skill) { mutableListOf() }.add(
                        SkillBook(book.bookId, book.title, true, progressPercent)
                    )
                }
                for (skill in secondary) {
                    skillMap.getOrPut(skill) { mutableListOf() }.add(
                        SkillBook(book.bookId, book.title, false, progressPercent)
                    )
                }
            }

            val paths = skillMap.map { (skill, books) ->
                val overall = if (books.isNotEmpty()) books.map { it.progressPercent }.average().toFloat() else 0f
                SkillPath(skill, books, overall)
            }.sortedByDescending { it.overallProgress }

            _state.update { it.copy(skillPaths = paths, isLoading = false) }
        }
    }
}
