package com.example.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KrishiSyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun testKrishiSyncWorkerExecutionSuccess() = runBlocking {
        val worker = TestListenableWorkerBuilder<KrishiSyncWorker>(context).build()

        val result = worker.doWork()

        assertTrue("KrishiSyncWorker should succeed", result is ListenableWorker.Result.Success)

        val output = (result as ListenableWorker.Result.Success).outputData
        val syncedCount = output.getInt(KrishiSyncWorker.KEY_SYNCED_COUNT, -1)
        val timestamp = output.getLong(KrishiSyncWorker.KEY_SYNC_TIMESTAMP, 0L)

        assertTrue("Synced count should be non-negative", syncedCount >= 0)
        assertTrue("Timestamp should be populated", timestamp > 0L)
    }

    @Test
    fun testKrishiWorkManagerSchedulerEnqueuesWork() {
        // Enqueue periodic sync
        KrishiWorkManagerScheduler.schedulePeriodicSync(context)

        val workManager = WorkManager.getInstance(context)
        val periodicWorkInfos = workManager.getWorkInfosForUniqueWork(KrishiWorkManagerScheduler.PERIODIC_SYNC_WORK_NAME).get()

        assertNotNull(periodicWorkInfos)
        assertTrue("Periodic work should be scheduled", periodicWorkInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, periodicWorkInfos[0].state)

        // Enqueue immediate one-time sync
        KrishiWorkManagerScheduler.triggerImmediateSync(context)
        val oneTimeWorkInfos = workManager.getWorkInfosForUniqueWork(KrishiWorkManagerScheduler.ONE_TIME_SYNC_WORK_NAME).get()

        assertNotNull(oneTimeWorkInfos)
        assertTrue("One-time sync work should be enqueued", oneTimeWorkInfos.isNotEmpty())
    }
}
