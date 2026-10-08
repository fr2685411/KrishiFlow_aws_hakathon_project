package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.SpoilagePreventionRecord
import com.example.ui.components.FreshnessGauge
import com.example.ui.components.QualityGradeBadge
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.theme.WarningOrange
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun SurplusPriorityScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    var selectedBatchForDiscount by remember { mutableStateOf<ProduceBatchEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Environmental Impact Hero Banner
        item {
            EnvironmentalImpactBanner(
                rescuedKg = uiState.totalRescuedKg,
                lossPreventedRupees = uiState.totalLossPreventedRupees,
                co2SavedKg = uiState.totalCo2SavedKg
            )
        }

        // Section Title: Surplus-Priority Queue
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SpoilageRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = SpoilageRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Surplus-Priority Queue",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Batches ranked dynamically by remaining shelf life, transit distance, and compatible nearby buyer demand.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Urgent Surplus Batch Cards
        if (uiState.surplusBatches.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "Zero Critical Spoilage Risk!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GreenPrimary
                            )
                            Text(
                                text = "All current warehouse batches are well within safe freshness thresholds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(uiState.surplusBatches, key = { "surplus_${it.id}_${it.batchCode}" }) { batch ->
                SurplusBatchCard(
                    batch = batch,
                    onTriggerFlashDiscount = {
                        selectedBatchForDiscount = batch
                    }
                )
            }
        }

        // Audit Trail of Rescued Produce
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Recycling,
                        contentDescription = null,
                        tint = EcoTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Food Waste Rescue Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${uiState.spoilageRecords.size} rescues",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(uiState.spoilageRecords, key = { "spoilage_${it.id}_${it.batchCode}_${it.timestamp}" }) { record ->
            RescueLogItem(record = record)
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }

    // Flash Discount Dialog
    selectedBatchForDiscount?.let { batch ->
        FlashDiscountDialog(
            batch = batch,
            onDismiss = { selectedBatchForDiscount = null },
            onApply = { discountPct ->
                viewModel.applySurplusDiscount(batch.id, discountPct)
                selectedBatchForDiscount = null
            }
        )
    }
}

@Composable
fun EnvironmentalImpactBanner(
    rescuedKg: Double,
    lossPreventedRupees: Double,
    co2SavedKg: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF14532D) // Dark forest green
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF14532D), Color(0xFF166534))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GreenAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Waste & Spoilage Prevention",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Environmental Hackathon Pilot",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenAccent
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GreenAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "0% Loss Aim",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Food Rescued", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBF7D0))
                        Text(
                            text = "${rescuedKg.toInt()} kg",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text("Prevented landfill rot", style = MaterialTheme.typography.labelSmall, color = Color(0xFF86EFAC))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Value Protected", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBF7D0))
                        Text(
                            text = "₹${lossPreventedRupees.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                        )
                        Text("Farmer revenue saved", style = MaterialTheme.typography.labelSmall, color = Color(0xFF86EFAC))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CO2e Avoided", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBF7D0))
                        Text(
                            text = "${co2SavedKg.toInt()} kg",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text("Greenhouse gas cut", style = MaterialTheme.typography.labelSmall, color = Color(0xFF86EFAC))
                    }
                }
            }
        }
    }
}

@Composable
fun SurplusBatchCard(
    batch: ProduceBatchEntity,
    onTriggerFlashDiscount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED) // Warm alert tint
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SpoilageRed.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SpoilageRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Surplus Action Needed",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SpoilageRed
                            )
                        }
                    }
                    Text(
                        text = batch.batchCode,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                QualityGradeBadge(grade = batch.qualityGrade)
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        text = "From: ${batch.farmerName} • ${batch.farmLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (batch.discountPercent > 0) {
                        Text(
                            text = "₹${batch.deliveredPricePerKg.toInt()}/kg",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${batch.effectivePricePerKg.toInt()}/kg (-${batch.discountPercent}%)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpoilageRed
                            )
                        )
                    } else {
                        Text(
                            text = "₹${batch.deliveredPricePerKg.toInt()}/kg",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Freshness & Shelf Life Urgency Indicator
            FreshnessGauge(
                freshnessPercentage = batch.freshnessPercentage,
                remainingHours = batch.remainingHours
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${batch.availableQuantityKg.toInt()} kg remaining unsold",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WarningOrange
                )
                Button(
                    onClick = onTriggerFlashDiscount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (batch.discountPercent > 0) GreenPrimary else WarningOrange
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (batch.discountPercent > 0) "Update Discount" else "Flash Discount Alert",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RescueLogItem(record: SpoilagePreventionRecord) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = record.cropName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = GreenLight
                    ) {
                        Text(
                            text = "${record.quantityKg.toInt()} kg Rescued",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Sold to: ${record.buyerName} (-${record.discountAppliedPercent}% flash sale)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${record.rescuedValueRupees.toInt()}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                )
                Text(
                    text = "-${record.co2SavedKg.toInt()} kg CO2e",
                    style = MaterialTheme.typography.labelSmall,
                    color = EcoTeal
                )
            }
        }
    }
}

@Composable
fun FlashDiscountDialog(
    batch: ProduceBatchEntity,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    var selectedDiscount by remember { mutableStateOf(if (batch.discountPercent > 0) batch.discountPercent else 20) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Discount,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Surplus Priority Clearance", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Batch ${batch.batchCode} (${batch.cropName}) has ${batch.remainingHours} hours left in optimal freshness window.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Broadcast automated discount to nearby vendors to ensure 100% stock clearance before spoilage:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(15, 20, 25, 35).forEach { pct ->
                        OutlinedButton(
                            onClick = { selectedDiscount = pct },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDiscount == pct) WarningOrange.copy(alpha = 0.15f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedDiscount == pct) WarningOrange else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = "$pct%",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedDiscount == pct) WarningOrange else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val newPrice = batch.deliveredPricePerKg * (1.0 - selectedDiscount / 100.0)
                        Text(
                            text = "New Vendor Price: ₹${newPrice.toInt()}/kg (Original: ₹${batch.deliveredPricePerKg.toInt()}/kg)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Farmer payout is protected at agreed PO rate of ₹${batch.hubProcuredPricePerKg.toInt()}/kg.",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(selectedDiscount) },
                colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
            ) {
                Text("Broadcast Clearance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
