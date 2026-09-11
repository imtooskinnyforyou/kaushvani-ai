package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.HistoricalDynamicPricingEngine
import com.example.data.db.ProductDao
import com.example.data.model.*
import com.example.data.repository.ProductHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Dedicated ViewModel to track product history (historical sales, pricing trends, and material costs),
 * and compute AI dynamic pricing grounded in historical data, seasonal surges, and fair living wage guarantees.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductHistoryPricingViewModel(
    private val productHistoryRepository: ProductHistoryRepository,
    private val productDao: ProductDao,
    private val pricingEngine: HistoricalDynamicPricingEngine
) : ViewModel() {

    // All registered artisan products
    val allProducts: StateFlow<List<ProductEntity>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Product
    val selectedProductId = MutableStateFlow(1L)

    val selectedProduct: StateFlow<ProductEntity?> = combine(allProducts, selectedProductId) { products, id ->
        products.find { it.id == id } ?: products.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Historical Records for the active product (reactive Room Flow)
    val productHistoryList: StateFlow<List<ProductHistoryEntity>> = selectedProductId
        .flatMapLatest { id -> productHistoryRepository.getHistoryForProduct(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aggregated Metrics
    val historicalMetrics = MutableStateFlow(HistoricalPricingMetrics())

    // Editable Dynamic Pricing Parameters
    val currentMaterialCostInput = MutableStateFlow(140.0)
    val currentLaborHoursInput = MutableStateFlow(6.5)
    val currentHourlyWageInput = MutableStateFlow(180.0)
    val currentPackagingCostInput = MutableStateFlow(60.0)
    val selectedSeason = MutableStateFlow(PricingSeason.NORMAL)
    val selectedChannel = MutableStateFlow(ChannelType.DIRECT_CRAFT_FAIR)

    // AI Pricing Calculation State
    val isAiAnalyzing = MutableStateFlow(false)
    val pricingRecommendation = MutableStateFlow<DynamicPricingRecommendation?>(null)
    val actionNotification = MutableStateFlow<String?>("")

    init {
        // Automatically sync inputs and calculate metrics whenever product or history changes
        viewModelScope.launch {
            selectedProduct.filterNotNull().collect { product ->
                currentMaterialCostInput.value = product.rawMaterialCost
                currentLaborHoursInput.value = product.laborHours
                currentHourlyWageInput.value = if (product.hourlyWageRate > 0) product.hourlyWageRate else 180.0
                currentPackagingCostInput.value = if (product.packagingCost > 0) product.packagingCost else 50.0

                refreshMetricsAndPricing()
            }
        }

        viewModelScope.launch {
            productHistoryList.collect {
                refreshMetricsAndPricing()
            }
        }
    }

    fun selectProduct(productId: Long) {
        selectedProductId.value = productId
    }

    fun updateMaterialCost(cost: Double) {
        currentMaterialCostInput.value = cost.coerceAtLeast(0.0)
        refreshMetricsAndPricing()
    }

    fun updateLaborHours(hours: Double) {
        currentLaborHoursInput.value = hours.coerceAtLeast(0.5)
        refreshMetricsAndPricing()
    }

    fun updateHourlyWage(wage: Double) {
        currentHourlyWageInput.value = wage.coerceAtLeast(100.0)
        refreshMetricsAndPricing()
    }

    fun updatePackagingCost(packaging: Double) {
        currentPackagingCostInput.value = packaging.coerceAtLeast(0.0)
        refreshMetricsAndPricing()
    }

    fun updateSeason(season: PricingSeason) {
        selectedSeason.value = season
        calculateDynamicPricing()
    }

    fun updateChannel(channel: ChannelType) {
        selectedChannel.value = channel
        calculateDynamicPricing()
    }

    fun refreshMetricsAndPricing() {
        val prodId = selectedProductId.value
        val cost = currentMaterialCostInput.value

        viewModelScope.launch {
            val metrics = productHistoryRepository.calculateMetricsForProduct(prodId, cost)
            historicalMetrics.value = metrics
            calculateDynamicPricing()
        }
    }

    /**
     * Executes AI Dynamic Pricing based on historical sales data, current material costs,
     * seasonal demand surge, and fair wage floor rules.
     */
    fun calculateDynamicPricing() {
        val product = selectedProduct.value ?: return
        viewModelScope.launch {
            isAiAnalyzing.value = true
            try {
                val result = pricingEngine.suggestDynamicPricing(
                    product = product,
                    historicalRecords = productHistoryList.value,
                    metrics = historicalMetrics.value,
                    currentMaterialCost = currentMaterialCostInput.value,
                    currentLaborHours = currentLaborHoursInput.value,
                    hourlyWageRate = currentHourlyWageInput.value,
                    packagingCost = currentPackagingCostInput.value,
                    season = selectedSeason.value,
                    channel = selectedChannel.value
                )

                result.onSuccess { recommendation ->
                    pricingRecommendation.value = recommendation
                }.onFailure { err ->
                    actionNotification.value = "AI मूल्य निर्धारण गणना में त्रुटि: ${err.message}"
                }
            } finally {
                isAiAnalyzing.value = false
            }
        }
    }

    fun applyDynamicPricingToProduct() {
        applyPricingToProduct()
    }

    /**
     * Applies the AI suggested dynamic prices directly to the Product entity in Room DB
     * and logs a new history snapshot record.
     */
    fun applyPricingToProduct() {
        val product = selectedProduct.value ?: return
        val recommendation = pricingRecommendation.value ?: return

        viewModelScope.launch {
            try {
                // 1. Update Product in Room
                val updatedProduct = product.copy(
                    retailPrice = recommendation.recommendedRetailPrice,
                    wholesalePrice = recommendation.recommendedWholesalePrice,
                    fairMinPrice = recommendation.fairLivingWageFloorPrice,
                    rawMaterialCost = currentMaterialCostInput.value,
                    laborHours = currentLaborHoursInput.value,
                    hourlyWageRate = currentHourlyWageInput.value,
                    packagingCost = currentPackagingCostInput.value,
                    timestamp = System.currentTimeMillis()
                )
                productDao.updateProduct(updatedProduct)

                // 2. Insert new history audit record
                val newHistory = ProductHistoryEntity(
                    productId = product.id,
                    productTitle = product.title,
                    category = product.category,
                    recordedDate = System.currentTimeMillis(),
                    salePeriod = "${selectedSeason.value.displayName} (AI Applied)",
                    materialCost = currentMaterialCostInput.value,
                    laborHours = currentLaborHoursInput.value,
                    hourlyWageRate = currentHourlyWageInput.value,
                    packagingCost = currentPackagingCostInput.value,
                    sellingPrice = recommendation.recommendedRetailPrice,
                    suggestedRetailPrice = recommendation.recommendedRetailPrice,
                    unitsSold = 1,
                    unitsInStock = product.stockAvailable,
                    salesChannel = selectedChannel.value.channelName,
                    demandIndex = selectedSeason.value.demandMultiplier,
                    competitorMarketAverage = recommendation.recommendedRetailPrice * 1.08,
                    profitMarginPercent = recommendation.estimatedProfitMarginPercent,
                    notes = "AI गतिशीलन: ${recommendation.pricingStrategyTag}"
                )
                productHistoryRepository.insertHistory(newHistory)

                actionNotification.value = "✓ नया मूल्य सफलतापूर्वक लागू किया गया (खुदरा ₹${recommendation.recommendedRetailPrice.toInt()}, थोक ₹${recommendation.recommendedWholesalePrice.toInt()})"
            } catch (e: Exception) {
                actionNotification.value = "त्रुटि: ${e.message}"
            }
        }
    }

    /**
     * Records a new transaction/sale into the Room database product history.
     */
    fun recordNewSale(
        unitsSold: Int,
        sellingPrice: Double,
        channel: String,
        notes: String = ""
    ) {
        val product = selectedProduct.value ?: return
        viewModelScope.launch {
            try {
                val directCost = currentMaterialCostInput.value + (currentLaborHoursInput.value * currentHourlyWageInput.value) + currentPackagingCostInput.value
                val margin = if (sellingPrice > directCost) {
                    ((sellingPrice - directCost) / sellingPrice) * 100.0
                } else 15.0

                val record = ProductHistoryEntity(
                    productId = product.id,
                    productTitle = product.title,
                    category = product.category,
                    recordedDate = System.currentTimeMillis(),
                    salePeriod = "${selectedSeason.value.displayName} Record",
                    materialCost = currentMaterialCostInput.value,
                    laborHours = currentLaborHoursInput.value,
                    hourlyWageRate = currentHourlyWageInput.value,
                    packagingCost = currentPackagingCostInput.value,
                    sellingPrice = sellingPrice,
                    suggestedRetailPrice = pricingRecommendation.value?.recommendedRetailPrice ?: sellingPrice,
                    unitsSold = unitsSold,
                    unitsInStock = (product.stockAvailable - unitsSold).coerceAtLeast(0),
                    salesChannel = channel,
                    demandIndex = selectedSeason.value.demandMultiplier,
                    competitorMarketAverage = sellingPrice * 1.05,
                    profitMarginPercent = margin,
                    notes = notes
                )

                productHistoryRepository.insertHistory(record)
                actionNotification.value = "✓ नई बिक्री दर्ज की गई: $unitsSold इकाइयाँ @ ₹${sellingPrice.toInt()}"
            } catch (e: Exception) {
                actionNotification.value = "बिक्री दर्ज करने में त्रुटि: ${e.message}"
            }
        }
    }

    fun dismissNotification() {
        actionNotification.value = null
    }
}
