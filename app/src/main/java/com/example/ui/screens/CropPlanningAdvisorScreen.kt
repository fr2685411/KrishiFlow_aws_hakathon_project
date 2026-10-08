package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.advisory.AiAdvisoryResult
import com.example.data.advisory.AiCropRecommendation
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
import com.example.data.advisory.CropAdvisoryEngine
import com.example.data.model.AgroSeason
import com.example.data.model.CropRecommendation
import com.example.data.model.CropRiskLevel
import com.example.data.model.IrrigationSource
import com.example.data.model.NaturalOrderCategory
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CropPlanningAdvisorScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onNavigateBack: () -> Unit,
    onOpenSustainableShop: () -> Unit = {},
    onOpenYieldCalculator: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    // Affecting Parameters State
    var selectedSoil by rememberSaveable { mutableStateOf(SoilType.BLACK_CLAY) }
    var selectedSeason by rememberSaveable { mutableStateOf(AgroSeason.RABI) }
    var selectedIrrigation by rememberSaveable { mutableStateOf(IrrigationSource.DRIP_MICRO) }
    var selectedPreviousCrop by rememberSaveable { mutableStateOf(PreviousCropType.HEAVY_FEEDER_GRAIN) }
    var farmAcres by rememberSaveable { mutableDoubleStateOf(3.5) }
    var isParameterCardExpanded by remember { mutableStateOf(true) }
    var selectedAdvisoryTab by rememberSaveable { mutableStateOf(0) } // 0 = Gemini AI, 1 = Classical Rules

    val aiAdvisoryResult by viewModel.aiAdvisoryResult.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiAdvisoryLoading.collectAsStateWithLifecycle()
    val weatherData by viewModel.weatherData.collectAsStateWithLifecycle()

    // Automatic initial analysis with Gemini AI
    LaunchedEffect(Unit) {
        if (aiAdvisoryResult == null) {
            viewModel.runGeminiCropAnalysis(
                soilType = selectedSoil,
                season = selectedSeason,
                irrigation = selectedIrrigation,
                previousCrop = selectedPreviousCrop,
                farmAcres = farmAcres
            )
        }
    }

    // Live calculation of recommendations
    val recommendations = remember(selectedSoil, selectedSeason, selectedIrrigation, selectedPreviousCrop, farmAcres) {
        CropAdvisoryEngine.evaluateCropPlan(
            CropAdvisoryEngine.AdvisoryQuery(
                soilType = selectedSoil,
                season = selectedSeason,
                irrigation = selectedIrrigation,
                previousCrop = selectedPreviousCrop,
                farmAcres = farmAcres
            )
        )
    }

    val topRecommendation = recommendations.firstOrNull()

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
                                text = "Gemini Crop Advisory",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "AI 3.5",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF7C3AED),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Soil, Location & Climate Land Intelligence",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("crop_advisor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenYieldCalculator,
                        modifier = Modifier.testTag("crop_advisor_open_calculator_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Harvest Yield Calculator",
                            tint = GoldPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenSustainableShop,
                        modifier = Modifier.testTag("crop_advisor_open_shop_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Buy Sustainable Inputs in Shop",
                            tint = GreenPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            selectedSoil = SoilType.BLACK_CLAY
                            selectedSeason = AgroSeason.RABI
                            selectedIrrigation = IrrigationSource.DRIP_MICRO
                            selectedPreviousCrop = PreviousCropType.HEAVY_FEEDER_GRAIN
                            farmAcres = 3.5
                        },
                        modifier = Modifier.testTag("crop_advisor_reset_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset parameters",
                            tint = GreenPrimary
                        )
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
            // LIVE AGRO-CLIMATE & GPS TELEMETRY CONTEXT BANNER
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = weatherData.locationName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "${weatherData.currentTemp.toInt()}°C • ${weatherData.rainProbabilityToday}% Rain",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = GreenPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "AI analyzes live GPS coordinates, ambient vapor pressure, and recent rainfall to recommend precision crops for your parcel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // =========================================================================
            // 1. AFFECTING PARAMETERS CARD (Soil, Season, Water, Rotation, Acres)
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header + Collapse Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isParameterCardExpanded = !isParameterCardExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = CircleShape, color = GreenLight, modifier = Modifier.size(32.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Landscape,
                                            contentDescription = null,
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Farm Conditions & Parameters",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "खेत के कारक (मिट्टी, ऋतु, जल, पूर्व फसल)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isParameterCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle parameter controls",
                                tint = GreenPrimary
                            )
                        }

                        if (isParameterCardExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // PARAMETER 1: Soil Type
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "1. Soil Texture & Type (मिट्टी का प्रकार)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SoilType.values().forEach { soil ->
                                        val isSelected = selectedSoil == soil
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedSoil = soil },
                                            label = { Text(soil.displayName, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GreenPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = selectedSoil.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // PARAMETER 2: Agro Season
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "2. Sowing Season (बुवाई का मौसम)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AgroSeason.values().forEach { season ->
                                        val isSelected = selectedSeason == season
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedSeason = season },
                                            label = {
                                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                    Text(season.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text(season.months, fontSize = 9.sp)
                                                }
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GreenPrimary,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            // PARAMETER 3: Water & Irrigation Source
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "3. Irrigation Source (सिंचाई की सुविधा)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IrrigationSource.values().forEach { irrigation ->
                                        val isSelected = selectedIrrigation == irrigation
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedIrrigation = irrigation },
                                            label = { Text(irrigation.displayName, fontSize = 12.sp) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.WaterDrop,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF0284C7),
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            // PARAMETER 4: Previous Crop (CRITICAL FOR PRESERVING NATURAL ORDER)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "4. Previously Harvested Crop (पिछली फसल - प्रकृति चक्र)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PreviousCropType.values().forEach { prevCrop ->
                                        val isSelected = selectedPreviousCrop == prevCrop
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedPreviousCrop = prevCrop },
                                            label = { Text(prevCrop.displayName, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = when (prevCrop.naturalOrderCategory) {
                                                    NaturalOrderCategory.REQUIRES_RESTITUTION -> Color(0xFFD97706)
                                                    NaturalOrderCategory.HIGH_PEST_CARRYOVER -> Color(0xFFDC2626)
                                                    NaturalOrderCategory.NITROGEN_ENRICHED -> GreenPrimary
                                                    NaturalOrderCategory.BIOFUMIGATED -> Color(0xFF0284C7)
                                                    NaturalOrderCategory.RESTORED_PRISTINE -> GreenPrimary
                                                },
                                                selectedLabelColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Soil Status: ${selectedPreviousCrop.residualEffect}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            // PARAMETER 5: Farm Land Area (Acres)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "5. Land Cultivation Area",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GreenLight
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.getDefault(), "%.1f", farmAcres)} Acres",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = GreenPrimary
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Slider(
                                    value = farmAcres.toFloat(),
                                    onValueChange = { farmAcres = (it * 2).roundToInt() / 2.0 },
                                    valueRange = 0.5f..15.0f,
                                    steps = 28,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GreenPrimary,
                                        activeTrackColor = GreenPrimary
                                    ),
                                    modifier = Modifier.testTag("farm_acres_slider")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(1.0, 2.5, 4.0, 7.5, 10.0).forEach { acreVal ->
                                        OutlinedButton(
                                            onClick = { farmAcres = acreVal },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("${acreVal.toInt()} Ac", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // GEMINI AI ANALYSIS TRIGGER BUTTON
            // =========================================================================
            item {
                Button(
                    onClick = {
                        viewModel.runGeminiCropAnalysis(
                            soilType = selectedSoil,
                            season = selectedSeason,
                            irrigation = selectedIrrigation,
                            previousCrop = selectedPreviousCrop,
                            farmAcres = farmAcres
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_gemini_crop_analysis_btn")
                ) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini AI Analyzing Soil & Climate...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyze Land with Gemini AI (एआई फसल विश्लेषण)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // =========================================================================
            // 2. NATURAL ORDER & AGRO-ECOLOGY DIAGNOSTIC BANNER
            // =========================================================================
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Park,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Natural Order Directive (प्रकृति चक्र संतुलन)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                        }

                        val directiveMessage = when (selectedPreviousCrop.naturalOrderCategory) {
                            NaturalOrderCategory.REQUIRES_RESTITUTION ->
                                "Following cereals/maize, the soil's nitrogen bank is depleted. The Natural Order prioritizes nodule-forming Legumes (Chickpea, Soybean, Moong) to biologically regenerate soil fertility without chemical urea burns."
                            NaturalOrderCategory.HIGH_PEST_CARRYOVER ->
                                "High disease pressure from previous solanaceous crops. Break fungal wilt and root-knot nematode cycles by avoiding consecutive nightshades. Prioritize biofumigant mustard or restorative pulse intercrops."
                            NaturalOrderCategory.NITROGEN_ENRICHED ->
                                "Preceding pulse crop left fertile organic nitrogen nodules. Ideal conditions for grains or high-value vegetables that can leverage this free biological nutrient boost."
                            NaturalOrderCategory.BIOFUMIGATED ->
                                "Soil root zone is biologically sanitized from previous mustard glucosinolates. High suitability for cash horticulture crops."
                            NaturalOrderCategory.RESTORED_PRISTINE ->
                                "Living soil carbon is at peak vitality following green manuring. Soil biodiversity is primed for maximum yields."
                        }

                        Text(
                            text = directiveMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // =========================================================================
            // GEMINI AI VERDICT CARD
            // =========================================================================
            aiAdvisoryResult?.let { aiResult ->
                item {
                    GeminiAiVerdictCard(aiResult = aiResult)
                }
            }

            // =========================================================================
            // ADVISORY MODE TABS (Gemini AI Recommendations vs Classical ICAR Rules)
            // =========================================================================
            item {
                TabRow(
                    selectedTabIndex = selectedAdvisoryTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = GreenPrimary
                ) {
                    Tab(
                        selected = selectedAdvisoryTab == 0,
                        onClick = { selectedAdvisoryTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF7C3AED))
                                Text("Gemini AI (${aiAdvisoryResult?.recommendations?.size ?: 3})", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedAdvisoryTab == 1,
                        onClick = { selectedAdvisoryTab = 1 },
                        text = {
                            Text("Classical Rules (${recommendations.size})", fontWeight = FontWeight.Bold)
                        }
                    )
                }
            }

            // =========================================================================
            // 3. RANKED CROP RECOMMENDATIONS LIST
            // =========================================================================
            if (selectedAdvisoryTab == 0 && aiAdvisoryResult != null) {
                items(aiAdvisoryResult!!.recommendations) { aiCrop ->
                    GeminiAiCropCard(
                        crop = aiCrop,
                        farmAcres = farmAcres,
                        onOpenSustainableShop = onOpenSustainableShop,
                        onAdoptPlan = {
                            viewModel.createSampleProcurementOrder(
                                cropName = aiCrop.cropName,
                                qtyKg = aiCrop.expectedYieldQuintalsPerAcre * farmAcres * 100.0
                            )
                        }
                    )
                }
            } else {
                items(recommendations) { crop ->
                    CropPlanCard(
                        crop = crop,
                        farmAcres = farmAcres,
                        onOpenSustainableShop = onOpenSustainableShop,
                        onAdoptPlan = {
                            viewModel.createSampleProcurementOrder(
                                cropName = crop.cropName,
                                qtyKg = crop.totalEstimatedProductionQuintals * 100.0
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual Crop Advisory & Production Forecast Card with
 * Natural Order Preservation Solutions and Risk Radar.
 */
@Composable
fun CropPlanCard(
    crop: CropRecommendation,
    farmAcres: Double,
    onOpenSustainableShop: () -> Unit = {},
    onAdoptPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand_chevron"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("crop_card_${crop.cropName.replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Crop Name, Hindi Name, Eco-Harmony Score
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
                            text = crop.cropName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = crop.hindiName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Variety: ${crop.suitableVariety} • ${crop.maturityDays} Days to Maturity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Natural Order & Eco-Harmony Score Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        crop.naturalHarmonyScore >= 80 -> Color(0xFFDCFCE7)
                        crop.naturalHarmonyScore >= 60 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            crop.naturalHarmonyScore >= 80 -> GreenPrimary.copy(alpha = 0.5f)
                            crop.naturalHarmonyScore >= 60 -> GoldPrimary.copy(alpha = 0.5f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = when {
                                crop.naturalHarmonyScore >= 80 -> GreenPrimary
                                crop.naturalHarmonyScore >= 60 -> GoldPrimary
                                else -> Color(0xFFDC2626)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${crop.naturalHarmonyScore}% Harmony",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    crop.naturalHarmonyScore >= 80 -> GreenPrimary
                                    crop.naturalHarmonyScore >= 60 -> GoldPrimary
                                    else -> Color(0xFFDC2626)
                                }
                            )
                        )
                    }
                }
            }

            // Natural Order Verdict
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = crop.naturalHarmonyVerdict,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // =========================================================================
            // ESTIMATED PRODUCTION MATRIX (Yield/Acre, Total Tonnes, Economics)
            // =========================================================================
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
                        text = "Estimated Yield",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${crop.expectedYieldPerAcreQuintals} Qtl / Ac",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total: ${crop.totalEstimatedProductionTonnes} Tonnes",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Benchmark APMC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${crop.expectedMarketRatePerQuintal.toInt()} / Qtl",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Input: ₹${(crop.estimatedInputCostRupees / 1000).toInt()}k",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Estimated Profit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%,.0f", crop.estimatedNetProfitRupees)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GreenPrimary
                    )
                    Text(
                        text = "${farmAcres} Acres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // =========================================================================
            // RISK RADAR SUMMARY BAR
            // =========================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(crop.overallRiskLevel.colorHex),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Risk Profile: ${crop.overallRiskLevel.label}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(crop.overallRiskLevel.colorHex)
                            )
                        )
                    }

                    Text(
                        text = "Weather: ${crop.weatherRiskPercent}% • Pest: ${crop.pestRiskPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Weather and Pest visual progress bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Drought / Rain", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${crop.weatherRiskPercent}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { crop.weatherRiskPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (crop.weatherRiskPercent > 40) Color(0xFFEF4444) else GreenPrimary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pest / Disease", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${crop.pestRiskPercent}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { crop.pestRiskPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (crop.pestRiskPercent > 40) Color(0xFFEF4444) else EcoTeal,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // =========================================================================
            // EXPANDABLE REGENERATIVE SOLUTIONS TO PRESERVE NATURAL ORDER
            // =========================================================================
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Regenerative Solutions & Natural Order Protocols",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )

                    // 1. Biological Soil Remedy
                    SolutionRow(
                        icon = Icons.Default.Science,
                        title = "Biological Soil Inoculation",
                        content = crop.naturalOrderPreservationPlan.biologicalSoilRemedy,
                        tint = GreenPrimary
                    )

                    // 2. Companion Planting & Trap Crops
                    SolutionRow(
                        icon = Icons.Default.Park,
                        title = "Companion Trap Crop (कीट फंदा)",
                        content = crop.naturalOrderPreservationPlan.companionPlantingTrapCrop,
                        tint = GoldPrimary
                    )

                    // 3. Polyculture / Intercropping Recipe
                    SolutionRow(
                        icon = Icons.Default.Agriculture,
                        title = "Polyculture Intercrop Ratio (अंतर-फसल)",
                        content = crop.naturalOrderPreservationPlan.polycultureIntercropRecipe,
                        tint = EcoTeal
                    )

                    // 4. Water Conservation Method
                    SolutionRow(
                        icon = Icons.Default.WaterDrop,
                        title = "Moisture & Mulching Conservation",
                        content = crop.naturalOrderPreservationPlan.waterConservationMethod,
                        tint = Color(0xFF0284C7)
                    )

                    // 5. Natural Pest Mitigation
                    SolutionRow(
                        icon = Icons.Default.BugReport,
                        title = "Natural Non-Chemical Pest Control",
                        content = crop.naturalOrderPreservationPlan.naturalPestMitigation,
                        tint = Color(0xFFD97706)
                    )
                }
            }

            // Bottom Actions: Toggle Solutions Details + Adopt Crop Plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide Solutions" else "View Eco Solutions",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(rotationAngle)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenSustainableShop,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buy Inputs", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAdoptPlan,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("adopt_crop_plan_${crop.cropName.replace(" ", "_")}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adopt Plan", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SolutionRow(
    icon: ImageVector,
    title: String,
    content: String,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Gemini AI Holistic Land Verdict Card.
 */
@Composable
fun GeminiAiVerdictCard(
    aiResult: AiAdvisoryResult,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC4B5FD)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Gemini Agro-Verdict",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5B21B6)
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF7C3AED).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (aiResult.isLiveAiGenerated) "✨ gemini-3.5-flash" else "ICAR Bio-Model",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF6D28D9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = aiResult.summaryVerdict,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4C1D95)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    aiResult.locationAnalyzed,
                    aiResult.soilAnalyzed,
                    aiResult.seasonAnalyzed
                ).forEach { chipText ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFDDD6FE))
                    ) {
                        Text(
                            text = chipText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF6D28D9)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gemini AI Recommended Crop Card with Soil/Climate Match, Risk Assessment, and Sustainable Practices.
 */
@Composable
fun GeminiAiCropCard(
    crop: AiCropRecommendation,
    farmAcres: Double,
    onOpenSustainableShop: () -> Unit = {},
    onAdoptPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_crop_card_${crop.cropName.replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Match Score & Titles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crop.cropName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (crop.hindiName.isNotBlank()) {
                        Text(
                            text = crop.hindiName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GreenPrimary
                        )
                    }
                    Text(
                        text = "Maturity: ${crop.maturityDays} Days • Farm Size: ${String.format(Locale.getDefault(), "%.1f", farmAcres)} Acres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenPrimary,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${crop.agroClimaticMatchScore}% Match",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // AI Reasoning Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(14.dp))
                        Text(
                            text = "AI Agronomist Scientific Match:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                        )
                    }
                    Text(
                        text = crop.aiReasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Financial Snapshot
            val totalProductionQuintals = crop.expectedYieldQuintalsPerAcre * farmAcres
            val totalNetProfit = crop.estimatedNetProfitPerAcreRupees * farmAcres

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFECFDF5),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6EE7B7)),
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
                            text = "Expected Production",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", totalProductionQuintals)} Qtl",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                        )
                        Text(
                            text = "(${crop.expectedYieldQuintalsPerAcre} Qtl/Acre)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Est. Net Profit",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "₹${totalNetProfit.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                        Text(
                            text = "₹${crop.estimatedNetProfitPerAcreRupees.toInt()}/Acre",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                }
            }

            // Expandable Risk Assessment & Sustainable Practices
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Risk & Sustainable Practices" else "View Risk Assessment & Sustainable Practices",
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

                    // Multi-Factor Risk Assessment Radar
                    Text(
                        text = "⚠️ Multi-Factor Risk Assessment:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("☀️ Drought/Climate:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text(crop.climateAndDroughtRisk, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🐛 Pest/Disease:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text(crop.pestAndDiseaseRisk, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📈 Market Glut:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Text(crop.marketGlutRisk, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Sustainable & Regenerative Practices
                    Text(
                        text = "🌱 Recommended Sustainable Practices:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                    )
                    crop.sustainablePractices.forEach { practice ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                            Text(practice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    if (crop.naturalOrderPreservation.isNotBlank()) {
                        Text(
                            text = "🔄 Soil Restoration: ${crop.naturalOrderPreservation}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                        )
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
                    Text("Shop Bio-Inputs", fontSize = 11.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAdoptPlan,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adopt Plan", fontSize = 12.sp)
                }
            }
        }
    }
}
