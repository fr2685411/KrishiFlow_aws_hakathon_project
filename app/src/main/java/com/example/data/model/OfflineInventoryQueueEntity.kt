package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Status of an offline-queued item
 */
enum class SyncQueueStatus(val displayName: String) {
    QUEUED("Pending Sync (Offline)"),
    SYNCING("Syncing to Firestore..."),
    SYNCED("Synced to Cloud"),
    FAILED("Sync Failed (Retrying)")
}

/**
 * Action type performed offline
 */
enum class OfflineActionType {
    SCAN_HARVEST_BATCH,
    UPDATE_STOCK,
    DISPATCH_LOT,
    SPOILAGE_DISCOUNT
}

/**
 * Room entity representing an inventory action scanned while offline.
 * Acts as the authoritative local cache queue for Cloud Firestore.
 * Once network connectivity is restored, items are pushed to Firestore
 * and marked as SYNCED with timestamp.
 */
@Entity(tableName = "offline_inventory_sync_queue")
data class OfflineInventoryQueueEntity(
    @PrimaryKey(autoGenerate = true) val queueId: Long = 0,
    val batchCode: String,
    val cropName: String,
    val category: String,
    val variety: String,
    val qualityGrade: String,
    val scannedQuantityKg: Double,
    val farmerName: String,
    val farmLocation: String,
    val askingPricePerKg: Double,
    val storageCondition: String,
    val expectedFreshnessDays: Int,
    val actionType: OfflineActionType = OfflineActionType.SCAN_HARVEST_BATCH,
    val syncStatus: SyncQueueStatus = SyncQueueStatus.QUEUED,
    val scannedTimestamp: Long = System.currentTimeMillis(),
    val syncedTimestamp: Long? = null,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)
