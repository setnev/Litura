package com.litura.app.engine

import com.litura.app.data.datastore.UserPreferencesDataStore
import com.litura.app.data.local.dao.MockFriendDao
import com.litura.app.data.local.entity.MockFriendEntity
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class CompetitiveStandings(
    val friendsAhead: Int,
    val friendsBehind: Int,
    val rankDelta: String,
    val highlights: List<CompetitiveHighlight>
)

data class CompetitiveHighlight(
    val friendName: String,
    val message: String
)

@Singleton
class CompetitiveEngine @Inject constructor(
    private val mockFriendDao: MockFriendDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) {
    private data class FriendSeed(
        val id: String,
        val name: String,
        val avatarId: String,
        val baseXp: Int,
        val baseBites: Int,
        val dailyXp: Int,
        val dailyBites: Int,
        val competency: Double,
        val streak: Int
    )

    private val seeds = listOf(
        FriendSeed("f_alex", "Alex", "avatar_02", 200, 20, 30, 3, 78.0, 2),
        FriendSeed("f_sam", "Sam", "avatar_03", 350, 35, 45, 5, 82.0, 5),
        FriendSeed("f_jordan", "Jordan", "avatar_04", 100, 10, 20, 2, 71.0, 1),
        FriendSeed("f_casey", "Casey", "avatar_05", 500, 50, 55, 6, 88.0, 7),
        FriendSeed("f_riley", "Riley", "avatar_06", 280, 28, 35, 4, 75.0, 3)
    )

    suspend fun seedFriendsIfNeeded() {
        if (mockFriendDao.getFriendCount() > 0) return
        val now = System.currentTimeMillis()
        val friends = seeds.map { s ->
            MockFriendEntity(
                friendId = s.id,
                displayName = s.name,
                avatarId = s.avatarId,
                totalXp = s.baseXp,
                totalBitesCompleted = s.baseBites,
                avgCompetency = s.competency,
                currentStreakDays = s.streak,
                dailyXpRate = s.dailyXp,
                dailyBiteRate = s.dailyBites,
                seededAt = now
            )
        }
        mockFriendDao.insertFriends(friends)
    }

    suspend fun updateFriendProgress() {
        val friends = mockFriendDao.getAllFriends().first()
        val now = System.currentTimeMillis()
        for (friend in friends) {
            val daysSinceSeeded = TimeUnit.MILLISECONDS.toDays(now - friend.seededAt).toInt()
            val expectedXp = friend.dailyXpRate * daysSinceSeeded
            val expectedBites = friend.dailyBiteRate * daysSinceSeeded
            val xpDelta = (friend.dailyXpRate + expectedXp) - friend.totalXp
            val biteDelta = (friend.dailyBiteRate + expectedBites) - friend.totalBitesCompleted
            if (xpDelta > 0 || biteDelta > 0) {
                mockFriendDao.updateFriendProgress(
                    friend.friendId,
                    maxOf(xpDelta, 0),
                    maxOf(biteDelta, 0)
                )
            }
        }
    }

    suspend fun getComparisons(): CompetitiveStandings {
        val prefs = userPreferencesDataStore.preferences.first()
        val friends = mockFriendDao.getAllFriends().first()
        val userXp = prefs.totalXp

        var ahead = 0
        var behind = 0
        val highlights = mutableListOf<CompetitiveHighlight>()

        for (friend in friends) {
            if (friend.totalXp > userXp) {
                ahead++
            } else {
                behind++
                if (userXp - friend.totalXp < friend.dailyXpRate * 2) {
                    highlights.add(
                        CompetitiveHighlight(
                            friend.displayName,
                            "You passed ${friend.displayName} recently!"
                        )
                    )
                }
            }
        }

        val delta = if (behind > ahead) "+${behind - ahead}" else if (ahead > behind) "-${ahead - behind}" else "0"

        return CompetitiveStandings(
            friendsAhead = ahead,
            friendsBehind = behind,
            rankDelta = delta,
            highlights = highlights
        )
    }
}
