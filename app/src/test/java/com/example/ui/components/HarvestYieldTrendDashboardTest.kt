package com.example.ui.components

import com.example.data.model.CropCategory
import com.example.data.model.HarvestHistoryLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HarvestYieldTrendDashboardTest {

    @Test
    fun testYieldPerAcreComputation() {
        val now = System.currentTimeMillis()
        val log = HarvestHistoryLogEntity(
            cropName = "Tomato",
            category = CropCategory.VEGETABLES,
            variety = "Hybrid",
            harvestSeason = "Kharif",
            plantedAreaAcres = 2.0,
            harvestQuantityKg = 16000.0,
            harvestDateTimestamp = now - 1000000,
            saleDateTimestamp = now,
            quantitySoldKg = 16000.0,
            soldRatePerKg = 25.0,
            totalRevenueRupees = 400000.0,
            buyerOrMandiName = "Nashik APMC"
        )

        // 16,000 kg / 2.0 acres = 8,000 kg/acre = 80 Quintals/Acre
        assertEquals(80.0, log.yieldQuintalsPerAcre, 0.01)
    }

    @Test
    fun testMonthlyYieldMetricsCalculation() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3600 * 1000L

        val logs = listOf(
            HarvestHistoryLogEntity(
                cropName = "Tomato",
                category = CropCategory.VEGETABLES,
                variety = "Vaishnavi",
                harvestSeason = "Rabi",
                plantedAreaAcres = 1.0,
                harvestQuantityKg = 7000.0,
                harvestDateTimestamp = now - (30 * dayMs),
                saleDateTimestamp = now - (28 * dayMs),
                quantitySoldKg = 7000.0,
                soldRatePerKg = 22.0,
                totalRevenueRupees = 154000.0,
                buyerOrMandiName = "Mandi A"
            ),
            HarvestHistoryLogEntity(
                cropName = "Tomato",
                category = CropCategory.VEGETABLES,
                variety = "Abhinav",
                harvestSeason = "Rabi",
                plantedAreaAcres = 2.0,
                harvestQuantityKg = 18000.0,
                harvestDateTimestamp = now - (60 * dayMs),
                saleDateTimestamp = now - (58 * dayMs),
                quantitySoldKg = 18000.0,
                soldRatePerKg = 24.0,
                totalRevenueRupees = 432000.0,
                buyerOrMandiName = "Mandi B"
            )
        )

        val totalHarvestKg = logs.sumOf { it.harvestQuantityKg }
        val totalRevenue = logs.sumOf { it.totalRevenueRupees }
        val totalAcres = logs.sumOf { it.plantedAreaAcres }
        val avgYield = (totalHarvestKg / totalAcres) / 100.0

        assertEquals(25000.0, totalHarvestKg, 0.01)
        assertEquals(586000.0, totalRevenue, 0.01)
        assertEquals(3.0, totalAcres, 0.01)
        // 25,000 kg / 3.0 acres = 8333.33 kg / acre = 83.33 Qtl/Ac
        assertEquals(83.33, avgYield, 0.05)
    }
}
