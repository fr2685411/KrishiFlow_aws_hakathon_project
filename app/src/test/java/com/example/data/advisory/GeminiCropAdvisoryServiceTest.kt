package com.example.data.advisory

import com.example.data.model.AgroSeason
import com.example.data.model.IrrigationSource
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GeminiCropAdvisoryServiceTest {

    private val service = GeminiCropAdvisoryService()

    private val sampleInput = CropAdvisoryAiInput(
        soilType = SoilType.BLACK_CLAY,
        season = AgroSeason.RABI,
        irrigation = IrrigationSource.DRIP_MICRO,
        previousCrop = PreviousCropType.HEAVY_FEEDER_GRAIN,
        farmAcres = 3.5,
        locationName = "Dindori Farm Hub, Nashik",
        latitude = 20.0110,
        longitude = 73.7903,
        currentTemp = 28.5,
        humidity = 60,
        rainProbability = 15
    )

    @Test
    fun testSynthesizedAdvisoryGenerationWhenOffline() {
        val result = service.generateSynthesizedAdvisory(sampleInput, isLiveAi = false)

        assertNotNull(result)
        assertEquals("Dindori Farm Hub, Nashik", result.locationAnalyzed)
        assertFalse("Should be marked as not live AI when synthesized", result.isLiveAiGenerated)
        assertTrue("Should have recommendations", result.recommendations.isNotEmpty())

        val topCrop = result.recommendations.first()
        assertTrue("Match score should be between 40 and 100", topCrop.agroClimaticMatchScore in 40..100)
        assertTrue("Should have expected yield per acre", topCrop.expectedYieldQuintalsPerAcre > 0.0)
        assertTrue("Should have estimated net profit", topCrop.estimatedNetProfitPerAcreRupees > 0.0)
        assertTrue("Should have sustainable practices", topCrop.sustainablePractices.isNotEmpty())
        assertTrue("Should have climate and drought risk", topCrop.climateAndDroughtRisk.isNotBlank())
        assertTrue("Should have pest and disease risk", topCrop.pestAndDiseaseRisk.isNotBlank())
        assertTrue("Should have market glut risk", topCrop.marketGlutRisk.isNotBlank())
    }

    @Test
    fun testGenerateAdvisoryGracefullyHandlesDefaultKey() = runBlocking {
        // When running in unit test environment without live credentials,
        // it gracefully falls back without throwing exceptions
        val result = service.generateAdvisory(sampleInput)

        assertNotNull(result)
        assertTrue(result.recommendations.isNotEmpty())
        assertTrue(result.summaryVerdict.isNotBlank())
    }
}
