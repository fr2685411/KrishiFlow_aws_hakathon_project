package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.advisory.GeminiSellOrStoreRecommendation
import com.example.data.model.ProduceBatchEntity
import com.example.data.weather.FarmWeatherDashboardData
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiViewModel

/**
 * Gemini AI Harvest & Weather Profit Maximizer Card.
 * Analyzes active harvest inventory, live APMC market rates, and real-time hyper-local weather
 * to deliver recommendations on whether to sell immediately or store crops to maximize net farmer profits.
 */
@Composable
fun GeminiHarvestProfitMaximizerCard(
    viewModel: KrishiViewModel,
    batches: List<ProduceBatchEntity>,
    weatherData: FarmWeatherDashboardData,
    onNavigateToSurplusScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recommendation by viewModel.sellOrStoreRecommendation.collectAsState()
    val isLoading by viewModel.isSellOrStoreLoading.collectAsState()

    // Default crop selection from active inventory
    val defaultBatch = batches.firstOrNull { it.availableQuantityKg > 0 }
    var selectedBatchCode by remember(batches) {
        mutableStateOf(defaultBatch?.batchCode ?: "KF-TOM-101")
    }

    val currentSelectedBatch = batches.firstOrNull { it.batchCode == selectedBatchCode } ?: defaultBatch

    // Automatically trigger initial analysis if no recommendation is present yet
    LaunchedEffect(currentSelectedBatch?.batchCode) {
        if (recommendation == null && currentSelectedBatch != null) {
            viewModel.runGeminiSellOrStoreAnalysis(
                cropName = currentSelectedBatch.cropName,
                variety = currentSelectedBatch.variety,
                quantityKg = currentSelectedBatch.availableQuantityKg,
                askingPricePerKg = currentSelectedBatch.farmerAskingPricePerKg,
                storageCondition = currentSelectedBatch.storageCondition.name,
                freshnessDaysRemaining = currentSelectedBatch.expectedFreshnessDays
            )
        }
    }

    var isDetailsExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("gemini_harvest_profit_maximizer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with AI Sparkle badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Gemini Profit Maximizer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldLight
                            ) {
                                Text(
                                    text = "AI Sell vs. Store",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Harvest Shelf-Life × Weather Forecast × APMC Mandi Rates",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI Refresh / Re-analyze button
                IconButtonWithLoading(
                    isLoading = isLoading,
                    onClick = {
                        currentSelectedBatch?.let { batch ->
                            viewModel.runGeminiSellOrStoreAnalysis(
                                cropName = batch.cropName,
                                variety = batch.variety,
                                quantityKg = batch.availableQuantityKg,
                                askingPricePerKg = batch.farmerAskingPricePerKg,
                                storageCondition = batch.storageCondition.name,
                                freshnessDaysRemaining = batch.expectedFreshnessDays
                            )
                        }
                    }
                )
            }

            // Crop Batch Selection Chips
            if (batches.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Crop to Optimize:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(batches) { batch ->
                            val isSelected = batch.batchCode == selectedBatchCode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) GreenLight else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) GreenPrimary else Color.Transparent),
                                modifier = Modifier
                                    .clickable {
                                        selectedBatchCode = batch.batchCode
                                        viewModel.runGeminiSellOrStoreAnalysis(
                                            cropName = batch.cropName,
                                            variety = batch.variety,
                                            quantityKg = batch.availableQuantityKg,
                                            askingPricePerKg = batch.farmerAskingPricePerKg,
                                            storageCondition = batch.storageCondition.name,
                                            freshnessDaysRemaining = batch.expectedFreshnessDays
                                        )
                                    }
                                    .testTag("crop_select_chip_${batch.batchCode}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFlorist,
                                        contentDescription = null,
                                        tint = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${batch.cropName} (${batch.availableQuantityKg.toInt()} kg)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Context Strip (Weather & Market price used by Gemini)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                        Text(
                            text = "${weatherData.currentTemp.toInt()}°C, ${weatherData.humidity}% Hum",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                        Text(
                            text = "${weatherData.rainProbabilityToday}% Rain",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${weatherData.locationName.take(15)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Loading State or Recommendation Card
            if (isLoading) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldLight.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth().testTag("gemini_loading_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Gemini 3.5 Flash is analyzing...",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary
                            )
                            Text(
                                text = "Correlating 3-day rainfall, shelf-life rot curves & APMC terminal price rallies",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (recommendation != null) {
                val rec = recommendation!!

                // Big Decision Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(rec.timingDecision.colorHex).copy(alpha = 0.12f),
                    border = BorderStroke(1.5.dp, Color(rec.timingDecision.colorHex).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("gemini_recommendation_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(rec.timingDecision.colorHex)
                            ) {
                                Text(
                                    text = rec.timingDecision.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Expected Profit Boost
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "+${rec.potentialProfitGainPercent.toInt()}% Profit Gain",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenPrimary
                                    )
                                )
                            }
                        }

                        // Headline & Hindi context
                        Text(
                            text = rec.headline,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = rec.timingDecision.hindiLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(rec.timingDecision.colorHex)
                        )

                        // Primary reasoning
                        Text(
                            text = rec.primaryReasoning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Metrics Snapshot Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Action Window Tile
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LockClock, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(13.dp))
                                        Text("Action Window", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = rec.recommendedActionWindow,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }

                            // Net Profit per Kg
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(13.dp))
                                        Text("Net Added Margin", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "+₹${rec.projectedNetProfitPerKg}/kg",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                                    )
                                }
                            }

                            // Storage Risk
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(rec.timingDecision.colorHex), modifier = Modifier.size(13.dp))
                                        Text("Storage Risk", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = rec.storageRiskLevel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(rec.timingDecision.colorHex))
                                    )
                                }
                            }
                        }

                        // Expandable Details (Weather impact & Storage protocols)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDetailsExpanded = !isDetailsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDetailsExpanded) "Hide Deep AI Diagnostic" else "View Weather & Storage Protocols",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                            Icon(
                                imageVector = if (isDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        AnimatedVisibility(visible = isDetailsExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Weather impact
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                                        Text("Micro-Climate Risk Impact:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Text(
                                        text = rec.weatherImpactAnalysis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Storage guidelines
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                                        Text("Post-Harvest Handling Protocol:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Text(
                                        text = rec.storageGuidelines,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Target buyers
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                                        Text("Target Buyer Channels:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Text(
                                        text = rec.targetBuyersOrMarkets,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Execution Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateToSurplusScreen() },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("execute_gemini_action_btn")
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Sale / View Mandi Demand", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IconButtonWithLoading(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(36.dp)
            .clickable(enabled = !isLoading) { onClick() }
            .testTag("reanalyze_gemini_timing_btn")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = GreenPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Re-analyze with Gemini",
                    tint = GreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
