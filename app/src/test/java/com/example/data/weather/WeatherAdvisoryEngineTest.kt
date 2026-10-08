package com.example.data.weather

import com.example.data.location.FarmCoordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAdvisoryEngineTest {

    private val sampleCoords = FarmCoordinates(
        latitude = 20.0110,
        longitude = 73.7903,
        locationName = "Dindori Farm Hub, Nashik",
        isLiveGps = true
    )

    @Test
    fun testHeavyRainForecastTriggersPauseIrrigationAndRainAlert() {
        val rainCurrent = CurrentWeatherDto(
            temperature2m = 24.0,
            relativeHumidity2m = 92.0,
            apparentTemperature = 25.0,
            precipitation = 8.5,
            weatherCode = 65, // Heavy rain
            windSpeed10m = 18.0
        )
        val rainDaily = DailyWeatherDto(
            time = listOf("2026-10-05", "2026-10-06", "2026-10-07"),
            weatherCode = listOf(65, 63, 61),
            temperature2mMax = listOf(26.0, 25.0, 24.0),
            temperature2mMin = listOf(19.0, 18.0, 18.0),
            precipitationSum = listOf(18.0, 12.0, 5.0),
            precipitationProbabilityMax = listOf(85, 75, 60),
            windSpeed10mMax = listOf(22.0, 20.0, 16.0)
        )
        val response = OpenMeteoResponse(
            latitude = 20.0110,
            longitude = 73.7903,
            current = rainCurrent,
            daily = rainDaily
        )

        val dashboard = WeatherAdvisoryEngine.generateDashboardData(response, sampleCoords)

        // Verify alert
        val criticalAlert = dashboard.alerts.firstOrNull { it.severity == AlertSeverity.CRITICAL }
        assertNotNull("Should have a critical rain alert", criticalAlert)
        assertTrue(criticalAlert!!.message.contains("rainfall", ignoreCase = true))

        // Verify irrigation advice is to PAUSE
        assertEquals(IrrigationAction.PAUSE_IRRIGATION, dashboard.irrigationAdvice.action)
        assertTrue(dashboard.irrigationAdvice.waterSavingEstimatePercent >= 50)

        // Verify harvest advice is to POSTPONE or ACCELERATE
        assertTrue(
            dashboard.harvestAdvice.action == HarvestAction.POSTPONE_HARVEST ||
            dashboard.harvestAdvice.action == HarvestAction.ACCELERATE_HARVEST
        )
    }

    @Test
    fun testDrySunnyConditionsTriggerSafeHarvestWindow() {
        val dryCurrent = CurrentWeatherDto(
            temperature2m = 30.0,
            relativeHumidity2m = 45.0,
            apparentTemperature = 30.5,
            precipitation = 0.0,
            weatherCode = 0, // Clear sky
            windSpeed10m = 10.0
        )
        val dryDaily = DailyWeatherDto(
            time = listOf("2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08"),
            weatherCode = listOf(0, 0, 1, 0),
            temperature2mMax = listOf(31.0, 31.5, 30.0, 31.0),
            temperature2mMin = listOf(18.0, 18.5, 17.5, 18.0),
            precipitationSum = listOf(0.0, 0.0, 0.0, 0.0),
            precipitationProbabilityMax = listOf(5, 5, 10, 5),
            windSpeed10mMax = listOf(12.0, 10.0, 11.0, 10.0)
        )
        val response = OpenMeteoResponse(
            latitude = 20.0110,
            longitude = 73.7903,
            current = dryCurrent,
            daily = dryDaily
        )

        val dashboard = WeatherAdvisoryEngine.generateDashboardData(response, sampleCoords)

        // Verify harvest advice
        assertEquals(HarvestAction.SAFE_WINDOW, dashboard.harvestAdvice.action)
        assertTrue(dashboard.harvestAdvice.safeWindowDays >= 3)

        // Verify alert
        val favorableAlert = dashboard.alerts.firstOrNull { it.severity == AlertSeverity.FAVORABLE }
        assertNotNull("Should have favorable weather advisory", favorableAlert)
    }

    @Test
    fun testExtremeHeatConditionsTriggerEveningOnlyIrrigation() {
        val heatCurrent = CurrentWeatherDto(
            temperature2m = 38.5,
            relativeHumidity2m = 32.0,
            apparentTemperature = 41.0,
            precipitation = 0.0,
            weatherCode = 1,
            windSpeed10m = 14.0
        )
        val heatDaily = DailyWeatherDto(
            time = listOf("2026-10-05", "2026-10-06"),
            weatherCode = listOf(1, 1),
            temperature2mMax = listOf(39.0, 38.5),
            temperature2mMin = listOf(22.0, 21.0),
            precipitationSum = listOf(0.0, 0.0),
            precipitationProbabilityMax = listOf(5, 5),
            windSpeed10mMax = listOf(14.0, 15.0)
        )
        val response = OpenMeteoResponse(
            latitude = 20.0110,
            longitude = 73.7903,
            current = heatCurrent,
            daily = heatDaily
        )

        val dashboard = WeatherAdvisoryEngine.generateDashboardData(response, sampleCoords)

        // Verify heatwave alert
        val heatAlert = dashboard.alerts.firstOrNull { it.id.contains("heat", ignoreCase = true) }
        assertNotNull("Should have heat stress alert", heatAlert)

        // Verify irrigation advice is evening only
        assertEquals(IrrigationAction.EVENING_ONLY, dashboard.irrigationAdvice.action)
    }

    @Test
    fun testWeeklyForecastGeneratedProperly() {
        val dashboard = WeatherAdvisoryEngine.generateDashboardData(null, sampleCoords)
        assertNotNull(dashboard.weeklyForecast)
        assertTrue("Weekly forecast should have multiple days", dashboard.weeklyForecast.size >= 5)

        val today = dashboard.weeklyForecast.first()
        assertEquals("Today", today.dayOfWeek)
        assertTrue(today.maxTemp > 0)
        assertTrue(today.conditionName.isNotBlank())
        assertTrue(today.irrigationDirective.isNotBlank())
    }
}
