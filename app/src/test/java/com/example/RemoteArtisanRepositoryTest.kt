package com.example

import com.example.data.api.KaarigarApiService
import com.example.data.api.model.BuyerInquiryDto
import com.example.data.api.model.ProductDto
import com.example.data.api.model.toDto
import com.example.data.api.model.toEntity
import com.example.data.db.BuyerInquiryDao
import com.example.data.db.ProductDao
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import com.example.data.repository.ArtisanRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RemoteArtisanRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    // Test Fake ProductDao
    private class FakeProductDao : ProductDao {
        val storage = mutableListOf<ProductEntity>()

        override fun getAllProducts() = flowOf(storage.toList())
        override suspend fun getAllProductsList() = storage.toList()
        override suspend fun getProductsModifiedSince(since: Long) = storage.filter { it.timestamp > since }
        override fun getProductsByStatus(status: String) = flowOf(storage.filter { it.status == status })
        override suspend fun getProductById(id: Long) = storage.find { it.id == id }
        override fun getProductsByCategory(category: String) = flowOf(storage.filter { it.category == category })
        override suspend fun insertProduct(product: ProductEntity): Long {
            val id = if (product.id > 0) product.id else (storage.size + 1).toLong()
            val toSave = product.copy(id = id)
            storage.removeAll { it.id == id }
            storage.add(toSave)
            return id
        }
        override suspend fun insertProducts(products: List<ProductEntity>) {
            products.forEach { insertProduct(it) }
        }
        override suspend fun updateProduct(product: ProductEntity) {
            storage.removeAll { it.id == product.id }
            storage.add(product)
        }
        override suspend fun deleteProductById(id: Long) {
            storage.removeAll { it.id == id }
        }
        override fun getProductCount() = flowOf(storage.size)
        override suspend fun incrementInquiryCount(id: Long) {
            val p = storage.find { it.id == id }
            if (p != null) {
                updateProduct(p.copy(inquiryCount = p.inquiryCount + 1))
            }
        }
    }

    // Test Fake BuyerInquiryDao
    private class FakeBuyerInquiryDao : BuyerInquiryDao {
        val storage = mutableListOf<BuyerInquiryEntity>()

        override fun getAllInquiries() = flowOf(storage.toList())
        override suspend fun getAllInquiriesList() = storage.toList()
        override suspend fun getInquiriesModifiedSince(since: Long) = storage.filter { it.timestamp > since }
        override fun getInquiriesByStatus(status: String) = flowOf(storage.filter { it.status == status })
        override fun getInquiriesForProduct(productId: Long) = flowOf(storage.filter { it.productId == productId })
        override suspend fun insertInquiry(inquiry: BuyerInquiryEntity): Long {
            val id = if (inquiry.id > 0) inquiry.id else (storage.size + 1).toLong()
            val toSave = inquiry.copy(id = id)
            storage.removeAll { it.id == id }
            storage.add(toSave)
            return id
        }
        override suspend fun insertInquiries(inquiries: List<BuyerInquiryEntity>) {
            inquiries.forEach { insertInquiry(it) }
        }
        override suspend fun updateInquiry(inquiry: BuyerInquiryEntity) {
            storage.removeAll { it.id == inquiry.id }
            storage.add(inquiry)
        }
        override suspend fun updateStatus(id: Long, status: String) {
            val item = storage.find { it.id == id }
            if (item != null) updateInquiry(item.copy(status = status))
        }
        override suspend fun respondToInquiry(id: Long, status: String, response: String) {
            val item = storage.find { it.id == id }
            if (item != null) updateInquiry(item.copy(status = status, artisanResponse = response))
        }
        override suspend fun deleteInquiryById(id: Long) {
            storage.removeAll { it.id == id }
        }
    }

    // Test Fake KaarigarApiService
    private class FakeKaarigarApiService : KaarigarApiService {
        val remoteProducts = mutableListOf<ProductDto>()
        val remoteInquiries = mutableListOf<BuyerInquiryDto>()

        override suspend fun checkHealth() = Response.success(
            com.example.data.api.model.BackendHealthResponse("ok", "healthy", "1.0.0", System.currentTimeMillis())
        )

        override suspend fun getProducts(
            category: String?,
            craftType: String?,
            region: String?,
            query: String?,
            limit: Int?
        ): Response<List<ProductDto>> {
            var result = remoteProducts.toList()
            if (category != null) result = result.filter { it.category.contains(category, ignoreCase = true) }
            return Response.success(result)
        }

        override suspend fun getProductById(id: Long): Response<ProductDto> {
            val item = remoteProducts.find { it.id == id }
            return if (item != null) Response.success(item) else Response.error(404, "".toResponseBody(null))
        }

        override suspend fun createProduct(product: ProductDto): Response<ProductDto> {
            remoteProducts.add(product)
            return Response.success(product)
        }

        override suspend fun updateProduct(id: Long, product: ProductDto): Response<ProductDto> {
            remoteProducts.removeAll { it.id == id }
            remoteProducts.add(product)
            return Response.success(product)
        }

        override suspend fun deleteProduct(id: Long): Response<Unit> {
            remoteProducts.removeAll { it.id == id }
            return Response.success(Unit)
        }

        override suspend fun syncProducts(products: List<ProductDto>): Response<List<ProductDto>> {
            remoteProducts.clear()
            remoteProducts.addAll(products)
            return Response.success(products)
        }

        override suspend fun batchSyncProducts(request: com.example.data.api.model.ProductBatchSyncRequest) = Response.success(
            com.example.data.api.model.ProductBatchSyncResponse(true, request.products.size, emptyList(), System.currentTimeMillis())
        )

        override suspend fun getInquiries(
            status: String?,
            artisanPhone: String?
        ): Response<List<BuyerInquiryDto>> {
            var result = remoteInquiries.toList()
            if (status != null) result = result.filter { it.status == status }
            return Response.success(result)
        }

        override suspend fun getInquiryById(id: Long): Response<BuyerInquiryDto> {
            val item = remoteInquiries.find { it.id == id }
            return if (item != null) Response.success(item) else Response.error(404, "".toResponseBody(null))
        }

        override suspend fun getInquiriesForProduct(productId: Long): Response<List<BuyerInquiryDto>> {
            return Response.success(remoteInquiries.filter { it.productId == productId })
        }

        override suspend fun createInquiry(inquiry: BuyerInquiryDto): Response<BuyerInquiryDto> {
            remoteInquiries.add(inquiry)
            return Response.success(inquiry)
        }

        override suspend fun updateInquiryStatus(id: Long, request: com.example.data.api.model.UpdateInquiryStatusRequest): Response<BuyerInquiryDto> {
            val item = remoteInquiries.find { it.id == id }
            return if (item != null) {
                val updated = item.copy(status = request.status)
                remoteInquiries.removeAll { it.id == id }
                remoteInquiries.add(updated)
                Response.success(updated)
            } else {
                Response.error(404, "".toResponseBody(null))
            }
        }

        override suspend fun respondToInquiry(id: Long, request: com.example.data.api.model.RespondInquiryRequest): Response<BuyerInquiryDto> {
            val item = remoteInquiries.find { it.id == id }
            return if (item != null) {
                val updated = item.copy(status = request.status, artisanResponse = request.response)
                remoteInquiries.removeAll { it.id == id }
                remoteInquiries.add(updated)
                Response.success(updated)
            } else {
                Response.error(404, "".toResponseBody(null))
            }
        }

        override suspend fun deleteInquiry(id: Long): Response<Unit> {
            remoteInquiries.removeAll { it.id == id }
            return Response.success(Unit)
        }

        override suspend fun batchSyncInquiries(request: com.example.data.api.model.InquiryBatchSyncRequest) = Response.success(
            com.example.data.api.model.InquiryBatchSyncResponse(true, request.inquiries.size, emptyList(), System.currentTimeMillis())
        )

        override suspend fun getActivityEvents(): Response<List<com.example.data.api.model.UserActivityEventDto>> {
            return Response.success(emptyList())
        }

        override suspend fun logActivityEvent(event: com.example.data.api.model.UserActivityEventDto): Response<com.example.data.api.model.UserActivityEventDto> {
            return Response.success(event)
        }

        override suspend fun syncActivityEvents(request: com.example.data.api.model.SyncActivityEventsRequest): Response<com.example.data.api.model.SyncActivityEventsResponse> {
            return Response.success(
                com.example.data.api.model.SyncActivityEventsResponse(
                    success = true,
                    syncedCount = request.events.size,
                    serverTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    @Test
    fun `test Product Dto and Entity mapping`() {
        val entity = ProductEntity(
            id = 5,
            title = "Terracotta Elephant",
            description = "Handcrafted clay art",
            category = "Pottery",
            craftType = "Terracotta",
            region = "Gorakhpur",
            retailPrice = 950.0
        )
        val dto = entity.toDto()
        assertEquals(entity.id, dto.id)
        assertEquals(entity.title, dto.title)
        assertEquals(entity.category, dto.category)

        val backToEntity = dto.toEntity()
        assertEquals(entity.id, backToEntity.id)
        assertEquals(entity.title, backToEntity.title)
    }

    @Test
    fun `test repository remote product sync and Room offline caching`() = runTest(testDispatcher) {
        val fakeProductDao = FakeProductDao()
        val fakeInquiryDao = FakeBuyerInquiryDao()
        val fakeApi = FakeKaarigarApiService()

        // Prepopulate remote API with a product
        fakeApi.remoteProducts.add(
            ProductDto(
                id = 42,
                title = "Remote Handloom Saree",
                description = "Authentic Banarasi Brocade",
                category = "Handloom",
                craftType = "Brocade Silk",
                region = "Varanasi, UP",
                retailPrice = 8500.0
            )
        )

        val repository = ArtisanRepository(
            productDao = fakeProductDao,
            buyerInquiryDao = fakeInquiryDao,
            apiService = fakeApi,
            ioDispatcher = testDispatcher
        )

        // 1. Initial local cache is empty
        assertEquals(0, fakeProductDao.storage.size)

        // 2. Perform remote refresh
        val refreshResult = repository.refreshProducts()
        assertTrue(refreshResult.isSuccess)
        assertEquals(1, refreshResult.getOrNull()?.size)

        // 3. Verify Room local cache was populated from remote API
        assertEquals(1, fakeProductDao.storage.size)
        assertEquals("Remote Handloom Saree", fakeProductDao.storage[0].title)

        // 4. Save a new product offline in repository
        val newProduct = ProductEntity(
            id = 100,
            title = "Artisan Clay Diya Set",
            description = "Set of 6 handcrafted diyas",
            category = "Pottery",
            craftType = "Terracotta",
            region = "Gorakhpur, UP",
            retailPrice = 450.0
        )
        repository.saveProduct(newProduct)

        // Verify product is in local Room cache and sent to remote API
        assertNotNull(fakeProductDao.storage.find { it.id == 100L })
        assertNotNull(fakeApi.remoteProducts.find { it.id == 100L })
    }

    @Test
    fun `test repository remote inquiry creation and lifecycle`() = runTest(testDispatcher) {
        val fakeProductDao = FakeProductDao()
        val fakeInquiryDao = FakeBuyerInquiryDao()
        val fakeApi = FakeKaarigarApiService()

        val repository = ArtisanRepository(
            productDao = fakeProductDao,
            buyerInquiryDao = fakeInquiryDao,
            apiService = fakeApi,
            ioDispatcher = testDispatcher
        )

        val inquiry = BuyerInquiryEntity(
            id = 201,
            productId = 42,
            productTitle = "Remote Handloom Saree",
            buyerName = "Rahul Verma",
            buyerCompany = "Indie Craft Store",
            requestedQuantity = 20,
            message = "Need 20 pieces before next month",
            status = "NEW"
        )

        // 1. Save inquiry
        repository.saveInquiry(inquiry)

        // 2. Verify Room cache and Remote API
        assertEquals(1, fakeInquiryDao.storage.size)
        assertEquals(1, fakeApi.remoteInquiries.size)

        // 3. Respond to inquiry
        repository.respondToInquiry(201, "RESPONDED", "Order accepted with delivery in 2 weeks")
        assertEquals("RESPONDED", fakeInquiryDao.storage.first().status)
        assertEquals("RESPONDED", fakeApi.remoteInquiries.first().status)
    }
}
