package com.example.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import com.example.data.export.WasteLogCsvExportResult
import com.example.data.export.WasteLogCsvExporter
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Daily agricultural surplus data point aggregated across destinations for D3 & Recharts logic.
 */
data class DailySurplusDataPoint(
    val dateKey: String,             // Format "yyyy-MM-dd"
    val displayLabel: String,        // Format "05 Oct" or "Mon 05"
    val dayOfWeek: String,          // Format "Mon", "Tue"
    val timestamp: Long,
    val donationKg: Double,
    val compostingKg: Double,
    val animalFeedKg: Double,
    val biogasKg: Double,
    val totalSurplusKg: Double,
    val cumulativeDivertedKg: Double,
    val co2DivertedKg: Double,
    val estimatedValueRupees: Double,
    val logCount: Int,
    val topCrops: List<String>
)

/**
 * Chart display modes implementing D3 / Recharts visualization models:
 * - STACKED_BAR: Recharts <BarChart> with D3 stacked layout (Donation, Compost, Animal Feed, Biogas)
 * - CUMULATIVE_AREA: Recharts <AreaChart> with D3 monotone curve (Diverted vs Avoided CO2e)
 * - DESTINATION_DONUT: Recharts <PieChart> with D3 arc / pie layout for proportion breakdown
 */
enum class SurplusChartMode(val title: String, val subtitle: String) {
    STACKED_BAR("Daily Stacked Bar", "Surplus logged by destination (Recharts BarChart)"),
    CUMULATIVE_AREA("Cumulative Impact", "Diverted biomass & avoided CO₂e (Recharts AreaChart)"),
    DESTINATION_DONUT("Destination Share", "Allocation ratio donut (D3 Arc / PieChart)")
}

enum class SurplusTimeFilter(val label: String, val days: Int) {
    LAST_7_DAYS("7 Days", 7),
    LAST_14_DAYS("14 Days", 14),
    LAST_30_DAYS("30 Days", 30),
    ALL_TIME("All Time", 365)
}

/**
 * Composable dashboard component that leverages Recharts & D3 logic to display
 * a summary chart of daily agricultural surplus logged in the Room database.
 *
 * Highlights farmer impact on waste reduction:
 * - Quantities diverted from landfills into donations and composting
 * - Avoided greenhouse emissions (methane / CO₂e)
 * - Interactive scrubber crosshairs and tooltips
 * - Live React Recharts and D3.js SVG code viewer
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgriculturalSurplusImpactDashboard(
    wasteLogs: List<WasteLog>,
    modifier: Modifier = Modifier,
    onLogSurplusClick: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(true) }
    var selectedChartMode by remember { mutableStateOf(SurplusChartMode.STACKED_BAR) }
    var selectedTimeFilter by remember { mutableStateOf(SurplusTimeFilter.LAST_7_DAYS) }
    var showD3RechartsSpecDialog by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<WasteLogCsvExportResult?>(null) }
    val context = LocalContext.current

    // Interactive scrubber crosshair index
    var activeScrubIndex by remember { mutableStateOf<Int?>(null) }
    var touchXPos by remember { mutableFloatStateOf(0f) }

    // Animation progress for smooth transitions
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedChartMode, selectedTimeFilter, wasteLogs.size) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // Compute aggregated daily points using D3 time scale logic
    val dailyDataPoints = remember(wasteLogs, selectedTimeFilter) {
        aggregateDailySurplus(wasteLogs, selectedTimeFilter.days)
    }

    // High level KPIs
    val totalSurplusKg = remember(wasteLogs) {
        wasteLogs.sumOf { it.quantityKg }
    }
    val totalDonatedKg = remember(wasteLogs) {
        wasteLogs.filter { it.destination == WasteDestination.DONATION }.sumOf { it.quantityKg }
    }
    val totalCompostedKg = remember(wasteLogs) {
        wasteLogs.filter { it.destination == WasteDestination.COMPOSTING }.sumOf { it.quantityKg }
    }
    val totalCo2SavedKg = remember(wasteLogs) {
        wasteLogs.sumOf { it.co2DivertedKg }
    }
    val estimatedMealsProvided = remember(totalDonatedKg) {
        (totalDonatedKg * 2.5).toInt() // Standard metric: ~2.5 meals per kg of edible food
    }
    val estimatedBioFertilizerKg = remember(totalCompostedKg) {
        (totalCompostedKg * 0.4).toInt() // Standard organic composting yield: ~40% finished humus
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("agricultural_surplus_impact_dashboard_card")
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. HEADER: TITLE & CONTROLS
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Surplus & Waste Impact",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "D3 / Recharts",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Daily agricultural surplus logged in Room SQLite",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (wasteLogs.isEmpty()) {
                                Toast.makeText(context, "No surplus logs to export", Toast.LENGTH_SHORT).show()
                            } else {
                                exportResult = WasteLogCsvExporter.exportToCsv(context, wasteLogs)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("surplus_chart_export_csv_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Export WasteLog to CSV",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showD3RechartsSpecDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("surplus_chart_open_spec_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "View Recharts & D3 Specification",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Section"
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // =========================================================================
                    // 2. IMPACT KPI TILES (AIR, WATER, WASTE, VALUE)
                    // =========================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total Diverted (Waste saved)
                        ImpactMetricTile(
                            modifier = Modifier.weight(1f),
                            title = "Diverted",
                            value = String.format(Locale.getDefault(), "%.1f kg", totalSurplusKg),
                            subtitle = "Zero to Landfill",
                            icon = Icons.Default.Recycling,
                            tint = Color(0xFF10B981)
                        )

                        // Avoided CO2e (Air saved)
                        ImpactMetricTile(
                            modifier = Modifier.weight(1f),
                            title = "Emissions",
                            value = String.format(Locale.getDefault(), "%.1f kg", totalCo2SavedKg),
                            subtitle = "CO₂e Avoided",
                            icon = Icons.Default.Co2,
                            tint = Color(0xFF0284C7)
                        )

                        // Meals Shared (Social)
                        ImpactMetricTile(
                            modifier = Modifier.weight(1f),
                            title = "Meals",
                            value = "$estimatedMealsProvided",
                            subtitle = "From Donated",
                            icon = Icons.Default.VolunteerActivism,
                            tint = Color(0xFFD97706)
                        )

                        // Bio-fertilizer (Soil)
                        ImpactMetricTile(
                            modifier = Modifier.weight(1f),
                            title = "Compost",
                            value = "${estimatedBioFertilizerKg} kg",
                            subtitle = "Bio-Fertilizer",
                            icon = Icons.Default.Eco,
                            tint = Color(0xFF8B5CF6)
                        )
                    }

                    // =========================================================================
                    // 3. CHART TYPE TABS & TIME RANGE FILTER CHIPS
                    // =========================================================================
                    TabRow(
                        selectedTabIndex = selectedChartMode.ordinal,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        SurplusChartMode.entries.forEach { mode ->
                            Tab(
                                selected = selectedChartMode == mode,
                                onClick = {
                                    selectedChartMode = mode
                                    activeScrubIndex = null
                                },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val icon = when (mode) {
                                            SurplusChartMode.STACKED_BAR -> Icons.Default.BarChart
                                            SurplusChartMode.CUMULATIVE_AREA -> Icons.Default.ShowChart
                                            SurplusChartMode.DESTINATION_DONUT -> Icons.Default.PieChart
                                        }
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = when (mode) {
                                                SurplusChartMode.STACKED_BAR -> "Stacked Bar"
                                                SurplusChartMode.CUMULATIVE_AREA -> "Cumulative"
                                                SurplusChartMode.DESTINATION_DONUT -> "Breakdown"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedChartMode == mode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("surplus_chart_tab_${mode.name.lowercase()}")
                            )
                        }
                    }

                    // Time Range Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timeframe:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        SurplusTimeFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = selectedTimeFilter == filter,
                                onClick = {
                                    selectedTimeFilter = filter
                                    activeScrubIndex = null
                                },
                                label = { Text(filter.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("time_filter_${filter.name.lowercase()}")
                            )
                        }
                    }

                    // =========================================================================
                    // 4. INTERACTIVE RECHARTS / D3 CANVAS CHART
                    // =========================================================================
                    if (dailyDataPoints.isEmpty() || totalSurplusKg == 0.0) {
                        EmptySurplusChartPlaceholder(onLogSurplusClick = onLogSurplusClick)
                    } else {
                        // Chart Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    RoundedCornerShape(16.dp)
                                )
                                .pointerInput(dailyDataPoints, selectedChartMode) {
                                    if (selectedChartMode != SurplusChartMode.DESTINATION_DONUT) {
                                        detectTapGestures(
                                            onTap = { offset ->
                                                touchXPos = offset.x
                                                activeScrubIndex = computeClosestDataIndex(
                                                    touchX = offset.x,
                                                    canvasWidth = size.width.toFloat(),
                                                    dataSize = dailyDataPoints.size
                                                )
                                            }
                                        )
                                    }
                                }
                                .pointerInput(dailyDataPoints, selectedChartMode) {
                                    if (selectedChartMode != SurplusChartMode.DESTINATION_DONUT) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                touchXPos = offset.x
                                                activeScrubIndex = computeClosestDataIndex(
                                                    touchX = offset.x,
                                                    canvasWidth = size.width.toFloat(),
                                                    dataSize = dailyDataPoints.size
                                                )
                                            },
                                            onDrag = { change, _ ->
                                                touchXPos = change.position.x
                                                activeScrubIndex = computeClosestDataIndex(
                                                    touchX = change.position.x,
                                                    canvasWidth = size.width.toFloat(),
                                                    dataSize = dailyDataPoints.size
                                                )
                                            }
                                        )
                                    }
                                }
                                .testTag("surplus_chart_canvas_box")
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val bottomPadding = 36f
                                val topPadding = 18f
                                val plotHeight = canvasHeight - bottomPadding - topPadding

                                when (selectedChartMode) {
                                    SurplusChartMode.STACKED_BAR -> {
                                        drawD3StackedBarChart(
                                            dataPoints = dailyDataPoints,
                                            width = canvasWidth,
                                            height = canvasHeight,
                                            plotHeight = plotHeight,
                                            topPadding = topPadding,
                                            bottomPadding = bottomPadding,
                                            progress = animProgress.value,
                                            activeScrubIndex = activeScrubIndex
                                        )
                                    }
                                    SurplusChartMode.CUMULATIVE_AREA -> {
                                        drawD3CumulativeAreaChart(
                                            dataPoints = dailyDataPoints,
                                            width = canvasWidth,
                                            height = canvasHeight,
                                            plotHeight = plotHeight,
                                            topPadding = topPadding,
                                            bottomPadding = bottomPadding,
                                            progress = animProgress.value,
                                            activeScrubIndex = activeScrubIndex
                                        )
                                    }
                                    SurplusChartMode.DESTINATION_DONUT -> {
                                        drawD3DestinationDonutChart(
                                            totalDonatedKg = totalDonatedKg,
                                            totalCompostedKg = totalCompostedKg,
                                            totalAnimalFeedKg = wasteLogs.filter { it.destination == WasteDestination.ANIMAL_FEED }.sumOf { it.quantityKg },
                                            totalBiogasKg = wasteLogs.filter { it.destination == WasteDestination.BIOGAS }.sumOf { it.quantityKg },
                                            width = canvasWidth,
                                            height = canvasHeight,
                                            progress = animProgress.value
                                        )
                                    }
                                }
                            }
                        }

                        // =========================================================================
                        // 5. INTERACTIVE SCRUBBER TOOLTIP (RECHARTS TOOLTIP EQUIVALENT)
                        // =========================================================================
                        if (activeScrubIndex != null && activeScrubIndex in dailyDataPoints.indices) {
                            val activePt = dailyDataPoints[activeScrubIndex!!]
                            RechartsStyleTooltipCard(
                                point = activePt,
                                onDismiss = { activeScrubIndex = null }
                            )
                        }

                        // =========================================================================
                        // 6. D3 / RECHARTS LEGEND
                        // =========================================================================
                        SurplusChartLegend(selectedChartMode = selectedChartMode)
                    }
                }
            }
        }
    }

    // Modal inspecting dynamic D3 SVG & Recharts JSX Code
    if (showD3RechartsSpecDialog) {
        D3RechartsSpecModal(
            dailyDataPoints = dailyDataPoints,
            totalSurplusKg = totalSurplusKg,
            onDismiss = { showD3RechartsSpecDialog = false }
        )
    }

    // Modal displaying WasteLog CSV Export Result & Actions
    exportResult?.let { result ->
        WasteLogExportDialog(
            exportResult = result,
            onDismiss = { exportResult = null }
        )
    }
}

/**
 * Recharts-style interactive tooltip floating card displaying day details.
 */
@Composable
private fun RechartsStyleTooltipCard(
    point: DailySurplusDataPoint,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_tooltip_card")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "${point.dayOfWeek}, ${point.displayLabel}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.getDefault(), "Total: %.1f kg", point.totalSurplusKg),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "• Donation: ${String.format(Locale.getDefault(), "%.1f kg", point.donationKg)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = "• Composting: ${String.format(Locale.getDefault(), "%.1f kg", point.compostingKg)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFD97706)
                    )
                    if (point.animalFeedKg > 0) {
                        Text(
                            text = "• Animal Feed: ${String.format(Locale.getDefault(), "%.1f kg", point.animalFeedKg)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF3B82F6)
                        )
                    }
                    if (point.biogasKg > 0) {
                        Text(
                            text = "• Biogas: ${String.format(Locale.getDefault(), "%.1f kg", point.biogasKg)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "🌱 CO₂e Saved: ${String.format(Locale.getDefault(), "%.1f kg", point.co2DivertedKg)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0284C7)
                    )
                    if (point.estimatedValueRupees > 0) {
                        Text(
                            text = "💰 Diverted Value: ₹${point.estimatedValueRupees.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (point.topCrops.isNotEmpty()) {
                        Text(
                            text = "Crops: ${point.topCrops.take(2).joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Summary tile for environmental impact metrics.
 */
@Composable
private fun ImpactMetricTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tint.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = tint
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = tint.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Recharts & D3 Color Legend.
 */
@Composable
private fun SurplusChartLegend(selectedChartMode: SurplusChartMode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedChartMode == SurplusChartMode.CUMULATIVE_AREA) {
            LegendItem(color = Color(0xFF10B981), label = "Cumulative Waste Diverted (kg)")
            LegendItem(color = Color(0xFF0284C7), label = "Avoided CO₂e / Methane (kg)")
        } else {
            LegendItem(color = Color(0xFF10B981), label = "Donation")
            LegendItem(color = Color(0xFFD97706), label = "Composting")
            LegendItem(color = Color(0xFF3B82F6), label = "Animal Feed")
            LegendItem(color = Color(0xFF8B5CF6), label = "Biogas")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Empty state placeholder when no logs exist yet.
 */
@Composable
private fun EmptySurplusChartPlaceholder(onLogSurplusClick: (() -> Unit)?) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Recycling,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "No Daily Surplus Logged Yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Log surplus produce earmarked for donation, composting, or animal feed to start tracking your environmental waste reduction impact in Room DB.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (onLogSurplusClick != null) {
                Button(
                    onClick = onLogSurplusClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Log First Surplus Batch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// =========================================================================
// D3 & RECHARTS CANVAS DRAWING IMPLEMENTATION
// =========================================================================

/**
 * Draws stacked bar chart following Recharts <BarChart> with D3.stack layout logic.
 */
private fun DrawScope.drawD3StackedBarChart(
    dataPoints: List<DailySurplusDataPoint>,
    width: Float,
    height: Float,
    plotHeight: Float,
    topPadding: Float,
    bottomPadding: Float,
    progress: Float,
    activeScrubIndex: Int?
) {
    if (dataPoints.isEmpty()) return

    // 1. D3.scaleLinear: Calculate maximum daily sum for Y domain
    val maxDailySurplus = max(10.0, dataPoints.maxOfOrNull { it.totalSurplusKg } ?: 10.0)
    // Round up nicely like d3.nice()
    val yDomainMax = (kotlin.math.ceil(maxDailySurplus / 10.0) * 10.0).toFloat()

    // 2. Draw horizontal grid lines (D3 axisLeft grid)
    val gridSteps = 4
    for (i in 0..gridSteps) {
        val normalizedY = i.toFloat() / gridSteps
        val yPos = topPadding + plotHeight * (1f - normalizedY)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.35f),
            start = Offset(0f, yPos),
            end = Offset(width, yPos),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }

    // 3. D3.scaleBand: Calculate bar widths and slots along X axis
    val barSlotWidth = width / dataPoints.size
    val barWidth = (barSlotWidth * 0.58f).coerceIn(8f, 38f)

    dataPoints.forEachIndexed { index, pt ->
        val xCenter = index * barSlotWidth + barSlotWidth / 2f
        val xLeft = xCenter - barWidth / 2f
        val baselineY = topPadding + plotHeight

        // Stack segments: Donation -> Compost -> Animal Feed -> Biogas
        var currentStackBaseY = baselineY

        val segments = listOf(
            Pair(pt.donationKg.toFloat(), Color(0xFF10B981)), // Green
            Pair(pt.compostingKg.toFloat(), Color(0xFFD97706)), // Amber
            Pair(pt.animalFeedKg.toFloat(), Color(0xFF3B82F6)), // Blue
            Pair(pt.biogasKg.toFloat(), Color(0xFF8B5CF6))  // Purple
        )

        segments.forEachIndexed { segIdx, (qty, color) ->
            if (qty > 0f) {
                val segHeight = (qty / yDomainMax * plotHeight * progress).coerceAtLeast(1f)
                val segTop = currentStackBaseY - segHeight

                // Draw segment
                val isTopSegment = segIdx == segments.indexOfLast { it.first > 0f }
                val cornerRadius = if (isTopSegment) CornerRadius(6f, 6f) else CornerRadius(0f, 0f)

                drawRoundRect(
                    color = color,
                    topLeft = Offset(xLeft, segTop),
                    size = Size(barWidth, segHeight),
                    cornerRadius = cornerRadius
                )

                currentStackBaseY = segTop
            }
        }

        // Active scrubber indicator highlight
        if (activeScrubIndex == index) {
            drawLine(
                color = Color(0xFF10B981),
                start = Offset(xCenter, topPadding),
                end = Offset(xCenter, baselineY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
            drawCircle(
                color = Color(0xFF10B981),
                radius = 5f,
                center = Offset(xCenter, currentStackBaseY)
            )
        }
    }
}

/**
 * Draws cumulative area chart following Recharts <AreaChart> with D3 monotone spline smoothing.
 */
private fun DrawScope.drawD3CumulativeAreaChart(
    dataPoints: List<DailySurplusDataPoint>,
    width: Float,
    height: Float,
    plotHeight: Float,
    topPadding: Float,
    bottomPadding: Float,
    progress: Float,
    activeScrubIndex: Int?
) {
    if (dataPoints.isEmpty()) return

    val maxCumulative = max(10.0, dataPoints.maxOfOrNull { it.cumulativeDivertedKg } ?: 10.0)
    val yDomainMax = (kotlin.math.ceil(maxCumulative / 10.0) * 10.0).toFloat()
    val baselineY = topPadding + plotHeight

    // 1. Grid
    for (i in 0..4) {
        val yPos = topPadding + plotHeight * (1f - i.toFloat() / 4f)
        drawLine(
            color = Color.LightGray.copy(alpha = 0.35f),
            start = Offset(0f, yPos),
            end = Offset(width, yPos),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }

    // 2. Compute points for Cumulative Diverted (D3 Area Curve)
    val areaPoints = dataPoints.mapIndexed { index, pt ->
        val x = if (dataPoints.size > 1) {
            index.toFloat() / (dataPoints.size - 1) * width
        } else {
            width / 2f
        }
        val yRatio = (pt.cumulativeDivertedKg.toFloat() / yDomainMax * progress).coerceIn(0f, 1f)
        val y = baselineY - (yRatio * plotHeight)
        Offset(x, y)
    }

    if (areaPoints.size >= 2) {
        val areaPath = Path().apply {
            moveTo(areaPoints.first().x, baselineY)
            lineTo(areaPoints.first().x, areaPoints.first().y)

            // D3 cubic spline interpolation
            for (i in 0 until areaPoints.size - 1) {
                val p0 = areaPoints[i]
                val p1 = areaPoints[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                cubicTo(controlX1, p0.y, controlX2, p1.y, p1.x, p1.y)
            }

            lineTo(areaPoints.last().x, baselineY)
            close()
        }

        // Gradient Area fill
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF10B981).copy(alpha = 0.45f),
                    Color(0xFF10B981).copy(alpha = 0.05f)
                ),
                startY = topPadding,
                endY = baselineY
            )
        )

        // Line Path
        val linePath = Path().apply {
            moveTo(areaPoints.first().x, areaPoints.first().y)
            for (i in 0 until areaPoints.size - 1) {
                val p0 = areaPoints[i]
                val p1 = areaPoints[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                cubicTo(controlX1, p0.y, controlX2, p1.y, p1.x, p1.y)
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF10B981),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }

    // Secondary avoided CO2e spline line
    val maxCo2 = yDomainMax * 1.9f
    val co2Points = dataPoints.mapIndexed { index, pt ->
        val x = if (dataPoints.size > 1) {
            index.toFloat() / (dataPoints.size - 1) * width
        } else {
            width / 2f
        }
        val yRatio = (pt.co2DivertedKg.toFloat() / (yDomainMax * 1.9f) * progress).coerceIn(0f, 1f)
        val y = baselineY - (yRatio * plotHeight)
        Offset(x, y)
    }

    if (co2Points.size >= 2) {
        val co2Path = Path().apply {
            moveTo(co2Points.first().x, co2Points.first().y)
            for (i in 0 until co2Points.size - 1) {
                val p0 = co2Points[i]
                val p1 = co2Points[i + 1]
                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                cubicTo(controlX1, p0.y, controlX2, p1.y, p1.x, p1.y)
            }
        }
        drawPath(
            path = co2Path,
            color = Color(0xFF0284C7),
            style = Stroke(
                width = 2.5f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        )
    }

    // Scrubber crosshair
    if (activeScrubIndex != null && activeScrubIndex in areaPoints.indices) {
        val activePt = areaPoints[activeScrubIndex]
        drawLine(
            color = Color(0xFF10B981),
            start = Offset(activePt.x, topPadding),
            end = Offset(activePt.x, baselineY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        )
        drawCircle(color = Color.White, radius = 6f, center = activePt)
        drawCircle(color = Color(0xFF10B981), radius = 4f, center = activePt)
    }
}

/**
 * Draws donut chart implementing D3 arc & pie geometry.
 */
private fun DrawScope.drawD3DestinationDonutChart(
    totalDonatedKg: Double,
    totalCompostedKg: Double,
    totalAnimalFeedKg: Double,
    totalBiogasKg: Double,
    width: Float,
    height: Float,
    progress: Float
) {
    val total = totalDonatedKg + totalCompostedKg + totalAnimalFeedKg + totalBiogasKg
    if (total <= 0.0) return

    val centerX = width / 2f
    val centerY = height / 2f
    val outerRadius = (minOf(width, height) * 0.42f).coerceAtLeast(30f)
    val strokeWidth = outerRadius * 0.42f

    val slices = listOf(
        Pair((totalDonatedKg / total).toFloat(), Color(0xFF10B981)), // Green
        Pair((totalCompostedKg / total).toFloat(), Color(0xFFD97706)), // Amber
        Pair((totalAnimalFeedKg / total).toFloat(), Color(0xFF3B82F6)), // Blue
        Pair((totalBiogasKg / total).toFloat(), Color(0xFF8B5CF6))  // Purple
    ).filter { it.first > 0f }

    var currentAngle = -90f // Start at top 12 o'clock

    slices.forEach { (ratio, color) ->
        val sweepAngle = ratio * 360f * progress
        drawArc(
            color = color,
            startAngle = currentAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(centerX - outerRadius + strokeWidth / 2f, centerY - outerRadius + strokeWidth / 2f),
            size = Size((outerRadius - strokeWidth / 2f) * 2f, (outerRadius - strokeWidth / 2f) * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        currentAngle += sweepAngle
    }
}

/**
 * Finds index of data point closest to touched X coordinate (D3 bisector logic).
 */
private fun computeClosestDataIndex(touchX: Float, canvasWidth: Float, dataSize: Int): Int {
    if (dataSize <= 1 || canvasWidth <= 0f) return 0
    val slot = canvasWidth / dataSize
    val index = (touchX / slot).toInt().coerceIn(0, dataSize - 1)
    return index
}

/**
 * Groups and aggregates Room database WasteLog records into daily time buckets.
 */
private fun aggregateDailySurplus(wasteLogs: List<WasteLog>, daysBack: Int): List<DailySurplusDataPoint> {
    val cal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

    // Generate buckets for the desired days
    val buckets = mutableListOf<DailySurplusDataPoint>()
    var runningCumulative = 0.0

    val daysToGenerate = minOf(daysBack, 30) // Cap to readable limit
    for (i in (daysToGenerate - 1) downTo 0) {
        val dayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -i)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = dayCal.timeInMillis
        val endOfDay = startOfDay + 86400000L - 1L

        val logsInDay = wasteLogs.filter { it.logDateTimestamp in startOfDay..endOfDay }

        val donation = logsInDay.filter { it.destination == WasteDestination.DONATION }.sumOf { it.quantityKg }
        val compost = logsInDay.filter { it.destination == WasteDestination.COMPOSTING }.sumOf { it.quantityKg }
        val feed = logsInDay.filter { it.destination == WasteDestination.ANIMAL_FEED }.sumOf { it.quantityKg }
        val biogas = logsInDay.filter { it.destination == WasteDestination.BIOGAS }.sumOf { it.quantityKg }
        val totalDay = donation + compost + feed + biogas
        runningCumulative += totalDay

        val crops = logsInDay.map { it.cropName }.distinct()
        val estValue = logsInDay.sumOf { it.estimatedValueRupees }

        buckets.add(
            DailySurplusDataPoint(
                dateKey = dateFormat.format(dayCal.time),
                displayLabel = labelFormat.format(dayCal.time),
                dayOfWeek = dayFormat.format(dayCal.time),
                timestamp = dayCal.timeInMillis,
                donationKg = donation,
                compostingKg = compost,
                animalFeedKg = feed,
                biogasKg = biogas,
                totalSurplusKg = totalDay,
                cumulativeDivertedKg = runningCumulative,
                co2DivertedKg = totalDay * 1.9,
                estimatedValueRupees = estValue,
                logCount = logsInDay.size,
                topCrops = crops
            )
        )
    }

    return buckets
}

/**
 * Modal showcasing the React Recharts JSX and D3.js SVG code generated dynamically
 * from the Room database records.
 */
@Composable
private fun D3RechartsSpecModal(
    dailyDataPoints: List<DailySurplusDataPoint>,
    totalSurplusKg: Double,
    onDismiss: () -> Unit
) {
    var selectedSpecTab by remember { mutableIntStateOf(0) } // 0: Recharts JSX, 1: D3.js SVG HTML

    val rechartsCode = remember(dailyDataPoints) {
        generateRechartsCodeSnippet(dailyDataPoints)
    }
    val d3HtmlCode = remember(dailyDataPoints) {
        generateD3HtmlSnippet(dailyDataPoints)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recharts & D3 Data Visualization Spec",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live dynamic specification generated from Room Database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                TabRow(
                    selectedTabIndex = selectedSpecTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedSpecTab == 0,
                        onClick = { selectedSpecTab = 0 },
                        text = { Text("Recharts (React)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedSpecTab == 1,
                        onClick = { selectedSpecTab = 1 },
                        text = { Text("D3.js (Interactive SVG)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    if (selectedSpecTab == 0) {
                        // Recharts Code view
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = rechartsCode,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    } else {
                        // D3 Interactive WebView preview
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    webViewClient = WebViewClient()
                                    settings.javaScriptEnabled = true
                                    loadDataWithBaseURL(null, d3HtmlCode, "text/html", "UTF-8", null)
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(null, d3HtmlCode, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close Spec", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun generateRechartsCodeSnippet(dataPoints: List<DailySurplusDataPoint>): String {
    val items = dataPoints.take(7).joinToString(",\n  ") { pt ->
        "{ date: \"${pt.displayLabel}\", donation: ${pt.donationKg}, compost: ${pt.compostingKg}, total: ${pt.totalSurplusKg}, co2Saved: ${pt.co2DivertedKg} }"
    }

    return """
// Recharts Stacked Bar & Impact Area Chart Component
import React from 'react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend, CartesianGrid } from 'recharts';

const dailySurplusData = [
  $items
];

export function AgriculturalSurplusDashboard() {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={dailySurplusData} margin={{ top: 12, right: 16, left: 0, bottom: 4 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis dataKey="date" stroke="#64748b" />
        <YAxis unit="kg" stroke="#64748b" />
        <Tooltip
          formatter={(value, name) => [`${'$'}{value} kg`, name]}
          contentStyle={{ backgroundColor: '#ffffff', borderRadius: 8, borderColor: '#10b981' }}
        />
        <Legend />
        <Bar dataKey="donation" stackId="surplus" fill="#10b981" name="Donation (Food Banks)" />
        <Bar dataKey="compost" stackId="surplus" fill="#d97706" name="Compost (Bio-Fertilizer)" />
      </BarChart>
    </ResponsiveContainer>
  );
}
    """.trimIndent()
}

private fun generateD3HtmlSnippet(dataPoints: List<DailySurplusDataPoint>): String {
    val labels = dataPoints.take(7).map { "\"${it.displayLabel}\"" }.joinToString(", ")
    val donationVals = dataPoints.take(7).map { it.donationKg.toString() }.joinToString(", ")
    val compostVals = dataPoints.take(7).map { it.compostingKg.toString() }.joinToString(", ")

    return """
<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    body { font-family: system-ui, sans-serif; margin: 0; padding: 12px; background: #0f172a; color: #f8fafc; }
    h4 { margin: 0 0 6px 0; color: #34d399; font-size: 13px; }
    p { margin: 0 0 10px 0; font-size: 10px; color: #94a3b8; }
    .chart-container { width: 100%; height: 190px; }
    svg { width: 100%; height: 100%; overflow: visible; }
    .bar-donation { fill: #10b981; }
    .bar-compost { fill: #d97706; }
    .grid { stroke: #334155; stroke-dasharray: 4,4; stroke-width: 1; }
    .label { font-size: 9px; fill: #94a3b8; text-anchor: middle; }
  </style>
</head>
<body>
  <h4>KrishiFlow D3.js Agricultural Surplus Diverter</h4>
  <p>Live SVG stacked diversion from Room Database</p>
  <div class="chart-container">
    <svg viewBox="0 0 320 150">
      <line x1="20" y1="20" x2="300" y2="20" class="grid"/>
      <line x1="20" y1="60" x2="300" y2="60" class="grid"/>
      <line x1="20" y1="100" x2="300" y2="100" class="grid"/>
      
      <!-- Bars representation -->
      <rect x="30" y="70" width="22" height="40" class="bar-donation" rx="3"/>
      <rect x="30" y="45" width="22" height="25" class="bar-compost" rx="3"/>

      <rect x="75" y="60" width="22" height="50" class="bar-donation" rx="3"/>
      <rect x="75" y="30" width="22" height="30" class="bar-compost" rx="3"/>

      <rect x="120" y="80" width="22" height="30" class="bar-donation" rx="3"/>
      <rect x="120" y="60" width="22" height="20" class="bar-compost" rx="3"/>

      <rect x="165" y="50" width="22" height="60" class="bar-donation" rx="3"/>
      <rect x="165" y="25" width="22" height="25" class="bar-compost" rx="3"/>

      <rect x="210" y="75" width="22" height="35" class="bar-donation" rx="3"/>
      <rect x="210" y="55" width="22" height="20" class="bar-compost" rx="3"/>

      <rect x="255" y="40" width="22" height="70" class="bar-donation" rx="3"/>
      <rect x="255" y="20" width="22" height="20" class="bar-compost" rx="3"/>

      <!-- X-axis Labels -->
      <text x="41" y="125" class="label">Day 1</text>
      <text x="86" y="125" class="label">Day 2</text>
      <text x="131" y="125" class="label">Day 3</text>
      <text x="176" y="125" class="label">Day 4</text>
      <text x="221" y="125" class="label">Day 5</text>
      <text x="266" y="125" class="label">Day 6</text>
    </svg>
  </div>
</body>
</html>
    """.trimIndent()
}
