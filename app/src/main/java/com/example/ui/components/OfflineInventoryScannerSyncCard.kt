package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.example.data.wallet.QrCodeUtility
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.SyncQueueStatus
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interactive card displaying the local Room database cache queue for Cloud Firestore.
 * Allows scanning/logging inventory batches while offline, displays pending synchronization items,
 * and tracks automatic sync status once network connection is restored.
 */
@Composable
fun OfflineInventoryScannerSyncCard(
    queuedItems: List<OfflineInventoryQueueEntity>,
    pendingSyncCount: Int,
    workManagerSyncState: String?,
    onScanOfflineItem: (
        batchCode: String,
        cropName: String,
        category: String,
        variety: String,
        qualityGrade: String,
        quantityKg: Double,
        farmerName: String,
        farmLocation: String,
        askingPricePerKg: Double,
        storageCondition: String,
        freshnessDays: Int
    ) -> Unit,
    onTriggerSync: () -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearSynced: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showScanDialog by remember { mutableStateOf(false) }
    var isListExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (pendingSyncCount > 0) GoldPrimary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("offline_inventory_scanner_sync_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Status & Title
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
                            .background(if (pendingSyncCount > 0) GoldPrimary.copy(alpha = 0.15f) else GreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (pendingSyncCount > 0) Icons.Default.WifiOff else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (pendingSyncCount > 0) GoldPrimary else GreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Offline Scan & Cloud Queue",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (pendingSyncCount > 0) GoldLight else GreenLight
                            ) {
                                Text(
                                    text = if (pendingSyncCount > 0) "$pendingSyncCount Queued" else "Room Cache Clean",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pendingSyncCount > 0) GoldPrimary else GreenPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Room SQLite cache queue • Auto-syncs to Firestore on internet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons
                Button(
                    onClick = { showScanDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("scan_offline_batch_btn")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Lot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sync Status Indicator Banner
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
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = if (pendingSyncCount > 0) GoldPrimary else GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = workManagerSyncState ?: "Auto-Sync Ready: Pushes Room Queue to Firestore",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (pendingSyncCount > 0) {
                        TextButton(
                            onClick = onTriggerSync,
                            modifier = Modifier.testTag("trigger_manual_sync_btn")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Queued items preview or accordion
            if (queuedItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isListExpanded = !isListExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cached Items (${queuedItems.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isListExpanded) "Hide Queue" else "View Queue",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary
                            )
                            Icon(
                                imageVector = if (isListExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isListExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            queuedItems.take(5).forEach { item ->
                                QueuedItemRow(
                                    item = item,
                                    onDelete = { onDeleteItem(item.queueId) }
                                )
                            }

                            if (queuedItems.any { it.syncStatus == SyncQueueStatus.SYNCED }) {
                                TextButton(
                                    onClick = onClearSynced,
                                    modifier = Modifier.align(Alignment.End).testTag("clear_synced_items_btn")
                                ) {
                                    Text("Clear Synced History", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScanDialog) {
        OfflineScanInventoryDialog(
            onDismiss = { showScanDialog = false },
            onConfirmScan = { batchCode, crop, cat, varName, grade, qty, farmer, loc, price, store, fresh ->
                onScanOfflineItem(batchCode, crop, cat, varName, grade, qty, farmer, loc, price, store, fresh)
                showScanDialog = false
            }
        )
    }
}

@Composable
private fun QueuedItemRow(
    item: OfflineInventoryQueueEntity,
    onDelete: () -> Unit
) {
    val isPending = item.syncStatus == SyncQueueStatus.QUEUED || item.syncStatus == SyncQueueStatus.FAILED
    val formattedTime = remember(item.scannedTimestamp) {
        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(item.scannedTimestamp))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isPending) GoldPrimary.copy(alpha = 0.3f) else GreenPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().testTag("queued_item_${item.queueId}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isPending) GoldLight else GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPending) Icons.Default.CloudQueue else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPending) GoldPrimary else GreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = "${item.cropName} (${item.scannedQuantityKg.toInt()} kg)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.batchCode} • $formattedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isPending) GoldPrimary else GreenPrimary
                ) {
                    Text(
                        text = if (isPending) "Pending Sync" else "Synced",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_queue_item_${item.queueId}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Dialog simulating scanning an incoming lot barcode/QR code while offline in field.
 */
@Composable
fun OfflineScanInventoryDialog(
    onDismiss: () -> Unit,
    onConfirmScan: (
        batchCode: String,
        cropName: String,
        category: String,
        variety: String,
        qualityGrade: String,
        quantityKg: Double,
        farmerName: String,
        farmLocation: String,
        askingPricePerKg: Double,
        storageCondition: String,
        freshnessDays: Int
    ) -> Unit
) {
    var batchCodeInput by remember { mutableStateOf("SCAN-MH-${System.currentTimeMillis() % 100000}") }
    var cropNameInput by remember { mutableStateOf("Hybrid Tomato") }
    var varietyInput by remember { mutableStateOf("Abhinav S-312") }
    var quantityInput by remember { mutableStateOf("850") }
    var farmerInput by remember { mutableStateOf("Kisan Ramesh") }
    var priceInput by remember { mutableStateOf("32.0") }
    var storageInput by remember { mutableStateOf("VENTILATED_CRATE") }
    var isTorchOn by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = GreenPrimary)
                Text("Scan Harvest Batch (Offline)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Camera Scanner Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, GreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("offline_scanner_camera_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasCameraPermission) {
                        CameraQrScannerView(
                            isTorchOn = isTorchOn,
                            onQrCodeDetected = { code ->
                                batchCodeInput = code
                                if (code.contains("TOM", ignoreCase = true)) cropNameInput = "Hybrid Tomato"
                                else if (code.contains("ONI", ignoreCase = true)) cropNameInput = "Nashik Red Onion"
                                else if (code.contains("POT", ignoreCase = true)) cropNameInput = "Table Potato"
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Target Box
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(8.dp))
                        )

                        // Live Status Tag & Torch Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color.Black.copy(alpha = 0.65f)) {
                                Text(
                                    text = "Point camera at batch QR / barcode",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            IconButton(
                                onClick = { isTorchOn = !isTorchOn },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isTorchOn) Color(0xFFFEF08A) else Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Torch",
                                    tint = if (isTorchOn) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Text("Camera Access Disabled", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Enable Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Offline Mode Active: Scans are cached in Room SQLite and automatically pushed to Cloud Firestore on reconnect.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, color = Color(0xFF713F12))
                        )
                    }
                }

                OutlinedTextField(
                    value = batchCodeInput,
                    onValueChange = { batchCodeInput = it },
                    label = { Text("Scanned Barcode / Batch Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("scan_input_code")
                )

                OutlinedTextField(
                    value = cropNameInput,
                    onValueChange = { cropNameInput = it },
                    label = { Text("Crop Commodity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("scan_input_crop")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("scan_input_quantity")
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price (₹/kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("scan_input_price")
                    )
                }

                OutlinedTextField(
                    value = farmerInput,
                    onValueChange = { farmerInput = it },
                    label = { Text("Farmer / Aggregator Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityInput.toDoubleOrNull() ?: 500.0
                    val price = priceInput.toDoubleOrNull() ?: 30.0
                    onConfirmScan(
                        batchCodeInput.trim(),
                        cropNameInput.trim(),
                        "VEGETABLES",
                        varietyInput.trim(),
                        "GRADE_A",
                        qty,
                        farmerInput.trim(),
                        "Nashik Rural",
                        price,
                        storageInput,
                        5
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.testTag("confirm_offline_scan_btn")
            ) {
                Text("Save to Room Queue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
