package com.example

import com.example.data.db.ProductDao
import com.example.data.db.ProductHistoryDao
import com.example.data.model.ArtisanAnalyticsDashboardData
import com.example.data.model.ProductEntity
import com.example.data.model.ProductHistoryEntity
import com.example.data.repository.ArtisanAnalyticsRepository
import com.example.data.repository.ArtisanAnalyticsRepositoryImpl
import com.example.ui.components.d3.D3ChartsHtmlGenerator
import com.example.ui.viewmodel.AnalyticsTimeFilter
import com.example.ui.viewmodel.ArtisanSalesAnalyticsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ArtisanSalesAnalyticsD3Test {

    // Test Fake for ProductHistoryDao
    private class FakeProductHistoryDao(
        private val historyList: List<ProductHistoryEntity> = emptyList()
    ) : ProductHistoryDao {
        override fun getAllHistory(): Flow<List<ProductHistoryEntity>> = flowOf(historyList)
        override fun getHistoryForProduct(productId: Long): Flow<List<ProductHistoryEntity>> =
            flowOf(historyList.filter { it.productId == productId })
        override suspend fun getHistoryForProductSync(productId: Long): List<ProductHistoryEntity> =
            historyList.filter { it.productId == productId }
        override fun getHistoryByCategory(category: String): Flow<List<ProductHistoryEntity>> =
            flowOf(historyList.filter { it.category == category })
        override suspend fun getHistoryByCategorySync(category: String): List<ProductHistoryEntity> =
            historyList.filter { it.category == category }
        override suspend fun getAllHistorySync(): List<ProductHistoryEntity> = historyList
        override suspend fun getAverageMaterialCost(productId: Long): Double? =
            historyList.filter { it.productId == productId }.map { it.materialCost }.average().takeIf { !it.isNaN() }
        override suspend fun getAverageSellingPrice(productId: Long): Double? =
            historyList.filter { it.productId == productId }.map { it.sellingPrice }.average().takeIf { !it.isNaN() }
        override suspend fun getTotalUnitsSold(productId: Long): Int? =
            historyList.filter { it.productId == productId }.sumOf { it.unitsSold }
        override suspend fun getMinSellingPrice(productId: Long): Double? =
            historyList.filter { it.productId == productId }.minOfOrNull { it.sellingPrice }
        override suspend fun getMaxSellingPrice(productId: Long): Double? =
            historyList.filter { it.productId == productId }.maxOfOrNull { it.sellingPrice }
        override suspend fun insertHistory(history: ProductHistoryEntity): Long = 1L
        override suspend fun insertHistories(histories: List<ProductHistoryEntity>) {}
        override suspend fun deleteHistoryById(id: Long) {}
        override suspend fun deleteHistoryForProduct(productId: Long) {}
        override suspend fun getHistoryCount(): Int = historyList.size
    }

    // Test Fake for ProductDao
    private class FakeProductDao(
        private val productList: List<ProductEntity> = emptyList()
    ) : ProductDao {
        override fun getAllProducts(): Flow<List<ProductEntity>> = flowOf(productList)
        override suspend fun getAllProductsList(): List<ProductEntity> = productList
        override suspend fun getProductsModifiedSince(since: Long): List<ProductEntity> =
            productList.filter { it.timestamp > since }
        override fun getProductsByStatus(status: String): Flow<List<ProductEntity>> =
            flowOf(productList.filter { it.status == status })
        override suspend fun getProductById(id: Long): ProductEntity? = productList.find { it.id == id }
        override fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
            flowOf(productList.filter { it.category == category })
        override suspend fun insertProduct(product: ProductEntity): Long = 1L
        override suspend fun insertProducts(products: List<ProductEntity>) {}
        override suspend fun updateProduct(product: ProductEntity) {}
        override suspend fun deleteProductById(id: Long) {}
        override fun getProductCount(): Flow<Int> = flowOf(productList.size)
        override suspend fun incrementInquiryCount(id: Long) {}
    }

    // Test Fake for ArtisanAnalyticsRepository
    private class FakeArtisanAnalyticsRepository(
        private val data: ArtisanAnalyticsDashboardData
    ) : ArtisanAnalyticsRepository {
        override val analyticsData: Flow<ArtisanAnalyticsDashboardData> = flowOf(data)
        override suspend fun getAnalyticsDataSync(): ArtisanAnalyticsDashboardData = data
    }

    @Test
    fun testD3ChartsHtmlGenerator_generatesValidHtmlWithAllCharts() = runBlocking {
        val repository = ArtisanAnalyticsRepositoryImpl(
            historyDao = FakeProductHistoryDao(),
            productDao = FakeProductDao()
        )

        val seedData = repository.getAnalyticsDataSync()
        val html = D3ChartsHtmlGenerator.generateHtml(seedData, isDarkMode = false)

        assertNotNull(html)
        assertTrue("Must include d3.v7 script reference", html.contains("d3.v7.min.js"))
        assertTrue("Must contain monthly sales performance chart", html.contains("monthlySalesSvg"))
        assertTrue("Must contain category distribution donut chart", html.contains("categorySvg"))
        assertTrue("Must contain pricing trends chart", html.contains("pricingTrendsSvg"))
        assertTrue("Must contain living wage shaded area", html.contains("fairWageGradient"))
        assertTrue("Must contain Android bridge interaction hook", html.contains("window.AndroidBridge"))
    }

    @Test
    fun testArtisanAnalyticsRepository_aggregatesDataCorrectly() = runBlocking {
        val now = System.currentTimeMillis()
        val day = 86400000L

        val mockHistories = listOf(
            ProductHistoryEntity(
                id = 1,
                productId = 10,
                productTitle = "Handcrafted Clay Vase",
                category = "Pottery",
                recordedDate = now - day * 60,
                materialCost = 120.0,
                laborHours = 5.0,
                hourlyWageRate = 180.0,
                packagingCost = 40.0,
                sellingPrice = 1450.0,
                unitsSold = 15,
                profitMarginPercent = 28.5
            ),
            ProductHistoryEntity(
                id = 2,
                productId = 20,
                productTitle = "Katan Silk Dupatta",
                category = "Handloom & Textiles",
                recordedDate = now - day * 10,
                materialCost = 2500.0,
                laborHours = 40.0,
                hourlyWageRate = 200.0,
                packagingCost = 120.0,
                sellingPrice = 13500.0,
                unitsSold = 8,
                profitMarginPercent = 24.0
            )
        )

        val mockProducts = listOf(
            ProductEntity(
                id = 10,
                title = "Handcrafted Clay Vase",
                description = "Decorative terracotta pottery vase",
                category = "Pottery",
                craftType = "Terracotta",
                region = "Gorakhpur",
                retailPrice = 1450.0,
                wholesalePrice = 1100.0,
                rawMaterialCost = 120.0,
                laborHours = 5.0,
                hourlyWageRate = 180.0
            ),
            ProductEntity(
                id = 20,
                title = "Katan Silk Dupatta",
                description = "Handwoven pure silk dupatta",
                category = "Handloom & Textiles",
                craftType = "Banarasi Silk",
                region = "Varanasi",
                retailPrice = 13500.0,
                wholesalePrice = 11500.0,
                rawMaterialCost = 2500.0,
                laborHours = 40.0,
                hourlyWageRate = 200.0
            )
        )

        val repository = ArtisanAnalyticsRepositoryImpl(
            historyDao = FakeProductHistoryDao(mockHistories),
            productDao = FakeProductDao(mockProducts)
        )
        val data: ArtisanAnalyticsDashboardData = repository.analyticsData.first()

        assertEquals(23, data.summaryMetrics.totalUnitsSold)
        assertTrue("Total revenue should reflect units sold * selling price", data.summaryMetrics.totalGrossRevenue > 100000.0)
        assertEquals(2, data.categoryDistribution.size)
        assertEquals(2, data.pricingTrends.size)

        // Verify living wage protection calculation
        val vaseTrend = data.pricingTrends.find { it.productId == 10L }!!
        assertEquals(1060.0, vaseTrend.fairFloorPrice, 0.01) // 120 + 5*180 + 40 = 1060
        assertTrue("Selling price 1450 >= fair floor 1060", vaseTrend.isAboveLivingWage)
    }

    @Test
    fun testArtisanSalesAnalyticsViewModel_filteringAndExportReport() = runBlocking {
        val seedData = ArtisanAnalyticsRepositoryImpl(
            historyDao = FakeProductHistoryDao(),
            productDao = FakeProductDao()
        ).getAnalyticsDataSync()

        val repository = FakeArtisanAnalyticsRepository(seedData)
        val viewModel = ArtisanSalesAnalyticsViewModel(repository)
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(AnalyticsTimeFilter.ALL, state.selectedTimeFilter)
        assertTrue(state.data.monthlySales.isNotEmpty())
        assertTrue(state.aiNarrativeSummary.isNotEmpty())

        val exportText = viewModel.getExportableSummaryText()
        assertTrue(exportText.contains("हुनरसेतु - शिल्पकार बिक्री व मूल्य रुझान सारांश"))
        assertTrue(exportText.contains("कुल राजस्व"))

        // Test interaction callback
        viewModel.handleChartItemClicked("category", "Pottery", "मिट्टी शिल्प")
        val updatedState = viewModel.uiState.first { it.lastClickedItem != null }
        assertEquals("category: मिट्टी शिल्प", updatedState.lastClickedItem)
    }
}
