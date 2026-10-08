package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropPriceThreshold
import com.example.data.model.MandiRateEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.ThresholdTriggerCondition
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

/**
 * Interactive card displaying configured price alerts for registered crops,
 * live APMC market prices, threshold comparisons, and actions to add/edit/test local alerts.
 */
@Composable
fun CropPriceAlertThresholdCard(
    thresholds: List<CropPriceThreshold>,
    registeredBatches: List<ProduceBatchEntity>,
    mandiRates: List<MandiRateEntity>,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onSaveThreshold: (CropPriceThreshold) -> Unit,
    onDeleteThreshold: (String) -> Unit,
    onToggleThreshold: (String, Boolean) -> Unit,
    onSendTestAlert: (cropName: String, targetPrice: Double, currentPrice: Double, mandiName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingThreshold by remember { mutableStateOf<CropPriceThreshold?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.testTag("crop_price_alert_threshold_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title and notification permission status
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
                            .background(GoldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Market Price Alerts",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GoldLight
                            ) {
                                Text(
                                    text = "भांडवली भाव अलर्ट",
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
                            text = "Instant local alerts when mandi prices hit your profit target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Add button
                IconButton(
                    onClick = {
                        editingThreshold = null
                        showCreateDialog = true
                    },
                    modifier = Modifier.testTag("add_price_threshold_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set New Alert",
                        tint = GreenPrimary
                    )
                }
            }

            // Notification permission banner if not granted
            if (!hasNotificationPermission) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth().testTag("notification_permission_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Notifications",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Allow KrishiFlow to send local notifications when target mandi prices trigger.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F)
                            )
                        }
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_notification_permission_btn")
                        ) {
                            Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Thresholds List
            if (thresholds.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No Price Alerts Configured",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set a target price per kg for your harvested crops. When APMC Mandis reach it, you'll receive a local device notification immediately.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                editingThreshold = null
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_state_add_threshold_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create First Alert", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    thresholds.forEach { threshold ->
                        val matchingMandi = mandiRates.firstOrNull { rate ->
                            rate.commodity.contains(threshold.cropName, ignoreCase = true) ||
                            threshold.cropName.contains(rate.commodity, ignoreCase = true)
                        }

                        ThresholdItemRow(
                            threshold = threshold,
                            currentMandiRate = matchingMandi,
                            onToggle = { isEnabled -> onToggleThreshold(threshold.id, isEnabled) },
                            onEdit = {
                                editingThreshold = threshold
                                showCreateDialog = true
                            },
                            onDelete = { onDeleteThreshold(threshold.id) },
                            onTestTrigger = {
                                val currentPrice = matchingMandi?.maxPricePerKg ?: (threshold.targetPricePerKg + 6.0)
                                val mandi = matchingMandi?.mandiName ?: "Pimpalgaon APMC"
                                onSendTestAlert(threshold.cropName, threshold.targetPricePerKg, currentPrice, mandi)
                            }
                        )
                    }
                }
            }

            // Quick simulation and test helper row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${thresholds.count { it.isEnabled }} active alerts monitored against live APMC rates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        val firstThreshold = thresholds.firstOrNull()
                        val crop = firstThreshold?.cropName ?: "Hybrid Tomato"
                        val target = firstThreshold?.targetPricePerKg ?: 32.0
                        onSendTestAlert(crop, target, target + 6.0, "Nashik APMC")
                    },
                    modifier = Modifier.testTag("test_local_notification_btn")
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test Notification", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialog for creating or editing threshold
    if (showCreateDialog) {
        PriceThresholdConfigDialog(
            existingThreshold = editingThreshold,
            registeredBatches = registeredBatches,
            mandiRates = mandiRates,
            onDismiss = {
                showCreateDialog = false
                editingThreshold = null
            },
            onSave = { threshold ->
                onSaveThreshold(threshold)
                showCreateDialog = false
                editingThreshold = null
            }
        )
    }
}

@Composable
private fun ThresholdItemRow(
    threshold: CropPriceThreshold,
    currentMandiRate: MandiRateEntity?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestTrigger: () -> Unit
) {
    val currentPrice = currentMandiRate?.maxPricePerKg
    val isThresholdReached = currentPrice != null && currentPrice >= threshold.targetPricePerKg

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isThresholdReached && threshold.isEnabled) GoldLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            if (isThresholdReached && threshold.isEnabled) GoldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("threshold_row_${threshold.id}")
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isThresholdReached) GoldPrimary else GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThresholdReached) Icons.Default.TrendingUp else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = threshold.cropName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (threshold.variety.isNotBlank()) {
                            Text(
                                text = threshold.variety,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Switch to enable/disable
                Switch(
                    checked = threshold.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GreenPrimary
                    ),
                    modifier = Modifier.testTag("toggle_threshold_${threshold.id}")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Pricing comparison row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alert Target Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "≥ ₹${threshold.targetPricePerKg.toInt()} /kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenPrimary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Live Mandi Rate (${currentMandiRate?.mandiName?.take(14) ?: "Local APMC"})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (currentPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "₹${currentPrice.toInt()} /kg",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isThresholdReached) GoldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (isThresholdReached) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldPrimary
                                ) {
                                    Text(
                                        text = "HIT! 🎯",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Awaiting APMC data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action buttons: Test Alert, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onTestTrigger,
                    modifier = Modifier.testTag("test_threshold_${threshold.id}")
                ) {
                    Text("Trigger Test Alert", fontSize = 11.sp, color = GreenPrimary)
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_threshold_${threshold.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_threshold_${threshold.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Dialog to configure a price alert threshold for an existing or custom registered crop.
 */
@Composable
fun PriceThresholdConfigDialog(
    existingThreshold: CropPriceThreshold?,
    registeredBatches: List<ProduceBatchEntity>,
    mandiRates: List<MandiRateEntity>,
    onDismiss: () -> Unit,
    onSave: (CropPriceThreshold) -> Unit
) {
    // Collect distinct crop names from registered batches or mandi rates
    val availableCropNames = remember(registeredBatches, mandiRates) {
        val batchCrops = registeredBatches.map { it.cropName }
        val mandiCrops = mandiRates.map { it.commodity }
        (batchCrops + mandiCrops + listOf("Hybrid Tomato", "Red Onion", "Sharbati Wheat", "Soybean", "Pomegranate", "Green Chilli"))
            .filter { it.isNotBlank() }
            .distinct()
    }

    var selectedCropName by remember {
        mutableStateOf(existingThreshold?.cropName ?: availableCropNames.firstOrNull() ?: "Hybrid Tomato")
    }
    var varietyInput by remember { mutableStateOf(existingThreshold?.variety ?: "") }
    var targetPriceInput by remember {
        mutableStateOf(existingThreshold?.targetPricePerKg?.toString() ?: "35.0")
    }
    var targetMandiInput by remember {
        mutableStateOf(existingThreshold?.targetMandiName ?: "All Nearby APMC Mandis")
    }
    var notesInput by remember { mutableStateOf(existingThreshold?.notes ?: "") }
    var isEnabled by remember { mutableStateOf(existingThreshold?.isEnabled ?: true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.AddAlert,
                    contentDescription = null,
                    tint = GreenPrimary
                )
                Text(
                    text = if (existingThreshold != null) "Edit Price Alert" else "Set Crop Price Alert",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select a registered crop and enter the price floor at which you want to receive an urgent selling notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Crop Selection chips
                Text(
                    text = "Registered Crop:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    items(availableCropNames) { crop ->
                        val isSelected = selectedCropName.equals(crop, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GreenLight else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) GreenPrimary else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable {
                                    selectedCropName = crop
                                    // Autofill variety if matching batch found
                                    val matched = registeredBatches.firstOrNull { it.cropName.equals(crop, ignoreCase = true) }
                                    if (matched != null && varietyInput.isBlank()) {
                                        varietyInput = matched.variety
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = crop,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Variety input
                OutlinedTextField(
                    value = varietyInput,
                    onValueChange = { varietyInput = it },
                    label = { Text("Variety (optional)") },
                    placeholder = { Text("e.g. Abhinav, Garwa, Sharbati") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("alert_variety_input")
                )

                // Target Price Input
                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = {
                        targetPriceInput = it
                        errorMessage = null
                    },
                    label = { Text("Target Mandi Price (₹ / kg) *") },
                    placeholder = { Text("e.g. 35.0") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = GreenPrimary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color(0xFFEF4444))
                        } else {
                            Text("You'll be alerted when APMC rate meets or exceeds this.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("alert_target_price_input")
                )

                // Target Mandi / Scope
                OutlinedTextField(
                    value = targetMandiInput,
                    onValueChange = { targetMandiInput = it },
                    label = { Text("Monitored Mandi") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPriceInput.toDoubleOrNull()
                    if (price == null || price <= 0.0) {
                        errorMessage = "Please enter a valid price greater than 0"
                        return@Button
                    }

                    val threshold = CropPriceThreshold(
                        id = existingThreshold?.id ?: java.util.UUID.randomUUID().toString(),
                        cropName = selectedCropName.trim(),
                        variety = varietyInput.trim(),
                        targetPricePerKg = price,
                        targetMandiName = targetMandiInput.trim().ifBlank { "All Nearby Mandis" },
                        triggerCondition = ThresholdTriggerCondition.GREATER_THAN_EQUAL,
                        isEnabled = isEnabled,
                        notes = notesInput.trim()
                    )
                    onSave(threshold)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                modifier = Modifier.testTag("save_price_threshold_btn")
            ) {
                Text("Save Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
