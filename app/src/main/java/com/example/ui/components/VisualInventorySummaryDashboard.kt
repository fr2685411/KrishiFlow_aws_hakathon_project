package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatchStatus
import com.example.data.model.CropCategory
import com.example.data.model.ProduceBatchEntity
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.theme.WarningOrange
import java.text.DecimalFormat

data class CropTypeStockBreakdown(
    val category: CropCategory,
    val cropCount: Int,
    val totalStockKg: Double,
    val percentageOfTotal: Float,
    val color: Color
)

data class UrgentExpiringBatchInfo(
    val batch: ProduceBatchEntity,
    val remainingHours: Long,
    val urgencyLevel: UrgencyLevel,
    val progressRatio: Float // 1.0 (Fresh) -> 0.0 (Expired/Spoiled)
)

enum class UrgencyLevel(val label: String, val color: Color, val containerColor: Color) {
    CRITICAL("< 24h Left", SpoilageRed, Color(0xFFFEE2E2)),
    HIGH("1 - 2 Days", WarningOrange, Color(0xFFFEF3C7)),
    MODERATE("3 - 4 Days", GoldPrimary, Color(0xFFFEF9C3)),
    STABLE("> 4 Days", GreenPrimary, Color(0xFFDCFCE7))
}

/**
 * Visual Inventory Summary Dashboard.
 * Displays:
 * 1. Total Current Stock with active lots counter & estimated market valuation.
 * 2. Visual distribution breakdown by crop type/category (proportional progress bars & chips).
 * 3. Urgent expiry monitor with real-time progress bars for items nearing suggested sell-by dates.
 */
@Composable
fun VisualInventorySummaryDashboard(
    batches: List<ProduceBatchEntity>,
    onNavigateToSurplusScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isBreakdownExpanded by remember { mutableStateOf(false) }
    var isUrgentListExpanded by remember { mutableStateOf(true) }

    // Active inventory calculation (lots that have available stock and are not completed or spoiled)
    val activeBatches = remember(batches) {
        batches.filter {
            it.availableQuantityKg > 0 &&
                    it.status != BatchStatus.COMPLETED &&
                    it.status != BatchStatus.SPOILED
        }
    }

    val totalCurrentStockKg = remember(activeBatches) {
        activeBatches.sumOf { it.availableQuantityKg }
    }

    val totalValuationRupees = remember(activeBatches) {
        activeBatches.sumOf { it.availableQuantityKg * it.effectivePricePerKg }
    }

    // Category breakdown
    val categoryBreakdowns = remember(activeBatches, totalCurrentStockKg) {
        if (totalCurrentStockKg <= 0.0) {
            emptyList()
        } else {
            val colorPalette = listOf(
                GreenPrimary,
                GoldPrimary,
                EcoTeal,
                WarningOrange,
                Color(0xFF8B5CF6)
            )
            CropCategory.values().mapIndexedNotNull { index, category ->
                val categoryBatches = activeBatches.filter { it.category == category }
                val catStockKg = categoryBatches.sumOf { it.availableQuantityKg }
                if (catStockKg > 0) {
                    CropTypeStockBreakdown(
                        category = category,
                        cropCount = categoryBatches.map { it.cropName }.distinct().size,
                        totalStockKg = catStockKg,
                        percentageOfTotal = (catStockKg / totalCurrentStockKg).toFloat(),
                        color = colorPalette[index % colorPalette.size]
                    )
                } else null
            }.sortedByDescending { it.totalStockKg }
        }
    }

    // Batches nearing their sell-by / expiry date
    val nearingExpiryBatches = remember(activeBatches) {
        activeBatches.map { batch ->
            val hoursLeft = batch.remainingHours
            val urgency = when {
                hoursLeft <= 24 -> UrgencyLevel.CRITICAL
                hoursLeft <= 48 -> UrgencyLevel.HIGH
                hoursLeft <= 96 -> UrgencyLevel.MODERATE
                else -> UrgencyLevel.STABLE
            }
            // Freshness ratio from 0.0 to 1.0
            val ratio = (batch.freshnessPercentage / 100f).coerceIn(0f, 1f)
            UrgentExpiringBatchInfo(
                batch = batch,
                remainingHours = hoursLeft,
                urgencyLevel = urgency,
                progressRatio = ratio
            )
        }.sortedBy { it.remainingHours }
    }

    val criticalCount = remember(nearingExpiryBatches) {
        nearingExpiryBatches.count { it.urgencyLevel == UrgencyLevel.CRITICAL || it.urgencyLevel == UrgencyLevel.HIGH }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_inventory_summary_dashboard")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. HEADER: Title and Quick Alert Badge
            // =========================================================================
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GreenLight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Inventory & Stock Health",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "कुल स्टॉक, फसल वर्ग व एक्सपायरी ट्रैकर",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (criticalCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SpoilageRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SpoilageRed.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onNavigateToSurplusScreen() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = SpoilageRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$criticalCount Urgent",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpoilageRed
                                )
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 2. TOTAL CURRENT STOCK METRICS STRIP
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Stock
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "CURRENT AVAILABLE STOCK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val df = DecimalFormat("#,##0")
                            Text(
                                text = df.format(totalCurrentStockKg),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "kg (${(totalCurrentStockKg / 1000.0).format(1)} T)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(bottom = 3.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${activeBatches.size} active lots in farmhub",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Total Stock Value
                    Column(
                        modifier = Modifier.weight(0.9f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "EST. REALIZABLE VALUE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "₹${totalValuationRupees.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GreenLight
                        ) {
                            Text(
                                text = "100% Quality Inspected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    color = GreenPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 3. BREAKDOWN BY CROP TYPE / CATEGORY
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBreakdownExpanded = !isBreakdownExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Crop Type Stock Breakdown (फसल अनुसार वितरण)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = if (isBreakdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Proportional Multi-Segment Progress Bar
                if (totalCurrentStockKg > 0 && categoryBreakdowns.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            categoryBreakdowns.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .weight(cat.percentageOfTotal.coerceAtLeast(0.01f))
                                        .height(10.dp)
                                        .background(cat.color)
                                )
                            }
                        }
                    }

                    // Chips overview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryBreakdowns.take(3).forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, cat.color.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Column {
                                        Text(
                                            text = cat.category.displayName.take(10),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${cat.totalStockKg.toInt()} kg (${(cat.percentageOfTotal * 100).toInt()}%)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Detailed breakdown accordion
                    AnimatedVisibility(visible = isBreakdownExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            categoryBreakdowns.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(item.color)
                                            )
                                            Column {
                                                Text(
                                                    text = item.category.displayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "${item.cropCount} varieties registered",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${item.totalStockKg.toInt()} kg",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = item.color
                                                )
                                            )
                                            Text(
                                                text = "${(item.percentageOfTotal * 100).toInt()}% of total",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // =========================================================================
            // 4. PROGRESS BARS FOR ITEMS NEARING SUGGESTED SELL-BY / EXPIRY DATES
            // =========================================================================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isUrgentListExpanded = !isUrgentListExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (criticalCount > 0) SpoilageRed else GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sell-By Date Health Tracker (शीघ्र बिक्री निगरानी)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isUrgentListExpanded) "Hide" else "Show All (${nearingExpiryBatches.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                        Icon(
                            imageVector = if (isUrgentListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (nearingExpiryBatches.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "All lots are fresh! No items currently nearing expiration.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                            )
                        }
                    }
                } else {
                    // Display up to 3 priority items or full list when expanded
                    val itemsToShow = if (isUrgentListExpanded) nearingExpiryBatches.take(5) else nearingExpiryBatches.take(2)

                    itemsToShow.forEach { item ->
                        BatchSellByProgressCard(item = item)
                    }

                    if (nearingExpiryBatches.size > 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSurplusScreen() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All Lots in Priority Surplus Board",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Crop Batch Progress Bar card visualizing remaining shelf life and urgency status.
 */
@Composable
private fun BatchSellByProgressCard(
    item: UrgentExpiringBatchInfo
) {
    val animatedProgress by animateFloatAsState(
        targetValue = item.progressRatio,
        label = "FreshnessProgress"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.urgencyLevel == UrgencyLevel.CRITICAL) SpoilageRed.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Crop Name & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.batch.cropName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• ${item.batch.variety}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = item.urgencyLevel.containerColor
                ) {
                    Text(
                        text = item.urgencyLevel.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = item.urgencyLevel.color,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Row 2: Shelf-Life Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .background(
                            when (item.urgencyLevel) {
                                UrgencyLevel.CRITICAL -> SpoilageRed
                                UrgencyLevel.HIGH -> WarningOrange
                                UrgencyLevel.MODERATE -> GoldPrimary
                                UrgencyLevel.STABLE -> GreenPrimary
                            }
                        )
                )
            }

            // Row 3: Metrics details (Remaining hours, available quantity, storage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.remainingHours}h remaining to sell • Lot: #${item.batch.batchCode}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (item.urgencyLevel == UrgencyLevel.CRITICAL) SpoilageRed else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${item.batch.availableQuantityKg.toInt()} kg in ${item.batch.storageCondition.displayName.take(14)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                )
            }
        }
    }
}

private fun Double.format(digits: Int): String {
    return "%.${digits}f".format(this)
}
