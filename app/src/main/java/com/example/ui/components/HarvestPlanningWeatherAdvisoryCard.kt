package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.weather.DailyAgronomyForecast
import com.example.data.weather.FarmWeatherDashboardData
import com.example.data.weather.HarvestAction
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed

/**
 * Dedicated Agricultural Weather & Harvest Planning Card.
 * Uses real-time weather from Open-Meteo free API to provide
 * actionable harvest scheduling, field spraying windows, and
 * spoilage prevention advice for farmers and procurement leads.
 */
@Composable
fun HarvestPlanningWeatherAdvisoryCard(
    weatherData: FarmWeatherDashboardData,
    onOpenHarvestLogs: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCropTab by remember { mutableIntStateOf(0) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("harvest_planning_weather_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. HEADER ROW: WEATHER MODULE & FREE API BADGE
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GreenPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Harvest Planning Advisory",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "FREE API",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "${weatherData.locationName} • Open-Meteo Meteorological Feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshWeather,
                    modifier = Modifier.testTag("harvest_weather_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Forecast",
                        tint = GreenPrimary
                    )
                }
            }

            // =========================================================================
            // 2. PRIMARY HARVEST WINDOW ACTION BANNER
            // =========================================================================
            val harvestActionColor = Color(weatherData.harvestAdvice.action.colorHex)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = harvestActionColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, harvestActionColor.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth().testTag("harvest_action_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(harvestActionColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (weatherData.harvestAdvice.action) {
                                HarvestAction.SAFE_WINDOW -> Icons.Default.CheckCircle
                                HarvestAction.ACCELERATE_HARVEST -> Icons.Default.Warning
                                HarvestAction.POSTPONE_HARVEST -> Icons.Default.Thunderstorm
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = weatherData.harvestAdvice.headline,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = harvestActionColor
                        )
                        Text(
                            text = weatherData.harvestAdvice.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "⏱️ Safe Window: ${weatherData.harvestAdvice.safeWindowDays} Days",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = harvestActionColor
                                )
                            )
                            Text(
                                text = "💧 Rain Prob: ${weatherData.rainProbabilityToday}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 3. FIELD OPERATIONS SUITABILITY METRICS
            // =========================================================================
            Text(
                text = "Field Operations Suitability (मौसम अनुसार कृषि कार्य):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Spraying Suitability
                val isSafeSpraying = weatherData.windSpeed < 18.0 && weatherData.rainProbabilityToday < 50
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSafeSpraying) GreenLight.copy(alpha = 0.5f) else Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, if (isSafeSpraying) GreenPrimary.copy(alpha = 0.3f) else SpoilageRed.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = if (isSafeSpraying) GreenPrimary else SpoilageRed, modifier = Modifier.size(14.dp))
                            Text("Spraying Window", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSafeSpraying) "Favorable (${weatherData.windSpeed.toInt()} km/h)" else "High Drift Risk",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSafeSpraying) GreenPrimary else SpoilageRed
                            )
                        )
                        Text(
                            text = if (isSafeSpraying) "Safe for neem & bio-sprays" else "Postpone foliar sprays",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 2: Sun Curing & Drying
                val isSunDryingGood = weatherData.humidity < 70 && weatherData.rainProbabilityToday < 35 && weatherData.currentTemp >= 24.0
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSunDryingGood) GoldLight.copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isSunDryingGood) GoldPrimary.copy(alpha = 0.3f) else Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = if (isSunDryingGood) GoldPrimary else Color(0xFF64748B), modifier = Modifier.size(14.dp))
                            Text("Sun Curing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSunDryingGood) "Optimal Drying" else "Slow Evaporation",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSunDryingGood) GoldPrimary else Color(0xFF64748B)
                            )
                        )
                        Text(
                            text = if (isSunDryingGood) "Ideal for onions & grains" else "High ambient moisture",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Metric 3: Machinery & Soil Trafficability
                val isSoilDry = weatherData.precipitationMm < 2.0 && weatherData.rainProbabilityToday < 60
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSoilDry) GreenLight.copy(alpha = 0.5f) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, if (isSoilDry) GreenPrimary.copy(alpha = 0.3f) else GoldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = if (isSoilDry) GreenPrimary else GoldPrimary, modifier = Modifier.size(14.dp))
                            Text("Tractor / Combine", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSoilDry) "Firm Soil" else "Wet Soil Caution",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSoilDry) GreenPrimary else GoldPrimary
                            )
                        )
                        Text(
                            text = if (isSoilDry) "No wheel compaction" else "Risk of tyre sinkage",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =========================================================================
            // 4. CROP-SPECIFIC HARVEST GUIDANCE TABS
            // =========================================================================
            Text(
                text = "Crop-Specific Harvest Preparedness (फसल विशिष्ट कटाई सलाह):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val cropCategories = listOf("🍅 Perishables", "🧅 Bulb & Tuber", "🌾 Grains & Pulses", "🍇 Horticulture")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                cropCategories.forEachIndexed { index, title ->
                    val isSelected = selectedCropTab == index
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { selectedCropTab = index }
                            .testTag("crop_advisory_tab_$index")
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Content for selected crop category
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (selectedCropTab) {
                        0 -> {
                            Text("Tomato, Capsicum, Brinjal, Okra, Leafy Greens:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenPrimary)
                            Text(
                                text = if (weatherData.rainProbabilityToday >= 50)
                                    "⚠️ Rain expected within 48h! Harvest mature breaker/pink stage tomatoes today. Rain creates micro-cracks in skin leading to fruit rot."
                                else
                                    "✅ Dry conditions. Harvest in early morning (6:00 AM - 9:30 AM) when field heat is low. Move directly to shaded sorting bay.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        1 -> {
                            Text("Onion, Garlic, Ginger, Potato:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldPrimary)
                            Text(
                                text = if (weatherData.humidity > 70 || weatherData.rainProbabilityToday > 40)
                                    "⚠️ High ambient humidity! Do not harvest onions today. Wet necks cannot cure properly and will rot in storage."
                                else
                                    "✅ Clear weather suitable for onion harvesting and field windrow curing. 3 sunny days will dry leaves and seal bulbs.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        2 -> {
                            Text("Wheat, Rice, Gram, Soybean, Moong:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenPrimary)
                            Text(
                                text = "Harvest grains only when moisture is below 12-14%. If rain is predicted within 72h, accelerate combine harvesting and tarp store bags immediately.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        3 -> {
                            Text("Pomegranate, Grapes, Citrus, Guava:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldPrimary)
                            Text(
                                text = "High ambient heat followed by rain causes severe fruit cracking in pomegranates and berry drop in grapes. Pick export/Grade A fruit ahead of wet spells.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 5. 7-DAY AGRO-METEOROLOGY FORECAST HORIZONTAL SCROLL
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = "7-Day Agronomy Forecast (7 दिन का पूर्वानुमान)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Open-Meteo V1",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weatherData.weeklyForecast.forEach { forecast ->
                    DailyForecastCard(forecast = forecast)
                }
            }

            // =========================================================================
            // 6. ACTION SHORTCUTS (ROOM DB HARVEST LOGS & ATTRIBUTION)
            // =========================================================================
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenHarvestLogs,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("weather_open_harvest_logs_btn")
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Harvest Logs & PDF", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Text(
                        text = "Zero-Key Free API",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(forecast: DailyAgronomyForecast) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = forecast.dayOfWeek,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = forecast.date,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = getWeatherIcon(forecast.weatherCode),
                contentDescription = null,
                tint = if (forecast.rainProbability > 50) Color(0xFF0284C7) else GoldPrimary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "${forecast.maxTemp.toInt()}° / ${forecast.minTemp.toInt()}°",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(10.dp))
                Text(
                    text = "${forecast.rainProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (forecast.rainProbability >= 60) Color(0xFFDC2626) else Color(0xFF0284C7)
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = when (forecast.harvestSuitability) {
                    "Postpone" -> SpoilageRed.copy(alpha = 0.15f)
                    "Accelerate Today" -> Color(0xFFFEF3C7)
                    else -> GreenLight
                }
            ) {
                Text(
                    text = forecast.harvestSuitability,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (forecast.harvestSuitability) {
                            "Postpone" -> SpoilageRed
                            "Accelerate Today" -> GoldPrimary
                            else -> GreenPrimary
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
        }
    }
}

private fun getWeatherIcon(weatherCode: Int): ImageVector {
    return when (weatherCode) {
        0 -> Icons.Default.WbSunny
        1, 2 -> Icons.Default.WbCloudy
        3 -> Icons.Default.Cloud
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop
        95, 96, 99 -> Icons.Default.Thunderstorm
        else -> Icons.Default.WbCloudy
    }
}
