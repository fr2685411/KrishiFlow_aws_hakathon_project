package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Database entity recording past harvest quantities, sale dates,
 * realized market rates, and acre yields to enable historical trend analysis
 * and accurate forecasting of future crop production cycles.
 */
@Entity(tableName = "harvest_history_logs")
data class HarvestHistoryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val category: CropCategory = CropCategory.VEGETABLES,
    val variety: String,
    val harvestSeason: String, // e.g. "Kharif 2025", "Rabi 2025-26", "Zaid 2026"
    val plantedAreaAcres: Double,
    val harvestQuantityKg: Double,
    val harvestDateTimestamp: Long,
    val saleDateTimestamp: Long,
    val quantitySoldKg: Double,
    val soldRatePerKg: Double,
    val totalRevenueRupees: Double,
    val buyerOrMandiName: String,
    val storageLossKg: Double = 0.0,
    val cycleDurationDays: Int = 90,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val yieldKgPerAcre: Double
        get() = if (plantedAreaAcres > 0) harvestQuantityKg / plantedAreaAcres else 0.0

    val yieldQuintalsPerAcre: Double
        get() = yieldKgPerAcre / 100.0

    val formattedHarvestDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(harvestDateTimestamp))

    val formattedSaleDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(saleDateTimestamp))

    val storageLossPercentage: Double
        get() = if (harvestQuantityKg > 0) (storageLossKg / harvestQuantityKg) * 100.0 else 0.0
}
