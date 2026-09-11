package com.example.data.api.model

import com.example.data.model.ProductEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "title") val title: String,
    @Json(name = "regionalTitle") val regionalTitle: String = "",
    @Json(name = "description") val description: String,
    @Json(name = "regionalDescription") val regionalDescription: String = "",
    @Json(name = "category") val category: String,
    @Json(name = "craftType") val craftType: String,
    @Json(name = "region") val region: String,
    @Json(name = "rawMaterialCost") val rawMaterialCost: Double = 0.0,
    @Json(name = "laborHours") val laborHours: Double = 0.0,
    @Json(name = "hourlyWageRate") val hourlyWageRate: Double = 150.0,
    @Json(name = "packagingCost") val packagingCost: Double = 50.0,
    @Json(name = "fairMinPrice") val fairMinPrice: Double = 0.0,
    @Json(name = "fairMaxPrice") val fairMaxPrice: Double = 0.0,
    @Json(name = "retailPrice") val retailPrice: Double = 0.0,
    @Json(name = "wholesalePrice") val wholesalePrice: Double = 0.0,
    @Json(name = "minOrderQuantity") val minOrderQuantity: Int = 1,
    @Json(name = "stockAvailable") val stockAvailable: Int = 10,
    @Json(name = "imageUri") val imageUri: String = "",
    @Json(name = "imageStyleFilter") val imageStyleFilter: String = "Natural Studio",
    @Json(name = "voiceTranscript") val voiceTranscript: String = "",
    @Json(name = "originalLanguage") val originalLanguage: String = "Hindi",
    @Json(name = "isGiTagged") val isGiTagged: Boolean = false,
    @Json(name = "tags") val tags: String = "",
    @Json(name = "materialsUsed") val materialsUsed: String = "",
    @Json(name = "dimensions") val dimensions: String = "",
    @Json(name = "weightKg") val weightKg: Double = 0.5,
    @Json(name = "careInstructions") val careInstructions: String = "",
    @Json(name = "artisanName") val artisanName: String = "Ram Prasad Prajapati",
    @Json(name = "artisanLocation") val artisanLocation: String = "Gorakhpur, Uttar Pradesh",
    @Json(name = "artisanPhone") val artisanPhone: String = "+91 98765 43210",
    @Json(name = "status") val status: String = "PUBLISHED",
    @Json(name = "viewCount") val viewCount: Int = 24,
    @Json(name = "inquiryCount") val inquiryCount: Int = 3,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ProductBatchSyncRequest(
    @Json(name = "products") val products: List<ProductDto>,
    @Json(name = "lastSyncTimestamp") val lastSyncTimestamp: Long = 0L,
    @Json(name = "artisanPhone") val artisanPhone: String = ""
)

@JsonClass(generateAdapter = true)
data class ProductBatchSyncResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "syncedCount") val syncedCount: Int,
    @Json(name = "remoteProducts") val remoteProducts: List<ProductDto> = emptyList(),
    @Json(name = "serverTimestamp") val serverTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "message") val message: String = "Products synced successfully"
)

fun ProductDto.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        title = title,
        regionalTitle = regionalTitle,
        description = description,
        regionalDescription = regionalDescription,
        category = category,
        craftType = craftType,
        region = region,
        rawMaterialCost = rawMaterialCost,
        laborHours = laborHours,
        hourlyWageRate = hourlyWageRate,
        packagingCost = packagingCost,
        fairMinPrice = fairMinPrice,
        fairMaxPrice = fairMaxPrice,
        retailPrice = retailPrice,
        wholesalePrice = wholesalePrice,
        minOrderQuantity = minOrderQuantity,
        stockAvailable = stockAvailable,
        imageUri = imageUri,
        imageStyleFilter = imageStyleFilter,
        voiceTranscript = voiceTranscript,
        originalLanguage = originalLanguage,
        isGiTagged = isGiTagged,
        tags = tags,
        materialsUsed = materialsUsed,
        dimensions = dimensions,
        weightKg = weightKg,
        careInstructions = careInstructions,
        artisanName = artisanName,
        artisanLocation = artisanLocation,
        artisanPhone = artisanPhone,
        status = status,
        viewCount = viewCount,
        inquiryCount = inquiryCount,
        timestamp = timestamp
    )
}

fun ProductEntity.toDto(): ProductDto {
    return ProductDto(
        id = id,
        title = title,
        regionalTitle = regionalTitle,
        description = description,
        regionalDescription = regionalDescription,
        category = category,
        craftType = craftType,
        region = region,
        rawMaterialCost = rawMaterialCost,
        laborHours = laborHours,
        hourlyWageRate = hourlyWageRate,
        packagingCost = packagingCost,
        fairMinPrice = fairMinPrice,
        fairMaxPrice = fairMaxPrice,
        retailPrice = retailPrice,
        wholesalePrice = wholesalePrice,
        minOrderQuantity = minOrderQuantity,
        stockAvailable = stockAvailable,
        imageUri = imageUri,
        imageStyleFilter = imageStyleFilter,
        voiceTranscript = voiceTranscript,
        originalLanguage = originalLanguage,
        isGiTagged = isGiTagged,
        tags = tags,
        materialsUsed = materialsUsed,
        dimensions = dimensions,
        weightKg = weightKg,
        careInstructions = careInstructions,
        artisanName = artisanName,
        artisanLocation = artisanLocation,
        artisanPhone = artisanPhone,
        status = status,
        viewCount = viewCount,
        inquiryCount = inquiryCount,
        timestamp = timestamp
    )
}
