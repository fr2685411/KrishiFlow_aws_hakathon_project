package com.example.data.weather

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: CurrentWeatherDto?,
    @Json(name = "daily") val daily: DailyWeatherDto?
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature2m: Double = 28.0,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double = 65.0,
    @Json(name = "apparent_temperature") val apparentTemperature: Double = 29.5,
    @Json(name = "precipitation") val precipitation: Double = 0.0,
    @Json(name = "weather_code") val weatherCode: Int = 1,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double = 12.0
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    @Json(name = "time") val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double> = emptyList()
)

enum class AlertSeverity(val colorHex: Long) {
    CRITICAL(0xFFDC2626),
    WARNING(0xFFD97706),
    ADVISORY(0xFF0284C7),
    FAVORABLE(0xFF15803D)
}

data class WeatherAlert(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val severity: AlertSeverity,
    val message: String,
    val recommendation: String
)

enum class IrrigationAction(val label: String, val hindiLabel: String, val colorHex: Long) {
    PAUSE_IRRIGATION("Pause Irrigation (सिंचाई रोकें)", "बारिश की संभावना - पानी व बिजली बचाएं", 0xFF0284C7),
    LIGHT_DRIP_ONLY("Light Drip Only (हल्की ड्रिप)", "नमी बनी रहे, जल भराव से बचें", 0xFF0D9488),
    FULL_IRRIGATION("Scheduled Irrigation (नियमित सिंचाई)", "शुष्क मौसम - ड्रिप या नाली से सिंचाई करें", 0xFF15803D),
    EVENING_ONLY("Evening Irrigation Only (शाम को सिंचाई)", "उच्च तापमान - वाष्पीकरण से बचें", 0xFFD97706)
}

data class IrrigationAdvice(
    val action: IrrigationAction,
    val headline: String,
    val detail: String,
    val waterSavingEstimatePercent: Int
)

enum class HarvestAction(val label: String, val hindiLabel: String, val colorHex: Long) {
    SAFE_WINDOW("Optimal Harvest Window (उत्तम कटाई समय)", "खुला मौसम - फसल कटाई व सुखाने का सही समय", 0xFF15803D),
    ACCELERATE_HARVEST("Accelerate Harvest Before Rain (जल्द कटाई करें)", "बारिश आने वाली है - पकी फसल को शेड में रखें", 0xFFDC2626),
    POSTPONE_HARVEST("Postpone Outdoor Harvest (कटाई स्थगित करें)", "बारिश या नमी अधिक है - सड़न का जोखिम", 0xFFD97706)
}

data class HarvestAdvice(
    val action: HarvestAction,
    val headline: String,
    val detail: String,
    val safeWindowDays: Int
)

data class DailyAgronomyForecast(
    val date: String,
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainProbability: Int,
    val precipitationMm: Double,
    val weatherCode: Int,
    val conditionName: String,
    val irrigationDirective: String,
    val harvestSuitability: String
)

data class FarmWeatherDashboardData(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsLive: Boolean,
    val currentTemp: Double,
    val feelsLikeTemp: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val precipitationMm: Double,
    val rainProbabilityToday: Int,
    val alerts: List<WeatherAlert>,
    val irrigationAdvice: IrrigationAdvice,
    val harvestAdvice: HarvestAdvice,
    val weeklyForecast: List<DailyAgronomyForecast>,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
