package com.litura.app.ui.screens.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.local.dao.BadgeDao
import com.litura.app.data.local.entity.BadgeEntity
import com.litura.app.data.repository.BookRepository
import com.litura.app.domain.model.HiddenBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class BadgeDisplayItem(
    val badgeId: String,
    val bookTitle: String,
    val badgeType: String,
    val isEarned: Boolean,
    val earnedAt: Long?,
    val isHidden: Boolean
)

data class BadgesUiState(
    val earnedBadges: List<BadgeDisplayItem> = emptyList(),
    val lockedBadges: List<BadgeDisplayItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val badgeDao: BadgeDao,
    private val bookRepository: BookRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val json: Json
) : ViewModel() {

    private val _state = MutableStateFlow(BadgesUiState())
    val state: StateFlow<BadgesUiState> = _state.asStateFlow()

    init {
        loadBadges()
    }

    private fun loadBadges() {
        viewModelScope.launch {
            val prefs = userPreferencesDataStore.preferences.first()
            badgeDao.getEarnedBadges(prefs.userId).collect { earned ->
                val earnedIds = earned.map { it.badgeId }.toSet()
                val earnedItems = earned.mapNotNull { badge ->
                    val book = bookRepository.getBookById(badge.bookId) ?: return@mapNotNull null
                    BadgeDisplayItem(
                        badgeId = badge.badgeId,
                        bookTitle = book.title,
                        badgeType = badge.badgeType,
                        isEarned = true,
                        earnedAt = badge.earnedAt,
                        isHidden = badge.badgeType == "HIDDEN"
                    )
                }

                // Build locked badges from all books
                val allBooks = bookRepository.getAllBooks().first()
                val locked = mutableListOf<BadgeDisplayItem>()
                for (book in allBooks) {
                    if (book.completionBadge !in earnedIds) {
                        locked.add(BadgeDisplayItem(book.completionBadge, book.title, "COMPLETION", false, null, false))
                    }
                    val specialties = json.decodeFromString<List<String>>(book.specialtyBadgesJson)
                    for (s in specialties) {
                        if (s !in earnedIds) {
                            locked.add(BadgeDisplayItem(s, book.title, "SPECIALTY", false, null, false))
                        }
                    }
                    val hidden = json.decodeFromString<List<HiddenBadge>>(book.hiddenBadgesJson)
                    for (h in hidden) {
                        if (h.id !in earnedIds) {
                            locked.add(BadgeDisplayItem(h.id, book.title, "HIDDEN", false, null, true))
                        }
                    }
                }

                _state.update {
                    it.copy(
                        earnedBadges = earnedItems,
                        lockedBadges = locked,
                        isLoading = false
                    )
                }
            }
        }
    }
}
