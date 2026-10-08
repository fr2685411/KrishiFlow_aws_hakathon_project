package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.model.OrderStatus
import com.example.ui.components.CloudArchitectureGuideDialog
import com.example.ui.components.KrishiGeminiAssistantDialog
import com.example.ui.components.KrishiNavigationRail
import com.example.ui.components.KrishiSideDrawerContent
import com.example.ui.components.SecureAuthDialog
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.screens.AwsHackathonBuildItScreen
import com.example.ui.screens.CropPlanningAdvisorScreen
import com.example.ui.screens.HarvestHistoryLogScreen
import com.example.ui.screens.HarvestYieldCalculatorScreen
import com.example.ui.screens.HubOperationsScreen
import com.example.ui.screens.KisanFarmerAppScreen
import com.example.ui.screens.KrishiMitraScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.OrdersFulfillmentScreen
import com.example.ui.screens.ProduceDashboardScreen
import com.example.ui.screens.RegionalLogisticsMapScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SaathiDriverAppScreen
import com.example.ui.screens.SurplusPriorityScreen
import com.example.ui.screens.SustainableShopScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.screens.VyaparBuyerAppScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.components.FarmerProducePostDialog
import com.example.ui.screens.AddNewBatchDialog
import com.example.ui.screens.AppSettingsScreen
import com.example.ui.screens.CropPlanningAdvisorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.HarvestHistoryLogScreen
import com.example.ui.screens.HarvestYieldCalculatorScreen
import com.example.ui.screens.HubOperationsScreen
import com.example.ui.screens.KisanFarmerAppScreen
import com.example.ui.screens.KrishiMitraScreen
import com.example.ui.screens.MandiIntelligenceScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.OrdersFulfillmentScreen
import com.example.ui.screens.ProduceDashboardScreen
import com.example.ui.screens.RegionalLogisticsMapScreen
import com.example.ui.screens.SustainableShopScreen
import com.example.ui.screens.EcosystemClarificationDialog
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SaathiDriverAppScreen
import com.example.ui.screens.SurplusPriorityScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiViewModel

enum class AppPersonaMode(
    val title: String,
    val shortName: String,
    val subtitle: String,
    val icon: ImageVector,
    val themeColor: Color
) {
    KISAN_FARMER(
        title = "🌾 Kisan (Farmer)",
        shortName = "Kisan",
        subtitle = "Farmer View",
        icon = Icons.Default.Agriculture,
        themeColor = Color(0xFF15803D)
    ),
    VYAPAR_MANDI(
        title = "🏪 Vyapar (Buyer & Mart)",
        shortName = "Vyapar",
        subtitle = "Buyer View",
        icon = Icons.Default.Storefront,
        themeColor = Color(0xFFD97706)
    ),
    SAATHI_FLEET(
        title = "🚚 Saathi (Driver)",
        shortName = "Saathi",
        subtitle = "Driver View",
        icon = Icons.Default.LocalShipping,
        themeColor = Color(0xFF0D9488)
    ),
    KRISHI_MITRA(
        title = "🤝 Krishi Mitra (Field Lead)",
        shortName = "Mitra",
        subtitle = "Field Lead",
        icon = Icons.Default.SupportAgent,
        themeColor = Color(0xFF7C3AED)
    ),
    CENTRAL_OPS(
        title = "🌐 Central Ops Hub",
        shortName = "Ops Hub",
        subtitle = "Ops Hub",
        icon = Icons.Default.Eco,
        themeColor = Color(0xFF166534)
    )
}

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishiApp(
    viewModel: KrishiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasCompletedRoleSelection by rememberSaveable { mutableStateOf(true) }
    var activePersona by rememberSaveable { mutableStateOf(AppPersonaMode.CENTRAL_OPS) }
    var showThemeSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showPersonaSelectorDialog by remember { mutableStateOf(false) }
    var showProjectOverviewDialog by remember { mutableStateOf(false) }
    var showEcosystemClarificationDialog by remember { mutableStateOf(false) }
    var showKisanAddHarvestDialog by remember { mutableStateOf(false) }
    var showSecureAuthDialog by remember { mutableStateOf(false) }
    var showUserProfileScreen by rememberSaveable { mutableStateOf(false) }
    var showProduceDashboardScreen by rememberSaveable { mutableStateOf(false) }
    var showCropAdvisorScreen by rememberSaveable { mutableStateOf(false) }
    var showSustainableShopScreen by rememberSaveable { mutableStateOf(false) }
    var showYieldCalculatorScreen by rememberSaveable { mutableStateOf(false) }
    var showHarvestHistoryLogScreen by rememberSaveable { mutableStateOf(false) }
    var showGeminiAssistantDialog by rememberSaveable { mutableStateOf(false) }
    var showCloudArchitectureDialog by rememberSaveable { mutableStateOf(false) }
    var showAwsHackathonBuildItScreen by rememberSaveable { mutableStateOf(false) }
    var showAppSettingsScreen by rememberSaveable { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var showTopBarOverflowMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    // BackHandler so user gestures never close the app unexpectedly
    BackHandler(enabled = showAppSettingsScreen) {
        showAppSettingsScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && showAwsHackathonBuildItScreen) {
        showAwsHackathonBuildItScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showAwsHackathonBuildItScreen && showProduceDashboardScreen) {
        showProduceDashboardScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && showUserProfileScreen) {
        showUserProfileScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && showCropAdvisorScreen) {
        showCropAdvisorScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && !showCropAdvisorScreen && showSustainableShopScreen) {
        showSustainableShopScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && !showCropAdvisorScreen && !showSustainableShopScreen && showYieldCalculatorScreen) {
        showYieldCalculatorScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && !showCropAdvisorScreen && !showSustainableShopScreen && !showYieldCalculatorScreen && showHarvestHistoryLogScreen) {
        showHarvestHistoryLogScreen = false
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && !hasCompletedRoleSelection) {
        hasCompletedRoleSelection = true
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && hasCompletedRoleSelection && activePersona != AppPersonaMode.CENTRAL_OPS) {
        activePersona = AppPersonaMode.CENTRAL_OPS
    }

    BackHandler(enabled = !showAppSettingsScreen && !showProduceDashboardScreen && !showUserProfileScreen && hasCompletedRoleSelection && activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab != 0) {
        viewModel.setActiveTab(0)
    }

    if (showAppSettingsScreen) {
        SettingsScreen(
            onNavigateBack = { showAppSettingsScreen = false }
        )
    } else if (showAwsHackathonBuildItScreen) {
        AwsHackathonBuildItScreen(
            onNavigateBack = { showAwsHackathonBuildItScreen = false }
        )
    } else if (showProduceDashboardScreen) {
        ProduceDashboardScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = { showProduceDashboardScreen = false },
            onOpenHarvestHistoryLogs = { showHarvestHistoryLogScreen = true }
        )
    } else if (showUserProfileScreen) {
        UserProfileScreen(
            viewModel = viewModel,
            onNavigateBack = { showUserProfileScreen = false },
            onNavigateToSettings = {
                showUserProfileScreen = false
                showAppSettingsScreen = true
            }
        )
    } else if (showCropAdvisorScreen) {
        CropPlanningAdvisorScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = { showCropAdvisorScreen = false },
            onOpenSustainableShop = { showSustainableShopScreen = true },
            onOpenYieldCalculator = {
                showCropAdvisorScreen = false
                showYieldCalculatorScreen = true
            }
        )
    } else if (showSustainableShopScreen) {
        SustainableShopScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = { showSustainableShopScreen = false }
        )
    } else if (showYieldCalculatorScreen) {
        HarvestYieldCalculatorScreen(
            viewModel = viewModel,
            onNavigateBack = { showYieldCalculatorScreen = false },
            onOpenSustainableShop = {
                showYieldCalculatorScreen = false
                showSustainableShopScreen = true
            },
            onListHarvestBatch = { cropName, qtyKg, pricePerKg ->
                showYieldCalculatorScreen = false
                showKisanAddHarvestDialog = true
            }
        )
    } else if (showHarvestHistoryLogScreen) {
        HarvestHistoryLogScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateBack = { showHarvestHistoryLogScreen = false }
        )
    } else if (!hasCompletedRoleSelection) {
        RoleSelectionScreen(
            initialSelected = activePersona,
            onRoleConfirmed = { selected ->
                activePersona = selected
                hasCompletedRoleSelection = true
            }
        )
    } else {
        val centralOpsNavItems = listOf(
            NavItem(
                title = "Hub",
                icon = Icons.Default.Storefront,
                testTag = "nav_hub",
                badgeCount = uiState.openDemands.size
            ),
            NavItem(
                title = "Produce",
                icon = Icons.Default.Agriculture,
                testTag = "nav_produce_dashboard",
                badgeCount = uiState.producePosts.size
            ),
            NavItem(
                title = "Surplus",
                icon = Icons.Default.Eco,
                testTag = "nav_surplus",
                badgeCount = uiState.surplusBatches.size
            ),
            NavItem(
                title = "Market",
                icon = Icons.Default.ShoppingBag,
                testTag = "nav_market"
            ),
            NavItem(
                title = "Map",
                icon = Icons.Default.Map,
                testTag = "nav_map"
            ),
            NavItem(
                title = "Orders",
                icon = Icons.Default.LocalShipping,
                testTag = "nav_orders",
                badgeCount = uiState.orders.count { it.status == OrderStatus.OUT_FOR_DELIVERY }
            )
        )

        val vyaparNavItems = listOf(
            NavItem(
                title = "Wholesale",
                icon = Icons.Default.Storefront,
                testTag = "nav_vyapar_catalog"
            ),
            NavItem(
                title = "Deals",
                icon = Icons.Default.Bolt,
                testTag = "nav_vyapar_deals",
                badgeCount = uiState.surplusBatches.size
            ),
            NavItem(
                title = "Mandi",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                testTag = "nav_vyapar_mandi"
            ),
            NavItem(
                title = "Post RFQ",
                icon = Icons.Default.PostAdd,
                testTag = "nav_vyapar_rfq",
                badgeCount = uiState.openDemands.size
            ),
            NavItem(
                title = "Orders",
                icon = Icons.Default.LocalShipping,
                testTag = "nav_vyapar_orders",
                badgeCount = uiState.orders.count { it.status == OrderStatus.OUT_FOR_DELIVERY }
            )
        )

        val activeNavItems = if (activePersona == AppPersonaMode.VYAPAR_MANDI) vyaparNavItems else centralOpsNavItems

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val screenWidth = maxWidth
            val isExpandedScreen = screenWidth >= 720.dp

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        drawerTonalElevation = 6.dp,
                        modifier = Modifier.widthIn(max = 340.dp)
                    ) {
                        KrishiSideDrawerContent(
                            activePersona = activePersona,
                            onSelectPersona = { selected ->
                                activePersona = selected
                                viewModel.setActiveTab(0)
                            },
                            uiState = uiState,
                            onNavigateToProduceDashboard = { showProduceDashboardScreen = true },
                            onNavigateToCropAdvisor = { showCropAdvisorScreen = true },
                            onNavigateToYieldCalculator = { showYieldCalculatorScreen = true },
                            onNavigateToHarvestHistoryLogs = { showHarvestHistoryLogScreen = true },
                            onNavigateToSustainableShop = { showSustainableShopScreen = true },
                            onNavigateToUserProfile = { showUserProfileScreen = true },
                            onNavigateToSecureAuth = { showSecureAuthDialog = true },
                            onOpenProjectOverview = { showProjectOverviewDialog = true },
                            onOpenEcosystemClarification = { showEcosystemClarificationDialog = true },
                            onTriggerWorkManagerSync = { viewModel.triggerWorkManagerSync() },
                            onSelectTab = { tab -> viewModel.setActiveTab(tab) },
                            onOpenThemeSelector = { showThemeSelectionDialog = true },
                            onOpenGeminiAssistant = { showGeminiAssistantDialog = true },
                            onOpenCloudArchitecture = { showCloudArchitectureDialog = true },
                            onOpenAwsHackathonBuildIt = { showAwsHackathonBuildItScreen = true },
                            onNavigateToSettings = { showAppSettingsScreen = true },
                            onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                        )
                    }
                }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Adaptive Navigation Rail on wide screens (tablets, foldables unfolded, landscape)
                    if (isExpandedScreen) {
                        KrishiNavigationRail(
                            activePersona = activePersona,
                            activeTab = uiState.activeTab,
                            uiState = uiState,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onSelectTab = { tab -> viewModel.setActiveTab(tab) },
                            onNavigateToProduceDashboard = { showProduceDashboardScreen = true },
                            onNavigateToSustainableShop = { showSustainableShopScreen = true },
                            onNavigateToCropAdvisor = { showCropAdvisorScreen = true },
                            onNavigateToUserProfile = { showUserProfileScreen = true }
                        )
                    }

                    Scaffold(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(
                                        onClick = { coroutineScope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("open_side_drawer_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open Side Toolbar & Menu",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF166534)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Agriculture,
                                                contentDescription = "KrishiFlow Logo",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f, fill = false),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "KrishiFlow",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = if (screenWidth < 360.dp) 18.sp else 20.sp,
                                                    letterSpacing = (-0.2).sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (screenWidth >= 600.dp) {
                                                Text(
                                                    text = "Digital Agro-Procurement & Mandi Flow",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                },
                                actions = {
                                    // 0. Settings & BYOK API Key Configuration
                                    IconButton(
                                        onClick = { showAppSettingsScreen = true },
                                        modifier = Modifier.testTag("top_settings_byok_btn")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(GreenPrimary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Settings & API Keys",
                                                tint = GreenPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // 0. AWS Hackathon: Build It Lab Quick Launcher
                                    IconButton(
                                        onClick = { showAwsHackathonBuildItScreen = true },
                                        modifier = Modifier.testTag("top_aws_hackathon_build_it_btn")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = "AWS Hackathon: Build It Track",
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // 1. Gemini AI Assistant (Voice & Text Control)
                                    IconButton(
                                        onClick = { showGeminiAssistantDialog = true },
                                        modifier = Modifier.testTag("top_gemini_ai_assistant_btn")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Gemini AI Assistant & Voice Control",
                                                tint = Color(0xFF7C3AED),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // 1. Quick Theme Options Button
                                    IconButton(
                                        onClick = { showThemeSelectionDialog = true },
                                        modifier = Modifier.testTag("top_theme_options_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = "Theme Options (रंग थीम)",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // 2. Quick Shopping Cart with Counter Badge
                                    IconButton(
                                        onClick = { showSustainableShopScreen = true },
                                        modifier = Modifier.testTag("top_sustainable_shop_btn")
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                if (uiState.cartItems.isNotEmpty()) {
                                                    Badge(
                                                        containerColor = Color(0xFFDC2626),
                                                        contentColor = Color.White
                                                    ) {
                                                        Text("${uiState.cartItems.sumOf { it.quantity }}")
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = "Sustainable Input Shop",
                                                tint = GreenPrimary
                                            )
                                        }
                                    }

                                    // 3. More Options Overflow Menu
                                    Box {
                                        IconButton(
                                            onClick = { showTopBarOverflowMenu = true },
                                            modifier = Modifier.testTag("top_bar_overflow_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "More Options",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showTopBarOverflowMenu,
                                            onDismissRequest = { showTopBarOverflowMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Settings & API Keys (BYOK)") },
                                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showAppSettingsScreen = true
                                                },
                                                modifier = Modifier.testTag("overflow_settings_btn")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Theme Options (${appThemeMode.label})") },
                                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showThemeSelectionDialog = true
                                                },
                                                modifier = Modifier.testTag("overflow_theme_btn")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Switch Role: ${activePersona.shortName}") },
                                                leadingIcon = { Icon(activePersona.icon, contentDescription = null, tint = activePersona.themeColor) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showPersonaSelectorDialog = true
                                                },
                                                modifier = Modifier.testTag("overflow_switch_role_btn")
                                            )
                                            HorizontalDivider()
                                             DropdownMenuItem(
                                                text = { Text("Gemini AI Assistant & Voice") },
                                                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED)) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showGeminiAssistantDialog = true
                                                },
                                                modifier = Modifier.testTag("overflow_gemini_ai_btn")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Cloud Sync (WorkManager)") },
                                                leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    viewModel.triggerWorkManagerSync()
                                                },
                                                modifier = Modifier.testTag("firestore_cloud_sync_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Produce Dashboard") },
                                                leadingIcon = { Icon(Icons.Default.Agriculture, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showProduceDashboardScreen = true
                                                },
                                                modifier = Modifier.testTag("top_produce_dashboard_btn")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Crop Planning Advisor") },
                                                leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showCropAdvisorScreen = true
                                                },
                                                modifier = Modifier.testTag("crop_advisor_topbar_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Harvest Yield Calculator") },
                                                leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showYieldCalculatorScreen = true
                                                },
                                                modifier = Modifier.testTag("top_yield_calculator_btn")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Harvest & Sales History (PDF)") },
                                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showHarvestHistoryLogScreen = true
                                                },
                                                modifier = Modifier.testTag("overflow_harvest_logs")
                                            )
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                text = { Text("User & Farm Profile") },
                                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showUserProfileScreen = true
                                                },
                                                modifier = Modifier.testTag("user_profile_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Secure Google Auth") },
                                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showSecureAuthDialog = true
                                                },
                                                modifier = Modifier.testTag("auth_account_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Ecosystem Feature Guide") },
                                                leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showEcosystemClarificationDialog = true
                                                },
                                                modifier = Modifier.testTag("help_clarify_features_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("🏆 AWS Hackathon: Build It Lab") },
                                                leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706)) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showAwsHackathonBuildItScreen = true
                                                },
                                                modifier = Modifier.testTag("aws_hackathon_overflow_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Cloud Stacks: Build vs Ship") },
                                                leadingIcon = { Icon(Icons.Default.CloudDone, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showCloudArchitectureDialog = true
                                                },
                                                modifier = Modifier.testTag("cloud_arch_guide_topbar_button")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Project Architecture Overview") },
                                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = GreenPrimary) },
                                                onClick = {
                                                    showTopBarOverflowMenu = false
                                                    showProjectOverviewDialog = true
                                                },
                                                modifier = Modifier.testTag("info_action_button")
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            // Suppress bottom bar on wide screens (NavigationRail handles it)
                            if (!isExpandedScreen && (activePersona == AppPersonaMode.CENTRAL_OPS || activePersona == AppPersonaMode.VYAPAR_MANDI)) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    activeNavItems.forEachIndexed { index, item ->
                                        val isSelected = uiState.activeTab == index
                                        val activeColor = if (activePersona == AppPersonaMode.VYAPAR_MANDI) Color(0xFFD97706) else GreenPrimary
                                        val activeIndicator = if (activePersona == AppPersonaMode.VYAPAR_MANDI) Color(0xFFFEF3C7) else GreenLight

                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { viewModel.setActiveTab(index) },
                                            alwaysShowLabel = (screenWidth >= 360.dp || isSelected),
                                            icon = {
                                                if (item.badgeCount > 0) {
                                                    BadgedBox(
                                                        badge = {
                                                            Badge(
                                                                containerColor = if (item.testTag == "nav_surplus" || item.testTag == "nav_vyapar_deals") SpoilageRed else activeColor
                                                            ) {
                                                                Text("${item.badgeCount}")
                                                            }
                                                        }
                                                    ) {
                                                        Icon(imageVector = item.icon, contentDescription = item.title)
                                                    }
                                                } else {
                                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                                }
                                            },
                                            label = {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = if (screenWidth < 360.dp) 10.sp else 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = activeColor,
                                                selectedTextColor = activeColor,
                                                indicatorColor = activeIndicator
                                            ),
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            when (activePersona) {
                                AppPersonaMode.KISAN_FARMER -> {
                                    KisanFarmerAppScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onListHarvestClick = { showKisanAddHarvestDialog = true },
                                        onOpenProduceDashboard = { showProduceDashboardScreen = true },
                                        onOpenCropAdvisor = { showCropAdvisorScreen = true },
                                        onOpenSustainableShop = { showSustainableShopScreen = true },
                                        onOpenYieldCalculator = { showYieldCalculatorScreen = true },
                                        onOpenHarvestHistoryLogs = { showHarvestHistoryLogScreen = true },
                                        onOpenGeminiAssistant = { showGeminiAssistantDialog = true }
                                    )
                                }
                                AppPersonaMode.SAATHI_FLEET -> {
                                    SaathiDriverAppScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                                AppPersonaMode.KRISHI_MITRA -> {
                                    KrishiMitraScreen(
                                        uiState = uiState,
                                        viewModel = viewModel,
                                        onAssistListing = { farmer ->
                                            showKisanAddHarvestDialog = true
                                        }
                                    )
                                }
                                AppPersonaMode.VYAPAR_MANDI -> {
                                    VyaparBuyerAppScreen(
                                        uiState = uiState,
                                        viewModel = viewModel
                                    )
                                }
                                AppPersonaMode.CENTRAL_OPS -> {
                                    when (uiState.activeTab) {
                                        0 -> HubOperationsScreen(uiState = uiState, viewModel = viewModel)
                                        1 -> ProduceDashboardScreen(uiState = uiState, viewModel = viewModel)
                                        2 -> SurplusPriorityScreen(uiState = uiState, viewModel = viewModel)
                                        3 -> MarketplaceScreen(uiState = uiState, viewModel = viewModel)
                                        4 -> RegionalLogisticsMapScreen(uiState = uiState)
                                        5 -> OrdersFulfillmentScreen(uiState = uiState, viewModel = viewModel)
                                        else -> HubOperationsScreen(uiState = uiState, viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Persona Selector Dialog
    if (showPersonaSelectorDialog) {
        PersonaSelectorModal(
            currentPersona = activePersona,
            onSelect = { persona ->
                activePersona = persona
                viewModel.setActiveTab(0)
                showPersonaSelectorDialog = false
            },
            onOpenFullRoleSelection = {
                showPersonaSelectorDialog = false
                hasCompletedRoleSelection = false
            },
            onDismiss = { showPersonaSelectorDialog = false }
        )
    }

    // Theme Selection Dialog
    if (showThemeSelectionDialog) {
        ThemeSelectionDialog(
            currentTheme = appThemeMode,
            onThemeSelected = { newTheme ->
                viewModel.setAppThemeMode(newTheme)
            },
            onDismiss = { showThemeSelectionDialog = false }
        )
    }

    if (showEcosystemClarificationDialog) {
        EcosystemClarificationDialog(onDismiss = { showEcosystemClarificationDialog = false })
    }

    if (showProjectOverviewDialog) {
        ProjectOverviewDialog(
            onDismiss = { showProjectOverviewDialog = false },
            onOpenCloudArchitecture = {
                showProjectOverviewDialog = false
                showCloudArchitectureDialog = true
            }
        )
    }

    if (showCloudArchitectureDialog) {
        CloudArchitectureGuideDialog(
            onDismiss = { showCloudArchitectureDialog = false },
            onOpenBuildItLab = {
                showCloudArchitectureDialog = false
                showAwsHackathonBuildItScreen = true
            }
        )
    }

    if (showKisanAddHarvestDialog) {
        val currentUserName = (uiState.authState as? com.example.data.auth.AuthUiState.Authenticated)?.user?.displayName ?: "Ramesh Patil"
        val currentUserPhone = (uiState.authState as? com.example.data.auth.AuthUiState.Authenticated)?.user?.phoneNumber?.ifBlank { "+91 98220 14829" } ?: "+91 98220 14829"

        FarmerProducePostDialog(
            initialFarmerName = currentUserName,
            initialFarmerPhone = currentUserPhone,
            initialFarmLocation = "Niphad, Nashik",
            initialDistrict = "Nashik",
            onDismiss = { showKisanAddHarvestDialog = false },
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
                showKisanAddHarvestDialog = false
            }
        )
    }

    if (showSecureAuthDialog) {
        SecureAuthDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showSecureAuthDialog = false }
        )
    }

    if (showGeminiAssistantDialog) {
        KrishiGeminiAssistantDialog(
            uiState = uiState,
            viewModel = viewModel,
            activeRole = activePersona.title,
            onDismiss = { showGeminiAssistantDialog = false },
            onNavigateToShop = { showSustainableShopScreen = true },
            onNavigateToProduceDashboard = { showProduceDashboardScreen = true },
            onNavigateToCropAdvisor = { showCropAdvisorScreen = true },
            onNavigateToYieldCalculator = { showYieldCalculatorScreen = true },
            onNavigateToMaps = {
                activePersona = AppPersonaMode.CENTRAL_OPS
                viewModel.setActiveTab(4)
            },
            onOpenAddHarvest = { showKisanAddHarvestDialog = true },
            onNavigateToSurplus = {
                activePersona = AppPersonaMode.CENTRAL_OPS
                viewModel.setActiveTab(2)
            },
            onNavigateToHarvestLogs = { showHarvestHistoryLogScreen = true },
            onOpenSettings = {
                showGeminiAssistantDialog = false
                showAppSettingsScreen = true
            }
        )
    }
}

@Composable
fun PersonaSelectorModal(
    currentPersona: AppPersonaMode,
    onSelect: (AppPersonaMode) -> Unit,
    onOpenFullRoleSelection: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Switch User Profession", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Switch between tailored experiences for Farmers, Facilitators, Buyers, Drivers & Ops",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppPersonaMode.values().forEach { persona ->
                    val isSelected = currentPersona == persona
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) persona.themeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) persona.themeColor else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(persona) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(persona.themeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = persona.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) persona.themeColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = persona.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = persona.themeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenFullRoleSelection,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Role Selection Screen")
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
fun ProjectOverviewDialog(
    onDismiss: () -> Unit,
    onOpenCloudArchitecture: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "KrishiFlow Ecosystem Architecture",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📱 4 Targeted Apps + 1 Central Ops Sync:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                )
                Text(
                    text = "1. Kisan App: High-contrast, bilingual interface for farmers to see verified buyer demand, pledge harvests, and view instant UPI settlements.\n" +
                            "2. Krishi Mitra App (Field Lead): Regional facilitator managing rural clusters. Visits farms, assists non-smartphone farmers, verifies quality grades, and ensures 100% data accuracy.\n" +
                            "3. Vyapar App: Procurement catalog for Mandi Wholesalers, D-Mart, and Street Vendors to post RFQs and reserve graded lots.\n" +
                            "4. Saathi Fleet App: Turn-by-turn route, electronic weighing checklist, and proof-of-delivery for drivers.\n" +
                            "5. Central Ops Hub: Real-time Surplus-Priority Queue reducing post-harvest waste to 0%.",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "☁️ Synchronized Cloud Backend (AWS):",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                )
                Text(
                    text = "Multiple serverless microservices running in sync on Amazon API Gateway, AWS Lambda, Amazon DynamoDB, and Amazon EventBridge. When a farmer pledges stock on the Kisan app, it instantly reflects on the Vyapar app and dispatches the Saathi vehicle.",
                    style = MaterialTheme.typography.bodySmall
                )

                Button(
                    onClick = onOpenCloudArchitecture,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("overview_open_build_vs_ship_btn")
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Explore Build vs Ship Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Got It")
            }
        }
    )
}
