package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProductHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductHistoryDao {

    @Query("SELECT * FROM product_history ORDER BY recordedDate DESC")
    fun getAllHistory(): Flow<List<ProductHistoryEntity>>

    @Query("SELECT * FROM product_history WHERE productId = :productId ORDER BY recordedDate DESC")
    fun getHistoryForProduct(productId: Long): Flow<List<ProductHistoryEntity>>

    @Query("SELECT * FROM product_history WHERE productId = :productId ORDER BY recordedDate DESC")
    suspend fun getHistoryForProductSync(productId: Long): List<ProductHistoryEntity>

    @Query("SELECT * FROM product_history WHERE category = :category ORDER BY recordedDate DESC")
    fun getHistoryByCategory(category: String): Flow<List<ProductHistoryEntity>>

    @Query("SELECT * FROM product_history WHERE category = :category ORDER BY recordedDate DESC")
    suspend fun getHistoryByCategorySync(category: String): List<ProductHistoryEntity>

    @Query("SELECT * FROM product_history ORDER BY recordedDate DESC")
    suspend fun getAllHistorySync(): List<ProductHistoryEntity>

    @Query("SELECT AVG(materialCost) FROM product_history WHERE productId = :productId")
    suspend fun getAverageMaterialCost(productId: Long): Double?

    @Query("SELECT AVG(sellingPrice) FROM product_history WHERE productId = :productId")
    suspend fun getAverageSellingPrice(productId: Long): Double?

    @Query("SELECT SUM(unitsSold) FROM product_history WHERE productId = :productId")
    suspend fun getTotalUnitsSold(productId: Long): Int?

    @Query("SELECT MIN(sellingPrice) FROM product_history WHERE productId = :productId")
    suspend fun getMinSellingPrice(productId: Long): Double?

    @Query("SELECT MAX(sellingPrice) FROM product_history WHERE productId = :productId")
    suspend fun getMaxSellingPrice(productId: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ProductHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<ProductHistoryEntity>)

    @Query("DELETE FROM product_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM product_history WHERE productId = :productId")
    suspend fun deleteHistoryForProduct(productId: Long)

    @Query("SELECT COUNT(*) FROM product_history")
    suspend fun getHistoryCount(): Int
}
