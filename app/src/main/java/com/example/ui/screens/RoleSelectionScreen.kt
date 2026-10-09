package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppPersonaMode
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

data class ProfessionRoleItem(
    val persona: AppPersonaMode,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val keyFeatures: List<String>,
    val badgeLabel: String,
    val icon: ImageVector,
    val accentColor: Color
)

val AvailableProfessions = listOf(
    ProfessionRoleItem(
        persona = AppPersonaMode.KISAN_FARMER,
        title = "Kisan (Farmer)",
        hindiTitle = "किसान साथी",
        description = "Sell crops directly with zero middleman broker deductions. View guaranteed rates and receive instant UPI settlements.",
        keyFeatures = listOf("0% Commission", "Instant Bank Transfer", "1-Tap Harvest Pledge"),
        badgeLabel = "Direct Farmer Payout",
        icon = Icons.Default.Agriculture,
        accentColor = Color(0xFF15803D)
    ),
    ProfessionRoleItem(
        persona = AppPersonaMode.KRISHI_MITRA,
        title = "Krishi Mitra (Field Lead)",
        hindiTitle = "कृषि मित्र / फील्ड लीड",
        description = "Regional village facilitator assisting rural farmers, conducting on-site crop quality checks, and managing accurate listings.",
        keyFeatures = listOf("Assisted Farmer Onboarding", "Physical Moisture & Quality Audit", "Cluster Roster"),
        badgeLabel = "Village Facilitator",
        icon = Icons.Default.SupportAgent,
        accentColor = Color(0xFF7C3AED)
    ),
    ProfessionRoleItem(
        persona = AppPersonaMode.VYAPAR_MANDI,
        title = "Vyapar (Buyer & Mart)",
        hindiTitle = "व्यापारी / मंडी व रिटेल",
        description = "APMC Mandi wholesalers, supermarket chains (D-Mart, JioMart), and street vendors sourcing verified graded produce.",
        keyFeatures = listOf("Bulk Farm-Direct Sourcing", "Post Purchase RFQs", "Quality Certified Lots"),
        badgeLabel = "Wholesale & Retail",
        icon = Icons.Default.Storefront,
        accentColor = Color(0xFFD97706)
    ),
    ProfessionRoleItem(
        persona = AppPersonaMode.SAATHI_FLEET,
        title = "Saathi (Logistics Driver)",
        hindiTitle = "सारथी / वाहन चालक",
        description = "Collection van and transport fleet driver. Navigate pickup routes, perform digital weighing, and complete delivery drops.",
        keyFeatures = listOf("Farm-Gate Pickup Routes", "Digital Weighing Checklist", "Instant Payout Release"),
        badgeLabel = "Fleet & Delivery",
        icon = Icons.Default.LocalShipping,
        accentColor = Color(0xFF0D9488)
    ),
    ProfessionRoleItem(
        persona = AppPersonaMode.CENTRAL_OPS,
        title = "Central Ops & Sustainability",
        hindiTitle = "केंद्रीय संचालन एवं ज़ीरो वेस्ट",
        description = "Complete supply chain operations console. Manage the real-time Surplus-Priority Queue to eliminate food waste.",
        keyFeatures = listOf("Surplus-Priority Queue", "Mandi Price Intelligence", "Food Loss Analytics"),
        badgeLabel = "Full Ecosystem",
        icon = Icons.Default.Eco,
        accentColor = Color(0xFF166534)
    )
)

@Composable
fun RoleSelectionScreen(
    initialSelected: AppPersonaMode,
    onRoleConfirmed: (AppPersonaMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPersona by remember { mutableStateOf(initialSelected) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GreenLight
                    ) {
                        Text(
                            text = "कृषि प्रवाह • KrishiFlow Ecosystem",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = "Choose Your Role",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Select your profession to customize the app interface, features, and tools specifically for your daily workflow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Profession Cards List
            items(AvailableProfessions, key = { it.persona.name }) { item ->
                val isSelected = selectedPersona == item.persona
                ProfessionCard(
                    item = item,
                    isSelected = isSelected,
                    onSelect = { selectedPersona = item.persona }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Fixed Confirmation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val selectedItem = AvailableProfessions.firstOrNull { it.persona == selectedPersona } ?: AvailableProfessions[0]

                Button(
                    onClick = { onRoleConfirmed(selectedPersona) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("confirm_profession_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = selectedItem.accentColor)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Enter as ${selectedItem.title.substringBefore(" (")}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "You can easily switch roles anytime from the top bar.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun ProfessionCard(
    item: ProfessionRoleItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("profession_card_${item.persona.name}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) item.accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) item.accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp)
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) item.accentColor else item.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else item.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.hindiTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Unselected",
                    tint = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Key Feature Pills
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.keyFeatures.forEach { feature ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) item.accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = item.accentColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) item.accentColor else MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
