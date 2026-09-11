package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buyer_inquiries")
data class BuyerInquiryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productTitle: String,
    val buyerName: String,
    val buyerCompany: String = "",
    val buyerType: String = "Wholesale Boutique", // Exporter, Wholesale Boutique, Retail Chain, Corporate Gifting, Direct Buyer
    val requestedQuantity: Int = 50,
    val offeredPricePerUnit: Double = 0.0,
    val message: String,
    val buyerPhone: String = "+91 94123 78901",
    val buyerEmail: String = "buyer@craftstore.in",
    val buyerCity: String = "Mumbai, Maharashtra",
    val requiredDate: String = "Within 2-3 weeks",
    val status: String = "NEW", // NEW, RESPONDED, ACCEPTED, REJECTED, COMPLETED
    val artisanResponse: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ArtisanProfile(
    val name: String = "Ram Prasad Prajapati",
    val craftSpecialty: String = "Traditional Terracotta & Pottery",
    val experienceYears: Int = 18,
    val villageOrCluster: String = "Bhiti Rawat, Gorakhpur",
    val state: String = "Uttar Pradesh",
    val phone: String = "+91 98765 43210",
    val upiId: String = "ramprasad.artisan@upi",
    val artisanCardNo: String = "PMV-UP-2024-8849",
    val isVishwakarmaEnrolled: Boolean = true,
    val totalSalesCount: Int = 142,
    val totalRevenue: Double = 184500.0,
    val preferredLanguage: String = "Hindi",
    val localePreference: String = "hi-IN"
)
