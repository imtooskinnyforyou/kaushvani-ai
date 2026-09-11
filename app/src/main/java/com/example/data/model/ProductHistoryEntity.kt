package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity tracking historical product sales, price points, and material cost fluctuations over time.
 * This historical data powers the AI Dynamic Pricing engine to identify elasticity, seasonal demand surges,
 * and fair living wage protection for artisans.
 */
@Entity(
    tableName = "product_history",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["recordedDate"]),
        Index(value = ["category"])
    ]
)
data class ProductHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productTitle: String,
    val category: String,
    val recordedDate: Long = System.currentTimeMillis(),
    val salePeriod: String = "Normal Season", // e.g., "Diwali Festive", "Wedding Season", "Summer Craft Fair"
    val materialCost: Double, // Cost of raw materials (clay, silk yarn, brass, dyes) at the time
    val laborHours: Double = 5.0, // Craft production hours
    val hourlyWageRate: Double = 180.0, // Living wage rate
    val packagingCost: Double = 50.0,
    val sellingPrice: Double, // Actual transaction/realized price
    val suggestedRetailPrice: Double = 0.0, // Price recommended at that time
    val unitsSold: Int, // Volume of items sold in this period
    val unitsInStock: Int = 10,
    val salesChannel: String = "Direct Craft Fair", // "B2B Wholesale", "E-Commerce", "Direct Craft Fair", "Export"
    val buyerType: String = "RETAIL",
    val demandIndex: Double = 1.0, // 1.0 = normal, 1.4 = festive peak, 0.8 = off-season
    val competitorMarketAverage: Double = 0.0,
    val profitMarginPercent: Double = 0.0,
    val notes: String = ""
)
