package com.example.data.advisory

import com.example.data.model.AgroSeason
import com.example.data.model.CropCategory
import com.example.data.model.CropRiskLevel
import com.example.data.model.IrrigationSource
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CropAdvisoryEngineTest {

    @Test
    fun testRestorativeLegumeRecommendationAfterHeavyCereal() {
        val query = CropAdvisoryEngine.AdvisoryQuery(
            soilType = SoilType.BLACK_CLAY,
            season = AgroSeason.RABI,
            irrigation = IrrigationSource.DRIP_MICRO,
            previousCrop = PreviousCropType.HEAVY_FEEDER_GRAIN,
            farmAcres = 4.0
        )

        val recommendations = CropAdvisoryEngine.evaluateCropPlan(query)
        assertTrue("Recommendations should not be empty", recommendations.isNotEmpty())

        val topCrop = recommendations.first()
        // In Rabi on Black Clay following Cereal, Chickpea (Chana) is the top restorative legume
        assertEquals("Desi Chickpea (Chana)", topCrop.cropName)
        assertTrue("Chickpea should have high natural harmony score >= 90", topCrop.naturalHarmonyScore >= 90)
        assertTrue("Verdict should mention restorative rotation or nitrogen", topCrop.naturalHarmonyVerdict.contains("nitrogen", ignoreCase = true))

        // Check production calculation
        assertTrue("Expected yield per acre should be positive", topCrop.expectedYieldPerAcreQuintals > 0)
        assertEquals(topCrop.expectedYieldPerAcreQuintals * 4.0, topCrop.totalEstimatedProductionQuintals, 0.5)
        assertEquals(topCrop.totalEstimatedProductionQuintals / 10.0, topCrop.totalEstimatedProductionTonnes, 0.1)

        // Check economics
        assertTrue("Gross revenue should be > 0", topCrop.estimatedGrossRevenueRupees > 0)
        assertTrue("Input cost should be > 0", topCrop.estimatedInputCostRupees > 0)
        assertTrue("Net profit should be positive for top legume", topCrop.estimatedNetProfitRupees > 0)

        // Check preservation plan
        val plan = topCrop.naturalOrderPreservationPlan
        assertNotNull(plan)
        assertTrue(plan.biologicalSoilRemedy.contains("Rhizobium", ignoreCase = true))
        assertTrue(plan.companionPlantingTrapCrop.contains("Mustard") || plan.companionPlantingTrapCrop.contains("Coriander"))
    }

    @Test
    fun testPestCarryoverConflictOnConsecutiveSolanaceae() {
        val query = CropAdvisoryEngine.AdvisoryQuery(
            soilType = SoilType.RED_LOAMY,
            season = AgroSeason.KHARIF,
            irrigation = IrrigationSource.DRIP_MICRO,
            previousCrop = PreviousCropType.SOLANACEOUS_VEG, // Previous crop was tomato/potato/chilli
            farmAcres = 2.0
        )

        val recommendations = CropAdvisoryEngine.evaluateCropPlan(query)
        val tomatoRec = recommendations.firstOrNull { it.cropName.contains("Tomato", ignoreCase = true) }
        assertNotNull("Tomato should be evaluated", tomatoRec)

        // Tomato following Tomato should be penalized for breaking natural order
        assertTrue(
            "Tomato following solanaceous vegetable should have higher pest risk or warning",
            tomatoRec!!.pestRiskPercent >= 35
        )
        assertTrue(
            "Verdict should flag ecological conflict or pest carryover",
            tomatoRec.naturalHarmonyVerdict.contains("Ecological Conflict") ||
            tomatoRec.naturalHarmonyVerdict.contains("Solanaceae") ||
            tomatoRec.naturalHarmonyScore < 80
        )
    }

    @Test
    fun testRainfedIrrigationPenalizesHighWaterCrops() {
        val rainfedQuery = CropAdvisoryEngine.AdvisoryQuery(
            soilType = SoilType.BLACK_CLAY,
            season = AgroSeason.KHARIF,
            irrigation = IrrigationSource.RAINFED, // Strictly rainfed
            previousCrop = PreviousCropType.FALLOW_GREEN_MANURE,
            farmAcres = 5.0
        )

        val recommendations = CropAdvisoryEngine.evaluateCropPlan(rainfedQuery)
        val tomatoRec = recommendations.firstOrNull { it.cropName.contains("Tomato", ignoreCase = true) }
        assertNotNull(tomatoRec)

        // Tomato requires drip micro irrigation, rainfed should increase weather risk
        assertTrue("Weather risk for tomato in rainfed should be high", tomatoRec!!.weatherRiskPercent >= 40)
    }

    @Test
    fun testProductionScalingWithAcres() {
        val query1Acre = CropAdvisoryEngine.AdvisoryQuery(
            soilType = SoilType.ALLUVIAL_LOAM,
            season = AgroSeason.RABI,
            irrigation = IrrigationSource.LIMITED_WELL,
            previousCrop = PreviousCropType.LEGUME_PULSE,
            farmAcres = 1.0
        )
        val recs1Acre = CropAdvisoryEngine.evaluateCropPlan(query1Acre)
        val wheat1Ac = recs1Acre.first { it.cropName.contains("Wheat") }

        val query5Acres = query1Acre.copy(farmAcres = 5.0)
        val recs5Acres = CropAdvisoryEngine.evaluateCropPlan(query5Acres)
        val wheat5Ac = recs5Acres.first { it.cropName.contains("Wheat") }

        // Total production on 5 acres should be 5x of 1 acre
        assertEquals(wheat1Ac.totalEstimatedProductionQuintals * 5.0, wheat5Ac.totalEstimatedProductionQuintals, 0.5)
        assertEquals(wheat1Ac.estimatedInputCostRupees * 5.0, wheat5Ac.estimatedInputCostRupees, 1.0)
    }
}
