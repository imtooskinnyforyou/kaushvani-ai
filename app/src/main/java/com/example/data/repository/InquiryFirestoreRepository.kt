package com.example.data.repository

import android.util.Log
import com.example.data.db.BuyerInquiryDao
import com.example.data.db.ProductDao
import com.example.data.firebase.FirestoreService
import com.example.data.model.BuyerInquiryEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 * InquiryFirestoreRepository defines the cloud-synchronized repository contract
 * for buyer inquiries, negotiations, and artisan orders in KAARIGAR.
 */
interface InquiryFirestoreRepository {
    /**
     * Real-time stream of inquiries relevant to a user (as artisan recipient or buyer sender).
     */
    fun observeInquiriesForUser(userId: String, isArtisan: Boolean = true): Flow<List<BuyerInquiryEntity>>

    /**
     * Real-time stream of inquiries for a specific handcrafted item.
     */
    fun observeInquiriesForProduct(productId: Long): Flow<List<BuyerInquiryEntity>>

    /**
     * Real-time stream of a single inquiry by its ID.
     */
    fun observeInquiryById(inquiryId: Long): Flow<BuyerInquiryEntity?>

    /**
     * Real-time stream of local inquiries from Room database.
     */
    fun observeLocalInquiries(): Flow<List<BuyerInquiryEntity>>

    /**
     * Submit a new buyer inquiry or wholesale RFQ to Room and Firestore.
     */
    suspend fun submitInquiry(
        inquiry: BuyerInquiryEntity,
        buyerId: String? = null,
        artisanId: String? = null
    ): Result<Long>

    /**
     * Submit an artisan response and update inquiry state in Room and Firestore.
     */
    suspend fun respondToInquiry(
        inquiryId: Long,
        responseText: String,
        status: String = "RESPONDED"
    ): Result<Unit>

    /**
     * Update inquiry status (NEW, RESPONDED, ACCEPTED, NEGOTIATING, REJECTED, FULFILLED).
     */
    suspend fun updateInquiryStatus(inquiryId: Long, status: String): Result<Unit>

    /**
     * Delete an inquiry from Room and Firestore.
     */
    suspend fun deleteInquiry(inquiryId: Long): Result<Unit>

    /**
     * Fetch single inquiry details (checking local Room cache first, then Firestore).
     */
    suspend fun getInquiryById(inquiryId: Long): Result<BuyerInquiryEntity?>
}

/**
 * Default implementation of [InquiryFirestoreRepository] providing real-time cloud synchronization
 * with local SQLite / Room offline cache.
 */
class InquiryFirestoreRepositoryImpl(
    private val firestoreService: FirestoreService,
    private val buyerInquiryDao: BuyerInquiryDao,
    private val productDao: ProductDao? = null,
    private val authRepository: AuthRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : InquiryFirestoreRepository {

    companion object {
        private const val TAG = "InquiryFirestoreRepo"
    }

    override fun observeInquiriesForUser(userId: String, isArtisan: Boolean): Flow<List<BuyerInquiryEntity>> {
        return firestoreService.observeInquiriesForUser(userId, isArtisan)
            .onEach { remoteList ->
                if (remoteList.isNotEmpty()) {
                    withContext(ioDispatcher) {
                        try {
                            buyerInquiryDao.insertInquiries(remoteList)
                            Log.d(TAG, "Cached ${remoteList.size} real-time Firestore inquiries in Room.")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed caching Firestore inquiries to Room: ${e.message}")
                        }
                    }
                }
            }
            .catch { e ->
                Log.w(TAG, "Firestore inquiry stream error, falling back to Room: ${e.message}")
                emit(buyerInquiryDao.getAllInquiriesList())
            }
            .flowOn(ioDispatcher)
    }

    override fun observeInquiriesForProduct(productId: Long): Flow<List<BuyerInquiryEntity>> {
        return firestoreService.observeInquiriesForProduct(productId)
            .flowOn(ioDispatcher)
    }

    override fun observeInquiryById(inquiryId: Long): Flow<BuyerInquiryEntity?> {
        return firestoreService.observeInquiryById(inquiryId)
            .flowOn(ioDispatcher)
    }

    override fun observeLocalInquiries(): Flow<List<BuyerInquiryEntity>> {
        return buyerInquiryDao.getAllInquiries()
            .flowOn(ioDispatcher)
    }

    override suspend fun submitInquiry(
        inquiry: BuyerInquiryEntity,
        buyerId: String?,
        artisanId: String?
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            // 1. Insert into local Room database
            val localId = buyerInquiryDao.insertInquiry(inquiry)
            val updatedInquiry = inquiry.copy(id = localId)

            // Increment inquiry count on local product
            productDao?.incrementInquiryCount(inquiry.productId)

            // 2. Transmit to Cloud Firestore
            val resolvedBuyerId = buyerId
                ?: authRepository?.getCurrentUserId()
                ?: "buyer_${inquiry.buyerPhone.replace(Regex("[^0-9]"), "")}"

            val resolvedArtisanId = artisanId ?: "artisan_demo_user"

            val cloudResult = firestoreService.submitInquiry(
                inquiry = updatedInquiry,
                buyerId = resolvedBuyerId,
                artisanId = resolvedArtisanId
            )
            if (cloudResult.isFailure) {
                Log.w(TAG, "Inquiry saved to Room locally; cloud sync pending: ${cloudResult.exceptionOrNull()?.message}")
            }

            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting inquiry: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun respondToInquiry(
        inquiryId: Long,
        responseText: String,
        status: String
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Update Room DB
            buyerInquiryDao.respondToInquiry(inquiryId, status, responseText)

            // 2. Update Cloud Firestore
            firestoreService.respondToInquiry(inquiryId, responseText, status)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to inquiry $inquiryId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateInquiryStatus(inquiryId: Long, status: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Update Room DB
            buyerInquiryDao.updateStatus(inquiryId, status)

            // 2. Update Cloud Firestore
            firestoreService.updateInquiryStatus(inquiryId, status)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inquiry status: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteInquiry(inquiryId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Delete from Room DB
            buyerInquiryDao.deleteInquiryById(inquiryId)

            // 2. Delete from Cloud Firestore
            firestoreService.deleteInquiry(inquiryId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting inquiry $inquiryId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getInquiryById(inquiryId: Long): Result<BuyerInquiryEntity?> = withContext(ioDispatcher) {
        try {
            val localList = buyerInquiryDao.getAllInquiriesList()
            val local = localList.find { it.id == inquiryId }
            if (local != null) {
                return@withContext Result.success(local)
            }

            val cloudResult = firestoreService.getInquiryById(inquiryId)
            val cloudInquiry = cloudResult.getOrNull()
            if (cloudInquiry != null) {
                buyerInquiryDao.insertInquiry(cloudInquiry)
            }
            Result.success(cloudInquiry)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting inquiry $inquiryId: ${e.message}", e)
            Result.failure(e)
        }
    }
}
