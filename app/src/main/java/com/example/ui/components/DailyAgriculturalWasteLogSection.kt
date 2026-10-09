package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.widget.Toast
import com.example.data.export.WasteLogCsvExportResult
import com.example.data.export.WasteLogCsvExporter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropCategory
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import com.example.data.model.WasteReason
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.util.Locale

/**
 * Interactive Jetpack Compose component that tracks daily agricultural surplus and waste.
 * Farmers can view logs from the Room Database, inspect aggregate metrics (donations vs. composting),
 * and record surplus earmarked for donation or organic composting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyAgriculturalWasteLogSection(
    wasteLogs: List<WasteLog>,
    onLogWaste: (
        cropName: String,
        category: CropCategory,
        quantityKg: Double,
        destination: WasteDestination,
        reason: WasteReason,
        destinationPartner: String,
        estimatedValueRupees: Double,
        notes: String
    ) -> Unit,
    onDeleteLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showLogDialog by remember { mutableStateOf(false) }
    var selectedFilterDestination by remember { mutableStateOf<WasteDestination?>(null) }
    var logToDelete by remember { mutableStateOf<WasteLog?>(null) }
    var exportResult by remember { mutableStateOf<WasteLogCsvExportResult?>(null) }
    val context = LocalContext.current

    val totalDonatedKg = remember(wasteLogs) {
        wasteLogs.filter { it.destination == WasteDestination.DONATION }.sumOf { it.quantityKg }
    }
    val totalCompostedKg = remember(wasteLogs) {
        wasteLogs.filter { it.destination == WasteDestination.COMPOSTING }.sumOf { it.quantityKg }
    }
    val totalDivertedKg = remember(wasteLogs) {
        wasteLogs.sumOf { it.quantityKg }
    }
    val totalCo2SavedKg = remember(wasteLogs) {
        wasteLogs.sumOf { it.co2DivertedKg }
    }

    val filteredLogs = remember(wasteLogs, selectedFilterDestination) {
        if (selectedFilterDestination == null) wasteLogs
        else wasteLogs.filter { it.destination == selectedFilterDestination }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("daily_waste_log_section_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. HEADER & ACTION
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Recycling,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Daily Surplus & Waste Diverter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "Room DB",
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
                            text = "Track agricultural surplus logged for donation or composting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // =========================================================================
                    // 2. AGGREGATE SUMMARY TILES (DONATION, COMPOSTING, CO2 SAVED)
                    // =========================================================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Donation Tile
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Donated",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF047857)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f kg", totalDonatedKg),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = "To Food Banks / NGOs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color(0xFF047857).copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Composting Tile
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Composted",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFFB45309)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f kg", totalCompostedKg),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Into Bio-Fertilizer",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color(0xFFB45309).copy(alpha = 0.8f)
                                )
                            }
                        }

                        // CO2 Saved Tile
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF3B82F6).copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Co2,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "CO₂e Saved",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f kg", totalCo2SavedKg),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1D4ED8)
                                )
                                Text(
                                    text = "Diverted from Dump",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = Color(0xFF1D4ED8).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // =========================================================================
                    // 3. ACTION BAR: LOG BUTTON, CSV EXPORT + DESTINATION FILTERS
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showLogDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("log_waste_open_dialog_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Log Surplus", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (wasteLogs.isEmpty()) {
                                        Toast.makeText(context, "No surplus logs to export", Toast.LENGTH_SHORT).show()
                                    } else {
                                        exportResult = WasteLogCsvExporter.exportToCsv(context, wasteLogs)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("export_waste_logs_csv_btn")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Export CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            }
                        }

                        Text(
                            text = "${filteredLogs.size} logs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Destination filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedFilterDestination == null,
                            onClick = { selectedFilterDestination = null },
                            label = { Text("All (${wasteLogs.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = selectedFilterDestination == WasteDestination.DONATION,
                            onClick = {
                                selectedFilterDestination =
                                    if (selectedFilterDestination == WasteDestination.DONATION) null else WasteDestination.DONATION
                            },
                            leadingIcon = {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = { Text("Donations") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF047857)
                            )
                        )
                        FilterChip(
                            selected = selectedFilterDestination == WasteDestination.COMPOSTING,
                            onClick = {
                                selectedFilterDestination =
                                    if (selectedFilterDestination == WasteDestination.COMPOSTING) null else WasteDestination.COMPOSTING
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Eco, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = { Text("Composting") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD97706).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFB45309)
                            )
                        )
                        FilterChip(
                            selected = selectedFilterDestination == WasteDestination.ANIMAL_FEED,
                            onClick = {
                                selectedFilterDestination =
                                    if (selectedFilterDestination == WasteDestination.ANIMAL_FEED) null else WasteDestination.ANIMAL_FEED
                            },
                            label = { Text("Animal Feed") }
                        )
                        FilterChip(
                            selected = selectedFilterDestination == WasteDestination.BIOGAS,
                            onClick = {
                                selectedFilterDestination =
                                    if (selectedFilterDestination == WasteDestination.BIOGAS) null else WasteDestination.BIOGAS
                            },
                            label = { Text("Biogas") }
                        )
                    }

                    // =========================================================================
                    // 4. LIST OF RECORDED WASTE LOGS
                    // =========================================================================
                    if (filteredLogs.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "No surplus or waste logs recorded yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap 'Log Surplus / Waste' above to divert unsold crops to food banks or organic compost.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredLogs.forEach { log ->
                                WasteLogItemCard(
                                    log = log,
                                    onDelete = { logToDelete = log }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating a new waste/surplus log
    if (showLogDialog) {
        LogWasteEntryDialog(
            onDismiss = { showLogDialog = false },
            onSave = { cropName, category, qty, dest, reason, partner, estValue, notes ->
                onLogWaste(cropName, category, qty, dest, reason, partner, estValue, notes)
                showLogDialog = false
            }
        )
    }

    // Confirmation dialog for deleting a log
    if (logToDelete != null) {
        val target = logToDelete!!
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Waste Log?") },
            text = { Text("Are you sure you want to delete the record for ${target.cropName} (${target.formattedQuantity})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLog(target.id)
                        logToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            }
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
 * Individual Card display for a single WasteLog entry.
 */
@Composable
fun WasteLogItemCard(
    log: WasteLog,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destinationBadgeColor = Color(log.destination.badgeColorHex)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("waste_log_item_${log.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                    Text(
                        text = log.cropName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = log.category.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = destinationBadgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = log.destination.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = destinationBadgeColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Metrics row: Quantity, Reason, Value, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Scale, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        text = log.formattedQuantity,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (log.estimatedValueRupees > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(13.dp))
                        Text(
                            text = log.formattedValue,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Co2, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(14.dp))
                    Text(
                        text = log.formattedCo2Diverted,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1D4ED8), fontSize = 10.sp)
                    )
                }
            }

            // Partner / Reason / Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (log.destinationPartner.isNotBlank()) {
                        Text(
                            text = "Partner: ${log.destinationPartner}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Cause: ${log.reason.displayName} • ${log.formattedDate}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.notes.isNotBlank()) {
                        Text(
                            text = "\"${log.notes}\"",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_waste_log_${log.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog enabling farmers to log quantities of agricultural surplus for donation or composting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogWasteEntryDialog(
    onDismiss: () -> Unit,
    onSave: (
        cropName: String,
        category: CropCategory,
        quantityKg: Double,
        destination: WasteDestination,
        reason: WasteReason,
        destinationPartner: String,
        estimatedValueRupees: Double,
        notes: String
    ) -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CropCategory.VEGETABLES) }
    var quantityText by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf(WasteDestination.DONATION) }
    var reason by remember { mutableStateOf(WasteReason.MARKET_SURPLUS) }
    var destinationPartner by remember { mutableStateOf("") }
    var estimatedValueText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cropSuggestions = listOf("Tomato", "Red Onion", "Cabbage", "Capsicum", "Potato", "Mango", "Green Chilli")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("log_waste_entry_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Log Agricultural Surplus & Waste",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Persisted to Room Database",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Crop Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Crop Name *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = {
                            cropName = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g. Hybrid Tomato, Red Onion") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("waste_crop_name_input")
                    )

                    // Quick suggestions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cropSuggestions.forEach { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { cropName = suggestion }
                            ) {
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Crop Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CropCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Quantity
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quantity in Kilograms (kg) *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it
                            errorMessage = null
                        },
                        placeholder = { Text("e.g. 150.0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = GreenPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("waste_quantity_input")
                    )
                }

                // Destination Selection (Donation vs. Composting vs. Other)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Waste / Surplus Destination *", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WasteDestination.entries.forEach { dest ->
                            val isSelected = destination == dest
                            val badgeColor = Color(dest.badgeColorHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) badgeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) badgeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        destination = dest
                                        // Auto-suggest partner based on destination
                                        if (destinationPartner.isBlank()) {
                                            destinationPartner = when (dest) {
                                                WasteDestination.DONATION -> "Local Roti Bank / Food NGO"
                                                WasteDestination.COMPOSTING -> "Farm Aerobic Vermicompost Unit"
                                                WasteDestination.ANIMAL_FEED -> "Local Gaushala Dairy Shelter"
                                                WasteDestination.BIOGAS -> "Gram Panchayat Biogas Digester"
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (dest) {
                                            WasteDestination.DONATION -> Icons.Default.VolunteerActivism
                                            WasteDestination.COMPOSTING -> Icons.Default.Eco
                                            WasteDestination.ANIMAL_FEED -> Icons.Default.Agriculture
                                            WasteDestination.BIOGAS -> Icons.Default.Recycling
                                        },
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dest.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (dest) {
                                                WasteDestination.DONATION -> "Direct to food banks & community kitchens to fight hunger"
                                                WasteDestination.COMPOSTING -> "Converted to rich organic nitrogen/potash fertilizer for soil"
                                                WasteDestination.ANIMAL_FEED -> "Diverted as fodder for dairy cows and shelter livestock"
                                                WasteDestination.BIOGAS -> "Fed into rural digester to generate clean cooking methane"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Underlying Reason
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Underlying Surplus / Waste Cause", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WasteReason.entries.forEach { r ->
                            FilterChip(
                                selected = reason == r,
                                onClick = { reason = r },
                                label = { Text(r.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Destination Partner / Facility Name
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recipient Partner / Pit Location", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    OutlinedTextField(
                        value = destinationPartner,
                        onValueChange = { destinationPartner = it },
                        placeholder = { Text("e.g. Akshaya Patra Kitchen, On-Farm Compost Bed 3") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("waste_partner_input")
                    )
                }

                // Estimated Market Value
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Estimated Farm Value in ₹ (Optional)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    OutlinedTextField(
                        value = estimatedValueText,
                        onValueChange = { estimatedValueText = it },
                        placeholder = { Text("e.g. 2400") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = GoldPrimary) },
                        modifier = Modifier.fillMaxWidth().testTag("waste_est_value_input")
                    )
                }

                // Notes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Additional Notes (Optional)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("e.g. Harvested early morning, trimmings shredded before adding dry leaves") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("waste_notes_input")
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (cropName.isBlank()) {
                                errorMessage = "Please enter crop name"
                                return@Button
                            }
                            val qty = quantityText.toDoubleOrNull()
                            if (qty == null || qty <= 0.0) {
                                errorMessage = "Please enter a valid positive quantity in kg"
                                return@Button
                            }
                            val estValue = estimatedValueText.toDoubleOrNull() ?: 0.0

                            onSave(cropName, category, qty, destination, reason, destinationPartner, estValue, notes)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("waste_save_button")
                    ) {
                        Text("Save to Room DB", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
