package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.FallbackVerifiedPlaces
import com.example.data.gemini.GeminiMapsGroundingService
import com.example.data.gemini.GroundedPlaceItem
import com.example.ui.components.RealGoogleMapsTerrainView
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import kotlinx.coroutines.launch

data class MapNode(
    val id: String,
    val title: String,
    val type: MapNodeType,
    val subtitle: String,
    val xRatio: Float, // Normalized X position on map canvas (0.0 to 1.0)
    val yRatio: Float, // Normalized Y position on map canvas (0.0 to 1.0)
    val distanceKm: Double,
    val etaMinutes: Int,
    val lat: Double,
    val lng: Double,
    val address: String,
    val phone: String,
    val currentActivity: String
)

enum class MapNodeType(
    val label: String,
    val color: Color,
    val icon: ImageVector
) {
    FARM_GATE("Farm Gate", Color(0xFF15803D), Icons.Default.Agriculture),
    COLLECTION_HUB("KrishiFlow Hub", Color(0xFF0D9488), Icons.Default.LocalShipping),
    APMC_MANDI("APMC Mandi", Color(0xFFD97706), Icons.Default.TrendingUp),
    BUYER_DC("Buyer / Supermarket", Color(0xFF7C3AED), Icons.Default.Storefront),
    ACTIVE_VAN("Live Fleet Van", Color(0xFF2563EB), Icons.Default.Navigation)
}

val InteractiveRegionalMapNodes = listOf(
    MapNode(
        id = "NODE-1",
        title = "Tukaram Shinde Farm",
        type = MapNodeType.FARM_GATE,
        subtitle = "Tomato (1,800 kg ready) • Grade A",
        xRatio = 0.22f,
        yRatio = 0.20f,
        distanceKm = 14.2,
        etaMinutes = 22,
        lat = 19.1248,
        lng = 73.9782,
        address = "Plot 4B, Narayangaon Village, Junnar",
        phone = "+91 98221 44521",
        currentActivity = "Packed in KrishiFlow crates, awaiting Van #MH-12-KF-4820"
    ),
    MapNode(
        id = "NODE-2",
        title = "Manchar Chilling & Sorting Hub",
        type = MapNodeType.COLLECTION_HUB,
        subtitle = "Solar Pre-cooling • 4 Tonnes Cap",
        xRatio = 0.48f,
        yRatio = 0.38f,
        distanceKm = 24.5,
        etaMinutes = 35,
        lat = 19.0089,
        lng = 73.9452,
        address = "State Highway 50, Manchar Bypass",
        phone = "+91 2133 223401",
        currentActivity = "Sorting Capsicum & Onions into Grade A and B batches"
    ),
    MapNode(
        id = "NODE-3",
        title = "D-Mart Regional DC (Chakan)",
        type = MapNodeType.BUYER_DC,
        subtitle = "Procurement Gate #3 • Bulk Inward",
        xRatio = 0.70f,
        yRatio = 0.58f,
        distanceKm = 38.0,
        etaMinutes = 52,
        lat = 18.7612,
        lng = 73.8562,
        address = "MIDC Phase 2, Chakan Industrial Area",
        phone = "+91 20 6732 9900",
        currentActivity = "PO-2026-881 verified; automated gate pass generated"
    ),
    MapNode(
        id = "NODE-4",
        title = "Pune Gultekdi Market Yard",
        type = MapNodeType.APMC_MANDI,
        subtitle = "Wholesale Mandi & Street Vendor Line",
        xRatio = 0.82f,
        yRatio = 0.84f,
        distanceKm = 56.4,
        etaMinutes = 75,
        lat = 18.4892,
        lng = 73.8647,
        address = "Gultekdi Market Yard Terminal, Pune",
        phone = "+91 20 2426 1234",
        currentActivity = "Surplus lot discount: 25% off for local cart vendor unions"
    ),
    MapNode(
        id = "NODE-5",
        title = "Van #MH-12-KF-4820 (In Transit)",
        type = MapNodeType.ACTIVE_VAN,
        subtitle = "Carrying 1,200 kg • Next: D-Mart DC",
        xRatio = 0.58f,
        yRatio = 0.46f,
        distanceKm = 11.2,
        etaMinutes = 16,
        lat = 18.8912,
        lng = 73.9011,
        address = "Pune-Nashik Expressway Km 44",
        phone = "+91 99220 84729",
        currentActivity = "Speed: 52 km/h • GPS Live Telemetry Active"
    )
)

@Composable
fun RegionalLogisticsMapScreen(
    uiState: KrishiUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedNode by remember { mutableStateOf(InteractiveRegionalMapNodes[0]) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingPlaces by remember { mutableStateOf(false) }
    var groundedPlaces by remember { mutableStateOf<List<GroundedPlaceItem>>(FallbackVerifiedPlaces) }
    var groundingNotice by remember { mutableStateOf("Grounding verified with regional agricultural hubs & mandis.") }
    var showEcosystemFlowGuide by remember { mutableStateOf(false) }

    fun runGroundingSearch(q: String) {
        isLoadingPlaces = true
        coroutineScope.launch {
            val (places, notice) = GeminiMapsGroundingService.queryNearbyAgriculturalPlaces(
                query = q,
                locationContext = "Pune, Junnar, Manchar, Chakan Maharashtra",
                context = context
            )
            groundedPlaces = places
            groundingNotice = notice
            isLoadingPlaces = false
        }
    }

    LaunchedEffect(Unit) {
        runGroundingSearch("")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with "Clarify Features" Guide Action
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Live Regional Route & Maps",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Farm-Gate to DC Tracking • Google Maps Grounding",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCCFBF1)
                                )
                            }
                        }

                        // Feature Clarification Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF08A),
                            modifier = Modifier
                                .clickable { showEcosystemFlowGuide = true }
                                .testTag("clarify_features_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF713F12),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "How It Works",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF713F12)
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = "Tracks collection from rural farm-gates through KrishiFlow sorting hubs directly to supermarket DCs and mandis. Tap any node on the map below for live telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCCFBF1)
                    )
                }
            }
        }

        // Real Google Maps Terrain View with Topographic & Elevation Features
        item {
            RealGoogleMapsTerrainView(
                selectedNode = selectedNode,
                onNodeSelect = { selectedNode = it }
            )
        }

        // Selected Map Node Live Inspection Card
        item {
            SelectedNodeDetailCard(
                node = selectedNode,
                onOpenDirections = {
                    try {
                        val gmmIntentUri = Uri.parse("geo:${selectedNode.lat},${selectedNode.lng}?q=${Uri.encode(selectedNode.title)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        Log.w("RegionalMap", "Navigation app not found: ${e.message}")
                    }
                },
                onCallPhone = {
                    try {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedNode.phone}"))
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(callIntent)
                    } catch (e: Exception) {
                        Log.w("RegionalMap", "Dialer app not found: ${e.message}")
                    }
                }
            )
        }

        // Section: Gemini 3.5 Flash Google Maps Grounding Finder
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Find Nearby Mandis & Cold Stores",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                        Text(
                            text = "Gemini 3.5 Flash Maps",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Grounded with real-time Google Maps data for accurate locations, operating hours, and live commodity handling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Search Input with instant search button
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        runGroundingSearch(it)
                    },
                    placeholder = { Text("Search Mandi, Cold Store, or Depot...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (isLoadingPlaces) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        searchQuery = ""
                                        runGroundingSearch("")
                                    }
                                    .padding(4.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_maps_search_input")
                )

                // Quick Presets
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = searchQuery == "Mandi",
                            onClick = {
                                searchQuery = "Mandi"
                                runGroundingSearch("Mandi")
                            },
                            label = { Text("APMC Mandis") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = searchQuery == "Cold Storage",
                            onClick = {
                                searchQuery = "Cold Storage"
                                runGroundingSearch("Cold Storage")
                            },
                            label = { Text("Cold Storages") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = searchQuery == "Procurement",
                            onClick = {
                                searchQuery = "Procurement"
                                runGroundingSearch("Procurement")
                            },
                            label = { Text("Collection Hubs") }
                        )
                    }
                }
            }
        }

        // Grounded Places Feed
        itemsIndexed(groundedPlaces, key = { index, place -> "${place.title}_${place.latitude}_$index" }) { _, place ->
            GroundedPlaceCard(
                place = place,
                onNavigate = {
                    try {
                        val gmmIntentUri = Uri.parse("geo:${place.latitude},${place.longitude}?q=${Uri.encode(place.title)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        Log.w("RegionalMap", "Navigation app not found: ${e.message}")
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Comprehensive Ecosystem Clarification & Feature Guide Dialog
    if (showEcosystemFlowGuide) {
        EcosystemClarificationDialog(
            onDismiss = { showEcosystemFlowGuide = false }
        )
    }
}

@Composable
fun SelectedNodeDetailCard(
    node: MapNode,
    onOpenDirections: () -> Unit,
    onCallPhone: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, node.type.color.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = node.type.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = node.type.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = node.type.color
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${node.distanceKm} km • ~${node.etaMinutes} mins ETA",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = node.type.color
                )
            }

            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "${node.subtitle} • ${node.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Live Telemetry Activity:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(node.currentActivity, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallPhone,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Hub", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenDirections,
                    colors = ButtonDefaults.buttonColors(containerColor = node.type.color),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Navigation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GroundedPlaceCard(
    place: GroundedPlaceItem,
    onNavigate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "${place.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${place.category} • ${place.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hours: ${place.operatingHours}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigate) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EcosystemClarificationDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("How KrishiFlow Works (Clear Guide)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "A simple 5-step flow that completely eliminates middleman exploitation and stops fresh crop spoilage:",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(shape = RoundedCornerShape(8.dp), color = GreenLight, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("1. Farmer Lists Harvest (Kisan App)", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 12.sp)
                        Text("Farmer or Krishi Mitra lists crop, quantity, and village location with guaranteed rates.", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEDE9FE), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("2. Quality Audited (Krishi Mitra)", fontWeight = FontWeight.Bold, color = Color(0xFF6D28D9), fontSize = 12.sp)
                        Text("Field lead checks moisture and certifies produce as Grade A or B on-site.", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF3C7), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("3. Pre-Procured by Buyers (Vyapar App)", fontWeight = FontWeight.Bold, color = Color(0xFFB45309), fontSize = 12.sp)
                        Text("Supermarkets and mandi traders reserve lots before vehicles depart, ensuring 0% unsold inventory.", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFCCFBF1), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("4. Transit & Delivery (Saathi Fleet)", fontWeight = FontWeight.Bold, color = EcoTeal, fontSize = 12.sp)
                        Text("Vans collect directly from farm gate and deliver along optimized transit routes.", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("5. Instant UPI Settlement (Zero Deduction)", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 12.sp)
                        Text("The moment the drop is verified, 100% of the funds are deposited into the farmer's bank account.", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Understood!")
            }
        }
    )
}
