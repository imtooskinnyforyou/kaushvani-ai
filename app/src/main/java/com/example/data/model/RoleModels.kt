package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class UserRole {
    ARTISAN,
    BUYER,
    ADMIN
}

data class BulkRequirement(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val category: String,
    val quantityRequired: Int,
    val targetBudgetPerUnit: Double,
    val buyerName: String,
    val buyerCompany: String,
    val buyerCity: String,
    val buyerPhone: String,
    val buyerEmail: String,
    val description: String,
    val deadlineDate: String = "Within 30 Days",
    val status: String = "ACTIVE", // ACTIVE, FULFILLED, CLOSED
    val timestamp: Long = System.currentTimeMillis()
)

data class CraftCategory(
    val id: String,
    val name: String,
    val hindiName: String,
    val description: String,
    val iconName: String = "Palette",
    val itemCount: Int = 12,
    val isActive: Boolean = true
)

data class AiProcessingEvent(
    val id: Long = System.currentTimeMillis(),
    val taskType: String, // GEMINI_VISION_ENHANCE, VOICE_TRANSLATION, FAIR_PRICE_ENGINE, SEO_TAG_GENERATION
    val languageOrFilter: String,
    val durationMs: Long,
    val status: String = "SUCCESS", // SUCCESS, FAILED
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

data class ModerationFlag(
    val id: Long = System.currentTimeMillis(),
    val productId: Long,
    val productTitle: String,
    val artisanName: String,
    val reason: String,
    val flaggedBy: String = "System AI & Community",
    val status: String = "PENDING_REVIEW", // PENDING_REVIEW, RESOLVED_TAKEN_DOWN, RESOLVED_APPROVED
    val timestamp: Long = System.currentTimeMillis()
)

data class ArtisanRegistryItem(
    val id: Long,
    val name: String,
    val craftSpecialty: String,
    val villageOrCluster: String,
    val state: String,
    val phone: String,
    val vishwakarmaId: String,
    val isVerified: Boolean = true,
    val totalProducts: Int = 4,
    val totalSales: Double = 35000.0,
    val joinedDate: String = "Jan 2024"
)
