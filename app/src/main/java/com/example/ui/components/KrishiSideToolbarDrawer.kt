package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.OrderStatus
import com.example.ui.AppPersonaMode
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState

/**
 * High-polish responsive Side Toolbar & Navigation Drawer for KrishiFlow.
 * Eliminates topbar clutter and ensures all features, navigation tabs,
 * agricultural tools, and system options remain cleanly accessible across
 * all screen sizes (phones, foldables, tablets).
 */
@Composable
fun KrishiSideDrawerContent(
    activePersona: AppPersonaMode,
    onSelectPersona: (AppPersonaMode) -> Unit,
    uiState: KrishiUiState,
    onNavigateToProduceDashboard: () -> Unit,
    onNavigateToCropAdvisor: () -> Unit,
    onNavigateToYieldCalculator: () -> Unit,
    onNavigateToHarvestHistoryLogs: () -> Unit,
    onNavigateToSustainableShop: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToSecureAuth: () -> Unit,
    onOpenProjectOverview: () -> Unit,
    onOpenEcosystemClarification: () -> Unit,
    onTriggerWorkManagerSync: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenThemeSelector: () -> Unit = {},
    onOpenGeminiAssistant: () -> Unit = {},
    onOpenCloudArchitecture: () -> Unit = {},
    onOpenAwsHackathonBuildIt: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(min = 280.dp, max = 340.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .testTag("side_navigation_drawer_content")
    ) {
        // =========================================================================
        // 1. BRAND HEADER WITH STATUS & WORKMANAGER SYNC
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF166534),
                            Color(0xFF15803D)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color(0xFF166534),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "KrishiFlow",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 21.sp
                            )
                        )
                        Text(
                            text = "Kisan Digital Agri-Network",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFBBF7D0),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // WorkManager Cloud Synchronization Status Strip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (uiState.workManagerSyncState?.contains("Syncing") == true) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFFBBF7D0),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = uiState.workManagerSyncState ?: "Cloud Sync Ready",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = onTriggerWorkManagerSync,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("drawer_quick_sync_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync Now",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // =========================================================================
        // 2. ACTIVE ROLE & PERSONA SELECTOR
        // =========================================================================
        SectionHeader(title = "APP ROLE & INTERFACE")

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AppPersonaMode.entries.forEach { persona ->
                val isSelected = activePersona == persona
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) persona.themeColor.copy(alpha = 0.14f) else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, persona.themeColor.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectPersona(persona)
                            onCloseDrawer()
                        }
                        .testTag("drawer_persona_${persona.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) persona.themeColor else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) persona.themeColor else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = persona.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = persona.themeColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        // =========================================================================
        // 3. CORE HUBS & MARKETPLACE VIEWS (CENTRAL OPS / VYAPAR)
        // =========================================================================
        SectionHeader(title = "CORE HUBS & MARKET")

        DrawerNavItem(
            label = "Hub & Match Orders",
            icon = Icons.Default.Storefront,
            badgeCount = uiState.openDemands.size,
            isSelected = activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab == 0,
            testTag = "drawer_nav_hub",
            onClick = {
                onSelectPersona(AppPersonaMode.CENTRAL_OPS)
                onSelectTab(0)
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Produce Dashboard",
            icon = Icons.Default.Agriculture,
            badgeCount = uiState.producePosts.size,
            isSelected = false,
            testTag = "top_produce_dashboard_btn",
            onClick = {
                onNavigateToProduceDashboard()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Zero-Waste Surplus Queue",
            icon = Icons.Default.Eco,
            badgeCount = uiState.surplusBatches.size,
            badgeColor = SpoilageRed,
            isSelected = activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab == 2,
            testTag = "drawer_nav_surplus",
            onClick = {
                onSelectPersona(AppPersonaMode.CENTRAL_OPS)
                onSelectTab(2)
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Wholesale Marketplace",
            icon = Icons.Default.ShoppingBag,
            isSelected = activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab == 3,
            testTag = "drawer_nav_market",
            onClick = {
                onSelectPersona(AppPersonaMode.CENTRAL_OPS)
                onSelectTab(3)
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Logistics & Route Map",
            icon = Icons.Default.Map,
            isSelected = activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab == 4,
            testTag = "drawer_nav_map",
            onClick = {
                onSelectPersona(AppPersonaMode.CENTRAL_OPS)
                onSelectTab(4)
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Orders & Inbound Delivery",
            icon = Icons.Default.LocalShipping,
            badgeCount = uiState.orders.count { it.status == OrderStatus.OUT_FOR_DELIVERY },
            isSelected = activePersona == AppPersonaMode.CENTRAL_OPS && uiState.activeTab == 5,
            testTag = "drawer_nav_orders",
            onClick = {
                onSelectPersona(AppPersonaMode.CENTRAL_OPS)
                onSelectTab(5)
                onCloseDrawer()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        // =========================================================================
        // 4. SMART AGRICULTURAL TOOLS (ALL OPTIONS PROMINENTLY VISIBLE)
        // =========================================================================
        SectionHeader(title = "SMART AGRI TOOLS")

        DrawerNavItem(
            label = "KrishiMitra Gemini AI Assistant",
            icon = Icons.Default.AutoAwesome,
            tagText = "Voice & AI",
            testTag = "drawer_gemini_ai_assistant_btn",
            onClick = {
                onOpenGeminiAssistant()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "AI Crop Advisory & Planning",
            icon = Icons.Default.Eco,
            testTag = "crop_advisor_topbar_button",
            onClick = {
                onNavigateToCropAdvisor()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Harvest Yield Calculator",
            icon = Icons.Default.Calculate,
            testTag = "top_yield_calculator_btn",
            onClick = {
                onNavigateToYieldCalculator()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Harvest History & PDF Report",
            icon = Icons.Default.History,
            tagText = "PDF Ready",
            testTag = "drawer_harvest_logs_btn",
            onClick = {
                onNavigateToHarvestHistoryLogs()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Sustainable Inputs Shop",
            icon = Icons.Default.ShoppingCart,
            badgeCount = uiState.cartItems.sumOf { it.quantity },
            testTag = "top_sustainable_shop_btn",
            onClick = {
                onNavigateToSustainableShop()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "APMC Mandi Intelligence",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            testTag = "drawer_mandi_rates_btn",
            onClick = {
                onSelectPersona(AppPersonaMode.VYAPAR_MANDI)
                onCloseDrawer()
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

        // =========================================================================
        // 5. ACCOUNT, CLOUD & SYSTEM ARCHITECTURE
        // =========================================================================
        SectionHeader(title = "ACCOUNT & SETTINGS")

        DrawerNavItem(
            label = "User & Farm Profile",
            icon = Icons.Default.AccountCircle,
            testTag = "user_profile_button",
            onClick = {
                onNavigateToUserProfile()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Secure Login & Credentials",
            icon = Icons.Default.Lock,
            testTag = "auth_account_button",
            onClick = {
                onNavigateToSecureAuth()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "WorkManager Cloud Sync",
            icon = Icons.Default.Sync,
            testTag = "firestore_cloud_sync_button",
            onClick = {
                onTriggerWorkManagerSync()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Ecosystem Guide & FAQ",
            icon = Icons.Default.HelpOutline,
            testTag = "help_clarify_features_button",
            onClick = {
                onOpenEcosystemClarification()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "⚙️ Settings & API Keys (BYOK)",
            icon = Icons.Default.Settings,
            testTag = "drawer_settings_byok_btn",
            onClick = {
                onNavigateToSettings()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Theme Options (रंग शैली)",
            icon = Icons.Default.Palette,
            testTag = "drawer_theme_options_btn",
            onClick = {
                onOpenThemeSelector()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "🏆 AWS Hackathon: Build It Lab",
            icon = Icons.Default.EmojiEvents,
            testTag = "aws_hackathon_build_it_drawer_button",
            onClick = {
                onOpenAwsHackathonBuildIt()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Cloud Stacks: Build vs Ship",
            icon = Icons.Default.CloudDone,
            testTag = "cloud_arch_guide_drawer_button",
            onClick = {
                onOpenCloudArchitecture()
                onCloseDrawer()
            }
        )

        DrawerNavItem(
            label = "Project Architecture Info",
            icon = Icons.Default.Info,
            testTag = "info_action_button",
            onClick = {
                onOpenProjectOverview()
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerNavItem(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    badgeCount: Int = 0,
    badgeColor: Color = GreenPrimary,
    tagText: String? = null
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (badgeCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = "$badgeCount",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (tagText != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldLight
                    ) {
                        Text(
                            text = tagText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = GreenLight,
            selectedTextColor = GreenPrimary,
            selectedIconColor = GreenPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .testTag(testTag)
    )
}

/**
 * Adaptive Side Navigation Rail for tablets, Chromebooks, and expanded foldable screens (>= 720dp).
 */
@Composable
fun KrishiNavigationRail(
    activePersona: AppPersonaMode,
    activeTab: Int,
    uiState: KrishiUiState,
    onOpenDrawer: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onNavigateToProduceDashboard: () -> Unit,
    onNavigateToSustainableShop: () -> Unit,
    onNavigateToCropAdvisor: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.testTag("rail_open_drawer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Full Side Toolbar",
                        tint = GreenPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activePersona.themeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activePersona.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        modifier = modifier.fillMaxHeight()
    ) {
        NavigationRailItem(
            selected = activeTab == 0,
            onClick = { onSelectTab(0) },
            icon = {
                BadgedBox(badge = {
                    if (uiState.openDemands.isNotEmpty()) {
                        Badge(containerColor = GreenPrimary) { Text("${uiState.openDemands.size}") }
                    }
                }) {
                    Icon(Icons.Default.Storefront, contentDescription = "Hub")
                }
            },
            label = { Text("Hub", fontSize = 10.sp) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = GreenPrimary,
                selectedTextColor = GreenPrimary,
                indicatorColor = GreenLight
            ),
            modifier = Modifier.testTag("rail_tab_hub")
        )

        NavigationRailItem(
            selected = false,
            onClick = onNavigateToProduceDashboard,
            icon = {
                BadgedBox(badge = {
                    if (uiState.producePosts.isNotEmpty()) {
                        Badge(containerColor = GreenPrimary) { Text("${uiState.producePosts.size}") }
                    }
                }) {
                    Icon(Icons.Default.Agriculture, contentDescription = "Produce")
                }
            },
            label = { Text("Produce", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_tab_produce")
        )

        NavigationRailItem(
            selected = activeTab == 2,
            onClick = { onSelectTab(2) },
            icon = {
                BadgedBox(badge = {
                    if (uiState.surplusBatches.isNotEmpty()) {
                        Badge(containerColor = SpoilageRed) { Text("${uiState.surplusBatches.size}") }
                    }
                }) {
                    Icon(Icons.Default.Eco, contentDescription = "Zero Waste")
                }
            },
            label = { Text("Surplus", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_tab_surplus")
        )

        NavigationRailItem(
            selected = activeTab == 3,
            onClick = { onSelectTab(3) },
            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Marketplace") },
            label = { Text("Market", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_tab_market")
        )

        NavigationRailItem(
            selected = false,
            onClick = onNavigateToSustainableShop,
            icon = {
                BadgedBox(badge = {
                    if (uiState.cartItems.isNotEmpty()) {
                        Badge(containerColor = SpoilageRed) { Text("${uiState.cartItems.sumOf { it.quantity }}") }
                    }
                }) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Shop")
                }
            },
            label = { Text("Shop", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_tab_shop")
        )

        Spacer(modifier = Modifier.weight(1f))

        NavigationRailItem(
            selected = false,
            onClick = onNavigateToUserProfile,
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 10.sp) },
            modifier = Modifier.testTag("rail_tab_profile")
        )
    }
}
