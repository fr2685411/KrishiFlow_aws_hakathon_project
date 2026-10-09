package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import com.example.data.model.CropPriceSpikeAlert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BatchStatus
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.StorageCondition
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageLight
import com.example.ui.theme.SpoilageRed
import com.example.ui.theme.WarningLight
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import java.util.Locale

enum class ShelfLifeFilter(val label: String, val hindiLabel: String) {
    ALL("All Harvest Stock", "कुल भंडार"),
    CRITICAL("🚨 Critical (<36h)", "सड़न का गंभीर खतरा"),
    MODERATE("⚠️ Moderate (36-72h)", "मध्यम शेल्फ-लाइफ"),
    FRESH("🌿 Fresh (>72h)", "ताजा उपज")
}

data class RedistributionChannel(
    val id: String,
    val name: String,
    val description: String,
    val recommendedDiscount: Int
)

val REDISTRIBUTION_CHANNELS = listOf(
    RedistributionChannel(
        id = "local_mandi_cart",
        name = "Local Sabzi Mandi & Pushcart Vendors",
        description = "Instant evening market clearance for fast neighborhood consumption (20% off).",
        recommendedDiscount = 20
    ),
    RedistributionChannel(
        id = "food_processing",
        name = "Food Processing & Puree/Flake Units",
        description = "Bulk absorption for tomato puree, dried onion flakes, and starch conversion (35% off).",
        recommendedDiscount = 35
    ),
    RedistributionChannel(
        id = "community_zero_waste",
        name = "Community Food Rescue & Bulk Kitchens",
        description = "Zero-waste redistribution to regional community mess and dhabas (25% off).",
        recommendedDiscount = 25
    ),
    RedistributionChannel(
        id = "biogas_livestock",
        name = "Livestock Fodder & Biogas Digestion",
        description = "Complete organic salvage preventing landfill methane emission (60% off / subsidy).",
        recommendedDiscount = 60
    )
)

/**
 * Dashboard component to track current harvest inventory, including real-time
 * shelf-life decay monitoring and proactive alerts for surplus redistribution before spoilage.
 */
@Composable
fun HarvestInventoryShelfLifeDashboard(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSurplusScreen: () -> Unit = {},
    onOpenHarvestHistoryLogs: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(ShelfLifeFilter.ALL) }
    var batchForRedistribution by remember { mutableStateOf<ProduceBatchEntity?>(null) }

    // Active batches in storage / inventory
    val activeBatches = remember(uiState.batches) {
        uiState.batches.filter { it.status != BatchStatus.COMPLETED && it.status != BatchStatus.SPOILED && it.availableQuantityKg > 0 }
    }

    val criticalBatches = remember(activeBatches) {
        activeBatches.filter { it.remainingHours in 1..36 || it.freshnessPercentage <= 35 }
    }

    val moderateBatches = remember(activeBatches) {
        activeBatches.filter { it.remainingHours in 37..72 && it.freshnessPercentage > 35 }
    }

    val freshBatches = remember(activeBatches) {
        activeBatches.filter { it.remainingHours > 72 && it.freshnessPercentage > 50 }
    }

    val filteredBatches = remember(activeBatches, selectedFilter) {
        when (selectedFilter) {
            ShelfLifeFilter.ALL -> activeBatches.sortedBy { it.remainingHours }
            ShelfLifeFilter.CRITICAL -> criticalBatches.sortedBy { it.remainingHours }
            ShelfLifeFilter.MODERATE -> moderateBatches.sortedBy { it.remainingHours }
            ShelfLifeFilter.FRESH -> freshBatches.sortedBy { it.remainingHours }
        }
    }

    val totalActiveKg = remember(activeBatches) { activeBatches.sumOf { it.availableQuantityKg } }
    val totalActiveValue = remember(activeBatches) { activeBatches.sumOf { it.availableQuantityKg * it.farmerAskingPricePerKg } }
    val criticalKg = remember(criticalBatches) { criticalBatches.sumOf { it.availableQuantityKg } }
    val criticalValueLoss = remember(criticalBatches) { criticalBatches.sumOf { it.availableQuantityKg * it.farmerAskingPricePerKg } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("harvest_inventory_shelf_life_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // =========================================================================
        // 0. REAL-TIME MARKET PRICE SPIKE NOTIFICATIONS (SALES TIMING OPTIMIZATION)
        // =========================================================================
        if (uiState.priceSpikeAlerts.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().testTag("price_spike_notifications_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Price Spike Alerts (${uiState.priceSpikeAlerts.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GoldPrimary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldLight
                    ) {
                        Text(
                            text = "Peak Window",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                uiState.priceSpikeAlerts.forEach { alert ->
                    CropPriceSpikeNotificationCard(
                        alert = alert,
                        onExecuteSale = { viewModel.executeSpikeSaleOptimization(it) },
                        onDismiss = { viewModel.dismissPriceSpikeAlert(it) }
                    )
                }
            }
        }

        // Live Rate Spike Simulation Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live APMC Mandi Rate Tracker",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable {
                        viewModel.triggerMarketPriceSpikeSimulation("Tomato", 38.0)
                    }
                    .testTag("simulate_price_spike_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Simulate Rate Spike (+38%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 10.sp)
                    )
                }
            }
        }

        // =========================================================================
        // HISTORICAL HARVEST LOGS & FUTURE CYCLE ESTIMATOR BANNER
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenHarvestHistoryLogs() }
                .testTag("open_harvest_history_btn")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(shape = CircleShape, color = GreenLight, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Historical Harvest & Sale Logs (${uiState.harvestLogs.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "View past yields & estimate future production cycles",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
            }
        }

        // =========================================================================
        // 1. KPI SUMMARY TILES (TOTAL STOCK, VALUE, CRITICAL AT-RISK)
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Active Stock Tile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        Text("Active Harvest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", totalActiveKg / 1000.0)} MT",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                    Text(
                        text = "₹${totalActiveValue.toInt()} • ${activeBatches.size} Batches",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Critical Spoilage Risk Tile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (criticalBatches.isNotEmpty()) SpoilageLight else MaterialTheme.colorScheme.surfaceVariant,
                border = if (criticalBatches.isNotEmpty()) androidx.compose.foundation.BorderStroke(1.dp, SpoilageRed.copy(alpha = 0.5f)) else null,
                modifier = Modifier.weight(1.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (criticalBatches.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (criticalBatches.isNotEmpty()) SpoilageRed else GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (criticalBatches.isNotEmpty()) "Spoilage Alert" else "Zero Waste Status",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (criticalBatches.isNotEmpty()) SpoilageRed else GreenPrimary
                        )
                    }
                    Text(
                        text = if (criticalBatches.isNotEmpty()) "${criticalBatches.size} Batches (${criticalKg.toInt()} kg)" else "0 Spoilage Risk",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (criticalBatches.isNotEmpty()) SpoilageRed else GreenPrimary
                        )
                    )
                    Text(
                        text = if (criticalBatches.isNotEmpty()) "At risk: ₹${criticalValueLoss.toInt()}" else "All produce stable",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (criticalBatches.isNotEmpty()) SpoilageRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // =========================================================================
        // 2. PROACTIVE SURPLUS REDISTRIBUTION ALERT BANNER
        // =========================================================================
        if (criticalBatches.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SpoilageRed),
                modifier = Modifier.fillMaxWidth().testTag("surplus_spoilage_alert_banner")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Surplus Spoilage Warning (${criticalBatches.size} Batches < 36h)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                            Text(
                                text = "फसल सड़न चेतावनी: शेल्फ-लाइफ खत्म होने से पहले तुरंत री-डिस्ट्रीब्यूट करें।",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.9f))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                // Trigger flash redistribution on the most urgent batch
                                batchForRedistribution = criticalBatches.firstOrNull()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SpoilageRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("trigger_urgent_redistribution_btn")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Redistribute Urgent Batch", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToSurplusScreen,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                            modifier = Modifier.testTag("view_all_surplus_btn")
                        ) {
                            Text("Zero-Waste View", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. SHELF-LIFE FILTER TABS
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShelfLifeFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                val count = when (filter) {
                    ShelfLifeFilter.ALL -> activeBatches.size
                    ShelfLifeFilter.CRITICAL -> criticalBatches.size
                    ShelfLifeFilter.MODERATE -> moderateBatches.size
                    ShelfLifeFilter.FRESH -> freshBatches.size
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text("${filter.label} ($count)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filter == ShelfLifeFilter.CRITICAL) SpoilageRed else GreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_chip_${filter.name}")
                )
            }
        }

        // =========================================================================
        // 4. BATCH INVENTORY LIST WITH SHELF-LIFE COUNTDOWNS
        // =========================================================================
        if (filteredBatches.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(28.dp))
                    Text(
                        text = "No produce in this shelf-life bracket.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "All active inventory is safe and fresh.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredBatches.forEach { batch ->
                    val matchingSpike = uiState.priceSpikeAlerts.firstOrNull {
                        it.matchedBatchCode == batch.batchCode || it.cropName.equals(batch.cropName, ignoreCase = true)
                    }
                    HarvestBatchShelfLifeCard(
                        batch = batch,
                        spikeAlert = matchingSpike,
                        onExecuteSpikeSale = { alert -> viewModel.executeSpikeSaleOptimization(alert) },
                        onTriggerRedistribute = { batchForRedistribution = batch },
                        onMoveToColdStorage = {
                            viewModel.applySurplusDiscount(batch.id, 0) // Keep sync
                        }
                    )
                }
            }
        }
    }

    // =========================================================================
    // 5. REDISTRIBUTION MODAL DIALOG
    // =========================================================================
    batchForRedistribution?.let { batch ->
        SurplusRedistributionDialog(
            batch = batch,
            onDismiss = { batchForRedistribution = null },
            onConfirmRedistribution = { channel, discountPct ->
                viewModel.applySurplusDiscount(batch.id, discountPct)
                batchForRedistribution = null
            }
        )
    }
}

/**
 * Individual Batch Card showing Freshness Decay Progress, Remaining Hours,
 * Storage Condition, and Quick Surplus Redistribution Action.
 */
@Composable
fun HarvestBatchShelfLifeCard(
    batch: ProduceBatchEntity,
    spikeAlert: CropPriceSpikeAlert? = null,
    onExecuteSpikeSale: ((CropPriceSpikeAlert) -> Unit)? = null,
    onTriggerRedistribute: () -> Unit,
    onMoveToColdStorage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = batch.remainingHours in 1..36 || batch.freshnessPercentage <= 35
    val isModerate = batch.remainingHours in 37..72 && !isCritical

    val statusColor = when {
        isCritical -> SpoilageRed
        isModerate -> WarningOrange
        else -> GreenPrimary
    }

    val statusBg = when {
        isCritical -> SpoilageLight
        isModerate -> WarningLight
        else -> GreenLight
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_shelf_card_${batch.batchCode}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Header (Crop, Variety, Batch Code & Shelf Life Tag)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${batch.cropName} (${batch.variety})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = batch.qualityGrade.displayName.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = "Batch ${batch.batchCode} • ${batch.farmLocation} (${batch.distanceKm} km)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (batch.remainingHours > 0) "${batch.remainingHours}h left" else "Expired",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusColor),
                            maxLines = 1
                        )
                    }
                }
            }

            // Price Spike Banner on Batch Card
            if (spikeAlert != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("batch_spike_badge_${batch.batchCode}")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Column {
                                Text(
                                    text = "Market Price Spike: ₹${spikeAlert.currentSpikePricePerKg.toInt()}/kg (+${spikeAlert.spikePercentage.toInt()}%)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldPrimary)
                                )
                                Text(
                                    text = "${spikeAlert.mandiName} • +₹${spikeAlert.potentialExtraEarningsRupees.toInt()} extra profit",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                        if (onExecuteSpikeSale != null) {
                            Button(
                                onClick = { onExecuteSpikeSale(spikeAlert) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("batch_sell_spike_btn_${batch.batchCode}")
                            ) {
                                Text("Sell at Peak", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Row 2: Shelf-Life Decay Gauge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shelf-Life Freshness Decay:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${batch.freshnessPercentage}% Farm Fresh",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusColor)
                    )
                }

                LinearProgressIndicator(
                    progress = { (batch.freshnessPercentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // Row 3: Inventory Stock & Economic Exposure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stock in Storage", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = "${batch.availableQuantityKg.toInt()} Kg / ${batch.totalQuantityKg.toInt()} Kg",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = batch.storageCondition.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = GreenPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Current Price", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.1f", batch.effectivePricePerKg)} / kg",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (batch.discountPercent > 0) {
                        Surface(shape = RoundedCornerShape(4.dp), color = SpoilageLight) {
                            Text(
                                text = "-${batch.discountPercent}% Flash Deal",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = SpoilageRed, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                maxLines = 1
                            )
                        }
                    } else if (isCritical) {
                        Text(
                            text = "Loss risk: ₹${(batch.availableQuantityKg * batch.farmerAskingPricePerKg).toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = SpoilageRed, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Row 4: Action Buttons (Redistribute or Cold Storage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCritical || batch.isSurplusUrgent) {
                    Button(
                        onClick = onTriggerRedistribute,
                        colors = ButtonDefaults.buttonColors(containerColor = SpoilageRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("redistribute_btn_${batch.batchCode}")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("⚡ Trigger Redistribution", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onTriggerRedistribute,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("surplus_offer_btn_${batch.batchCode}")
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Broadcast Surplus Offer", fontSize = 11.sp, color = GreenPrimary)
                    }
                }
            }
        }
    }
}

/**
 * Dialog to trigger instant surplus redistribution with recommended discount and channel.
 */
@Composable
fun SurplusRedistributionDialog(
    batch: ProduceBatchEntity,
    onDismiss: () -> Unit,
    onConfirmRedistribution: (channel: RedistributionChannel, discountPercent: Int) -> Unit
) {
    var selectedChannel by remember { mutableStateOf(REDISTRIBUTION_CHANNELS[0]) }
    var discountPercent by remember { mutableIntStateOf(selectedChannel.recommendedDiscount) }

    val discountedPricePerKg = batch.farmerAskingPricePerKg * (1.0 - (discountPercent / 100.0))
    val totalSalvagedRupees = batch.availableQuantityKg * discountedPricePerKg

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("surplus_redistribution_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Surplus Redistribution Action",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "फसल को सड़न से बचाने हेतु त्वरित बिक्री",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Batch Overview Strip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${batch.cropName} (${batch.variety})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${batch.availableQuantityKg.toInt()} Kg Available • ${batch.remainingHours}h shelf-life",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpoilageRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Base Price: ₹${batch.farmerAskingPricePerKg.toInt()}/kg", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "New: ₹${String.format(Locale.getDefault(), "%.1f", discountedPricePerKg)}/kg",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                        }
                    }
                }

                // Channel Selection
                Text(
                    text = "Select Redistribution Channel (बिक्री माध्यम):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    REDISTRIBUTION_CHANNELS.forEach { channel ->
                        val isSelected = selectedChannel == channel
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GreenLight else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedChannel = channel
                                    discountPercent = channel.recommendedDiscount
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GreenPrimary else Color.LightGray)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${channel.name} (-${channel.recommendedDiscount}%)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = channel.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Discount Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Flash Discount:", style = MaterialTheme.typography.labelSmall)
                        Text("$discountPercent% OFF", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SpoilageRed))
                    }
                    Slider(
                        value = discountPercent.toFloat(),
                        onValueChange = { discountPercent = it.toInt() },
                        valueRange = 10f..70f,
                        steps = 11,
                        colors = SliderDefaults.colors(thumbColor = SpoilageRed, activeTrackColor = SpoilageRed)
                    )
                }

                // Financial Salvage Matrix
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Salvaged Payout", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF166534))
                            Text(
                                text = "₹${totalSalvagedRupees.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                        }
                        Text(
                            text = "Guaranteed Zero-Waste",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirmRedistribution(selectedChannel, discountPercent) },
                        colors = ButtonDefaults.buttonColors(containerColor = SpoilageRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("confirm_broadcast_redistribution_btn")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Broadcast Offer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
