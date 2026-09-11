package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a queued catalog synchronization operation (Create, Update, Delete)
 * to be processed in the background via WorkManager when network connectivity is available.
 */
@Entity(tableName = "catalog_sync_queue")
data class CatalogSyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val payloadJson: String = "{}",
    val status: String = "PENDING", // "PENDING", "PROCESSING", "SYNCED", "FAILED"
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val lastAttemptTimestamp: Long = 0L,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
