package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.litura.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getBookById(bookId: String): BookEntity?

    @Query("SELECT * FROM books WHERE purchaseState = 'OWNED_DOWNLOADED'")
    fun getDownloadedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET purchaseState = :state WHERE bookId = :bookId")
    suspend fun updatePurchaseState(bookId: String, state: String)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE bookId = :bookId")
    suspend fun updateFavorite(bookId: String, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int
}
