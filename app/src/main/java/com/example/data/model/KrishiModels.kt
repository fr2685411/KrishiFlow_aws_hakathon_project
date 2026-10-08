package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.concurrent.TimeUnit

enum class CropCategory(val displayName: String) {
    VEGETABLES("Vegetables"),
    GRAINS("Grains & Cereals"),
    PULSES("Pulses & Legumes"),
    OILSEEDS("Oilseeds"),
    FRUITS("Fruits")
}

enum class QualityGrade(val displayName: String, val description: String) {
    GRADE_A("Grade A (Export / Mart)", "Clean, sorted, uniform size, premium shelf life"),
    GRADE_B("Grade B (Standard Mandi)", "Good quality, minor variation, suitable for retail"),
    GRADE_C("Grade C (Quick Sale)", "Fully ripe, immediate consumption or processing")
}

enum class StorageCondition(val displayName: String) {
    VENTILATED_CRATE("Ventilated Crates"),
    COLD_STORAGE("Cold Storage (4°C - 8°C)"),
    AMBIENT_SHED("Ambient Dry Shed"),
    TEMP_CONTROLLED_VAN("Temp-Controlled Transport")
}

enum class BatchStatus(val displayName: String) {
    LISTED("Farmer Listed"),
    PROCURED_IN_HUB("Hub Inventory"),
    ALLOCATED("Order Allocated"),
    DISPATCHED("In Transit"),
    COMPLETED("Delivered & Settled"),
    SPOILED("Disposed")
}

enum class BuyerType(val displayName: String) {
    LOCAL_VENDOR("Local Vendor / Cart"),
    MANDI_WHOLESALER("Mandi Trader / Wholesaler"),
    SUPERMARKET_MART("Supermarket Mart (Dmart/JioMart)"),
    RETAIL_CONSUMER("Direct Bulk Consumer")
}

enum class OrderStatus(val displayName: String) {
    PENDING_CONFIRMATION("Pending Match"),
    RESERVED("Stock Reserved"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED_PAID("Delivered & Payout Sent")
}

@Entity(tableName = "produce_batches")
data class ProduceBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchCode: String,
    val cropName: String,
    val category: CropCategory,
    val variety: String,
    val qualityGrade: QualityGrade,
    val farmerName: String,
    val farmerPhone: String,
    val farmLocation: String,
    val distanceKm: Int,
    val totalQuantityKg: Double,
    val availableQuantityKg: Double,
    val reservedQuantityKg: Double = 0.0,
    val soldQuantityKg: Double = 0.0,
    val farmerAskingPricePerKg: Double,
    val hubProcuredPricePerKg: Double,
    val deliveredPricePerKg: Double,
    val discountPercent: Int = 0,
    val harvestTimestamp: Long,
    val expiryTimestamp: Long,
    val expectedFreshnessDays: Int,
    val storageCondition: StorageCondition,
    val status: BatchStatus,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val effectivePricePerKg: Double
        get() = if (discountPercent > 0) {
            deliveredPricePerKg * (1.0 - (discountPercent / 100.0))
        } else {
            deliveredPricePerKg
        }

    val remainingHours: Long
        get() {
            val diff = expiryTimestamp - System.currentTimeMillis()
            return if (diff > 0) TimeUnit.MILLISECONDS.toHours(diff) else 0L
        }

    val freshnessPercentage: Int
        get() {
            val totalLifeMs = (expectedFreshnessDays * 24 * 3600 * 1000L).coerceAtLeast(1L)
            val elapsedMs = (System.currentTimeMillis() - harvestTimestamp).coerceAtLeast(0L)
            val remainingRatio = 1.0 - (elapsedMs.toDouble() / totalLifeMs.toDouble())
            return (remainingRatio * 100).toInt().coerceIn(0, 100)
        }

    val isSurplusUrgent: Boolean
        get() = freshnessPercentage <= 45 && availableQuantityKg > 0 && status != BatchStatus.COMPLETED
}

@Entity(tableName = "buyer_demands")
data class BuyerDemandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val demandCode: String,
    val buyerName: String,
    val buyerType: BuyerType,
    val buyerPhone: String,
    val destinationLocation: String,
    val cropRequested: String,
    val minGrade: QualityGrade,
    val requiredQuantityKg: Double,
    val maxOfferedPricePerKg: Double,
    val status: String = "OPEN", // OPEN, MATCHED, FULFILLED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_transactions")
data class OrderTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderCode: String,
    val batchId: Long,
    val buyerName: String,
    val buyerType: BuyerType,
    val farmerName: String,
    val cropName: String,
    val quantityKg: Double,
    val farmerPayoutTotal: Double,
    val transportCostTotal: Double,
    val buyerPaymentTotal: Double,
    val grossMarginTotal: Double,
    val status: OrderStatus,
    val destinationAddress: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val deliveredTimestamp: Long? = null
)

@Entity(tableName = "mandi_rates")
data class MandiRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mandiName: String,
    val state: String,
    val commodity: String,
    val variety: String,
    val modalPricePerKg: Double,
    val minPricePerKg: Double,
    val maxPricePerKg: Double,
    val middlemanCommissionCutPercent: Double,
    val krishiFlowNetFarmerRate: Double,
    val priceTrend: String, // UP, DOWN, STABLE
    val arrivalTonnes: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "spoilage_prevention_records")
data class SpoilagePreventionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchCode: String,
    val cropName: String,
    val quantityKg: Double,
    val originalSpoilageRiskPercent: Int,
    val discountAppliedPercent: Int,
    val buyerName: String,
    val rescuedValueRupees: Double,
    val co2SavedKg: Double,
    val timestamp: Long = System.currentTimeMillis()
)

class KrishiConverters {
    @TypeConverter
    fun fromCropCategory(value: CropCategory): String = value.name

    @TypeConverter
    fun toCropCategory(value: String): CropCategory = try {
        CropCategory.valueOf(value)
    } catch (_: Exception) {
        CropCategory.VEGETABLES
    }

    @TypeConverter
    fun fromQualityGrade(value: QualityGrade): String = value.name

    @TypeConverter
    fun toQualityGrade(value: String): QualityGrade = try {
        QualityGrade.valueOf(value)
    } catch (_: Exception) {
        QualityGrade.GRADE_A
    }

    @TypeConverter
    fun fromStorageCondition(value: StorageCondition): String = value.name

    @TypeConverter
    fun toStorageCondition(value: String): StorageCondition = try {
        StorageCondition.valueOf(value)
    } catch (_: Exception) {
        StorageCondition.VENTILATED_CRATE
    }

    @TypeConverter
    fun fromBatchStatus(value: BatchStatus): String = value.name

    @TypeConverter
    fun toBatchStatus(value: String): BatchStatus = try {
        BatchStatus.valueOf(value)
    } catch (_: Exception) {
        BatchStatus.LISTED
    }

    @TypeConverter
    fun fromBuyerType(value: BuyerType): String = value.name

    @TypeConverter
    fun toBuyerType(value: String): BuyerType = try {
        BuyerType.valueOf(value)
    } catch (_: Exception) {
        BuyerType.LOCAL_VENDOR
    }

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = try {
        OrderStatus.valueOf(value)
    } catch (_: Exception) {
        OrderStatus.PENDING_CONFIRMATION
    }

    @TypeConverter
    fun fromSyncQueueStatus(value: SyncQueueStatus): String = value.name

    @TypeConverter
    fun toSyncQueueStatus(value: String): SyncQueueStatus = try {
        SyncQueueStatus.valueOf(value)
    } catch (_: Exception) {
        SyncQueueStatus.QUEUED
    }

    @TypeConverter
    fun fromOfflineActionType(value: OfflineActionType): String = value.name

    @TypeConverter
    fun toOfflineActionType(value: String): OfflineActionType = try {
        OfflineActionType.valueOf(value)
    } catch (_: Exception) {
        OfflineActionType.SCAN_HARVEST_BATCH
    }

    @TypeConverter
    fun fromWasteDestination(value: WasteDestination): String = value.name

    @TypeConverter
    fun toWasteDestination(value: String): WasteDestination = try {
        WasteDestination.valueOf(value)
    } catch (_: Exception) {
        WasteDestination.DONATION
    }

    @TypeConverter
    fun fromWasteReason(value: WasteReason): String = value.name

    @TypeConverter
    fun toWasteReason(value: String): WasteReason = try {
        WasteReason.valueOf(value)
    } catch (_: Exception) {
        WasteReason.MARKET_SURPLUS
    }
}
