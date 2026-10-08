package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropCategory
import com.example.data.model.FarmerProducePost
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.components.CropPriceTrendChart
import com.example.ui.components.FarmerProducePostDialog
import com.example.ui.components.FarmWeatherDashboardCard
import com.example.ui.components.HarvestInventoryShelfLifeDashboard
import com.example.ui.components.HarvestPlanningWeatherAdvisoryCard
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import kotlinx.coroutines.launch
import java.util.Locale

enum class ProduceSortOrder(val title: String) {
    LATEST("Newest First"),
    PRICE_LOW_TO_HIGH("Price (Low to High)"),
    PRICE_HIGH_TO_LOW("Price (High to Low)"),
    QUANTITY_HIGH("Largest Volume"),
    FRESHNESS("Freshness")
}

/**
 * Produce Dashboard Screen:
 * Displays a list of available agricultural produce fetched from Cloud Firestore using a LazyColumn.
 * Features:
 * - Real-time Firestore reactive flow observation
 * - Search bar with instant crop name filtering
 * - Category filter chips (All, Vegetables, Fruits, Grains, Pulses)
 * - Quality Grade filter chips (Grade A, B, C)
 * - Sorting options
 * - Dynamic metrics summary (Total Lots, Volume in Tonnes, Avg Rate)
 * - Produce detail dialog with direct farmer contact & procurement simulation
 * - Extended FAB to post new agricultural produce directly to Firestore
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduceDashboardScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onOpenHarvestHistoryLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Real-time Firestore flow of produce posts
    val liveFirestorePosts by viewModel.firestoreProducePosts.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()

    // Search and filter state
    var activeDashboardTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<CropCategory?>(null) }
    var selectedGradeFilter by remember { mutableStateOf<QualityGrade?>(null) }
    var selectedSortOrder by remember { mutableStateOf(ProduceSortOrder.LATEST) }

    // Sort menu state (Bottom Sheet & Dropdown)
    var showSortBottomSheet by remember { mutableStateOf(false) }
    var showSortDropdownMenu by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState()

    // 30-Day Price Trend Chart State
    var showPriceTrendChartSection by remember { mutableStateOf(false) }
    var trendSelectedCrop by remember { mutableStateOf("Tomato") }

    // Dialog state
    var showPostProduceDialog by remember { mutableStateOf(false) }
    var selectedPostForDetails by remember { mutableStateOf<FarmerProducePost?>(null) }
    var isManualRefreshing by remember { mutableStateOf(false) }

    // Combined list: prioritize live Firestore posts, fall back to uiState.producePosts
    val basePosts = if (liveFirestorePosts.isNotEmpty()) liveFirestorePosts else uiState.producePosts

    // Filter and sort the produce posts
    val filteredProduce = remember(basePosts, searchQuery, selectedCategoryFilter, selectedGradeFilter, selectedSortOrder) {
        basePosts.filter { post ->
            // Search filter by crop name (also supports variety or location)
            val matchesQuery = searchQuery.isBlank() ||
                    post.cropName.contains(searchQuery.trim(), ignoreCase = true) ||
                    post.variety.contains(searchQuery.trim(), ignoreCase = true) ||
                    post.farmerName.contains(searchQuery.trim(), ignoreCase = true) ||
                    post.district.contains(searchQuery.trim(), ignoreCase = true)

            // Category filter
            val matchesCategory = selectedCategoryFilter == null || post.category == selectedCategoryFilter

            // Grade filter
            val matchesGrade = selectedGradeFilter == null || post.qualityGrade == selectedGradeFilter

            matchesQuery && matchesCategory && matchesGrade
        }.let { list ->
            when (selectedSortOrder) {
                ProduceSortOrder.LATEST -> list.sortedByDescending { it.createdAt }
                ProduceSortOrder.PRICE_LOW_TO_HIGH -> list.sortedBy { it.askingPricePerKg }
                ProduceSortOrder.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.askingPricePerKg }
                ProduceSortOrder.QUANTITY_HIGH -> list.sortedByDescending { it.quantityKg }
                ProduceSortOrder.FRESHNESS -> list.sortedByDescending { it.expectedFreshnessDays }
            }
        }
    }

    // Calculated metrics
    val totalWeightKg = filteredProduce.sumOf { it.quantityKg }
    val totalWeightTonnes = totalWeightKg / 1000.0
    val totalValuation = filteredProduce.sumOf { it.totalEstimatedPayout }
    val averageRate = if (totalWeightKg > 0.0) totalValuation / totalWeightKg else 0.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Produce Dashboard",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = GreenPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "Firestore Live",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "कृषि उपज डैशबोर्ड • Real-time Agricultural Produce Feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("produce_dashboard_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = GreenPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSortBottomSheet = true },
                        modifier = Modifier.testTag("produce_dashboard_sort_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort produce list",
                            tint = GreenPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            isManualRefreshing = true
                            coroutineScope.launch {
                                viewModel.firestoreRepo.getAllProducePosts()
                                viewModel.triggerCloudSync()
                                isManualRefreshing = false
                                Toast.makeText(context, "Produce feed synced with Cloud Firestore", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("produce_dashboard_refresh_btn")
                    ) {
                        if (isManualRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = GreenPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh from Firestore",
                                tint = GreenPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPostProduceDialog = true },
                containerColor = GreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Post Produce", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("add_produce_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("produce_dashboard_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Dashboard View Switcher: Shelf-Life Tracking vs. Marketplace Lots
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val criticalCount = remember(uiState.batches) {
                        uiState.batches.count { it.remainingHours in 1..36 && it.availableQuantityKg > 0 }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeDashboardTab == 0) GreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeDashboardTab = 0 }
                            .testTag("tab_shelf_life_dashboard")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Shelf-Life & Spoilage",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeDashboardTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (criticalCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = SpoilageRed
                                ) {
                                    Text(
                                        text = "$criticalCount",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeDashboardTab == 1) GreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeDashboardTab = 1 }
                            .testTag("tab_all_marketplace_lots")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Marketplace Lots (${filteredProduce.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeDashboardTab == 1) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            if (activeDashboardTab == 0) {
                item {
                    HarvestInventoryShelfLifeDashboard(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                        onOpenHarvestHistoryLogs = onOpenHarvestHistoryLogs
                    )
                }
            } else {
                // Search Bar Item
                item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("produce_search_bar"),
                    placeholder = {
                        Text(
                            text = "Search by crop name (e.g. Tomato, Onion, Wheat)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.testTag("clear_search_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // Horizontal Filter Chips for Produce Type (e.g. 'Grains', 'Vegetables', 'Fruits', 'Pulses')
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Produce Type / उपज के प्रकार",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (selectedCategoryFilter != null) {
                            Text(
                                text = "Clear Type",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier
                                    .clickable { selectedCategoryFilter = null }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Produce Chip
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = {
                                Text(
                                    text = "All Produce (${basePosts.size})",
                                    fontWeight = if (selectedCategoryFilter == null) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Text("🌟", fontSize = 14.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .testTag("produce_type_chip_all")
                                .testTag("filter_category_all")
                        )

                        // Produce Types: Grains, Vegetables, Fruits, Pulses
                        val produceTypes = listOf(
                            Triple(CropCategory.GRAINS, "Grains", "🌾"),
                            Triple(CropCategory.VEGETABLES, "Vegetables", "🥬"),
                            Triple(CropCategory.FRUITS, "Fruits", "🍎"),
                            Triple(CropCategory.PULSES, "Pulses", "🫘")
                        )

                        produceTypes.forEach { (cat, label, emoji) ->
                            val isSelected = selectedCategoryFilter == cat
                            val count = basePosts.count { it.category == cat }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryFilter = if (isSelected) null else cat
                                },
                                label = {
                                    Text(
                                        text = "$label ($count)",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Text(emoji, fontSize = 14.sp)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .testTag("produce_type_chip_${label.lowercase()}")
                                    .testTag("filter_category_${cat.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Summary Metrics Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Available Lots",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${filteredProduce.size} Batches",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GreenPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total Volume",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (totalWeightTonnes >= 1.0) "${String.format(Locale.getDefault(), "%.1f", totalWeightTonnes)} Tonnes" else "${totalWeightKg.toInt()} kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Avg Asking Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.1f", averageRate)}/kg",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary
                            )
                        }
                    }
                }
            }

            // 30-Day Price Trends & Market Decision Intelligence Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showPriceTrendChartSection) GreenLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showPriceTrendChartSection) GreenPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPriceTrendChartSection = !showPriceTrendChartSection }
                        .testTag("toggle_price_trend_chart_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "30-Day Crop Price Trends",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (showPriceTrendChartSection) "Tap to hide interactive price analytics" else "Interactive charts to make informed pricing decisions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (showPriceTrendChartSection) GreenPrimary else GreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (showPriceTrendChartSection) "Hide Chart ▲" else "View Chart 📈",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (showPriceTrendChartSection) Color.White else GreenPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Expanded 30-Day D3 / Recharts-style Interactive Chart View
            if (showPriceTrendChartSection) {
                item {
                    CropPriceTrendChart(
                        cropName = trendSelectedCrop,
                        showCropSelector = true,
                        onCropChange = { trendSelectedCrop = it },
                        modifier = Modifier.testTag("dashboard_price_trend_chart")
                    )
                }
            }

            // Grade & Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Grade Filter Chips (scrollable)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Grade:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        QualityGrade.values().forEach { grade ->
                            val isSelected = selectedGradeFilter == grade
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedGradeFilter = if (selectedGradeFilter == grade) null else grade
                                },
                                label = {
                                    Text(
                                        when (grade) {
                                            QualityGrade.GRADE_A -> "Grade A"
                                            QualityGrade.GRADE_B -> "Grade B"
                                            QualityGrade.GRADE_C -> "Grade C"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EcoTeal,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_grade_${grade.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort Menu Button (triggers Bottom Sheet or Dropdown Menu)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { showSortBottomSheet = true }
                                .testTag("sort_menu_button")
                                .testTag("sort_dropdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort Menu",
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Sort: ${selectedSortOrder.title}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF78350F),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Attached DropdownMenu
                        DropdownMenu(
                            expanded = showSortDropdownMenu,
                            onDismissRequest = { showSortDropdownMenu = false },
                            modifier = Modifier.testTag("sort_dropdown_menu")
                        ) {
                            listOf(
                                Triple(ProduceSortOrder.PRICE_LOW_TO_HIGH, "Price (Low to High)", Icons.Default.ArrowDownward),
                                Triple(ProduceSortOrder.PRICE_HIGH_TO_LOW, "Price (High to Low)", Icons.Default.ArrowUpward),
                                Triple(ProduceSortOrder.LATEST, "Newest First", Icons.Default.Schedule)
                            ).forEach { (order, label, icon) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            fontWeight = if (selectedSortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedSortOrder == order) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selectedSortOrder == order) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    trailingIcon = if (selectedSortOrder == order) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = GreenPrimary) }
                                    } else null,
                                    onClick = {
                                        selectedSortOrder = order
                                        showSortDropdownMenu = false
                                        Toast.makeText(context, "Sorted by $label", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag(
                                        when (order) {
                                            ProduceSortOrder.PRICE_LOW_TO_HIGH -> "sort_option_price_low_to_high"
                                            ProduceSortOrder.PRICE_HIGH_TO_LOW -> "sort_option_price_high_to_low"
                                            ProduceSortOrder.LATEST -> "sort_option_newest"
                                            else -> "sort_option_${order.name.lowercase()}"
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section Header: Available Produce Count
            item {
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
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Results for \"$searchQuery\" (${filteredProduce.size})" else "All Available Produce (${filteredProduce.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (searchQuery.isNotBlank() || selectedCategoryFilter != null || selectedGradeFilter != null) {
                        Text(
                            text = "Reset Filters",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            ),
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    selectedCategoryFilter = null
                                    selectedGradeFilter = null
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Empty State
            if (filteredProduce.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("produce_empty_state_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(GreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = if (searchQuery.isNotBlank()) "No produce found matching \"$searchQuery\"" else "No agricultural produce listed yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Try clearing your search query or check spelling for common crops like Tomato, Onion, or Wheat."
                                else
                                    "Farmers can list their freshly harvested crops anytime to connect directly with supermarket marts and wholesale buyers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (searchQuery.isNotBlank()) {
                                Button(
                                    onClick = { searchQuery = "" },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Clear Search")
                                }
                            } else {
                                Button(
                                    onClick = { showPostProduceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Post First Harvest Batch")
                                }
                            }
                        }
                    }
                }
            } else {
                // List of Available Produce
                items(
                    items = filteredProduce,
                    key = { it.postId.ifBlank { "post_${it.hashCode()}" } }
                ) { produce ->
                    ProduceDashboardItemCard(
                        produce = produce,
                        onClick = { selectedPostForDetails = produce },
                        onProcureClick = {
                            Toast.makeText(context, "Procurement request created for ${produce.cropName} (${produce.formattedQuantity})", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Sort Modal Bottom Sheet
    if (showSortBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortBottomSheet = false },
            sheetState = sortSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("sort_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sort Produce By",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )
                        Text(
                            text = "उपज क्रमबद्ध करें • Order listings by price or date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = GreenLight,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                val sortOptions = listOf(
                    Triple(
                        ProduceSortOrder.PRICE_LOW_TO_HIGH,
                        "Price (Low to High)",
                        "Lowest asking rates first • Value & bulk wholesale"
                    ),
                    Triple(
                        ProduceSortOrder.PRICE_HIGH_TO_LOW,
                        "Price (High to Low)",
                        "Highest asking rates first • Premium & export lots"
                    ),
                    Triple(
                        ProduceSortOrder.LATEST,
                        "Newest First",
                        "Latest harvest posts first • Fresh arrival priority"
                    )
                )

                sortOptions.forEach { (order, label, description) ->
                    val isSelected = selectedSortOrder == order

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GreenLight.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSortOrder = order
                                showSortBottomSheet = false
                                Toast.makeText(context, "Sorted by $label", Toast.LENGTH_SHORT).show()
                            }
                            .testTag(
                                when (order) {
                                    ProduceSortOrder.PRICE_LOW_TO_HIGH -> "sort_option_price_low_to_high"
                                    ProduceSortOrder.PRICE_HIGH_TO_LOW -> "sort_option_price_high_to_low"
                                    ProduceSortOrder.LATEST -> "sort_option_newest"
                                    else -> "sort_option_${order.name.lowercase()}"
                                }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (order) {
                                            ProduceSortOrder.PRICE_LOW_TO_HIGH -> Icons.Default.ArrowDownward
                                            ProduceSortOrder.PRICE_HIGH_TO_LOW -> Icons.Default.ArrowUpward
                                            ProduceSortOrder.LATEST -> Icons.Default.Schedule
                                            else -> Icons.Default.SwapVert
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else GreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedSortOrder = order
                                    showSortBottomSheet = false
                                    Toast.makeText(context, "Sorted by $label", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = GreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    if (selectedPostForDetails != null) {
        ProduceDetailDialog(
            produce = selectedPostForDetails!!,
            onDismiss = { selectedPostForDetails = null },
            onProcure = {
                Toast.makeText(context, "Direct contract match initiated for ${selectedPostForDetails?.cropName}", Toast.LENGTH_SHORT).show()
                selectedPostForDetails = null
            }
        )
    }

    // Post Produce Dialog
    if (showPostProduceDialog) {
        val currentUserName = (uiState.authState as? com.example.data.auth.AuthUiState.Authenticated)?.user?.displayName ?: "Ramesh Patil"
        val currentUserPhone = (uiState.authState as? com.example.data.auth.AuthUiState.Authenticated)?.user?.phoneNumber?.ifBlank { "+91 98220 14829" } ?: "+91 98220 14829"

        FarmerProducePostDialog(
            initialFarmerName = currentUserName,
            initialFarmerPhone = currentUserPhone,
            initialFarmLocation = "Niphad, Nashik",
            initialDistrict = "Nashik",
            onDismiss = { showPostProduceDialog = false },
            onSubmit = { crop, cat, varName, gr, fName, fPhone, fLoc, dist, qty, unit, askPrice, mandiRate, storage, freshDays, organic, readyNow, notes ->
                viewModel.postFarmerProduce(
                    cropName = crop,
                    category = cat,
                    variety = varName,
                    grade = gr,
                    farmerName = fName,
                    farmerPhone = fPhone,
                    farmLocation = fLoc,
                    district = dist,
                    quantityKg = qty,
                    quantityUnit = unit,
                    askingPricePerKg = askPrice,
                    mandiBenchmarkPricePerKg = mandiRate,
                    storageCondition = storage,
                    expectedFreshnessDays = freshDays,
                    isOrganicOrResidueFree = organic,
                    readyForImmediatePickup = readyNow,
                    notes = notes
                )
                showPostProduceDialog = false
                Toast.makeText(context, "Produce '$crop' saved to Cloud Firestore!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Individual produce listing card in the Produce Dashboard LazyColumn
 */
@Composable
private fun ProduceDashboardItemCard(
    produce: FarmerProducePost,
    onClick: () -> Unit,
    onProcureClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevron_rotation"
    )
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy))
            .clickable { isExpanded = !isExpanded }
            .testTag("produce_item_card_${produce.postId}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Crop Avatar, Name, Variety, Firestore Sync Tag, Expand Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val cropEmoji = when {
                        produce.cropName.contains("Tomato", ignoreCase = true) -> "🍅"
                        produce.cropName.contains("Onion", ignoreCase = true) -> "🧅"
                        produce.cropName.contains("Potato", ignoreCase = true) -> "🥔"
                        produce.cropName.contains("Chilli", ignoreCase = true) -> "🌶️"
                        produce.cropName.contains("Corn", ignoreCase = true) -> "🌽"
                        produce.cropName.contains("Wheat", ignoreCase = true) -> "🌾"
                        produce.cropName.contains("Pomegranate", ignoreCase = true) -> "🍇"
                        produce.cropName.contains("Soybean", ignoreCase = true) -> "🫘"
                        else -> "🌱"
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cropEmoji, fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = produce.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${produce.variety.ifBlank { "Standard Variety" }} • ${produce.district}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Firestore Synced",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { isExpanded = !isExpanded }
                            .testTag("expand_chevron_${produce.postId}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                            tint = GreenPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }

            // Metrics Display (Quantity, Price/kg, Total Valuation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(14.dp), tint = GreenPrimary)
                        Text(
                            text = "Available Qty",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = produce.formattedQuantity,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Asking Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.2f", produce.askingPricePerKg)}/kg",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Valuation",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", produce.totalEstimatedPayout)}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                }
            }

            // Quality & Logistics Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (produce.qualityGrade) {
                        QualityGrade.GRADE_A -> Color(0xFFFEF3C7)
                        QualityGrade.GRADE_B -> Color(0xFFF1F5F9)
                        QualityGrade.GRADE_C -> Color(0xFFFFEDD5)
                    }
                ) {
                    Text(
                        text = produce.qualityGrade.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (produce.qualityGrade) {
                            QualityGrade.GRADE_A -> Color(0xFF92400E)
                            QualityGrade.GRADE_B -> Color(0xFF334155)
                            QualityGrade.GRADE_C -> Color(0xFF9A3412)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (produce.isOrganicOrResidueFree) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(11.dp))
                            Text("Residue-Free", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }

                if (produce.readyForImmediatePickup) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFCCFBF1)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EcoTeal, modifier = Modifier.size(11.dp))
                            Text("Ready Pickup", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EcoTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Fresh: ${produce.expectedFreshnessDays}d",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable View: Seller Location, Harvest Date, Quality Grade & Detailed Specs
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                        .testTag("expanded_produce_details_${produce.postId}"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Detail 1: Seller Location & Farm Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Seller Location",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "विक्रेता व खेत स्थान / Seller Location",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                )
                                Text(
                                    text = "${produce.farmerName} • ${produce.farmerPhone}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${produce.farmLocation}, District: ${produce.district}, ${produce.state}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GreenLight,
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "Calling ${produce.farmerName}: ${produce.farmerPhone}", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("call_farmer_btn_${produce.postId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Call",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Detail 2: Harvest Date & Freshness Shelf Life
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Harvest Date",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "कटाई की तारीख / Harvest Date",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                )
                                Text(
                                    text = "${produce.expectedFreshnessDays} Days Freshness Left",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    )
                                )
                            }
                            Text(
                                text = produce.harvestDate.ifBlank { "Recent Harvest (Peak Freshness)" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (produce.expectedFreshnessDays / 15f).coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = GreenPrimary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }

                    // Detail 3: Quality Grade & Storage Environment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grade,
                                contentDescription = "Quality Grade",
                                tint = EcoTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "गुणवत्ता स्तर / Quality Grade: ${produce.qualityGrade.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EcoTeal
                                )
                            )
                            Text(
                                text = produce.qualityGrade.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Storage: ${produce.storageCondition.displayName} • ${if (produce.isOrganicOrResidueFree) "100% Residue-Free" else "Standard Farming"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Detail 4: Direct Mandi Price Margin Comparison
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GoldLight.copy(alpha = 0.5f),
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
                                Text(
                                    text = "APMC Mandi Modal: ₹${produce.mandiBenchmarkPricePerKg}/kg",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "Farmer Asking: ₹${produce.askingPricePerKg}/kg",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF08A)) {
                                    Text(
                                        text = "+₹${produce.middlemanSavingsRupees.toInt()} Extra",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF713F12)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "0% Middleman Commission",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }

                    // Detail 4b: 30-Day APMC Price Trend Chart for Informed Pricing Decisions
                    var showItemPriceTrendChart by remember { mutableStateOf(false) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showItemPriceTrendChart) GreenLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (showItemPriceTrendChart) GreenPrimary else Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showItemPriceTrendChart = !showItemPriceTrendChart }
                            .testTag("item_price_trend_toggle_${produce.postId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "30-Day Price Trend Analysis (${produce.cropName})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Compare asking rate (₹${produce.askingPricePerKg}/kg) with 30-day Mandi trend",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (showItemPriceTrendChart) GreenPrimary else GreenLight
                            ) {
                                Text(
                                    text = if (showItemPriceTrendChart) "Hide ▲" else "Chart 📈",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (showItemPriceTrendChart) Color.White else GreenPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (showItemPriceTrendChart) {
                        CropPriceTrendChart(
                            cropName = produce.cropName,
                            farmerAskingPrice = produce.askingPricePerKg,
                            initialDays = 30,
                            showCropSelector = false,
                            modifier = Modifier.testTag("card_price_trend_chart_${produce.postId}")
                        )
                    }

                    // Detail 5: Packaging & Special Handling Notes
                    if (produce.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "विवरण / Packaging: ${produce.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Farmer Attribution & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Farmer: ${produce.farmerName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = produce.farmLocation,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("toggle_expand_btn_${produce.postId}")
                    ) {
                        Text(if (isExpanded) "Hide ▲" else "Details ▼", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onProcureClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("procure_btn_${produce.postId}")
                    ) {
                        Text("Procure", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Detailed view dialog for a selected agricultural produce post
 */
@Composable
private fun ProduceDetailDialog(
    produce: FarmerProducePost,
    onDismiss: () -> Unit,
    onProcure: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onProcure,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm Match & Procure")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${produce.cropName} (${produce.variety})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Farmer Info
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("किसान विवरण / Farmer Details", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text("${produce.farmerName} • ${produce.farmerPhone}", style = MaterialTheme.typography.bodyMedium)
                        Text(produce.farmLocation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Batch Financial Breakdown
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GreenLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("आर्थिक विवरण / Commercial Payout", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Quantity:")
                            Text(produce.formattedQuantity, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Asking Rate:")
                            Text("₹${produce.askingPricePerKg} / kg", fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("APMC Mandi Modal:")
                            Text("₹${produce.mandiBenchmarkPricePerKg} / kg", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Batch Payout:", fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.getDefault(), "%,.2f", produce.totalEstimatedPayout)}", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }

                // Grade & Storage
                Text("Quality: ${produce.qualityGrade.displayName}", style = MaterialTheme.typography.bodySmall)
                Text("Storage: ${produce.storageCondition.displayName}", style = MaterialTheme.typography.bodySmall)
                Text("Expected Shelf Life: ${produce.expectedFreshnessDays} Days", style = MaterialTheme.typography.bodySmall)

                if (produce.notes.isNotBlank()) {
                    Text("Packaging Notes: ${produce.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Firestore Document ID
                Text(
                    text = "Firestore Doc: ${produce.postId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
