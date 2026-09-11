package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.ai.DynamicPricingService
import com.example.data.ai.DynamicPricingServiceImpl
import com.example.data.ai.GeminiMetadataService
import com.example.data.ai.GeminiMetadataServiceImpl
import com.example.data.ai.model.DynamicPricingRequest
import com.example.data.ai.model.DynamicPricingResponse
import com.example.data.ai.model.ProductMetadataRequest
import com.example.data.ai.model.ProductStructuredMetadata
import com.example.data.analytics.EventTrackingService
import com.example.data.analytics.RoomEventTrackingService
import com.example.data.api.KaarigarApiService
import com.example.data.api.RetrofitClient
import com.example.data.api.model.RespondInquiryRequest
import com.example.data.api.model.UpdateInquiryStatusRequest
import com.example.data.api.model.toDto
import com.example.data.api.model.toEntity
import com.example.data.db.BuyerInquiryDao
import com.example.data.db.ProductDao
import com.example.data.db.UserActivityEventDao
import com.example.data.firebase.AuthUser
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.model.ArtisanProfile
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import com.example.data.model.UserActivityEventEntity
import com.example.data.sync.DataSyncManager
import com.example.data.sync.SyncState
import com.example.data.sync.SyncSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * ArtisanRepository provides the single source of truth for the application.
 * - Room Database acts as the local offline-first cache, activity event store, and reactive data stream for Compose UI.
 * - Retrofit KaarigarApiService & DataSyncManager handle remote REST API communications and bidirectional synchronization.
 * - Google Cloud Firestore & Firebase Auth handle multi-user cloud synchronization and identity.
 */
class ArtisanRepository(
    private val productDao: ProductDao,
    private val buyerInquiryDao: BuyerInquiryDao,
    private val eventDao: UserActivityEventDao? = null,
    private val apiService: KaarigarApiService = RetrofitClient.apiService,
    val firestoreService: FirestoreService? = null,
    val firebaseAuthManager: FirebaseAuthManager? = null,
    val productFirestoreRepository: ProductFirestoreRepository? = firestoreService?.let {
        ProductFirestoreRepositoryImpl(it, productDao)
    },
    val inquiryFirestoreRepository: InquiryFirestoreRepository? = firestoreService?.let {
        InquiryFirestoreRepositoryImpl(it, buyerInquiryDao, productDao)
    },
    private val geminiMetadataService: GeminiMetadataService = GeminiMetadataServiceImpl(),
    private val dynamicPricingService: DynamicPricingService = DynamicPricingServiceImpl(),
    val syncQueueDao: com.example.data.db.CatalogSyncQueueDao? = null,
    val queuedAiTaskDao: com.example.data.db.QueuedAiTaskDao? = null,
    val context: Context? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "ArtisanRepository"
    }

    // Bidirectional Data Synchronization Manager (Room <-> Retrofit REST API)
    val dataSyncManager: DataSyncManager = DataSyncManager(
        productDao = productDao,
        buyerInquiryDao = buyerInquiryDao,
        eventDao = eventDao,
        apiService = apiService,
        ioDispatcher = ioDispatcher
    )

    // Event Tracking Service (offline-first Room event logger)
    val eventTrackingService: EventTrackingService? = eventDao?.let {
        RoomEventTrackingService(it, apiService, ioDispatcher)
    }

    // Reactive streams backed by Room offline cache
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allInquiries: Flow<List<BuyerInquiryEntity>> = buyerInquiryDao.getAllInquiries()
    val totalProductCount: Flow<Int> = productDao.getProductCount()
    val allActivityEvents: Flow<List<UserActivityEventEntity>>? = eventTrackingService?.allEvents
    val unsyncedEventsCount: Flow<Int>? = eventTrackingService?.unsyncedCount
    val pendingCatalogQueueCount: Flow<Int>? = syncQueueDao?.observePendingCount()
    val pendingAiTasksCount: Flow<Int>? = queuedAiTaskDao?.observePendingCount()

    // Multi-User Firestore Real-Time Stream
    val cloudProductsFlow: Flow<List<ProductEntity>>? = firestoreService?.observeAllProducts()

    // Synchronization observable states
    val syncState: StateFlow<SyncState> = dataSyncManager.syncState
    val isSyncing: StateFlow<Boolean> = dataSyncManager.isSyncing
    val lastSyncTimestamp: StateFlow<Long> = dataSyncManager.lastSyncTimestamp

    private val _artisanProfile = MutableStateFlow(
        ArtisanProfile(
            name = "Ram Prasad Prajapati",
            craftSpecialty = "Traditional Terracotta & Pottery",
            experienceYears = 18,
            villageOrCluster = "Bhiti Rawat, Gorakhpur",
            state = "Uttar Pradesh",
            phone = "+91 98765 43210",
            upiId = "ramprasad.artisan@upi",
            artisanCardNo = "PMV-UP-2024-8849",
            isVishwakarmaEnrolled = true,
            totalSalesCount = 142,
            totalRevenue = 184500.0,
            preferredLanguage = "Hindi"
        )
    )
    val artisanProfile = _artisanProfile.asStateFlow()

    suspend fun allProductsList(): List<ProductEntity> = withContext(ioDispatcher) {
        productDao.getAllProductsList()
    }

    suspend fun allInquiriesList(): List<BuyerInquiryEntity> = withContext(ioDispatcher) {
        buyerInquiryDao.getAllInquiriesList()
    }

    // ==========================================
    // REMOTE SYNC (Retrofit <-> Room)
    // ==========================================

    /**
     * Complete bidirectional synchronization cycle for products, inquiries, and events.
     */
    suspend fun syncAllData(artisanPhone: String = _artisanProfile.value.phone): Result<SyncSummary> {
        val result = dataSyncManager.syncAll(artisanPhone)
        if (result.isSuccess) {
            val summary = result.getOrNull()
            eventTrackingService?.logEvent(
                eventType = RoomEventTrackingService.EVENT_OFFLINE_SYNC,
                title = "डेटा सिंक पूर्ण (Full Data Synced)",
                description = "Uploaded: ${summary?.productsUploaded ?: 0} prod, ${summary?.inquiriesUploaded ?: 0} inq. Downloaded: ${summary?.productsDownloaded ?: 0} prod, ${summary?.inquiriesDownloaded ?: 0} inq.",
                category = "SYSTEM"
            )
        }
        return result
    }

    /**
     * Fetch products from remote API and update local Room cache.
     */
    suspend fun refreshProducts(): Result<List<ProductEntity>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getProducts()
            if (response.isSuccessful) {
                val remoteDtos = response.body() ?: emptyList()
                if (remoteDtos.isNotEmpty()) {
                    val entities = remoteDtos.map { it.toEntity() }
                    productDao.insertProducts(entities)
                    Log.d(TAG, "Successfully synced ${entities.size} products from remote API into Room cache")
                    Result.success(entities)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Log.w(TAG, "Failed to refresh remote products: ${response.code()} ${response.message()}")
                Result.failure(Exception("Remote error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network exception refreshing remote products, using Room offline cache: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetch inquiries from remote API and update local Room cache.
     */
    suspend fun refreshInquiries(): Result<List<BuyerInquiryEntity>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getInquiries()
            if (response.isSuccessful) {
                val remoteDtos = response.body() ?: emptyList()
                if (remoteDtos.isNotEmpty()) {
                    val entities = remoteDtos.map { it.toEntity() }
                    buyerInquiryDao.insertInquiries(entities)
                    Log.d(TAG, "Successfully synced ${entities.size} inquiries from remote API into Room cache")
                    Result.success(entities)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Log.w(TAG, "Failed to refresh remote inquiries: ${response.code()}")
                Result.failure(Exception("Remote error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network exception refreshing remote inquiries, using Room offline cache: ${e.message}")
            Result.failure(e)
        }
    }

    // ==========================================
    // PRODUCT CRUD WITH REMOTE SYNC & OFFLINE CACHE
    // ==========================================

    suspend fun saveProduct(product: ProductEntity): Long = withContext(ioDispatcher) {
        // 1. Save locally to Room immediately for guaranteed offline persistence & UI responsiveness
        val localId = productDao.insertProduct(product)
        val productWithId = product.copy(id = localId)

        // Log user activity event locally in Room
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_PRODUCT_CREATED,
            title = "शिल्प दर्ज किया गया (Product Created): ${product.title.take(30)}",
            description = "${product.category} • ₹${product.retailPrice.toInt()} • ${product.craftType}",
            category = "CATALOG",
            metadata = mapOf("productId" to localId.toString(), "category" to product.category)
        )

        // 2. Transmit to remote backend via Retrofit
        var remoteSynced = false
        try {
            val response = apiService.createProduct(productWithId.toDto())
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Product successfully synced with remote backend API")
                remoteSynced = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Product saved to local Room cache; remote sync deferred: ${e.message}")
        }

        // 3. If remote sync was not immediate, queue for background sync via WorkManager
        if (!remoteSynced) {
            try {
                syncQueueDao?.insert(
                    com.example.data.model.CatalogSyncQueueEntity(
                        productId = localId,
                        operation = "CREATE",
                        status = "PENDING"
                    )
                )
                context?.let { ctx ->
                    com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                        ctx,
                        com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue background sync task: ${e.message}")
            }
        }

        // 4. Multi-User Cloud Sync via Google Cloud Firestore
        val currentUid = firebaseAuthManager?.getCurrentUserId() ?: "artisan_${_artisanProfile.value.phone.replace(Regex("[^0-9]"), "")}"
        try {
            firestoreService?.saveProduct(productWithId, currentUid)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore product sync note: ${e.message}")
        }

        localId
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(ioDispatcher) {
        // 1. Update local Room cache
        productDao.updateProduct(product)

        // Log activity event
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_PRODUCT_UPDATED,
            title = "शिल्प अपडेट किया गया (Product Updated): ${product.title.take(30)}",
            description = "Status: ${product.status} • Price: ₹${product.retailPrice.toInt()}",
            category = "CATALOG",
            metadata = mapOf("productId" to product.id.toString())
        )

        // 2. Sync to remote API
        var remoteSynced = false
        try {
            val response = apiService.updateProduct(product.id, product.toDto())
            if (response.isSuccessful) {
                remoteSynced = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Product updated in Room cache; remote update queued: ${e.message}")
        }

        // 3. If remote update failed, queue for WorkManager background retry
        if (!remoteSynced) {
            try {
                syncQueueDao?.insert(
                    com.example.data.model.CatalogSyncQueueEntity(
                        productId = product.id,
                        operation = "UPDATE",
                        status = "PENDING"
                    )
                )
                context?.let { ctx ->
                    com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                        ctx,
                        com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue background update sync: ${e.message}")
            }
        }

        // 4. Sync to Firestore
        val currentUid = firebaseAuthManager?.getCurrentUserId() ?: "artisan_${_artisanProfile.value.phone.replace(Regex("[^0-9]"), "")}"
        try {
            firestoreService?.saveProduct(product, currentUid)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore product update note: ${e.message}")
        }
    }

    suspend fun deleteProduct(id: Long) = withContext(ioDispatcher) {
        // 1. Delete from local Room cache
        productDao.deleteProductById(id)

        // Log activity event
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_PRODUCT_DELETED,
            title = "शिल्प हटाया गया (Product Deleted)",
            description = "Product ID #$id removed from catalog",
            category = "CATALOG",
            metadata = mapOf("productId" to id.toString())
        )

        // 2. Delete on remote API
        var remoteSynced = false
        try {
            val response = apiService.deleteProduct(id)
            if (response.isSuccessful) {
                remoteSynced = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Product deleted locally; remote delete queued: ${e.message}")
        }

        if (!remoteSynced) {
            try {
                syncQueueDao?.insert(
                    com.example.data.model.CatalogSyncQueueEntity(
                        productId = id,
                        operation = "DELETE",
                        status = "PENDING"
                    )
                )
                context?.let { ctx ->
                    com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                        ctx,
                        com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue background delete sync: ${e.message}")
            }
        }

        // 3. Delete in Firestore
        try {
            firestoreService?.deleteProduct(id)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore product delete note: ${e.message}")
        }
    }

    /**
     * Enqueues an offline voice-to-catalog AI extraction task into Room and dispatches
     * WorkManager to execute when network connectivity is restored.
     */
    suspend fun queueVoiceCatalogAiTask(
        productId: Long?,
        transcript: String,
        languageCode: String,
        artisanState: String = "Uttar Pradesh",
        artisanCluster: String = "Gorakhpur",
        categoryHint: String = "Pottery"
    ): Long = withContext(ioDispatcher) {
        val payload = org.json.JSONObject().apply {
            put("transcript", transcript)
            put("language_code", languageCode)
            put("artisan_state", artisanState)
            put("artisan_cluster", artisanCluster)
            put("category_hint", categoryHint)
            productId?.let { put("product_id", it) }
        }.toString()

        val task = com.example.data.model.QueuedAiTaskEntity(
            taskType = com.example.data.sync.CatalogBackgroundSyncWorker.TASK_TYPE_VOICE_CATALOG_ENRICHMENT,
            productId = productId,
            title = "वॉयस कैटलॉग एआई विश्लेषण (Voice Catalog AI)",
            inputPayloadJson = payload,
            status = "PENDING"
        )
        val taskId = queuedAiTaskDao?.insert(task) ?: 0L
        context?.let { ctx ->
            com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                ctx,
                com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED
            )
        }
        taskId
    }

    /**
     * Enqueues a dynamic pricing AI calculation task into Room for background retry.
     */
    suspend fun queueDynamicPricingAiTask(
        productId: Long,
        materialCost: Double,
        laborHours: Double,
        hourlyWage: Double,
        packagingCost: Double,
        season: com.example.data.model.PricingSeason,
        channel: com.example.data.model.ChannelType
    ): Long = withContext(ioDispatcher) {
        val payload = org.json.JSONObject().apply {
            put("product_id", productId)
            put("material_cost", materialCost)
            put("labor_hours", laborHours)
            put("hourly_wage", hourlyWage)
            put("packaging_cost", packagingCost)
            put("season", season.name)
            put("channel", channel.name)
        }.toString()

        val task = com.example.data.model.QueuedAiTaskEntity(
            taskType = com.example.data.sync.CatalogBackgroundSyncWorker.TASK_TYPE_DYNAMIC_PRICING,
            productId = productId,
            title = "डायनामिक प्राइसिंग एआई गणना (Dynamic Pricing AI)",
            inputPayloadJson = payload,
            status = "PENDING"
        )
        val taskId = queuedAiTaskDao?.insert(task) ?: 0L
        context?.let { ctx ->
            com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                ctx,
                com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED
            )
        }
        taskId
    }

    /**
     * Triggers immediate background synchronization via WorkManager.
     */
    fun triggerWorkManagerSync() {
        context?.let { ctx ->
            com.example.data.sync.WorkManagerSyncScheduler.enqueueImmediateSync(
                ctx,
                com.example.data.sync.CatalogBackgroundSyncWorker.TRIGGER_USER_MANUAL
            )
        }
    }

    suspend fun getProductById(id: Long): ProductEntity? = withContext(ioDispatcher) {
        // Fetch from local Room cache
        var local = productDao.getProductById(id)
        if (local == null) {
            // Attempt to fetch from remote API if not cached locally
            try {
                val response = apiService.getProductById(id)
                if (response.isSuccessful && response.body() != null) {
                    val entity = response.body()!!.toEntity()
                    productDao.insertProduct(entity)
                    local = entity
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch product $id from remote: ${e.message}")
            }
        }
        local
    }

    // ==========================================
    // INQUIRY CRUD WITH REMOTE SYNC & OFFLINE CACHE
    // ==========================================

    suspend fun saveInquiry(inquiry: BuyerInquiryEntity): Long = withContext(ioDispatcher) {
        // 1. Save locally in Room
        val localId = buyerInquiryDao.insertInquiry(inquiry)
        productDao.incrementInquiryCount(inquiry.productId)

        // Log activity event
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_INQUIRY_SENT,
            title = "नई पूछताछ आई (Inquiry Received): ${inquiry.buyerName}",
            description = "${inquiry.productTitle} • ${inquiry.requestedQuantity} pcs from ${inquiry.buyerCity}",
            category = "INQUIRY",
            metadata = mapOf("inquiryId" to localId.toString(), "buyer" to inquiry.buyerName)
        )

        // 2. Post inquiry to remote backend API
        try {
            val response = apiService.createInquiry(inquiry.copy(id = localId).toDto())
            if (response.isSuccessful) {
                Log.d(TAG, "Inquiry created on remote backend API")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Inquiry saved to Room cache; remote sync deferred: ${e.message}")
        }

        // 3. Multi-User Sync to Google Cloud Firestore
        val buyerUid = firebaseAuthManager?.getCurrentUserId() ?: "buyer_${inquiry.buyerPhone.replace(Regex("[^0-9]"), "")}"
        val artisanUid = "artisan_${_artisanProfile.value.phone.replace(Regex("[^0-9]"), "")}"
        try {
            firestoreService?.submitInquiry(inquiry.copy(id = localId), buyerUid, artisanUid)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore inquiry submit note: ${e.message}")
        }

        localId
    }

    suspend fun updateInquiryStatus(inquiryId: Long, status: String) = withContext(ioDispatcher) {
        // 1. Update local Room cache
        buyerInquiryDao.updateStatus(inquiryId, status)

        // Log activity event
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_INQUIRY_RESPONDED,
            title = "पूछताछ स्थिति बदली (Inquiry Status: $status)",
            description = "Inquiry #$inquiryId updated to $status",
            category = "INQUIRY",
            metadata = mapOf("inquiryId" to inquiryId.toString(), "status" to status)
        )

        // 2. Sync to remote API
        try {
            apiService.updateInquiryStatus(inquiryId, UpdateInquiryStatusRequest(status))
        } catch (e: Exception) {
            Log.w(TAG, "Inquiry status updated locally: ${e.message}")
        }

        // 3. Sync to Firestore
        try {
            firestoreService?.respondToInquiry(inquiryId, "", status)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore inquiry status update note: ${e.message}")
        }
    }

    suspend fun respondToInquiry(inquiryId: Long, status: String, responseText: String) = withContext(ioDispatcher) {
        // 1. Update local Room cache
        buyerInquiryDao.respondToInquiry(inquiryId, status, responseText)

        // Log activity event
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_INQUIRY_RESPONDED,
            title = "पूछताछ का उत्तर भेजा (Inquiry Responded)",
            description = "Response: \"${responseText.take(40)}...\"",
            category = "INQUIRY",
            metadata = mapOf("inquiryId" to inquiryId.toString())
        )

        // 2. Sync response to remote API
        try {
            apiService.respondToInquiry(inquiryId, RespondInquiryRequest(status = status, response = responseText))
        } catch (e: Exception) {
            Log.w(TAG, "Inquiry response updated locally: ${e.message}")
        }

        // 3. Sync response to Firestore
        try {
            firestoreService?.respondToInquiry(inquiryId, responseText, status)
        } catch (e: Exception) {
            Log.w(TAG, "Firestore inquiry response note: ${e.message}")
        }
    }

    suspend fun deleteInquiry(id: Long) = withContext(ioDispatcher) {
        // 1. Delete from Room cache
        buyerInquiryDao.deleteInquiryById(id)

        // 2. Delete on remote API
        try {
            apiService.deleteInquiry(id)
        } catch (e: Exception) {
            Log.w(TAG, "Inquiry deleted locally: ${e.message}")
        }
    }

    /**
     * Synchronize all unsynced offline events with the remote backend
     */
    suspend fun syncPendingActivityEvents(): Result<Int> = withContext(ioDispatcher) {
        if (eventTrackingService == null) {
            return@withContext Result.success(0)
        }
        val result = eventTrackingService.syncPendingEvents()
        if (result.isSuccess) {
            val count = result.getOrDefault(0)
            if (count > 0) {
                eventTrackingService.logEvent(
                    eventType = RoomEventTrackingService.EVENT_OFFLINE_SYNC,
                    title = "सिंक पूर्ण (Sync Completed)",
                    description = "$count offline activity events synchronized with server",
                    category = "SYSTEM"
                )
            }
        }
        result
    }

    // ==========================================
    // AI SERVICES
    // ==========================================

    suspend fun generateProductMetadata(
        request: ProductMetadataRequest,
        context: Context? = null
    ): Result<ProductStructuredMetadata> {
        val result = geminiMetadataService.generateMetadata(request, context)
        if (result.isSuccess) {
            val meta = result.getOrNull()
            eventTrackingService?.logEvent(
                eventType = RoomEventTrackingService.EVENT_CATALOG_GENERATED,
                title = "AI कैटलॉग विवरण तैयार (Catalog Generated)",
                description = "${meta?.category ?: request.category} • ${meta?.specificCraftType ?: ""}",
                category = "CATALOG"
            )
        }
        return result
    }

    suspend fun generateProductMetadataFromImageAndVoice(
        imageBitmap: Bitmap?,
        imageUri: String?,
        voiceDescription: String,
        category: String,
        rawMaterialCost: Double,
        laborHours: Double,
        language: String,
        context: Context? = null
    ): ProductStructuredMetadata {
        val currentProfile = _artisanProfile.value
        val result = geminiMetadataService.generateMetadataFromImageAndVoice(
            imageBitmap = imageBitmap,
            imageUri = imageUri,
            voiceDescription = voiceDescription,
            category = category,
            rawMaterialCost = rawMaterialCost,
            laborHours = laborHours,
            language = language,
            artisanName = currentProfile.name,
            clusterLocation = currentProfile.villageOrCluster,
            state = currentProfile.state,
            context = context
        )
        eventTrackingService?.logEvent(
            eventType = RoomEventTrackingService.EVENT_VOICE_NOTE_RECORDED,
            title = "वॉयस व इमेज विवरण तैयार (Voice & Image Processed)",
            description = "${result.title} • $category • ₹${result.retailPrice.toInt()}",
            category = "VOICE"
        )
        return result
    }

    suspend fun calculateDynamicPrice(
        request: DynamicPricingRequest
    ): Result<DynamicPricingResponse> {
        val result = dynamicPricingService.calculateDynamicPrice(request)
        if (result.isSuccess) {
            val pricing = result.getOrNull()
            eventTrackingService?.logEvent(
                eventType = RoomEventTrackingService.EVENT_PRICING_CALCULATED,
                title = "उचित मूल्य गणना (Fair Pricing Calculated)",
                description = "${request.category} • Min ₹${pricing?.minFairPrice?.toInt()} - Max ₹${pricing?.maxFairPrice?.toInt()} • Retail ₹${pricing?.optimalRetailPrice?.toInt()}",
                category = "PRICING"
            )
        }
        return result
    }

    fun updateProfile(profile: ArtisanProfile) {
        _artisanProfile.value = profile
    }
}
