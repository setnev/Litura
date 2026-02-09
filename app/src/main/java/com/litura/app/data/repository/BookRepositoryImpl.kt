package com.litura.app.data.repository

import com.litura.app.data.local.dao.BookDao
import com.litura.app.data.local.entity.BookEntity
import com.litura.app.domain.model.PurchaseState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao
) : BookRepository {
    override fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()
    override fun getDownloadedBooks(): Flow<List<BookEntity>> = bookDao.getDownloadedBooks()
    override fun searchBooks(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)
    override suspend fun getBookById(bookId: String): BookEntity? = bookDao.getBookById(bookId)
    override suspend fun updatePurchaseState(bookId: String, state: PurchaseState) =
        bookDao.updatePurchaseState(bookId, state.name)
    override suspend fun toggleFavorite(bookId: String, isFavorite: Boolean) =
        bookDao.updateFavorite(bookId, isFavorite)
    override suspend fun getBookCount(): Int = bookDao.getBookCount()
}
