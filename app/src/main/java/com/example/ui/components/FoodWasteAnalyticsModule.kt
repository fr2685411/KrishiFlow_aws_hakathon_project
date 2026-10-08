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
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.ActionableTipCategory
import com.example.data.analytics.CropWasteAnalysis
import com.example.data.analytics.FoodWasteActionableTip
import com.example.data.analytics.FoodWasteCalculatorEngine
import com.example.data.analytics.PotentialFoodWasteReport
import com.example.data.analytics.WasteSeverity
import com.example.data.model.HarvestHistoryLogEntity
import com.example.data.model.ProduceBatchEntity
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageLight
import com.example.ui.theme.SpoilageRed

/**
 * Interactive food waste analytics module that computes potential food waste
 * based on historical sales vs. harvest records and provides actionable tips
 * on how to minimize surplus loss before spoilage.
 */
@Composable
fun FoodWasteAnalyticsModule(
    harvestLogs: List<HarvestHistoryLogEntity>,
    activeBatches: List<ProduceBatchEntity> = emptyList(),
    onNavigateToSurplusScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val report: PotentialFoodWasteReport = remember(harvestLogs, activeBatches) {
        FoodWasteCalculatorEngine.calculatePotentialWaste(harvestLogs, activeBatches)
    }

    var selectedCropForTips by remember { mutableStateOf<String?>(null) }
    var showAllTips by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("food_waste_analytics_module")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. MODULE HEADER & SEVERITY BADGE
            // =========================================================================
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(report.overallSeverity.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = Color(report.overallSeverity.colorHex),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Surplus & Food Waste Forecaster",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(report.overallSeverity.colorHex).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = report.overallSeverity.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(report.overallSeverity.colorHex)
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Historical Harvest vs. Realized Sales Gap Analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =========================================================================
            // 2. KEY METRIC SUMMARY TILES (UNSOLD SURPLUS, FINANCIAL LOSS, CO2 BURDEN)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Surplus At Risk
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SpoilageLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, SpoilageRed.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f).testTag("kpi_waste_surplus_kg")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = SpoilageRed, modifier = Modifier.size(14.dp))
                            Text("Unsold Surplus", style = MaterialTheme.typography.labelSmall, color = SpoilageRed)
                        }
                        Text(
                            text = "${report.totalHistoricalUnsoldKg.toInt()} kg",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SpoilageRed
                        )
                        Text(
                            text = "${report.overallWastePercentage}% of harvest",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = Color(0xFF991B1B)
                        )
                    }
                }

                // Financial Value At Risk
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f).testTag("kpi_waste_loss_rupees")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Text("Loss at Risk", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                        }
                        Text(
                            text = "₹${report.totalFinancialLossRupees.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )
                        Text(
                            text = "Preventable Revenue",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = Color(0xFF854D0E)
                        )
                    }
                }

                // Carbon Burden Tile
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenLight.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f).testTag("kpi_waste_co2_kg")
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                            Text("CO2 Footprint", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                        }
                        Text(
                            text = "${report.totalCo2WasteKg.toInt()} kg",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )
                        Text(
                            text = "Avoidable GHG",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = Color(0xFF166534)
                        )
                    }
                }
            }

            // =========================================================================
            // 3. PER-CROP SURPLUS & POTENTIAL LOSS BREAKDOWN
            // =========================================================================
            Text(
                text = "Crop-Wise Surplus & Vulnerability Breakdown",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.cropBreakdowns.forEach { cropWaste ->
                    val isExpanded = selectedCropForTips == cropWaste.cropName

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) GreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCropForTips = if (isExpanded) null else cropWaste.cropName
                            }
                            .testTag("crop_waste_row_${cropWaste.cropName.replace(" ", "_")}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(cropWaste.severity.colorHex).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Agriculture,
                                            contentDescription = null,
                                            tint = Color(cropWaste.severity.colorHex),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = cropWaste.cropName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Harvest: ${cropWaste.totalHarvestedKg.toInt()} kg | Sold: ${cropWaste.totalSoldKg.toInt()} kg",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${cropWaste.potentialWastePercentage}% Loss",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(cropWaste.severity.colorHex)
                                            )
                                        )
                                        Text(
                                            text = "-₹${cropWaste.financialLossAtRiskRupees.toInt()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Loss progress bar
                            LinearProgressIndicator(
                                progress = { (cropWaste.potentialWastePercentage / 30.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(cropWaste.severity.colorHex),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Vulnerability cause preview
                            Text(
                                text = "⚠️ Root Cause: ${cropWaste.primaryLossCause}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Expandable Actionable Tips for this Crop
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                    Text(
                                        text = "Actionable Tips to Minimize ${cropWaste.cropName} Spoilage:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                    )

                                    cropWaste.actionableTips.forEach { tip ->
                                        ActionableTipCard(tip = tip)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 4. ACTIONABLE PREVENTATIVE TIPS SUMMARY (ZERO-WASTE PLAYBOOK)
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Zero-Waste Farm Action Playbook",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GreenLight,
                    modifier = Modifier.clickable { showAllTips = !showAllTips }
                ) {
                    Text(
                        text = if (showAllTips) "Collapse Tips" else "View All (${report.highPriorityActionableTips.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val tipsToDisplay = if (showAllTips) report.highPriorityActionableTips else report.highPriorityActionableTips.take(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tipsToDisplay.forEach { tip ->
                    ActionableTipCard(tip = tip)
                }
            }

            // Bottom action: Jump to Surplus Clearance Queue
            OutlinedButton(
                onClick = onNavigateToSurplusScreen,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("go_to_surplus_clearance_btn")
            ) {
                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Post Surplus to Mandi / Discount Marketplace (0% Spoilage)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
            }
        }
    }
}

/**
 * Visual card displaying an actionable waste reduction tip
 */
@Composable
private fun ActionableTipCard(tip: FoodWasteActionableTip) {
    val categoryIcon = when (tip.category) {
        ActionableTipCategory.STORAGE -> Icons.Default.Warehouse
        ActionableTipCategory.DISCOUNT -> Icons.Default.LocalOffer
        ActionableTipCategory.FOOD_PROCESSING -> Icons.Default.SolarPower
        ActionableTipCategory.BUYER_MATCH -> Icons.Default.Shield
        ActionableTipCategory.HARVEST_TIMING -> Icons.Default.Schedule
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF111827)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GreenPrimary
                    ) {
                        Text(
                            text = "-${tip.expectedLossReductionPct}% Waste",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color(0xFF4B5563)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "🎯 ${tip.targetCrop}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "⏱️ ${tip.urgencyLevel}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
