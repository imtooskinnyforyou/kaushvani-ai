package com.example.data.db

import androidx.room.*
import com.example.data.model.CatalogSyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogSyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CatalogSyncQueueEntity): Long

    @Query("SELECT * FROM catalog_sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<CatalogSyncQueueEntity>

    @Query("SELECT COUNT(*) FROM catalog_sync_queue WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM catalog_sync_queue WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT * FROM catalog_sync_queue WHERE productId = :productId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestItemForProduct(productId: Long): CatalogSyncQueueEntity?

    @Update
    suspend fun update(item: CatalogSyncQueueEntity)

    @Query("UPDATE catalog_sync_queue SET status = :status, errorMessage = :error, lastAttemptTimestamp = :timestamp, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, error: String?, timestamp: Long)

    @Query("DELETE FROM catalog_sync_queue WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM catalog_sync_queue WHERE status = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("SELECT * FROM catalog_sync_queue ORDER BY createdAt DESC")
    fun observeAllQueue(): Flow<List<CatalogSyncQueueEntity>>
}
