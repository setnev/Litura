package com.litura.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.litura.app.data.local.entity.BookEntity
import com.litura.app.data.repository.BookRepository
import com.litura.app.domain.model.PurchaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BookSort { TITLE, READER_LEVEL, BITE_COUNT, PRICE }

data class LibraryBookItem(
    val bookId: String,
    val title: String,
    val author: String,
    val biteCount: Int,
    val price: Double,
    val purchaseState: PurchaseState,
    val isFavorite: Boolean,
    val genres: String,
    val lexile: Int,
    val difficultyTier: String,
    val primarySkills: String,
    val isExpanded: Boolean = false
)

data class LibraryUiState(
    val books: List<LibraryBookItem> = emptyList(),
    val searchQuery: String = "",
    val selectedSort: BookSort = BookSort.TITLE,
    val isSearchVisible: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryUiState())
    val state: StateFlow<LibraryUiState> = _state.asStateFlow()

    init {
        loadBooks()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect { books ->
                val items = books.map { it.toLibraryItem() }
                _state.update {
                    it.copy(
                        books = sortBooks(items, it.selectedSort),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                loadBooks()
            } else {
                bookRepository.searchBooks(query).collect { books ->
                    _state.update {
                        it.copy(books = sortBooks(books.map { b -> b.toLibraryItem() }, it.selectedSort))
                    }
                }
            }
        }
    }

    fun setSort(sort: BookSort) {
        _state.update { it.copy(selectedSort = sort, books = sortBooks(it.books, sort)) }
    }

    fun toggleSearch() {
        _state.update { it.copy(isSearchVisible = !it.isSearchVisible) }
    }

    fun toggleExpanded(bookId: String) {
        _state.update { state ->
            state.copy(
                books = state.books.map {
                    if (it.bookId == bookId) it.copy(isExpanded = !it.isExpanded) else it
                }
            )
        }
    }

    fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            val book = _state.value.books.find { it.bookId == bookId } ?: return@launch
            bookRepository.toggleFavorite(bookId, !book.isFavorite)
        }
    }

    fun togglePurchase(bookId: String) {
        viewModelScope.launch {
            val book = _state.value.books.find { it.bookId == bookId } ?: return@launch
            val newState = when (book.purchaseState) {
                PurchaseState.NOT_OWNED -> PurchaseState.OWNED_DOWNLOADED
                PurchaseState.OWNED_NOT_DOWNLOADED -> PurchaseState.OWNED_DOWNLOADED
                PurchaseState.OWNED_DOWNLOADED -> PurchaseState.OWNED_DOWNLOADED
            }
            bookRepository.updatePurchaseState(bookId, newState)
        }
    }

    private fun sortBooks(books: List<LibraryBookItem>, sort: BookSort): List<LibraryBookItem> {
        return when (sort) {
            BookSort.TITLE -> books.sortedBy { it.title }
            BookSort.READER_LEVEL -> books.sortedBy { it.lexile }
            BookSort.BITE_COUNT -> books.sortedBy { it.biteCount }
            BookSort.PRICE -> books.sortedBy { it.price }
        }
    }

    private fun BookEntity.toLibraryItem() = LibraryBookItem(
        bookId = bookId,
        title = title,
        author = author,
        biteCount = totalBites,
        price = priceAmount,
        purchaseState = PurchaseState.valueOf(purchaseState),
        isFavorite = isFavorite,
        genres = genresJson.replace("[", "").replace("]", "").replace("\"", ""),
        lexile = lexile,
        difficultyTier = difficultyTier,
        primarySkills = primarySkillsJson.replace("[", "").replace("]", "").replace("\"", "")
    )
}
