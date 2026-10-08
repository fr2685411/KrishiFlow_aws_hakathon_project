package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EcoTeal
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Data point representing a single day's APMC Mandi modal price benchmark.
 */
data class CropPricePoint(
    val dayNumber: Int,
    val dateString: String,
    val modalPrice: Double,
    val minPrice: Double,
    val maxPrice: Double,
    val arrivalTonnes: Double
)

/**
 * Deterministic 30-day historical price data generator for agricultural crops.
 * Simulates realistic Mandi arrival volumes and price movements to assist
 * farmers in setting optimal asking prices.
 */
object CropPriceHistoryService {

    fun generate30DayPriceHistory(cropName: String, days: Int = 30): List<CropPricePoint> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days + 1)
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val (basePrice, volatility, trendStep) = when {
            cropName.contains("Tomato", ignoreCase = true) -> Triple(18.5, 3.2, 0.16)
            cropName.contains("Onion", ignoreCase = true) -> Triple(23.0, 2.5, 0.22)
            cropName.contains("Wheat", ignoreCase = true) -> Triple(31.0, 1.2, 0.08)
            cropName.contains("Pomegranate", ignoreCase = true) -> Triple(95.0, 8.5, 0.45)
            cropName.contains("Chana", ignoreCase = true) || cropName.contains("Gram", ignoreCase = true) -> Triple(62.0, 3.0, 0.18)
            cropName.contains("Chilli", ignoreCase = true) -> Triple(36.0, 5.0, 0.35)
            cropName.contains("Potato", ignoreCase = true) -> Triple(19.0, 1.8, 0.10)
            cropName.contains("Corn", ignoreCase = true) || cropName.contains("Maize", ignoreCase = true) -> Triple(20.0, 1.5, 0.12)
            cropName.contains("Soybean", ignoreCase = true) -> Triple(44.0, 2.8, 0.20)
            else -> Triple(25.0, 3.0, 0.15)
        }

        val pseudoSeed = cropName.hashCode().toLong()
        val random = Random(pseudoSeed)
        val list = mutableListOf<CropPricePoint>()

        var currentPrice = basePrice
        for (i in 1..days) {
            val dateLabel = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            // Natural cyclical fluctuation
            val wave = kotlin.math.sin(i * 0.35) * volatility * 0.6
            val randomJitter = (random.nextDouble() - 0.46) * volatility
            currentPrice = (basePrice + wave + (i * trendStep) + randomJitter).coerceAtLeast(8.0)
            val roundedModal = (currentPrice * 10).roundToInt() / 10.0

            val minPrice = ((roundedModal - (volatility * 0.6)).coerceAtLeast(roundedModal * 0.8) * 10).roundToInt() / 10.0
            val maxPrice = ((roundedModal + (volatility * 0.7)) * 10).roundToInt() / 10.0
            val volume = 20.0 + (random.nextDouble() * 80.0)

            list.add(
                CropPricePoint(
                    dayNumber = i,
                    dateString = dateLabel,
                    modalPrice = roundedModal,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    arrivalTonnes = (volume * 10).roundToInt() / 10.0
                )
            )
        }
        return list
    }
}

/**
 * Recharts/D3-style Interactive Price Trend Chart for Android Jetpack Compose.
 *
 * Visualizes 30-day APMC modal market price fluctuations with:
 * - Smooth Bézier curve area chart
 * - Dynamic gradient fill under the line
 * - Interactive touch scrubber with floating tooltip
 * - Highest, Lowest, and Average 30-day price markers
 * - Farmer pricing recommendation engine
 */
@Composable
fun CropPriceTrendChart(
    cropName: String,
    modifier: Modifier = Modifier,
    farmerAskingPrice: Double? = null,
    initialDays: Int = 30,
    showCropSelector: Boolean = false,
    onCropChange: ((String) -> Unit)? = null
) {
    var selectedTimeframe by remember { mutableIntStateOf(initialDays) }
    var selectedCrop by remember(cropName) { mutableStateOf(cropName) }

    val rawData = remember(selectedCrop, selectedTimeframe) {
        CropPriceHistoryService.generate30DayPriceHistory(selectedCrop, selectedTimeframe)
    }

    val minModal = remember(rawData) { rawData.minOfOrNull { it.modalPrice } ?: 10.0 }
    val maxModal = remember(rawData) { rawData.maxOfOrNull { it.modalPrice } ?: 50.0 }
    val avgModal = remember(rawData) { (rawData.map { it.modalPrice }.average() * 10).roundToInt() / 10.0 }
    val firstPrice = remember(rawData) { rawData.firstOrNull()?.modalPrice ?: 10.0 }
    val latestPrice = remember(rawData) { rawData.lastOrNull()?.modalPrice ?: 10.0 }
    val percentageChange = remember(firstPrice, latestPrice) {
        if (firstPrice > 0) ((latestPrice - firstPrice) / firstPrice * 100) else 0.0
    }

    // Touch interaction state
    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    val activePoint = touchedIndex?.let { rawData.getOrNull(it) } ?: rawData.lastOrNull()

    // Smooth reveal animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedCrop, selectedTimeframe) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("crop_price_trend_chart_card_${selectedCrop.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Crop badge & 30-Day Tag
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
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "$selectedCrop 30-Day Price Trend",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "APMC Mandi Modal Price • D3/Recharts Analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (percentageChange >= 0) Color(0xFFDCFCE7) else Color(0xFFFFE4E6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (percentageChange >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (percentageChange >= 0) GreenPrimary else SpoilageRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${if (percentageChange >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", percentageChange)}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (percentageChange >= 0) GreenPrimary else SpoilageRed
                            )
                        )
                    }
                }
            }

            // Optional Crop Selector Row if enabled
            if (showCropSelector) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Tomato", "Red Onion", "Wheat", "Pomegranate", "Desi Chana", "Potato", "Green Chilli").forEach { crop ->
                        val isSelected = selectedCrop.contains(crop, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCrop = crop
                                onCropChange?.invoke(crop)
                            },
                            label = { Text(crop, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Timeframe Selector Chips (7D, 14D, 30D)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historical Horizon:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7 to "7 Days", 14 to "14 Days", 30 to "30 Days").forEach { (days, label) ->
                        val isSelected = selectedTimeframe == days
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surface,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier
                                .clickable {
                                    selectedTimeframe = days
                                    touchedIndex = null
                                }
                                .testTag("timeframe_chip_${days}d")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Live Interactive Tooltip Inspector
            activePoint?.let { point ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("chart_active_point_tooltip")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${point.dateString} (Day ${point.dayNumber})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Mandi Modal: ₹${point.modalPrice}/kg",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Range: ₹${point.minPrice} - ₹${point.maxPrice}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Arrival: ${point.arrivalTonnes} Tonnes",
                                style = MaterialTheme.typography.labelSmall,
                                color = EcoTeal
                            )
                        }
                    }
                }
            }

            // Canvas Chart Viewport (Bézier Curve + Gradient Area Fill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .pointerInput(rawData) {
                        detectTapGestures { offset ->
                            val widthPerPoint = size.width / (rawData.size - 1).coerceAtLeast(1)
                            val index = ((offset.x / widthPerPoint).roundToInt()).coerceIn(0, rawData.size - 1)
                            touchedIndex = index
                        }
                    }
                    .pointerInput(rawData) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val widthPerPoint = size.width / (rawData.size - 1).coerceAtLeast(1)
                            val index = ((change.position.x / widthPerPoint).roundToInt()).coerceIn(0, rawData.size - 1)
                            touchedIndex = index
                        }
                    }
                    .testTag("recharts_bezier_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (rawData.size < 2) return@Canvas

                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val paddingBottom = 20.dp.toPx()
                    val paddingTop = 12.dp.toPx()
                    val usableHeight = canvasHeight - paddingBottom - paddingTop

                    val pricePadding = (maxModal - minModal).coerceAtLeast(2.0) * 0.15
                    val effectiveMin = (minModal - pricePadding).coerceAtLeast(0.0)
                    val effectiveMax = maxModal + pricePadding
                    val priceRange = (effectiveMax - effectiveMin).coerceAtLeast(1.0)

                    // 1. Draw Horizontal Grid Lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val gridY = paddingTop + (usableHeight / gridSteps) * i
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, gridY),
                            end = Offset(canvasWidth, gridY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // 2. Draw Optional Farmer Asking Price Benchmark Line
                    farmerAskingPrice?.let { askPrice ->
                        val askY = paddingTop + usableHeight - ((askPrice - effectiveMin) / priceRange * usableHeight).toFloat()
                        drawLine(
                            color = GoldPrimary,
                            start = Offset(0f, askY),
                            end = Offset(canvasWidth, askY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                        )
                    }

                    // 3. Compute Coordinates for Bézier Path
                    val points = rawData.mapIndexed { index, point ->
                        val x = (index.toFloat() / (rawData.size - 1)) * canvasWidth
                        val normalizedPrice = ((point.modalPrice - effectiveMin) / priceRange).toFloat()
                        val animatedY = paddingTop + usableHeight - (normalizedPrice * usableHeight * animProgress.value)
                        Offset(x, animatedY)
                    }

                    // 4. Construct Smooth Cubic Bézier Curve Path
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]
                            val p1 = points[i]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                        }
                    }

                    // 5. Construct Closed Gradient Fill Path (Area Chart)
                    val areaPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, canvasHeight - paddingBottom)
                        lineTo(points.first().x, canvasHeight - paddingBottom)
                        close()
                    }

                    // Draw Gradient Area Fill under the curve
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GreenPrimary.copy(alpha = 0.38f),
                                GreenLight.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = paddingTop,
                            endY = canvasHeight - paddingBottom
                        )
                    )

                    // Draw High-Resolution Curve Stroke
                    drawPath(
                        path = linePath,
                        color = GreenPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 6. Draw Min and Max Highlighting Dots
                    val minPoint = points.minByOrNull { rawData[points.indexOf(it)].modalPrice }
                    val maxPoint = points.maxByOrNull { rawData[points.indexOf(it)].modalPrice }

                    minPoint?.let {
                        drawCircle(color = SpoilageRed, radius = 4.dp.toPx(), center = it)
                        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = it)
                    }

                    maxPoint?.let {
                        drawCircle(color = GoldPrimary, radius = 5.dp.toPx(), center = it)
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = it)
                    }

                    // 7. Draw Active Touched Scrubber Indicator
                    touchedIndex?.let { idx ->
                        if (idx in points.indices) {
                            val touchedPoint = points[idx]
                            // Vertical tracking dashed line
                            drawLine(
                                color = GreenPrimary.copy(alpha = 0.7f),
                                start = Offset(touchedPoint.x, paddingTop),
                                end = Offset(touchedPoint.x, canvasHeight - paddingBottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )
                            // Outer pulsing glow circle
                            drawCircle(
                                color = GreenPrimary.copy(alpha = 0.25f),
                                radius = 9.dp.toPx(),
                                center = touchedPoint
                            )
                            // Core center dot
                            drawCircle(
                                color = GreenPrimary,
                                radius = 5.dp.toPx(),
                                center = touchedPoint
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = touchedPoint
                            )
                        }
                    }
                }
            }

            // X-Axis Date Range Indicators & Touch Hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rawData.firstOrNull()?.dateString} (30d ago)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Touch or drag to inspect day",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenPrimary
                    )
                }

                Text(
                    text = "Today (${rawData.lastOrNull()?.dateString})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 30-Day Metrics Summary Grid (Lowest, Average, Highest)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-Day Lowest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.1f", minModal)}/kg",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpoilageRed
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "30-Day Average",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.1f", avgModal)}/kg",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "30-Day Peak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.1f", maxModal)}/kg",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    )
                }
            }

            // Farmer Pricing Decision Advisor Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GoldLight.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF78350F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Farmer Pricing Recommendation / मूल्य निर्धारण सलाह",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        )
                    }

                    val recommendedPrice = (latestPrice * 1.08).roundToInt()
                    val adviceText = if (percentageChange >= 0) {
                        "Prices trended up +${String.format(Locale.getDefault(), "%.1f", percentageChange)}% over 30 days. Recommended asking price: ₹$recommendedPrice/kg. Buyers on KrishiFlow are active at direct farmgate rates with zero commission."
                    } else {
                        "Prices stabilized near ₹$latestPrice/kg. Recommend setting asking price around ₹$recommendedPrice/kg to match instant procurement contracts and prevent storage spoilage."
                    }

                    Text(
                        text = adviceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF713F12)
                    )
                }
            }
        }
    }
}
