package com.example.data.repository

import android.util.Log
import com.example.data.firebase.FirestoreService
import com.example.data.model.ArtisanProfile
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

/**
 * Detailed real-time product performance metrics calculated dynamically from Firestore products & inquiries.
 */
data class ProductPerformanceItem(
    val productId: Long,
    val title: String,
    val regionalTitle: String = "",
    val category: String,
    val craftType: String,
    val retailPrice: Double,
    val wholesalePrice: Double,
    val stockAvailable: Int,
    val minOrderQuantity: Int,
    val viewCount: Int,
    val inquiryCount: Int,
    val requestedQuantityTotal: Int,
    val completedOrdersCount: Int,
    val revenueGenerated: Double,
    val conversionRatePercent: Double,
    val stockStatus: String, // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
    val imageUri: String = "",
    val imageStyleFilter: String = "Natural Studio",
    val isGiTagged: Boolean = false,
    val status: String = "PUBLISHED",
    val performanceScore: Double = 0.0
)

/**
 * Category-level performance and catalog valuation breakdown.
 */
data class CategoryPerformance(
    val category: String,
    val productCount: Int,
    val publishedCount: Int,
    val totalViews: Int,
    val inquiryCount: Int,
    val totalRevenue: Double,
    val totalStockValue: Double,
    val averagePrice: Double
)

/**
 * Aggregate sales & revenue analytics computed dynamically from accepted/completed inquiries and profile sales.
 */
data class SalesAggregate(
    val totalSalesRevenue: Double = 0.0,
    val inquirySalesRevenue: Double = 0.0,
    val baseProfileRevenue: Double = 0.0,
    val totalSalesCount: Int = 0,
    val acceptedOrdersCount: Int = 0,
    val pendingPipelineRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val totalUnitsSold: Int = 0,
    val wholesaleGmv: Double = 0.0,
    val retailGmv: Double = 0.0
)

/**
 * Inquiry volume & conversion funnel analytics.
 */
data class InquiryAggregate(
    val totalInquiries: Int = 0,
    val newInquiriesCount: Int = 0,
    val activeInquiriesCount: Int = 0,
    val acceptedOrdersCount: Int = 0,
    val rejectedInquiriesCount: Int = 0,
    val conversionRatePercent: Double = 0.0,
    val totalVolumeUnitsRequested: Int = 0,
    val averageUnitsPerInquiry: Double = 0.0,
    val statusBreakdown: Map<String, Int> = emptyMap()
)

/**
 * Aggregated live dashboard metrics model for real-time synchronization between Firestore and UI.
 */
data class DashboardMetrics(
    val totalProducts: Int = 0,
    val publishedProducts: Int = 0,
    val draftProducts: Int = 0,
    val giTaggedProductsCount: Int = 0,
    val lowStockProductsCount: Int = 0,
    val totalViewsCount: Int = 0,
    val estimatedCatalogValue: Double = 0.0,
    val totalInquiries: Int = 0,
    val newInquiriesCount: Int = 0,
    val activeInquiries: Int = 0,
    val acceptedOrdersCount: Int = 0,
    val rejectedInquiriesCount: Int = 0,
    val inquiryConversionRate: Double = 0.0,
    val totalSalesCount: Int = 0,
    val totalSalesRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val pendingPipelineRevenue: Double = 0.0,
    val salesAggregate: SalesAggregate = SalesAggregate(),
    val inquiryAggregate: InquiryAggregate = InquiryAggregate(),
    val categoryBreakdown: Map<String, Int> = emptyMap(),
    val categoryPerformance: List<CategoryPerformance> = emptyList(),
    val productPerformanceList: List<ProductPerformanceItem> = emptyList(),
    val topPerformingProducts: List<ProductPerformanceItem> = emptyList(),
    val recentInquiries: List<BuyerInquiryEntity> = emptyList(),
    val recentProducts: List<ProductEntity> = emptyList(),
    val activeArtisanProfile: ArtisanProfile? = null,
    val isRealtimeSynced: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * DashboardRepository defines the contract for aggregating live multi-collection data
 * (products, buyer inquiries, sales, and artisan profile metrics) from Cloud Firestore and local Room cache.
 */
interface DashboardRepository {
    /**
     * Real-time aggregated StateFlow representing current live metrics across products, inquiries, and sales.
     */
    val dashboardMetrics: StateFlow<DashboardMetrics>

    /**
     * Observe live dashboard metrics as a Flow, optionally filtered by artisan ID.
     */
    fun observeDashboardMetrics(artisanId: String? = null): Flow<DashboardMetrics>

    /**
     * Observe per-product performance analytics in real-time.
     */
    fun observeProductPerformance(artisanId: String? = null): Flow<List<ProductPerformanceItem>>

    /**
     * Observe category-level performance breakdown in real-time.
     */
    fun observeCategoryPerformance(artisanId: String? = null): Flow<List<CategoryPerformance>>

    /**
     * Observe sales aggregate analytics in real-time.
     */
    fun observeSalesAggregate(artisanId: String? = null): Flow<SalesAggregate>

    /**
     * Observe inquiry aggregate and conversion funnel in real-time.
     */
    fun observeInquiryAggregate(artisanId: String? = null): Flow<InquiryAggregate>

    /**
     * Fetch the latest aggregate snapshot from Firestore.
     */
    suspend fun getDashboardSnapshot(artisanId: String? = null): Result<DashboardMetrics>

    /**
     * Fetch the latest product performance snapshot.
     */
    suspend fun getProductPerformanceSnapshot(artisanId: String? = null): Result<List<ProductPerformanceItem>>

    /**
     * Force refresh/synchronization of remote Firestore collections to local cache.
     */
    suspend fun refreshDashboard(): Result<DashboardMetrics>
}

/**
 * Production implementation of [DashboardRepository] that aggregates real-time streams
 * from Cloud Firestore (Products, Inquiries, User Profiles) and local Room persistence.
 */
class DashboardRepositoryImpl(
    private val firestoreService: FirestoreService,
    private val productFirestoreRepository: ProductFirestoreRepository,
    private val inquiryFirestoreRepository: InquiryFirestoreRepository,
    private val userProfileFirestoreRepository: UserProfileFirestoreRepository,
    private val authRepository: AuthRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DashboardRepository {

    companion object {
        private const val TAG = "DashboardRepository"
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Primary real-time aggregated dashboard state flow
    override val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        productFirestoreRepository.observeAllProducts()
            .catch { e ->
                Log.w(TAG, "Error in products stream for dashboard: ${e.message}")
                emit(emptyList())
            },
        firestoreService.observeAllInquiries()
            .catch { e ->
                Log.w(TAG, "Error in inquiries stream for dashboard: ${e.message}")
                emit(emptyList())
            },
        userProfileFirestoreRepository.currentProfileState
    ) { products, inquiries, profile ->
        aggregateMetrics(products, inquiries, profile)
    }.flowOn(ioDispatcher)
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = DashboardMetrics()
        )

    override fun observeDashboardMetrics(artisanId: String?): Flow<DashboardMetrics> {
        val targetProductFlow = if (!artisanId.isNullOrBlank()) {
            productFirestoreRepository.observeProductsByArtisan(artisanId)
        } else {
            productFirestoreRepository.observeAllProducts()
        }

        val targetInquiryFlow = if (!artisanId.isNullOrBlank()) {
            inquiryFirestoreRepository.observeInquiriesForUser(artisanId, isArtisan = true)
        } else {
            firestoreService.observeAllInquiries()
        }

        return combine(
            targetProductFlow.catch { emit(emptyList()) },
            targetInquiryFlow.catch { emit(emptyList()) },
            userProfileFirestoreRepository.currentProfileState
        ) { products, inquiries, profile ->
            aggregateMetrics(products, inquiries, profile)
        }.catch { e ->
            Log.e(TAG, "Error in observeDashboardMetrics: ${e.message}")
            emit(dashboardMetrics.value)
        }.flowOn(ioDispatcher)
    }

    override fun observeProductPerformance(artisanId: String?): Flow<List<ProductPerformanceItem>> {
        return observeDashboardMetrics(artisanId).map { it.productPerformanceList }
    }

    override fun observeCategoryPerformance(artisanId: String?): Flow<List<CategoryPerformance>> {
        return observeDashboardMetrics(artisanId).map { it.categoryPerformance }
    }

    override fun observeSalesAggregate(artisanId: String?): Flow<SalesAggregate> {
        return observeDashboardMetrics(artisanId).map { it.salesAggregate }
    }

    override fun observeInquiryAggregate(artisanId: String?): Flow<InquiryAggregate> {
        return observeDashboardMetrics(artisanId).map { it.inquiryAggregate }
    }

    override suspend fun getDashboardSnapshot(artisanId: String?): Result<DashboardMetrics> = withContext(ioDispatcher) {
        try {
            val current = dashboardMetrics.value
            Result.success(current)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting dashboard snapshot: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductPerformanceSnapshot(artisanId: String?): Result<List<ProductPerformanceItem>> = withContext(ioDispatcher) {
        try {
            val current = dashboardMetrics.value.productPerformanceList
            Result.success(current)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product performance snapshot: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshDashboard(): Result<DashboardMetrics> = withContext(ioDispatcher) {
        try {
            productFirestoreRepository.syncRemoteToLocal()
            val current = dashboardMetrics.value
            Result.success(current)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing dashboard: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun aggregateMetrics(
        products: List<ProductEntity>,
        inquiries: List<BuyerInquiryEntity>,
        profile: ArtisanProfile?
    ): DashboardMetrics {
        val totalProducts = products.size
        val publishedProducts = products.count { it.status.equals("PUBLISHED", ignoreCase = true) }
        val draftProducts = products.count { !it.status.equals("PUBLISHED", ignoreCase = true) }
        val giTaggedCount = products.count { it.isGiTagged }

        // Low stock threshold: stock <= 5 or less than minimum order quantity
        val lowStockCount = products.count { it.stockAvailable <= 5 || (it.minOrderQuantity > 1 && it.stockAvailable < it.minOrderQuantity) }

        // Dynamic calculation of inquiries
        val totalInquiries = inquiries.size
        val newInquiriesCount = inquiries.count { it.status.equals("NEW", ignoreCase = true) }
        val activeInquiries = inquiries.count {
            it.status.uppercase() in listOf("NEW", "RESPONDED", "NEGOTIATING", "QUOTE_SENT", "PENDING")
        }
        val acceptedOrdersCount = inquiries.count {
            it.status.uppercase() in listOf("ACCEPTED", "COMPLETED", "FULFILLED", "SHIPPED")
        }
        val rejectedInquiriesCount = inquiries.count {
            it.status.uppercase() in listOf("REJECTED", "CANCELLED", "DECLINED")
        }

        val totalVolumeUnitsRequested = inquiries.sumOf { it.requestedQuantity }
        val averageUnitsPerInquiry = if (totalInquiries > 0) totalVolumeUnitsRequested.toDouble() / totalInquiries else 0.0
        val conversionRatePercent = if (totalInquiries > 0) (acceptedOrdersCount.toDouble() / totalInquiries) * 100.0 else 0.0
        val statusBreakdown = inquiries.groupBy { it.status.uppercase() }.mapValues { it.value.size }

        val inquiryAggregate = InquiryAggregate(
            totalInquiries = totalInquiries,
            newInquiriesCount = newInquiriesCount,
            activeInquiriesCount = activeInquiries,
            acceptedOrdersCount = acceptedOrdersCount,
            rejectedInquiriesCount = rejectedInquiriesCount,
            conversionRatePercent = conversionRatePercent,
            totalVolumeUnitsRequested = totalVolumeUnitsRequested,
            averageUnitsPerInquiry = averageUnitsPerInquiry,
            statusBreakdown = statusBreakdown
        )

        // Dynamic calculation of sales from accepted and completed wholesale / retail inquiries
        var wholesaleGmv = 0.0
        var retailGmv = 0.0
        var totalUnitsSold = 0

        val acceptedInquiries = inquiries.filter {
            it.status.uppercase() in listOf("ACCEPTED", "COMPLETED", "FULFILLED", "SHIPPED")
        }

        val inquirySalesRevenue = acceptedInquiries.sumOf { inquiry ->
            val unitPrice = if (inquiry.offeredPricePerUnit > 0) inquiry.offeredPricePerUnit else 0.0
            val orderTotal = unitPrice * inquiry.requestedQuantity.coerceAtLeast(1)
            totalUnitsSold += inquiry.requestedQuantity.coerceAtLeast(1)

            val isWholesale = inquiry.buyerType.contains("Wholesale", ignoreCase = true) ||
                              inquiry.buyerType.contains("Exporter", ignoreCase = true) ||
                              inquiry.buyerType.contains("Corporate", ignoreCase = true) ||
                              inquiry.requestedQuantity >= 10

            if (isWholesale) {
                wholesaleGmv += orderTotal
            } else {
                retailGmv += orderTotal
            }
            orderTotal
        }

        // Pending quotation / negotiation pipeline revenue
        val pendingPipelineRevenue = inquiries
            .filter { it.status.uppercase() in listOf("NEW", "RESPONDED", "NEGOTIATING", "QUOTE_SENT", "PENDING") }
            .sumOf { inquiry ->
                val unitPrice = if (inquiry.offeredPricePerUnit > 0) inquiry.offeredPricePerUnit else 0.0
                unitPrice * inquiry.requestedQuantity.coerceAtLeast(1)
            }

        val baseRevenue = profile?.totalRevenue ?: 0.0
        val totalSalesRevenue = baseRevenue + inquirySalesRevenue
        val totalSalesCount = (profile?.totalSalesCount ?: 0) + acceptedOrdersCount

        val averageOrderValue = if (acceptedOrdersCount > 0) {
            inquirySalesRevenue / acceptedOrdersCount
        } else if (totalSalesCount > 0) {
            totalSalesRevenue / totalSalesCount
        } else {
            0.0
        }

        val salesAggregate = SalesAggregate(
            totalSalesRevenue = totalSalesRevenue,
            inquirySalesRevenue = inquirySalesRevenue,
            baseProfileRevenue = baseRevenue,
            totalSalesCount = totalSalesCount,
            acceptedOrdersCount = acceptedOrdersCount,
            pendingPipelineRevenue = pendingPipelineRevenue,
            averageOrderValue = averageOrderValue,
            totalUnitsSold = totalUnitsSold,
            wholesaleGmv = wholesaleGmv,
            retailGmv = retailGmv
        )

        // Dynamic calculation of views and catalog value
        val totalViewsCount = products.sumOf { it.viewCount }
        val estimatedCatalogValue = products.sumOf { product ->
            val price = if (product.wholesalePrice > 0) product.wholesalePrice else product.retailPrice
            price * product.stockAvailable.coerceAtLeast(1)
        }

        // Compute Detailed Product Performance List dynamically
        val productPerformanceList = products.map { product ->
            val productInquiries = inquiries.filter {
                it.productId == product.id || it.productTitle.equals(product.title, ignoreCase = true)
            }
            val productInquiryCount = productInquiries.size
            val requestedQtyTotal = productInquiries.sumOf { it.requestedQuantity }
            val completedOrders = productInquiries.filter {
                it.status.uppercase() in listOf("ACCEPTED", "COMPLETED", "FULFILLED", "SHIPPED")
            }
            val productRevenue = completedOrders.sumOf { inq ->
                val unitP = if (inq.offeredPricePerUnit > 0) inq.offeredPricePerUnit else {
                    if (product.wholesalePrice > 0) product.wholesalePrice else product.retailPrice
                }
                unitP * inq.requestedQuantity.coerceAtLeast(1)
            }
            val convRate = if (productInquiryCount > 0) (completedOrders.size.toDouble() / productInquiryCount) * 100.0 else 0.0

            val stockStatus = when {
                product.stockAvailable <= 0 -> "OUT_OF_STOCK"
                product.stockAvailable <= 5 || (product.minOrderQuantity > 1 && product.stockAvailable < product.minOrderQuantity) -> "LOW_STOCK"
                else -> "IN_STOCK"
            }

            // Weighted Performance Score: Views (1x) + Inquiries (15x) + Revenue (0.01x) + GI Tag boost (20x)
            val score = (product.viewCount * 1.0) + (productInquiryCount * 15.0) + (productRevenue * 0.01) + (if (product.isGiTagged) 20.0 else 0.0)

            ProductPerformanceItem(
                productId = product.id,
                title = product.title,
                regionalTitle = product.regionalTitle,
                category = product.category,
                craftType = product.craftType,
                retailPrice = product.retailPrice,
                wholesalePrice = product.wholesalePrice,
                stockAvailable = product.stockAvailable,
                minOrderQuantity = product.minOrderQuantity,
                viewCount = product.viewCount,
                inquiryCount = productInquiryCount,
                requestedQuantityTotal = requestedQtyTotal,
                completedOrdersCount = completedOrders.size,
                revenueGenerated = productRevenue,
                conversionRatePercent = convRate,
                stockStatus = stockStatus,
                imageUri = product.imageUri,
                imageStyleFilter = product.imageStyleFilter,
                isGiTagged = product.isGiTagged,
                status = product.status,
                performanceScore = score
            )
        }

        // Top 5 Highest Performing Products
        val topPerformingProducts = productPerformanceList
            .sortedByDescending { it.performanceScore }
            .take(5)

        // Category breakdown & Category Performance
        val categoryBreakdown = products
            .groupBy { it.category.ifBlank { "Traditional Crafts" } }
            .mapValues { it.value.size }

        val categoryPerformance = products
            .groupBy { it.category.ifBlank { "Traditional Crafts" } }
            .map { (catName, catProducts) ->
                val catViews = catProducts.sumOf { it.viewCount }
                val catStockVal = catProducts.sumOf { p ->
                    val pPrice = if (p.wholesalePrice > 0) p.wholesalePrice else p.retailPrice
                    pPrice * p.stockAvailable.coerceAtLeast(1)
                }
                val catAvgPrice = if (catProducts.isNotEmpty()) {
                    catProducts.map { if (it.wholesalePrice > 0) it.wholesalePrice else it.retailPrice }.average()
                } else 0.0

                val catInquiries = inquiries.filter { inq ->
                    catProducts.any { cp -> cp.id == inq.productId || cp.title.equals(inq.productTitle, ignoreCase = true) }
                }

                val catRevenue = catInquiries
                    .filter { it.status.uppercase() in listOf("ACCEPTED", "COMPLETED", "FULFILLED", "SHIPPED") }
                    .sumOf { inq -> inq.offeredPricePerUnit * inq.requestedQuantity.coerceAtLeast(1) }

                CategoryPerformance(
                    category = catName,
                    productCount = catProducts.size,
                    publishedCount = catProducts.count { it.status.equals("PUBLISHED", ignoreCase = true) },
                    totalViews = catViews,
                    inquiryCount = catInquiries.size,
                    totalRevenue = catRevenue,
                    totalStockValue = catStockVal,
                    averagePrice = catAvgPrice
                )
            }.sortedByDescending { it.productCount }

        val recentInquiries = inquiries.sortedByDescending { it.timestamp }.take(5)
        val recentProducts = products.sortedByDescending { it.timestamp }.take(5)

        return DashboardMetrics(
            totalProducts = totalProducts,
            publishedProducts = publishedProducts,
            draftProducts = draftProducts,
            giTaggedProductsCount = giTaggedCount,
            lowStockProductsCount = lowStockCount,
            totalViewsCount = totalViewsCount,
            estimatedCatalogValue = estimatedCatalogValue,
            totalInquiries = totalInquiries,
            newInquiriesCount = newInquiriesCount,
            activeInquiries = activeInquiries,
            acceptedOrdersCount = acceptedOrdersCount,
            rejectedInquiriesCount = rejectedInquiriesCount,
            inquiryConversionRate = conversionRatePercent,
            totalSalesCount = totalSalesCount,
            totalSalesRevenue = totalSalesRevenue,
            averageOrderValue = averageOrderValue,
            pendingPipelineRevenue = pendingPipelineRevenue,
            salesAggregate = salesAggregate,
            inquiryAggregate = inquiryAggregate,
            categoryBreakdown = categoryBreakdown,
            categoryPerformance = categoryPerformance,
            productPerformanceList = productPerformanceList,
            topPerformingProducts = topPerformingProducts,
            recentInquiries = recentInquiries,
            recentProducts = recentProducts,
            activeArtisanProfile = profile,
            isRealtimeSynced = true,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }
}

