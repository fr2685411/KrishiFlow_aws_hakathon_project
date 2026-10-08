package com.example.data.calculator

import com.example.data.model.SoilType
import kotlin.math.roundToInt

enum class CropCategoryType(val displayName: String, val hindiName: String) {
    VEGETABLES("Vegetables & Horticulture", "सब्जियां एवं बागवानी"),
    PULSES_OILSEEDS("Pulses & Oilseeds", "दलहन एवं तिलहन"),
    GRAINS_CEREALS("Grains & Cereals", "अनाज एवं खाद्यान्न"),
    CASH_CROPS("Cash & Commercial Crops", "व्यावसायिक एवं नकदी फसलें")
}

data class CalculatorCrop(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: CropCategoryType,
    val baseYieldQuintalsPerAcre: Double,
    val benchmarkMarketRateRupeesPerQuintal: Double,
    val averageCostPerAcreRupees: Double,
    val seedRequirementPerAcreKg: Double,
    val packagingUnitName: String, // e.g., "50kg Gunny Bags", "20kg Crates"
    val packagingUnitCapacityKg: Double,
    val maturityDays: Int,
    val preferredSoil: SoilType
)

enum class SoilOrganicCarbonGrade(
    val displayName: String,
    val hindiName: String,
    val multiplier: Double,
    val carbonPercentText: String,
    val description: String
) {
    GRADE_A_HIGH(
        displayName = "High Fertility (>0.8% OC)",
        hindiName = "उत्कृष्ट जैविक कार्बन (उपजाऊ)",
        multiplier = 1.20,
        carbonPercentText = ">0.8% Organic Carbon",
        description = "Rich microbial population, active earthworm activity, and superior cation exchange capacity (+20% yield bonus)."
    ),
    GRADE_B_MEDIUM(
        displayName = "Medium Fertility (0.5% - 0.8% OC)",
        hindiName = "मध्यम जैविक कार्बन (संतुलित)",
        multiplier = 1.00,
        carbonPercentText = "0.5% - 0.8% Organic Carbon",
        description = "Balanced soil health; standard agricultural productivity baseline (100% standard yield)."
    ),
    GRADE_C_DEGRADED(
        displayName = "Low / Degraded (<0.5% OC)",
        hindiName = "कम जैविक कार्बन (सुधार आवश्यक)",
        multiplier = 0.80,
        carbonPercentText = "<0.5% Organic Carbon",
        description = "Compacted or leached soil with poor moisture holding. Needs bio-fertilizer and compost recharge (-20% yield reduction)."
    )
}

enum class CalculatorIrrigationType(
    val displayName: String,
    val hindiName: String,
    val multiplier: Double,
    val waterSavingDescription: String
) {
    DRIP_FERTIGATION("Drip Micro-Fertigation", "ड्रिप एवं फर्टिगेशन", 1.15, "Precise root-zone moisture; +15% yield and 45% water conservation."),
    SURFACE_CANAL("Canal / Flood Furrow", "नहर / नाली सिंचाई", 1.00, "Standard surface irrigation; 100% baseline water availability."),
    RAINFED_WELL("Rainfed / Limited Well", "वर्षा आधारित / सीमित कुआं", 0.75, "Moisture-constrained; vulnerable to rainfall dry spells (-25% yield).")
}

data class HarvestYieldEstimate(
    val crop: CalculatorCrop,
    val landSizeAcres: Double,
    val soilType: SoilType,
    val organicCarbonGrade: SoilOrganicCarbonGrade,
    val irrigationType: CalculatorIrrigationType,
    val effectiveYieldPerAcreQuintals: Double,
    val totalProductionQuintals: Double,
    val totalProductionTonnes: Double,
    val totalProductionKg: Double,
    val yieldRangeMinQuintals: Double,
    val yieldRangeMaxQuintals: Double,
    val grossMarketValueRupees: Double,
    val totalCultivationCostRupees: Double,
    val netFarmerProfitRupees: Double,
    val roiPercent: Int,
    val totalPackagesNeeded: Int,
    val seedQuantityNeededKg: Double,
    val co2SequestrationKg: Double,
    val soilQualityImpactSummary: String
)

object HarvestYieldCalculator {

    val CROPS: List<CalculatorCrop> = listOf(
        CalculatorCrop(
            id = "crop_tomato",
            name = "Hybrid Tomato",
            hindiName = "टमाटर (अभिनव / वैष्णवी)",
            category = CropCategoryType.VEGETABLES,
            baseYieldQuintalsPerAcre = 210.0,
            benchmarkMarketRateRupeesPerQuintal = 2400.0,
            averageCostPerAcreRupees = 42000.0,
            seedRequirementPerAcreKg = 0.15,
            packagingUnitName = "25kg Plastic Crates",
            packagingUnitCapacityKg = 25.0,
            maturityDays = 75,
            preferredSoil = SoilType.BLACK_CLAY
        ),
        CalculatorCrop(
            id = "crop_onion",
            name = "Nashik Dark Red Onion",
            hindiName = "नासिक लाल प्याज",
            category = CropCategoryType.VEGETABLES,
            baseYieldQuintalsPerAcre = 140.0,
            benchmarkMarketRateRupeesPerQuintal = 2600.0,
            averageCostPerAcreRupees = 32000.0,
            seedRequirementPerAcreKg = 4.0,
            packagingUnitName = "50kg Mesh Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 110,
            preferredSoil = SoilType.ALLUVIAL_LOAM
        ),
        CalculatorCrop(
            id = "crop_potato",
            name = "Table Potato (Kufri Jyoti)",
            hindiName = "आलू (कुफरी ज्योति)",
            category = CropCategoryType.VEGETABLES,
            baseYieldQuintalsPerAcre = 180.0,
            benchmarkMarketRateRupeesPerQuintal = 1750.0,
            averageCostPerAcreRupees = 38000.0,
            seedRequirementPerAcreKg = 800.0, // Seed tubers
            packagingUnitName = "50kg Jute Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 90,
            preferredSoil = SoilType.ALLUVIAL_LOAM
        ),
        CalculatorCrop(
            id = "crop_chilli",
            name = "Green Chilli (G-4 / Teja)",
            hindiName = "हरी मिर्च (तेजा / जी-4)",
            category = CropCategoryType.VEGETABLES,
            baseYieldQuintalsPerAcre = 70.0,
            benchmarkMarketRateRupeesPerQuintal = 4800.0,
            averageCostPerAcreRupees = 35000.0,
            seedRequirementPerAcreKg = 0.4,
            packagingUnitName = "25kg Gunny Bags",
            packagingUnitCapacityKg = 25.0,
            maturityDays = 85,
            preferredSoil = SoilType.RED_LOAMY
        ),
        CalculatorCrop(
            id = "crop_wheat",
            name = "Sharbati Wheat",
            hindiName = "शरबती गेहूं (लोक-1)",
            category = CropCategoryType.GRAINS_CEREALS,
            baseYieldQuintalsPerAcre = 22.0,
            benchmarkMarketRateRupeesPerQuintal = 2450.0,
            averageCostPerAcreRupees = 14500.0,
            seedRequirementPerAcreKg = 40.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 120,
            preferredSoil = SoilType.BLACK_CLAY
        ),
        CalculatorCrop(
            id = "crop_rice",
            name = "Basmati / Fine Paddy",
            hindiName = "बासमती / सुगन्धित धान",
            category = CropCategoryType.GRAINS_CEREALS,
            baseYieldQuintalsPerAcre = 19.0,
            benchmarkMarketRateRupeesPerQuintal = 3800.0,
            averageCostPerAcreRupees = 17000.0,
            seedRequirementPerAcreKg = 15.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 135,
            preferredSoil = SoilType.ALLUVIAL_LOAM
        ),
        CalculatorCrop(
            id = "crop_maize",
            name = "Yellow Maize / Corn",
            hindiName = "पीली मक्का",
            category = CropCategoryType.GRAINS_CEREALS,
            baseYieldQuintalsPerAcre = 30.0,
            benchmarkMarketRateRupeesPerQuintal = 2150.0,
            averageCostPerAcreRupees = 15000.0,
            seedRequirementPerAcreKg = 8.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 100,
            preferredSoil = SoilType.RED_LOAMY
        ),
        CalculatorCrop(
            id = "crop_chana",
            name = "Desi Chickpea (Chana)",
            hindiName = "देसी चना (फुले विक्रम)",
            category = CropCategoryType.PULSES_OILSEEDS,
            baseYieldQuintalsPerAcre = 9.5,
            benchmarkMarketRateRupeesPerQuintal = 5700.0,
            averageCostPerAcreRupees = 12500.0,
            seedRequirementPerAcreKg = 30.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 105,
            preferredSoil = SoilType.BLACK_CLAY
        ),
        CalculatorCrop(
            id = "crop_soybean",
            name = "Yellow Soybean",
            hindiName = "सोयाबीन (JS-9560)",
            category = CropCategoryType.PULSES_OILSEEDS,
            baseYieldQuintalsPerAcre = 11.0,
            benchmarkMarketRateRupeesPerQuintal = 4750.0,
            averageCostPerAcreRupees = 13500.0,
            seedRequirementPerAcreKg = 30.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 95,
            preferredSoil = SoilType.BLACK_CLAY
        ),
        CalculatorCrop(
            id = "crop_mustard",
            name = "Mustard (Pusa Bold)",
            hindiName = "पीली सरसों (पूसा बोल्ड)",
            category = CropCategoryType.PULSES_OILSEEDS,
            baseYieldQuintalsPerAcre = 8.5,
            benchmarkMarketRateRupeesPerQuintal = 5650.0,
            averageCostPerAcreRupees = 11000.0,
            seedRequirementPerAcreKg = 2.0,
            packagingUnitName = "50kg Gunny Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 110,
            preferredSoil = SoilType.ALLUVIAL_LOAM
        ),
        CalculatorCrop(
            id = "crop_cotton",
            name = "Bt Long Staple Cotton",
            hindiName = "कपास (लॉन्ग स्टेपल)",
            category = CropCategoryType.CASH_CROPS,
            baseYieldQuintalsPerAcre = 13.0,
            benchmarkMarketRateRupeesPerQuintal = 7200.0,
            averageCostPerAcreRupees = 24000.0,
            seedRequirementPerAcreKg = 1.0,
            packagingUnitName = "Bales / 50kg Bags",
            packagingUnitCapacityKg = 50.0,
            maturityDays = 160,
            preferredSoil = SoilType.BLACK_CLAY
        ),
        CalculatorCrop(
            id = "crop_sugarcane",
            name = "Sugarcane (Co-86032)",
            hindiName = "गन्ना (को-86032)",
            category = CropCategoryType.CASH_CROPS,
            baseYieldQuintalsPerAcre = 450.0, // 45 tonnes/acre
            benchmarkMarketRateRupeesPerQuintal = 340.0,
            averageCostPerAcreRupees = 55000.0,
            seedRequirementPerAcreKg = 2500.0, // Setts
            packagingUnitName = "Tractor Trollies",
            packagingUnitCapacityKg = 3000.0,
            maturityDays = 360,
            preferredSoil = SoilType.BLACK_CLAY
        )
    )

    fun calculate(
        crop: CalculatorCrop,
        landSizeAcres: Double,
        soilType: SoilType,
        organicCarbonGrade: SoilOrganicCarbonGrade,
        irrigationType: CalculatorIrrigationType
    ): HarvestYieldEstimate {
        val safeAcres = landSizeAcres.coerceIn(0.1, 500.0)

        // Soil texture affinity multiplier (comparing with preferred soil)
        val soilTextureMultiplier = when {
            crop.preferredSoil == soilType -> 1.10
            soilType == SoilType.BLACK_CLAY || soilType == SoilType.ALLUVIAL_LOAM -> 1.00
            soilType == SoilType.RED_LOAMY -> 0.95
            soilType == SoilType.SANDY_LOAM -> 0.88
            soilType == SoilType.LATERITE -> 0.85
            else -> 0.95
        }

        val organicCarbonMultiplier = organicCarbonGrade.multiplier
        val irrigationMultiplier = irrigationType.multiplier

        val totalCombinedMultiplier = soilTextureMultiplier * organicCarbonMultiplier * irrigationMultiplier
        val effectiveYieldPerAcre = crop.baseYieldQuintalsPerAcre * totalCombinedMultiplier

        val totalQuintals = effectiveYieldPerAcre * safeAcres
        val totalTonnes = totalQuintals / 10.0
        val totalKg = totalQuintals * 100.0

        val yieldMin = totalQuintals * 0.88
        val yieldMax = totalQuintals * 1.12

        val grossRevenue = totalQuintals * crop.benchmarkMarketRateRupeesPerQuintal
        val totalCost = crop.averageCostPerAcreRupees * safeAcres
        val netProfit = grossRevenue - totalCost

        val roi = if (totalCost > 0) ((netProfit / totalCost) * 100.0).roundToInt() else 0

        val packagesNeeded = (totalKg / crop.packagingUnitCapacityKg).roundToInt().coerceAtLeast(1)
        val seedNeeded = crop.seedRequirementPerAcreKg * safeAcres
        val co2Fixed = totalTonnes * 420.0

        val impactSummary = buildImpactSummary(
            crop = crop,
            soilType = soilType,
            ocGrade = organicCarbonGrade,
            irrigation = irrigationType,
            multiplier = totalCombinedMultiplier
        )

        return HarvestYieldEstimate(
            crop = crop,
            landSizeAcres = safeAcres,
            soilType = soilType,
            organicCarbonGrade = organicCarbonGrade,
            irrigationType = irrigationType,
            effectiveYieldPerAcreQuintals = (effectiveYieldPerAcre * 10).roundToInt() / 10.0,
            totalProductionQuintals = (totalQuintals * 10).roundToInt() / 10.0,
            totalProductionTonnes = (totalTonnes * 100).roundToInt() / 100.0,
            totalProductionKg = totalKg.roundToInt().toDouble(),
            yieldRangeMinQuintals = (yieldMin * 10).roundToInt() / 10.0,
            yieldRangeMaxQuintals = (yieldMax * 10).roundToInt() / 10.0,
            grossMarketValueRupees = grossRevenue.roundToInt().toDouble(),
            totalCultivationCostRupees = totalCost.roundToInt().toDouble(),
            netFarmerProfitRupees = netProfit.roundToInt().toDouble(),
            roiPercent = roi,
            totalPackagesNeeded = packagesNeeded,
            seedQuantityNeededKg = (seedNeeded * 10).roundToInt() / 10.0,
            co2SequestrationKg = co2Fixed.roundToInt().toDouble(),
            soilQualityImpactSummary = impactSummary
        )
    }

    private fun buildImpactSummary(
        crop: CalculatorCrop,
        soilType: SoilType,
        ocGrade: SoilOrganicCarbonGrade,
        irrigation: CalculatorIrrigationType,
        multiplier: Double
    ): String {
        val pctDiff = ((multiplier - 1.0) * 100.0).roundToInt()
        val prefix = if (pctDiff >= 0) "+$pctDiff%" else "$pctDiff%"

        return when {
            multiplier >= 1.25 -> "$prefix Peak Performance: High organic carbon (${ocGrade.carbonPercentText}) and precision ${irrigation.displayName} boost harvest yield significantly above baseline."
            multiplier >= 1.0 -> "$prefix Stable Yield: Balanced soil conditions and adequate water match state agricultural university benchmark yields."
            else -> "$prefix Sub-optimal Output: Nutrient depletion or water constraint reduces harvest potential. Applying organic vermicompost and drip lines can recover up to 25% yield."
        }
    }
}
