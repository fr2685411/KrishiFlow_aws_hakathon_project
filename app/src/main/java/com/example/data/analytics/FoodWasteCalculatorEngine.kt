package com.example.data.analytics

import com.example.data.model.CropCategory
import com.example.data.model.HarvestHistoryLogEntity
import com.example.data.model.ProduceBatchEntity
import kotlin.math.roundToInt

/**
 * Waste Severity Level
 */
enum class WasteSeverity(val label: String, val colorHex: Long) {
    MINIMAL("Controlled (<5%)", 0xFF16A34A),
    MODERATE("Moderate Risk (5-15%)", 0xFFF59E0B),
    SEVERE("High Waste Risk (>15%)", 0xFFDC2626)
}

/**
 * Waste Prevention Tip Category
 */
enum class ActionableTipCategory(val iconTitle: String) {
    STORAGE("Cold Chain & Ventilation"),
    DISCOUNT("Dynamic Margin Clearance"),
    FOOD_PROCESSING("Solar Dehydration & Processing"),
    BUYER_MATCH("Fast-Track Mandi / Vendor Routing"),
    HARVEST_TIMING("Staggered Harvesting Schedule")
}

/**
 * Actionable recommendation tailored to a specific crop and surplus vulnerability
 */
data class FoodWasteActionableTip(
    val title: String,
    val category: ActionableTipCategory,
    val description: String,
    val expectedLossReductionPct: Int,
    val targetCrop: String,
    val urgencyLevel: String // "Immediate (24h)", "Next Harvest", "Storage Upgrade"
)

/**
 * Per-crop breakdown of historical harvest vs. sales, surplus loss, and potential waste
 */
data class CropWasteAnalysis(
    val cropName: String,
    val category: CropCategory,
    val totalHarvestedKg: Double,
    val totalSoldKg: Double,
    val historicalRecordedLossKg: Double,
    val unsoldSurplusKg: Double,
    val potentialWastePercentage: Double,
    val financialLossAtRiskRupees: Double,
    val co2EmissionBurdenKg: Double, // Approx 1.9 kg CO2e per kg organic waste dumped
    val severity: WasteSeverity,
    val primaryLossCause: String,
    val currentActiveStockKg: Double = 0.0,
    val actionableTips: List<FoodWasteActionableTip>
)

/**
 * Complete Waste Analytics Report covering all crops
 */
data class PotentialFoodWasteReport(
    val totalHistoricalHarvestKg: Double,
    val totalHistoricalSoldKg: Double,
    val totalHistoricalUnsoldKg: Double,
    val overallWastePercentage: Double,
    val totalFinancialLossRupees: Double,
    val totalCo2WasteKg: Double,
    val overallSeverity: WasteSeverity,
    val topAtRiskCrop: String,
    val cropBreakdowns: List<CropWasteAnalysis>,
    val highPriorityActionableTips: List<FoodWasteActionableTip>
)

/**
 * Calculator Engine that analyzes historical sales vs. harvest records,
 * cross-references active farm batches, and computes potential food waste
 * with actionable prevention guidelines.
 */
object FoodWasteCalculatorEngine {

    fun calculatePotentialWaste(
        logs: List<HarvestHistoryLogEntity>,
        activeBatches: List<ProduceBatchEntity> = emptyList()
    ): PotentialFoodWasteReport {
        if (logs.isEmpty()) {
            return generateDefaultDemoReport(activeBatches)
        }

        val cropAnalyses = mutableListOf<CropWasteAnalysis>()

        val groupedByCrop = logs.groupBy { it.cropName }

        for ((crop, cropLogs) in groupedByCrop) {
            val totalHarvested = cropLogs.sumOf { it.harvestQuantityKg }
            val totalSold = cropLogs.sumOf { it.quantitySoldKg }
            val recordedStorageLoss = cropLogs.sumOf { it.storageLossKg }
            val avgPrice = if (totalSold > 0) cropLogs.sumOf { it.totalRevenueRupees } / totalSold else 25.0

            // Unsold Surplus = Harvested - Sold (or recorded storage loss if higher)
            val unsoldSurplus = (totalHarvested - totalSold).coerceAtLeast(recordedStorageLoss).coerceAtLeast(0.0)
            val wastePercentage = if (totalHarvested > 0) (unsoldSurplus / totalHarvested) * 100.0 else 0.0

            val financialLoss = unsoldSurplus * avgPrice
            val co2Burden = unsoldSurplus * 1.9 // EPA / FAO food waste emission factor

            val severity = when {
                wastePercentage >= 15.0 -> WasteSeverity.SEVERE
                wastePercentage >= 5.0 -> WasteSeverity.MODERATE
                else -> WasteSeverity.MINIMAL
            }

            val matchingActiveStock = activeBatches
                .filter { it.cropName.contains(crop, ignoreCase = true) }
                .sumOf { it.availableQuantityKg }

            val primaryCause = determineLossCause(crop, wastePercentage, cropLogs.firstOrNull()?.category ?: CropCategory.VEGETABLES)
            val tips = generateActionableTips(crop, wastePercentage, matchingActiveStock, avgPrice)

            cropAnalyses.add(
                CropWasteAnalysis(
                    cropName = crop,
                    category = cropLogs.firstOrNull()?.category ?: CropCategory.VEGETABLES,
                    totalHarvestedKg = totalHarvested,
                    totalSoldKg = totalSold,
                    historicalRecordedLossKg = recordedStorageLoss,
                    unsoldSurplusKg = unsoldSurplus,
                    potentialWastePercentage = (wastePercentage * 10.0).roundToInt() / 10.0,
                    financialLossAtRiskRupees = (financialLoss * 10.0).roundToInt() / 10.0,
                    co2EmissionBurdenKg = (co2Burden * 10.0).roundToInt() / 10.0,
                    severity = severity,
                    primaryLossCause = primaryCause,
                    currentActiveStockKg = matchingActiveStock,
                    actionableTips = tips
                )
            )
        }

        // Aggregate overall totals
        val totalHarvest = cropAnalyses.sumOf { it.totalHarvestedKg }
        val totalSold = cropAnalyses.sumOf { it.totalSoldKg }
        val totalUnsold = cropAnalyses.sumOf { it.unsoldSurplusKg }
        val totalLossRs = cropAnalyses.sumOf { it.financialLossAtRiskRupees }
        val totalCo2 = cropAnalyses.sumOf { it.co2EmissionBurdenKg }
        val overallPct = if (totalHarvest > 0) (totalUnsold / totalHarvest) * 100.0 else 0.0

        val overallSev = when {
            overallPct >= 15.0 -> WasteSeverity.SEVERE
            overallPct >= 5.0 -> WasteSeverity.MODERATE
            else -> WasteSeverity.MINIMAL
        }

        val topCrop = cropAnalyses.maxByOrNull { it.unsoldSurplusKg }?.cropName ?: "Produce"
        val topTips = cropAnalyses.flatMap { it.actionableTips }
            .distinctBy { it.title }
            .sortedByDescending { it.expectedLossReductionPct }
            .take(5)

        return PotentialFoodWasteReport(
            totalHistoricalHarvestKg = totalHarvest,
            totalHistoricalSoldKg = totalSold,
            totalHistoricalUnsoldKg = totalUnsold,
            overallWastePercentage = (overallPct * 10.0).roundToInt() / 10.0,
            totalFinancialLossRupees = (totalLossRs * 10.0).roundToInt() / 10.0,
            totalCo2WasteKg = (totalCo2 * 10.0).roundToInt() / 10.0,
            overallSeverity = overallSev,
            topAtRiskCrop = topCrop,
            cropBreakdowns = cropAnalyses.sortedByDescending { it.unsoldSurplusKg },
            highPriorityActionableTips = topTips
        )
    }

    private fun determineLossCause(cropName: String, wastePct: Double, category: CropCategory): String {
        val lower = cropName.lowercase()
        return when {
            lower.contains("tomato") -> "Ethylene gas buildup and crate compression causing 48-hour soft rot in transit."
            lower.contains("onion") -> "Sprouting & neck rot caused by ambient monsoon humidity exceeding 70% in chawl sheds."
            lower.contains("wheat") -> "Moisture condensation & khapra beetle infestation in unlined gunny sacks."
            lower.contains("chilli") -> "Rapid desiccation and fungal anthracnose lesions under direct sun."
            category == CropCategory.FRUITS -> "Bruising during unpadded loading and premature pulp softening."
            else -> "Market arrival mismatch: surplus harvested during mandi gluts with low local buyer absorption."
        }
    }

    fun generateActionableTips(
        cropName: String,
        wastePercentage: Double,
        activeStockKg: Double,
        pricePerKg: Double
    ): List<FoodWasteActionableTip> {
        val tips = mutableListOf<FoodWasteActionableTip>()
        val lower = cropName.lowercase()

        // 1. Immediate Clearance / Margin Clearance Tip
        tips.add(
            FoodWasteActionableTip(
                title = "Zero-Waste Quick Auction / Dynamic Discount",
                category = ActionableTipCategory.DISCOUNT,
                description = "For batches with under 4 days shelf-life, offer 10-15% bulk discounts on KrishiFlow Vyapar to local sauce processors, catering units, and mandi aggregators before spoilage sets in.",
                expectedLossReductionPct = 65,
                targetCrop = cropName,
                urgencyLevel = "Immediate (24-48h)"
            )
        )

        // 2. Storage & Preservation Guidelines
        if (lower.contains("tomato")) {
            tips.add(
                FoodWasteActionableTip(
                    title = "Ventilated Plastic Crates & Shaded Stacking",
                    category = ActionableTipCategory.STORAGE,
                    description = "Eliminate wooden boxes and jute bags. Stack in 4-tier ventilated plastic crates at 12-15°C. Never stack ripe tomatoes above 3 tiers to avoid base skin rupture.",
                    expectedLossReductionPct = 40,
                    targetCrop = cropName,
                    urgencyLevel = "Storage Upgrade"
                )
            )
            tips.add(
                FoodWasteActionableTip(
                    title = "Solar Dehydration & Puree Conversion",
                    category = ActionableTipCategory.FOOD_PROCESSING,
                    description = "Divert Grade-B soft surplus into village solar poly-dryers for sun-dried tomato halves or boil into rustic puree (shelf life extends from 5 days to 8 months).",
                    expectedLossReductionPct = 85,
                    targetCrop = cropName,
                    urgencyLevel = "Value Addition"
                )
            )
        } else if (lower.contains("onion")) {
            tips.add(
                FoodWasteActionableTip(
                    title = "Raised Bamboo Chawl with False Bottom",
                    category = ActionableTipCategory.STORAGE,
                    description = "Store harvested Nashik Garwa onions on raised slatted bamboo platforms with 6-inch bottom air clearance. Ensure relative humidity remains under 65% to halt sprouting.",
                    expectedLossReductionPct = 50,
                    targetCrop = cropName,
                    urgencyLevel = "Storage Upgrade"
                )
            )
            tips.add(
                FoodWasteActionableTip(
                    title = "Onion Flakes & Powder Processing",
                    category = ActionableTipCategory.FOOD_PROCESSING,
                    description = "Thin-slice smaller (<35mm) surplus onions and dehydrate into export-grade flakes, selling to spice blenders at 4x raw farmgate rate.",
                    expectedLossReductionPct = 90,
                    targetCrop = cropName,
                    urgencyLevel = "Value Addition"
                )
            )
        } else {
            tips.add(
                FoodWasteActionableTip(
                    title = "Airtight Hermetic Grain Bags (PICS)",
                    category = ActionableTipCategory.STORAGE,
                    description = "Store dried grains and pulses in multi-layer hermetic bags (PICS) with moisture strictly under 10%. Suffocates weevils and mould without toxic fumigants.",
                    expectedLossReductionPct = 80,
                    targetCrop = cropName,
                    urgencyLevel = "Storage Upgrade"
                )
            )
        }

        // 3. Staggered Harvest Plan
        tips.add(
            FoodWasteActionableTip(
                title = "Staggered Morning Plucking Schedule",
                category = ActionableTipCategory.HARVEST_TIMING,
                description = "Pluck produce between 5:30 AM and 8:30 AM before field heat sets in. Pre-cool under shaded shed before transit to reduce field heat by 8°C and double transit vitality.",
                expectedLossReductionPct = 35,
                targetCrop = cropName,
                urgencyLevel = "Next Harvest"
            )
        )

        return tips
    }

    private fun generateDefaultDemoReport(activeBatches: List<ProduceBatchEntity>): PotentialFoodWasteReport {
        val tomatoStock = activeBatches.firstOrNull { it.cropName.contains("Tomato", ignoreCase = true) }?.availableQuantityKg ?: 1200.0
        val onionStock = activeBatches.firstOrNull { it.cropName.contains("Onion", ignoreCase = true) }?.availableQuantityKg ?: 4000.0
        val wheatStock = activeBatches.firstOrNull { it.cropName.contains("Wheat", ignoreCase = true) }?.availableQuantityKg ?: 6000.0

        val breakdowns = listOf(
            CropWasteAnalysis(
                cropName = "Hybrid Tomato",
                category = CropCategory.VEGETABLES,
                totalHarvestedKg = 18000.0,
                totalSoldKg = 15200.0,
                historicalRecordedLossKg = 1600.0,
                unsoldSurplusKg = 2800.0,
                potentialWastePercentage = 15.6,
                financialLossAtRiskRupees = 78400.0,
                co2EmissionBurdenKg = 5320.0,
                severity = WasteSeverity.SEVERE,
                primaryLossCause = "Ethylene gas buildup and crate compression causing 48-hour soft rot in transit.",
                currentActiveStockKg = tomatoStock,
                actionableTips = generateActionableTips("Hybrid Tomato", 15.6, tomatoStock, 28.0)
            ),
            CropWasteAnalysis(
                cropName = "Red Onion",
                category = CropCategory.VEGETABLES,
                totalHarvestedKg = 24000.0,
                totalSoldKg = 21800.0,
                historicalRecordedLossKg = 1400.0,
                unsoldSurplusKg = 2200.0,
                potentialWastePercentage = 9.2,
                financialLossAtRiskRupees = 55000.0,
                co2EmissionBurdenKg = 4180.0,
                severity = WasteSeverity.MODERATE,
                primaryLossCause = "Sprouting & neck rot caused by ambient monsoon humidity exceeding 70% in chawl sheds.",
                currentActiveStockKg = onionStock,
                actionableTips = generateActionableTips("Red Onion", 9.2, onionStock, 25.0)
            ),
            CropWasteAnalysis(
                cropName = "Sharbati Wheat",
                category = CropCategory.GRAINS,
                totalHarvestedKg = 12000.0,
                totalSoldKg = 11600.0,
                historicalRecordedLossKg = 250.0,
                unsoldSurplusKg = 400.0,
                potentialWastePercentage = 3.3,
                financialLossAtRiskRupees = 14000.0,
                co2EmissionBurdenKg = 760.0,
                severity = WasteSeverity.MINIMAL,
                primaryLossCause = "Moisture condensation & khapra beetle infestation in unlined gunny sacks.",
                currentActiveStockKg = wheatStock,
                actionableTips = generateActionableTips("Sharbati Wheat", 3.3, wheatStock, 35.0)
            )
        )

        val totalHarvest = breakdowns.sumOf { it.totalHarvestedKg }
        val totalSold = breakdowns.sumOf { it.totalSoldKg }
        val totalUnsold = breakdowns.sumOf { it.unsoldSurplusKg }
        val totalLoss = breakdowns.sumOf { it.financialLossAtRiskRupees }
        val totalCo2 = breakdowns.sumOf { it.co2EmissionBurdenKg }

        return PotentialFoodWasteReport(
            totalHistoricalHarvestKg = totalHarvest,
            totalHistoricalSoldKg = totalSold,
            totalHistoricalUnsoldKg = totalUnsold,
            overallWastePercentage = ((totalUnsold / totalHarvest) * 1000.0).roundToInt() / 10.0,
            totalFinancialLossRupees = totalLoss,
            totalCo2WasteKg = totalCo2,
            overallSeverity = WasteSeverity.MODERATE,
            topAtRiskCrop = "Hybrid Tomato",
            cropBreakdowns = breakdowns,
            highPriorityActionableTips = breakdowns.flatMap { it.actionableTips }.distinctBy { it.title }.take(5)
        )
    }
}
