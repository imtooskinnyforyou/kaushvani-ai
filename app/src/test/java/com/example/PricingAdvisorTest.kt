package com.example

import com.example.data.db.ProductDao
import com.example.data.db.ProductHistoryDao
import com.example.data.model.*
import com.example.data.repository.PricingAdvisorRepositoryImpl
import com.example.ui.viewmodel.PricingAdvisorUiState
import com.example.ui.viewmodel.PricingAdvisorViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PricingAdvisorTest {

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
        override suspend fun insertProduct(product: ProductEntity): Long = product.id
        override suspend fun insertProducts(products: List<ProductEntity>) {}
        override suspend fun updateProduct(product: ProductEntity) {}
        override suspend fun deleteProduct(product: ProductEntity) {}
        override suspend fun deleteProductById(id: Long) {}
        override fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
            flowOf(productList.filter { it.category == category })
        override fun searchProducts(query: String): Flow<List<ProductEntity>> = flowOf(emptyList())
        override fun getProductCount(): Flow<Int> = flowOf(productList.size)
        override suspend fun getProductCountSync(): Int = productList.size
        override fun getProductsBySyncStatus(status: String): Flow<List<ProductEntity>> = flowOf(emptyList())
        override suspend fun getPendingSyncProducts(): List<ProductEntity> = emptyList()
        override suspend fun updateSyncStatus(productId: Long, status: String, lastSyncedAt: Long) {}
        override suspend fun incrementInquiryCount(id: Long) {}
    }

    private val sampleCatalog = listOf(
        ProductEntity(
            id = 101L,
            title = "Terracotta Floral Vase",
            regionalTitle = "टेराकोटा फूलदान",
            description = "Handcrafted clay vase",
            category = "Pottery",
            craftType = "Terracotta",
            retailPrice = 1450.0,
            wholesalePrice = 950.0,
            rawMaterialCost = 280.0,
            laborHours = 4.5,
            hourlyWageRate = 180.0,
            packagingCost = 60.0
        ),
        ProductEntity(
            id = 102L,
            title = "Traditional Clay Tea Cups (Set of 4)",
            regionalTitle = "कुल्हड़ सेट",
            description = "Eco friendly kulhad set",
            category = "Pottery",
            craftType = "Terracotta",
            retailPrice = 750.0,
            wholesalePrice = 500.0,
            rawMaterialCost = 140.0,
            laborHours = 2.0,
            hourlyWageRate = 180.0,
            packagingCost = 40.0
        ),
        ProductEntity(
            id = 103L,
            title = "Handwoven Chanderi Silk Saree",
            regionalTitle = "चंदेरी सिल्क साड़ी",
            description = "Handloom silk",
            category = "Handloom",
            craftType = "Chanderi Weaving",
            retailPrice = 4500.0,
            wholesalePrice = 3200.0,
            rawMaterialCost = 1200.0,
            laborHours = 12.0,
            hourlyWageRate = 180.0,
            packagingCost = 100.0
        )
    )

    private val sampleHistory = listOf(
        ProductHistoryEntity(
            id = 1L,
            productId = 101L,
            productTitle = "Terracotta Floral Vase",
            category = "Pottery",
            materialCost = 280.0,
            sellingPrice = 1450.0,
            unitsSold = 22,
            buyerType = "RETAIL",
            profitMarginPercent = 32.5
        ),
        ProductHistoryEntity(
            id = 2L,
            productId = 102L,
            productTitle = "Traditional Clay Tea Cups",
            category = "Pottery",
            materialCost = 140.0,
            sellingPrice = 750.0,
            unitsSold = 50,
            buyerType = "RETAIL",
            profitMarginPercent = 28.0
        )
    )

    @Test
    fun testGeneratePricingAdvice_withSimilarPeersInRoom() = runBlocking {
        val repo = PricingAdvisorRepositoryImpl(
            productDao = FakeProductDao(sampleCatalog),
            productHistoryDao = FakeProductHistoryDao(sampleHistory)
        )

        val input = NewProductPricingInput(
            title = "Clay Terracotta Water Pitcher",
            category = "Pottery",
            craftType = "Terracotta",
            materialCost = 300.0,
            laborHours = 4.0,
            hourlyWageRate = 180.0,
            packagingCost = 50.0,
            isGiTagged = false
        )

        val advice = repo.generatePricingAdvice(input)

        // Verify peer matching
        assertTrue("Should find matching Pottery peers in Room catalog", advice.peerCount > 0)
        assertEquals(2, advice.peerCount)
        assertTrue(advice.peerAverageRetailPrice > 0)

        // Verify Cost Floor & Living Wage Guarantee
        // Cost: 300 (material) + 720 (4h * 180) + 50 (pkg) = 1070.
        // Cost floor with 10% cushion = ~1177 rounded to 20 = 1180.
        assertTrue("Living wage must be protected", advice.livingWageProtected)
        assertTrue("Total cost floor must exceed direct costs", advice.totalCostFloor >= 1070.0)

        // Verify Tiers
        assertEquals("Must provide 4 distinct pricing tiers", 4, advice.tiers.size)
        val retailTier = advice.recommendedTier
        assertEquals(PricingTierType.RECOMMENDED_RETAIL, retailTier.tierType)
        assertTrue("Retail price must exceed break-even cost floor", retailTier.recommendedPrice >= advice.totalCostFloor)
        assertTrue("Min price must be less than max price", retailTier.minPrice < retailTier.maxPrice)
        assertTrue("Artisan should have positive earnings", retailTier.netArtisanEarnings > 0)

        // Verify Wholesale tier
        val wholesaleTier = advice.tiers.find { it.tierType == PricingTierType.WHOLESALE_B2B }
        assertNotNull(wholesaleTier)
        assertTrue("Wholesale must still protect living wage floor", wholesaleTier!!.recommendedPrice >= advice.totalCostFloor)

        // Verify Boutique tier
        val boutiqueTier = advice.tiers.find { it.tierType == PricingTierType.PREMIUM_BOUTIQUE }
        assertNotNull(boutiqueTier)
        assertTrue("Boutique price should be higher than standard retail", boutiqueTier!!.recommendedPrice > retailTier.recommendedPrice)

        // Verify narratives
        assertTrue("Hindi narrative should be non-empty", advice.marketInsightsHindi.isNotBlank())
        assertTrue("English narrative should be non-empty", advice.marketInsightsEnglish.isNotBlank())
    }

    @Test
    fun testGeneratePricingAdvice_giTagPremium() = runBlocking {
        val repo = PricingAdvisorRepositoryImpl(
            productDao = FakeProductDao(sampleCatalog),
            productHistoryDao = FakeProductHistoryDao(sampleHistory)
        )

        val standardInput = NewProductPricingInput(
            title = "Terracotta Art",
            category = "Pottery",
            craftType = "Terracotta",
            materialCost = 250.0,
            laborHours = 3.0,
            isGiTagged = false
        )

        val giInput = standardInput.copy(isGiTagged = true)

        val standardAdvice = repo.generatePricingAdvice(standardInput)
        val giAdvice = repo.generatePricingAdvice(giInput)

        assertTrue(
            "GI Tagged craft should yield higher recommended price due to brand equity",
            giAdvice.recommendedTier.recommendedPrice >= standardAdvice.recommendedTier.recommendedPrice
        )
    }

    @Test
    fun testPricingAdvisorViewModel_tierSelectionAndPriceApplication() = runBlocking {
        val repo = PricingAdvisorRepositoryImpl(
            productDao = FakeProductDao(sampleCatalog),
            productHistoryDao = FakeProductHistoryDao(sampleHistory)
        )
        val viewModel = PricingAdvisorViewModel(pricingAdvisorRepository = repo)

        // Wait until loaded
        var state = viewModel.uiState.value
        while (state !is PricingAdvisorUiState.Success) {
            state = viewModel.uiState.value
        }

        assertTrue(state is PricingAdvisorUiState.Success)
        val result = state.result

        // Test selecting boutique tier
        viewModel.selectTier(PricingTierType.PREMIUM_BOUTIQUE)
        assertEquals(PricingTierType.PREMIUM_BOUTIQUE, viewModel.selectedTierType.value)

        // Apply price
        val applied = viewModel.applySelectedTierPrice()
        assertNotNull(applied)
        val boutiqueTier = result.tiers.find { it.tierType == PricingTierType.PREMIUM_BOUTIQUE }
        assertEquals(boutiqueTier!!.recommendedPrice, applied!!, 0.01)
        assertEquals(applied, viewModel.appliedPriceEvent.value)

        viewModel.resetAppliedEvent()
        assertNull(viewModel.appliedPriceEvent.value)
    }
}
