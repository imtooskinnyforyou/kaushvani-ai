package com.example.data.model

/**
 * Data models for the Pricing Advisor component.
 * Leverages Room database history and material costs to suggest competitive
 * price ranges for new products based on similar items in the catalog.
 */

enum class PricingTierType(val label: String, val hindiLabel: String) {
    HAAT_DIRECT("Exhibition / Haat", "मेला / हाट"),
    RECOMMENDED_RETAIL("Catalog Retail", "कैटलॉग खुदरा"),
    PREMIUM_BOUTIQUE("Boutique / Gift", "प्रीमियम बुटीक"),
    WHOLESALE_B2B("B2B Wholesale", "थोक / B2B")
}

data class NewProductPricingInput(
    val title: String = "",
    val category: String = "Pottery",
    val craftType: String = "Terracotta",
    val materialCost: Double = 250.0,
    val laborHours: Double = 4.0,
    val hourlyWageRate: Double = 180.0,
    val packagingCost: Double = 50.0,
    val isGiTagged: Boolean = false
)

data class SimilarCatalogProduct(
    val productId: Long,
    val title: String,
    val category: String,
    val craftType: String,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val rawMaterialCost: Double,
    val laborHours: Double,
    val unitsSold: Int,
    val averageHistoricalSellingPrice: Double?,
    val historicalMarginPercent: Double?,
    val similarityReason: String
)

data class CompetitivePriceTier(
    val tierType: PricingTierType,
    val title: String,
    val hindiTitle: String,
    val minPrice: Double,
    val maxPrice: Double,
    val recommendedPrice: Double,
    val profitMarginPercent: Double,
    val netArtisanEarnings: Double,
    val hourlyReturn: Double,
    val description: String,
    val hindiDescription: String,
    val isRecommended: Boolean = false
)

data class PricingAdvisorResult(
    val input: NewProductPricingInput,
    val materialCost: Double,
    val laborHours: Double,
    val hourlyWageRate: Double,
    val directLaborCost: Double,
    val packagingCost: Double,
    val totalCostFloor: Double,
    val livingWageProtected: Boolean,
    val tiers: List<CompetitivePriceTier>,
    val recommendedTier: CompetitivePriceTier,
    val similarProducts: List<SimilarCatalogProduct>,
    val peerCount: Int,
    val peerAverageRetailPrice: Double,
    val peerMinRetailPrice: Double,
    val peerMaxRetailPrice: Double,
    val peerAverageMargin: Double,
    val materialCostPercentOfRetail: Double,
    val marketInsightsHindi: String,
    val marketInsightsEnglish: String
)
