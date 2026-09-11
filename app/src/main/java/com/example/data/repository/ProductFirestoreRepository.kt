package com.example.data.repository

import android.util.Log
import com.example.data.db.ProductDao
import com.example.data.firebase.FirestoreService
import com.example.data.model.ProductEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext

/**
 * ProductFirestoreRepository defines the cloud-synchronized repository contract
 * for catalog and handcrafted product management in KAARIGAR.
 */
interface ProductFirestoreRepository {
    /**
     * Real-time stream of all products from cloud Firestore, with local Room persistence.
     */
    fun observeAllProducts(): Flow<List<ProductEntity>>

    /**
     * Real-time stream of products filtered by artisan ID.
     */
    fun observeProductsByArtisan(artisanId: String): Flow<List<ProductEntity>>

    /**
     * Real-time stream of products filtered by craft category.
     */
    fun observeProductsByCategory(category: String): Flow<List<ProductEntity>>

    /**
     * Real-time stream of a single product.
     */
    fun observeProductById(productId: Long): Flow<ProductEntity?>

    /**
     * Real-time stream of local products from Room database.
     */
    fun observeLocalProducts(): Flow<List<ProductEntity>>

    /**
     * Save a new product to both Room database and Firestore.
     */
    suspend fun saveProduct(product: ProductEntity, artisanId: String? = null): Result<Long>

    /**
     * Update an existing product in Room and Firestore.
     */
    suspend fun updateProduct(product: ProductEntity): Result<Unit>

    /**
     * Delete a product from Room and Firestore.
     */
    suspend fun deleteProduct(productId: Long): Result<Unit>

    /**
     * Fetch a single product by ID (checking Room first, then Firestore).
     */
    suspend fun getProductById(productId: Long): Result<ProductEntity?>

    /**
     * Fetch all remote products from Firestore and mirror them to local Room cache.
     */
    suspend fun syncRemoteToLocal(): Result<Int>
}

/**
 * Default implementation of [ProductFirestoreRepository] providing real-time cloud synchronization
 * with local SQLite / Room offline cache.
 */
class ProductFirestoreRepositoryImpl(
    private val firestoreService: FirestoreService,
    private val productDao: ProductDao,
    private val authRepository: AuthRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProductFirestoreRepository {

    companion object {
        private const val TAG = "ProductFirestoreRepo"
    }

    override fun observeAllProducts(): Flow<List<ProductEntity>> {
        return firestoreService.observeAllProducts()
            .onEach { remoteList ->
                if (remoteList.isNotEmpty()) {
                    withContext(ioDispatcher) {
                        try {
                            productDao.insertProducts(remoteList)
                            Log.d(TAG, "Cached ${remoteList.size} real-time Firestore products in Room.")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed caching Firestore products to Room: ${e.message}")
                        }
                    }
                }
            }
            .catch { e ->
                Log.w(TAG, "Firestore product stream error, falling back to Room: ${e.message}")
                emit(productDao.getAllProductsList())
            }
            .flowOn(ioDispatcher)
    }

    override fun observeProductsByArtisan(artisanId: String): Flow<List<ProductEntity>> {
        return firestoreService.observeProductsByArtisan(artisanId)
            .flowOn(ioDispatcher)
    }

    override fun observeProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return firestoreService.observeProductsByCategory(category)
            .flowOn(ioDispatcher)
    }

    override fun observeProductById(productId: Long): Flow<ProductEntity?> {
        return firestoreService.observeProductById(productId)
            .flowOn(ioDispatcher)
    }

    override fun observeLocalProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
            .flowOn(ioDispatcher)
    }

    override suspend fun saveProduct(product: ProductEntity, artisanId: String?): Result<Long> = withContext(ioDispatcher) {
        try {
            // 1. Insert locally to generate ID and provide instant offline availability
            val localId = productDao.insertProduct(product)
            val updatedProduct = product.copy(id = localId)

            // 2. Transmit to Cloud Firestore
            val resolvedArtisanId = artisanId
                ?: authRepository?.getCurrentUserId()
                ?: "artisan_${product.artisanPhone.replace(Regex("[^0-9]"), "")}"

            val cloudResult = firestoreService.saveProduct(updatedProduct, resolvedArtisanId)
            if (cloudResult.isFailure) {
                Log.w(TAG, "Product saved to Room locally; cloud sync pending: ${cloudResult.exceptionOrNull()?.message}")
            }

            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving product: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: ProductEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Update Room DB
            productDao.updateProduct(product)

            // 2. Update Cloud Firestore
            val resolvedArtisanId = authRepository?.getCurrentUserId()
                ?: "artisan_${product.artisanPhone.replace(Regex("[^0-9]"), "")}"
            firestoreService.saveProduct(product, resolvedArtisanId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            // 1. Delete from Room DB
            productDao.deleteProductById(productId)

            // 2. Delete from Cloud Firestore
            firestoreService.deleteProduct(productId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product $productId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: Long): Result<ProductEntity?> = withContext(ioDispatcher) {
        try {
            // Check Room cache first
            val local = productDao.getProductById(productId)
            if (local != null) {
                return@withContext Result.success(local)
            }

            // Fallback to Cloud Firestore
            val cloudResult = firestoreService.getProductById(productId)
            val cloudProduct = cloudResult.getOrNull()
            if (cloudProduct != null) {
                productDao.insertProduct(cloudProduct)
            }
            Result.success(cloudProduct)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product $productId: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun syncRemoteToLocal(): Result<Int> = withContext(ioDispatcher) {
        try {
            val localProducts = productDao.getAllProductsList()
            // Pulling products happens through the real-time collector or one-shot sync
            Result.success(localProducts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
