package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.BadgeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM earned_badges WHERE userId = :userId")
    fun getEarnedBadges(userId: String): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM earned_badges WHERE userId = :userId AND bookId = :bookId")
    fun getEarnedBadgesForBook(userId: String, bookId: String): Flow<List<BadgeEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM earned_badges WHERE userId = :userId AND badgeId = :badgeId)")
    suspend fun hasBadge(userId: String, badgeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBadge(badge: BadgeEntity)
}
