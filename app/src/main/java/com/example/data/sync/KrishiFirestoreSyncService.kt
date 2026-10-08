package com.example.data.sync

import android.util.Log
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.MandiRateEntity
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.SyncQueueStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class CloudSyncState {
    data object Idle : CloudSyncState()
    data class Syncing(val message: String) : CloudSyncState()
    data class Success(val message: String, val lastSyncTime: Long = System.currentTimeMillis()) : CloudSyncState()
    data class Offline(val reason: String) : CloudSyncState()
}

/**
 * Cloud Firestore Data Synchronization Service for KrishiFlow.
 * Enables two-way synchronization for Farmer harvest listings,
 * Supermarket/Vendor RFQs, Order Transactions, and APMC Mandi rates.
 */
class KrishiFirestoreSyncService {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance unavailable (running in local Room offline mode): ${e.message}")
            null
        }
    }

    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    suspend fun syncAllData(
        batches: List<ProduceBatchEntity>,
        demands: List<BuyerDemandEntity>,
        orders: List<OrderTransactionEntity>,
        mandiRates: List<MandiRateEntity>,
        harvestLogs: List<com.example.data.model.HarvestHistoryLogEntity> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val db = firestore
        if (db == null) {
            _syncState.value = CloudSyncState.Offline("Cloud synchronization active in local-first caching mode.")
            return@withContext
        }

        try {
            _syncState.value = CloudSyncState.Syncing("Uploading farmer produce batches to Cloud Firestore...")
            
            // 1. Sync Farmer Produce Batches
            for (batch in batches) {
                val batchDoc = db.collection(COLLECTION_PRODUCE_BATCHES).document(batch.batchCode)
                val batchMap = hashMapOf(
                    "batchCode" to batch.batchCode,
                    "cropName" to batch.cropName,
                    "category" to batch.category.name,
                    "variety" to batch.variety,
                    "qualityGrade" to batch.qualityGrade.name,
                    "farmerName" to batch.farmerName,
                    "farmLocation" to batch.farmLocation,
                    "distanceKm" to batch.distanceKm,
                    "totalQuantityKg" to batch.totalQuantityKg,
                    "availableQuantityKg" to batch.availableQuantityKg,
                    "reservedQuantityKg" to batch.reservedQuantityKg,
                    "farmerAskingPricePerKg" to batch.farmerAskingPricePerKg,
                    "deliveredPricePerKg" to batch.deliveredPricePerKg,
                    "discountPercent" to batch.discountPercent,
                    "freshnessPercentage" to batch.freshnessPercentage,
                    "storageCondition" to batch.storageCondition.name,
                    "status" to batch.status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
                batchDoc.set(batchMap, SetOptions.merge()).await()
            }

            _syncState.value = CloudSyncState.Syncing("Uploading commercial vendor demands...")

            // 2. Sync Commercial Buyer Demands
            for (demand in demands) {
                val demandDoc = db.collection(COLLECTION_BUYER_DEMANDS).document(demand.demandCode)
                val demandMap = hashMapOf(
                    "demandCode" to demand.demandCode,
                    "buyerName" to demand.buyerName,
                    "buyerType" to demand.buyerType.name,
                    "cropRequested" to demand.cropRequested,
                    "minGrade" to demand.minGrade.name,
                    "requiredQuantityKg" to demand.requiredQuantityKg,
                    "maxOfferedPricePerKg" to demand.maxOfferedPricePerKg,
                    "destinationLocation" to demand.destinationLocation,
                    "status" to demand.status,
                    "updatedAt" to System.currentTimeMillis()
                )
                demandDoc.set(demandMap, SetOptions.merge()).await()
            }

            // 3. Sync Mandi Benchmarks
            for (rate in mandiRates) {
                val rateDoc = db.collection(COLLECTION_MANDI_RATES)
                    .document("${rate.mandiName}_${rate.commodity}".replace(" ", "_"))
                val rateMap = hashMapOf(
                    "mandiName" to rate.mandiName,
                    "state" to rate.state,
                    "commodity" to rate.commodity,
                    "variety" to rate.variety,
                    "modalPricePerKg" to rate.modalPricePerKg,
                    "minPricePerKg" to rate.minPricePerKg,
                    "maxPricePerKg" to rate.maxPricePerKg,
                    "middlemanCommissionCutPercent" to rate.middlemanCommissionCutPercent,
                    "krishiFlowNetFarmerRate" to rate.krishiFlowNetFarmerRate,
                    "arrivalTonnes" to rate.arrivalTonnes,
                    "updatedAt" to System.currentTimeMillis()
                )
                rateDoc.set(rateMap, SetOptions.merge()).await()
            }

            // 4. Sync Orders
            for (order in orders) {
                val orderDoc = db.collection(COLLECTION_ORDERS).document(order.orderCode)
                val orderMap = hashMapOf(
                    "orderCode" to order.orderCode,
                    "buyerName" to order.buyerName,
                    "buyerType" to order.buyerType.name,
                    "cropName" to order.cropName,
                    "farmerName" to order.farmerName,
                    "quantityKg" to order.quantityKg,
                    "farmerPayoutTotal" to order.farmerPayoutTotal,
                    "buyerPaymentTotal" to order.buyerPaymentTotal,
                    "grossMarginTotal" to order.grossMarginTotal,
                    "destinationAddress" to order.destinationAddress,
                    "status" to order.status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
                orderDoc.set(orderMap, SetOptions.merge()).await()
            }

            // 5. Sync Harvest History Logs
            for (log in harvestLogs) {
                val logDoc = db.collection(COLLECTION_HARVEST_LOGS).document("log_${log.id}")
                val logMap = hashMapOf(
                    "id" to log.id,
                    "cropName" to log.cropName,
                    "category" to log.category.name,
                    "variety" to log.variety,
                    "harvestSeason" to log.harvestSeason,
                    "plantedAreaAcres" to log.plantedAreaAcres,
                    "harvestQuantityKg" to log.harvestQuantityKg,
                    "harvestDateTimestamp" to log.harvestDateTimestamp,
                    "saleDateTimestamp" to log.saleDateTimestamp,
                    "quantitySoldKg" to log.quantitySoldKg,
                    "soldRatePerKg" to log.soldRatePerKg,
                    "totalRevenueRupees" to log.totalRevenueRupees,
                    "buyerOrMandiName" to log.buyerOrMandiName,
                    "storageLossKg" to log.storageLossKg,
                    "cycleDurationDays" to log.cycleDurationDays,
                    "notes" to log.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                logDoc.set(logMap, SetOptions.merge()).await()
            }

            val totalSynced = batches.size + demands.size + orders.size + mandiRates.size + harvestLogs.size
            _syncState.value = CloudSyncState.Success(
                message = "Cloud synced $totalSynced records across Firestore collections."
            )
            Log.d(TAG, "Successfully synced $totalSynced records to Firestore.")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync failed or queued offline: ${e.message}")
            _syncState.value = CloudSyncState.Offline("Cloud sync offline cache enabled: ${e.localizedMessage ?: "Network queued"}")
        }
    }

    /**
     * Uploads offline-scanned inventory queue items to Cloud Firestore.
     * Returns the list of successfully uploaded queueIds so the local Room DAO
     * can transition their state to SYNCED.
     */
    suspend fun syncOfflineQueueItems(
        queueItems: List<OfflineInventoryQueueEntity>
    ): List<Long> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext emptyList()
        val successfullySyncedIds = mutableListOf<Long>()

        for (item in queueItems) {
            try {
                val batchDoc = db.collection(COLLECTION_PRODUCE_BATCHES).document(item.batchCode)
                val batchMap = hashMapOf(
                    "batchCode" to item.batchCode,
                    "cropName" to item.cropName,
                    "category" to item.category,
                    "variety" to item.variety,
                    "qualityGrade" to item.qualityGrade,
                    "farmerName" to item.farmerName,
                    "farmLocation" to item.farmLocation,
                    "distanceKm" to 12,
                    "totalQuantityKg" to item.scannedQuantityKg,
                    "availableQuantityKg" to item.scannedQuantityKg,
                    "reservedQuantityKg" to 0.0,
                    "farmerAskingPricePerKg" to item.askingPricePerKg,
                    "deliveredPricePerKg" to (item.askingPricePerKg + 3.0),
                    "discountPercent" to 0,
                    "freshnessPercentage" to 95,
                    "storageCondition" to item.storageCondition,
                    "status" to "PROCURED_IN_HUB",
                    "actionType" to item.actionType.name,
                    "source" to "OFFLINE_ROOM_SCAN_CACHE",
                    "scannedAt" to item.scannedTimestamp,
                    "syncedAt" to System.currentTimeMillis()
                )
                batchDoc.set(batchMap, SetOptions.merge()).await()
                successfullySyncedIds.add(item.queueId)
                Log.d(TAG, "Successfully uploaded offline queued batch ${item.batchCode} to Firestore.")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to upload queued batch ${item.batchCode}: ${e.message}")
            }
        }

        successfullySyncedIds
    }

    companion object {
        private const val TAG = "KrishiFirestoreSync"
        const val COLLECTION_PRODUCE_BATCHES = "krishiflow_produce_batches"
        const val COLLECTION_BUYER_DEMANDS = "krishiflow_buyer_demands"
        const val COLLECTION_MANDI_RATES = "krishiflow_mandi_rates"
        const val COLLECTION_ORDERS = "krishiflow_orders"
        const val COLLECTION_HARVEST_LOGS = "krishiflow_harvest_history_logs"
    }
}
