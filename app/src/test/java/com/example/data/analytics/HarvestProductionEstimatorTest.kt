package com.example.data.analytics

import com.example.data.local.KrishiDatabase
import com.example.data.model.CropCategory
import com.example.data.model.HarvestHistoryLogEntity
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class HarvestProductionEstimatorTest {

    private lateinit var database: KrishiDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KrishiDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testRoomDatabaseHarvestLogInsertionAndQuery() = runBlocking {
        val now = System.currentTimeMillis()
        val dao = database.harvestHistoryLogDao()

        val log = HarvestHistoryLogEntity(
            cropName = "Tomato",
            category = CropCategory.VEGETABLES,
            variety = "Hybrid Abhinav",
            harvestSeason = "Kharif 2025",
            plantedAreaAcres = 2.0,
            harvestQuantityKg = 18000.0,
            harvestDateTimestamp = now - TimeUnit.DAYS.toMillis(30),
            saleDateTimestamp = now - TimeUnit.DAYS.toMillis(28),
            quantitySoldKg = 17500.0,
            soldRatePerKg = 25.0,
            totalRevenueRupees = 437500.0,
            buyerOrMandiName = "Pimpalgaon Baswant APMC",
            storageLossKg = 500.0,
            cycleDurationDays = 75,
            notes = "Drip fertigation yielded high quality firm fruit."
        )

        val insertedId = dao.insertLog(log)
        assertTrue(insertedId > 0)

        val logs = dao.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals("Tomato", logs[0].cropName)
        assertEquals(90.0, logs[0].yieldQuintalsPerAcre, 0.01) // 18000 / 2 = 9000 kg/acre = 90 Qtl/acre

        dao.deleteLogById(insertedId)
        val remaining = dao.getAllLogs().first()
        assertEquals(0, remaining.size)
    }

    @Test
    fun testHistoricalTrendAnalysis() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            HarvestHistoryLogEntity(
                cropName = "Tomato",
                category = CropCategory.VEGETABLES,
                variety = "Hybrid Abhinav",
                harvestSeason = "Kharif 2025",
                plantedAreaAcres = 2.0,
                harvestQuantityKg = 18000.0,
                harvestDateTimestamp = now - TimeUnit.DAYS.toMillis(60),
                saleDateTimestamp = now - TimeUnit.DAYS.toMillis(58),
                quantitySoldKg = 18000.0,
                soldRatePerKg = 24.0,
                totalRevenueRupees = 432000.0,
                buyerOrMandiName = "Pimpalgaon APMC"
            ),
            HarvestHistoryLogEntity(
                cropName = "Tomato",
                category = CropCategory.VEGETABLES,
                variety = "Hybrid Vaishnavi",
                harvestSeason = "Rabi 2024-25",
                plantedAreaAcres = 1.0,
                harvestQuantityKg = 10000.0,
                harvestDateTimestamp = now - TimeUnit.DAYS.toMillis(180),
                saleDateTimestamp = now - TimeUnit.DAYS.toMillis(178),
                quantitySoldKg = 10000.0,
                soldRatePerKg = 28.0,
                totalRevenueRupees = 280000.0,
                buyerOrMandiName = "Direct Supermarket"
            )
        )

        val trends = HarvestProductionEstimator.analyzeCropTrends(logs)
        assertEquals(1, trends.size)

        val tomatoTrend = trends[0]
        assertEquals("Tomato", tomatoTrend.cropName)
        assertEquals(2, tomatoTrend.seasonsCount)
        assertEquals(28000.0, tomatoTrend.totalHarvestedKg, 0.01)
        assertEquals(712000.0, tomatoTrend.totalRevenue, 0.01)
        // Total harvest 28,000 kg / 3.0 acres = 9,333.3 kg/acre = 93.3 Qtl/acre
        assertEquals(93.3, tomatoTrend.avgYieldQuintalsPerAcre, 0.5)
    }

    @Test
    fun testFutureProductionCycleEstimation() {
        val now = System.currentTimeMillis()
        val logs = listOf(
            HarvestHistoryLogEntity(
                cropName = "Red Onion",
                category = CropCategory.VEGETABLES,
                variety = "Nashik Garwa Red",
                harvestSeason = "Rabi 2025",
                plantedAreaAcres = 2.0,
                harvestQuantityKg = 18000.0, // 90 Qtl / acre
                harvestDateTimestamp = now - TimeUnit.DAYS.toMillis(100),
                saleDateTimestamp = now - TimeUnit.DAYS.toMillis(90),
                quantitySoldKg = 17500.0,
                soldRatePerKg = 25.0,
                totalRevenueRupees = 437500.0,
                buyerOrMandiName = "Lasalgaon Mandi",
                cycleDurationDays = 110
            )
        )

        // Estimate future production for 3.0 acres of Red Onion
        val estimate = HarvestProductionEstimator.estimateFutureProduction(
            cropName = "Red Onion",
            plannedAcres = 3.0,
            sowingDateTimestamp = now,
            logs = logs
        )

        assertEquals("Red Onion", estimate.cropName)
        assertEquals(3.0, estimate.plannedAcres, 0.01)
        // Expected harvest: 3.0 acres * 90 Qtl/acre = 270 Qtl = 27,000 kg
        assertEquals(27000.0, estimate.estimatedHarvestKg, 100.0)
        assertEquals(270.0, estimate.estimatedHarvestQuintals, 1.0)
        assertTrue("Projected revenue min should be > 500,000", estimate.projectedRevenueMin > 500000.0)
        assertEquals(110, estimate.projectedCycleDays)
        assertNotNull(estimate.estimatedHarvestDateString)
    }
}
