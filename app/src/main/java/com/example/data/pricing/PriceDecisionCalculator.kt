package com.example.data.pricing

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic, safety-first price decision engine for KAUSHVANI.
 *
 * Core Mandates:
 * 1. Deterministic calculation first: Base Cost = Material + Labour + Packaging.
 * 2. Final recommended price CANNOT be below total base cost.
 * 3. Detect unusually high or low input values (e.g. ₹10 material + 1000h labor, or ₹100,000 material + 1h labor).
 * 4. Ask artisan to confirm suspicious inputs.
 * 5. Show a price RANGE instead of pretending there is one perfect price.
 * 6. Show transparent breakdown: "Why this price?"
 * 7. Allow the artisan to manually change the final selling price.
 * 8. Never claim "live market price" unless real verified market data is being used.
 * 9. If market data is unavailable, clearly state:
 *    "Market comparison unavailable — recommendation based on your costs and configured pricing rules."
 */

enum class PricingConfidence {
    LOW,
    MEDIUM,
    HIGH
}

enum class SafetyWarningType {
    ABNORMAL_LABOR_HOURS,
    EXTREME_MATERIAL_COST,
    PRICE_BELOW_COST,
    UNUSUALLY_HIGH_PRICE,
    UNUSUALLY_LOW_PRICE
}

data class PriceSafetyCheck(
    val isSuspicious: Boolean = false,
    val warningMessage: String? = null,
    val hindiWarningMessage: String? = null,
    val warningType: SafetyWarningType? = null,
    val requiresArtisanConfirmation: Boolean = false
)

data class CostComponentBreakdown(
    val title: String,
    val hindiTitle: String,
    val amount: Double,
    val percentageOfBase: Double,
    val explanation: String,
    val hindiExplanation: String
)

data class PriceCalculationResult(
    val materialCost: Double,
    val laborHours: Double,
    val hourlyWageRate: Double,
    val laborCost: Double,
    val packagingCost: Double,
    val baseCost: Double,
    val minFairPrice: Double,
    val maxFairPrice: Double,
    val suggestedPrice: Double,
    val wholesalePrice: Double,
    val confidence: PricingConfidence,
    val marketDataStatus: String,
    val hindiMarketDataStatus: String,
    val explanationBreakdown: List<CostComponentBreakdown>,
    val safetyCheck: PriceSafetyCheck,
    val configurableMarginPercent: Double,
    val targetChannel: String = "Both"
)

object PriceDecisionCalculator {

    const val DEFAULT_HOURLY_WAGE = 180.0 // Standard fair living wage floor in INR
    const val DEFAULT_PACKAGING_COST = 40.0 // Standard safe packaging in INR

    /**
     * Calculates transparent fair-trade pricing deterministically based on real costs.
     *
     * @param materialCost Raw material expenditure in INR
     * @param laborHours Handcrafting time in hours
     * @param hourlyWageRate Artisan living wage per hour in INR (minimum ₹150/hr)
     * @param packagingCost Packaging & protective wrapping in INR
     * @param targetChannel "Retail", "Wholesale", or "Both"
     * @param verifiedPeerAveragePrice Optional verified peer average price from Room database (null if unavailable)
     * @param configurableMarginPercent Desired profit margin over base cost (default 28%)
     */
    fun calculatePrice(
        materialCost: Double,
        laborHours: Double,
        hourlyWageRate: Double = DEFAULT_HOURLY_WAGE,
        packagingCost: Double = DEFAULT_PACKAGING_COST,
        targetChannel: String = "Both",
        verifiedPeerAveragePrice: Double? = null,
        configurableMarginPercent: Double = 28.0
    ): PriceCalculationResult {
        val safeMaterialCost = max(0.0, materialCost)
        val safeLaborHours = max(0.0, laborHours)
        val safeHourlyWage = max(150.0, hourlyWageRate)
        val safePackaging = max(0.0, packagingCost)

        // 1. BASE COST = Material + Labour + Packaging
        val laborCost = safeLaborHours * safeHourlyWage
        val baseCost = safeMaterialCost + laborCost + safePackaging

        // 2. SAFETY CHECKS: Detect suspicious / abnormal inputs
        val safetyCheck = evaluateSafety(safeMaterialCost, safeLaborHours, laborCost, baseCost)

        // 3. DETERMINISTIC MARGIN & PRICE RANGE
        // Minimum fair price: Base Cost + 15% minimum livelihood cushion
        val minFairPrice = roundToNearest(baseCost * 1.15, 10.0)

        // Maximum fair price: Base Cost + 45% (or 50% for high-skill artisan craft)
        val maxFairPrice = roundToNearest(baseCost * 1.45, 10.0)

        // Baseline suggested retail price using configurable business margin
        val marginMultiplier = 1.0 + (configurableMarginPercent.coerceIn(15.0, 60.0) / 100.0)
        val unadjustedSuggested = baseCost * marginMultiplier

        // Rule 1: Final recommended price CANNOT be below total base cost
        var suggestedPrice = max(baseCost, roundToNearest(unadjustedSuggested, 10.0))

        // Wholesale price: Base Cost + 12% to 18% margin for bulk buyer
        val wholesalePrice = max(baseCost, roundToNearest(baseCost * 1.15, 10.0))

        // If target is wholesale only, calibrate suggested price to wholesale
        if (targetChannel.equals("Wholesale", ignoreCase = true) || targetChannel.contains("थोक")) {
            suggestedPrice = wholesalePrice
        }

        // 4. MARKET DATA STATUS (Never claim "live market price" without real data)
        val marketStatus: String
        val hindiMarketStatus: String
        val confidence: PricingConfidence

        if (verifiedPeerAveragePrice != null && verifiedPeerAveragePrice > 0) {
            val formattedPeer = "₹" + verifiedPeerAveragePrice.toInt()
            marketStatus = "Verified Room catalog comparison available (Peer average: $formattedPeer)."
            hindiMarketStatus = "सत्यापित कैटलॉग तुलना उपलब्ध (औसत मूल्य: $formattedPeer)।"
            confidence = if (safetyCheck.isSuspicious) PricingConfidence.LOW else PricingConfidence.HIGH
        } else {
            marketStatus = "Market comparison unavailable — recommendation based on your costs and configured pricing rules."
            hindiMarketStatus = "बाज़ार तुलना अनुपलब्ध — मूल्य केवल आपकी वास्तविक लागत और निर्धारित नियमों पर आधारित है।"
            confidence = if (safetyCheck.isSuspicious) PricingConfidence.LOW else PricingConfidence.MEDIUM
        }

        // 5. TRANSPARENT BREAKDOWN: "Why this price?"
        val breakdown = mutableListOf<CostComponentBreakdown>()
        val totalForPct = if (baseCost > 0) baseCost else 1.0

        breakdown.add(
            CostComponentBreakdown(
                title = "Raw Materials",
                hindiTitle = "कच्चा माल",
                amount = safeMaterialCost,
                percentageOfBase = (safeMaterialCost / totalForPct) * 100.0,
                explanation = "Direct cost of clay, yarn, wood, dyes, metal, or raw inputs.",
                hindiExplanation = "शिल्प निर्माण में प्रयुक्त सामग्री का वास्तविक खर्च।"
            )
        )

        breakdown.add(
            CostComponentBreakdown(
                title = "Artisan Fair Labor",
                hindiTitle = "कारीगरी मजदूरी",
                amount = laborCost,
                percentageOfBase = (laborCost / totalForPct) * 100.0,
                explanation = "${safeLaborHours.format(1)} hrs × ₹${safeHourlyWage.toInt()}/hr guaranteed living wage.",
                hindiExplanation = "${safeLaborHours.format(1)} घंटे × ₹${safeHourlyWage.toInt()}/घंटा संरक्षित मजदूरी।"
            )
        )

        if (safePackaging > 0) {
            breakdown.add(
                CostComponentBreakdown(
                    title = "Eco Packaging & Safety",
                    hindiTitle = "सुरक्षित पैकेजिंग",
                    amount = safePackaging,
                    percentageOfBase = (safePackaging / totalForPct) * 100.0,
                    explanation = "Protective wrapping, bubble sheet, and eco carton box.",
                    hindiExplanation = "सुरक्षित शिपिंग व पार्सल पैकेजिंग का खर्च।"
                )
            )
        }

        val profitMarginAmount = suggestedPrice - baseCost
        breakdown.add(
            CostComponentBreakdown(
                title = "Artisan Fair Margin",
                hindiTitle = "कारीगर का सुरक्षित मुनाफा",
                amount = max(0.0, profitMarginAmount),
                percentageOfBase = (profitMarginAmount / totalForPct) * 100.0,
                explanation = "${configurableMarginPercent.toInt()}% sustainable business margin to support your livelihood.",
                hindiExplanation = "${configurableMarginPercent.toInt()}% आय वृद्धि व पारिवारिक सुरक्षा हेतु।"
            )
        )

        return PriceCalculationResult(
            materialCost = safeMaterialCost,
            laborHours = safeLaborHours,
            hourlyWageRate = safeHourlyWage,
            laborCost = laborCost,
            packagingCost = safePackaging,
            baseCost = baseCost,
            minFairPrice = minFairPrice,
            maxFairPrice = maxFairPrice,
            suggestedPrice = suggestedPrice,
            wholesalePrice = wholesalePrice,
            confidence = confidence,
            marketDataStatus = marketStatus,
            hindiMarketDataStatus = hindiMarketStatus,
            explanationBreakdown = breakdown,
            safetyCheck = safetyCheck,
            configurableMarginPercent = configurableMarginPercent,
            targetChannel = targetChannel
        )
    }

    /**
     * Evaluates inputs for anomalies (e.g. ₹10 material + 1000 hrs, or ₹100,000 material + 1 hr).
     */
    fun evaluateSafety(
        materialCost: Double,
        laborHours: Double,
        laborCost: Double,
        baseCost: Double
    ): PriceSafetyCheck {
        // Case 1: Abnormal labor hours (e.g. 1000 hours entered for small material cost)
        if (laborHours >= 200.0 || (laborHours >= 50.0 && materialCost < 30.0)) {
            return PriceSafetyCheck(
                isSuspicious = true,
                warningType = SafetyWarningType.ABNORMAL_LABOR_HOURS,
                warningMessage = "Abnormal labor time detected: ${laborHours.toInt()} hours for ₹${materialCost.toInt()} material. Please verify.",
                hindiWarningMessage = "असामान्य श्रम समय: ₹${materialCost.toInt()} सामग्री के लिए ${laborHours.toInt()} घंटे दर्ज किए गए हैं। कृपया पुष्टि करें।",
                requiresArtisanConfirmation = true
            )
        }

        // Case 2: Extreme / Unusually high raw material cost (e.g. ₹100,000 with 1 hour labor)
        if (materialCost >= 50000.0 || (materialCost >= 25000.0 && laborHours <= 2.0)) {
            val formatted = NumberFormat.getNumberInstance(Locale("en", "IN")).format(materialCost.toLong())
            return PriceSafetyCheck(
                isSuspicious = true,
                warningType = SafetyWarningType.EXTREME_MATERIAL_COST,
                warningMessage = "Unusual raw material cost: ₹$formatted. Please verify if this is correct.",
                hindiWarningMessage = "असामान्य कच्चा माल लागत: ₹$formatted। क्या यह सही है? कृपया पुष्टि करें।",
                requiresArtisanConfirmation = true
            )
        }

        // Case 3: Zero or near-zero total base cost
        if (baseCost <= 0.0) {
            return PriceSafetyCheck(
                isSuspicious = true,
                warningType = SafetyWarningType.UNUSUALLY_LOW_PRICE,
                warningMessage = "Base cost is zero. Please provide material cost or labor time.",
                hindiWarningMessage = "लागत शून्य है। कृपया कच्चा माल या श्रम समय दर्ज करें।",
                requiresArtisanConfirmation = true
            )
        }

        return PriceSafetyCheck(isSuspicious = false)
    }

    private fun roundToNearest(value: Double, step: Double): Double {
        return (value / step).roundToInt() * step
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)
}
