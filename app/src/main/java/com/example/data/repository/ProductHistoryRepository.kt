package com.example.data.repository

import com.example.data.db.ProductHistoryDao
import com.example.data.model.HistoricalPricingMetrics
import com.example.data.model.ProductHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

interface ProductHistoryRepository {
    val allHistory: Flow<List<ProductHistoryEntity>>
    fun getHistoryForProduct(productId: Long): Flow<List<ProductHistoryEntity>>
    fun getHistoryByCategory(category: String): Flow<List<ProductHistoryEntity>>
    suspend fun insertHistory(history: ProductHistoryEntity): Long
    suspend fun deleteHistory(id: Long)
    suspend fun calculateMetricsForProduct(productId: Long, currentMaterialCost: Double): HistoricalPricingMetrics
}

class ProductHistoryRepositoryImpl(
    private val historyDao: ProductHistoryDao
) : ProductHistoryRepository {

    override val allHistory: Flow<List<ProductHistoryEntity>> = historyDao.getAllHistory()

    override fun getHistoryForProduct(productId: Long): Flow<List<ProductHistoryEntity>> {
        return historyDao.getHistoryForProduct(productId)
    }

    override fun getHistoryByCategory(category: String): Flow<List<ProductHistoryEntity>> {
        return historyDao.getHistoryByCategory(category)
    }

    override suspend fun insertHistory(history: ProductHistoryEntity): Long = withContext(Dispatchers.IO) {
        historyDao.insertHistory(history)
    }

    override suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
    }

    override suspend fun calculateMetricsForProduct(
        productId: Long,
        currentMaterialCost: Double
    ): HistoricalPricingMetrics = withContext(Dispatchers.IO) {
        val records = historyDao.getHistoryForProductSync(productId)
        if (records.isEmpty()) {
            return@withContext HistoricalPricingMetrics(
                productId = productId,
                totalRecords = 0,
                totalUnitsSold = 0,
                averageSellingPrice = 0.0,
                minSellingPrice = 0.0,
                maxSellingPrice = 0.0,
                averageMaterialCost = currentMaterialCost,
                materialCostInflationPercent = 0.0,
                averageProfitMarginPercent = 25.0,
                highestDemandSeason = "Diwali Festive",
                salesVelocityTrend = "NEW",
                elasticitySummary = "No prior historical sales records found for this craft item."
            )
        }

        val totalUnits = records.sumOf { it.unitsSold }
        val avgSellingPrice = records.map { it.sellingPrice }.average()
        val minPrice = records.minOfOrNull { it.sellingPrice } ?: 0.0
        val maxPrice = records.maxOfOrNull { it.sellingPrice } ?: 0.0
        val avgMaterialCost = records.map { it.materialCost }.average()

        val inflationPercent = if (avgMaterialCost > 0.0) {
            ((currentMaterialCost - avgMaterialCost) / avgMaterialCost) * 100.0
        } else {
            0.0
        }

        val avgMargin = records.map { it.profitMarginPercent }.average()
        val peakSeasonRecord = records.maxByOrNull { it.unitsSold }
        val peakSeason = peakSeasonRecord?.salePeriod ?: "Diwali Festive"

        val trend = if (records.size >= 2) {
            val sorted = records.sortedBy { it.recordedDate }
            val firstHalf = sorted.take(sorted.size / 2).map { it.unitsSold }.average()
            val secondHalf = sorted.drop(sorted.size / 2).map { it.unitsSold }.average()
            if (secondHalf > firstHalf * 1.1) "RISING" else if (secondHalf < firstHalf * 0.9) "DECLINING" else "STEADY"
        } else {
            "STEADY"
        }

        val elasticityNote = if (inflationPercent > 10.0) {
            "Raw materials are ${inflationPercent.roundToInt()}% higher than historical avg (₹${avgMaterialCost.roundToInt()}). Upward price correction advised to protect artisan living wage."
        } else if (inflationPercent < -5.0) {
            "Raw material cost is currently below historical average. Opportunity for competitive wholesale volume pricing."
        } else {
            "Raw material costs remain stable within normal historical band (₹${avgMaterialCost.roundToInt()})."
        }

        HistoricalPricingMetrics(
            productId = productId,
            totalRecords = records.size,
            totalUnitsSold = totalUnits,
            averageSellingPrice = avgSellingPrice,
            minSellingPrice = minPrice,
            maxSellingPrice = maxPrice,
            averageMaterialCost = avgMaterialCost,
            materialCostInflationPercent = inflationPercent,
            averageProfitMarginPercent = avgMargin,
            highestDemandSeason = peakSeason,
            salesVelocityTrend = trend,
            elasticitySummary = elasticityNote
        )
    }
}
