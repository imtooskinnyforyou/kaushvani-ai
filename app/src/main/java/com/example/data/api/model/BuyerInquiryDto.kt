package com.example.data.api.model

import com.example.data.model.BuyerInquiryEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BuyerInquiryDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "productId") val productId: Long,
    @Json(name = "productTitle") val productTitle: String,
    @Json(name = "buyerName") val buyerName: String,
    @Json(name = "buyerCompany") val buyerCompany: String = "",
    @Json(name = "buyerType") val buyerType: String = "Wholesale Boutique",
    @Json(name = "requestedQuantity") val requestedQuantity: Int = 50,
    @Json(name = "offeredPricePerUnit") val offeredPricePerUnit: Double = 0.0,
    @Json(name = "message") val message: String,
    @Json(name = "buyerPhone") val buyerPhone: String = "+91 94123 78901",
    @Json(name = "buyerEmail") val buyerEmail: String = "buyer@craftstore.in",
    @Json(name = "buyerCity") val buyerCity: String = "Mumbai, Maharashtra",
    @Json(name = "requiredDate") val requiredDate: String = "Within 2-3 weeks",
    @Json(name = "status") val status: String = "NEW",
    @Json(name = "artisanResponse") val artisanResponse: String = "",
    @Json(name = "notes") val notes: String = "",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class UpdateInquiryStatusRequest(
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class RespondInquiryRequest(
    @Json(name = "status") val status: String,
    @Json(name = "response") val response: String
)

@JsonClass(generateAdapter = true)
data class InquiryBatchSyncRequest(
    @Json(name = "inquiries") val inquiries: List<BuyerInquiryDto>,
    @Json(name = "lastSyncTimestamp") val lastSyncTimestamp: Long = 0L,
    @Json(name = "artisanPhone") val artisanPhone: String = ""
)

@JsonClass(generateAdapter = true)
data class InquiryBatchSyncResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "syncedCount") val syncedCount: Int,
    @Json(name = "remoteInquiries") val remoteInquiries: List<BuyerInquiryDto> = emptyList(),
    @Json(name = "serverTimestamp") val serverTimestamp: Long = System.currentTimeMillis(),
    @Json(name = "message") val message: String = "Inquiries synced successfully"
)

@JsonClass(generateAdapter = true)
data class BackendHealthResponse(
    @Json(name = "status") val status: String = "OK",
    @Json(name = "service") val service: String = "Kaarigar-API",
    @Json(name = "version") val version: String = "1.0.0",
    @Json(name = "serverTimestamp") val serverTimestamp: Long = System.currentTimeMillis()
)

fun BuyerInquiryDto.toEntity(): BuyerInquiryEntity {
    return BuyerInquiryEntity(
        id = id,
        productId = productId,
        productTitle = productTitle,
        buyerName = buyerName,
        buyerCompany = buyerCompany,
        buyerType = buyerType,
        requestedQuantity = requestedQuantity,
        offeredPricePerUnit = offeredPricePerUnit,
        message = message,
        buyerPhone = buyerPhone,
        buyerEmail = buyerEmail,
        buyerCity = buyerCity,
        requiredDate = requiredDate,
        status = status,
        artisanResponse = artisanResponse,
        notes = notes,
        timestamp = timestamp
    )
}

fun BuyerInquiryEntity.toDto(): BuyerInquiryDto {
    return BuyerInquiryDto(
        id = id,
        productId = productId,
        productTitle = productTitle,
        buyerName = buyerName,
        buyerCompany = buyerCompany,
        buyerType = buyerType,
        requestedQuantity = requestedQuantity,
        offeredPricePerUnit = offeredPricePerUnit,
        message = message,
        buyerPhone = buyerPhone,
        buyerEmail = buyerEmail,
        buyerCity = buyerCity,
        requiredDate = requiredDate,
        status = status,
        artisanResponse = artisanResponse,
        notes = notes,
        timestamp = timestamp
    )
}
