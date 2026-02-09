package com.litura.app.di

import android.content.Context
import androidx.room.Room
import com.litura.app.data.local.dao.BadgeDao
import com.litura.app.data.local.dao.BiteDao
import com.litura.app.data.local.dao.BiteProgressDao
import com.litura.app.data.local.dao.BookDao
import com.litura.app.data.local.dao.MockFriendDao
import com.litura.app.data.local.dao.QuestionDao
import com.litura.app.data.local.dao.ReadingProgressDao
import com.litura.app.data.local.dao.TelemetryDao
import com.litura.app.data.local.database.LituraDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLituraDatabase(@ApplicationContext context: Context): LituraDatabase =
        Room.databaseBuilder(context, LituraDatabase::class.java, "litura.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBookDao(db: LituraDatabase): BookDao = db.bookDao()
    @Provides fun provideBiteDao(db: LituraDatabase): BiteDao = db.biteDao()
    @Provides fun provideQuestionDao(db: LituraDatabase): QuestionDao = db.questionDao()
    @Provides fun provideReadingProgressDao(db: LituraDatabase): ReadingProgressDao = db.readingProgressDao()
    @Provides fun provideBiteProgressDao(db: LituraDatabase): BiteProgressDao = db.biteProgressDao()
    @Provides fun provideTelemetryDao(db: LituraDatabase): TelemetryDao = db.telemetryDao()
    @Provides fun provideBadgeDao(db: LituraDatabase): BadgeDao = db.badgeDao()
    @Provides fun provideMockFriendDao(db: LituraDatabase): MockFriendDao = db.mockFriendDao()
}
