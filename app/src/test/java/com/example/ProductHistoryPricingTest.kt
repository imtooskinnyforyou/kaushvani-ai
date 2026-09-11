package com.example

import com.example.data.ai.HistoricalDynamicPricingEngineImpl
import com.example.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ProductHistoryPricingTest {

    @Test
    fun testHistoricalMetricsCalculation() {
        val historyRecords = listOf(
            ProductHistoryEntity(
                productId = 1L,
                productTitle = "Terracotta Water Pitcher",
                category = "Pottery",
                sellingPrice = 1200.0,
                materialCost = 100.0,
                laborHours = 5.0,
                unitsSold = 20,
                salesChannel = "Direct Craft Fair",
                salePeriod = "March 2024"
            ),
            ProductHistoryEntity(
                productId = 1L,
                productTitle = "Terracotta Water Pitcher",
                category = "Pottery",
                sellingPrice = 1600.0,
                materialCost = 120.0,
                laborHours = 5.0,
                unitsSold = 40,
                salesChannel = "Direct Craft Fair",
                salePeriod = "October 2024"
            )
        )

        val totalUnits = historyRecords.sumOf { it.unitsSold }
        assertEquals(60, totalUnits)

        val avgSellingPrice = historyRecords.sumOf { it.sellingPrice * it.unitsSold } / totalUnits
        assertTrue(avgSellingPrice > 1400.0 && avgSellingPrice < 1500.0)

        val historicalAvgCost = historyRecords.map { it.materialCost }.average()
        val currentCost = 150.0
        val inflation = ((currentCost - historicalAvgCost) / historicalAvgCost) * 100.0
        assertTrue("Inflation should be positive", inflation > 0.0)
    }

    @Test
    fun testAiEngineLivingWageFloorProtection() = runBlocking {
        val engine = HistoricalDynamicPricingEngineImpl()
        val product = ProductEntity(
            id = 1L,
            title = "Terracotta Water Pitcher",
            description = "Handcrafted clay pitcher by master artisans",
            category = "Pottery",
            craftType = "Terracotta Earthenware",
            region = "Gorakhpur",
            retailPrice = 1400.0,
            wholesalePrice = 1000.0,
            rawMaterialCost = 150.0,
            laborHours = 6.0,
            hourlyWageRate = 180.0
        )

        val metrics = HistoricalPricingMetrics(
            productId = 1L,
            totalUnitsSold = 60,
            averageSellingPrice = 1466.0,
            averageMaterialCost = 110.0,
            materialCostInflationPercent = 36.3,
            highestDemandSeason = "Festive",
            elasticitySummary = "Resilient pricing",
            averageProfitMarginPercent = 25.0
        )

        val recommendationResult = engine.suggestDynamicPricing(
            product = product,
            historicalRecords = emptyList(),
            metrics = metrics,
            currentMaterialCost = 150.0,
            currentLaborHours = 6.0,
            hourlyWageRate = 180.0,
            packagingCost = 50.0,
            season = PricingSeason.DIWALI_FESTIVE,
            channel = ChannelType.DIRECT_CRAFT_FAIR
        )

        assertTrue(recommendationResult.isSuccess)
        val recommendation = recommendationResult.getOrThrow()
        assertNotNull(recommendation)
        // Living wage floor must protect 6 hours * 180 = 1080 labor + 150 material + 50 packaging * 1.15
        assertTrue("Fair wage floor must protect artisan livelihood", recommendation.fairLivingWageFloorPrice >= 1230.0)
        // Recommended retail price must be higher than wage floor
        assertTrue("Retail price must be greater than or equal to wage floor", recommendation.recommendedRetailPrice >= recommendation.fairLivingWageFloorPrice)
        // Recommended retail should have festive boost
        assertTrue("Festive season retail price should be substantial", recommendation.recommendedRetailPrice > 1600.0)
    }
}
