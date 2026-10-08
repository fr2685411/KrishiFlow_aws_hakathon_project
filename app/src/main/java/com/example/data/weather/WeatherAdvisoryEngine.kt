package com.example.data.weather

import com.example.data.location.FarmCoordinates
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object WeatherAdvisoryEngine {

    fun generateDashboardData(
        response: OpenMeteoResponse?,
        coords: FarmCoordinates
    ): FarmWeatherDashboardData {
        val current = response?.current ?: getSimulatedCurrent()
        val daily = response?.daily ?: getSimulatedDaily()

        val temp = current.temperature2m
        val apparentTemp = current.apparentTemperature
        val humidity = current.relativeHumidity2m.roundToInt()
        val windSpeed = current.windSpeed10m
        val precipitationMm = current.precipitation
        val weatherCode = current.weatherCode
        val conditionDesc = decodeWeatherCode(weatherCode)

        val rainProbabilityToday = daily.precipitationProbabilityMax.firstOrNull() ?: if (precipitationMm > 0) 80 else 15

        // 1. Analyze Alerts
        val alerts = mutableListOf<WeatherAlert>()
        if (rainProbabilityToday >= 65 || precipitationMm >= 8.0) {
            alerts.add(
                WeatherAlert(
                    id = "alert-rain-deluge",
                    title = "Precipitation Warning",
                    hindiTitle = "भारी वर्षा चेतावनी (${rainProbabilityToday}% संभावना)",
                    severity = AlertSeverity.CRITICAL,
                    message = "Significant rainfall expected in your farm zone. Soil moisture will surge.",
                    recommendation = "Turn off tube wells/drip pumps immediately. Clear drainage furrows to avoid waterlogging."
                )
            )
        } else if (temp >= 36.0) {
            alerts.add(
                WeatherAlert(
                    id = "alert-heat-stress",
                    title = "High Heatwave Advisory",
                    hindiTitle = "तीव्र ताप लहर (${temp.toInt()}°C)",
                    severity = AlertSeverity.WARNING,
                    message = "High thermal evaporation stress. Risk of flower drop in tomato, chilli, and pomegranate.",
                    recommendation = "Mulch plant beds with organic straw. Conduct irrigation strictly during dusk/evening."
                )
            )
        } else if (windSpeed >= 25.0) {
            alerts.add(
                WeatherAlert(
                    id = "alert-wind-hazard",
                    title = "High Gust Winds (${windSpeed.toInt()} km/h)",
                    hindiTitle = "तेज हवाएं - छिड़काव स्थगित करें",
                    severity = AlertSeverity.WARNING,
                    message = "Wind speeds exceed safe thresholds for foliar spraying.",
                    recommendation = "Avoid spraying bio-pesticides or neem oil to prevent spray drift away from the target canopy."
                )
            )
        } else {
            alerts.add(
                WeatherAlert(
                    id = "alert-favorable",
                    title = "Optimal Agro-Climate Window",
                    hindiTitle = "अनुकूल कृषि मौसम • उत्तम समय",
                    severity = AlertSeverity.FAVORABLE,
                    message = "Clear skies, balanced humidity ($humidity%), and steady gentle breeze.",
                    recommendation = "Ideal timing for planting, weeding, and solar grain curing."
                )
            )
        }

        // 2. Evaluate Irrigation Directive
        val irrigationAdvice = when {
            rainProbabilityToday >= 55 || precipitationMm >= 4.0 -> {
                IrrigationAdvice(
                    action = IrrigationAction.PAUSE_IRRIGATION,
                    headline = "Pause Irrigation (सिंचाई रोकें)",
                    detail = "Natural rainfall will supply required root hydration. Halting electric pumps saves ~65% water and prevents collar rot.",
                    waterSavingEstimatePercent = 65
                )
            }
            temp >= 35.0 -> {
                IrrigationAdvice(
                    action = IrrigationAction.EVENING_ONLY,
                    headline = "Evening Drip Only (शाम को सिंचाई)",
                    detail = "Daytime heat causes intense evaporation. Irrigate after 5:30 PM to achieve 90% water penetration to roots.",
                    waterSavingEstimatePercent = 35
                )
            }
            humidity <= 35 -> {
                IrrigationAdvice(
                    action = IrrigationAction.FULL_IRRIGATION,
                    headline = "Scheduled Irrigation (नियमित ड्रिप)",
                    detail = "Dry atmospheric vapor deficit. Maintain uniform soil moisture in active root zones.",
                    waterSavingEstimatePercent = 25
                )
            }
            else -> {
                IrrigationAdvice(
                    action = IrrigationAction.LIGHT_DRIP_ONLY,
                    headline = "Light Drip Cycle (हल्की ड्रिप)",
                    detail = "Adequate ambient moisture. Run a short 30-45 min cycle to preserve soil aeration.",
                    waterSavingEstimatePercent = 45
                )
            }
        }

        // 3. Evaluate Harvest Directive
        val rainInNext2Days = daily.precipitationProbabilityMax.take(3).any { it >= 60 }
        val harvestAdvice = when {
            precipitationMm >= 2.0 -> {
                HarvestAdvice(
                    action = HarvestAction.POSTPONE_HARVEST,
                    headline = "Postpone Outdoor Harvest (कटाई रोकें)",
                    detail = "Damp vegetation and soggy soil. Harvesting wet produce causes mud blemishes and fungal rot.",
                    safeWindowDays = 0
                )
            }
            rainInNext2Days -> {
                HarvestAdvice(
                    action = HarvestAction.ACCELERATE_HARVEST,
                    headline = "Accelerate Harvest Before Rain (जल्द कटाई करें)",
                    detail = "Rain arriving in 24–48 hours. Harvest mature tomato, onion, and vegetable crops today and move into ventilated storage.",
                    safeWindowDays = 1
                )
            }
            else -> {
                HarvestAdvice(
                    action = HarvestAction.SAFE_WINDOW,
                    headline = "Optimal 3-Day Harvest Window (उत्तम कटाई समय)",
                    detail = "Consecutive dry, sunny days ahead. Ideal window for harvesting pulses, grains, and horticulture produce.",
                    safeWindowDays = 3
                )
            }
        }

        // 4. Generate 7-Day Forecast
        val weeklyForecast = mutableListOf<DailyAgronomyForecast>()
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val dayCount = daily.time.size.coerceAtMost(7).coerceAtLeast(1)
        for (i in 0 until dayCount) {
            val maxT = daily.temperature2mMax.getOrElse(i) { temp + 2.0 }
            val minT = daily.temperature2mMin.getOrElse(i) { temp - 6.0 }
            val rainProb = daily.precipitationProbabilityMax.getOrElse(i) { 10 }
            val rainMm = daily.precipitationSum.getOrElse(i) { 0.0 }
            val code = daily.weatherCode.getOrElse(i) { 1 }

            val irriDir = when {
                rainProb >= 60 || rainMm >= 4.0 -> "Pause Irrigation"
                maxT >= 35.0 -> "Dusk/Evening Drip"
                else -> "Regular Drip Cycle"
            }

            val harvestDir = when {
                rainMm >= 3.0 -> "Postpone"
                rainProb >= 60 -> "Accelerate Today"
                else -> "Safe Window"
            }

            weeklyForecast.add(
                DailyAgronomyForecast(
                    date = dateFormat.format(calendar.time),
                    dayOfWeek = if (i == 0) "Today" else dayFormat.format(calendar.time),
                    maxTemp = (maxT * 10).roundToInt() / 10.0,
                    minTemp = (minT * 10).roundToInt() / 10.0,
                    rainProbability = rainProb,
                    precipitationMm = (rainMm * 10).roundToInt() / 10.0,
                    weatherCode = code,
                    conditionName = decodeWeatherCode(code),
                    irrigationDirective = irriDir,
                    harvestSuitability = harvestDir
                )
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return FarmWeatherDashboardData(
            locationName = coords.locationName,
            latitude = coords.latitude,
            longitude = coords.longitude,
            isGpsLive = coords.isLiveGps,
            currentTemp = (temp * 10).roundToInt() / 10.0,
            feelsLikeTemp = (apparentTemp * 10).roundToInt() / 10.0,
            humidity = humidity,
            windSpeed = (windSpeed * 10).roundToInt() / 10.0,
            weatherCode = weatherCode,
            conditionDescription = conditionDesc,
            precipitationMm = precipitationMm,
            rainProbabilityToday = rainProbabilityToday,
            alerts = alerts,
            irrigationAdvice = irrigationAdvice,
            harvestAdvice = harvestAdvice,
            weeklyForecast = weeklyForecast
        )
    }

    fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky (साफ मौसम)"
            1 -> "Mainly Clear (हल्के बादल)"
            2 -> "Partly Cloudy (आंशिक बादल)"
            3 -> "Overcast (घने बादल)"
            45, 48 -> "Foggy / Mist (कोहरा)"
            51, 53, 55 -> "Light Drizzle (बूंदाबांदी)"
            61, 63 -> "Moderate Rain (बारिश)"
            65 -> "Heavy Rain (तेज बारिश)"
            80, 81, 82 -> "Rain Showers (वर्षा बौछारें)"
            95, 96, 99 -> "Thunderstorm (आंधी-तूफान)"
            else -> "Scattered Clouds (बादल)"
        }
    }

    private fun getSimulatedCurrent(): CurrentWeatherDto {
        return CurrentWeatherDto(
            temperature2m = 29.4,
            relativeHumidity2m = 58.0,
            apparentTemperature = 30.8,
            precipitation = 0.0,
            weatherCode = 1,
            windSpeed10m = 12.5
        )
    }

    private fun getSimulatedDaily(): DailyWeatherDto {
        return DailyWeatherDto(
            time = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7"),
            weatherCode = listOf(1, 2, 61, 1, 0, 2, 1),
            temperature2mMax = listOf(31.5, 30.0, 27.5, 29.0, 32.0, 31.0, 30.5),
            temperature2mMin = listOf(19.0, 18.5, 17.0, 17.5, 18.0, 19.5, 18.0),
            precipitationSum = listOf(0.0, 1.2, 14.5, 0.5, 0.0, 0.0, 0.0),
            precipitationProbabilityMax = listOf(15, 35, 85, 20, 5, 10, 10),
            windSpeed10mMax = listOf(14.0, 18.5, 26.0, 12.0, 11.0, 13.0, 12.0)
        )
    }
}
