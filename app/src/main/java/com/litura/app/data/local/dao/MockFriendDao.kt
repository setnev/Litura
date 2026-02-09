package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.litura.app.data.local.entity.MockFriendEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MockFriendDao {
    @Query("SELECT * FROM mock_friends ORDER BY totalXp DESC")
    fun getAllFriends(): Flow<List<MockFriendEntity>>

    @Query("SELECT COUNT(*) FROM mock_friends")
    suspend fun getFriendCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<MockFriendEntity>)

    @Query("UPDATE mock_friends SET totalXp = totalXp + :xpDelta, totalBitesCompleted = totalBitesCompleted + :biteDelta WHERE friendId = :friendId")
    suspend fun updateFriendProgress(friendId: String, xpDelta: Int, biteDelta: Int)
}
