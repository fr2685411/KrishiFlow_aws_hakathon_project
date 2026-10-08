package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Agronomic parameters affecting crop suitability, yield production, and ecological balance.
 */
enum class SoilType(val displayName: String, val hindiName: String, val description: String) {
    BLACK_CLAY(
        displayName = "Black Cotton (Regur)",
        hindiName = "काली चिकनी मिट्टी",
        description = "High moisture retention, rich in calcium/magnesium, ideal for cotton, pulses, soybean, wheat, onion."
    ),
    ALLUVIAL_LOAM(
        displayName = "Alluvial Loam",
        hindiName = "जलोढ़ दोमट मिट्टी",
        description = "Highly fertile river silt, balanced porosity, excellent for grains, vegetables, mustard, potato."
    ),
    RED_LOAMY(
        displayName = "Red Loamy",
        hindiName = "लाल दोमट मिट्टी",
        description = "Well-drained, porous, rich in iron, responsive to organic manures; ideal for groundnut, millets, pulses, tomato."
    ),
    SANDY_LOAM(
        displayName = "Sandy Loam",
        hindiName = "बलुई दोमट मिट्टी",
        description = "Rapid drainage, warms quickly, lower water capacity; ideal for cucurbits, carrots, groundnut, tuber crops."
    ),
    LATERITE(
        displayName = "Laterite / Hill Soil",
        hindiName = "लेटराइट मिट्टी",
        description = "Porous, acidic, leached; responsive to lime and organic mulches; suitable for ginger, turmeric, cashew, millets."
    )
}

enum class AgroSeason(val displayName: String, val hindiName: String, val months: String) {
    KHARIF("Kharif (Monsoon)", "खरीफ (मानसून)", "June – October"),
    RABI("Rabi (Winter)", "रबी (सर्दियां)", "October – March"),
    ZAID("Zaid (Summer)", "जायद (गर्मी)", "March – June")
}

enum class IrrigationSource(val displayName: String, val hindiName: String, val waterReliability: String) {
    RAINFED("Rainfed Only", "केवल वर्षा आधारित", "Low water buffer; depends on monsoon regularity"),
    LIMITED_WELL("Limited Borewell / Dug Well", "सीमित बोरवेल / कुआं", "Moderate buffer; 2-4 hours irrigation/day"),
    DRIP_MICRO("Drip / Micro-Irrigation", "ड्रिप / टपक सिंचाई", "High efficiency; 40-50% water saving with fertigation"),
    CANAL_PERENNIAL("Canal / River Lifting", "नहर / बारहमासी जल", "Abundant surface water; suited for high-demand crops")
}

enum class PreviousCropType(
    val displayName: String,
    val hindiName: String,
    val residualEffect: String,
    val naturalOrderCategory: NaturalOrderCategory
) {
    HEAVY_FEEDER_GRAIN(
        displayName = "Cereal / Sugarcane / Maize",
        hindiName = "मक्का / गन्ना / गेहूं",
        residualEffect = "Soil depleted in Nitrogen, Phosphorus, and biological organic carbon.",
        naturalOrderCategory = NaturalOrderCategory.REQUIRES_RESTITUTION
    ),
    SOLANACEOUS_VEG(
        displayName = "Tomato / Potato / Chilli / Eggplant",
        hindiName = "टमाटर / आलू / मिर्च",
        residualEffect = "Soil harbors persistent wilt/blight fungi and root-knot nematodes.",
        naturalOrderCategory = NaturalOrderCategory.HIGH_PEST_CARRYOVER
    ),
    LEGUME_PULSE(
        displayName = "Pulses (Chickpea / Soybean / Moong)",
        hindiName = "दलहन (चना / सोयाबीन / मूंग)",
        residualEffect = "Soil biologically enriched with symbiotic atmospheric nitrogen nodules.",
        naturalOrderCategory = NaturalOrderCategory.NITROGEN_ENRICHED
    ),
    BRASSICA_OILSEED(
        displayName = "Mustard / Oilseed",
        hindiName = "सरसों / तिलहन",
        residualEffect = "Glucosinolate breakdown acts as a natural soil biofumigant.",
        naturalOrderCategory = NaturalOrderCategory.BIOFUMIGATED
    ),
    FALLOW_GREEN_MANURE(
        displayName = "Fallow / Dhaincha Green Manure",
        hindiName = "परती / ढैंचा हरी खाद",
        residualEffect = "Highest soil biological fertility and restored organic active carbon.",
        naturalOrderCategory = NaturalOrderCategory.RESTORED_PRISTINE
    )
}

enum class NaturalOrderCategory {
    REQUIRES_RESTITUTION,
    HIGH_PEST_CARRYOVER,
    NITROGEN_ENRICHED,
    BIOFUMIGATED,
    RESTORED_PRISTINE
}

enum class CropRiskLevel(val label: String, val colorHex: Long) {
    LOW("Low Risk (सुरक्षित)", 0xFF15803D),
    MODERATE("Moderate Risk (मध्यम जोखिम)", 0xFFD97706),
    HIGH("High Risk (सावधानी आवश्यक)", 0xFFDC2626)
}

/**
 * Detailed crop plan recommendations produced by the Natural Order Planning Engine.
 */
data class CropRecommendation(
    val cropName: String,
    val hindiName: String,
    val category: CropCategory,
    val suitableVariety: String,
    val maturityDays: Int,
    val naturalHarmonyScore: Int, // 0 to 100 score indicating alignment with ecological order
    val naturalHarmonyVerdict: String,
    val expectedYieldPerAcreQuintals: Double,
    val totalEstimatedProductionQuintals: Double,
    val totalEstimatedProductionTonnes: Double,
    val expectedMarketRatePerQuintal: Double,
    val estimatedGrossRevenueRupees: Double,
    val estimatedInputCostRupees: Double,
    val estimatedNetProfitRupees: Double,
    val overallRiskLevel: CropRiskLevel,
    val weatherRiskPercent: Int,
    val pestRiskPercent: Int,
    val marketGlutRiskPercent: Int,
    val waterDepletionRiskPercent: Int,
    val naturalOrderPreservationPlan: NaturalOrderPreservationPlan
)

/**
 * Regenerative, biological solutions to preserve natural order and restore agro-ecosystem health.
 */
data class NaturalOrderPreservationPlan(
    val rotationBenefitSummary: String,
    val biologicalSoilRemedy: String,
    val companionPlantingTrapCrop: String,
    val polycultureIntercropRecipe: String,
    val waterConservationMethod: String,
    val naturalPestMitigation: String
)
