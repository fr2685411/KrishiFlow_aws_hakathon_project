package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.KrishiDatabase
import com.example.data.model.SyncQueueStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * Background synchronization worker using Android Jetpack WorkManager.
 * Pushes Room SQLite database entities (Produce Batches, Buyer Demands,
 * Orders, Mandi Rates, and Harvest Logs) to Cloud Firestore when network
 * connectivity is available to ensure resilient cloud-based availability for farmers.
 */
class KrishiSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting KrishiSyncWorker execution (Attempt: $runAttemptCount)...")

        try {
            val database = KrishiDatabase.getInstance(applicationContext)

            // Read fresh records from Room database
            val batches = database.produceBatchDao().getAllBatches().firstOrNull() ?: emptyList()
            val demands = database.buyerDemandDao().getAllDemands().firstOrNull() ?: emptyList()
            val orders = database.orderTransactionDao().getAllOrders().firstOrNull() ?: emptyList()
            val mandiRates = database.mandiRateDao().getAllRates().firstOrNull() ?: emptyList()
            val harvestLogs = database.harvestHistoryLogDao().getAllLogs().firstOrNull() ?: emptyList()

            val queueDao = database.offlineInventoryQueueDao()
            val pendingQueueItems = queueDao.getPendingSyncItemsDirect()
            Log.d(TAG, "Found ${pendingQueueItems.size} pending offline scanned items in Room queue.")

            val syncService = KrishiFirestoreSyncService()

            // 1. Flush offline inventory queue to Firestore
            var queuedSyncedCount = 0
            if (pendingQueueItems.isNotEmpty()) {
                val syncedIds = syncService.syncOfflineQueueItems(pendingQueueItems)
                queuedSyncedCount = syncedIds.size
                for (id in syncedIds) {
                    queueDao.updateStatus(id, SyncQueueStatus.SYNCED, System.currentTimeMillis())
                }
                Log.d(TAG, "Marked $queuedSyncedCount offline queued items as SYNCED in local Room database.")
            }

            // 2. Full bidirectional sync of core datasets
            val totalRecords = batches.size + demands.size + orders.size + mandiRates.size + harvestLogs.size + queuedSyncedCount
            Log.d(TAG, "Collected $totalRecords entities from local Room database for Firestore sync.")

            syncService.syncAllData(
                batches = batches,
                demands = demands,
                orders = orders,
                mandiRates = mandiRates,
                harvestLogs = harvestLogs
            )

            val outputData = workDataOf(
                KEY_SYNCED_COUNT to totalRecords,
                KEY_SYNC_TIMESTAMP to System.currentTimeMillis(),
                KEY_SYNC_MESSAGE to "Synced $totalRecords items ($queuedSyncedCount offline queue flushed) to Firestore."
            )

            Log.i(TAG, "KrishiSyncWorker finished successfully. $totalRecords records pushed.")
            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "KrishiSyncWorker encountered error: ${e.message}", e)
            if (runAttemptCount < 3) {
                Log.w(TAG, "Scheduling retry for KrishiSyncWorker (Attempt #$runAttemptCount).")
                Result.retry()
            } else {
                Log.e(TAG, "KrishiSyncWorker failed after max retries.")
                Result.failure(
                    workDataOf(
                        KEY_SYNC_MESSAGE to "Failed to sync: ${e.localizedMessage ?: "Unknown error"}",
                        KEY_SYNC_TIMESTAMP to System.currentTimeMillis()
                    )
                )
            }
        }
    }

    companion object {
        const val TAG = "KrishiSyncWorker"
        const val KEY_SYNCED_COUNT = "key_synced_count"
        const val KEY_SYNC_TIMESTAMP = "key_sync_timestamp"
        const val KEY_SYNC_MESSAGE = "key_sync_message"
    }
}
