package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.data.auth.AuthUiState
import com.example.data.gemini.KrishiGeminiKeyManager
import com.example.ui.AppPersonaMode
import com.example.ui.components.CloudArchitectureGuideDialog
import com.example.ui.components.SecureAuthDialog
import com.example.ui.components.ThemeSelectionDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FarmerDetails
import com.example.data.model.MandiManagerDetails
import com.example.data.model.OrderStatus
import com.example.data.model.RegionalManagerDetails
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.VendorDetails
import com.example.ui.components.OrderProcurementTrackingView
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserProfileScreen(
    viewModel: KrishiViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeProfileTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedProfession by remember { mutableStateOf(UserRole.Profession.FARMER) }
    var currentProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    var userGeminiApiKey by remember { mutableStateOf(KrishiGeminiKeyManager.getUserApiKey(context)) }
    var isDeveloperKeyDisabled by remember { mutableStateOf(KrishiGeminiKeyManager.isDeveloperKeyDisabled(context)) }
    var showCloudArchDialog by remember { mutableStateOf(false) }

    // Form editing states
    var editFullName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editDistrict by remember { mutableStateOf("") }
    var editVillageOrCity by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }

    // Farmer-specific form states
    var editLandAcres by remember { mutableStateOf("4.5") }
    var editUpiId by remember { mutableStateOf("") }
    var editSoilType by remember { mutableStateOf("Rich Black Clay Soil") }
    var editIrrigationType by remember { mutableStateOf("Automated Drip") }
    var editKccNumber by remember { mutableStateOf("") }
    var editPrimaryCrops by remember { mutableStateOf(listOf("Tomato", "Onion", "Pomegranate")) }
    var newCropInput by remember { mutableStateOf("") }

    // Vendor-specific form states
    var editBusinessName by remember { mutableStateOf("") }
    var editVendorType by remember { mutableStateOf("SUPERMARKET_MART") }
    var editGstin by remember { mutableStateOf("") }
    var editDailyCapacityKg by remember { mutableStateOf("15000") }
    var editCreditLimitRupees by remember { mutableStateOf("500000") }
    var editPaymentTerms by remember { mutableStateOf("Instant / T+1") }

    // Mandi Manager-specific form states
    var editMandiName by remember { mutableStateOf("") }
    var editMandiCode by remember { mutableStateOf("") }
    var editApmcZone by remember { mutableStateOf("") }
    var editTradersCount by remember { mutableStateOf("280") }
    var editDailyYardTons by remember { mutableStateOf("450") }
    var editHasWeighbridge by remember { mutableStateOf(true) }
    var editColdStorageTons by remember { mutableStateOf("80") }

    // Regional Manager-specific form states
    var editEmployeeId by remember { mutableStateOf("KF-MH-REG-01") }
    var editAssignedHub by remember { mutableStateOf("Western Maharashtra Agri-Logistics Hub") }
    var editManagedDistricts by remember { mutableStateOf(listOf("Nashik", "Pune", "Ahmednagar", "Satara", "Solapur")) }
    var editDirectSupervisors by remember { mutableStateOf("12") }
    var editEmergencyDispatchAuthority by remember { mutableStateOf(true) }

    // Sync form fields from profile object
    fun populateFields(profile: UserProfile) {
        currentProfile = profile
        editFullName = profile.fullName
        editEmail = profile.email
        editPhone = profile.phoneNumber
        editDistrict = profile.district
        editVillageOrCity = profile.villageOrCity
        editAddress = profile.address

        profile.farmerDetails?.let {
            editLandAcres = it.landAreaAcres.toString()
            editUpiId = it.upiId
            editSoilType = it.soilType
            editIrrigationType = it.irrigationType
            editKccNumber = it.kisanCreditCardNo
            if (it.primaryCrops.isNotEmpty()) editPrimaryCrops = it.primaryCrops
        }

        profile.vendorDetails?.let {
            editBusinessName = it.businessName
            editVendorType = it.vendorType
            editGstin = it.gstin
            editDailyCapacityKg = it.dailyProcurementCapacityKg.toInt().toString()
            editCreditLimitRupees = it.creditLimitRupees.toInt().toString()
            editPaymentTerms = it.preferredPaymentTerms
        }

        profile.mandiManagerDetails?.let {
            editMandiName = it.mandiName
            editMandiCode = it.mandiCode
            editApmcZone = it.apmcZone
            editTradersCount = it.licensedTradersCount.toString()
            editDailyYardTons = it.dailyYardCapacityTons.toInt().toString()
            editHasWeighbridge = it.hasElectronicWeighbridge
            editColdStorageTons = it.coldStorageAvailableTons.toInt().toString()
        }

        profile.regionalManagerDetails?.let {
            editEmployeeId = it.employeeId
            editAssignedHub = it.assignedHub
            if (it.managedDistricts.isNotEmpty()) editManagedDistricts = it.managedDistricts
            editDirectSupervisors = it.directSupervisorsCount.toString()
            editEmergencyDispatchAuthority = it.emergencyDispatchAuthority
        }
    }

    // Load profession-specific profile from Firestore
    fun loadProfileForProfession(profession: UserRole.Profession) {
        val role = UserRole(profession)
        val defaultProfile = UserProfile(
            userId = "USER_${profession.code}",
            fullName = when (profession) {
                UserRole.Profession.FARMER -> "Ramesh Patil"
                UserRole.Profession.VENDOR -> "Avenue Supermarts (D-Mart Procurement)"
                UserRole.Profession.MANDI_MANAGER -> "Vijayrao Suryavanshi"
                UserRole.Profession.REGIONAL_MANAGER -> "Dr. Sunita Kulkarni"
            },
            role = role,
            district = if (profession == UserRole.Profession.FARMER) "Nashik" else "Pune",
            farmerDetails = if (profession == UserRole.Profession.FARMER) FarmerDetails(
                landAreaAcres = 4.5,
                primaryCrops = listOf("Tomato", "Onion", "Pomegranate"),
                upiId = "rameshpatil@okaxis"
            ) else null,
            vendorDetails = if (profession == UserRole.Profession.VENDOR) VendorDetails(
                businessName = "Avenue Supermarts (D-Mart)",
                vendorType = "SUPERMARKET_MART",
                dailyProcurementCapacityKg = 15000.0,
                creditLimitRupees = 500000.0
            ) else null,
            mandiManagerDetails = if (profession == UserRole.Profession.MANDI_MANAGER) MandiManagerDetails(
                mandiName = "Pune APMC Market Yard",
                mandiCode = "MH-PUN-01",
                licensedTradersCount = 280,
                dailyYardCapacityTons = 450.0
            ) else null,
            regionalManagerDetails = if (profession == UserRole.Profession.REGIONAL_MANAGER) RegionalManagerDetails(
                employeeId = "KF-MH-REG-01",
                assignedHub = "Western Maharashtra Agri-Logistics Hub"
            ) else null
        )
        populateFields(defaultProfile)

        isLoading = true
        coroutineScope.launch {
            val list = viewModel.userRepository.getUsersByProfession(role).getOrDefault(emptyList())
            val matched = list.firstOrNull()
            if (matched != null) {
                populateFields(matched)
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedProfession) {
        loadProfileForProfession(selectedProfession)
    }

    fun saveProfileToFirestore() {
        val base = currentProfile ?: return
        isSaving = true

        val updatedFarmerDetails = if (selectedProfession == UserRole.Profession.FARMER) {
            FarmerDetails(
                landAreaAcres = editLandAcres.toDoubleOrNull() ?: 4.0,
                primaryCrops = editPrimaryCrops,
                upiId = editUpiId,
                soilType = editSoilType,
                irrigationType = editIrrigationType,
                kisanCreditCardNo = editKccNumber
            )
        } else base.farmerDetails

        val updatedVendorDetails = if (selectedProfession == UserRole.Profession.VENDOR) {
            VendorDetails(
                businessName = editBusinessName.ifBlank { editFullName },
                vendorType = editVendorType,
                gstin = editGstin,
                dailyProcurementCapacityKg = editDailyCapacityKg.toDoubleOrNull() ?: 10000.0,
                creditLimitRupees = editCreditLimitRupees.toDoubleOrNull() ?: 300000.0,
                preferredPaymentTerms = editPaymentTerms
            )
        } else base.vendorDetails

        val updatedMandiDetails = if (selectedProfession == UserRole.Profession.MANDI_MANAGER) {
            MandiManagerDetails(
                mandiName = editMandiName,
                mandiCode = editMandiCode,
                apmcZone = editApmcZone,
                licensedTradersCount = editTradersCount.toIntOrNull() ?: 250,
                dailyYardCapacityTons = editDailyYardTons.toDoubleOrNull() ?: 400.0,
                hasElectronicWeighbridge = editHasWeighbridge,
                coldStorageAvailableTons = editColdStorageTons.toDoubleOrNull() ?: 50.0
            )
        } else base.mandiManagerDetails

        val updatedRegionalDetails = if (selectedProfession == UserRole.Profession.REGIONAL_MANAGER) {
            RegionalManagerDetails(
                employeeId = editEmployeeId,
                assignedHub = editAssignedHub,
                managedDistricts = editManagedDistricts,
                directSupervisorsCount = editDirectSupervisors.toIntOrNull() ?: 10,
                emergencyDispatchAuthority = editEmergencyDispatchAuthority
            )
        } else base.regionalManagerDetails

        val updatedProfile = base.copy(
            fullName = editFullName,
            email = editEmail,
            phoneNumber = editPhone,
            role = UserRole(selectedProfession),
            district = editDistrict,
            villageOrCity = editVillageOrCity,
            address = editAddress,
            farmerDetails = updatedFarmerDetails,
            vendorDetails = updatedVendorDetails,
            mandiManagerDetails = updatedMandiDetails,
            regionalManagerDetails = updatedRegionalDetails,
            updatedAt = System.currentTimeMillis()
        )

        viewModel.saveUserProfile(updatedProfile) { result ->
            isSaving = false
            if (result.isSuccess) {
                currentProfile = updatedProfile
                isEditMode = false
                statusMessage = "Profile updated successfully in Cloud Firestore!"
            } else {
                statusMessage = "Failed to update profile: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    val professionColor = when (selectedProfession) {
        UserRole.Profession.FARMER -> Color(0xFF15803D)
        UserRole.Profession.VENDOR -> Color(0xFFD97706)
        UserRole.Profession.MANDI_MANAGER -> Color(0xFF2563EB)
        UserRole.Profession.REGIONAL_MANAGER -> Color(0xFF7C3AED)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "User Profile",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Firestore Cloud Backend Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to dashboard"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("profile_theme_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Theme Options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { loadProfileForProfession(selectedProfession) },
                        modifier = Modifier.testTag("profile_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh from Firestore",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val activeOrdersCount = remember(uiState.orders) {
                uiState.orders.count {
                    it.status == OrderStatus.PENDING_CONFIRMATION ||
                    it.status == OrderStatus.RESERVED ||
                    it.status == OrderStatus.OUT_FOR_DELIVERY
                }
            }

            // =========================================================================
            // TOP TAB NAVIGATION: Profile & Role Settings vs Orders & Procurement Tracking
            // =========================================================================
            TabRow(
                selectedTabIndex = activeProfileTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = GreenPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeProfileTab == 0,
                    onClick = { activeProfileTab = 0 },
                    text = { Text("Profile & Role", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_profile_details")
                )
                Tab(
                    selected = activeProfileTab == 1,
                    onClick = { activeProfileTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Order Tracking", fontWeight = FontWeight.Bold)
                            if (activeOrdersCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = GreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$activeOrdersCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_order_tracking")
                )
            }

            if (activeProfileTab == 1) {
                // Dedicated State-Based Order Tracking System ('Pending' -> 'In Transit' -> 'Delivered')
                OrderProcurementTrackingView(
                    orders = uiState.orders,
                    onAdvanceStatus = { order, next -> viewModel.updateOrderStatus(order, next) },
                    onCreateSampleOrder = { viewModel.createSampleProcurementOrder() }
                )
            } else {
                // Profile & Role Settings Tab
            // =========================================================================
            // 1. PROFESSION SWITCHER PILLS (Farmer, Vendor, Mandi Manager, Regional Mgr)
            // =========================================================================
            Text(
                text = "Select Profession Role",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserRole.Profession.values().forEach { prof ->
                    val isSelected = selectedProfession == prof
                    val chipColor = when (prof) {
                        UserRole.Profession.FARMER -> Color(0xFF15803D)
                        UserRole.Profession.VENDOR -> Color(0xFFD97706)
                        UserRole.Profession.MANDI_MANAGER -> Color(0xFF2563EB)
                        UserRole.Profession.REGIONAL_MANAGER -> Color(0xFF7C3AED)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedProfession = prof },
                        label = {
                            Text(
                                text = prof.displayTitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (prof) {
                                    UserRole.Profession.FARMER -> Icons.Default.Agriculture
                                    UserRole.Profession.VENDOR -> Icons.Default.Storefront
                                    UserRole.Profession.MANDI_MANAGER -> Icons.Default.AccountBalance
                                    UserRole.Profession.REGIONAL_MANAGER -> Icons.Default.SupportAgent
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.15f),
                            selectedLabelColor = chipColor,
                            selectedLeadingIconColor = chipColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) chipColor else MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = chipColor
                        ),
                        modifier = Modifier.testTag("role_chip_${prof.code.lowercase()}")
                    )
                }
            }

            // =========================================================================
            // 2. USER PROFILE HEADER CARD
            // =========================================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, professionColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(professionColor.copy(alpha = 0.15f))
                                .border(2.dp, professionColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (selectedProfession) {
                                    UserRole.Profession.FARMER -> Icons.Default.Agriculture
                                    UserRole.Profession.VENDOR -> Icons.Default.Storefront
                                    UserRole.Profession.MANDI_MANAGER -> Icons.Default.AccountBalance
                                    UserRole.Profession.REGIONAL_MANAGER -> Icons.Default.SupportAgent
                                },
                                contentDescription = "Profile Avatar",
                                tint = professionColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = editFullName.ifBlank { "User Profile" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Identity",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = selectedProfession.displayTitle + " • " + (editDistrict.ifBlank { "Maharashtra" }),
                                style = MaterialTheme.typography.bodySmall,
                                color = professionColor,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = editEmail.ifBlank { "user@krishiflow.in" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit / View toggle button
                        IconButton(
                            onClick = { isEditMode = !isEditMode },
                            modifier = Modifier
                                .background(
                                    if (isEditMode) professionColor else professionColor.copy(alpha = 0.12f),
                                    CircleShape
                                )
                                .testTag("toggle_edit_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Done Editing" else "Edit Profile",
                                tint = if (isEditMode) Color.White else professionColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

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
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Firestore: krishiflow_users",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = professionColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isEditMode) "EDITING MODE" else "VIEW MODE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = professionColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Compact Procurement Orders Live Status Banner in Profile
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeProfileTab = 1 }
                    .testTag("profile_orders_banner")
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
                        Surface(
                            shape = CircleShape,
                            color = GreenLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Procurement Orders Tracking",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${uiState.orders.size} Total Orders • $activeOrdersCount In Transit / Pending",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Tracking",
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Status notice
            AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GreenLight.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = statusMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // =========================================================================
            // 2.5 LOGIN CREDENTIALS & AUTH LINKING (GOOGLE & PHONE NUMBER)
            // =========================================================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth().testTag("auth_credentials_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Account & Sign-In Options",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        TextButton(
                            onClick = { showAuthDialog = true },
                            modifier = Modifier.testTag("open_auth_dialog_button")
                        ) {
                            Text("Switch Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }

                    // Display Current Auth State
                    when (val auth = uiState.authState) {
                        is AuthUiState.Authenticated -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Active: ${auth.user.displayName}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF14532D)
                                        )
                                        Text(
                                            text = if (auth.user.authProvider == "GOOGLE")
                                                "🌐 Google Account: ${auth.user.email}"
                                            else if (auth.user.authProvider == "PHONE")
                                                "📱 Phone Verified: ${auth.user.phoneNumber}"
                                            else
                                                "✉️ Email Verified: ${auth.user.email}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = "Sign in via Google or Phone Number to safeguard and sync your farm & APMC data:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Google and Phone Sign-In Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1-Tap Google Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val persona = when (selectedProfession) {
                                        UserRole.Profession.FARMER -> AppPersonaMode.KISAN_FARMER
                                        UserRole.Profession.VENDOR -> AppPersonaMode.VYAPAR_MANDI
                                        UserRole.Profession.MANDI_MANAGER -> AppPersonaMode.CENTRAL_OPS
                                        UserRole.Profession.REGIONAL_MANAGER -> AppPersonaMode.CENTRAL_OPS
                                    }
                                    viewModel.signInWithGoogle(role = persona)
                                }
                                .testTag("profile_google_login_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Login",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }

                        // Phone Number OTP Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GreenLight.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showAuthDialog = true }
                                .testTag("profile_phone_login_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Phone (OTP)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                androidx.compose.material3.LinearProgressIndicator(
                    color = professionColor,
                    modifier = Modifier.fillMaxWidth().testTag("profile_loading_bar")
                )
            }

            // =====================================================================
            // 3. COMMON ACCOUNT DETAILS SECTION
            // =====================================================================
            SectionHeader(title = "Basic Contact & Location Details", icon = Icons.Default.AccountCircle)

                if (isEditMode) {
                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_full_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f).testTag("input_phone"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = editDistrict,
                            onValueChange = { editDistrict = it },
                            label = { Text("District") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("input_district"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = editVillageOrCity,
                        onValueChange = { editVillageOrCity = it },
                        label = { Text("Village / City") },
                        modifier = Modifier.fillMaxWidth().testTag("input_village_city"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Operational Street / Hub Address") },
                        modifier = Modifier.fillMaxWidth().testTag("input_address"),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    InfoDisplayCard {
                        InfoRow(label = "Full Name", value = editFullName.ifBlank { "Not configured" })
                        InfoRow(label = "Phone", value = editPhone.ifBlank { "+91 98220 14829" })
                        InfoRow(label = "Email", value = editEmail.ifBlank { "user@krishiflow.in" })
                        InfoRow(label = "District / State", value = "${editDistrict.ifBlank { "Nashik" }}, Maharashtra")
                        InfoRow(label = "Village / Area", value = editVillageOrCity.ifBlank { "Niphad" })
                        InfoRow(label = "Street Address", value = editAddress.ifBlank { "Pimpalgaon Road, Niphad" })
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // =====================================================================
                // 4. PROFESSION-SPECIFIC FORM FIELDS & DATA
                // =====================================================================
                when (selectedProfession) {
                    UserRole.Profession.FARMER -> {
                        SectionHeader(title = "🌾 Farmer Agricultural Profile", icon = Icons.Default.Agriculture)

                        if (isEditMode) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editLandAcres,
                                    onValueChange = { editLandAcres = it },
                                    label = { Text("Land Area (Acres)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f).testTag("input_farmer_land_acres"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editUpiId,
                                    onValueChange = { editUpiId = it },
                                    label = { Text("UPI ID (Instant Payout)") },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                    modifier = Modifier.weight(1f).testTag("input_farmer_upi"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = editSoilType,
                                onValueChange = { editSoilType = it },
                                label = { Text("Soil Type") },
                                modifier = Modifier.fillMaxWidth().testTag("input_farmer_soil"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = editIrrigationType,
                                onValueChange = { editIrrigationType = it },
                                label = { Text("Irrigation System") },
                                leadingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("input_farmer_irrigation"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = editKccNumber,
                                onValueChange = { editKccNumber = it },
                                label = { Text("Kisan Credit Card (KCC) No.") },
                                modifier = Modifier.fillMaxWidth().testTag("input_farmer_kcc"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text(
                                text = "Primary Crops Cultivated",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                editPrimaryCrops.forEach { crop ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF15803D).copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF15803D).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = crop, style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D))
                                            Text(
                                                text = "✕",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Red,
                                                modifier = Modifier
                                                    .clickable { editPrimaryCrops = editPrimaryCrops - crop }
                                                    .padding(horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCropInput,
                                    onValueChange = { newCropInput = it },
                                    label = { Text("Add Crop") },
                                    modifier = Modifier.weight(1f).testTag("input_new_crop"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = {
                                        if (newCropInput.isNotBlank()) {
                                            editPrimaryCrops = editPrimaryCrops + newCropInput.trim()
                                            newCropInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    modifier = Modifier.testTag("add_crop_button")
                                ) {
                                    Text("Add")
                                }
                            }
                        } else {
                            InfoDisplayCard {
                                InfoRow(label = "Landholding", value = "$editLandAcres Acres")
                                InfoRow(label = "Primary Crops", value = editPrimaryCrops.joinToString(", "))
                                InfoRow(label = "UPI ID for Payouts", value = editUpiId.ifBlank { "rameshpatil@okaxis" })
                                InfoRow(label = "Soil Classification", value = editSoilType)
                                InfoRow(label = "Irrigation System", value = editIrrigationType)
                                InfoRow(label = "KCC Account", value = editKccNumber.ifBlank { "MH-KCC-99482718" })
                            }
                        }
                    }

                    UserRole.Profession.VENDOR -> {
                        SectionHeader(title = "🏪 Commercial Vendor Profile", icon = Icons.Default.Storefront)

                        if (isEditMode) {
                            OutlinedTextField(
                                value = editBusinessName,
                                onValueChange = { editBusinessName = it },
                                label = { Text("Business / Enterprise Name") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("input_vendor_business"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editVendorType,
                                    onValueChange = { editVendorType = it },
                                    label = { Text("Category (Mart/Wholesale)") },
                                    modifier = Modifier.weight(1f).testTag("input_vendor_type"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editGstin,
                                    onValueChange = { editGstin = it },
                                    label = { Text("GSTIN Tax ID") },
                                    modifier = Modifier.weight(1f).testTag("input_vendor_gstin"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editDailyCapacityKg,
                                    onValueChange = { editDailyCapacityKg = it },
                                    label = { Text("Daily Capacity (Kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_vendor_capacity"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editCreditLimitRupees,
                                    onValueChange = { editCreditLimitRupees = it },
                                    label = { Text("Credit Limit (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_vendor_credit"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = editPaymentTerms,
                                onValueChange = { editPaymentTerms = it },
                                label = { Text("Preferred Settlement Terms") },
                                modifier = Modifier.fillMaxWidth().testTag("input_vendor_terms"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            InfoDisplayCard {
                                InfoRow(label = "Business Entity", value = editBusinessName.ifBlank { "Avenue Supermarts (D-Mart)" })
                                InfoRow(label = "Vendor Type", value = editVendorType)
                                InfoRow(label = "GSTIN", value = editGstin.ifBlank { "27AAACA1234D1Z5" })
                                InfoRow(label = "Daily Intake Capacity", value = "$editDailyCapacityKg Kg")
                                InfoRow(label = "Approved Credit Line", value = "₹$editCreditLimitRupees")
                                InfoRow(label = "Payment Terms", value = editPaymentTerms)
                            }
                        }
                    }

                    UserRole.Profession.MANDI_MANAGER -> {
                        SectionHeader(title = "🏛️ APMC Mandi Yard Profile", icon = Icons.Default.AccountBalance)

                        if (isEditMode) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editMandiName,
                                    onValueChange = { editMandiName = it },
                                    label = { Text("Mandi Name") },
                                    modifier = Modifier.weight(1.3f).testTag("input_mandi_name"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editMandiCode,
                                    onValueChange = { editMandiCode = it },
                                    label = { Text("Yard Code") },
                                    modifier = Modifier.weight(0.7f).testTag("input_mandi_code"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = editApmcZone,
                                onValueChange = { editApmcZone = it },
                                label = { Text("APMC Administrative Zone") },
                                modifier = Modifier.fillMaxWidth().testTag("input_mandi_zone"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editTradersCount,
                                    onValueChange = { editTradersCount = it },
                                    label = { Text("Licensed Traders") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_mandi_traders"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editDailyYardTons,
                                    onValueChange = { editDailyYardTons = it },
                                    label = { Text("Daily Throughput (Tons)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_mandi_capacity"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Electronic Weighbridge Installed", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = editHasWeighbridge,
                                    onCheckedChange = { editHasWeighbridge = it },
                                    modifier = Modifier.testTag("switch_weighbridge")
                                )
                            }
                        } else {
                            InfoDisplayCard {
                                InfoRow(label = "APMC Mandi", value = editMandiName.ifBlank { "Pune APMC Market Yard" })
                                InfoRow(label = "Yard Code", value = editMandiCode.ifBlank { "MH-PUN-01" })
                                InfoRow(label = "Administrative Zone", value = editApmcZone.ifBlank { "Western Maharashtra Central Zone" })
                                InfoRow(label = "Commission Agents", value = "$editTradersCount Licensed")
                                InfoRow(label = "Daily Yard Arrival Capacity", value = "$editDailyYardTons Metric Tons")
                                InfoRow(label = "Weighbridge Facility", value = if (editHasWeighbridge) "Automated Electronic" else "Manual Beam")
                                InfoRow(label = "Cold Storage Available", value = "$editColdStorageTons Tons")
                            }
                        }
                    }

                    UserRole.Profession.REGIONAL_MANAGER -> {
                        SectionHeader(title = "🌐 Regional Command Profile", icon = Icons.Default.SupportAgent)

                        if (isEditMode) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = editEmployeeId,
                                    onValueChange = { editEmployeeId = it },
                                    label = { Text("Employee ID") },
                                    modifier = Modifier.weight(0.8f).testTag("input_rm_empid"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editDirectSupervisors,
                                    onValueChange = { editDirectSupervisors = it },
                                    label = { Text("Field Supervisors") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.2f).testTag("input_rm_supervisors"),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = editAssignedHub,
                                onValueChange = { editAssignedHub = it },
                                label = { Text("Assigned Regional Command Hub") },
                                modifier = Modifier.fillMaxWidth().testTag("input_rm_hub"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Emergency Spoilage Dispatch Authority", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = editEmergencyDispatchAuthority,
                                    onCheckedChange = { editEmergencyDispatchAuthority = it },
                                    modifier = Modifier.testTag("switch_emergency_dispatch")
                                )
                            }
                        } else {
                            InfoDisplayCard {
                                InfoRow(label = "Employee ID", value = editEmployeeId.ifBlank { "KF-MH-REG-01" })
                                InfoRow(label = "Command Center Hub", value = editAssignedHub.ifBlank { "Western Maharashtra Agri-Logistics Hub" })
                                InfoRow(label = "Managed Districts", value = editManagedDistricts.joinToString(", "))
                                InfoRow(label = "Direct Field Staff", value = "$editDirectSupervisors Supervisors")
                                InfoRow(label = "Emergency Rerouting Authority", value = if (editEmergencyDispatchAuthority) "Granted (Level 3)" else "Restricted")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // =====================================================================
                // 5. ACTION BUTTONS: SAVE TO FIRESTORE / SWITCH PRIMARY ROLE
                // =====================================================================
                if (isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentProfile?.let { populateFields(it) }
                                isEditMode = false
                            },
                            modifier = Modifier.weight(1f).testTag("cancel_edit_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { saveProfileToFirestore() },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = professionColor),
                            modifier = Modifier.weight(1.5f).testTag("save_profile_firestore_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            } else {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save to Firestore")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { isEditMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = professionColor),
                        modifier = Modifier.fillMaxWidth().testTag("enter_edit_mode_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit ${selectedProfession.displayTitle} Details")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =====================================================================
                // 10. GEMINI API KEY & PUBLISHING SECURITY MANAGEMENT CARD
                // =====================================================================
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth().testTag("profile_gemini_api_key_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "AI Key Management & BYOK Inference",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Support for Gemini, OpenAI, Claude & Offline ICAR bio-models",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("profile_open_full_settings_btn")
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Open Full AI Settings (Gemini, OpenAI, Claude)",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "To protect your Google Cloud billing quota, you can remove your API key here or configure alternate providers (OpenAI, Claude). When publishing the app, tap 'Purge All Keys for Publishing' to ensure nobody can access or consume your developer key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Input field for custom API key
                        OutlinedTextField(
                            value = userGeminiApiKey,
                            onValueChange = { userGeminiApiKey = it },
                            label = { Text("Personal Gemini API Key") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth().testTag("profile_user_gemini_key_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Save Key
                            Button(
                                onClick = {
                                    if (userGeminiApiKey.isNotBlank()) {
                                        KrishiGeminiKeyManager.saveUserApiKey(context, userGeminiApiKey.trim())
                                        statusMessage = "Gemini API key saved securely!"
                                    }
                                },
                                enabled = userGeminiApiKey.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("profile_save_gemini_key_btn")
                            ) {
                                Text("Save Key", fontWeight = FontWeight.Bold)
                            }

                            // Remove My Key
                            OutlinedButton(
                                onClick = {
                                    KrishiGeminiKeyManager.removeUserApiKey(context)
                                    userGeminiApiKey = ""
                                    statusMessage = "API key removed from device."
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("profile_remove_gemini_key_btn")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remove Key")
                            }
                        }

                        // Nuclear Publishing Protection Option
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Publishing Protection Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                                Text(
                                    text = "Purges all stored keys and blocks developer build-time fallback. The public APK will run in zero-quota offline mode unless the user enters their own key.",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF7F1D1D)
                                )

                                Button(
                                    onClick = {
                                        KrishiGeminiKeyManager.purgeAllKeysForPublishing(context)
                                        userGeminiApiKey = ""
                                        isDeveloperKeyDisabled = true
                                        statusMessage = "Publishing Mode: All keys purged and developer key disabled!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("profile_purge_for_publishing_btn")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Purge All Keys for Publishing", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                if (isDeveloperKeyDisabled) {
                                    Text(
                                        text = "✓ Developer key fallback is currently DISABLED. App is safe for release.",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }

                                Button(
                                    onClick = { showCloudArchDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("profile_view_cloud_arch_btn")
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explore Build It vs Ship It Matrix", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } // Close if (activeProfileTab == 0)
        }
    }

    if (showCloudArchDialog) {
        CloudArchitectureGuideDialog(onDismiss = { showCloudArchDialog = false })
    }

    if (showAuthDialog) {
        SecureAuthDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showAuthDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = appThemeMode,
            onThemeSelected = { newTheme ->
                viewModel.setAppThemeMode(newTheme)
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoDisplayCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.3f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
