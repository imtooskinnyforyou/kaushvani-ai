package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for tracking local artisan business activities and performance events.
 * Stored locally offline and synchronized with the remote backend whenever connectivity is available.
 */
@Entity(tableName = "user_activity_events")
data class UserActivityEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String, // PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, INQUIRY_SENT, INQUIRY_RESPONDED, PRICING_CALCULATED, CATALOG_GENERATED, VOICE_NOTE_RECORDED, OFFLINE_SYNC
    val title: String, // Human-readable event summary
    val description: String = "", // Detailed notes / metadata context
    val category: String = "BUSINESS", // CATALOG, INQUIRY, PRICING, VOICE, BUSINESS, SYSTEM
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val metadataJson: String = "{}"
)
