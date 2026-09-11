package com.example.data.model

/**
 * Encapsulates aggregated statistical metrics from historical product sales and material costs.
 */
data class HistoricalPricingMetrics(
    val productId: Long = 0L,
    val totalRecords: Int = 0,
    val totalUnitsSold: Int = 0,
    val averageSellingPrice: Double = 0.0,
    val minSellingPrice: Double = 0.0,
    val maxSellingPrice: Double = 0.0,
    val averageMaterialCost: Double = 0.0,
    val materialCostInflationPercent: Double = 0.0, // Difference between current and historical average material cost
    val averageProfitMarginPercent: Double = 0.0,
    val highestDemandSeason: String = "Diwali Festive",
    val salesVelocityTrend: String = "STEADY", // RISING, STEADY, DECLINING
    val elasticitySummary: String = ""
)

/**
 * Seasonal pricing context factors for Indian handicraft markets.
 */
enum class PricingSeason(
    val displayName: String,
    val hindiName: String,
    val demandMultiplier: Double,
    val description: String
) {
    NORMAL(
        displayName = "Regular Season",
        hindiName = "सामान्य मौसम",
        demandMultiplier = 1.0,
        description = "Standard year-round craft sales volume"
    ),
    DIWALI_FESTIVE(
        displayName = "Festive Peak (Diwali / Dussehra)",
        hindiName = "त्योहारी मांग (दीपावली / दशहरा)",
        demandMultiplier = 1.35,
        description = "High willingness to pay, corporate hampers and home decor surge (+35%)"
    ),
    WEDDING_SEASON(
        displayName = "Wedding & Bridal Season",
        hindiName = "विवाह एवं लग्न मौसम",
        demandMultiplier = 1.40,
        description = "Premium handlooms, jewelry, and trousseau gifts surge (+40%)"
    ),
    OFF_SEASON_CLEARANCE(
        displayName = "Monsoon / Off-Season",
        hindiName = "मानसून / ऑफ-सीजन",
        demandMultiplier = 0.88,
        description = "Liquidation and B2B advance booking phase (-12%)"
    )
}

/**
 * Sales distribution channel for pricing elasticity.
 */
enum class ChannelType(
    val channelName: String,
    val hindiName: String,
    val wholesaleDiscountRate: Double
) {
    DIRECT_CRAFT_FAIR("Direct Craft Fair / Haat", "शिल्प मेला / हाट", 0.0),
    B2B_WHOLESALE("B2B Wholesale / Boutique", "थोक व्यापारी / बुटीक", 0.35),
    ECOMMERCE_D2C("Online E-Commerce", "ऑनलाइन ई-कॉमर्स", 0.10),
    EXPORT_MARKET("International Export", "अंतर्राष्ट्रीय निर्यात", 0.25)
}

/**
 * AI-generated dynamic pricing recommendation grounded in historical data.
 */
data class DynamicPricingRecommendation(
    val productId: Long,
    val productTitle: String,
    val recommendedRetailPrice: Double,
    val recommendedWholesalePrice: Double,
    val fairLivingWageFloorPrice: Double, // Absolute minimum to safeguard artisan hourly wage
    val currentProductionCost: Double,
    val estimatedProfitMarginPercent: Double,
    val materialInflationImpactPercent: Double,
    val seasonalAdjustmentAmount: Double,
    val pricingStrategyTag: String, // e.g. "Festive Surge (+25%)", "Material Cost Hedge (+15%)"
    val historicalElasticityNote: String,
    val justificationEnglish: String,
    val justificationHindi: String,
    val confidenceScore: Float = 0.95f,
    val timestamp: Long = System.currentTimeMillis()
)
