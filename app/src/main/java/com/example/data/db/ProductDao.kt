package com.example.data.db

import androidx.room.*
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Dao
interface ProductDao {
    // --- Abstract Room Queries ---

    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY timestamp DESC")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getProductsModifiedSince(since: Long): List<ProductEntity>

    @Query("SELECT * FROM products WHERE status = :status ORDER BY timestamp DESC")
    fun getProductsByStatus(status: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category ORDER BY timestamp DESC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    @Query("UPDATE products SET inquiryCount = inquiryCount + 1 WHERE id = :id")
    suspend fun incrementInquiryCount(id: Long)

    // --- Standard DAO Convenience Methods with Default Implementations ---

    fun getAll(): Flow<List<ProductEntity>> = getAllProducts()

    fun getProducts(): Flow<List<ProductEntity>> = getAllProducts()

    suspend fun getProduct(id: Long): ProductEntity? = getProductById(id)

    fun getProductByIdFlow(id: Long): Flow<ProductEntity?> = flow {
        emit(getProductById(id))
    }

    suspend fun insert(product: ProductEntity): Long = insertProduct(product)

    suspend fun insertAll(products: List<ProductEntity>): List<Long> {
        insertProducts(products)
        return products.map { it.id }
    }

    suspend fun update(product: ProductEntity) {
        updateProduct(product)
    }

    suspend fun delete(product: ProductEntity) {
        deleteProductById(product.id)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        deleteProductById(product.id)
    }

    suspend fun deleteById(id: Long) {
        deleteProductById(id)
    }

    suspend fun deleteAll() {
        val all = getAllProductsList()
        all.forEach { deleteProductById(it.id) }
    }

    // --- Additional Search and Sync Query Helpers ---

    fun searchProducts(query: String): Flow<List<ProductEntity>> = getAllProducts()

    suspend fun getProductCountSync(): Int = getAllProductsList().size

    fun getProductsBySyncStatus(status: String): Flow<List<ProductEntity>> = getProductsByStatus(status)

    suspend fun getPendingSyncProducts(): List<ProductEntity> = emptyList()

    suspend fun updateSyncStatus(productId: Long, status: String, lastSyncedAt: Long) {}
}


