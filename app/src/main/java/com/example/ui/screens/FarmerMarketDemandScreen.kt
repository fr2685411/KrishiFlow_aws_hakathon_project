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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropCategory
import com.example.data.model.QualityGrade
import com.example.ui.components.QualityGradeBadge
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.theme.WarningOrange

enum class MarketDemandLevel(
    val title: String,
    val containerColor: Color,
    val contentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    URGENT_PROCUREMENT(
        title = "Urgent Procurement",
        containerColor = Color(0xFFFEE2E2),
        contentColor = SpoilageRed,
        icon = Icons.Default.NotificationsActive
    ),
    HIGH_DEMAND(
        title = "High Demand",
        containerColor = Color(0xFFDCFCE7),
        contentColor = GreenPrimary,
        icon = Icons.Default.TrendingUp
    ),
    STEADY_DEMAND(
        title = "Steady Contract",
        containerColor = Color(0xFFE0E7FF),
        contentColor = Color(0xFF3730A3),
        icon = Icons.Default.Verified
    ),
    SURPLUS_WATCH(
        title = "Surplus Warning",
        containerColor = Color(0xFFFFEDD5),
        contentColor = WarningOrange,
        icon = Icons.Default.Warning
    )
}

data class CommodityDemandData(
    val id: String,
    val commodityName: String,
    val variety: String,
    val category: CropCategory,
    val demandLevel: MarketDemandLevel,
    val guaranteedHubPricePerKg: Double,
    val mandiBenchmarkPerKg: Double,
    val totalQuotaKg: Double,
    val committedKg: Double,
    val minQualityGrade: QualityGrade,
    val activeBuyerSummary: String,
    val deliveryWindow: String,
    val pickupHub: String
) {
    val remainingQuotaKg: Double
        get() = (totalQuotaKg - committedKg).coerceAtLeast(0.0)

    val fulfillmentPercentage: Int
        get() = if (totalQuotaKg > 0) ((committedKg / totalQuotaKg) * 100).toInt().coerceIn(0, 100) else 0

    val farmerGainPercent: Int
        get() = if (mandiBenchmarkPerKg > 0) {
            (((guaranteedHubPricePerKg - mandiBenchmarkPerKg) / mandiBenchmarkPerKg) * 100).toInt()
        } else 0
}

val SampleCommodityDemands = listOf(
    CommodityDemandData(
        id = "DEM-TOM-01",
        commodityName = "Hybrid Tomato",
        variety = "Abhinav / S-312",
        category = CropCategory.VEGETABLES,
        demandLevel = MarketDemandLevel.URGENT_PROCUREMENT,
        guaranteedHubPricePerKg = 22.0,
        mandiBenchmarkPerKg = 18.0,
        totalQuotaKg = 3000.0,
        committedKg = 1800.0,
        minQualityGrade = QualityGrade.GRADE_A,
        activeBuyerSummary = "D-Mart Regional DC, Reliance Fresh & 14 Local Street Vendors",
        deliveryWindow = "Next 24 to 36 Hours",
        pickupHub = "Pune Chakan Procurement Hub"
    ),
    CommodityDemandData(
        id = "DEM-ONI-02",
        commodityName = "Nashik Red Onion",
        variety = "Garwa Special",
        category = CropCategory.VEGETABLES,
        demandLevel = MarketDemandLevel.HIGH_DEMAND,
        guaranteedHubPricePerKg = 28.5,
        mandiBenchmarkPerKg = 24.0,
        totalQuotaKg = 8000.0,
        committedKg = 5200.0,
        minQualityGrade = QualityGrade.GRADE_A,
        activeBuyerSummary = "JioMart Wholesale, Gultekdi Traders & Kothrud Subji Line",
        deliveryWindow = "Weekly Continuous Intake",
        pickupHub = "Lasalgaon & Hadapsar Hubs"
    ),
    CommodityDemandData(
        id = "DEM-CAP-03",
        commodityName = "Green Capsicum",
        variety = "Indra Polyhouse",
        category = CropCategory.VEGETABLES,
        demandLevel = MarketDemandLevel.HIGH_DEMAND,
        guaranteedHubPricePerKg = 40.0,
        mandiBenchmarkPerKg = 34.0,
        totalQuotaKg = 1500.0,
        committedKg = 650.0,
        minQualityGrade = QualityGrade.GRADE_B,
        activeBuyerSummary = "3 Quick-Commerce Dark Stores & Hotel Catering Union",
        deliveryWindow = "Next 48 Hours",
        pickupHub = "Manchar / Khed Collection Point"
    ),
    CommodityDemandData(
        id = "DEM-WHT-04",
        commodityName = "Sharbati Wheat",
        variety = "MP Golden C-306",
        category = CropCategory.GRAINS,
        demandLevel = MarketDemandLevel.STEADY_DEMAND,
        guaranteedHubPricePerKg = 36.0,
        mandiBenchmarkPerKg = 32.0,
        totalQuotaKg = 12000.0,
        committedKg = 9200.0,
        minQualityGrade = QualityGrade.GRADE_A,
        activeBuyerSummary = "Direct Consumer Housing Societies & Flour Mills",
        deliveryWindow = "Farm-Gate Direct Truck Loading",
        pickupHub = "Sehore Central Grain Depot"
    ),
    CommodityDemandData(
        id = "DEM-COR-05",
        commodityName = "Fresh Coriander & Mint",
        variety = "Desi Sugandhi",
        category = CropCategory.VEGETABLES,
        demandLevel = MarketDemandLevel.URGENT_PROCUREMENT,
        guaranteedHubPricePerKg = 35.0,
        mandiBenchmarkPerKg = 28.0,
        totalQuotaKg = 800.0,
        committedKg = 320.0,
        minQualityGrade = QualityGrade.GRADE_A,
        activeBuyerSummary = "Early Morning Street Cart Vendors & Daily Subji Mandi",
        deliveryWindow = "Daily 5:30 AM Intake",
        pickupHub = "Otur / Junnar Fresh Van"
    ),
    CommodityDemandData(
        id = "DEM-POT-06",
        commodityName = "Jyoti Desi Potato",
        variety = "Kufri Jyoti",
        category = CropCategory.VEGETABLES,
        demandLevel = MarketDemandLevel.SURPLUS_WATCH,
        guaranteedHubPricePerKg = 18.5,
        mandiBenchmarkPerKg = 16.5,
        totalQuotaKg = 5000.0,
        committedKg = 4600.0,
        minQualityGrade = QualityGrade.GRADE_B,
        activeBuyerSummary = "Bulk Processing & Cold Storage Aggregators",
        deliveryWindow = "Staggered Weekly Schedule",
        pickupHub = "Indore Regional Logistics Center"
    )
)

@Composable
fun FarmerMarketDemandScreen(
    onPledgeHarvest: (CommodityDemandData) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<CropCategory?>(null) }
    var urgentOnlyFilter by remember { mutableStateOf(false) }
    var selectedDemandForPledge by remember { mutableStateOf<CommodityDemandData?>(null) }
    var pledgeSuccessMessage by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(searchQuery, selectedCategoryFilter, urgentOnlyFilter) {
        SampleCommodityDemands.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.commodityName.contains(searchQuery, ignoreCase = true) ||
                    item.variety.contains(searchQuery, ignoreCase = true) ||
                    item.pickupHub.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter
            val matchesUrgent = !urgentOnlyFilter || (item.demandLevel == MarketDemandLevel.URGENT_PROCUREMENT || item.demandLevel == MarketDemandLevel.HIGH_DEMAND)
            matchesSearch && matchesCategory && matchesUrgent
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Transparency for Farmers
        item {
            FarmerDemandHeroBanner(
                openCommoditiesCount = SampleCommodityDemands.size,
                totalOpenTonnage = SampleCommodityDemands.sumOf { it.remainingQuotaKg } / 1000.0
            )
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by crop, variety, or pickup hub...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Demand")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { searchQuery = "" }
                                    .padding(4.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_demand_input")
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null && !urgentOnlyFilter,
                            onClick = {
                                selectedCategoryFilter = null
                                urgentOnlyFilter = false
                            },
                            label = { Text("All Crops") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all_crops")
                        )
                    }
                    item {
                        FilterChip(
                            selected = urgentOnlyFilter,
                            onClick = { urgentOnlyFilter = !urgentOnlyFilter },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("High / Urgent Only")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpoilageRed,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_urgent_demand")
                        )
                    }
                    items(CropCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategoryFilter == category,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                            },
                            label = { Text(category.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Section Title: Live Market Demand List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Buyer Orders (${filteredList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Guaranteed PO Payouts",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GreenPrimary
                )
            }
        }

        // Commodity Demand Cards
        if (filteredList.isEmpty()) {
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
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No Demands Matching Filter",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try clearing filters to view all current market procurement contracts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { "demand_${it.id}_${it.commodityName}" }) { demand ->
                CommodityDemandCard(
                    demand = demand,
                    onPledgeClick = {
                        selectedDemandForPledge = demand
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Pledge Harvest Dialog
    selectedDemandForPledge?.let { demand ->
        PledgeHarvestDialog(
            demand = demand,
            onDismiss = { selectedDemandForPledge = null },
            onConfirmPledge = { qtyKg, location ->
                onPledgeHarvest(demand)
                pledgeSuccessMessage = "Successfully pledged $qtyKg kg of ${demand.commodityName}! KrishiFlow pickup scheduled from $location."
                selectedDemandForPledge = null
            }
        )
    }

    // Success notification
    pledgeSuccessMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { pledgeSuccessMessage = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Harvest Pledged Successfully!", fontWeight = FontWeight.Bold) },
            text = { Text(msg, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = { pledgeSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun FarmerDemandHeroBanner(
    openCommoditiesCount: Int,
    totalOpenTonnage: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14532D))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
                            .background(GreenAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Live Market Demand",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Verified Buyer Procurement Quotas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBBF7D0)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenAccent.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "0% Middlemen",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "Retail marts, mandi wholesalers, and urban street vendors have listed active procurement quotas below. Pledge your harvest directly to lock in guaranteed rates with instant UPI settlements upon hub delivery.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFDCFCE7)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF166534))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Open Quota", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBF7D0))
                    Text(
                        text = "%.1f Tonnes".format(totalOpenTonnage),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Active Commodities", style = MaterialTheme.typography.labelSmall, color = Color(0xFFBBF7D0))
                    Text(
                        text = "$openCommoditiesCount Crops Needed",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A))
                    )
                }
            }
        }
    }
}

@Composable
fun CommodityDemandCard(
    demand: CommodityDemandData,
    onPledgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("commodity_demand_card_${demand.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Status Badge & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = demand.demandLevel.containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = demand.demandLevel.icon,
                            contentDescription = null,
                            tint = demand.demandLevel.contentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = demand.demandLevel.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = demand.demandLevel.contentColor,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                QualityGradeBadge(grade = demand.minQualityGrade)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Commodity Name, Variety & Price Comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = demand.commodityName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${demand.variety} • ${demand.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Price Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${demand.guaranteedHubPricePerKg.toInt()}/kg",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                    Text(
                        text = "+${demand.farmerGainPercent}% vs Mandi (₹${demand.mandiBenchmarkPerKg.toInt()})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GreenAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quota Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quota: ${demand.committedKg.toInt()} / ${demand.totalQuotaKg.toInt()} kg pledged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${demand.remainingQuotaKg.toInt()} kg needed",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (demand.demandLevel == MarketDemandLevel.URGENT_PROCUREMENT) SpoilageRed else GreenPrimary
                    )
                }
                LinearProgressIndicator(
                    progress = { demand.fulfillmentPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (demand.demandLevel == MarketDemandLevel.URGENT_PROCUREMENT) SpoilageRed else GreenPrimary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Buyer Summary and Pickup Window Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Buyers: ${demand.activeBuyerSummary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Intake Window: ${demand.deliveryWindow} • ${demand.pickupHub}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pledge CTA Button
            Button(
                onClick = onPledgeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pledge_harvest_button_${demand.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (demand.demandLevel == MarketDemandLevel.URGENT_PROCUREMENT) SpoilageRed else GreenPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Pledge Harvest (${demand.remainingQuotaKg.toInt()} kg open)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PledgeHarvestDialog(
    demand: CommodityDemandData,
    onDismiss: () -> Unit,
    onConfirmPledge: (quantityKg: Double, farmLocation: String) -> Unit
) {
    var pledgeQuantity by remember { mutableStateOf(demand.remainingQuotaKg.coerceAtMost(500.0).toInt().toString()) }
    var farmLocation by remember { mutableStateOf("") }
    var harvestReadyDate by remember { mutableStateOf("Ready for immediate collection") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pledge Harvest: ${demand.commodityName}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Guaranteed PO Rate: ₹${demand.guaranteedHubPricePerKg.toInt()}/kg",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                        Text(
                            text = "Required Grade: ${demand.minQualityGrade.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = pledgeQuantity,
                    onValueChange = { pledgeQuantity = it },
                    label = { Text("Quantity to Pledge (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = farmLocation,
                    onValueChange = { farmLocation = it },
                    label = { Text("Farm / Village Location for Van Pickup") },
                    placeholder = { Text("e.g. Khed, Junnar, Dindori") },
                    modifier = Modifier.fillMaxWidth()
                )

                val qtyNum = pledgeQuantity.toDoubleOrNull() ?: 0.0
                val totalPayout = qtyNum * demand.guaranteedHubPricePerKg
                if (qtyNum > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated Farmer Payout:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "₹${totalPayout.toInt()}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = pledgeQuantity.toDoubleOrNull() ?: 100.0
                    onConfirmPledge(qty, farmLocation.ifBlank { "Local Farm Gate" })
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Confirm Pledge")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
