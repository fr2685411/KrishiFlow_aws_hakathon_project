package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.export.HarvestReportPdfGenerator
import com.example.ui.components.FoodWasteAnalyticsModule
import com.example.ui.components.HarvestYieldTrendDashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.analytics.HarvestProductionEstimator
import com.example.data.analytics.HistoricalCropTrendSummary
import com.example.data.analytics.ProductionCycleEstimate
import com.example.data.model.CropCategory
import com.example.data.model.HarvestHistoryLogEntity
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageLight
import com.example.ui.theme.SpoilageRed
import com.example.ui.theme.WarningLight
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestHistoryLogScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCropFilter by remember { mutableStateOf<String?>(null) }
    var showAddLogDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }

    // Future Cycle Estimator interactive state
    var estimatorCrop by remember { mutableStateOf("Tomato") }
    var estimatorAcreage by remember { mutableDoubleStateOf(2.0) }

    // Computed Trend Summaries
    val trendSummaries = remember(uiState.harvestLogs) {
        HarvestProductionEstimator.analyzeCropTrends(uiState.harvestLogs)
    }

    // Computed Future Production Estimate
    val futureEstimate = remember(estimatorCrop, estimatorAcreage, uiState.harvestLogs) {
        HarvestProductionEstimator.estimateFutureProduction(
            cropName = estimatorCrop,
            plannedAcres = estimatorAcreage,
            logs = uiState.harvestLogs
        )
    }

    // Filtered logs
    val filteredLogs = remember(uiState.harvestLogs, selectedCropFilter) {
        if (selectedCropFilter == null) {
            uiState.harvestLogs.sortedByDescending { it.saleDateTimestamp }
        } else {
            uiState.harvestLogs.filter { it.cropName.equals(selectedCropFilter, ignoreCase = true) }
                .sortedByDescending { it.saleDateTimestamp }
        }
    }

    val availableCrops = remember(uiState.harvestLogs) {
        listOf("All Crops") + uiState.harvestLogs.map { it.cropName }.distinct()
    }

    Scaffold(
        modifier = modifier.testTag("harvest_history_log_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Harvest & Sale History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Room DB Historical Trends & Future Cycle Estimator",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("harvest_log_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerWorkManagerSync() },
                        modifier = Modifier.testTag("sync_workmanager_btn")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync Room to Cloud", tint = GreenPrimary)
                    }
                    IconButton(
                        onClick = { showPdfExportDialog = true },
                        modifier = Modifier.testTag("export_pdf_top_bar_btn")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF Report", tint = GoldPrimary)
                    }
                    IconButton(
                        onClick = { showAddLogDialog = true },
                        modifier = Modifier.testTag("add_log_top_bar_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Log", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddLogDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Past Harvest", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("log_past_harvest_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("harvest_logs_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // WORKMANAGER BACKGROUND CLOUD SYNCHRONIZATION STATUS STRIP
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().testTag("workmanager_sync_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(shape = CircleShape, color = GreenLight, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = "WorkManager Auto-Sync to Cloud",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = uiState.workManagerSyncState ?: "Syncs Room DB to Firestore on internet connection",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (uiState.workManagerSyncState?.contains("Syncing") == true) GoldPrimary else GreenPrimary
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerWorkManagerSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("sync_now_workmanager_btn")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // =========================================================================
            // 0. PDF EXPORT BANNER FOR BANKING & AGRI AUTHORITIES
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("export_pdf_banner_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = CircleShape, color = GreenPrimary, modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Official Banking & Credit Summary Report",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                                    )
                                    Text(
                                        text = "बैंक ऋण व कृषि अधिकारी हेतु PDF रिपोर्ट",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(shape = RoundedCornerShape(6.dp), color = GoldLight) {
                                Text(
                                    text = "Bank Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldPrimary),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Export verified harvest quantities, acreage yields, and realized sales proceeds directly from your Room DB to share with KCC loan officers, NABARD inspectors, or banking managers.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = Color(0xFF1F2937)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (uiState.harvestLogs.isEmpty()) {
                                        Toast.makeText(context, "No harvest records to export yet.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        try {
                                            val file = HarvestReportPdfGenerator.generateReport(context, uiState.harvestLogs)
                                            HarvestReportPdfGenerator.shareReportPdf(context, file)
                                            Toast.makeText(context, "Generated PDF: ${file.name}", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.3f).testTag("quick_share_pdf_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share PDF Report", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }

                            OutlinedButton(
                                onClick = { showPdfExportDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("customize_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Customize", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 1. D3 / RECHARTS 6-MONTH HARVEST YIELD TRENDS DASHBOARD
            // =========================================================================
            item {
                HarvestYieldTrendDashboard(
                    harvestLogs = uiState.harvestLogs,
                    modifier = Modifier.fillMaxWidth().testTag("harvest_yield_trend_dashboard")
                )
            }

            // =========================================================================
            // 1B. FOOD WASTE & SURPLUS LOSS FORECASTER MODULE (ऐतिहासिक विक्री वि. उत्पादन)
            // =========================================================================
            item {
                FoodWasteAnalyticsModule(
                    harvestLogs = uiState.harvestLogs,
                    activeBatches = uiState.batches,
                    onNavigateToSurplusScreen = { onNavigateBack() },
                    modifier = Modifier.fillMaxWidth().testTag("food_waste_analytics_card")
                )
            }

            // =========================================================================
            // 2. FUTURE CYCLE ESTIMATOR TOOL CARD (भविष्य चक्र उत्पादन अनुमानक)
            // =========================================================================
            item {
                FutureProductionCycleEstimatorCard(
                    estimate = futureEstimate,
                    selectedCrop = estimatorCrop,
                    acreage = estimatorAcreage,
                    onCropSelected = { estimatorCrop = it },
                    onAcreageChanged = { estimatorAcreage = it },
                    availableCrops = listOf("Tomato", "Red Onion", "Sharbati Wheat", "Soybean", "Green Chilli")
                )
            }

            // =========================================================================
            // 2. HISTORICAL PRODUCTION TREND SUMMARY TILES
            // =========================================================================
            if (trendSummaries.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.QueryStats, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Historical Crop Trends & Yields",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "${uiState.harvestLogs.size} Seasons Logged",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            trendSummaries.forEach { summary ->
                                HistoricalCropSummaryCard(summary = summary)
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. LOG FILTER CHIPS
            // =========================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableCrops.forEach { crop ->
                        val isSelected = (crop == "All Crops" && selectedCropFilter == null) || (crop == selectedCropFilter)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCropFilter = if (crop == "All Crops") null else crop
                            },
                            label = { Text(crop, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("filter_crop_${crop.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            // =========================================================================
            // 4. HISTORICAL HARVEST & SALE LOG LIST
            // =========================================================================
            if (filteredLogs.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp))
                            Text(
                                text = "No harvest logs found for this filter.",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Record your past harvest quantities and sale dates to unlock automatic future yield forecasting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { showAddLogDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Log Harvest & Sale")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredLogs,
                    key = { it.id }
                ) { log ->
                    HarvestHistoryLogCard(
                        log = log,
                        onDelete = {
                            viewModel.deleteHarvestSaleLog(log.id)
                            Toast.makeText(context, "Removed log for ${log.cropName}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Modal dialog to record new harvest and sale log
    if (showAddLogDialog) {
        AddHarvestHistoryLogDialog(
            onDismiss = { showAddLogDialog = false },
            onConfirm = { crop, category, variety, season, acres, harvestKg, harvestDate, saleDate, soldKg, rate, mandi, loss, cycle, notes ->
                viewModel.recordHarvestSaleLog(
                    cropName = crop,
                    category = category,
                    variety = variety,
                    harvestSeason = season,
                    plantedAreaAcres = acres,
                    harvestQuantityKg = harvestKg,
                    harvestDateTimestamp = harvestDate,
                    saleDateTimestamp = saleDate,
                    quantitySoldKg = soldKg,
                    soldRatePerKg = rate,
                    buyerOrMandiName = mandi,
                    storageLossKg = loss,
                    cycleDurationDays = cycle,
                    notes = notes
                )
                showAddLogDialog = false
            }
        )
    }

    // Modal dialog to customize and export official PDF summary report
    if (showPdfExportDialog) {
        ExportPdfReportDialog(
            logs = uiState.harvestLogs,
            onDismiss = { showPdfExportDialog = false },
            onSharePdf = { name, phone, location, kisanId ->
                try {
                    val file = HarvestReportPdfGenerator.generateReport(
                        context = context,
                        logs = uiState.harvestLogs,
                        farmerName = name,
                        farmerPhone = phone,
                        farmLocation = location,
                        kisanId = kisanId
                    )
                    HarvestReportPdfGenerator.shareReportPdf(context, file)
                    Toast.makeText(context, "Exported PDF to ${file.name}", Toast.LENGTH_SHORT).show()
                    showPdfExportDialog = false
                } catch (e: Exception) {
                    Toast.makeText(context, "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            onViewPdf = { name, phone, location, kisanId ->
                try {
                    val file = HarvestReportPdfGenerator.generateReport(
                        context = context,
                        logs = uiState.harvestLogs,
                        farmerName = name,
                        farmerPhone = phone,
                        farmLocation = location,
                        kisanId = kisanId
                    )
                    HarvestReportPdfGenerator.viewReportPdf(context, file)
                    showPdfExportDialog = false
                } catch (e: Exception) {
                    Toast.makeText(context, "PDF opening failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

/**
 * Interactive Future Production Cycle Estimator Card:
 * Lets the farmer choose a crop and planned acreage to estimate production yield,
 * harvest date, and revenue projections from historical Room DB data.
 */
@Composable
fun FutureProductionCycleEstimatorCard(
    estimate: ProductionCycleEstimate,
    selectedCrop: String,
    acreage: Double,
    onCropSelected: (String) -> Unit,
    onAcreageChanged: (Double) -> Unit,
    availableCrops: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("future_cycle_estimator_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = CircleShape, color = GreenLight, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Future Production Cycle Estimator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                        Text(
                            text = "भविष्य चक्र उत्पादन एवं आय अनुमानक",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(shape = RoundedCornerShape(6.dp), color = GreenLight) {
                    Text(
                        text = "AI Forecast",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Input Controls: Crop Selector & Planned Acreage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Crop Chips
                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select Crop:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableCrops.forEach { crop ->
                            val isSelected = crop == selectedCrop
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { onCropSelected(crop) }
                                    .testTag("estimator_crop_btn_$crop")
                            ) {
                                Text(
                                    text = crop,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Acreage Slider
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Planned Farming Land:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", acreage)} Acres",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                }
                Slider(
                    value = acreage.toFloat(),
                    onValueChange = { onAcreageChanged(it.toDouble()) },
                    valueRange = 0.5f..15.0f,
                    steps = 29,
                    colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary),
                    modifier = Modifier.testTag("estimator_acreage_slider")
                )
            }

            // Calculation Results Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GreenLight.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Estimated Yield", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = GreenPrimary)
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", estimate.estimatedHarvestQuintals)} Qtl",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                            Text(
                                text = "(${estimate.estimatedHarvestKg.toInt()} kg total)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estimated Harvest", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = GreenPrimary)
                            Text(
                                text = estimate.estimatedHarvestDateString,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "~${estimate.projectedCycleDays} days duration",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Projected Revenue", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = GreenPrimary)
                            Text(
                                text = "₹${estimate.projectedRevenueMin.toInt() / 1000}k - ₹${estimate.projectedRevenueMax.toInt() / 1000}k",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldPrimary)
                            )
                            Text(
                                text = "At peak timing",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GoldPrimary
                            )
                        }
                    }

                    HorizontalDivider(color = GreenPrimary.copy(alpha = 0.2f))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = estimate.optimalSalesTimingAdvice,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF14532D)
                        )
                    }

                    Text(
                        text = estimate.historicalBasisNote,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Trend card showing aggregate stats for a specific crop across logged historical seasons.
 */
@Composable
fun HistoricalCropSummaryCard(summary: HistoricalCropTrendSummary, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.cropName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GreenLight
                ) {
                    Text(
                        text = summary.yieldTrendDirection,
                        style = MaterialTheme.typography.labelSmall.copy(color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${summary.seasonsCount} Seasons Logged (Best: ${summary.bestSeason})",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Avg Yield / Acre", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${summary.avgYieldQuintalsPerAcre} Qtl",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Avg Realized Rate", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "₹${summary.avgPricePerKg}/kg",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldPrimary)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Total Harvest: ${(summary.totalHarvestedKg / 1000.0).toInt()} MT",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${(summary.totalRevenue / 1000).toInt()}k Earned",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = GreenPrimary
                )
            }
        }
    }
}

/**
 * Individual Historical Harvest & Sale Log Entry Card
 */
@Composable
fun HarvestHistoryLogCard(
    log: HarvestHistoryLogEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("harvest_log_card_${log.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Crop, Variety, Season & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${log.cropName} (${log.variety})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GreenLight
                    ) {
                        Text(
                            text = log.harvestSeason,
                            style = MaterialTheme.typography.labelSmall.copy(color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_log_${log.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = SpoilageRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Dates & Mandi Channel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "Harvest: ${log.formattedHarvestDate}  •  Sold: ${log.formattedSaleDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(
                        text = log.buyerOrMandiName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Production Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Harvest Volume", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${log.harvestQuantityKg.toInt()} Kg",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "From ${log.plantedAreaAcres} Acres",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Yield per Acre", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", log.yieldQuintalsPerAcre)} Qtl/Acre",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                    Text(
                        text = "${log.cycleDurationDays} days cycle",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Revenue", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "₹${log.totalRevenueRupees.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                    Text(
                        text = "@ ₹${log.soldRatePerKg}/kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }
            }

            if (log.notes.isNotBlank()) {
                Text(
                    text = "📝 ${log.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dialog to add a historical harvest quantity and sale date log
 */
@Composable
fun AddHarvestHistoryLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        cropName: String,
        category: CropCategory,
        variety: String,
        harvestSeason: String,
        plantedAreaAcres: Double,
        harvestQuantityKg: Double,
        harvestDateTimestamp: Long,
        saleDateTimestamp: Long,
        quantitySoldKg: Double,
        soldRatePerKg: Double,
        buyerOrMandiName: String,
        storageLossKg: Double,
        cycleDurationDays: Int,
        notes: String
    ) -> Unit
) {
    var cropName by remember { mutableStateOf("Tomato") }
    var category by remember { mutableStateOf(CropCategory.VEGETABLES) }
    var variety by remember { mutableStateOf("Hybrid Super Red") }
    var season by remember { mutableStateOf("Kharif 2026") }
    var acresText by remember { mutableStateOf("2.0") }
    var harvestKgText by remember { mutableStateOf("16000") }
    var soldKgText by remember { mutableStateOf("15600") }
    var rateText by remember { mutableStateOf("26.0") }
    var mandiName by remember { mutableStateOf("Pimpalgaon Baswant APMC") }
    var lossKgText by remember { mutableStateOf("400") }
    var cycleDaysText by remember { mutableStateOf("80") }
    var notes by remember { mutableStateOf("Good fruit setting with organic biostimulant.") }

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
                .testTag("add_harvest_log_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Record Past Harvest & Sale Log",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "पूर्व फसल उत्पादन एवं बिक्री दर्ज करें",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name (e.g. Tomato, Onion, Wheat)") },
                        modifier = Modifier.fillMaxWidth().testTag("log_input_crop")
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = variety,
                            onValueChange = { variety = it },
                            label = { Text("Variety") },
                            modifier = Modifier.weight(1f).testTag("log_input_variety")
                        )
                        OutlinedTextField(
                            value = season,
                            onValueChange = { season = it },
                            label = { Text("Season (e.g. Rabi 2025-26)") },
                            modifier = Modifier.weight(1f).testTag("log_input_season")
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = acresText,
                            onValueChange = { acresText = it },
                            label = { Text("Planted Acres") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("log_input_acres")
                        )
                        OutlinedTextField(
                            value = harvestKgText,
                            onValueChange = { harvestKgText = it },
                            label = { Text("Harvest Quantity (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("log_input_harvest_kg")
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = soldKgText,
                            onValueChange = { soldKgText = it },
                            label = { Text("Quantity Sold (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("log_input_sold_kg")
                        )
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it },
                            label = { Text("Sold Rate (₹/kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("log_input_rate")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = mandiName,
                        onValueChange = { mandiName = it },
                        label = { Text("Buyer or Mandi Channel") },
                        modifier = Modifier.fillMaxWidth().testTag("log_input_mandi")
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lossKgText,
                            onValueChange = { lossKgText = it },
                            label = { Text("Storage Loss (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cycleDaysText,
                            onValueChange = { cycleDaysText = it },
                            label = { Text("Cycle Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Field Notes / Observations") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val acres = acresText.toDoubleOrNull() ?: 1.0
                                val harvestKg = harvestKgText.toDoubleOrNull() ?: 1000.0
                                val soldKg = soldKgText.toDoubleOrNull() ?: harvestKg
                                val rate = rateText.toDoubleOrNull() ?: 20.0
                                val loss = lossKgText.toDoubleOrNull() ?: 0.0
                                val cycle = cycleDaysText.toIntOrNull() ?: 90
                                val now = System.currentTimeMillis()

                                onConfirm(
                                    cropName,
                                    category,
                                    variety,
                                    season,
                                    acres,
                                    harvestKg,
                                    now - (10 * 86400000L),
                                    now,
                                    soldKg,
                                    rate,
                                    mandiName,
                                    loss,
                                    cycle,
                                    notes
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).testTag("save_harvest_log_btn")
                        ) {
                            Text("Save to Room DB", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog allowing the farmer to customize borrower identification details
 * and generate / share or view the official PDF report.
 */
@Composable
fun ExportPdfReportDialog(
    logs: List<HarvestHistoryLogEntity>,
    onDismiss: () -> Unit,
    onSharePdf: (farmerName: String, phone: String, location: String, kisanId: String) -> Unit,
    onViewPdf: (farmerName: String, phone: String, location: String, kisanId: String) -> Unit
) {
    var farmerName by remember { mutableStateOf("Ramesh Patil") }
    var farmerPhone by remember { mutableStateOf("+91 98220 14829") }
    var farmLocation by remember { mutableStateOf("Niphad, Nashik, Maharashtra") }
    var kisanId by remember { mutableStateOf("KF-KISAN-98421") }

    val totalRevenue = remember(logs) { logs.sumOf { it.totalRevenueRupees } }
    val totalHarvestKg = remember(logs) { logs.sumOf { it.harvestQuantityKg } }

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
                .testTag("export_pdf_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = CircleShape, color = GreenLight, modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = "Export Bank & Loan PDF Report",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "प्रमाणित फसल उत्पादन व आय रिपोर्ट",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GreenPrimary
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Summary Statistics Strip
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Records", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${logs.size} Seasons", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Output", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${(totalHarvestKg / 1000.0).toInt()} MT", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Proceeds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldPrimary))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Borrower & Landholding Identification",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    OutlinedTextField(
                        value = farmerName,
                        onValueChange = { farmerName = it },
                        label = { Text("Farmer / Borrower Name") },
                        modifier = Modifier.fillMaxWidth().testTag("pdf_input_farmer_name")
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = farmerPhone,
                            onValueChange = { farmerPhone = it },
                            label = { Text("Mobile Number") },
                            modifier = Modifier.weight(1f).testTag("pdf_input_phone")
                        )
                        OutlinedTextField(
                            value = kisanId,
                            onValueChange = { kisanId = it },
                            label = { Text("Kisan Registration ID") },
                            modifier = Modifier.weight(1f).testTag("pdf_input_kisan_id")
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = farmLocation,
                        onValueChange = { farmLocation = it },
                        label = { Text("Farm Location / District") },
                        modifier = Modifier.fillMaxWidth().testTag("pdf_input_location")
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Includes official Room DB ledger hash, APMC procurement settlement records, and credit repayment capacity assessment.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSharePdf(farmerName, farmerPhone, farmLocation, kisanId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("generate_share_pdf_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF to Bank / WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onViewPdf(farmerName, farmerPhone, farmLocation, kisanId)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("view_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open & View PDF")
                        }
                    }
                }
            }
        }
    }
}
