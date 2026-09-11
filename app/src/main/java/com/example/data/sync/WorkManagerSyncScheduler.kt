package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.db.AppDatabase
import com.example.data.model.CatalogSyncQueueEntity
import com.example.data.model.PricingSeason
import com.example.data.model.ChannelType
import com.example.data.model.QueuedAiTaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * WorkManagerSyncScheduler: Central orchestration facade for:
 * 1. Enqueueing background catalog updates and AI processing tasks in Room.
 * 2. Scheduling and dispatching WorkManager one-time and periodic background sync workers.
 * 3. Observing sync work progress, queue depths, and connection retry states.
 */
object WorkManagerSyncScheduler {

    private const val TAG = "WorkManagerScheduler"

    /**
     * Dispatches an immediate one-time sync worker with NetworkType.CONNECTED constraint.
     * Uses ExistingWorkPolicy.REPLACE to ensure latest tasks are picked up immediately.
     */
    fun enqueueImmediateSync(
        context: Context,
        triggerReason: String = CatalogBackgroundSyncWorker.TRIGGER_USER_MANUAL
    ): UUID {
        val workRequest = CatalogBackgroundSyncWorker.buildOneTimeWorkRequest(triggerReason)
        WorkManager.getInstance(context).enqueueUniqueWork(
            CatalogBackgroundSyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.d(TAG, "Enqueued immediate background sync work (${workRequest.id}) with trigger: $triggerReason")
        return workRequest.id
    }

    /**
     * Schedules periodic background sync worker to reconcile catalog and inquiries
     * when network is available.
     */
    fun schedulePeriodicSync(
        context: Context,
        repeatIntervalMinutes: Long = 60
    ) {
        val periodicRequest = CatalogBackgroundSyncWorker.buildPeriodicWorkRequest(repeatIntervalMinutes)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CatalogBackgroundSyncWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
        Log.d(TAG, "Scheduled periodic background sync every $repeatIntervalMinutes minutes")
    }

    /**
     * Queues a product catalog update (CREATE, UPDATE, DELETE) in the local Room queue
     * and triggers a WorkManager sync request with CONNECTED constraint.
     */
    suspend fun queueProductSync(
        context: Context,
        productId: Long,
        operation: String = "UPDATE",
        payloadJson: String = "{}"
    ): Long {
        val database = AppDatabase.getDatabase(context)
        val queueItem = CatalogSyncQueueEntity(
            productId = productId,
            operation = operation,
            payloadJson = payloadJson,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val queueId = database.catalogSyncQueueDao().insert(queueItem)
        Log.i(TAG, "Queued product sync #$queueId for productId #$productId ($operation)")

        // Trigger WorkManager
        enqueueImmediateSync(context, CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED)
        return queueId
    }

    /**
     * Queues a voice-to-catalog AI processing task for execution when online.
     */
    suspend fun queueVoiceCatalogAiTask(
        context: Context,
        productId: Long?,
        transcript: String,
        languageCode: String,
        artisanState: String = "Uttar Pradesh",
        artisanCluster: String = "Gorakhpur",
        categoryHint: String = "Pottery"
    ): Long {
        val database = AppDatabase.getDatabase(context)
        val payload = JSONObject().apply {
            put("transcript", transcript)
            put("language_code", languageCode)
            put("artisan_state", artisanState)
            put("artisan_cluster", artisanCluster)
            put("category_hint", categoryHint)
            productId?.let { put("product_id", it) }
        }.toString()

        val task = QueuedAiTaskEntity(
            taskType = CatalogBackgroundSyncWorker.TASK_TYPE_VOICE_CATALOG_ENRICHMENT,
            productId = productId,
            title = "वॉयस कैटलॉग एआई विश्लेषण (Voice Catalog AI)",
            inputPayloadJson = payload,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val taskId = database.queuedAiTaskDao().insert(task)
        Log.i(TAG, "Queued voice catalog AI task #$taskId for product $productId")

        enqueueImmediateSync(context, CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED)
        return taskId
    }

    /**
     * Queues a dynamic pricing AI calculation task for execution when online.
     */
    suspend fun queueDynamicPricingAiTask(
        context: Context,
        productId: Long,
        materialCost: Double,
        laborHours: Double,
        hourlyWage: Double,
        packagingCost: Double,
        season: PricingSeason,
        channel: ChannelType
    ): Long {
        val database = AppDatabase.getDatabase(context)
        val payload = JSONObject().apply {
            put("product_id", productId)
            put("material_cost", materialCost)
            put("labor_hours", laborHours)
            put("hourly_wage", hourlyWage)
            put("packaging_cost", packagingCost)
            put("season", season.name)
            put("channel", channel.name)
        }.toString()

        val task = QueuedAiTaskEntity(
            taskType = CatalogBackgroundSyncWorker.TASK_TYPE_DYNAMIC_PRICING,
            productId = productId,
            title = "डायनामिक प्राइसिंग एआई गणना (Dynamic Pricing AI)",
            inputPayloadJson = payload,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        val taskId = database.queuedAiTaskDao().insert(task)
        Log.i(TAG, "Queued dynamic pricing AI task #$taskId for product $productId")

        enqueueImmediateSync(context, CatalogBackgroundSyncWorker.TRIGGER_TASK_QUEUED)
        return taskId
    }

    /**
     * Observes pending catalog sync items count from Room.
     */
    fun observePendingCatalogCount(context: Context): Flow<Int> {
        return AppDatabase.getDatabase(context).catalogSyncQueueDao().observePendingCount()
    }

    /**
     * Observes pending AI tasks count from Room.
     */
    fun observePendingAiTasksCount(context: Context): Flow<Int> {
        return AppDatabase.getDatabase(context).queuedAiTaskDao().observePendingCount()
    }

    /**
     * Observes the WorkInfo stream for the background sync worker.
     */
    fun observeSyncWorkInfo(context: Context): Flow<WorkInfo?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(CatalogBackgroundSyncWorker.UNIQUE_WORK_NAME)
            .map { it.firstOrNull() }
    }

    /**
     * Cancels any pending background sync workers.
     */
    fun cancelAllSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CatalogBackgroundSyncWorker.UNIQUE_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(CatalogBackgroundSyncWorker.PERIODIC_WORK_NAME)
        Log.i(TAG, "All background sync workers cancelled.")
    }
}
