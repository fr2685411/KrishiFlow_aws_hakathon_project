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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuyerType
import com.example.data.model.CropCategory
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

@Composable
fun MarketplaceScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    var selectedBatchToBuy by remember { mutableStateOf<ProduceBatchEntity?>(null) }
    var showPostRfqDialog by remember { mutableStateOf(false) }
    var showPostHarvestDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Role Switcher (Farmer Portal vs Buyer Marketplace)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.setMarketplaceRole("BUYER") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (uiState.marketplaceRole == "BUYER") GreenPrimary else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.marketplaceRole == "BUYER") GreenPrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = if (uiState.marketplaceRole == "BUYER") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buyer & Marts",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.marketplaceRole == "BUYER") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.setMarketplaceRole("FARMER") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (uiState.marketplaceRole == "FARMER") GreenPrimary else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.marketplaceRole == "FARMER") GreenPrimary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = if (uiState.marketplaceRole == "FARMER") Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Farmer Portal",
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.marketplaceRole == "FARMER") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (uiState.marketplaceRole == "FARMER") {
            FarmerPortalContent(
                uiState = uiState,
                onPostHarvestClick = { showPostHarvestDialog = true }
            )
        } else {
            BuyerMarketplaceContent(
                uiState = uiState,
                viewModel = viewModel,
                onBuyBatchClick = { batch -> selectedBatchToBuy = batch },
                onPostRfqClick = { showPostRfqDialog = true }
            )
        }
    }

    // Direct Buy Dialog
    selectedBatchToBuy?.let { batch ->
        DirectBuyDialog(
            batch = batch,
            onDismiss = { selectedBatchToBuy = null },
            onConfirmOrder = { buyerName, buyerType, qty, address ->
                viewModel.placeDirectOrder(
                    batch = batch,
                    buyerName = buyerName,
                    buyerType = buyerType,
                    qtyKg = qty,
                    deliveryAddress = address
                )
                selectedBatchToBuy = null
            }
        )
    }

    // Post RFQ Dialog
    if (showPostRfqDialog) {
        PostDemandRfqDialog(
            onDismiss = { showPostRfqDialog = false },
            onSubmitRfq = { buyerName, buyerType, phone, address, crop, grade, qty, maxPrice ->
                viewModel.addNewDemand(
                    buyerName = buyerName,
                    buyerType = buyerType,
                    buyerPhone = phone,
                    destination = address,
                    cropRequested = crop,
                    minGrade = grade,
                    quantityKg = qty,
                    maxPricePerKg = maxPrice
                )
                showPostRfqDialog = false
            }
        )
    }

    // Post Harvest Dialog
    if (showPostHarvestDialog) {
        AddNewBatchDialog(
            onDismiss = { showPostHarvestDialog = false },
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
                showPostHarvestDialog = false
            }
        )
    }
}

@Composable
fun BuyerMarketplaceContent(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onBuyBatchClick: (ProduceBatchEntity) -> Unit,
    onPostRfqClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search and RFQ action banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wholesale & Retail Mart Sourcing",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Zero brokerage • Graded fresh farm lots",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onPostRfqClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post RFQ", fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search by crop, farmer, or region...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Active Stock Batches
        items(uiState.batches.filter { it.availableQuantityKg > 0 }, key = { "market_${it.id}_${it.batchCode}" }) { batch ->
            BuyerBatchCard(
                batch = batch,
                onOrderClick = { onBuyBatchClick(batch) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
fun BuyerBatchCard(
    batch: ProduceBatchEntity,
    onOrderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    Text(
                        text = batch.cropName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    QualityGradeBadge(grade = batch.qualityGrade)
                }
                if (batch.discountPercent > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SpoilageRed
                    ) {
                        Text(
                            text = "${batch.discountPercent}% FLASH OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${batch.variety} • Grown by ${batch.farmerName} (${batch.farmLocation})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                Column {
                    Text(
                        text = "Available: ${batch.availableQuantityKg.toInt()} kg",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary
                    )
                    Text(
                        text = "Delivered Price: ₹${batch.effectivePricePerKg.toInt()}/kg",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
                Button(
                    onClick = onOrderClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Order Now", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FarmerPortalContent(
    uiState: KrishiUiState,
    onPostHarvestClick: () -> Unit,
    onPledgeDemand: (CommodityDemandData) -> Unit = {}
) {
    var farmerSubTab by remember { mutableStateOf(0) } // 0: Live Market Demand, 1: Profit Calculator & My Batches
    var calcQuantity by remember { mutableStateOf(1000f) }
    var calcPrice by remember { mutableStateOf(25f) }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = farmerSubTab == 0,
                    onClick = { farmerSubTab = 0 },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Current Market Demand")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = farmerSubTab == 1,
                    onClick = { farmerSubTab = 1 },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Profit Calculator & My Crops")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (farmerSubTab == 0) {
            FarmerMarketDemandScreen(
                onPledgeHarvest = onPledgeDemand
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Direct Flow vs Mandi Comparison Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Agriculture, contentDescription = null, tint = GreenPrimary)
                                    Text(
                                        text = "Farmer Direct Advantage",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GreenPrimary
                                    )
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = GreenPrimary) {
                                    Text(
                                        text = "+18% More Net Income",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Traditional APMC Mandis deduct 8-15% middleman commission, mandi cess, and unofficial weighing deductions. KrishiFlow connects you directly with zero deduction and instant UPI settlements.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onPostHarvestClick,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("List Harvest for Direct Procurement", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Farmer Earnings Calculator
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = GoldPrimary)
                                Text(
                                    text = "Direct Flow Profit Calculator",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text("Quantity: ${calcQuantity.toInt()} kg", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = calcQuantity,
                                onValueChange = { calcQuantity = it },
                                valueRange = 100f..5000f,
                                steps = 49,
                                colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
                            )

                            Text("Market Base Rate: ₹${calcPrice.toInt()}/kg", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = calcPrice,
                                onValueChange = { calcPrice = it },
                                valueRange = 10f..80f,
                                steps = 70,
                                colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary)
                            )

                            val gross = calcQuantity * calcPrice
                            val mandiCut = gross * 0.14 // 14% middleman cuts + cess
                            val mandiNet = gross - mandiCut
                            val krishiFlowNet = gross + (gross * 0.05) // Premium direct pay
                            val extraEarnings = krishiFlowNet - mandiNet

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Traditional Mandi Net:", style = MaterialTheme.typography.bodySmall)
                                        Text("₹${mandiNet.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("KrishiFlow Direct Payout:", style = MaterialTheme.typography.bodySmall)
                                        Text("₹${krishiFlowNet.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Extra In Your Pocket:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("+₹${extraEarnings.toInt()}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GreenAccent))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "My Farm Listings Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(uiState.batches.take(4), key = { "farmer_${it.id}_${it.batchCode}" }) { batch ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(batch.cropName, fontWeight = FontWeight.Bold)
                                Text("${batch.totalQuantityKg.toInt()} kg • ₹${batch.hubProcuredPricePerKg.toInt()}/kg payout", style = MaterialTheme.typography.bodySmall)
                                Text("Status: ${batch.status.displayName}", style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                            }
                            Text(
                                text = "₹${(batch.totalQuantityKg * batch.hubProcuredPricePerKg).toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@Composable
fun DirectBuyDialog(
    batch: ProduceBatchEntity,
    onDismiss: () -> Unit,
    onConfirmOrder: (buyerName: String, buyerType: BuyerType, quantityKg: Double, address: String) -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var buyerType by remember { mutableStateOf(BuyerType.LOCAL_VENDOR) }
    var orderQuantity by remember { mutableStateOf("100") }
    var deliveryAddress by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Order ${batch.cropName}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Delivered Rate: ₹${batch.effectivePricePerKg.toInt()}/kg (Max available: ${batch.availableQuantityKg.toInt()} kg)",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Business / Buyer Name (e.g. Dmart, Local Cart)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderQuantity,
                    onValueChange = { orderQuantity = it },
                    label = { Text("Quantity (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address / Mandi Stall") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = orderQuantity.toDoubleOrNull() ?: 50.0
                    if (buyerName.isNotBlank() && qty > 0) {
                        onConfirmOrder(buyerName, buyerType, qty, deliveryAddress.ifBlank { "Standard Delivery Address" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm Purchase")
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
fun PostDemandRfqDialog(
    onDismiss: () -> Unit,
    onSubmitRfq: (
        buyerName: String,
        buyerType: BuyerType,
        phone: String,
        address: String,
        crop: String,
        grade: QualityGrade,
        qty: Double,
        maxPrice: Double
    ) -> Unit
) {
    var buyerName by remember { mutableStateOf("") }
    var buyerType by remember { mutableStateOf(BuyerType.LOCAL_VENDOR) }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Post Purchase Requirement (RFQ)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Crop Needed (e.g. Tomato, Potato, Onion)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (kg)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Max Target Rate (₹/kg)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = buyerName,
                    onValueChange = { buyerName = it },
                    label = { Text("Buyer Name / Enterprise") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone (+91)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Destination") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantity.toDoubleOrNull() ?: 100.0
                    val p = maxPrice.toDoubleOrNull() ?: 30.0
                    if (crop.isNotBlank() && buyerName.isNotBlank()) {
                        onSubmitRfq(
                            buyerName,
                            buyerType,
                            phone.ifBlank { "+91 99000 00000" },
                            address.ifBlank { "Central Market Hub" },
                            crop,
                            QualityGrade.GRADE_A,
                            q,
                            p
                        )
                    }
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
