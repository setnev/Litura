package com.litura.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.litura.app.data.local.entity.TelemetryEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Insert
    suspend fun insertEvent(event: TelemetryEventEntity)

    @Query("SELECT * FROM telemetry_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 100): Flow<List<TelemetryEventEntity>>

    @Query("SELECT * FROM telemetry_events WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getEventsForBook(bookId: String): Flow<List<TelemetryEventEntity>>

    @Query("DELETE FROM telemetry_events WHERE timestamp < :before")
    suspend fun deleteOldEvents(before: Long)
}
