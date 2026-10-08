package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.BuyerType
import com.example.data.model.CropCategory
import com.example.data.model.OrderStatus
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.components.FreshnessGauge
import com.example.ui.components.QualityGradeBadge
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

enum class VyaparBuyerCategory(val title: String, val buyerType: BuyerType) {
    SUPERMARKET("🏢 Supermarket DC", BuyerType.SUPERMARKET_MART),
    QUICK_COMMERCE("⚡ Quick-Commerce", BuyerType.SUPERMARKET_MART),
    HOTEL_CATERING("👨‍🍳 Hotel / HoReCa", BuyerType.MANDI_WHOLESALER),
    RETAIL_MANDI("🏪 Subji Mart / Vendor", BuyerType.LOCAL_VENDOR)
}

@Composable
fun VyaparBuyerAppScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    var selectedBuyerPersona by rememberSaveable { mutableStateOf(VyaparBuyerCategory.SUPERMARKET) }
    var selectedBatchForOrder by remember { mutableStateOf<ProduceBatchEntity?>(null) }
    var showCreateRfqModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Clean B2B Buyer Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Vyapar B2B Sourcing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "Wholesale Direct",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E)),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Farm-fresh procurement without middleman markups",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showCreateRfqModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("post_rfq_header_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Post RFQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Buyer Business Persona Switcher
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(VyaparBuyerCategory.values()) { category ->
                        val isSelected = selectedBuyerPersona == category
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFB45309) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.clickable { selectedBuyerPersona = category }
                        ) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Content Based on uiState.activeTab (Synchronized with Bottom Navigation)
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.activeTab) {
                0 -> VyaparCatalogTab(
                    uiState = uiState,
                    onBuyClick = { batch -> selectedBatchForOrder = batch }
                )
                1 -> VyaparSurplusDealsTab(
                    uiState = uiState,
                    onClaimDeal = { batch -> selectedBatchForOrder = batch }
                )
                2 -> MandiIntelligenceScreen(uiState = uiState)
                3 -> VyaparRfqDemandsTab(
                    uiState = uiState,
                    onCreateRfqClick = { showCreateRfqModal = true }
                )
                4 -> VyaparOrdersTrackingTab(
                    uiState = uiState,
                    viewModel = viewModel
                )
                else -> VyaparCatalogTab(
                    uiState = uiState,
                    onBuyClick = { batch -> selectedBatchForOrder = batch }
                )
            }
        }
    }

    // Direct Buy / Bulk PO Order Dialog
    selectedBatchForOrder?.let { batch ->
        VyaparPurchaseOrderDialog(
            batch = batch,
            buyerType = selectedBuyerPersona.buyerType,
            onDismiss = { selectedBatchForOrder = null },
            onConfirmOrder = { buyerName, buyerType, qty, address ->
                viewModel.placeDirectOrder(
                    batch = batch,
                    buyerName = buyerName,
                    buyerType = buyerType,
                    qtyKg = qty,
                    deliveryAddress = address
                )
                selectedBatchForOrder = null
            }
        )
    }

    // Post Custom Commercial RFQ Dialog
    if (showCreateRfqModal) {
        VyaparPostRfqDialog(
            defaultBuyerType = selectedBuyerPersona.buyerType,
            onDismiss = { showCreateRfqModal = false },
            onSubmit = { name, type, phone, dest, crop, grade, qty, price ->
                viewModel.addNewDemand(name, type, phone, dest, crop, grade, qty, price)
                showCreateRfqModal = false
            }
        )
    }
}

/**
 * Tab 0: Modern Wholesale Produce Catalog
 */
@Composable
fun VyaparCatalogTab(
    uiState: KrishiUiState,
    onBuyClick: (ProduceBatchEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CropCategory?>(null) }
    var onlyGradeA by remember { mutableStateOf(false) }

    val activeBatches = uiState.batches.filter { batch ->
        batch.availableQuantityKg > 0 &&
                (selectedCategory == null || batch.category == selectedCategory) &&
                (!onlyGradeA || batch.qualityGrade == QualityGrade.GRADE_A) &&
                (searchQuery.isBlank() || batch.cropName.contains(searchQuery, ignoreCase = true) || batch.variety.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search & Filter Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tomatoes, onions, capsicum, grains...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category and Grade Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Produce", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == CropCategory.VEGETABLES,
                        onClick = { selectedCategory = if (selectedCategory == CropCategory.VEGETABLES) null else CropCategory.VEGETABLES },
                        label = { Text("🥦 Vegetables", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == CropCategory.GRAINS,
                        onClick = { selectedCategory = if (selectedCategory == CropCategory.GRAINS) null else CropCategory.GRAINS },
                        label = { Text("🌾 Grains", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = onlyGradeA,
                        onClick = { onlyGradeA = !onlyGradeA },
                        label = { Text("⭐ Grade A Only", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFF92400E)
                        )
                    )
                }
            }
        }

        // Live Inventory Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activeBatches.size} Lots Ready For Same-Day Delivery",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total: ${activeBatches.sumOf { it.availableQuantityKg }.toInt()} kg",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                )
            }
        }

        if (activeBatches.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("No Produce Found", fontWeight = FontWeight.Bold)
                        Text("Try resetting filters or search terms.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            itemsIndexed(activeBatches, key = { index, batch -> "vyapar_batch_${batch.id}_${batch.batchCode}_$index" }) { _, batch ->
                VyaparProductCard(batch = batch, onOrderClick = { onBuyClick(batch) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

/**
 * Tab 1: Surplus Deals (Flash Discounted Produce)
 */
@Composable
fun VyaparSurplusDealsTab(
    uiState: KrishiUiState,
    onClaimDeal: (ProduceBatchEntity) -> Unit
) {
    val surplusBatches = uiState.surplusBatches

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // High Urgency Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Surplus Flash Rescue Deals",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "High-quality produce discounted 15%–40% to clear before peak freshness window dips. Great for restaurants & juice bars.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F1D1D),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        if (surplusBatches.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(48.dp))
                        Text("Zero Surplus Backlog!", fontWeight = FontWeight.Bold)
                        Text("All warehouse batches are well within safe freshness thresholds.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            itemsIndexed(surplusBatches, key = { index, batch -> "vyapar_surplus_${batch.id}_${batch.batchCode}_$index" }) { _, batch ->
                VyaparSurplusCard(batch = batch, onClaim = { onClaimDeal(batch) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

/**
 * Tab 3: Custom Buyer RFQs & Demand Pledges
 */
@Composable
fun VyaparRfqDemandsTab(
    uiState: KrishiUiState,
    onCreateRfqClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Commercial Demand RFQs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Broadcast your buying requirements directly to farmer clusters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCreateRfqClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New RFQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.allDemands.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("No Active RFQs", fontWeight = FontWeight.Bold)
                        Text("Create a demand tender to get matched with harvest batches.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            itemsIndexed(uiState.allDemands, key = { index, demand -> "vyapar_rfq_${demand.id}_${demand.demandCode}_$index" }) { _, demand ->
                VyaparRfqCard(demand = demand)
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

/**
 * Tab 4: Inbound Orders & Transit Tracking
 */
@Composable
fun VyaparOrdersTrackingTab(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel
) {
    val orders = uiState.orders

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Inbound Wholesale Shipments",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track farm-to-store refrigerated deliveries and instant payout records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (orders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("No Inbound Orders", fontWeight = FontWeight.Bold)
                        Text("Place an order from the Wholesale Catalog or Surplus Deals to initiate dispatch.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        } else {
            itemsIndexed(orders, key = { index, order -> "vyapar_order_${order.id}_${order.orderCode}_$index" }) { _, order ->
                VyaparOrderTrackingCard(order = order, onAdvance = { next -> viewModel.updateOrderStatus(order, next) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

// -------------------------------------------------------------
// Component Cards with Modern Clean Styling
// -------------------------------------------------------------

@Composable
fun VyaparProductCard(
    batch: ProduceBatchEntity,
    onOrderClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Crop Name, Grade Badge, Freshness
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = batch.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        QualityGradeBadge(grade = batch.qualityGrade)
                    }
                    Text(
                        text = "${batch.variety} • Code: ${batch.batchCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FreshnessGauge(
                    freshnessPercentage = batch.freshnessPercentage,
                    remainingHours = batch.remainingHours
                )
            }

            // Origin and Cold Chain details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "${batch.farmLocation} (${batch.distanceKm} km)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (batch.storageCondition == StorageCondition.COLD_STORAGE) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AcUnit, contentDescription = null, tint = EcoTeal, modifier = Modifier.size(12.dp))
                        Text(
                            text = batch.storageCondition.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = EcoTeal)
                        )
                    }
                }
            }

            // Pricing & B2B Order Call to Action
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "₹${batch.effectivePricePerKg.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                            Text(text = "/kg Delivered", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = "Available: ${batch.availableQuantityKg.toInt()} kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onOrderClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Instant PO Order", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VyaparSurplusCard(
    batch: ProduceBatchEntity,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpoilageRed.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = batch.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SpoilageRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "-${batch.discountPercent}% OFF",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SpoilageRed),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Lot: ${batch.batchCode} • ${batch.farmLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FreshnessGauge(
                    freshnessPercentage = batch.freshnessPercentage,
                    remainingHours = batch.remainingHours
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "₹${batch.effectivePricePerKg.toInt()}/kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpoilageRed)
                            )
                            Text(
                                text = "₹${batch.deliveredPricePerKg.toInt()}/kg",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }
                        Text(
                            text = "Remaining: ${batch.availableQuantityKg.toInt()} kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = SpoilageRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Claim Clearance Lot", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VyaparRfqCard(demand: BuyerDemandEntity) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${demand.requiredQuantityKg.toInt()} kg ${demand.cropRequested}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (demand.status == "OPEN") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (demand.status == "OPEN") "Seeking Farmers" else "Matched",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (demand.status == "OPEN") GreenPrimary else Color(0xFFB45309)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Target Price: Max ₹${demand.maxOfferedPricePerKg.toInt()}/kg • Destination: ${demand.destinationLocation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Posted by: ${demand.buyerName} (${demand.buyerType.displayName})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun VyaparOrderTrackingCard(
    order: OrderTransactionEntity,
    onAdvance: (OrderStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderCode,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${order.quantityKg.toInt()} kg ${order.cropName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.status) {
                        OrderStatus.DELIVERED_PAID -> Color(0xFFDCFCE7)
                        OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFE0F2FE)
                        else -> Color(0xFFFEF3C7)
                    }
                ) {
                    Text(
                        text = order.status.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                OrderStatus.DELIVERED_PAID -> GreenPrimary
                                OrderStatus.OUT_FOR_DELIVERY -> EcoTeal
                                else -> Color(0xFFB45309)
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Destination: ${order.destinationAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "B2B Invoice: ₹${order.buyerPaymentTotal.toInt()}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                )

                if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                    Button(
                        onClick = { onAdvance(OrderStatus.DELIVERED_PAID) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Confirm Store Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Interactive B2B Modals: Purchase Order & Demand RFQ
// -------------------------------------------------------------

@Composable
fun VyaparPurchaseOrderDialog(
    batch: ProduceBatchEntity,
    buyerType: BuyerType,
    onDismiss: () -> Unit,
    onConfirmOrder: (buyerName: String, buyerType: BuyerType, qty: Double, address: String) -> Unit
) {
    var buyerName by remember { mutableStateOf("City Supermarket Mart") }
    var deliveryAddress by remember { mutableStateOf("Kothrud Central DC, Gate 2, Pune") }
    var orderQuantityKg by remember { mutableDoubleStateOf(batch.availableQuantityKg.coerceAtMost(200.0)) }

    val totalAmount = orderQuantityKg * batch.effectivePricePerKg

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Confirm B2B Purchase Order", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Lot: ${batch.batchCode} • ${batch.cropName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Store / Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Receiving Dock Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order Quantity:", style = MaterialTheme.typography.bodySmall)
                        Text("${orderQuantityKg.toInt()} kg (Max: ${batch.availableQuantityKg.toInt()} kg)", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = orderQuantityKg.toFloat(),
                        onValueChange = { orderQuantityKg = it.toDouble() },
                        valueRange = 10f..batch.availableQuantityKg.toFloat(),
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rate per kg:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${batch.effectivePricePerKg.toInt()}/kg", fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated PO Total:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("₹${totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                        }
                        Text("• Includes direct cold-chain transport & verified farmer payout", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmOrder(buyerName, buyerType, orderQuantityKg, deliveryAddress)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm & Dispatch PO")
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
fun VyaparPostRfqDialog(
    defaultBuyerType: BuyerType,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        type: BuyerType,
        phone: String,
        destination: String,
        crop: String,
        grade: QualityGrade,
        qty: Double,
        price: Double
    ) -> Unit
) {
    var buyerName by remember { mutableStateOf("Regional Subji Supply Corp") }
    var buyerPhone by remember { mutableStateOf("+91 98230 44556") }
    var destination by remember { mutableStateOf("Viman Nagar Market Hub, Pune") }
    var cropRequested by remember { mutableStateOf("Hybrid Tomato") }
    var quantityKg by remember { mutableStateOf("500") }
    var maxPricePerKg by remember { mutableStateOf("22") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Broadcast Commercial Demand RFQ", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name / Outlet") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cropRequested,
                    onValueChange = { cropRequested = it },
                    label = { Text("Crop Commodity Needed") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityKg,
                        onValueChange = { quantityKg = it },
                        label = { Text("Quantity (kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxPricePerKg,
                        onValueChange = { maxPricePerKg = it },
                        label = { Text("Max Price (₹/kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Delivery Unloading Point") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityKg.toDoubleOrNull() ?: 200.0
                    val price = maxPricePerKg.toDoubleOrNull() ?: 25.0
                    onSubmit(buyerName, defaultBuyerType, buyerPhone, destination, cropRequested, QualityGrade.GRADE_A, qty, price)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Broadcast RFQ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
