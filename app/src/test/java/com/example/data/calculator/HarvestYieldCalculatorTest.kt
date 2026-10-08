package com.example.data.calculator

import com.example.data.model.SoilType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HarvestYieldCalculatorTest {

    @Test
    fun testTomatoYieldCalculationOnThreeAcres() {
        val tomato = HarvestYieldCalculator.CROPS.first { it.id == "crop_tomato" }

        val estimate = HarvestYieldCalculator.calculate(
            crop = tomato,
            landSizeAcres = 3.0,
            soilType = SoilType.BLACK_CLAY,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_B_MEDIUM,
            irrigationType = CalculatorIrrigationType.DRIP_FERTIGATION
        )

        assertNotNull(estimate)
        assertEquals("Hybrid Tomato", estimate.crop.name)
        assertEquals(3.0, estimate.landSizeAcres, 0.01)

        // Effective yield per acre should be higher than base yield due to drip + preferred soil
        assertTrue(estimate.effectiveYieldPerAcreQuintals > tomato.baseYieldQuintalsPerAcre)

        // Total production
        assertTrue(estimate.totalProductionQuintals > 600.0)
        assertTrue(estimate.totalProductionTonnes > 60.0)
        assertTrue(estimate.totalProductionKg > 60000.0)

        // Financials
        assertTrue(estimate.grossMarketValueRupees > 1000000.0)
        assertTrue(estimate.totalCultivationCostRupees > 100000.0)
        assertTrue(estimate.netFarmerProfitRupees > 0.0)
        assertTrue(estimate.roiPercent > 50)

        // Resources
        assertTrue(estimate.totalPackagesNeeded > 2000)
        assertTrue(estimate.seedQuantityNeededKg > 0.0)
        assertTrue(estimate.co2SequestrationKg > 0.0)
    }

    @Test
    fun testHighOrganicCarbonGradeDeliversHigherYieldThanDegraded() {
        val wheat = HarvestYieldCalculator.CROPS.first { it.id == "crop_wheat" }

        val highFertilityEstimate = HarvestYieldCalculator.calculate(
            crop = wheat,
            landSizeAcres = 5.0,
            soilType = SoilType.BLACK_CLAY,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_A_HIGH,
            irrigationType = CalculatorIrrigationType.SURFACE_CANAL
        )

        val degradedEstimate = HarvestYieldCalculator.calculate(
            crop = wheat,
            landSizeAcres = 5.0,
            soilType = SoilType.BLACK_CLAY,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_C_DEGRADED,
            irrigationType = CalculatorIrrigationType.SURFACE_CANAL
        )

        assertTrue(
            "Grade A fertility should yield more than Grade C degraded soil",
            highFertilityEstimate.totalProductionQuintals > degradedEstimate.totalProductionQuintals
        )
        assertTrue(
            "Grade A net profit should exceed degraded soil net profit",
            highFertilityEstimate.netFarmerProfitRupees > degradedEstimate.netFarmerProfitRupees
        )
    }

    @Test
    fun testDripMicroIrrigationYieldsMoreThanRainfed() {
        val onion = HarvestYieldCalculator.CROPS.first { it.id == "crop_onion" }

        val dripEstimate = HarvestYieldCalculator.calculate(
            crop = onion,
            landSizeAcres = 2.0,
            soilType = SoilType.ALLUVIAL_LOAM,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_B_MEDIUM,
            irrigationType = CalculatorIrrigationType.DRIP_FERTIGATION
        )

        val rainfedEstimate = HarvestYieldCalculator.calculate(
            crop = onion,
            landSizeAcres = 2.0,
            soilType = SoilType.ALLUVIAL_LOAM,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_B_MEDIUM,
            irrigationType = CalculatorIrrigationType.RAINFED_WELL
        )

        assertTrue(
            "Drip irrigation should yield more than rainfed",
            dripEstimate.totalProductionQuintals > rainfedEstimate.totalProductionQuintals
        )
    }

    @Test
    fun testSensitivityRangeCalculatedProperly() {
        val chickpea = HarvestYieldCalculator.CROPS.first { it.id == "crop_chana" }

        val estimate = HarvestYieldCalculator.calculate(
            crop = chickpea,
            landSizeAcres = 4.0,
            soilType = SoilType.BLACK_CLAY,
            organicCarbonGrade = SoilOrganicCarbonGrade.GRADE_B_MEDIUM,
            irrigationType = CalculatorIrrigationType.SURFACE_CANAL
        )

        assertTrue(estimate.yieldRangeMinQuintals < estimate.totalProductionQuintals)
        assertTrue(estimate.yieldRangeMaxQuintals > estimate.totalProductionQuintals)
    }
}
