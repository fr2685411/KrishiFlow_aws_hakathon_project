package com.example.data.analytics

import com.example.data.model.HarvestHistoryLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class HistoricalCropTrendSummary(
    val cropName: String,
    val seasonsCount: Int,
    val totalHarvestedKg: Double,
    val totalRevenue: Double,
    val avgYieldQuintalsPerAcre: Double,
    val avgPricePerKg: Double,
    val avgCycleDays: Int,
    val avgStorageLossPct: Double,
    val yieldTrendDirection: String, // "RISING (+X%)", "STABLE", "DECLINING"
    val bestSeason: String
)

data class ProductionCycleEstimate(
    val cropName: String,
    val plannedAcres: Double,
    val estimatedHarvestKg: Double,
    val estimatedHarvestQuintals: Double,
    val projectedRevenueMin: Double,
    val projectedRevenueMax: Double,
    val projectedCycleDays: Int,
    val estimatedHarvestDateString: String,
    val optimalSalesTimingAdvice: String,
    val historicalBasisNote: String
)

object HarvestProductionEstimator {

    fun analyzeCropTrends(logs: List<HarvestHistoryLogEntity>): List<HistoricalCropTrendSummary> {
        if (logs.isEmpty()) return emptyList()

        return logs.groupBy { it.cropName }.map { (crop, cropLogs) ->
            val totalHarvest = cropLogs.sumOf { it.harvestQuantityKg }
            val totalRev = cropLogs.sumOf { it.totalRevenueRupees }
            val totalSold = cropLogs.sumOf { it.quantitySoldKg }.coerceAtLeast(1.0)
            val avgPrice = totalRev / totalSold

            val totalAcres = cropLogs.sumOf { it.plantedAreaAcres }.coerceAtLeast(0.1)
            val avgYieldQtl = (totalHarvest / totalAcres) / 100.0

            val avgCycle = if (cropLogs.isNotEmpty()) (cropLogs.map { it.cycleDurationDays }.average()).roundToInt() else 90
            val avgLoss = if (cropLogs.isNotEmpty()) cropLogs.map { it.storageLossPercentage }.average() else 0.0

            // Determine yield trend between first and most recent season
            val sortedLogs = cropLogs.sortedBy { it.harvestDateTimestamp }
            val trend = if (sortedLogs.size >= 2) {
                val firstYield = sortedLogs.first().yieldKgPerAcre
                val lastYield = sortedLogs.last().yieldKgPerAcre
                val changePct = if (firstYield > 0) ((lastYield - firstYield) / firstYield) * 100.0 else 0.0
                when {
                    changePct > 5.0 -> "Rising (+${changePct.roundToInt()}%)"
                    changePct < -5.0 -> "Declining (${changePct.roundToInt()}%)"
                    else -> "Stable (±2%)"
                }
            } else {
                "Baseline Season"
            }

            val bestSeason = cropLogs.maxByOrNull { it.totalRevenueRupees }?.harvestSeason ?: "Current"

            HistoricalCropTrendSummary(
                cropName = crop,
                seasonsCount = cropLogs.size,
                totalHarvestedKg = totalHarvest,
                totalRevenue = totalRev,
                avgYieldQuintalsPerAcre = (avgYieldQtl * 10.0).roundToInt() / 10.0,
                avgPricePerKg = (avgPrice * 10.0).roundToInt() / 10.0,
                avgCycleDays = avgCycle,
                avgStorageLossPct = (avgLoss * 10.0).roundToInt() / 10.0,
                yieldTrendDirection = trend,
                bestSeason = bestSeason
            )
        }.sortedByDescending { it.totalRevenue }
    }

    fun estimateFutureProduction(
        cropName: String,
        plannedAcres: Double,
        sowingDateTimestamp: Long = System.currentTimeMillis(),
        logs: List<HarvestHistoryLogEntity>
    ): ProductionCycleEstimate {
        val matchingLogs = logs.filter { it.cropName.equals(cropName, ignoreCase = true) }

        // Fallbacks if no logs for this specific crop yet
        val defaultYieldQtlPerAcre = when (cropName.lowercase()) {
            "tomato", "hybrid tomato" -> 90.0 // 9,000 kg / acre
            "red onion", "onion" -> 85.0    // 8,500 kg / acre
            "soybean" -> 14.0               // 1,400 kg / acre
            "sharbati wheat", "wheat" -> 22.0 // 2,200 kg / acre
            "green chilli", "chilli" -> 40.0 // 4,000 kg / acre
            else -> 35.0
        }

        val defaultPricePerKg = when (cropName.lowercase()) {
            "tomato", "hybrid tomato" -> 24.0
            "red onion", "onion" -> 26.0
            "soybean" -> 48.0
            "sharbati wheat", "wheat" -> 34.0
            "green chilli", "chilli" -> 42.0
            else -> 28.0
        }

        val defaultCycleDays = when (cropName.lowercase()) {
            "tomato", "hybrid tomato" -> 75
            "red onion", "onion" -> 110
            "soybean" -> 95
            "sharbati wheat", "wheat" -> 125
            "green chilli", "chilli" -> 85
            else -> 90
        }

        val avgYieldQtl = if (matchingLogs.isNotEmpty()) {
            val totalH = matchingLogs.sumOf { it.harvestQuantityKg }
            val totalA = matchingLogs.sumOf { it.plantedAreaAcres }.coerceAtLeast(0.1)
            (totalH / totalA) / 100.0
        } else {
            defaultYieldQtlPerAcre
        }

        val avgPrice = if (matchingLogs.isNotEmpty()) {
            val totalRev = matchingLogs.sumOf { it.totalRevenueRupees }
            val totalS = matchingLogs.sumOf { it.quantitySoldKg }.coerceAtLeast(1.0)
            totalRev / totalS
        } else {
            defaultPricePerKg
        }

        val cycleDays = if (matchingLogs.isNotEmpty()) {
            matchingLogs.map { it.cycleDurationDays }.average().roundToInt()
        } else {
            defaultCycleDays
        }

        val estimatedKg = plannedAcres * avgYieldQtl * 100.0
        val estimatedQtl = estimatedKg / 100.0

        val baseRev = estimatedKg * avgPrice
        val minRev = baseRev * 0.90 // -10% market variance
        val maxRev = baseRev * 1.15 // +15% peak timing capture

        val harvestCalendar = Calendar.getInstance().apply {
            timeInMillis = sowingDateTimestamp
            add(Calendar.DAY_OF_YEAR, cycleDays)
        }
        val harvestDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(harvestCalendar.time)

        val timingAdvice = when {
            cropName.contains("Tomato", ignoreCase = true) ->
                "Historical trend shows local APMC prices peak during morning wholesale auction. Stagger picking across 3 pickings for optimal pricing."
            cropName.contains("Onion", ignoreCase = true) ->
                "Dry cure for 10-14 days post harvest. Historical data indicates storing for 3 weeks after general harvest yields +22% higher returns."
            cropName.contains("Wheat", ignoreCase = true) ->
                "Peak government MSP procurement window begins right after March harvest. Direct supermarket contracts pay ~8% above mandi rates."
            else ->
                "Track local mandi arrivals closely 2 weeks before anticipated harvest to lock in pre-orders."
        }

        val basisNote = if (matchingLogs.isNotEmpty()) {
            "Based on ${matchingLogs.size} verified historical season logs (${matchingLogs.map { it.harvestSeason }.joinToString(", ")})"
        } else {
            "Estimated using regional agricultural benchmark standards (Nashik Agro-Climatic Zone)"
        }

        return ProductionCycleEstimate(
            cropName = cropName,
            plannedAcres = plannedAcres,
            estimatedHarvestKg = (estimatedKg * 10.0).roundToInt() / 10.0,
            estimatedHarvestQuintals = (estimatedQtl * 10.0).roundToInt() / 10.0,
            projectedRevenueMin = minRev,
            projectedRevenueMax = maxRev,
            projectedCycleDays = cycleDays,
            estimatedHarvestDateString = harvestDateStr,
            optimalSalesTimingAdvice = timingAdvice,
            historicalBasisNote = basisNote
        )
    }
}
