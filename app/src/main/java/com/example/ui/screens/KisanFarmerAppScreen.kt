package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.data.notification.CropPriceAlertNotificationHelper
import com.example.ui.components.CropPriceAlertThresholdCard
import com.example.ui.components.FarmerProducePostCard
import com.example.ui.components.FarmWeatherDashboardCard
import com.example.ui.components.FoodWasteAnalyticsModule
import com.example.ui.components.GeminiHarvestProfitMaximizerCard
import com.example.ui.components.HarvestInventoryShelfLifeDashboard
import com.example.ui.components.OfflineInventoryScannerSyncCard
import com.example.ui.components.VisualInventorySummaryDashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

@Composable
fun KisanFarmerAppScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onListHarvestClick: () -> Unit,
    onOpenProduceDashboard: () -> Unit = {},
    onOpenCropAdvisor: () -> Unit = {},
    onOpenSustainableShop: () -> Unit = {},
    onOpenYieldCalculator: () -> Unit = {},
    onOpenHarvestHistoryLogs: () -> Unit = {},
    onOpenGeminiAssistant: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMapDialog by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(CropPriceAlertNotificationHelper.hasNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Kisan Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GreenPrimary),
                modifier = Modifier.fillMaxWidth()
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "KrishiFlow Kisan",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "किसान साथी • डायरेक्ट मंडी और मार्ट",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFBBF7D0)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF08A)
                        ) {
                            Text(
                                text = "Zero Brokerage",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF713F12)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "सीधे व्यापारियों और मार्ट्स को अपनी फसल बेचें। कोई दलाल नहीं, कोई काटा नहीं, सीधे बैंक खाते में भुगतान।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFDCFCE7)
                    )

                    // Big Action Button: List New Harvest
                    Button(
                        onClick = onListHarvestClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kisan_list_harvest_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "अपनी फसल जोड़ें / List My Harvest",
                            color = GreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // KrishiMitra Gemini AI Voice & Text Assistant Interactive Feature Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F3FF) // Soft Lavender / Violet
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC4B5FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kisan_gemini_ai_assistant_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF7C3AED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "KrishiMitra Gemini AI",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4C1D95)
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDDD6FE)
                                    ) {
                                        Text(
                                            text = "Voice + Text",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5B21B6)
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Your App-Aware AI Assistant • बोलकर या लिखकर पूछें",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF6D28D9)
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenGeminiAssistant,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                                .testTag("kisan_quick_mic_gemini_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speak to AI",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Gemini is custom-primed with KrishiFlow's live stock, weather, and mandi data. You can input your API key, ask agronomic queries, or tell it to open the shop, calculator, or dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4B5563),
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenGeminiAssistant,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kisan_open_gemini_chat_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ask KrishiMitra AI",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenGeminiAssistant,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C3AED)),
                            modifier = Modifier.testTag("kisan_configure_gemini_key_btn")
                        ) {
                            Text(
                                text = "🔑 API Key",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Local Market Price Alert Notification Thresholds for Farmer Crops
        item {
            CropPriceAlertThresholdCard(
                thresholds = uiState.priceThresholds,
                registeredBatches = uiState.batches,
                mandiRates = uiState.mandiRates,
                hasNotificationPermission = hasNotificationPermission,
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onSaveThreshold = { threshold -> viewModel.savePriceThreshold(threshold) },
                onDeleteThreshold = { id -> viewModel.deletePriceThreshold(id) },
                onToggleThreshold = { id, isEnabled -> viewModel.togglePriceThreshold(id, isEnabled) },
                onSendTestAlert = { crop, target, current, mandi ->
                    viewModel.triggerTestPriceAlertNotification(crop, target, current, mandi)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Real-Time Agro-Weather & Irrigation/Harvest Advisory Dashboard
        item {
            FarmWeatherDashboardCard(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Gemini AI Harvest & Weather Profit Maximizer: Suggests Best Time to Sell or Store Specific Crops
        item {
            val weatherData by viewModel.weatherData.collectAsState()
            GeminiHarvestProfitMaximizerCard(
                viewModel = viewModel,
                batches = uiState.batches,
                weatherData = weatherData,
                onNavigateToSurplusScreen = { onOpenProduceDashboard() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Visual Inventory Summary Dashboard: Total Available Stock, Category Breakdown & Sell-By Date Progress Tracker
        item {
            VisualInventorySummaryDashboard(
                batches = uiState.batches,
                onNavigateToSurplusScreen = { onOpenProduceDashboard() },
                modifier = Modifier.fillMaxWidth().testTag("kisan_visual_inventory_summary_dashboard")
            )
        }

        // Real-Time Harvest Inventory & Shelf-Life Spoilage Alert Dashboard
        item {
            HarvestInventoryShelfLifeDashboard(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                onNavigateToSurplusScreen = { onOpenProduceDashboard() },
                onOpenHarvestHistoryLogs = onOpenHarvestHistoryLogs
            )
        }

        // Offline Room Database Cache & Barcode/QR Inventory Scanner for Firestore Auto-Sync
        item {
            OfflineInventoryScannerSyncCard(
                queuedItems = uiState.queuedInventory,
                pendingSyncCount = uiState.pendingOfflineSyncCount,
                workManagerSyncState = uiState.workManagerSyncState,
                onScanOfflineItem = { batchCode, cropName, category, variety, qualityGrade, quantityKg, farmerName, farmLocation, askingPricePerKg, storageCondition, freshnessDays ->
                    viewModel.scanAndQueueOfflineInventory(
                        batchCode = batchCode,
                        cropName = cropName,
                        category = category,
                        variety = variety,
                        qualityGrade = qualityGrade,
                        quantityKg = quantityKg,
                        farmerName = farmerName,
                        farmLocation = farmLocation,
                        askingPricePerKg = askingPricePerKg,
                        storageCondition = storageCondition,
                        freshnessDays = freshnessDays
                    )
                },
                onTriggerSync = {
                    viewModel.triggerOfflineQueueManualSync()
                },
                onDeleteItem = { queueId ->
                    viewModel.deleteOfflineQueueItem(queueId)
                },
                onClearSynced = {
                    viewModel.clearCompletedOfflineSyncedItems()
                },
                modifier = Modifier.fillMaxWidth().testTag("kisan_offline_scanner_sync_card")
            )
        }

        // Potential Food Waste & Surplus Loss Forecaster Module
        item {
            FoodWasteAnalyticsModule(
                harvestLogs = uiState.harvestLogs,
                activeBatches = uiState.batches,
                onNavigateToSurplusScreen = { onOpenProduceDashboard() },
                modifier = Modifier.fillMaxWidth().testTag("kisan_food_waste_analytics_module")
            )
        }

        // Quick Navigation to Maps and Feature Clarification
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showMapDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kisan_view_maps_btn")
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("नज़दीकी मंडी व रूट (Maps)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showGuideDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kisan_how_it_works_btn")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("मदद व जानकारी (Guide)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Crop Planning & Natural Order Advisory Feature Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCropAdvisor() }
                    .testTag("kisan_crop_advisor_banner")
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GreenLight,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "फसल नियोजन एवं प्रकृति चक्र",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GreenPrimary
                                ) {
                                    Text(
                                        text = "ADVISOR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Crop Advisory, Yield Estimation & Risk Mitigation",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "मिट्टी, ऋतु और फसल चक्र अनुसार उत्पादन अनुमान व जैविक समाधान।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Crop Advisor",
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Sustainable Input Shop Feature Card (Seeds, Bio-Pesticides, Organic Nutrients)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSustainableShop() }
                    .testTag("kisan_sustainable_shop_banner")
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "जैविक कृषि दुकान (Sustainable Shop)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF0284C7)
                                ) {
                                    Text(
                                        text = "BIO-INPUTS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Buy Certified Seeds, Bio-Pesticides, Trichoderma & Vermicompost",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "मधुमक्खी व मित्रकीट सुरक्षित जैविक इनपुट्स सीधे खेत तक डिलीवरी।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Sustainable Shop",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Harvest Yield Calculator Feature Card (Precision Harvest & Revenue Estimation)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenYieldCalculator() }
                    .testTag("kisan_open_yield_calculator_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "फसल उपज कैलकुलेटर",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoldPrimary
                                ) {
                                    Text(
                                        text = "CALCULATOR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Harvest Yield, Market Revenue & Input Planning",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "जमीन, फसल व मिट्टी अनुसार अनुमानित उपज, मंडी आय व बीज आवश्यकता का सटीक हिसाब।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Yield Calculator",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section: My Posted Agricultural Produce (Saved to Cloud Firestore)
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
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "मेरी लिस्टेड उपज / My Produce (Firestore)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { onOpenProduceDashboard() }
                            .testTag("kisan_open_produce_dashboard_chip")
                    ) {
                        Text(
                            text = "डैशबोर्ड / All Feed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenLight,
                        modifier = Modifier
                            .clickable { onListHarvestClick() }
                            .testTag("kisan_add_produce_quick_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "+ Post",
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

        if (uiState.producePosts.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "अभी तक कोई फसल पोस्ट नहीं की गई",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "अपनी उपज (मात्रा व मूल्य) पोस्ट करें। यह सीधे Cloud Firestore में सुरक्षित होगी और व्यापारियों व मार्ट्स को तुरंत दिखेगी।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = onListHarvestClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("empty_post_produce_btn")
                        ) {
                            Text("Post Produce Now")
                        }
                    }
                }
            }
        } else {
            items(uiState.producePosts, key = { "post_${it.postId}" }) { post ->
                FarmerProducePostCard(post = post)
            }
        }

        // Live Market Procurement Offers (What buyers want right now!)
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
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "आज का बाज़ार भाव और मांग",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Live Rates",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GreenPrimary
                )
            }
        }

        // Simple high-contrast commodity cards for farmers
        items(SampleCommodityDemands.take(3), key = { "kisan_${it.id}" }) { demand ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = demand.commodityName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GreenLight
                        ) {
                            Text(
                                text = "₹${demand.guaranteedHubPricePerKg.toInt()}/kg सीधा भाव",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "मंडी से +${demand.farmerGainPercent}% ज़्यादा मुनाफा • दलाल कमीशन: ₹0",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = GreenAccent
                    )

                    Text(
                        text = "खरीदार: ${demand.activeBuyerSummary}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "खेत से पिकअप: ${demand.deliveryWindow}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${demand.remainingQuotaKg.toInt()} kg बाकी",
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

        // Direct UPI Payment & Passbook Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "भुगतान पासबुक / Payment Passbook",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                            Text(
                                text = "Instant UPI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "डिलीवरी होते ही बिना किसी कटौती के पूरा पैसा सीधे आपके बैंक खाते में जमा किया जाता है।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("कुल प्राप्त भुगतान", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "₹${uiState.totalFarmerPayoutRupees.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = GreenLight) {
                            Text(
                                text = "100% सफल",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
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
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
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

    if (showGuideDialog) {
        EcosystemClarificationDialog(onDismiss = { showGuideDialog = false })
    }
}
