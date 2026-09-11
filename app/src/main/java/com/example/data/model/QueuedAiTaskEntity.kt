package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an AI processing task queued for offline or background execution
 * via WorkManager. When connectivity is restored, the background worker automatically
 * picks up these tasks, invokes the appropriate AI service (Gemini metadata,
 * voice-to-catalog parsing, or dynamic pricing engine), updates the product, and marks the task complete.
 */
@Entity(tableName = "queued_ai_tasks")
data class QueuedAiTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskType: String, // "VOICE_CATALOG_ENRICHMENT", "DYNAMIC_PRICING", "PRODUCT_METADATA", "CRAFT_STORY"
    val productId: Long? = null,
    val title: String = "",
    val inputPayloadJson: String = "{}",
    val status: String = "PENDING", // "PENDING", "PROCESSING", "COMPLETED", "FAILED"
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val lastAttemptTimestamp: Long = 0L,
    val errorMessage: String? = null,
    val resultPayloadJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
