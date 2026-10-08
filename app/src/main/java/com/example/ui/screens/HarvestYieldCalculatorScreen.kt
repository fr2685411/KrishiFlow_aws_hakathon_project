package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.calculator.CalculatorCrop
import com.example.data.calculator.CalculatorIrrigationType
import com.example.data.calculator.CropCategoryType
import com.example.data.calculator.HarvestYieldCalculator
import com.example.data.calculator.HarvestYieldEstimate
import com.example.data.calculator.SoilOrganicCarbonGrade
import com.example.data.model.SoilType
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HarvestYieldCalculatorScreen(
    viewModel: KrishiViewModel,
    onNavigateBack: () -> Unit,
    onOpenSustainableShop: () -> Unit = {},
    onListHarvestBatch: (cropName: String, qtyKg: Double, pricePerKg: Double) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    // State Inputs
    var landSizeAcres by rememberSaveable { mutableDoubleStateOf(3.0) }
    var selectedCategory by rememberSaveable { mutableStateOf<CropCategoryType?>(null) }
    var selectedCropId by rememberSaveable { mutableStateOf("crop_tomato") }
    var selectedSoilType by rememberSaveable { mutableStateOf(SoilType.BLACK_CLAY) }
    var selectedOrganicCarbon by rememberSaveable { mutableStateOf(SoilOrganicCarbonGrade.GRADE_B_MEDIUM) }
    var selectedIrrigation by rememberSaveable { mutableStateOf(CalculatorIrrigationType.DRIP_FERTIGATION) }

    val crops = remember(selectedCategory) {
        if (selectedCategory == null) HarvestYieldCalculator.CROPS
        else HarvestYieldCalculator.CROPS.filter { it.category == selectedCategory }
    }

    val currentCrop = remember(selectedCropId) {
        HarvestYieldCalculator.CROPS.firstOrNull { it.id == selectedCropId }
            ?: HarvestYieldCalculator.CROPS.first()
    }

    // Dynamic Calculation
    val yieldEstimate = remember(currentCrop, landSizeAcres, selectedSoilType, selectedOrganicCarbon, selectedIrrigation) {
        HarvestYieldCalculator.calculate(
            crop = currentCrop,
            landSizeAcres = landSizeAcres,
            soilType = selectedSoilType,
            organicCarbonGrade = selectedOrganicCarbon,
            irrigationType = selectedIrrigation
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Harvest Yield Calculator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "फसल उपज एवं आय कैलकुलेटर • Precision Farm Tool",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("yield_calculator_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSustainableShop, modifier = Modifier.testTag("yield_calculator_shop_btn")) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Shop Bio-Inputs", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // 1. CLEAR HARVEST YIELD SUMMARY CARD (HIGHLIGHT RESULTS)
            // =========================================================================
            item {
                HarvestYieldSummaryCard(
                    estimate = yieldEstimate,
                    onOpenSustainableShop = onOpenSustainableShop,
                    onListHarvestBatch = {
                        val pricePerKg = yieldEstimate.crop.benchmarkMarketRateRupeesPerQuintal / 100.0
                        onListHarvestBatch(
                            yieldEstimate.crop.name,
                            yieldEstimate.totalProductionKg,
                            pricePerKg
                        )
                    }
                )
            }

            // =========================================================================
            // 2. INPUT: LAND SIZE (ACRES)
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("calculator_land_size_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Agriculture, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "1. Land Size (भूमि का क्षेत्रफल)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", landSizeAcres)} Acres",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Stepper Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { landSizeAcres = (landSizeAcres - 0.5).coerceAtLeast(0.5) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = GreenPrimary)
                            }

                            Slider(
                                value = landSizeAcres.toFloat(),
                                onValueChange = { landSizeAcres = (it * 10).roundToInt() / 10.0 },
                                valueRange = 0.5f..20.0f,
                                steps = 38,
                                modifier = Modifier.weight(1f).testTag("calculator_acres_slider"),
                                colors = SliderDefaults.colors(thumbColor = GreenPrimary, activeTrackColor = GreenPrimary)
                            )

                            IconButton(
                                onClick = { landSizeAcres = (landSizeAcres + 0.5).coerceAtMost(50.0) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = GreenPrimary)
                            }
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1.0, 2.5, 5.0, 10.0, 15.0).forEach { presetAcres ->
                                val isSelected = (landSizeAcres - presetAcres).let { kotlin.math.abs(it) < 0.1 }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { landSizeAcres = presetAcres },
                                    label = { Text("${presetAcres.toInt()} Ac", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. INPUT: CROP SELECTION
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("calculator_crop_selection_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "2. Select Crop Type (फसल का प्रकार)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        // Category Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All Crops (${HarvestYieldCalculator.CROPS.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            )

                            CropCategoryType.values().forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        // Crop Horizontal Carousel
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(crops) { crop ->
                                val isSelected = crop.id == selectedCropId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { selectedCropId = crop.id }
                                        .testTag("crop_select_${crop.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = crop.name,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = crop.hindiName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Base: ${crop.baseYieldQuintalsPerAcre.toInt()} Qtl/Ac",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (isSelected) Color(0xFFFEF08A) else GreenPrimary
                                        )
                                        Text(
                                            text = "₹${crop.benchmarkMarketRateRupeesPerQuintal.toInt()}/Qtl",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 4. INPUT: SOIL QUALITY & FERTILITY PROFILE
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().testTag("calculator_soil_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "3. Soil Quality & Irrigation Profile (मृदा गुणवत्ता)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Soil Texture
                        Text(
                            text = "Soil Texture (मिट्टी का प्रकार):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SoilType.values().forEach { st ->
                                val isSelected = selectedSoilType == st
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSoilType = st },
                                    label = { Text(st.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GreenPrimary, selectedLabelColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Organic Carbon Grade (Fertility)
                        Text(
                            text = "Soil Organic Carbon & Fertility Grade (जैविक कार्बन):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SoilOrganicCarbonGrade.values().forEach { grade ->
                                val isSelected = selectedOrganicCarbon == grade
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GreenLight else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GreenPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedOrganicCarbon = grade }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (grade) {
                                                        SoilOrganicCarbonGrade.GRADE_A_HIGH -> GreenPrimary
                                                        SoilOrganicCarbonGrade.GRADE_B_MEDIUM -> Color(0xFF0284C7)
                                                        SoilOrganicCarbonGrade.GRADE_C_DEGRADED -> Color(0xFFD97706)
                                                    }
                                                )
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${grade.displayName} • ${grade.hindiName}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = grade.description,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Irrigation Type
                        Text(
                            text = "Water & Irrigation Access (सिंचाई व्यवस्था):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CalculatorIrrigationType.values().forEach { irri ->
                                val isSelected = selectedIrrigation == irri
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedIrrigation = irri }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = irri.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = when (irri) {
                                                CalculatorIrrigationType.DRIP_FERTIGATION -> "+15% Yield"
                                                CalculatorIrrigationType.SURFACE_CANAL -> "Standard"
                                                CalculatorIrrigationType.RAINFED_WELL -> "-25% Stress"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color(0xFF0369A1) else MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

/**
 * Clear Summary Card displaying potential harvest yields, per-acre productivity,
 * economic valuation, and logistics requirements.
 */
@Composable
fun HarvestYieldSummaryCard(
    estimate: HarvestYieldEstimate,
    onOpenSustainableShop: () -> Unit = {},
    onListHarvestBatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("harvest_yield_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Crop Badge & Land Area Pill
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
                            text = estimate.crop.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GreenLight
                        ) {
                            Text(
                                text = estimate.crop.category.displayName.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${estimate.crop.hindiName} • ${estimate.landSizeAcres} Acres • ${estimate.soilType.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F766E).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "ESTIMATED HARVEST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color(0xFF0F766E)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // =========================================================================
            // 4-GRID HIGHLIGHT METRIC MATRIX
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF065F46),
                                Color(0xFF047857),
                                Color(0xFF0D9488)
                            )
                        )
                    )
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Total Production
                Column {
                    Text(
                        text = "TOTAL ESTIMATED YIELD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBBF7D0),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${estimate.totalProductionTonnes} MT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${estimate.totalProductionQuintals.toInt()} Qtl (${estimate.totalProductionKg.toInt()} Kg)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFDCFCE7)
                    )
                }

                // Metric 2: Per Acre Productivity & Value
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PRODUCTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBBF7D0),
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = "${estimate.effectiveYieldPerAcreQuintals} Q/Ac",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A)
                    )
                    Text(
                        text = "Net: ₹${estimate.netFarmerProfitRupees.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            // Financial Breakdown Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gross Market Revenue
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Gross Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "₹${estimate.grossMarketValueRupees.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                        Text(
                            text = "@ ₹${estimate.crop.benchmarkMarketRateRupeesPerQuintal.toInt()}/Qtl",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Input Cost
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Cultivation Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "₹${estimate.totalCultivationCostRupees.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Seed, Drip, Labor",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Net Profit & ROI
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                        Text(
                            text = "₹${estimate.netFarmerProfitRupees.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        )
                        Text(
                            text = "+${estimate.roiPercent}% ROI",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )
                    }
                }
            }

            // 3-Point Yield Sensitivity Range (Pessimistic - Expected - Optimistic)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Yield Sensitivity Range (संभावित उपज सीमा):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Adverse Season", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFFD97706))
                            Text("${estimate.yieldRangeMinQuintals.toInt()} Qtl", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Realistic Expected", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = GreenPrimary)
                            Text("${estimate.totalProductionQuintals.toInt()} Qtl", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Peak Precision Care", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF0284C7))
                            Text("${estimate.yieldRangeMaxQuintals.toInt()} Qtl", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0284C7)))
                        }
                    }

                    LinearProgressIndicator(
                        progress = { 0.65f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GreenPrimary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            // Soil Quality Impact Summary Note
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0FDF4),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = estimate.soilQualityImpactSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF065F46)
                    )
                }
            }

            // Expandable Logistics & Farm Requirements
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Logistics & Seed Requirements" else "View Logistics, Seed & Package Demands",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = GreenPrimary
                )
            }

            if (isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌱 Certified Seed Needed:", style = MaterialTheme.typography.bodySmall)
                        Text("${estimate.seedQuantityNeededKg} Kg", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("📦 Bags/Crates Required (${estimate.crop.packagingUnitName}):", style = MaterialTheme.typography.bodySmall)
                        Text("${estimate.totalPackagesNeeded} Units", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌿 Atmospheric CO2 Biomass Fixed:", style = MaterialTheme.typography.bodySmall)
                        Text("${estimate.co2SequestrationKg.toInt()} Kg CO2e", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("⏱️ Crop Maturity Days:", style = MaterialTheme.typography.bodySmall)
                        Text("${estimate.crop.maturityDays} Days", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onOpenSustainableShop,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buy Seeds in Shop", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onListHarvestBatch,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("calculator_list_batch_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("List Advance Batch", fontSize = 12.sp)
                }
            }
        }
    }
}
