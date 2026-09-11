package com.example.data.ai.model

/**
 * Skill tiers for artisans determining base minimum fair hourly living wage.
 */
enum class ArtisanSkillTier(
    val title: String,
    val hindiTitle: String,
    val hourlyWage: Double
) {
    APPRENTICE("Apprentice Artisan", "प्रशिक्षु कारीगर", 120.0),
    SKILLED("Skilled Craftsman", "दक्ष शिल्पकार", 175.0),
    MASTER("Master / State Awardee", "सिद्धहस्त / उस्ताद कारीगर", 300.0)
}

/**
 * Target market segment for the dynamic pricing calculation.
 */
enum class TargetMarketChannel(
    val title: String,
    val hindiTitle: String,
    val multiplier: Double
) {
    RETAIL_D2C("Direct Retail (Consumer)", "सीधे ग्राहक (खुदरा)", 1.0),
    WHOLESALE_B2B("Wholesale Bulk (B2B/Institutional)", "थोक व्यापार (B2B)", 0.75),
    EXPORT_PREMIUM("Export & Collectors", "निर्यात एवं विदेशी बाजार", 1.65)
}

/**
 * Request payload for the Gemini Dynamic Pricing Assistant.
 */
data class DynamicPricingRequest(
    val materialCost: Double,
    val laborHours: Double,
    val hourlyWage: Double = 175.0,
    val packagingCost: Double = 60.0,
    val category: String = "Pottery",
    val specificCraftType: String = "Terracotta Claycraft",
    val craftTags: List<String> = emptyList(),
    val skillTier: ArtisanSkillTier = ArtisanSkillTier.SKILLED,
    val targetMarket: TargetMarketChannel = TargetMarketChannel.RETAIL_D2C,
    val artisanName: String = "Ram Prasad Prajapati",
    val clusterLocation: String = "Bhiti Rawat, Gorakhpur",
    val state: String = "Uttar Pradesh",
    val language: String = "Hindi"
)

/**
 * Impact of a specific tag on pricing power and market positioning.
 */
data class TagPriceImpact(
    val tag: String,
    val percentageBoost: Int,
    val justification: String
)

/**
 * Cost slice in visual breakdown bar.
 */
data class CostSliceItem(
    val label: String,
    val hindiLabel: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: Long
)

/**
 * Full AI-generated response from the Dynamic Pricing Assistant.
 */
data class DynamicPricingResponse(
    val minFairPrice: Double,
    val maxFairPrice: Double,
    val optimalRetailPrice: Double,
    val wholesalePrice: Double,
    val exportPrice: Double,
    val suggestedMinOrderQty: Int = 10,
    val tagImpacts: List<TagPriceImpact> = emptyList(),
    val costBreakdown: List<CostSliceItem> = emptyList(),
    val benchmarkComparison: String = "",
    val marketInsights: String = "",
    val regionalMarketInsights: String = "",
    val negotiationTip: String = "",
    val regionalNegotiationTip: String = "",
    val isAiGenerated: Boolean = true,
    val confidenceScore: Float = 0.94f
)
