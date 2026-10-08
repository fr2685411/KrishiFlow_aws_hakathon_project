package com.example.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Scheduler responsible for configuring and dispatching WorkManager background sync tasks.
 * Ensures Room database modifications are automatically uploaded to Cloud Firestore
 * whenever network connectivity is established.
 */
object KrishiWorkManagerScheduler {
    private const val TAG = "KrishiWorkScheduler"

    const val PERIODIC_SYNC_WORK_NAME = "krishiflow_periodic_room_firestore_sync"
    const val ONE_TIME_SYNC_WORK_NAME = "krishiflow_onetime_room_firestore_sync"
    const val SYNC_WORK_TAG = "krishiflow_sync_worker_tag"

    /**
     * Registers a periodic background synchronization task (every 15 minutes)
     * constrained to run when the device has an active internet connection.
     */
    fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<KrishiSyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(SYNC_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )

        Log.d(TAG, "Enqueued periodic WorkManager background sync with NetworkType.CONNECTED constraint.")
    }

    /**
     * Triggers an immediate one-time sync task with NetworkType.CONNECTED constraint.
     * WorkManager will run it immediately if connected, or queue it until the internet is available.
     */
    fun triggerImmediateSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<KrishiSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .addTag(SYNC_WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeSyncRequest
        )

        Log.d(TAG, "Enqueued immediate one-time WorkManager sync request.")
    }

    /**
     * Observes real-time WorkManager execution status for one-time sync operations.
     */
    fun observeOneTimeSyncStatus(context: Context): Flow<WorkInfo?> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(ONE_TIME_SYNC_WORK_NAME)
            .map { it.firstOrNull() }
    }
}
