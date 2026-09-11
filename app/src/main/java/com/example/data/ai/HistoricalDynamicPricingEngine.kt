package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToInt

interface HistoricalDynamicPricingEngine {
    suspend fun suggestDynamicPricing(
        product: ProductEntity,
        historicalRecords: List<ProductHistoryEntity>,
        metrics: HistoricalPricingMetrics,
        currentMaterialCost: Double,
        currentLaborHours: Double,
        hourlyWageRate: Double,
        packagingCost: Double,
        season: PricingSeason,
        channel: ChannelType
    ): Result<DynamicPricingRecommendation>
}

class HistoricalDynamicPricingEngineImpl(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) : HistoricalDynamicPricingEngine {

    companion object {
        private const val TAG = "HistDynamicPricing"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
        private const val MODEL_NAME = "gemini-3.5-flash"
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun suggestDynamicPricing(
        product: ProductEntity,
        historicalRecords: List<ProductHistoryEntity>,
        metrics: HistoricalPricingMetrics,
        currentMaterialCost: Double,
        currentLaborHours: Double,
        hourlyWageRate: Double,
        packagingCost: Double,
        season: PricingSeason,
        channel: ChannelType
    ): Result<DynamicPricingRecommendation> = withContext(Dispatchers.IO) {
        // 1. Compute deterministic fair-wage baseline & cost structure
        val directLaborEarning = currentLaborHours * hourlyWageRate
        val totalDirectCost = currentMaterialCost + directLaborEarning + packagingCost

        // Cost floor: Absolute minimum to never compromise the artisan's ₹180/hr living wage
        val fairLivingWageFloorPrice = (totalDirectCost * 1.15).roundToInt().toDouble()

        // Base artisan craft margin
        val baseMargin = if (metrics.averageProfitMarginPercent > 10.0) {
            (metrics.averageProfitMarginPercent / 100.0).coerceIn(0.20, 0.45)
        } else {
            0.30
        }

        // Seasonal demand surge
        val seasonalMultiplier = season.demandMultiplier

        // Material inflation pass-through: if material cost rose > 10%, adjust base upward
        val materialInflationRate = metrics.materialCostInflationPercent
        val materialHedgeBonus = if (materialInflationRate > 10.0) {
            (materialInflationRate / 100.0) * currentMaterialCost * 0.5
        } else 0.0

        // Calculated retail price with seasonality and material adjustments
        val unroundedRetail = ((totalDirectCost + materialHedgeBonus) / (1.0 - baseMargin)) * seasonalMultiplier
        val recommendedRetail = max(fairLivingWageFloorPrice, (unroundedRetail / 50.0).roundToInt() * 50.0)

        // Wholesale price factoring channel discount
        val wholesaleDiscount = channel.wholesaleDiscountRate
        val calculatedWholesale = recommendedRetail * (1.0 - wholesaleDiscount)
        val recommendedWholesale = max(fairLivingWageFloorPrice, (calculatedWholesale / 50.0).roundToInt() * 50.0)

        val profitMarginPercent = if (recommendedRetail > 0) {
            (((recommendedRetail - totalDirectCost) / recommendedRetail) * 100.0).coerceAtLeast(15.0)
        } else 25.0

        val seasonalDiff = recommendedRetail - ((totalDirectCost / (1.0 - baseMargin)))

        // Strategy Tag
        val strategyTag = when {
            season == PricingSeason.DIWALI_FESTIVE -> "त्योहारी मांग वृद्धि (Festive Peak +35%)"
            season == PricingSeason.WEDDING_SEASON -> "विवाह प्रीमियम मूल्य (Bridal Season +40%)"
            materialInflationRate > 15.0 -> "कच्चे माल की हेजिंग (Raw Cost Hedge +${materialInflationRate.roundToInt()}%)"
            season == PricingSeason.OFF_SEASON_CLEARANCE -> "ऑफ-सीजन वॉल्यूम डिस्काउंट (Volume Push -12%)"
            else -> "संतुलित निष्पक्ष मूल्य (Fair Living Wage Balanced)"
        }

        val elasticityNote = if (historicalRecords.isNotEmpty()) {
            val maxSale = historicalRecords.maxByOrNull { it.unitsSold }
            if (maxSale != null) {
                "ऐतिहासिक डेटा: '${maxSale.salePeriod}' में ₹${maxSale.sellingPrice.toInt()} पर सर्वाधिक ${maxSale.unitsSold} इकाइयां बिकीं।"
            } else {
                "संतुलित बिक्री गति दर्ज की गई है।"
            }
        } else {
            "नया शिल्प: मानक बाजार दरों के आधार पर गणना की गई है।"
        }

        // Try Gemini AI for rich economic reasoning
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = buildGeminiPrompt(
                    product = product,
                    metrics = metrics,
                    currentMaterialCost = currentMaterialCost,
                    totalDirectCost = totalDirectCost,
                    recommendedRetail = recommendedRetail,
                    recommendedWholesale = recommendedWholesale,
                    season = season,
                    channel = channel,
                    strategyTag = strategyTag
                )

                val responseJson = callGeminiApi(prompt, apiKey)
                val aiJustification = parseGeminiResponse(responseJson)

                return@withContext Result.success(
                    DynamicPricingRecommendation(
                        productId = product.id,
                        productTitle = product.title,
                        recommendedRetailPrice = recommendedRetail,
                        recommendedWholesalePrice = recommendedWholesale,
                        fairLivingWageFloorPrice = fairLivingWageFloorPrice,
                        currentProductionCost = totalDirectCost,
                        estimatedProfitMarginPercent = profitMarginPercent,
                        materialInflationImpactPercent = materialInflationRate,
                        seasonalAdjustmentAmount = seasonalDiff,
                        pricingStrategyTag = strategyTag,
                        historicalElasticityNote = elasticityNote,
                        justificationEnglish = aiJustification.first,
                        justificationHindi = aiJustification.second,
                        confidenceScore = 0.94f
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call failed or timed out, using domain fallback: ${e.message}")
            }
        }

        // Domain-grounded fallback reasoning
        val fallbackEnglish = "Based on historical sales of ${metrics.totalUnitsSold} units and current raw material cost of ₹${currentMaterialCost.toInt()} (${if (materialInflationRate >= 0) "+${materialInflationRate.roundToInt()}%" else "${materialInflationRate.roundToInt()}%"} vs historical average), recommended retail is ₹${recommendedRetail.toInt()} to guarantee fair living wage of ₹${hourlyWageRate.toInt()}/hr under ${season.displayName} demand."
        val fallbackHindi = "ऐतिहासिक ${metrics.totalUnitsSold} इकाइयों की बिक्री और वर्तमान कच्चा माल लागत ₹${currentMaterialCost.toInt()} (औसत से ${if (materialInflationRate >= 0) "+${materialInflationRate.roundToInt()}%" else "${materialInflationRate.roundToInt()}%"}) को ध्यान में रखते हुए, ${season.hindiName} में कारीगर की ₹${hourlyWageRate.toInt()}/घंटे की उचित मजदूरी सुरक्षित रखने के लिए खुदरा मूल्य ₹${recommendedRetail.toInt()} और थोक मूल्य ₹${recommendedWholesale.toInt()} का सुझाव दिया गया है।"

        Result.success(
            DynamicPricingRecommendation(
                productId = product.id,
                productTitle = product.title,
                recommendedRetailPrice = recommendedRetail,
                recommendedWholesalePrice = recommendedWholesale,
                fairLivingWageFloorPrice = fairLivingWageFloorPrice,
                currentProductionCost = totalDirectCost,
                estimatedProfitMarginPercent = profitMarginPercent,
                materialInflationImpactPercent = materialInflationRate,
                seasonalAdjustmentAmount = seasonalDiff,
                pricingStrategyTag = strategyTag,
                historicalElasticityNote = elasticityNote,
                justificationEnglish = fallbackEnglish,
                justificationHindi = fallbackHindi,
                confidenceScore = 0.92f
            )
        )
    }

    private fun buildGeminiPrompt(
        product: ProductEntity,
        metrics: HistoricalPricingMetrics,
        currentMaterialCost: Double,
        totalDirectCost: Double,
        recommendedRetail: Double,
        recommendedWholesale: Double,
        season: PricingSeason,
        channel: ChannelType,
        strategyTag: String
    ): String {
        return """
            You are KAUSHVANI AI Dynamic Pricing Economist for Indian handicrafts.
            Generate a brief, clear 2-sentence rationale in English and a 2-sentence explanation in Hindi explaining why this dynamic price is optimal.
            
            Product: ${product.title} (${product.category}, ${product.craftType})
            Current Raw Material Cost: ₹${currentMaterialCost.toInt()} (Historical Average: ₹${metrics.averageMaterialCost.toInt()}, Inflation: ${metrics.materialCostInflationPercent.roundToInt()}%)
            Historical Units Sold: ${metrics.totalUnitsSold} units across ${metrics.totalRecords} sales records (Avg Selling Price: ₹${metrics.averageSellingPrice.toInt()})
            Current Direct Production Cost: ₹${totalDirectCost.toInt()}
            Target Market / Channel: ${channel.channelName}
            Market Season: ${season.displayName} (Multiplier: ${season.demandMultiplier}x)
            Calculated Retail Price: ₹${recommendedRetail.toInt()}
            Calculated Wholesale Price: ₹${recommendedWholesale.toInt()}
            Strategy: $strategyTag
            
            Respond ONLY in valid JSON with keys:
            {
              "justification_en": "concise 2 sentences in English",
              "justification_hi": "concise 2 sentences in Hindi (Devanagari script)"
            }
        """.trimIndent()
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val url = "$BASE_URL$MODEL_NAME:generateContent?key=$apiKey"
        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.3)
            })
        }

        val body = requestBodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Gemini API returned code ${response.code}")
            }
            val responseString = response.body?.string() ?: throw IllegalStateException("Empty response body")
            val root = JSONObject(responseString)
            val candidates = root.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            return parts.getJSONObject(0).getString("text")
        }
    }

    private fun parseGeminiResponse(jsonText: String): Pair<String, String> {
        val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(cleanJson)
        val en = obj.optString("justification_en", "")
        val hi = obj.optString("justification_hi", "")
        return Pair(en, hi)
    }
}
