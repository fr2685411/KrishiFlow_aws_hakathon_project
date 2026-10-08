package com.example.data.inventory

import com.example.data.model.BatchStatus
import com.example.data.model.CropCategory
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.components.REDISTRIBUTION_CHANNELS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class HarvestShelfLifeTrackingTest {

    @Test
    fun testFreshBatchHasHighFreshnessPercentage() {
        val now = System.currentTimeMillis()
        val harvestTime = now - TimeUnit.HOURS.toMillis(6) // Harvested 6 hours ago
        val expectedDays = 7
        val expiryTime = harvestTime + TimeUnit.DAYS.toMillis(expectedDays.toLong())

        val batch = ProduceBatchEntity(
            batchCode = "BATCH-TOM-001",
            cropName = "Tomato",
            category = CropCategory.VEGETABLES,
            variety = "Hybrid Vaishnavi",
            qualityGrade = QualityGrade.GRADE_A,
            farmerName = "Ramesh Patil",
            farmerPhone = "9822012345",
            farmLocation = "Dindori, Nashik",
            distanceKm = 14,
            totalQuantityKg = 1500.0,
            availableQuantityKg = 1500.0,
            farmerAskingPricePerKg = 24.0,
            hubProcuredPricePerKg = 24.0,
            deliveredPricePerKg = 28.0,
            discountPercent = 0,
            harvestTimestamp = harvestTime,
            expiryTimestamp = expiryTime,
            expectedFreshnessDays = expectedDays,
            storageCondition = StorageCondition.VENTILATED_CRATE,
            status = BatchStatus.PROCURED_IN_HUB
        )

        assertTrue("Fresh batch should have > 85% freshness", batch.freshnessPercentage >= 85)
        assertTrue("Remaining hours should be > 100h", batch.remainingHours > 100)
        assertFalse("Fresh batch should not be marked as urgent surplus", batch.isSurplusUrgent)
    }

    @Test
    fun testCriticalBatchTriggersSurplusUrgentFlag() {
        val now = System.currentTimeMillis()
        val expectedDays = 5
        val totalLifeMs = TimeUnit.DAYS.toMillis(expectedDays.toLong())
        // Harvested 4 days and 18 hours ago (only 6 hours left)
        val harvestTime = now - (totalLifeMs - TimeUnit.HOURS.toMillis(6))
        val expiryTime = harvestTime + totalLifeMs

        val criticalBatch = ProduceBatchEntity(
            batchCode = "BATCH-CRIT-99",
            cropName = "Spinach / Palak",
            category = CropCategory.VEGETABLES,
            variety = "All Green",
            qualityGrade = QualityGrade.GRADE_B,
            farmerName = "Suresh Shinde",
            farmerPhone = "9822099999",
            farmLocation = "Niphad, Nashik",
            distanceKm = 22,
            totalQuantityKg = 800.0,
            availableQuantityKg = 800.0,
            farmerAskingPricePerKg = 18.0,
            hubProcuredPricePerKg = 18.0,
            deliveredPricePerKg = 22.0,
            discountPercent = 0,
            harvestTimestamp = harvestTime,
            expiryTimestamp = expiryTime,
            expectedFreshnessDays = expectedDays,
            storageCondition = StorageCondition.VENTILATED_CRATE,
            status = BatchStatus.PROCURED_IN_HUB
        )

        assertTrue("Critical batch should have <= 36 hours remaining", criticalBatch.remainingHours <= 36)
        assertTrue("Freshness percentage should be <= 35%", criticalBatch.freshnessPercentage <= 35)
        assertTrue("Urgent surplus flag must be true", criticalBatch.isSurplusUrgent)
    }

    @Test
    fun testSurplusRedistributionChannelsConfiguration() {
        assertEquals(4, REDISTRIBUTION_CHANNELS.size)

        val localMandi = REDISTRIBUTION_CHANNELS.first { it.id == "local_mandi_cart" }
        assertEquals(20, localMandi.recommendedDiscount)

        val processing = REDISTRIBUTION_CHANNELS.first { it.id == "food_processing" }
        assertEquals(35, processing.recommendedDiscount)

        val zeroWaste = REDISTRIBUTION_CHANNELS.first { it.id == "community_zero_waste" }
        assertEquals(25, zeroWaste.recommendedDiscount)

        val biogas = REDISTRIBUTION_CHANNELS.first { it.id == "biogas_livestock" }
        assertEquals(60, biogas.recommendedDiscount)
    }

    @Test
    fun testFlashDiscountReducesEffectivePricePerKg() {
        val now = System.currentTimeMillis()
        val batch = ProduceBatchEntity(
            batchCode = "BATCH-ONION-45",
            cropName = "Red Onion",
            category = CropCategory.VEGETABLES,
            variety = "Nashik Dark Red",
            qualityGrade = QualityGrade.GRADE_A,
            farmerName = "Eknath Rao",
            farmerPhone = "9822055555",
            farmLocation = "Yeola, Nashik",
            distanceKm = 30,
            totalQuantityKg = 2000.0,
            availableQuantityKg = 2000.0,
            farmerAskingPricePerKg = 26.0,
            hubProcuredPricePerKg = 26.0,
            deliveredPricePerKg = 30.0,
            discountPercent = 30, // 30% discount applied
            harvestTimestamp = now - TimeUnit.DAYS.toMillis(2),
            expiryTimestamp = now + TimeUnit.DAYS.toMillis(8),
            expectedFreshnessDays = 10,
            storageCondition = StorageCondition.AMBIENT_SHED,
            status = BatchStatus.PROCURED_IN_HUB
        )

        // Effective delivered price should be 30.0 * (1 - 0.30) = 21.0
        assertEquals(21.0, batch.effectivePricePerKg, 0.01)
    }
}
