package com.example.data.advisory

import com.example.data.model.AgroSeason
import com.example.data.model.IrrigationSource
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType

data class AiCropRecommendation(
    val cropName: String,
    val hindiName: String,
    val agroClimaticMatchScore: Int,
    val aiReasoning: String,
    val expectedYieldQuintalsPerAcre: Double,
    val estimatedRevenuePerAcreRupees: Double,
    val estimatedCostPerAcreRupees: Double,
    val estimatedNetProfitPerAcreRupees: Double,
    val maturityDays: Int,
    val climateAndDroughtRisk: String,
    val pestAndDiseaseRisk: String,
    val marketGlutRisk: String,
    val sustainablePractices: List<String>,
    val naturalOrderPreservation: String
)

data class AiAdvisoryResult(
    val locationAnalyzed: String,
    val soilAnalyzed: String,
    val seasonAnalyzed: String,
    val weatherContext: String,
    val summaryVerdict: String,
    val recommendations: List<AiCropRecommendation>,
    val timestamp: Long = System.currentTimeMillis(),
    val isLiveAiGenerated: Boolean = true
)

data class CropAdvisoryAiInput(
    val soilType: SoilType,
    val season: AgroSeason,
    val irrigation: IrrigationSource,
    val previousCrop: PreviousCropType,
    val farmAcres: Double,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTemp: Double,
    val humidity: Int,
    val rainProbability: Int
)
