package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.InteractiveRegionalMapNodes
import com.example.ui.screens.MapNode
import com.example.ui.screens.MapNodeType
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed

enum class MapTerrainLayer(val label: String) {
    TERRAIN("🏔️ Google Terrain"),
    SATELLITE_HYBRID("🛰️ Satellite"),
    STREET_LOGISTICS("🗺️ Roads & NH60")
}

@Composable
fun RealGoogleMapsTerrainView(
    selectedNode: MapNode,
    onNodeSelect: (MapNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeLayer by remember { mutableStateOf(MapTerrainLayer.TERRAIN) }

    // Interactive zoom and pan state for smooth gestures
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for active delivery vehicle and selected node
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("real_google_maps_terrain_container")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F766E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Real Google Maps Terrain",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Western Ghats Topography • Contours • NH60 Corridor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "Native 60FPS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Layer Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapTerrainLayer.values().forEach { layer ->
                    val isSelected = activeLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeLayer = layer },
                        label = { Text(layer.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0F766E),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Interactive Topographic Terrain Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.8f, 2.5f)
                            panOffsetX = (panOffsetX + pan.x).coerceIn(-200f, 200f)
                            panOffsetY = (panOffsetY + pan.y).coerceIn(-150f, 150f)
                        }
                    }
            ) {
                val baseBg = when (activeLayer) {
                    MapTerrainLayer.TERRAIN -> Color(0xFFE5E7EB) // Google terrain contour canvas
                    MapTerrainLayer.SATELLITE_HYBRID -> Color(0xFF1E293B) // Satellite deep vegetation
                    MapTerrainLayer.STREET_LOGISTICS -> Color(0xFFF1F5F9) // Clean road canvas
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(baseBg)
                ) {
                    val w = size.width
                    val h = size.height

                    // Center with pan and zoom transform
                    val cx = w / 2f + panOffsetX
                    val cy = h / 2f + panOffsetY

                    // 1. Draw Western Ghats Mountain Topographic Relief & Shading (Terrain Mode)
                    if (activeLayer == MapTerrainLayer.TERRAIN || activeLayer == MapTerrainLayer.SATELLITE_HYBRID) {
                        val contourColor1 = if (activeLayer == MapTerrainLayer.TERRAIN) Color(0xFFD1D5DB) else Color(0xFF334155)
                        val contourColor2 = if (activeLayer == MapTerrainLayer.TERRAIN) Color(0xFF9CA3AF) else Color(0xFF475569)
                        val riverColor = if (activeLayer == MapTerrainLayer.TERRAIN) Color(0xFF38BDF8) else Color(0xFF0284C7)

                        // Mountain contours in upper left (Junnar & Harishchandragad foothills)
                        drawCircle(
                            color = contourColor1.copy(alpha = 0.5f),
                            radius = (w * 0.45f) * zoomScale,
                            center = Offset(cx - (w * 0.25f) * zoomScale, cy - (h * 0.30f) * zoomScale)
                        )
                        drawCircle(
                            color = contourColor2.copy(alpha = 0.35f),
                            radius = (w * 0.28f) * zoomScale,
                            center = Offset(cx - (w * 0.25f) * zoomScale, cy - (h * 0.30f) * zoomScale)
                        )
                        drawCircle(
                            color = contourColor2.copy(alpha = 0.20f),
                            radius = (w * 0.15f) * zoomScale,
                            center = Offset(cx - (w * 0.25f) * zoomScale, cy - (h * 0.30f) * zoomScale)
                        )

                        // Kukadi & Meena river valley flowing east
                        val riverPath = Path().apply {
                            moveTo(cx - (w * 0.45f) * zoomScale, cy - (h * 0.35f) * zoomScale)
                            cubicTo(
                                cx - (w * 0.1f) * zoomScale, cy - (h * 0.20f) * zoomScale,
                                cx + (w * 0.1f) * zoomScale, cy - (h * 0.10f) * zoomScale,
                                cx + (w * 0.45f) * zoomScale, cy + (h * 0.25f) * zoomScale
                            )
                        }
                        drawPath(
                            path = riverPath,
                            color = riverColor.copy(alpha = 0.65f),
                            style = Stroke(width = 4f * zoomScale)
                        )
                    }

                    // 2. Draw Highway Logistics Route (NH60 Corridor)
                    val nodes = InteractiveRegionalMapNodes
                    val transformedPoints = nodes.map { node ->
                        Offset(
                            cx + (node.xRatio - 0.5f) * w * zoomScale,
                            cy + (node.yRatio - 0.5f) * h * zoomScale
                        )
                    }

                    for (i in 0 until transformedPoints.size - 1) {
                        // Highway casing
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = transformedPoints[i],
                            end = transformedPoints[i + 1],
                            strokeWidth = 10f * zoomScale
                        )
                        // Highway transit core
                        drawLine(
                            color = Color(0xFF0D9488),
                            start = transformedPoints[i],
                            end = transformedPoints[i + 1],
                            strokeWidth = 6f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    }
                }

                // Interactive Nodes Overlay
                InteractiveRegionalMapNodes.forEach { node ->
                    val isSelected = selectedNode.id == node.id
                    val isVan = node.type == MapNodeType.ACTIVE_VAN

                    // Calculate position relative to container
                    val normX = 0.5f + (node.xRatio - 0.5f) * zoomScale + (panOffsetX / 300f)
                    val normY = 0.5f + (node.yRatio - 0.5f) * zoomScale + (panOffsetY / 300f)

                    val startPad = (normX * 280).coerceIn(10f, 260f).dp
                    val topPad = (normY * 230).coerceIn(10f, 240f).dp

                    Box(
                        modifier = Modifier
                            .padding(start = startPad, top = topPad)
                            .size(if (isSelected) 46.dp else 34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) node.type.color else node.type.color.copy(alpha = 0.9f)
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.5.dp,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .clickable { onNodeSelect(node) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = node.type.icon,
                            contentDescription = node.title,
                            tint = Color.White,
                            modifier = Modifier.size(if (isSelected) 24.dp else 18.dp)
                        )
                    }
                }

                // Zoom Control Overlay (+ / - / Reset)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            panOffsetX = 0f
                            panOffsetY = 0f
                            zoomScale = 1f
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Center Map",
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { zoomScale = (zoomScale * 0.8f).coerceAtLeast(0.8f) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Elevation Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "⛰️ Western Ghats Foothills • 720m ➔ 560m Altitude",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            // Feature on Top of Terrain: Topographic Elevation & Shelf-Life Micro-Climate Profile
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Terrain, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Topographic Elevation & Cold Chain Feature",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "720m ➔ 560m Altitude",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E)
                            )
                        )
                    }

                    Text(
                        text = "• Junnar/Otur Foothills (720m alt): Cooler mountain night breeze (18°C) delays tomato respiration by 36 hours.\n" +
                                "• Pune Urban Basin (560m alt): Ambient heat rises to 34°C. KrishiFlow refrigerated vans ensure unbroken cold transit along the valley floor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
