package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.SyncQueueStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Offline Inventory Synchronization Queue.
 * Caches items scanned by farmers while offline and provides queries
 * for WorkManager background workers and UI synchronization badges.
 */
@Dao
interface OfflineInventoryQueueDao {

    @Query("SELECT * FROM offline_inventory_sync_queue ORDER BY scannedTimestamp DESC")
    fun getAllQueuedItems(): Flow<List<OfflineInventoryQueueEntity>>

    @Query("SELECT * FROM offline_inventory_sync_queue WHERE syncStatus = 'QUEUED' OR syncStatus = 'FAILED' ORDER BY scannedTimestamp ASC")
    fun getPendingSyncItemsFlow(): Flow<List<OfflineInventoryQueueEntity>>

    @Query("SELECT * FROM offline_inventory_sync_queue WHERE syncStatus = 'QUEUED' OR syncStatus = 'FAILED' ORDER BY scannedTimestamp ASC")
    suspend fun getPendingSyncItemsDirect(): List<OfflineInventoryQueueEntity>

    @Query("SELECT COUNT(*) FROM offline_inventory_sync_queue WHERE syncStatus = 'QUEUED' OR syncStatus = 'FAILED'")
    fun getPendingSyncCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: OfflineInventoryQueueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OfflineInventoryQueueEntity>)

    @Update
    suspend fun updateQueueItem(item: OfflineInventoryQueueEntity)

    @Query("UPDATE offline_inventory_sync_queue SET syncStatus = :status, syncedTimestamp = :timestamp WHERE queueId = :queueId")
    suspend fun updateStatus(queueId: Long, status: SyncQueueStatus, timestamp: Long? = System.currentTimeMillis())

    @Query("DELETE FROM offline_inventory_sync_queue WHERE queueId = :queueId")
    suspend fun deleteQueueItem(queueId: Long)

    @Query("DELETE FROM offline_inventory_sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun clearCompletedSyncedItems()
}
