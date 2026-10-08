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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.OrderTransactionEntity
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun SaathiDriverAppScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    var showMapDialog by remember { mutableStateOf(false) }
    val activeTrips = uiState.orders.filter { it.status == OrderStatus.OUT_FOR_DELIVERY || it.status == OrderStatus.RESERVED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Driver Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EcoTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "KrishiFlow Saathi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Fleet & Delivery Executive App",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCCFBF1)
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
                            Text(
                                text = "Van #MH-12-KF-4820",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EcoTeal),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F766E))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Shipments", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCCFBF1))
                            Text("${activeTrips.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Load", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCCFBF1))
                            Text("${activeTrips.sumOf { it.quantityKg }.toInt()} kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Vehicle Capacity", style = MaterialTheme.typography.labelSmall, color = Color(0xFFCCFBF1))
                            Text("2,000 kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A)))
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showMapDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EcoTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_view_routes_map_btn")
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Live Regional Transit & Hubs Map", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "Today's Dispatch Tasks (${activeTrips.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (activeTrips.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp))
                        Text("All Deliveries Completed!", fontWeight = FontWeight.Bold)
                        Text("No pending transit tasks. New pickups will appear here once matched.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(activeTrips, key = { "driver_${it.id}_${it.orderCode}" }) { order ->
                DriverTripCard(
                    order = order,
                    onUpdateStatus = { next -> viewModel.updateOrderStatus(order, next) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showMapDialog) {
        AlertDialog(
            onDismissRequest = { showMapDialog = false },
            confirmButton = {
                Button(
                    onClick = { showMapDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoTeal)
                ) {
                    Text("Close Map")
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                ) {
                    RegionalLogisticsMapScreen(uiState = uiState)
                }
            }
        )
    }
}

@Composable
fun DriverTripCard(
    order: OrderTransactionEntity,
    onUpdateStatus: (OrderStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderCode,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                OrderStatusBadge(status = order.status)
            }

            Text(
                text = "${order.quantityKg.toInt()} kg ${order.cropName}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("1. Pickup: Farmer ${order.farmerName}", style = MaterialTheme.typography.bodySmall)
                    Text("2. Drop: ${order.buyerName} • ${order.destinationAddress}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            if (order.status == OrderStatus.RESERVED) {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.OUT_FOR_DELIVERY) },
                    colors = ButtonDefaults.buttonColors(containerColor = EcoTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Pickup & Transit Route", fontWeight = FontWeight.Bold)
                }
            } else if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                Button(
                    onClick = { onUpdateStatus(OrderStatus.DELIVERED_PAID) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Confirm Delivery & Release Farmer UPI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
