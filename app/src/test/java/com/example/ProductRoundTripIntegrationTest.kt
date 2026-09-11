package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.KaarigarApiService
import com.example.data.db.AppDatabase
import com.example.data.db.ProductDao
import com.example.data.model.ProductEntity
import com.example.data.repository.ArtisanRepository
import com.example.data.api.model.ProductDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
class ProductRoundTripIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var apiService: KaarigarApiService
    private lateinit var repository: ArtisanRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // 1. Setup MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        // 2. Setup Retrofit
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KaarigarApiService::class.java)

        // 3. Setup In-Memory Room Database
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Allowed for testing
            .build()

        // 4. Setup Repository
        repository = ArtisanRepository(
            productDao = database.productDao(),
            buyerInquiryDao = database.buyerInquiryDao(),
            apiService = apiService,
            ioDispatcher = testDispatcher,
            eventDao = null // Disable analytics for this test
        )
    }

    @After
    fun teardown() {
        database.close()
        mockWebServer.shutdown()
    }

    @Test
    fun `test full round-trip flow from local draft creation to sync and retrieval`() = runTest(testDispatcher) {
        // Enqueue Mock responses
        // Response for POST /products
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":1,"title":"Artisan Vase","description":"Handmade clay vase","category":"Pottery","craftType":"Terracotta","region":"India","retailPrice":1500.0,"status":"PUBLISHED"}""")
        )

        // Response for GET /products
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""[{"id":1,"title":"Artisan Vase","description":"Handmade clay vase","category":"Pottery","craftType":"Terracotta","region":"India","retailPrice":1500.0,"status":"PUBLISHED"}]""")
        )

        // 1. Local Product Draft Creation
        val localDraft = ProductEntity(
            title = "Artisan Vase",
            description = "Handmade clay vase",
            category = "Pottery",
            craftType = "Terracotta",
            region = "India",
            retailPrice = 1500.0
        )

        // 2. Submission to Room and Retrofit
        val localId = repository.saveProduct(localDraft)
        
        // Assert it's in local DB
        val cachedProduct = database.productDao().getProductById(localId)
        assertNotNull(cachedProduct)
        assertEquals("Artisan Vase", cachedProduct?.title)

        // Verify POST request sent
        val postRequest = mockWebServer.takeRequest()
        assertEquals("/products", postRequest.path)
        assertEquals("POST", postRequest.method)
        assertTrue(postRequest.body.readUtf8().contains("Artisan Vase"))

        // 3. Synchronization (Refresh from remote backend)
        val refreshResult = repository.refreshProducts()
        if (refreshResult.isFailure) {
            println("refreshResult failed: ${refreshResult.exceptionOrNull()}")
        }
        assertTrue(refreshResult.isSuccess)
        
        // Verify GET request sent
        val getRequest = mockWebServer.takeRequest()
        assertEquals("/products?limit=100", getRequest.path)
        assertEquals("GET", getRequest.method)
        
        // 4. Retrieval verification
        val products = database.productDao().getAllProducts().first()
        assertTrue(products.isNotEmpty())
        assertEquals("Artisan Vase", products[0].title)
    }
}
