package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CropCategory
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import java.util.Locale

data class CropPreset(
    val name: String,
    val hindiName: String,
    val category: CropCategory,
    val defaultVariety: String,
    val mandiBenchmarkRate: Double,
    val suggestedAskingRate: Double,
    val iconEmoji: String
)

val PopularCropPresets = listOf(
    CropPreset("Tomato", "टमाटर", CropCategory.VEGETABLES, "Himsona / Hybrid", 18.0, 22.0, "🍅"),
    CropPreset("Red Onion", "लाल प्याज", CropCategory.VEGETABLES, "Nashik Red Garwa", 24.0, 28.5, "🧅"),
    CropPreset("Potato", "आलू", CropCategory.VEGETABLES, "Pukhraj / Jyoti", 16.0, 19.5, "🥔"),
    CropPreset("Green Chilli", "हरी मिर्च", CropCategory.VEGETABLES, "G-4 / Jwala", 42.0, 52.0, "🌶️"),
    CropPreset("Sweet Corn", "स्वीट कॉर्न", CropCategory.VEGETABLES, "Sugar 75", 14.0, 18.0, "🌽"),
    CropPreset("Pomegranate", "अनार", CropCategory.FRUITS, "Bhagwa Export", 85.0, 110.0, "🍇"),
    CropPreset("Sharbati Wheat", "शरबती गेहूं", CropCategory.GRAINS, "Lokwan Grade-1", 28.0, 34.0, "🌾"),
    CropPreset("Yellow Soybean", "सोयाबीन", CropCategory.PULSES, "JS-335", 44.0, 50.0, "🫘")
)

/**
 * High-fidelity Dialog for Farmers to post their agricultural produce with:
 * - Crop taxonomy, variety, and harvest timing
 * - Quantities in kg, quintals, or tonnes with live unit conversions
 * - Asking price per kg with real-time APMC Mandi modal benchmark comparison
 * - Estimated farmer payout and 0% brokerage middleman savings calculation
 * - Quality grade selection & storage conditions
 * - Direct persistence to Cloud Firestore
 */
@Composable
fun FarmerProducePostDialog(
    initialFarmerName: String = "Ramesh Patil",
    initialFarmerPhone: String = "+91 98220 14829",
    initialFarmLocation: String = "Niphad, Nashik",
    initialDistrict: String = "Nashik",
    onDismiss: () -> Unit,
    onSubmit: (
        cropName: String,
        category: CropCategory,
        variety: String,
        grade: QualityGrade,
        farmerName: String,
        farmerPhone: String,
        farmLocation: String,
        district: String,
        quantityKg: Double,
        quantityUnit: String,
        askingPricePerKg: Double,
        mandiBenchmarkPricePerKg: Double,
        storageCondition: StorageCondition,
        expectedFreshnessDays: Int,
        isOrganicOrResidueFree: Boolean,
        readyForImmediatePickup: Boolean,
        notes: String
    ) -> Unit
) {
    // Form state
    var selectedCropName by remember { mutableStateOf("Tomato") }
    var selectedCategory by remember { mutableStateOf(CropCategory.VEGETABLES) }
    var variety by remember { mutableStateOf("Himsona / Hybrid") }
    var quantityInput by remember { mutableStateOf("2500") }
    var quantityUnit by remember { mutableStateOf("KG") } // "KG", "QUINTAL", "TONNE"
    var askingPriceInput by remember { mutableStateOf("22.5") }
    var mandiBenchmarkRate by remember { mutableStateOf(18.0) }
    var selectedGrade by remember { mutableStateOf(QualityGrade.GRADE_A) }
    var storageCondition by remember { mutableStateOf(StorageCondition.VENTILATED_CRATE) }
    var freshnessDays by remember { mutableFloatStateOf(8f) }
    var isOrganic by remember { mutableStateOf(false) }
    var readyNow by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("Freshly harvested from automated drip field. Sorted and packed in 20kg plastic crates.") }

    var farmerName by remember { mutableStateOf(initialFarmerName) }
    var farmerPhone by remember { mutableStateOf(initialFarmerPhone) }
    var farmLocation by remember { mutableStateOf(initialFarmLocation) }
    var district by remember { mutableStateOf(initialDistrict) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Computed total kg
    val parsedQty = quantityInput.toDoubleOrNull() ?: 0.0
    val totalQuantityKg = remember(parsedQty, quantityUnit) {
        when (quantityUnit.uppercase()) {
            "QUINTAL" -> parsedQty * 100.0
            "TONNE" -> parsedQty * 1000.0
            else -> parsedQty
        }
    }

    val askingPricePerKg = askingPriceInput.toDoubleOrNull() ?: 0.0
    val totalEstimatedRevenue = totalQuantityKg * askingPricePerKg
    val traditionalMandiTotal = totalQuantityKg * (mandiBenchmarkRate * 0.90)
    val estimatedSavings = (totalEstimatedRevenue - traditionalMandiTotal).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("farmer_produce_post_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenPrimary)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Agriculture,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "अपनी उपज लिस्ट करें / Post Produce",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color(0xFFBBF7D0),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Cloud Firestore Real-Time Sync",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBBF7D0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_post_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Farmer Details Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "किसान: $farmerName",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$farmLocation • $farmerPhone",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GreenLight
                            ) {
                                Text(
                                    text = "Verified Farmer",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GreenPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Section 1: Choose Crop
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. फसल चुनें / Select Commodity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )

                        // Fast Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PopularCropPresets.forEach { preset ->
                                val isSelected = selectedCropName.equals(preset.name, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCropName = preset.name
                                        selectedCategory = preset.category
                                        variety = preset.defaultVariety
                                        mandiBenchmarkRate = preset.mandiBenchmarkRate
                                        askingPriceInput = preset.suggestedAskingRate.toString()
                                    },
                                    label = {
                                        Text("${preset.iconEmoji} ${preset.name} (${preset.hindiName})")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCropName,
                                onValueChange = { selectedCropName = it },
                                label = { Text("Crop Name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("crop_name_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = variety,
                                onValueChange = { variety = it },
                                label = { Text("Variety") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("crop_variety_input"),
                                singleLine = true
                            )
                        }
                    }

                    // Section 2: Quantity & Unit
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "2. मात्रा / Harvest Quantity",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quantityInput,
                                onValueChange = { quantityInput = it },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("produce_quantity_input"),
                                singleLine = true,
                                trailingIcon = {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = GreenPrimary)
                                }
                            )

                            // Unit Segmented Tabs
                            Row(
                                modifier = Modifier
                                    .weight(1.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("KG", "QUINTAL", "TONNE").forEach { unit ->
                                    val isSelected = quantityUnit.equals(unit, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) GreenPrimary else Color.Transparent)
                                            .clickable { quantityUnit = unit }
                                            .padding(horizontal = 8.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (unit) {
                                                "QUINTAL" -> "Quintal (100kg)"
                                                "TONNE" -> "Tonne (1000kg)"
                                                else -> "Kg"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Quantity Helpers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(500, 1000, 2500, 5000).forEach { qty ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            quantityInput = when (quantityUnit.uppercase()) {
                                                "QUINTAL" -> (qty / 100).toString()
                                                "TONNE" -> (qty / 1000.0).toString()
                                                else -> qty.toString()
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "+$qty kg",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = "कुल मात्रा: ${totalQuantityKg.toInt()} Kilograms",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )
                    }

                    // Section 3: Pricing & Mandi Comparison
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "3. मूल्य / Asking Price & Benchmark",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = askingPriceInput,
                                onValueChange = { askingPriceInput = it },
                                label = { Text("Your Price (₹/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("produce_price_input"),
                                singleLine = true,
                                trailingIcon = {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary)
                                }
                            )

                            // Mandi Modal Reference Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GoldLight,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "APMC Mandi Modal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF78350F)
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%.2f", mandiBenchmarkRate)} / kg",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                    )
                                    Text(
                                        text = "Trad. after commission: ₹${String.format(Locale.getDefault(), "%.2f", mandiBenchmarkRate * 0.90)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        // Total Valuation Banner
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = GreenLight),
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
                                        text = "अनुमानित कुल भुगतान / Total Payout",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenPrimary
                                    )
                                    Text(
                                        text = "₹${String.format(Locale.getDefault(), "%,.2f", totalEstimatedRevenue)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFEF08A)) {
                                        Text(
                                            text = "+₹${estimatedSavings.toInt()} Extra",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF713F12)
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "0% Middleman Cut",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Section 4: Quality Grade
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "4. गुणवत्ता ग्रेड / Quality Grade",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QualityGrade.values().forEach { grade ->
                                val isSelected = selectedGrade == grade
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) GreenLight else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GreenPrimary) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedGrade = grade }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = when (grade) {
                                                    QualityGrade.GRADE_A -> "Grade A"
                                                    QualityGrade.GRADE_B -> "Grade B"
                                                    QualityGrade.GRADE_C -> "Grade C"
                                                },
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = GreenPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = when (grade) {
                                                QualityGrade.GRADE_A -> "Export / Supermarket Mart"
                                                QualityGrade.GRADE_B -> "Standard Mandi Trade"
                                                QualityGrade.GRADE_C -> "Quick Sale / Bulk Pulp"
                                            },
                                            fontSize = 9.sp,
                                            lineHeight = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 5: Storage & Farmgate Pickup
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "5. भंडारण व लॉजिस्टिक्स / Storage & Pickup",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                StorageCondition.VENTILATED_CRATE,
                                StorageCondition.COLD_STORAGE,
                                StorageCondition.AMBIENT_SHED
                            ).forEach { storage ->
                                val isSelected = storageCondition == storage
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { storageCondition = storage },
                                    label = { Text(storage.displayName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EcoTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Freshness Slider
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ताजगी अवधि (Freshness Lifespan)",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "${freshnessDays.toInt()} Days",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GreenPrimary
                                )
                            }
                            Slider(
                                value = freshnessDays,
                                onValueChange = { freshnessDays = it },
                                valueRange = 2f..15f,
                                steps = 12,
                                colors = SliderDefaults.colors(
                                    thumbColor = GreenPrimary,
                                    activeTrackColor = GreenPrimary
                                )
                            )
                        }

                        // Checkbox Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("कीटनाशक मुक्त / Organic Certified", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Premium pricing eligibility", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isOrganic,
                                onCheckedChange = { isOrganic = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = GreenPrimary, checkedTrackColor = GreenLight)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = EcoTeal, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("तुरंत पिकअप तैयार / Ready for Pickup", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("KrishiFlow Saathi vehicle assigned directly", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = readyNow,
                                onCheckedChange = { readyNow = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = EcoTeal, checkedTrackColor = Color(0xFFCCFBF1))
                            )
                        }
                    }

                    // Section 6: Additional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("पैकेजिंग व विशेष विवरण / Packaging Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("produce_notes_input"),
                        maxLines = 3
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Bottom Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                        }

                        Button(
                            onClick = {
                                if (selectedCropName.isBlank()) {
                                    errorMessage = "Please enter crop name"
                                    return@Button
                                }
                                if (totalQuantityKg <= 0.0) {
                                    errorMessage = "Please enter valid quantity"
                                    return@Button
                                }
                                if (askingPricePerKg <= 0.0) {
                                    errorMessage = "Please enter asking price"
                                    return@Button
                                }

                                isSubmitting = true
                                errorMessage = null

                                onSubmit(
                                    selectedCropName.trim(),
                                    selectedCategory,
                                    variety.trim(),
                                    selectedGrade,
                                    farmerName.trim(),
                                    farmerPhone.trim(),
                                    farmLocation.trim(),
                                    district.trim(),
                                    totalQuantityKg,
                                    quantityUnit,
                                    askingPricePerKg,
                                    mandiBenchmarkRate,
                                    storageCondition,
                                    freshnessDays.toInt(),
                                    isOrganic,
                                    readyNow,
                                    notes.trim()
                                )
                            },
                            enabled = !isSubmitting,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("submit_produce_post_btn")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving to Firestore...")
                            } else {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Post to Firestore", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
