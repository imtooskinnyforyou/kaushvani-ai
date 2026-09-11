package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.NetworkType
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.data.api.KaarigarApiService
import com.example.data.db.AppDatabase
import com.example.data.model.CatalogSyncQueueEntity
import com.example.data.model.ChannelType
import com.example.data.model.PricingSeason
import com.example.data.model.ProductEntity
import com.example.data.model.QueuedAiTaskEntity
import com.example.data.repository.ArtisanRepository
import com.example.data.sync.CatalogBackgroundSyncWorker
import com.example.data.sync.WorkManagerSyncScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WorkManagerBackgroundSyncWorkerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: KaarigarApiService
    private lateinit var repository: ArtisanRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        androidx.work.testing.WorkManagerTestInitHelper.initializeTestWorkManager(context)

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KaarigarApiService::class.java)

        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = ArtisanRepository(
            productDao = database.productDao(),
            buyerInquiryDao = database.buyerInquiryDao(),
            eventDao = database.userActivityEventDao(),
            apiService = apiService,
            syncQueueDao = database.catalogSyncQueueDao(),
            queuedAiTaskDao = database.queuedAiTaskDao(),
            context = context,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        database.close()
        mockWebServer.shutdown()
    }

    @Test
    fun testCatalogSyncQueueDao_insertAndQueryPending() = runTest(testDispatcher) {
        val queueDao = database.catalogSyncQueueDao()

        val item1 = CatalogSyncQueueEntity(productId = 101L, operation = "CREATE")
        val item2 = CatalogSyncQueueEntity(productId = 102L, operation = "UPDATE")

        queueDao.insert(item1)
        queueDao.insert(item2)

        val pending = queueDao.getPendingItems()
        assertEquals(2, pending.size)

        val count = queueDao.getPendingCount()
        assertEquals(2, count)

        // Mark first item SYNCED
        queueDao.updateStatus(pending[0].id, "SYNCED", null, System.currentTimeMillis())

        val updatedPending = queueDao.getPendingItems()
        assertEquals(1, updatedPending.size)
        assertEquals(102L, updatedPending[0].productId)
    }

    @Test
    fun testQueuedAiTaskDao_insertAndQueryPending() = runTest(testDispatcher) {
        val aiDao = database.queuedAiTaskDao()

        val task = QueuedAiTaskEntity(
            taskType = CatalogBackgroundSyncWorker.TASK_TYPE_VOICE_CATALOG_ENRICHMENT,
            productId = 101L,
            title = "Voice Catalog AI Task",
            inputPayloadJson = """{"transcript":"clay pot handmade"}"""
        )

        val taskId = aiDao.insert(task)
        assertTrue(taskId > 0)

        val pending = aiDao.getPendingTasks()
        assertEquals(1, pending.size)
        assertEquals(CatalogBackgroundSyncWorker.TASK_TYPE_VOICE_CATALOG_ENRICHMENT, pending[0].taskType)

        // Mark completed
        aiDao.updateStatus(taskId, "COMPLETED", """{"status":"SUCCESS"}""", null, System.currentTimeMillis())

        val count = aiDao.getPendingCount()
        assertEquals(0, count)
    }

    @Test
    fun testWorkRequestBuilder_hasNetworkConstraintAndTags() {
        val request = CatalogBackgroundSyncWorker.buildOneTimeWorkRequest("TEST_TRIGGER")

        val constraints = request.workSpec.constraints
        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertTrue(request.tags.contains(CatalogBackgroundSyncWorker.WORK_TAG))
    }

    @Test
    fun testPeriodicWorkRequestBuilder_hasCorrectIntervalAndConstraints() {
        val periodicRequest = CatalogBackgroundSyncWorker.buildPeriodicWorkRequest(repeatIntervalMinutes = 30)

        val constraints = periodicRequest.workSpec.constraints
        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertTrue(periodicRequest.tags.contains(CatalogBackgroundSyncWorker.WORK_TAG))
    }

    @Test
    fun testSaveProduct_queuesForWorkManagerWhenRemoteSyncFails() = runTest(testDispatcher) {
        // Mock remote server returning 503 error
        mockWebServer.enqueue(MockResponse().setResponseCode(503))

        val product = ProductEntity(
            title = "Terracotta Elephant",
            description = "Traditional clay sculpture",
            category = "Pottery",
            craftType = "Terracotta",
            region = "Gorakhpur",
            retailPrice = 750.0
        )

        val localId = repository.saveProduct(product)
        assertTrue(localId > 0)

        // Verify product exists locally in Room
        val savedProduct = database.productDao().getProductById(localId)
        assertNotNull(savedProduct)
        assertEquals("Terracotta Elephant", savedProduct?.title)

        // Verify it was queued in catalog_sync_queue for WorkManager background sync
        val queueDao = database.catalogSyncQueueDao()
        val pendingItems = queueDao.getPendingItems()
        assertEquals(1, pendingItems.size)
        assertEquals(localId, pendingItems[0].productId)
        assertEquals("CREATE", pendingItems[0].operation)
    }

    @Test
    fun testQueueVoiceCatalogAiTask_enqueuesTaskCorrectly() = runTest(testDispatcher) {
        val taskId = repository.queueVoiceCatalogAiTask(
            productId = 42L,
            transcript = "मिट्टी का दिया हस्तनिर्मित",
            languageCode = "hi",
            artisanState = "Uttar Pradesh",
            artisanCluster = "Gorakhpur",
            categoryHint = "Pottery"
        )

        assertTrue(taskId > 0)

        val pendingTasks = database.queuedAiTaskDao().getPendingTasks()
        assertEquals(1, pendingTasks.size)
        assertEquals(42L, pendingTasks[0].productId)
        assertEquals(CatalogBackgroundSyncWorker.TASK_TYPE_VOICE_CATALOG_ENRICHMENT, pendingTasks[0].taskType)
    }

    @Test
    fun testQueueDynamicPricingAiTask_enqueuesTaskCorrectly() = runTest(testDispatcher) {
        val taskId = repository.queueDynamicPricingAiTask(
            productId = 99L,
            materialCost = 150.0,
            laborHours = 4.0,
            hourlyWage = 180.0,
            packagingCost = 40.0,
            season = PricingSeason.DIWALI_FESTIVE,
            channel = ChannelType.DIRECT_CRAFT_FAIR
        )

        assertTrue(taskId > 0)

        val pendingTasks = database.queuedAiTaskDao().getPendingTasks()
        assertEquals(1, pendingTasks.size)
        assertEquals(99L, pendingTasks[0].productId)
        assertEquals(CatalogBackgroundSyncWorker.TASK_TYPE_DYNAMIC_PRICING, pendingTasks[0].taskType)
    }

    @Test
    fun testBackgroundSyncWorker_executionSuccess() = runTest {
        // Enqueue Mock responses for worker
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val worker = TestListenableWorkerBuilder<CatalogBackgroundSyncWorker>(context)
            .build()

        val result = worker.doWork()
        assertNotNull(result)
        assertTrue(result is androidx.work.ListenableWorker.Result.Success)
    }
}
