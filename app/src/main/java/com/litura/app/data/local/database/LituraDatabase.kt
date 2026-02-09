package com.litura.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.litura.app.data.local.dao.BadgeDao
import com.litura.app.data.local.dao.BiteDao
import com.litura.app.data.local.dao.BiteProgressDao
import com.litura.app.data.local.dao.BookDao
import com.litura.app.data.local.dao.MockFriendDao
import com.litura.app.data.local.dao.QuestionDao
import com.litura.app.data.local.dao.ReadingProgressDao
import com.litura.app.data.local.dao.TelemetryDao
import com.litura.app.data.local.entity.BadgeEntity
import com.litura.app.data.local.entity.BiteEntity
import com.litura.app.data.local.entity.BiteProgressEntity
import com.litura.app.data.local.entity.BookEntity
import com.litura.app.data.local.entity.MockFriendEntity
import com.litura.app.data.local.entity.QuestionEntity
import com.litura.app.data.local.entity.ReadingProgressEntity
import com.litura.app.data.local.entity.TelemetryEventEntity

@Database(
    entities = [
        BookEntity::class,
        BiteEntity::class,
        QuestionEntity::class,
        ReadingProgressEntity::class,
        BiteProgressEntity::class,
        TelemetryEventEntity::class,
        BadgeEntity::class,
        MockFriendEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LituraDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun biteDao(): BiteDao
    abstract fun questionDao(): QuestionDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun biteProgressDao(): BiteProgressDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun badgeDao(): BadgeDao
    abstract fun mockFriendDao(): MockFriendDao
}
