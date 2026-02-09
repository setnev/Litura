package com.litura.app.di

import com.litura.app.data.repository.BookRepository
import com.litura.app.data.repository.BookRepositoryImpl
import com.litura.app.data.repository.ReadingRepository
import com.litura.app.data.repository.ReadingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository
    @Binds abstract fun bindReadingRepository(impl: ReadingRepositoryImpl): ReadingRepository
}
