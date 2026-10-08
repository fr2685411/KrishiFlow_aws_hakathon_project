package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropCategory
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.components.QualityGradeBadge
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

data class ManagedFarmer(
    val id: String,
    val name: String,
    val phone: String,
    val village: String,
    val farmAcres: Double,
    val activeCrop: String,
    val estimatedHarvestKg: Double,
    val isVerified: Boolean,
    val smartphoneAccess: Boolean
)

val SampleManagedFarmers = listOf(
    ManagedFarmer(
        id = "FARM-01",
        name = "Tukaram Shinde",
        phone = "+91 98221 44521",
        village = "Narayangaon, Junnar",
        farmAcres = 3.5,
        activeCrop = "Hybrid Tomato",
        estimatedHarvestKg = 2500.0,
        isVerified = true,
        smartphoneAccess = false // Farmer has basic feature phone, Mitra manages listings
    ),
    ManagedFarmer(
        id = "FARM-02",
        name = "Baburao Patil",
        phone = "+91 94220 18392",
        village = "Manchar, Ambegaon",
        farmAcres = 5.0,
        activeCrop = "Nashik Red Onion",
        estimatedHarvestKg = 6000.0,
        isVerified = true,
        smartphoneAccess = false
    ),
    ManagedFarmer(
        id = "FARM-03",
        name = "Savitri Bai Gaikwad",
        phone = "+91 97632 99014",
        village = "Otur, Junnar",
        farmAcres = 2.0,
        activeCrop = "Green Capsicum",
        estimatedHarvestKg = 900.0,
        isVerified = false, // Pending field audit
        smartphoneAccess = true
    ),
    ManagedFarmer(
        id = "FARM-04",
        name = "Kashinath Daundkar",
        phone = "+91 98810 55231",
        village = "Rajgurunagar, Khed",
        farmAcres = 4.0,
        activeCrop = "Fresh Coriander & Mint",
        estimatedHarvestKg = 500.0,
        isVerified = true,
        smartphoneAccess = false
    )
)

@Composable
fun KrishiMitraScreen(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onAssistListing: (ManagedFarmer) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterUnverifiedOnly by remember { mutableStateOf(false) }
    var selectedFarmerForAudit by remember { mutableStateOf<ManagedFarmer?>(null) }
    var verifiedNotice by remember { mutableStateOf<String?>(null) }

    val filteredFarmers = remember(searchQuery, filterUnverifiedOnly) {
        SampleManagedFarmers.filter { f ->
            val matchesSearch = searchQuery.isBlank() ||
                    f.name.contains(searchQuery, ignoreCase = true) ||
                    f.village.contains(searchQuery, ignoreCase = true) ||
                    f.activeCrop.contains(searchQuery, ignoreCase = true)
            val matchesFilter = !filterUnverifiedOnly || !f.isVerified
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Krishi Mitra Officer Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6D28D9)),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Krishi Mitra Portal",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Ramesh Pawar • Lead #MH-PN-04",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFDDD6FE)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF08A)
                        ) {
                            Text(
                                text = "Cluster Lead",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF713F12)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Assigned Region: Junnar & Khed Talukas (42 Registered Farmers). Assist non-smartphone farmers with crop grading, accurate quantity estimation, and direct listing verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEDE9FE)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF5B21B6))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Farmers", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDDD6FE))
                            Text("42", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Harvest Ready", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDDD6FE))
                            Text("9.9 Tonnes", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFEF08A)))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Assisted Listings", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDDD6FE))
                            Text("100%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search farmer by name, village, or crop...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Farmers")
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mitra_search_farmers_input")
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = !filterUnverifiedOnly,
                        onClick = { filterUnverifiedOnly = false },
                        label = { Text("All Regional Farmers (${SampleManagedFarmers.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C3AED),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = filterUnverifiedOnly,
                        onClick = { filterUnverifiedOnly = true },
                        label = { Text("Needs Field Audit") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpoilageRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Regional Farmer Roster (${filteredFarmers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Assisted Operations",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7C3AED)
                )
            }
        }

        // Farmer Cards
        items(filteredFarmers, key = { "mitra_farmer_${it.id}_${it.name}" }) { farmer ->
            ManagedFarmerCard(
                farmer = farmer,
                onAssistListingClick = { onAssistListing(farmer) },
                onVerifyClick = { selectedFarmerForAudit = farmer }
            )
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Physical Audit & Quality Verification Modal
    selectedFarmerForAudit?.let { farmer ->
        FarmerAuditDialog(
            farmer = farmer,
            onDismiss = { selectedFarmerForAudit = null },
            onConfirmVerification = { notes, grade ->
                verifiedNotice = "Verified harvest for ${farmer.name}! Crop certified as $grade and published with Mitra Quality Seal."
                selectedFarmerForAudit = null
            }
        )
    }

    verifiedNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { verifiedNotice = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Quality Certified!", fontWeight = FontWeight.Bold) },
            text = { Text(notice) },
            confirmButton = {
                Button(
                    onClick = { verifiedNotice = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ManagedFarmerCard(
    farmer: ManagedFarmer,
    onAssistListingClick: () -> Unit,
    onVerifyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Name & Verification Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = farmer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (farmer.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified by Krishi Mitra",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (farmer.smartphoneAccess) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (farmer.smartphoneAccess) "App User" else "Feature Phone (Assisted)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (farmer.smartphoneAccess) GreenPrimary else Color(0xFFB45309)
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Location & Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(farmer.village, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "${farmer.farmAcres} Acres",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Current Crop & Volume
            Surface(
                shape = RoundedCornerShape(10.dp),
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
                        Text("Current Harvest Crop", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(farmer.activeCrop, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated Available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${farmer.estimatedHarvestKg.toInt()} kg",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                    }
                }
            }

            // Action Buttons for Krishi Mitra
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVerifyClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Field Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onAssistListingClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("List Crop for Farmer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FarmerAuditDialog(
    farmer: ManagedFarmer,
    onDismiss: () -> Unit,
    onConfirmVerification: (notes: String, grade: QualityGrade) -> Unit
) {
    var selectedGrade by remember { mutableStateOf(QualityGrade.GRADE_A) }
    var moisturePercent by remember { mutableStateOf("11.5") }
    var notes by remember { mutableStateOf("Physically verified on farm. Uniform sizing and zero pest marks.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Physical Quality Audit: ${farmer.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Verify crop quality, moisture content, and actual yield to certify the listing for Mandi buyers & supermarkets.",
                    style = MaterialTheme.typography.bodySmall
                )

                Text("Certified Grade:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QualityGrade.values().forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C3AED),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = moisturePercent,
                    onValueChange = { moisturePercent = it },
                    label = { Text("Moisture Level (%)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Inspection Certificate Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmVerification(notes, selectedGrade) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) {
                Text("Certify & Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
