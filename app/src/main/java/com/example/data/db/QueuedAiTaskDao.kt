package com.example.data.db

import androidx.room.*
import com.example.data.model.QueuedAiTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueuedAiTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: QueuedAiTaskEntity): Long

    @Query("SELECT * FROM queued_ai_tasks WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingTasks(): List<QueuedAiTaskEntity>

    @Query("SELECT COUNT(*) FROM queued_ai_tasks WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM queued_ai_tasks WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Query("SELECT * FROM queued_ai_tasks WHERE productId = :productId ORDER BY createdAt DESC")
    fun observeTasksForProduct(productId: Long): Flow<List<QueuedAiTaskEntity>>

    @Query("SELECT * FROM queued_ai_tasks WHERE productId = :productId ORDER BY createdAt DESC")
    suspend fun getTasksForProduct(productId: Long): List<QueuedAiTaskEntity>

    @Update
    suspend fun update(task: QueuedAiTaskEntity)

    @Query("UPDATE queued_ai_tasks SET status = :status, resultPayloadJson = :result, errorMessage = :error, lastAttemptTimestamp = :timestamp, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, result: String?, error: String?, timestamp: Long)

    @Query("DELETE FROM queued_ai_tasks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM queued_ai_tasks WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    @Query("SELECT * FROM queued_ai_tasks ORDER BY createdAt DESC")
    fun observeAllTasks(): Flow<List<QueuedAiTaskEntity>>
}
