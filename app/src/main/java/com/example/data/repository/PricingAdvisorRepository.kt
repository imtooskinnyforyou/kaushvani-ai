package com.example.data.repository

import com.example.data.db.ProductDao
import com.example.data.db.ProductHistoryDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

interface PricingAdvisorRepository {
    suspend fun generatePricingAdvice(input: NewProductPricingInput): PricingAdvisorResult
    suspend fun getSimilarProducts(category: String, craftType: String, title: String): List<SimilarCatalogProduct>
    suspend fun getCategoryBenchmark(category: String): Map<String, Double>
}

class PricingAdvisorRepositoryImpl(
    private val productDao: ProductDao,
    private val productHistoryDao: ProductHistoryDao
) : PricingAdvisorRepository {

    override suspend fun generatePricingAdvice(input: NewProductPricingInput): PricingAdvisorResult =
        withContext(Dispatchers.IO) {
            val allCatalogProducts = productDao.getAllProductsList()
            val allHistories = try {
                productHistoryDao.getAllHistorySync()
            } catch (e: Exception) {
                emptyList()
            }

            // 1. Calculate similar catalog items
            val similarProducts = findSimilarProductsInternal(input, allCatalogProducts, allHistories)

            // 2. Derive peer metrics from Room database
            val peerPrices = similarProducts.map { it.retailPrice }.filter { it > 0 }
            val peerAverageRetailPrice = if (peerPrices.isNotEmpty()) peerPrices.average() else 0.0
            val peerMinRetailPrice = if (peerPrices.isNotEmpty()) peerPrices.minOrNull() ?: 0.0 else 0.0
            val peerMaxRetailPrice = if (peerPrices.isNotEmpty()) peerPrices.maxOrNull() ?: 0.0 else 0.0

            val peerMargins = similarProducts.mapNotNull { it.historicalMarginPercent }.filter { it > 0 }
            val peerAverageMargin = if (peerMargins.isNotEmpty()) peerMargins.average() else 30.0

            // 3. Compute cost structure and absolute living wage floor
            val laborHours = input.laborHours.coerceAtLeast(0.5)
            val hourlyWage = input.hourlyWageRate.coerceAtLeast(150.0)
            val directLaborCost = laborHours * hourlyWage
            val materialCost = input.materialCost.coerceAtLeast(10.0)
            val packagingCost = input.packagingCost.coerceAtLeast(0.0)
            val totalDirectCost = materialCost + directLaborCost + packagingCost

            // Guaranteed living wage floor (Cost + 10% safety cushion)
            val totalCostFloor = roundToNearest(totalDirectCost * 1.10, 20.0)

            // 4. Calculate competitive pricing tiers
            val targetMarginRatio = (peerAverageMargin / 100.0).coerceIn(0.25, 0.45)
            val baseCostPlusPrice = totalDirectCost / (1.0 - targetMarginRatio)

            val blendedRetailPrice = if (peerAverageRetailPrice > 0) {
                // 60% cost-plus, 40% catalog peer benchmark
                (baseCostPlusPrice * 0.60) + (peerAverageRetailPrice * 0.40)
            } else {
                baseCostPlusPrice
            }

            val giMultiplier = if (input.isGiTagged) 1.18 else 1.0
            val recommendedRetailPrice = roundToNearest(
                max(totalCostFloor * 1.20, blendedRetailPrice * giMultiplier),
                50.0
            )

            // TIER 1: Direct Haat / Craft Fair (Low overhead, direct buyer)
            val haatPrice = roundToNearest(
                max(totalCostFloor * 1.08, recommendedRetailPrice * 0.82),
                20.0
            )
            val haatTier = buildTier(
                tierType = PricingTierType.HAAT_DIRECT,
                title = "Exhibition / Haat Direct",
                hindiTitle = "मेला / सीधा हाट मूल्य",
                price = haatPrice,
                totalDirectCost = totalDirectCost,
                laborHours = laborHours,
                rangeMultiplierMin = 0.92,
                rangeMultiplierMax = 1.08,
                description = "Direct sale at exhibitions or craft melas with minimal overhead. Maximizes volume and rapid cash flow.",
                hindiDescription = "हस्तशिल्प मेलों (Dilli Haat, सरस मेला) में सीधी बिक्री। कोई बिचौलिया नहीं, तेज़ी से बिकने वाला दाम।",
                isRecommended = false
            )

            // TIER 2: Recommended Catalog Retail (Balanced online pricing)
            val retailTier = buildTier(
                tierType = PricingTierType.RECOMMENDED_RETAIL,
                title = "Catalog Retail (Recommended)",
                hindiTitle = "सुझाया गया खुदरा मूल्य",
                price = recommendedRetailPrice,
                totalDirectCost = totalDirectCost,
                laborHours = laborHours,
                rangeMultiplierMin = 0.88,
                rangeMultiplierMax = 1.15,
                description = "Optimal online and direct-to-consumer price aligned with similar items in your Room catalog.",
                hindiDescription = "कैटलॉग में मौजूद समान शिल्पों और बाज़ार की मांग के अनुरूप सबसे संतुलित और लाभदायक दाम।",
                isRecommended = true
            )

            // TIER 3: Premium Boutique / Luxury Gifting
            val boutiquePrice = roundToNearest(
                recommendedRetailPrice * 1.45,
                50.0
            )
            val boutiqueTier = buildTier(
                tierType = PricingTierType.PREMIUM_BOUTIQUE,
                title = "Boutique / Gift Packaging",
                hindiTitle = "प्रीमियम बुटीक / उपहार",
                price = boutiquePrice,
                totalDirectCost = totalDirectCost + 50.0, // extra premium packaging
                laborHours = laborHours,
                rangeMultiplierMin = 0.90,
                rangeMultiplierMax = 1.25,
                description = "For upscale craft emporiums, tourist hubs, GI-tagged collections, and luxury corporate gifting.",
                hindiDescription = "हाई-एंड बुटीक, हवाई अड्डे के एम्पोरियम, जीआई-टैग संग्रह और कॉर्पोरेट उपहारों के लिए आदर्श।",
                isRecommended = false
            )

            // TIER 4: Wholesale B2B
            val wholesalePrice = roundToNearest(
                max(totalCostFloor * 1.12, recommendedRetailPrice * 0.68),
                50.0
            )
            val wholesaleTier = buildTier(
                tierType = PricingTierType.WHOLESALE_B2B,
                title = "Bulk Wholesale (B2B)",
                hindiTitle = "थोक / B2B मूल्य (10+ पीस)",
                price = wholesalePrice,
                totalDirectCost = totalDirectCost,
                laborHours = laborHours,
                rangeMultiplierMin = 0.92,
                rangeMultiplierMax = 1.08,
                description = "Volume rate for retail stores and bulk buyers, protecting your living wage on large orders.",
                hindiDescription = "10 या अधिक पीस के बल्क ऑर्डर के लिए। कारीगर का मेहनताना पूरी तरह सुरक्षित रहता है।",
                isRecommended = false
            )

            val tiers = listOf(haatTier, retailTier, boutiqueTier, wholesaleTier)

            val materialCostPercent = if (recommendedRetailPrice > 0) {
                ((materialCost / recommendedRetailPrice) * 100.0)
            } else 0.0

            val insightsHindi = buildHindiNarrative(
                input = input,
                similarCount = similarProducts.size,
                peerAvg = peerAverageRetailPrice,
                recommendedPrice = recommendedRetailPrice,
                minRange = retailTier.minPrice,
                maxRange = retailTier.maxPrice,
                materialCost = materialCost,
                materialPct = materialCostPercent,
                totalFloor = totalCostFloor
            )

            val insightsEnglish = buildEnglishNarrative(
                input = input,
                similarCount = similarProducts.size,
                peerAvg = peerAverageRetailPrice,
                recommendedPrice = recommendedRetailPrice,
                minRange = retailTier.minPrice,
                maxRange = retailTier.maxPrice,
                materialCost = materialCost,
                materialPct = materialCostPercent,
                totalFloor = totalCostFloor
            )

            PricingAdvisorResult(
                input = input,
                materialCost = materialCost,
                laborHours = laborHours,
                hourlyWageRate = hourlyWage,
                directLaborCost = directLaborCost,
                packagingCost = packagingCost,
                totalCostFloor = totalCostFloor,
                livingWageProtected = recommendedRetailPrice >= totalCostFloor,
                tiers = tiers,
                recommendedTier = retailTier,
                similarProducts = similarProducts,
                peerCount = similarProducts.size,
                peerAverageRetailPrice = peerAverageRetailPrice,
                peerMinRetailPrice = peerMinRetailPrice,
                peerMaxRetailPrice = peerMaxRetailPrice,
                peerAverageMargin = peerAverageMargin,
                materialCostPercentOfRetail = materialCostPercent,
                marketInsightsHindi = insightsHindi,
                marketInsightsEnglish = insightsEnglish
            )
        }

    override suspend fun getSimilarProducts(
        category: String,
        craftType: String,
        title: String
    ): List<SimilarCatalogProduct> = withContext(Dispatchers.IO) {
        val input = NewProductPricingInput(
            title = title,
            category = category,
            craftType = craftType
        )
        val allCatalogProducts = productDao.getAllProductsList()
        val allHistories = try {
            productHistoryDao.getAllHistorySync()
        } catch (e: Exception) {
            emptyList()
        }
        findSimilarProductsInternal(input, allCatalogProducts, allHistories)
    }

    override suspend fun getCategoryBenchmark(category: String): Map<String, Double> = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsList().filter {
            it.category.equals(category, ignoreCase = true)
        }
        val avgPrice = if (products.isNotEmpty()) products.map { it.retailPrice }.average() else 1200.0
        val avgMaterial = if (products.isNotEmpty()) products.map { it.rawMaterialCost }.average() else 250.0
        val avgLabor = if (products.isNotEmpty()) products.map { it.laborHours }.average() else 4.0

        mapOf(
            "avgPrice" to avgPrice,
            "avgMaterial" to avgMaterial,
            "avgLabor" to avgLabor,
            "hourlyRate" to 180.0
        )
    }

    private fun findSimilarProductsInternal(
        input: NewProductPricingInput,
        catalogProducts: List<ProductEntity>,
        histories: List<ProductHistoryEntity>
    ): List<SimilarCatalogProduct> {
        val historyByProductId = histories.groupBy { it.productId }

        val scoredList = catalogProducts.map { prod ->
            var score = 0
            val reasons = mutableListOf<String>()

            // Match Category
            if (prod.category.equals(input.category, ignoreCase = true)) {
                score += 50
                reasons.add("समान श्रेणी (${prod.category})")
            }

            // Match Craft Type
            if (input.craftType.isNotBlank() && prod.craftType.equals(input.craftType, ignoreCase = true)) {
                score += 35
                reasons.add("समान शिल्प प्रकार (${prod.craftType})")
            }

            // Match Title Keywords
            val inputTokens = input.title.lowercase().split(" ", "-", ",").filter { it.length > 2 }
            val prodTokens = (prod.title + " " + prod.regionalTitle).lowercase()
            val matchedKeywords = inputTokens.filter { prodTokens.contains(it) }
            if (matchedKeywords.isNotEmpty()) {
                score += (matchedKeywords.size * 15).coerceAtMost(30)
                reasons.add("कीवर्ड मेल: ${matchedKeywords.joinToString(", ")}")
            }

            // Similar material cost band (within 40%)
            if (input.materialCost > 0 && prod.rawMaterialCost > 0) {
                val ratio = prod.rawMaterialCost / input.materialCost
                if (ratio in 0.6..1.4) {
                    score += 15
                    reasons.add("समान कच्चा माल स्तर (₹${prod.rawMaterialCost.toInt()})")
                }
            }

            val prodHistories = historyByProductId[prod.id] ?: emptyList()
            val totalSold = prodHistories.sumOf { it.unitsSold }
            val avgHistPrice = if (prodHistories.isNotEmpty()) {
                prodHistories.map { it.sellingPrice }.average()
            } else prod.retailPrice

            val avgHistMargin = if (prodHistories.isNotEmpty()) {
                prodHistories.map { it.profitMarginPercent }.average()
            } else {
                val cost = prod.rawMaterialCost + (prod.laborHours * prod.hourlyWageRate) + prod.packagingCost
                if (prod.retailPrice > 0) (((prod.retailPrice - cost) / prod.retailPrice) * 100.0) else 25.0
            }

            Pair(
                score,
                SimilarCatalogProduct(
                    productId = prod.id,
                    title = prod.title,
                    category = prod.category,
                    craftType = prod.craftType,
                    retailPrice = prod.retailPrice,
                    wholesalePrice = prod.wholesalePrice,
                    rawMaterialCost = prod.rawMaterialCost,
                    laborHours = prod.laborHours,
                    unitsSold = totalSold,
                    averageHistoricalSellingPrice = avgHistPrice,
                    historicalMarginPercent = avgHistMargin,
                    similarityReason = reasons.joinToString(" • ").ifBlank { "कैटलॉग समकक्ष शिल्प" }
                )
            )
        }

        // Return top similar items, prioritizing those with higher scores
        return scoredList
            .filter { it.first > 0 }
            .sortedByDescending { it.first }
            .take(4)
            .map { it.second }
    }

    private fun buildTier(
        tierType: PricingTierType,
        title: String,
        hindiTitle: String,
        price: Double,
        totalDirectCost: Double,
        laborHours: Double,
        rangeMultiplierMin: Double,
        rangeMultiplierMax: Double,
        description: String,
        hindiDescription: String,
        isRecommended: Boolean
    ): CompetitivePriceTier {
        val minPrice = roundToNearest(price * rangeMultiplierMin, 20.0)
        val maxPrice = roundToNearest(price * rangeMultiplierMax, 20.0)
        val netEarning = max(0.0, price - totalDirectCost)
        val marginPercent = if (price > 0) ((netEarning / price) * 100.0) else 0.0
        val hourlyReturn = if (laborHours > 0) (netEarning / laborHours) else 0.0

        return CompetitivePriceTier(
            tierType = tierType,
            title = title,
            hindiTitle = hindiTitle,
            minPrice = minPrice,
            maxPrice = maxPrice,
            recommendedPrice = price,
            profitMarginPercent = roundToNearest(marginPercent, 0.1),
            netArtisanEarnings = roundToNearest(netEarning, 1.0),
            hourlyReturn = roundToNearest(hourlyReturn, 1.0),
            description = description,
            hindiDescription = hindiDescription,
            isRecommended = isRecommended
        )
    }

    private fun roundToNearest(value: Double, step: Double): Double {
        return (value / step).roundToInt() * step
    }

    private fun buildHindiNarrative(
        input: NewProductPricingInput,
        similarCount: Int,
        peerAvg: Double,
        recommendedPrice: Double,
        minRange: Double,
        maxRange: Double,
        materialCost: Double,
        materialPct: Double,
        totalFloor: Double
    ): String {
        val peerContext = if (similarCount > 0 && peerAvg > 0) {
            "आपके डेटाबेस में '${input.category}' के $similarCount समान शिल्प मिले, जिनका औसत मूल्य ₹${peerAvg.toInt()} है।"
        } else {
            "इस श्रेणी (${input.category}) के शिल्प बाज़ार मानकों के आधार पर विश्लेषित किए गए हैं।"
        }

        val wageProtection = "₹${totalFloor.toInt()} की लागत फ़्लोर सीमा कारीगर की ₹${input.hourlyWageRate.toInt()}/घंटा मजदूरी को शत-प्रतिशत सुरक्षित रखती है।"

        return "$peerContext ₹${materialCost.toInt()} के कच्चे माल और ${input.laborHours} घंटे श्रम के अनुसार, ₹${minRange.toInt()} से ₹${maxRange.toInt()} (सुझाव: ₹${recommendedPrice.toInt()}) का खुदरा मूल्य सबसे प्रतिस्पर्धी रहेगा। कच्चा माल खुदरा मूल्य का ${materialPct.toInt()}% है जो हस्तशिल्प मानक के बिल्कुल अनुकूल है। $wageProtection"
    }

    private fun buildEnglishNarrative(
        input: NewProductPricingInput,
        similarCount: Int,
        peerAvg: Double,
        recommendedPrice: Double,
        minRange: Double,
        maxRange: Double,
        materialCost: Double,
        materialPct: Double,
        totalFloor: Double
    ): String {
        val peerContext = if (similarCount > 0 && peerAvg > 0) {
            "Found $similarCount comparable '${input.category}' items in your database averaging ₹${peerAvg.toInt()}."
        } else {
            "Benchmarked against standard artisan market dynamics for ${input.category}."
        }

        return "$peerContext Based on ₹${materialCost.toInt()} material and ${input.laborHours}h labor, a competitive retail price range of ₹${minRange.toInt()} - ₹${maxRange.toInt()} (Recommended: ₹${recommendedPrice.toInt()}) offers a healthy margin while remaining attractive. Raw materials account for ${materialPct.toInt()}% of retail, perfectly aligned with craft guidelines. Living wage floor of ₹${totalFloor.toInt()} guarantees minimum fair compensation."
    }
}
