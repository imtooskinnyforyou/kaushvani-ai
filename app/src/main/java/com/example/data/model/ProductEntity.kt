package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.data.db.StringListConverters

@Entity(tableName = "products")
@TypeConverters(StringListConverters::class)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val name: String = title,
    val regionalTitle: String = "",
    val description: String = "",
    val regionalDescription: String = "",
    val category: String = "Pottery", // Pottery, Handloom, Metalcraft, Woodcraft, Paintings, Jewelry, Leather
    val craftType: String = "", // e.g. Terracotta, Banarasi Brocade, Dhokra Brass, Madhubani
    val region: String = "", // e.g. Gorakhpur (UP), Varanasi (UP), Bastar (CG), Madhubani (Bihar)
    val rawMaterialCost: Double = 0.0,
    val laborHours: Double = 0.0,
    val hourlyWageRate: Double = 150.0,
    val packagingCost: Double = 50.0,
    val fairMinPrice: Double = 0.0,
    val fairMaxPrice: Double = 0.0,
    val retailPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val minOrderQuantity: Int = 1,
    val stockAvailable: Int = 10,
    val imageUri: String = "",
    val originalImageUri: String = "",
    val imagePaths: List<String> = if (imageUri.isNotBlank()) listOf(imageUri) else emptyList(),
    val imageStyleFilter: String = "Natural Studio",
    val voiceTranscript: String = "",
    val originalLanguage: String = "Hindi",
    val isGiTagged: Boolean = false,
    val tags: String = "", // Comma-separated or JSON tags
    val materialsUsed: String = "",
    val dimensions: String = "",
    val weightKg: Double = 0.5,
    val careInstructions: String = "",
    val artisanName: String = "Ram Prasad Prajapati",
    val artisanLocation: String = "Gorakhpur, Uttar Pradesh",
    val artisanPhone: String = "+91 98765 43210",
    val status: String = "PUBLISHED", // DRAFT, PUBLISHED, SOLD_OUT
    val viewCount: Int = 24,
    val inquiryCount: Int = 3,
    val timestamp: Long = System.currentTimeMillis()
) {
    @Ignore
    constructor(
        name: String,
        description: String,
        imagePaths: List<String> = emptyList(),
        category: String = "Handicraft",
        retailPrice: Double = 0.0,
        id: Long = 0
    ) : this(
        id = id,
        title = name,
        name = name,
        description = description,
        imagePaths = imagePaths,
        imageUri = imagePaths.firstOrNull() ?: "",
        category = category,
        retailPrice = retailPrice
    )

    @Ignore
    constructor(
        name: String,
        description: String,
        imagePath: String,
        category: String = "Handicraft",
        retailPrice: Double = 0.0,
        id: Long = 0
    ) : this(
        id = id,
        title = name,
        name = name,
        description = description,
        imagePaths = if (imagePath.isNotBlank()) listOf(imagePath) else emptyList(),
        imageUri = imagePath,
        category = category,
        retailPrice = retailPrice
    )

    val effectiveName: String
        get() = name.ifBlank { title }

    val effectiveTitle: String
        get() = title.ifBlank { name }
}

