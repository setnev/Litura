package com.litura.app.data.repository

import com.litura.app.data.local.entity.BookEntity
import com.litura.app.domain.model.PurchaseState
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun getAllBooks(): Flow<List<BookEntity>>
    fun getDownloadedBooks(): Flow<List<BookEntity>>
    fun searchBooks(query: String): Flow<List<BookEntity>>
    suspend fun getBookById(bookId: String): BookEntity?
    suspend fun updatePurchaseState(bookId: String, state: PurchaseState)
    suspend fun toggleFavorite(bookId: String, isFavorite: Boolean)
    suspend fun getBookCount(): Int
}
