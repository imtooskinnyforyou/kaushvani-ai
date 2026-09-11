package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ArtisanProfile
import com.example.data.model.BuyerInquiryEntity
import com.example.data.model.ProductEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestoreService: Handles multi-user cloud document storage, real-time sync listeners,
 * and CRUD operations across Products, Buyer Inquiries, and User Profiles using Google Cloud Firestore.
 */
class FirestoreService(
    private val context: Context,
    private val firestoreInstance: FirebaseFirestore? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_INQUIRIES = "inquiries"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_EVENTS = "activity_events"
    }

    private val firestore: FirebaseFirestore?
        get() = firestoreInstance ?: FirebaseInitializer.getFirestoreInstance()

    init {
        FirebaseInitializer.initialize(context)
    }

    // ============================================================================
    // REAL-TIME OBSERVERS (Flows)
    // ============================================================================

    /**
     * Real-time stream of all products across all artisans in the multi-user catalog.
     */
    fun observeAllProducts(): Flow<List<ProductEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            Log.w(TAG, "Firestore not available, sending empty products list.")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_PRODUCTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Products snapshot listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToProductEntity(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing product doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(products)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of products by a specific artisan.
     */
    fun observeProductsByArtisan(artisanId: String): Flow<List<ProductEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo("artisanId", artisanId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Artisan products listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToProductEntity(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                    trySend(products)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of products for a category.
     */
    fun observeProductsByCategory(category: String): Flow<List<ProductEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_PRODUCTS)
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Category products listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToProductEntity(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                    trySend(products)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of a single product by ID.
     */
    fun observeProductById(productId: Long): Flow<ProductEntity?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_PRODUCTS)
            .document(productId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Product detail listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val product = try {
                        docToProductEntity(snapshot.id, snapshot.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                    trySend(product)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of all inquiries across the platform from Firestore.
     */
    fun observeAllInquiries(): Flow<List<BuyerInquiryEntity>> = observeInquiriesForUser("", true)

    /**
     * Real-time stream of inquiries relevant to the current user (as artisan or buyer).
     */
    fun observeInquiriesForUser(userId: String, isArtisan: Boolean = true): Flow<List<BuyerInquiryEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            Log.w(TAG, "Firestore not available, sending empty inquiries list.")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = if (userId.isNotBlank()) {
            val field = if (isArtisan) "artisanId" else "buyerId"
            db.collection(COLLECTION_INQUIRIES)
                .whereEqualTo(field, userId)
        } else {
            db.collection(COLLECTION_INQUIRIES)
        }

        val registration = query
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Inquiries snapshot listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val inquiries = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToInquiryEntity(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing inquiry doc ${doc.id}: ${e.message}")
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                    trySend(inquiries)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of inquiries for a specific product.
     */
    fun observeInquiriesForProduct(productId: Long): Flow<List<BuyerInquiryEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_INQUIRIES)
            .whereEqualTo("productId", productId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Product inquiries listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val inquiries = snapshot.documents.mapNotNull { doc ->
                        try {
                            docToInquiryEntity(doc.id, doc.data ?: emptyMap())
                        } catch (e: Exception) {
                            null
                        }
                    }.sortedByDescending { it.timestamp }
                    trySend(inquiries)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of a single inquiry by ID.
     */
    fun observeInquiryById(inquiryId: Long): Flow<BuyerInquiryEntity?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_INQUIRIES)
            .document(inquiryId.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Inquiry detail listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val inquiry = try {
                        docToInquiryEntity(snapshot.id, snapshot.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                    trySend(inquiry)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    // ============================================================================
    // PRODUCT OPERATIONS
    // ============================================================================

    /**
     * Upload or update a product document in Firestore.
     */
    suspend fun saveProduct(product: ProductEntity, userId: String = "artisan_demo_user"): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val docId = product.id.toString()
            val data = hashMapOf(
                "id" to product.id,
                "title" to product.title,
                "regionalTitle" to product.regionalTitle,
                "description" to product.description,
                "regionalDescription" to product.regionalDescription,
                "category" to product.category,
                "craftType" to product.craftType,
                "region" to product.region,
                "rawMaterialCost" to product.rawMaterialCost,
                "laborHours" to product.laborHours,
                "hourlyWageRate" to product.hourlyWageRate,
                "packagingCost" to product.packagingCost,
                "fairMinPrice" to product.fairMinPrice,
                "fairMaxPrice" to product.fairMaxPrice,
                "retailPrice" to product.retailPrice,
                "wholesalePrice" to product.wholesalePrice,
                "minOrderQuantity" to product.minOrderQuantity,
                "stockAvailable" to product.stockAvailable,
                "imageUri" to product.imageUri,
                "imageStyleFilter" to product.imageStyleFilter,
                "voiceTranscript" to product.voiceTranscript,
                "originalLanguage" to product.originalLanguage,
                "isGiTagged" to product.isGiTagged,
                "tags" to product.tags,
                "materialsUsed" to product.materialsUsed,
                "dimensions" to product.dimensions,
                "weightKg" to product.weightKg,
                "careInstructions" to product.careInstructions,
                "artisanName" to product.artisanName,
                "artisanLocation" to product.artisanLocation,
                "artisanPhone" to product.artisanPhone,
                "status" to product.status,
                "viewCount" to product.viewCount,
                "inquiryCount" to product.inquiryCount,
                "timestamp" to product.timestamp,
                "artisanId" to userId,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_PRODUCTS)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            Log.i(TAG, "Product ${product.id} successfully saved to Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch a single product by ID from Firestore.
     */
    suspend fun getProductById(productId: Long): Result<ProductEntity?> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val doc = db.collection(COLLECTION_PRODUCTS)
                .document(productId.toString())
                .get()
                .await()
            if (doc.exists()) {
                val entity = docToProductEntity(doc.id, doc.data ?: emptyMap())
                Result.success(entity)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product $productId from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a product document from Firestore.
     */
    suspend fun deleteProduct(productId: Long): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            db.collection(COLLECTION_PRODUCTS)
                .document(productId.toString())
                .delete()
                .await()
            Log.i(TAG, "Product $productId deleted from Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================================
    // INQUIRY OPERATIONS
    // ============================================================================

    /**
     * Submit a buyer inquiry to Firestore.
     */
    suspend fun submitInquiry(
        inquiry: BuyerInquiryEntity,
        buyerId: String = "buyer_demo_user",
        artisanId: String = "artisan_demo_user"
    ): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))

        try {
            val docId = inquiry.id.toString()
            val data = hashMapOf(
                "id" to inquiry.id,
                "productId" to inquiry.productId,
                "productTitle" to inquiry.productTitle,
                "buyerName" to inquiry.buyerName,
                "buyerCompany" to inquiry.buyerCompany,
                "buyerType" to inquiry.buyerType,
                "requestedQuantity" to inquiry.requestedQuantity,
                "offeredPricePerUnit" to inquiry.offeredPricePerUnit,
                "message" to inquiry.message,
                "buyerPhone" to inquiry.buyerPhone,
                "buyerEmail" to inquiry.buyerEmail,
                "buyerCity" to inquiry.buyerCity,
                "requiredDate" to inquiry.requiredDate,
                "status" to inquiry.status,
                "artisanResponse" to inquiry.artisanResponse,
                "notes" to inquiry.notes,
                "timestamp" to inquiry.timestamp,
                "buyerId" to buyerId,
                "artisanId" to artisanId,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_INQUIRIES)
                .document(docId)
                .set(data, SetOptions.merge())
                .await()

            Log.i(TAG, "Inquiry ${inquiry.id} submitted to Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting inquiry to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update inquiry response & status in Firestore.
     */
    suspend fun respondToInquiry(
        inquiryId: Long,
        responseText: String,
        newStatus: String = "RESPONDED"
    ): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val updates = mapOf(
                "artisanResponse" to responseText,
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_INQUIRIES)
                .document(inquiryId.toString())
                .update(updates)
                .await()
            Log.i(TAG, "Inquiry $inquiryId updated in Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inquiry response in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update only inquiry status in Firestore.
     */
    suspend fun updateInquiryStatus(
        inquiryId: Long,
        newStatus: String
    ): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val updates = mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_INQUIRIES)
                .document(inquiryId.toString())
                .update(updates)
                .await()
            Log.i(TAG, "Inquiry $inquiryId status updated to $newStatus in Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inquiry status in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch a single inquiry by ID from Firestore.
     */
    suspend fun getInquiryById(inquiryId: Long): Result<BuyerInquiryEntity?> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val doc = db.collection(COLLECTION_INQUIRIES)
                .document(inquiryId.toString())
                .get()
                .await()
            if (doc.exists()) {
                val entity = docToInquiryEntity(doc.id, doc.data ?: emptyMap())
                Result.success(entity)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching inquiry $inquiryId from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete an inquiry from Firestore.
     */
    suspend fun deleteInquiry(inquiryId: Long): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            db.collection(COLLECTION_INQUIRIES)
                .document(inquiryId.toString())
                .delete()
                .await()
            Log.i(TAG, "Inquiry $inquiryId deleted from Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting inquiry from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================================
    // USER PROFILE & REPUTATION
    // ============================================================================

    /**
     * Real-time stream of a specific user/artisan profile from Firestore.
     */
    fun observeUserProfile(userId: String): Flow<ArtisanProfile?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to user profile $userId: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = docToArtisanProfile(snapshot.data ?: emptyMap())
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Real-time stream of all registered artisans across the platform.
     */
    fun observeAllArtisans(): Flow<List<ArtisanProfile>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(COLLECTION_USERS)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to artisans list: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val list = snapshots.documents.mapNotNull { doc ->
                        doc.data?.let { docToArtisanProfile(it) }
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }.flowOn(ioDispatcher)

    /**
     * Get a user profile snapshot from Firestore.
     */
    suspend fun getUserProfile(userId: String): Result<ArtisanProfile?> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val doc = db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            if (doc.exists()) {
                val profile = docToArtisanProfile(doc.data ?: emptyMap())
                Result.success(profile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: ArtisanProfile, userId: String): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val data = hashMapOf(
                "name" to profile.name,
                "craftSpecialty" to profile.craftSpecialty,
                "experienceYears" to profile.experienceYears,
                "villageOrCluster" to profile.villageOrCluster,
                "state" to profile.state,
                "phone" to profile.phone,
                "upiId" to profile.upiId,
                "artisanCardNo" to profile.artisanCardNo,
                "isVishwakarmaEnrolled" to profile.isVishwakarmaEnrolled,
                "totalSalesCount" to profile.totalSalesCount,
                "totalRevenue" to profile.totalRevenue,
                "preferredLanguage" to profile.preferredLanguage,
                "localePreference" to profile.localePreference,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
            Log.i(TAG, "User profile saved in Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Update regional language preference and SpeechRecognizer locale in Firestore user document.
     */
    suspend fun updateUserLanguagePreference(
        userId: String,
        languageCode: String,
        languageName: String,
        localeTag: String,
        nativeName: String
    ): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            val updates = mapOf(
                "preferredLanguage" to languageName,
                "languageCode" to languageCode,
                "localePreference" to localeTag,
                "localeTag" to localeTag,
                "nativeLanguage" to nativeName,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS)
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()
            Log.i(TAG, "User language preference updated in Firestore for user: $userId (Locale: $localeTag)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user language preference in Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a user profile from Firestore.
     */
    suspend fun deleteUserProfile(userId: String): Result<Unit> = withContext(ioDispatcher) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available."))
        try {
            db.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .await()
            Log.i(TAG, "User profile deleted from Firestore for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user profile from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ============================================================================
    // MAPPERS
    // ============================================================================

    private fun docToProductEntity(idStr: String, data: Map<String, Any>): ProductEntity {
        val id = (data["id"] as? Number)?.toLong() ?: idStr.toLongOrNull() ?: System.currentTimeMillis()
        val retailPrice = (data["retailPrice"] as? Number)?.toDouble() ?: 0.0
        val wholesalePrice = (data["wholesalePrice"] as? Number)?.toDouble() ?: retailPrice

        return ProductEntity(
            id = id,
            title = data["title"] as? String ?: "Handcrafted Item",
            regionalTitle = data["regionalTitle"] as? String ?: "",
            description = data["description"] as? String ?: "",
            regionalDescription = data["regionalDescription"] as? String ?: "",
            category = data["category"] as? String ?: "Pottery",
            craftType = data["craftType"] as? String ?: "Terracotta",
            region = data["region"] as? String ?: "Gorakhpur (UP)",
            rawMaterialCost = (data["rawMaterialCost"] as? Number)?.toDouble() ?: 0.0,
            laborHours = (data["laborHours"] as? Number)?.toDouble() ?: 0.0,
            hourlyWageRate = (data["hourlyWageRate"] as? Number)?.toDouble() ?: 150.0,
            packagingCost = (data["packagingCost"] as? Number)?.toDouble() ?: 50.0,
            fairMinPrice = (data["fairMinPrice"] as? Number)?.toDouble() ?: 0.0,
            fairMaxPrice = (data["fairMaxPrice"] as? Number)?.toDouble() ?: 0.0,
            retailPrice = retailPrice,
            wholesalePrice = wholesalePrice,
            minOrderQuantity = (data["minOrderQuantity"] as? Number)?.toInt() ?: 1,
            stockAvailable = (data["stockAvailable"] as? Number)?.toInt() ?: 10,
            imageUri = data["imageUri"] as? String ?: "",
            imageStyleFilter = data["imageStyleFilter"] as? String ?: "Natural Studio",
            voiceTranscript = data["voiceTranscript"] as? String ?: "",
            originalLanguage = data["originalLanguage"] as? String ?: "Hindi",
            isGiTagged = data["isGiTagged"] as? Boolean ?: false,
            tags = data["tags"] as? String ?: "",
            materialsUsed = data["materialsUsed"] as? String ?: "",
            dimensions = data["dimensions"] as? String ?: "",
            weightKg = (data["weightKg"] as? Number)?.toDouble() ?: 0.5,
            careInstructions = data["careInstructions"] as? String ?: "",
            artisanName = data["artisanName"] as? String ?: "Ram Prasad Prajapati",
            artisanLocation = data["artisanLocation"] as? String ?: "Gorakhpur, Uttar Pradesh",
            artisanPhone = data["artisanPhone"] as? String ?: "+91 98765 43210",
            status = data["status"] as? String ?: "PUBLISHED",
            viewCount = (data["viewCount"] as? Number)?.toInt() ?: 24,
            inquiryCount = (data["inquiryCount"] as? Number)?.toInt() ?: 3,
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun docToInquiryEntity(idStr: String, data: Map<String, Any>): BuyerInquiryEntity {
        val id = (data["id"] as? Number)?.toLong() ?: idStr.toLongOrNull() ?: System.currentTimeMillis()
        val productId = (data["productId"] as? Number)?.toLong() ?: 0L
        val offeredPrice = (data["offeredPricePerUnit"] as? Number)?.toDouble() ?: 0.0
        val requestedQty = (data["requestedQuantity"] as? Number)?.toInt() ?: 1

        return BuyerInquiryEntity(
            id = id,
            productId = productId,
            productTitle = data["productTitle"] as? String ?: "Product Inquiry",
            buyerName = data["buyerName"] as? String ?: "Buyer",
            buyerCompany = data["buyerCompany"] as? String ?: "",
            buyerType = data["buyerType"] as? String ?: "Wholesale Boutique",
            requestedQuantity = requestedQty,
            offeredPricePerUnit = offeredPrice,
            message = data["message"] as? String ?: "",
            buyerPhone = data["buyerPhone"] as? String ?: "+91 94123 78901",
            buyerEmail = data["buyerEmail"] as? String ?: "buyer@craftstore.in",
            buyerCity = data["buyerCity"] as? String ?: "Mumbai, Maharashtra",
            requiredDate = data["requiredDate"] as? String ?: "Within 2-3 weeks",
            status = data["status"] as? String ?: "NEW",
            artisanResponse = data["artisanResponse"] as? String ?: "",
            notes = data["notes"] as? String ?: "",
            timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    private fun docToArtisanProfile(data: Map<String, Any>): ArtisanProfile {
        return ArtisanProfile(
            name = data["name"] as? String ?: "Ram Prasad Prajapati",
            craftSpecialty = data["craftSpecialty"] as? String ?: "Traditional Terracotta & Pottery",
            experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 18,
            villageOrCluster = data["villageOrCluster"] as? String ?: "Bhiti Rawat, Gorakhpur",
            state = data["state"] as? String ?: "Uttar Pradesh",
            phone = data["phone"] as? String ?: "+91 98765 43210",
            upiId = data["upiId"] as? String ?: "ramprasad.artisan@upi",
            artisanCardNo = data["artisanCardNo"] as? String ?: "PMV-UP-2024-8849",
            isVishwakarmaEnrolled = data["isVishwakarmaEnrolled"] as? Boolean ?: true,
            totalSalesCount = (data["totalSalesCount"] as? Number)?.toInt() ?: 142,
            totalRevenue = (data["totalRevenue"] as? Number)?.toDouble() ?: 184500.0,
            preferredLanguage = data["preferredLanguage"] as? String ?: "Hindi",
            localePreference = data["localePreference"] as? String ?: "hi-IN"
        )
    }
}
