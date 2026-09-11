package com.example.data.api.model

import com.example.data.model.UserActivityEventEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserActivityEventDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "eventType") val eventType: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "category") val category: String = "BUSINESS",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),
    @Json(name = "metadataJson") val metadataJson: String = "{}"
)

@JsonClass(generateAdapter = true)
data class SyncActivityEventsRequest(
    @Json(name = "events") val events: List<UserActivityEventDto>
)

@JsonClass(generateAdapter = true)
data class SyncActivityEventsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "syncedCount") val syncedCount: Int,
    @Json(name = "serverTimestamp") val serverTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "message") val message: String = "Events successfully synchronized"
)

fun UserActivityEventEntity.toDto(): UserActivityEventDto {
    return UserActivityEventDto(
        id = this.id,
        eventType = this.eventType,
        title = this.title,
        description = this.description,
        category = this.category,
        timestamp = this.timestamp,
        metadataJson = this.metadataJson
    )
}

fun UserActivityEventDto.toEntity(isSynced: Boolean = true): UserActivityEventEntity {
    return UserActivityEventEntity(
        id = this.id,
        eventType = this.eventType,
        title = this.title,
        description = this.description,
        category = this.category,
        timestamp = this.timestamp,
        isSynced = isSynced,
        syncedAt = if (isSynced) System.currentTimeMillis() else null,
        metadataJson = this.metadataJson
    )
}
