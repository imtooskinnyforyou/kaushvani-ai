package com.example

import com.example.data.analytics.EventTrackingService
import com.example.data.analytics.RoomEventTrackingService
import com.example.data.api.KaarigarApiService
import com.example.data.api.model.*
import com.example.data.db.UserActivityEventDao
import com.example.data.model.UserActivityEventEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EventTrackingServiceTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserActivityEventDao : UserActivityEventDao {
        val events = mutableListOf<UserActivityEventEntity>()

        override fun getAllEvents() = flowOf(events.toList())
        override fun getRecentEvents(limit: Int) = flowOf(events.take(limit))
        override suspend fun getUnsyncedEvents() = events.filter { !it.isSynced }
        override fun getUnsyncedEventsFlow() = flowOf(events.filter { !it.isSynced })
        override fun getUnsyncedCount() = flowOf(events.count { !it.isSynced })
        override fun getTotalEventCount() = flowOf(events.size)

        override suspend fun insertEvent(event: UserActivityEventEntity): Long {
            val id = if (event.id > 0) event.id else (events.size + 1).toLong()
            val toSave = event.copy(id = id)
            events.removeAll { it.id == id }
            events.add(0, toSave)
            return id
        }

        override suspend fun insertEvents(newEvents: List<UserActivityEventEntity>) {
            newEvents.forEach { insertEvent(it) }
        }

        override suspend fun updateEvent(event: UserActivityEventEntity) {
            events.removeAll { it.id == event.id }
            events.add(event)
        }

        override suspend fun markEventsAsSynced(eventIds: List<Long>, syncedAt: Long) {
            for (i in events.indices) {
                if (eventIds.contains(events[i].id)) {
                    events[i] = events[i].copy(isSynced = true, syncedAt = syncedAt)
                }
            }
        }

        override suspend fun deleteEventById(id: Long) {
            events.removeAll { it.id == id }
        }

        override suspend fun clearAllEvents() {
            events.clear()
        }
    }

    private class FakeOfflineApiService : KaarigarApiService {
        var networkAvailable = false
        val remoteSyncedEvents = mutableListOf<UserActivityEventDto>()

        override suspend fun checkHealth() = Response.success(
            BackendHealthResponse("ok", "healthy", "1.0.0", System.currentTimeMillis())
        )

        override suspend fun getProducts(
            category: String?,
            craftType: String?,
            region: String?,
            query: String?,
            limit: Int?
        ) = Response.success(emptyList<ProductDto>())

        override suspend fun getProductById(id: Long) = Response.error<ProductDto>(404, "".toResponseBody())
        override suspend fun createProduct(product: ProductDto) = Response.success(product)
        override suspend fun updateProduct(id: Long, product: ProductDto) = Response.success(product)
        override suspend fun deleteProduct(id: Long) = Response.success(Unit)
        override suspend fun syncProducts(products: List<ProductDto>) = Response.success(products)
        override suspend fun batchSyncProducts(request: ProductBatchSyncRequest) = Response.success(
            ProductBatchSyncResponse(true, request.products.size, emptyList(), System.currentTimeMillis())
        )

        override suspend fun getInquiries(
            status: String?,
            artisanPhone: String?
        ) = Response.success(emptyList<BuyerInquiryDto>())

        override suspend fun getInquiryById(id: Long) = Response.error<BuyerInquiryDto>(404, "".toResponseBody())
        override suspend fun getInquiriesForProduct(productId: Long) = Response.success(emptyList<BuyerInquiryDto>())
        override suspend fun createInquiry(inquiry: BuyerInquiryDto) = Response.success(inquiry)
        override suspend fun updateInquiryStatus(id: Long, request: UpdateInquiryStatusRequest): Response<BuyerInquiryDto> {
            return Response.success(
                BuyerInquiryDto(
                    id = id,
                    productId = 1,
                    productTitle = "Vase",
                    buyerName = "Buyer",
                    message = "Interested in 50 pieces",
                    status = request.status
                )
            )
        }
        override suspend fun respondToInquiry(id: Long, request: RespondInquiryRequest): Response<BuyerInquiryDto> {
            return Response.success(
                BuyerInquiryDto(
                    id = id,
                    productId = 1,
                    productTitle = "Vase",
                    buyerName = "Buyer",
                    message = "Interested in 50 pieces",
                    status = request.status,
                    artisanResponse = request.response
                )
            )
        }
        override suspend fun deleteInquiry(id: Long) = Response.success(Unit)
        override suspend fun batchSyncInquiries(request: InquiryBatchSyncRequest) = Response.success(
            InquiryBatchSyncResponse(true, request.inquiries.size, emptyList(), System.currentTimeMillis())
        )

        override suspend fun getActivityEvents(): Response<List<UserActivityEventDto>> {
            return if (networkAvailable) Response.success(remoteSyncedEvents) else throw RuntimeException("Network down")
        }

        override suspend fun logActivityEvent(event: UserActivityEventDto): Response<UserActivityEventDto> {
            if (!networkAvailable) throw RuntimeException("Network down (Offline)")
            remoteSyncedEvents.add(event)
            return Response.success(event)
        }

        override suspend fun syncActivityEvents(request: SyncActivityEventsRequest): Response<SyncActivityEventsResponse> {
            if (!networkAvailable) throw RuntimeException("Network down (Offline)")
            remoteSyncedEvents.addAll(request.events)
            return Response.success(
                SyncActivityEventsResponse(
                    success = true,
                    syncedCount = request.events.size,
                    serverTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    @Test
    fun testOfflineEventLogging_persistsLocallyInRoom() = runTest(testDispatcher) {
        val fakeDao = FakeUserActivityEventDao()
        val fakeApi = FakeOfflineApiService()
        fakeApi.networkAvailable = false // Device is completely offline

        val service = RoomEventTrackingService(fakeDao, fakeApi, testDispatcher)

        val eventId = service.logEvent(
            eventType = RoomEventTrackingService.EVENT_PRODUCT_CREATED,
            title = "शिल्प दर्ज किया गया (Product Created)",
            description = "Earthen Pot • Pottery",
            category = "CATALOG"
        )

        assertEquals(1L, eventId)
        val unsynced = fakeDao.getUnsyncedEvents()
        assertEquals(1, unsynced.size)
        assertEquals("PRODUCT_CREATED", unsynced[0].eventType)
        assertFalse(unsynced[0].isSynced)
    }

    @Test
    fun testSyncPendingEvents_whenNetworkRestored_syncsAllEventsAndMarksRoomSynced() = runTest(testDispatcher) {
        val fakeDao = FakeUserActivityEventDao()
        val fakeApi = FakeOfflineApiService()
        fakeApi.networkAvailable = false // Start offline

        val service = RoomEventTrackingService(fakeDao, fakeApi, testDispatcher)

        // Log 3 events while offline
        service.logEvent(RoomEventTrackingService.EVENT_PRODUCT_CREATED, "Event 1", category = "CATALOG")
        service.logEvent(RoomEventTrackingService.EVENT_INQUIRY_SENT, "Event 2", category = "INQUIRY")
        service.logEvent(RoomEventTrackingService.EVENT_PRICING_CALCULATED, "Event 3", category = "PRICING")

        assertEquals(3, fakeDao.getUnsyncedEvents().size)
        assertEquals(0, fakeApi.remoteSyncedEvents.size)

        // Connectivity is restored!
        fakeApi.networkAvailable = true

        val syncResult = service.syncPendingEvents()

        assertTrue(syncResult.isSuccess)
        assertEquals(3, syncResult.getOrNull())
        assertEquals(0, fakeDao.getUnsyncedEvents().size)
        assertEquals(3, fakeApi.remoteSyncedEvents.size)
    }

    @Test
    fun testImmediateSync_whenOnline_marksEventSyncedImmediately() = runTest(testDispatcher) {
        val fakeDao = FakeUserActivityEventDao()
        val fakeApi = FakeOfflineApiService()
        fakeApi.networkAvailable = true // Online

        val service = RoomEventTrackingService(fakeDao, fakeApi, testDispatcher)

        val id = service.logEvent(
            eventType = RoomEventTrackingService.EVENT_INQUIRY_RESPONDED,
            title = "पूछताछ का उत्तर दिया (Inquiry Responded)",
            category = "INQUIRY"
        )

        assertEquals(1L, id)
        assertEquals(0, fakeDao.getUnsyncedEvents().size)
        val allEvents = fakeDao.getAllEvents().first()
        assertTrue(allEvents[0].isSynced)
    }
}
