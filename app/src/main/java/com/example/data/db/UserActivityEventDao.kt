package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserActivityEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityEventDao {

    @Query("SELECT * FROM user_activity_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<UserActivityEventEntity>>

    @Query("SELECT * FROM user_activity_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<UserActivityEventEntity>>

    @Query("SELECT * FROM user_activity_events WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedEvents(): List<UserActivityEventEntity>

    @Query("SELECT * FROM user_activity_events WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getUnsyncedEventsFlow(): Flow<List<UserActivityEventEntity>>

    @Query("SELECT COUNT(*) FROM user_activity_events WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_activity_events")
    fun getTotalEventCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UserActivityEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<UserActivityEventEntity>)

    @Update
    suspend fun updateEvent(event: UserActivityEventEntity)

    @Query("UPDATE user_activity_events SET isSynced = 1, syncedAt = :syncedAt WHERE id IN (:eventIds)")
    suspend fun markEventsAsSynced(eventIds: List<Long>, syncedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_activity_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("DELETE FROM user_activity_events")
    suspend fun clearAllEvents()
}
