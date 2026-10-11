package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.CropCategory
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.components.BatchStatusBadge
import com.example.ui.components.FreshnessGauge
import com.example.ui.components.MetricStatCard
import com.example.ui.components.OfflineInventoryScannerSyncCard
import com.example.ui.components.QualityGradeBadge
import com.example.ui.components.VisualInventorySummaryDashboard
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun HubOperationsScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var selectedBatchForInspection by remember { mutableStateOf<ProduceBatchEntity?>(null) }
    var selectedDemandToMatch by remember { mutableStateOf<BuyerDemandEntity?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBatchDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_batch_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Crop Batch")
                    Text("New Batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Hub Stock",
                        value = "${(uiState.totalInventoryKg / 1000).format(1)} T",
                        subtitle = "${uiState.batches.count { it.status == BatchStatus.PROCURED_IN_HUB }} active batches",
                        icon = Icons.Default.Inventory2,
                        accentColor = GreenPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Open RFQs",
                        value = "${uiState.openDemands.size}",
                        subtitle = "${uiState.openDemands.sumOf { it.requiredQuantityKg }.toInt()} kg demand",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = GoldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Visual Inventory Summary Dashboard: Total Stock, Crop Type Breakdown & Expiry Progress Tracker
            item {
                VisualInventorySummaryDashboard(
                    batches = uiState.batches,
                    onNavigateToSurplusScreen = {
                        viewModel.setActiveTab(2) // Navigate to Surplus Priority Screen
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Local Room Cache Queue & Offline Barcode/QR Inventory Scanner for Firestore Sync
            item {
                OfflineInventoryScannerSyncCard(
                    queuedItems = uiState.queuedInventory,
                    pendingSyncCount = uiState.pendingOfflineSyncCount,
                    workManagerSyncState = uiState.workManagerSyncState,
                    onScanOfflineItem = { batchCode, cropName, category, variety, qualityGrade, quantityKg, farmerName, farmLocation, askingPricePerKg, storageCondition, freshnessDays ->
                        viewModel.scanAndQueueOfflineInventory(
                            batchCode = batchCode,
                            cropName = cropName,
                            category = category,
                            variety = variety,
                            qualityGrade = qualityGrade,
                            quantityKg = quantityKg,
                            farmerName = farmerName,
                            farmLocation = farmLocation,
                            askingPricePerKg = askingPricePerKg,
                            storageCondition = storageCondition,
                            freshnessDays = freshnessDays
                        )
                    },
                    onTriggerSync = {
                        viewModel.triggerOfflineQueueManualSync()
                    },
                    onDeleteItem = { queueId ->
                        viewModel.deleteOfflineQueueItem(queueId)
                    },
                    onClearSynced = {
                        viewModel.clearCompletedOfflineSyncedItems()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Demand-to-Supply Smart Matcher Section
            if (uiState.openDemands.isNotEmpty()) {
                item {
                    DemandMatcherBanner(
                        openDemands = uiState.openDemands,
                        batches = uiState.batches,
                        onMatchClick = { demand ->
                            selectedDemandToMatch = demand
                        }
                    )
                }
            }

            // Category filter chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Warehouse Inventory",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedCategory == null,
                                onClick = { viewModel.setCategoryFilter(null) },
                                label = { Text("All Commodities") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(CropCategory.values()) { cat ->
                            FilterChip(
                                selected = uiState.selectedCategory == cat,
                                onClick = { viewModel.setCategoryFilter(cat) },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Batch list items
            if (uiState.batches.isEmpty()) {
                item {
                    EmptyBatchState()
                }
            } else {
                items(uiState.batches, key = { "batch_${it.id}_${it.batchCode}" }) { batch ->
                    HubBatchItemCard(
                        batch = batch,
                        onVerifyIntake = {
                            selectedBatchForInspection = batch
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialog for Quality Inspection / Intake to Hub
    selectedBatchForInspection?.let { batch ->
        IntakeInspectionDialog(
            batch = batch,
            onDismiss = { selectedBatchForInspection = null },
            onConfirmIntake = {
                viewModel.verifyAndProcureBatch(batch)
                selectedBatchForInspection = null
            }
        )
    }

    // Dialog for Smart Demand Matching
    selectedDemandToMatch?.let { demand ->
        val candidateBatches = uiState.batches.filter {
            it.availableQuantityKg > 0 &&
                    it.cropName.contains(demand.cropRequested, ignoreCase = true)
        }
        DemandMatchSelectionDialog(
            demand = demand,
            candidateBatches = candidateBatches,
            onDismiss = { selectedDemandToMatch = null },
            onAllocate = { b, qty ->
                viewModel.matchDemandWithBatch(demand, b, qty)
                selectedDemandToMatch = null
            }
        )
    }

    // Dialog for Adding new Batch
    if (showAddBatchDialog) {
        AddNewBatchDialog(
            onDismiss = { showAddBatchDialog = false },
            onAdd = { crop, cat, varName, gr, fName, fPhone, fLoc, dist, qty, askPrice, storage, freshDays, notes ->
                viewModel.addNewBatch(
                    cropName = crop,
                    category = cat,
                    variety = varName,
                    grade = gr,
                    farmerName = fName,
                    farmerPhone = fPhone,
                    farmLocation = fLoc,
                    distanceKm = dist,
                    quantityKg = qty,
                    askingPricePerKg = askPrice,
                    storageCondition = storage,
                    expectedFreshnessDays = freshDays,
                    notes = notes
                )
                showAddBatchDialog = false
            }
        )
    }
}

@Composable
fun DemandMatcherBanner(
    openDemands: List<BuyerDemandEntity>,
    batches: List<ProduceBatchEntity>,
    onMatchClick: (BuyerDemandEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Demand Matcher • Live Opportunities",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "${openDemands.size} Pending",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Match verified buyer orders directly to farm inventory before purchase to maintain 0% idle holding risk.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            openDemands.take(2).forEach { demand ->
                val matchingBatches = batches.filter {
                    it.availableQuantityKg > 0 && it.cropName.contains(demand.cropRequested, ignoreCase = true)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${demand.buyerName} • ${demand.buyerType.displayName}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Needs: ${demand.requiredQuantityKg.toInt()} kg ${demand.cropRequested} (Max ₹${demand.maxOfferedPricePerKg.toInt()}/kg)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (matchingBatches.isNotEmpty()) "✓ ${matchingBatches.size} batch available (${matchingBatches.sumOf { it.availableQuantityKg }.toInt()} kg)" else "No matching stock yet",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (matchingBatches.isNotEmpty()) GreenPrimary else GoldPrimary
                            )
                        }
                        Button(
                            onClick = { onMatchClick(demand) },
                            enabled = matchingBatches.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                        ) {
                            Text("Allocate", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HubBatchItemCard(
    batch: ProduceBatchEntity,
    onVerifyIntake: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Code, Grade & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = batch.batchCode,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                    QualityGradeBadge(grade = batch.qualityGrade)
                }
                BatchStatusBadge(status = batch.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crop Name & Variety
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = batch.cropName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${batch.variety} • ${batch.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${batch.effectivePricePerKg.toInt()}/kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                    Text(
                        text = "Farmer: ₹${batch.hubProcuredPricePerKg.toInt()}/kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Freshness Indicator
            FreshnessGauge(
                freshnessPercentage = batch.freshnessPercentage,
                remainingHours = batch.remainingHours
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Farmer & Location info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${batch.farmLocation} (${batch.distanceKm} km)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = batch.farmerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantities Row: Total, Available, Reserved
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${batch.totalQuantityKg.toInt()} kg", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Available", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                    Text("${batch.availableQuantityKg.toInt()} kg", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Reserved", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                    Text("${batch.reservedQuantityKg.toInt()} kg", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldPrimary))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sold", style = MaterialTheme.typography.labelSmall, color = EcoTeal)
                    Text("${batch.soldQuantityKg.toInt()} kg", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EcoTeal))
                }
            }

            if (batch.status == BatchStatus.LISTED) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onVerifyIntake,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Quality Check & Intake into Hub", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun IntakeInspectionDialog(
    batch: ProduceBatchEntity,
    onDismiss: () -> Unit,
    onConfirmIntake: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Procurement Quality Check",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Verify batch ${batch.batchCode} from farmer ${batch.farmerName}:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("• Crop: ${batch.cropName} (${batch.variety})", style = MaterialTheme.typography.bodySmall)
                        Text("• Weighed Net Quantity: ${batch.totalQuantityKg.toInt()} kg", style = MaterialTheme.typography.bodySmall)
                        Text("• Declared Grade: ${batch.qualityGrade.displayName}", style = MaterialTheme.typography.bodySmall)
                        Text("• Inspection: Meets sorting, grading & moisture criteria", style = MaterialTheme.typography.bodySmall)
                        Text("• Farmer Agreed Payout: ₹${batch.hubProcuredPricePerKg}/kg (Total: ₹${(batch.totalQuantityKg * batch.hubProcuredPricePerKg).toInt()})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                    }
                }
                Text(
                    text = "Once approved, stock will be added to KrishiFlow central inventory ready for order reservation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmIntake,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Approve & Procure")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DemandMatchSelectionDialog(
    demand: BuyerDemandEntity,
    candidateBatches: List<ProduceBatchEntity>,
    onDismiss: () -> Unit,
    onAllocate: (ProduceBatchEntity, Double) -> Unit
) {
    var selectedBatch by remember { mutableStateOf(candidateBatches.firstOrNull()) }
    var allocateQty by remember { mutableStateOf(demand.requiredQuantityKg.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Match Demand: ${demand.buyerName}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Request: ${demand.requiredQuantityKg.toInt()} kg ${demand.cropRequested} at max ₹${demand.maxOfferedPricePerKg.toInt()}/kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Select source farmer batch to allocate:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                candidateBatches.forEach { b ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedBatch?.id == b.id) GreenLight else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedBatch?.id == b.id) GreenPrimary else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "${b.batchCode} • ${b.farmerName}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Available: ${b.availableQuantityKg.toInt()} kg • Price: ₹${b.effectivePricePerKg.toInt()}/kg",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = { selectedBatch = b },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedBatch?.id == b.id) GreenPrimary else MaterialTheme.colorScheme.surface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 32.dp)
                            ) {
                                Text(
                                    if (selectedBatch?.id == b.id) "Selected" else "Pick",
                                    fontSize = 11.sp,
                                    color = if (selectedBatch?.id == b.id) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = allocateQty,
                    onValueChange = { allocateQty = it },
                    label = { Text("Quantity to Reserve (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedBatch?.let { b ->
                        val qty = allocateQty.toDoubleOrNull() ?: demand.requiredQuantityKg
                        onAllocate(b, qty)
                    }
                },
                enabled = selectedBatch != null,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm Allocation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNewBatchDialog(
    onDismiss: () -> Unit,
    onAdd: (
        cropName: String,
        category: CropCategory,
        variety: String,
        grade: QualityGrade,
        farmerName: String,
        farmerPhone: String,
        farmLocation: String,
        distanceKm: Int,
        quantityKg: Double,
        askingPricePerKg: Double,
        storageCondition: StorageCondition,
        expectedFreshnessDays: Int,
        notes: String
    ) -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CropCategory.VEGETABLES) }
    var variety by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf(QualityGrade.GRADE_A) }
    var farmerName by remember { mutableStateOf("") }
    var farmerPhone by remember { mutableStateOf("") }
    var farmLocation by remember { mutableStateOf("") }
    var distanceKm by remember { mutableStateOf("30") }
    var quantityKg by remember { mutableStateOf("") }
    var askingPrice by remember { mutableStateOf("") }
    var freshnessDays by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("List New Harvest Batch", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cropName,
                    onValueChange = { cropName = it },
                    label = { Text("Crop Name (e.g. Tomato, Onion, Wheat)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety (e.g. Hybrid S-312, Garwa, Sharbati)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityKg,
                        onValueChange = { quantityKg = it },
                        label = { Text("Quantity (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = askingPrice,
                        onValueChange = { askingPrice = it },
                        label = { Text("Asking Price (₹/kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = freshnessDays,
                        onValueChange = { freshnessDays = it },
                        label = { Text("Shelf Life (Days)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = distanceKm,
                        onValueChange = { distanceKm = it },
                        label = { Text("Distance (km)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = farmerName,
                    onValueChange = { farmerName = it },
                    label = { Text("Farmer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = farmerPhone,
                    onValueChange = { farmerPhone = it },
                    label = { Text("Farmer Contact (+91)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = farmLocation,
                    onValueChange = { farmLocation = it },
                    label = { Text("Farm Location / Village / Tehsil") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Harvest / Quality Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cropName.isNotBlank() && quantityKg.toDoubleOrNull() != null && askingPrice.toDoubleOrNull() != null) {
                        onAdd(
                            cropName,
                            category,
                            variety.ifBlank { "Standard Variety" },
                            grade,
                            farmerName.ifBlank { "Verified Producer" },
                            farmerPhone.ifBlank { "+91 98000 00000" },
                            farmLocation.ifBlank { "Local Agriculture Zone" },
                            distanceKm.toIntOrNull() ?: 30,
                            quantityKg.toDoubleOrNull() ?: 100.0,
                            askingPrice.toDoubleOrNull() ?: 25.0,
                            StorageCondition.VENTILATED_CRATE,
                            freshnessDays.toIntOrNull() ?: 5,
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Publish Listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyBatchState() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No Batches Matching Filter",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add new farmer harvest listings or reset commodity filter to view stock.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
