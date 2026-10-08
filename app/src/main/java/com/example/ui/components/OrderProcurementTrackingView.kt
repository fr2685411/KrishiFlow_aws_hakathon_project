package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuyerType
import com.example.data.model.OrderStatus
import com.example.data.model.OrderTransactionEntity
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * State representation for the 3-stage produce procurement workflow:
 * 1. PENDING: Order placed, produce matched, stock reserved
 * 2. IN_TRANSIT: Farmgate pickup completed, in freight transit
 * 3. DELIVERED: Received at dock, weight verified, instant UPI payout released
 */
enum class ProcurementWorkflowState(
    val title: String,
    val hindiTitle: String,
    val stepIndex: Int,
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector
) {
    PENDING(
        title = "Pending",
        hindiTitle = "प्रतीक्षारत",
        stepIndex = 1,
        containerColor = Color(0xFFFEF3C7),
        contentColor = Color(0xFFB45309),
        icon = Icons.Default.Schedule
    ),
    IN_TRANSIT(
        title = "In Transit",
        hindiTitle = "मार्ग में",
        stepIndex = 2,
        containerColor = Color(0xFFE0F2FE),
        contentColor = Color(0xFF0369A1),
        icon = Icons.Default.LocalShipping
    ),
    DELIVERED(
        title = "Delivered",
        hindiTitle = "वितरित",
        stepIndex = 3,
        containerColor = Color(0xFFDCFCE7),
        contentColor = Color(0xFF15803D),
        icon = Icons.Default.CheckCircle
    );

    companion object {
        fun fromOrderStatus(status: OrderStatus): ProcurementWorkflowState = when (status) {
            OrderStatus.PENDING_CONFIRMATION, OrderStatus.RESERVED -> PENDING
            OrderStatus.OUT_FOR_DELIVERY -> IN_TRANSIT
            OrderStatus.DELIVERED_PAID -> DELIVERED
        }
    }
}

enum class OrderStateFilter(val label: String, val hindiLabel: String) {
    ALL("All Orders", "सभी ऑर्डर"),
    PENDING("Pending", "प्रतीक्षारत"),
    IN_TRANSIT("In Transit", "मार्ग में"),
    DELIVERED("Delivered", "वितरित")
}

/**
 * State-based Order Tracking Composable for the user's profile.
 * Visualizes order states ('Pending' -> 'In Transit' -> 'Delivered'),
 * order metrics, interactive 3-step progress steppers, and state advancement controls.
 */
@Composable
fun OrderProcurementTrackingView(
    orders: List<OrderTransactionEntity>,
    onAdvanceStatus: (OrderTransactionEntity, OrderStatus) -> Unit,
    onCreateSampleOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(OrderStateFilter.ALL) }

    val pendingCount = remember(orders) {
        orders.count { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.PENDING }
    }
    val inTransitCount = remember(orders) {
        orders.count { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.IN_TRANSIT }
    }
    val deliveredCount = remember(orders) {
        orders.count { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.DELIVERED }
    }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            OrderStateFilter.ALL -> orders
            OrderStateFilter.PENDING -> orders.filter { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.PENDING }
            OrderStateFilter.IN_TRANSIT -> orders.filter { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.IN_TRANSIT }
            OrderStateFilter.DELIVERED -> orders.filter { ProcurementWorkflowState.fromOrderStatus(it.status) == ProcurementWorkflowState.DELIVERED }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =========================================================================
        // 1. ORDER METRICS SUMMARY ROW
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Orders",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${orders.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "In Transit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$inTransitCount Active",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0369A1)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Delivered / Paid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$deliveredCount Settled",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GreenPrimary
                )
            }
        }

        // =========================================================================
        // 2. STATE FILTER CHIPS ('All', 'Pending', 'In Transit', 'Delivered')
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderStateFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                val count = when (filter) {
                    OrderStateFilter.ALL -> orders.size
                    OrderStateFilter.PENDING -> pendingCount
                    OrderStateFilter.IN_TRANSIT -> inTransitCount
                    OrderStateFilter.DELIVERED -> deliveredCount
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = "${filter.label} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (filter) {
                            OrderStateFilter.ALL -> GreenPrimary
                            OrderStateFilter.PENDING -> Color(0xFFD97706)
                            OrderStateFilter.IN_TRANSIT -> Color(0xFF0284C7)
                            OrderStateFilter.DELIVERED -> GreenPrimary
                        },
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_order_state_${filter.name.lowercase()}")
                )
            }
        }

        // =========================================================================
        // 3. ORDERS LIST WITH 3-STEP PROGRESS STEPPERS
        // =========================================================================
        if (filteredOrders.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No ${selectedFilter.label} orders found",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Procurement orders placed by commercial buyers or matched with your produce will appear here with live updates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = onCreateSampleOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("create_sample_order_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Sample Procurement Order")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                filteredOrders.forEach { order ->
                    OrderTrackingCard(
                        order = order,
                        onAdvanceStatus = { next -> onAdvanceStatus(order, next) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Order Tracking Card with interactive 3-step state progression:
 * [ Pending ] ----> [ In Transit ] ----> [ Delivered ]
 */
@Composable
fun OrderTrackingCard(
    order: OrderTransactionEntity,
    onAdvanceStatus: (OrderStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val currentState = ProcurementWorkflowState.fromOrderStatus(order.status)
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(order.createdTimestamp) { dateFormat.format(Date(order.createdTimestamp)) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotate"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderCode}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Order Code, Commodity Name & Live Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = order.orderCode,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GreenLight
                        ) {
                            Text(
                                text = "${order.quantityKg.toInt()} kg",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${order.cropName} • Placed $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // State Badge ('Pending', 'In Transit', 'Delivered')
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = currentState.containerColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentState.contentColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = currentState.icon,
                            contentDescription = null,
                            tint = currentState.contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = currentState.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = currentState.contentColor
                            )
                        )
                    }
                }
            }

            // =========================================================================
            // 3-STAGE STATE PROGRESS STEPPER
            // =========================================================================
            ProcurementStateStepper(
                currentState = currentState,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Payout and Financial Snapshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Farmer Direct Payout",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", order.farmerPayoutTotal)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Buyer Payment",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", order.buyerPaymentTotal)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Middleman Cut",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹0 (0% Cut)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = EcoTeal
                    )
                }
            }

            // Expandable Details (Farmer, Buyer, Transport Route)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Origin & Destination Route
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Origin: ${order.farmerName}'s Farmgate",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Destination: ${order.destinationAddress}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Buyer Information
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Commercial Buyer: ${order.buyerName} (${order.buyerType.displayName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Delivery Timestamp if delivered
                    if (order.deliveredTimestamp != null) {
                        val deliveredDate = dateFormat.format(Date(order.deliveredTimestamp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Delivered & POD Verified: $deliveredDate",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = GreenPrimary
                            )
                        }
                    }
                }
            }

            // Bottom Actions: State Advancement Button + Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand / Collapse Details Button
                Row(
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Details" else "View Route & Details",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(rotationAngle)
                    )
                }

                // State Progression Action Button
                when (order.status) {
                    OrderStatus.PENDING_CONFIRMATION, OrderStatus.RESERVED -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.OUT_FOR_DELIVERY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("advance_to_in_transit_${order.orderCode}")
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dispatch (In Transit)", fontSize = 12.sp)
                        }
                    }
                    OrderStatus.OUT_FOR_DELIVERY -> {
                        Button(
                            onClick = { onAdvanceStatus(OrderStatus.DELIVERED_PAID) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("advance_to_delivered_${order.orderCode}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm Delivered", fontSize = 12.sp)
                        }
                    }
                    OrderStatus.DELIVERED_PAID -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Payout Settled",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3-Stage Progress Stepper:
 * [ (1) Pending ] ────── [ (2) In Transit ] ────── [ (3) Delivered ]
 */
@Composable
fun ProcurementStateStepper(
    currentState: ProcurementWorkflowState,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        ProcurementWorkflowState.PENDING,
        ProcurementWorkflowState.IN_TRANSIT,
        ProcurementWorkflowState.DELIVERED
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = currentState.stepIndex > step.stepIndex
            val isCurrent = currentState.stepIndex == step.stepIndex

            val nodeBgColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> GreenPrimary
                    isCurrent -> step.contentColor
                    else -> MaterialTheme.colorScheme.surface
                },
                label = "node_bg"
            )
            val nodeBorderColor = when {
                isCompleted -> GreenPrimary
                isCurrent -> step.contentColor
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val nodeTextColor = when {
                isCompleted || isCurrent -> Color.White
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            // Step Node & Label Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(nodeBgColor)
                        .border(1.5.dp, nodeBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${step.stepIndex}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = nodeTextColor
                        )
                    }
                }

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    color = if (isCurrent) step.contentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Connecting line between steps
            if (index < steps.size - 1) {
                val isLineActive = currentState.stepIndex > index + 1
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(if (isLineActive) GreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }
    }
}
