package com.example.data.analytics

import android.util.Log
import com.example.data.api.KaarigarApiService
import com.example.data.api.model.SyncActivityEventsRequest
import com.example.data.api.model.toDto
import com.example.data.db.UserActivityEventDao
import com.example.data.model.UserActivityEventEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

interface EventTrackingService {
    val allEvents: Flow<List<UserActivityEventEntity>>
    val unsyncedEvents: Flow<List<UserActivityEventEntity>>
    val unsyncedCount: Flow<Int>
    val totalCount: Flow<Int>

    fun getRecentEvents(limit: Int = 10): Flow<List<UserActivityEventEntity>>

    suspend fun logEvent(
        eventType: String,
        title: String,
        description: String = "",
        category: String = "BUSINESS",
        metadata: Map<String, String> = emptyMap()
    ): Long

    suspend fun syncPendingEvents(): Result<Int>

    suspend fun clearHistory()
}

class RoomEventTrackingService(
    private val eventDao: UserActivityEventDao,
    private val apiService: KaarigarApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EventTrackingService {

    companion object {
        private const val TAG = "EventTrackingService"

        // Standard Event Types
        const val EVENT_PRODUCT_CREATED = "PRODUCT_CREATED"
        const val EVENT_PRODUCT_UPDATED = "PRODUCT_UPDATED"
        const val EVENT_PRODUCT_DELETED = "PRODUCT_DELETED"
        const val EVENT_INQUIRY_SENT = "INQUIRY_SENT"
        const val EVENT_INQUIRY_RESPONDED = "INQUIRY_RESPONDED"
        const val EVENT_PRICING_CALCULATED = "PRICING_CALCULATED"
        const val EVENT_CATALOG_GENERATED = "CATALOG_GENERATED"
        const val EVENT_VOICE_NOTE_RECORDED = "VOICE_NOTE_RECORDED"
        const val EVENT_OFFLINE_SYNC = "OFFLINE_SYNC"
    }

    override val allEvents: Flow<List<UserActivityEventEntity>> = eventDao.getAllEvents()
    override val unsyncedEvents: Flow<List<UserActivityEventEntity>> = eventDao.getUnsyncedEventsFlow()
    override val unsyncedCount: Flow<Int> = eventDao.getUnsyncedCount()
    override val totalCount: Flow<Int> = eventDao.getTotalEventCount()

    override fun getRecentEvents(limit: Int): Flow<List<UserActivityEventEntity>> {
        return eventDao.getRecentEvents(limit)
    }

    override suspend fun logEvent(
        eventType: String,
        title: String,
        description: String,
        category: String,
        metadata: Map<String, String>
    ): Long = withContext(ioDispatcher) {
        val metaJson = try {
            JSONObject(metadata as Map<*, *>).toString()
        } catch (e: Exception) {
            "{}"
        }

        val event = UserActivityEventEntity(
            eventType = eventType,
            title = title,
            description = description,
            category = category,
            timestamp = System.currentTimeMillis(),
            isSynced = false,
            syncedAt = null,
            metadataJson = metaJson
        )

        // 1. Immediately persist locally in Room database for offline reliability
        val eventId = eventDao.insertEvent(event)
        Log.d(TAG, "Activity event #$eventId logged offline in Room: [$eventType] $title")

        // 2. Opportunistic immediate sync if network is reachable
        try {
            val response = apiService.logActivityEvent(event.copy(id = eventId).toDto())
            if (response.isSuccessful) {
                eventDao.markEventsAsSynced(listOf(eventId), System.currentTimeMillis())
                Log.d(TAG, "Activity event #$eventId immediately synced to backend API")
            }
        } catch (e: Exception) {
            Log.i(TAG, "Activity event #$eventId saved locally in Room (offline cache active): ${e.message}")
        }

        eventId
    }

    override suspend fun syncPendingEvents(): Result<Int> = withContext(ioDispatcher) {
        try {
            val pendingEvents = eventDao.getUnsyncedEvents()
            if (pendingEvents.isEmpty()) {
                Log.d(TAG, "No pending offline events to sync.")
                return@withContext Result.success(0)
            }

            Log.d(TAG, "Attempting to sync ${pendingEvents.size} offline events to backend API...")
            val dtoList = pendingEvents.map { it.toDto() }
            val response = apiService.syncActivityEvents(SyncActivityEventsRequest(dtoList))

            if (response.isSuccessful) {
                val syncResult = response.body()
                val syncedIds = pendingEvents.map { it.id }
                eventDao.markEventsAsSynced(syncedIds, System.currentTimeMillis())
                val count = syncResult?.syncedCount ?: pendingEvents.size
                Log.d(TAG, "Successfully synced $count offline events to remote server!")
                Result.success(count)
            } else {
                Log.w(TAG, "Remote sync failed with status: ${response.code()}")
                Result.failure(Exception("Sync failed with status code ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network unavailable for event sync. Remaining in Room offline storage: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun clearHistory() = withContext(ioDispatcher) {
        eventDao.clearAllEvents()
    }
}
