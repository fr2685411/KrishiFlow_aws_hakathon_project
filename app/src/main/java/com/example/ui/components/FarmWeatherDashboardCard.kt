package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.weather.AlertSeverity
import com.example.data.weather.DailyAgronomyForecast
import com.example.data.weather.FarmWeatherDashboardData
import com.example.data.weather.HarvestAction
import com.example.data.weather.IrrigationAction
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun FarmWeatherDashboardCard(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val weatherData by viewModel.weatherData.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isWeatherRefreshing.collectAsStateWithLifecycle()
    var isForecastExpanded by remember { mutableStateOf(false) }

    // Runtime location permission launcher for ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.refreshWeather(requestGps = true)
        }
    }

    val rotationTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val refreshAngle by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing)),
        label = "angle"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("farm_weather_dashboard_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. LOCATION API & GPS HEADER ROW
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (weatherData.isGpsLive) GreenPrimary else GoldPrimary)
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = weatherData.locationName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (weatherData.isGpsLive) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GreenLight
                                ) {
                                    Text(
                                        text = "LIVE GPS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Real-Time Agro-Meteorology & Alerts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (!weatherData.isGpsLive) {
                        OutlinedButton(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("weather_detect_gps_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use GPS", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshWeather(requestGps = true) },
                        modifier = Modifier.size(32.dp).testTag("weather_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh weather",
                            tint = GreenPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (isRefreshing) refreshAngle else 0f)
                        )
                    }
                }
            }

            // =========================================================================
            // 2. MAIN TEMPERATURE & LIVE CONDITIONS BANNER
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF065F46),
                                Color(0xFF047857),
                                Color(0xFF0D9488)
                            )
                        )
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${weatherData.currentTemp.toInt()}°C",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Feels ${weatherData.feelsLikeTemp.toInt()}°C",
                            fontSize = 12.sp,
                            color = Color(0xFFBBF7D0),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = weatherData.conditionDescription,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF7DD3FC), modifier = Modifier.size(14.dp))
                            Text("Rain: ${weatherData.rainProbabilityToday}%", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = Color(0xFFFDE047), modifier = Modifier.size(14.dp))
                            Text("Wind: ${weatherData.windSpeed.toInt()} km/h", fontSize = 11.sp, color = Color.White)
                        }
                        Text(
                            text = "Humidity: ${weatherData.humidity}%",
                            fontSize = 11.sp,
                            color = Color(0xFFDCFCE7)
                        )
                    }

                    Icon(
                        imageVector = getWeatherIcon(weatherData.weatherCode),
                        contentDescription = null,
                        tint = Color(0xFFFEF08A),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // =========================================================================
            // 3. REAL-TIME AGRICULTURAL WEATHER ALERT BANNER
            // =========================================================================
            weatherData.alerts.firstOrNull()?.let { alert ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(alert.severity.colorHex).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(alert.severity.colorHex).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("weather_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = when (alert.severity) {
                                AlertSeverity.CRITICAL -> Icons.Default.Warning
                                AlertSeverity.WARNING -> Icons.Default.Warning
                                AlertSeverity.ADVISORY -> Icons.Default.Cloud
                                AlertSeverity.FAVORABLE -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = Color(alert.severity.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${alert.title} • ${alert.hindiTitle}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(alert.severity.colorHex)
                                )
                            )
                            Text(
                                text = alert.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "💡 Action: ${alert.recommendation}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(alert.severity.colorHex)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 4. IRRIGATION & HARVEST SCHEDULE ADVISORY CARDS
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CARD A: IRRIGATION ADVISORY
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(weatherData.irrigationAdvice.action.colorHex).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.weight(1f).testTag("irrigation_advisory_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(weatherData.irrigationAdvice.action.colorHex),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Irrigation Schedule",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(weatherData.irrigationAdvice.action.colorHex).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = weatherData.irrigationAdvice.action.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(weatherData.irrigationAdvice.action.colorHex)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = weatherData.irrigationAdvice.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )

                        Text(
                            text = "⚡ Saves ~${weatherData.irrigationAdvice.waterSavingEstimatePercent}% Water & Power",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        )
                    }
                }

                // CARD B: HARVEST SCHEDULE ADVISORY
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(weatherData.harvestAdvice.action.colorHex).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.weight(1f).testTag("harvest_advisory_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = Color(weatherData.harvestAdvice.action.colorHex),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Harvest Window",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(weatherData.harvestAdvice.action.colorHex).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = weatherData.harvestAdvice.action.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(weatherData.harvestAdvice.action.colorHex)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = weatherData.harvestAdvice.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )

                        Text(
                            text = "⏱️ ${weatherData.harvestAdvice.safeWindowDays} Safe Days Window",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(weatherData.harvestAdvice.action.colorHex)
                            )
                        )
                    }
                }
            }

            // =========================================================================
            // 5. EXPANDABLE 7-DAY AGRONOMIC FORECAST
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isForecastExpanded = !isForecastExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isForecastExpanded) "Hide 7-Day Forecast" else "View 7-Day Farm Weather & Irrigation Forecast",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                }
                Icon(
                    imageVector = if (isForecastExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GreenPrimary
                )
            }

            AnimatedVisibility(visible = isForecastExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Daily Farm Operations & Irrigation Guide (7 दिन का पूर्वानुमान):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weatherData.weeklyForecast.forEach { dayForecast ->
                            DailyForecastChipCard(dayForecast = dayForecast)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastChipCard(dayForecast: DailyAgronomyForecast) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.width(118.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dayForecast.dayOfWeek,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dayForecast.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = getWeatherIcon(dayForecast.weatherCode),
                contentDescription = null,
                tint = if (dayForecast.rainProbability > 50) Color(0xFF0284C7) else GoldPrimary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "${dayForecast.maxTemp.toInt()}° / ${dayForecast.minTemp.toInt()}°",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )

            // Rain probability bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(10.dp))
                Text(
                    text = "${dayForecast.rainProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayForecast.rainProbability >= 60) Color(0xFFDC2626) else Color(0xFF0284C7)
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (dayForecast.rainProbability >= 60) Color(0xFFE0F2FE) else GreenLight
            ) {
                Text(
                    text = dayForecast.irrigationDirective,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dayForecast.rainProbability >= 60) Color(0xFF0284C7) else GreenPrimary
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
