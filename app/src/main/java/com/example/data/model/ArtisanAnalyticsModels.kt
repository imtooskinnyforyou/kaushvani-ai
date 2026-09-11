package com.example.data.model

/**
 * Data models for the Artisan Sales & Pricing Analytics Dashboard.
 * These models aggregate Room database historical records, catalog distribution,
 * and time-series pricing data to drive interactive D3.js visualization charts.
 */

data class MonthlySalesPerformance(
    val monthKey: String, // e.g. "2025-10"
    val monthLabel: String, // e.g. "Oct '25"
    val hindiMonthLabel: String, // e.g. "अक्तूबर '25"
    val unitsSold: Int,
    val totalRevenue: Double,
    val averageSellingPrice: Double,
    val averageProfitMarginPercent: Double,
    val topSellingCategory: String,
    val topSellingProduct: String
)

data class ProductCategoryDistribution(
    val category: String,
    val hindiCategory: String,
    val productCount: Int,
    val totalUnitsSold: Int,
    val totalRevenue: Double,
    val percentage: Double,
    val colorHex: String
)

data class PricingTrendPoint(
    val id: Long,
    val productId: Long,
    val productTitle: String,
    val category: String,
    val recordedDate: Long,
    val dateLabel: String,
    val sellingPrice: Double,
    val materialCost: Double,
    val laborHours: Double,
    val hourlyWageRate: Double,
    val laborCost: Double,
    val packagingCost: Double,
    val fairFloorPrice: Double,
    val suggestedRetailPrice: Double,
    val profitMarginPercent: Double,
    val salePeriod: String,
    val salesChannel: String,
    val isAboveLivingWage: Boolean
)

data class ArtisanSalesSummaryMetrics(
    val totalGrossRevenue: Double = 0.0,
    val totalUnitsSold: Int = 0,
    val averageOrderValue: Double = 0.0,
    val averageProfitMarginPercent: Double = 0.0,
    val fairLivingWageComplianceRate: Double = 100.0, // Percentage of sales >= fair floor price
    val totalProductsCataloged: Int = 0,
    val topPerformingCategory: String = "Pottery",
    val peakSeason: String = "Diwali Festive",
    val monthlyRevenueGrowthPercent: Double = 12.4
)

data class ArtisanAnalyticsDashboardData(
    val summaryMetrics: ArtisanSalesSummaryMetrics = ArtisanSalesSummaryMetrics(),
    val monthlySales: List<MonthlySalesPerformance> = emptyList(),
    val categoryDistribution: List<ProductCategoryDistribution> = emptyList(),
    val pricingTrends: List<PricingTrendPoint> = emptyList(),
    val availableCategories: List<String> = emptyList()
)
