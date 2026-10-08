package com.example.data.advisory

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AgroSeason
import com.example.data.model.CropCategory
import com.example.data.model.CropRecommendation
import com.example.data.model.CropRiskLevel
import com.example.data.model.IrrigationSource
import com.example.data.model.NaturalOrderCategory
import com.example.data.model.PreviousCropType
import com.example.data.model.SoilType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@JsonClass(generateAdapter = true)
data class GeminiApiRequest(
    @Json(name = "contents") val contents: List<GeminiApiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiApiConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiContent(
    @Json(name = "parts") val parts: List<GeminiApiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiApiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiConfig(
    @Json(name = "temperature") val temperature: Float? = 0.3f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40
)

@JsonClass(generateAdapter = true)
data class GeminiApiResponse(
    @Json(name = "candidates") val candidates: List<GeminiApiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiCandidate(
    @Json(name = "content") val content: GeminiApiContent? = null
)

interface GeminiRestService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateCropAdvisory(
        @Query("key") apiKey: String,
        @Body request: GeminiApiRequest
    ): GeminiApiResponse
}

class GeminiCropAdvisoryService {

    private val apiService: GeminiRestService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(GeminiRestService::class.java)
    }

    suspend fun generateAdvisory(
        input: CropAdvisoryAiInput,
        customApiKey: String = ""
    ): AiAdvisoryResult = withContext(Dispatchers.IO) {
        val apiKey = if (customApiKey.isNotBlank()) {
            customApiKey
        } else {
            try {
                val buildKey = BuildConfig.GEMINI_API_KEY
                if (buildKey.isBlank() || buildKey == "DEFAULT_GEMINI_API_KEY" || buildKey == "MY_GEMINI_API_KEY" || buildKey == "DEFAULT_KEY") "" else buildKey
            } catch (e: Exception) {
                ""
            }
        }

        if (apiKey.isBlank() || apiKey == "DEFAULT_GEMINI_API_KEY") {
            logW("GeminiCropAdvisory", "Gemini API key is default or missing, generating expert agro-ecological synthesized advisory.")
            return@withContext generateSynthesizedAdvisory(input, isLiveAi = false)
        }

        val prompt = buildPrompt(input)
        val request = GeminiApiRequest(
            contents = listOf(
                GeminiApiContent(
                    parts = listOf(GeminiApiPart(text = prompt))
                )
            ),
            generationConfig = GeminiApiConfig(temperature = 0.3f)
        )

        try {
            val response = apiService.generateCropAdvisory(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (rawText.isNullOrBlank()) {
                return@withContext generateSynthesizedAdvisory(input, isLiveAi = false)
            }

            parseGeminiJsonResponse(rawText, input)
        } catch (e: Exception) {
            logE("GeminiCropAdvisory", "Gemini API call failed: ${e.message}", e)
            generateSynthesizedAdvisory(input, isLiveAi = false)
        }
    }

    private fun logW(tag: String, msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            Log.e(tag, msg, tr)
        } catch (_: Throwable) {
            println("[$tag] $msg: ${tr?.message}")
        }
    }

    private fun buildPrompt(input: CropAdvisoryAiInput): String {
        return """
            You are an expert Indian Agro-Climatologist and Soil Scientist for ICAR (Indian Council of Agricultural Research).
            Analyze the following farmer's specific land, location, and seasonal parameters to recommend the most optimal crops, assess risks, and specify sustainable practices:

            FARM PARAMETERS:
            - Location: ${input.locationName} (GPS Lat: ${input.latitude}, Lon: ${input.longitude})
            - Live Weather Context: Temp ${input.currentTemp}°C, Humidity ${input.humidity}%, Rain Probability ${input.rainProbability}%
            - Soil Texture: ${input.soilType.displayName} (${input.soilType.hindiName})
            - Sowing Season: ${input.season.displayName} (${input.season.hindiName} - ${input.season.months})
            - Irrigation Infrastructure: ${input.irrigation.displayName} (${input.irrigation.hindiName})
            - Preceding Crop Harvested: ${input.previousCrop.displayName} (${input.previousCrop.hindiName})
            - Land Size: ${input.farmAcres} Acres

            TASK REQUIREMENTS:
            1. Recommend 3 to 4 optimal crops suited to this soil, climate, and rotational natural order.
            2. For each crop, calculate expected yield (Quintals/Acre), financial revenue, cost, and net profit.
            3. Provide detailed multi-factor risk assessments (Drought/Climate, Pests & Pathogens, Market Price Glut).
            4. Provide specific non-chemical, sustainable & regenerative practices (biofertilizers, companion trap crops, mulching).
            5. Provide an overall summary verdict explaining why these crops preserve ecological balance and maximize farmer income.

            OUTPUT FORMAT:
            Return ONLY valid JSON matching this schema (do NOT wrap with markdown code fences):
            {
              "summaryVerdict": "Comprehensive ecological analysis and recommendation rationale...",
              "recommendations": [
                {
                  "cropName": "Crop Name (e.g. Desi Chickpea / Chana)",
                  "hindiName": "फसल का नाम (हिंदी)",
                  "agroClimaticMatchScore": 95,
                  "aiReasoning": "Detailed scientific reason matching soil texture and season...",
                  "expectedYieldQuintalsPerAcre": 9.5,
                  "estimatedRevenuePerAcreRupees": 54000,
                  "estimatedCostPerAcreRupees": 18000,
                  "estimatedNetProfitPerAcreRupees": 36000,
                  "maturityDays": 105,
                  "climateAndDroughtRisk": "Low - High drought tolerance with deep taproot",
                  "pestAndDiseaseRisk": "Moderate - Monitor for pod borer with delta traps",
                  "marketGlutRisk": "Low - High domestic pulse demand and MSP support",
                  "sustainablePractices": [
                    "Rhizobium biological seed inoculation",
                    "Intercrop with Mustard (6:1 ratio)",
                    "Neem extract (NSKE 10,000 ppm) for organic pest defense"
                  ],
                  "naturalOrderPreservation": "Fixes 40kg atmospheric nitrogen, regenerating soil after previous heavy feeder cereals."
                }
              ]
            }
        """.trimIndent()
    }

    private fun parseGeminiJsonResponse(rawText: String, input: CropAdvisoryAiInput): AiAdvisoryResult {
        return try {
            val cleaned = rawText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            val summary = json.optString("summaryVerdict", "Gemini AI agro-climatic land analysis completed.")
            val recsArray = json.optJSONArray("recommendations") ?: JSONArray()

            val list = mutableListOf<AiCropRecommendation>()
            for (i in 0 until recsArray.length()) {
                val item = recsArray.getJSONObject(i)
                val practicesJson = item.optJSONArray("sustainablePractices")
                val practicesList = mutableListOf<String>()
                if (practicesJson != null) {
                    for (j in 0 until practicesJson.length()) {
                        practicesList.add(practicesJson.getString(j))
                    }
                }

                list.add(
                    AiCropRecommendation(
                        cropName = item.optString("cropName", "Recommended Crop"),
                        hindiName = item.optString("hindiName", ""),
                        agroClimaticMatchScore = item.optInt("agroClimaticMatchScore", 85).coerceIn(40, 99),
                        aiReasoning = item.optString("aiReasoning", "Strong match for local soil and season."),
                        expectedYieldQuintalsPerAcre = item.optDouble("expectedYieldQuintalsPerAcre", 8.0),
                        estimatedRevenuePerAcreRupees = item.optDouble("estimatedRevenuePerAcreRupees", 45000.0),
                        estimatedCostPerAcreRupees = item.optDouble("estimatedCostPerAcreRupees", 15000.0),
                        estimatedNetProfitPerAcreRupees = item.optDouble("estimatedNetProfitPerAcreRupees", 30000.0),
                        maturityDays = item.optInt("maturityDays", 100),
                        climateAndDroughtRisk = item.optString("climateAndDroughtRisk", "Moderate"),
                        pestAndDiseaseRisk = item.optString("pestAndDiseaseRisk", "Low"),
                        marketGlutRisk = item.optString("marketGlutRisk", "Low"),
                        sustainablePractices = if (practicesList.isNotEmpty()) practicesList else listOf("Bio-inoculation", "Micro-irrigation"),
                        naturalOrderPreservation = item.optString("naturalOrderPreservation", "Promotes biological soil fertility.")
                    )
                )
            }

            if (list.isEmpty()) {
                generateSynthesizedAdvisory(input, isLiveAi = false)
            } else {
                AiAdvisoryResult(
                    locationAnalyzed = input.locationName,
                    soilAnalyzed = "${input.soilType.displayName} (${input.soilType.hindiName})",
                    seasonAnalyzed = "${input.season.displayName} (${input.season.hindiName})",
                    weatherContext = "Temp ${input.currentTemp.toInt()}°C • Rain ${input.rainProbability}%",
                    summaryVerdict = summary,
                    recommendations = list,
                    isLiveAiGenerated = true
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiCropAdvisory", "Failed to parse Gemini response JSON: ${e.message}", e)
            generateSynthesizedAdvisory(input, isLiveAi = false)
        }
    }

    /**
     * Synthesizes expert agronomic recommendations using local agro-climatic algorithms
     * when offline or API key is unconfigured.
     */
    fun generateSynthesizedAdvisory(input: CropAdvisoryAiInput, isLiveAi: Boolean = false): AiAdvisoryResult {
        val query = CropAdvisoryEngine.AdvisoryQuery(
            soilType = input.soilType,
            season = input.season,
            irrigation = input.irrigation,
            previousCrop = input.previousCrop,
            farmAcres = input.farmAcres
        )

        val localEvaluations = CropAdvisoryEngine.evaluateCropPlan(query)
        val aiRecs = localEvaluations.take(4).map { crop ->
            val plan = crop.naturalOrderPreservationPlan
            val practices = listOf(
                plan.biologicalSoilRemedy,
                plan.companionPlantingTrapCrop,
                plan.polycultureIntercropRecipe,
                plan.waterConservationMethod
            )

            AiCropRecommendation(
                cropName = crop.cropName,
                hindiName = crop.hindiName,
                agroClimaticMatchScore = crop.naturalHarmonyScore,
                aiReasoning = "Calculated high biological compatibility with ${input.soilType.displayName} soil during ${input.season.displayName}. ${crop.naturalHarmonyVerdict}",
                expectedYieldQuintalsPerAcre = (crop.expectedYieldPerAcreQuintals * 10).roundToInt() / 10.0,
                estimatedRevenuePerAcreRupees = (crop.estimatedGrossRevenueRupees / input.farmAcres.coerceAtLeast(0.5)).roundToInt().toDouble(),
                estimatedCostPerAcreRupees = (crop.estimatedInputCostRupees / input.farmAcres.coerceAtLeast(0.5)).roundToInt().toDouble(),
                estimatedNetProfitPerAcreRupees = (crop.estimatedNetProfitRupees / input.farmAcres.coerceAtLeast(0.5)).roundToInt().toDouble(),
                maturityDays = crop.maturityDays,
                climateAndDroughtRisk = "${crop.overallRiskLevel.label} (${crop.weatherRiskPercent}% risk) - Irrigation demand aligned with ${input.irrigation.displayName}.",
                pestAndDiseaseRisk = "${crop.pestRiskPercent}% risk - Natural resistance against previous ${input.previousCrop.displayName} pathogens.",
                marketGlutRisk = "${crop.marketGlutRiskPercent}% risk - Expected APMC rate @ ₹${crop.expectedMarketRatePerQuintal.toInt()}/Qtl.",
                sustainablePractices = practices,
                naturalOrderPreservation = plan.naturalPestMitigation
            )
        }

        val summary = "AI analysis evaluated ${input.soilType.displayName} soil and ${input.season.displayName} seasonal weather at ${input.locationName}. Following ${input.previousCrop.displayName}, planting restorative nitrogen-fixing pulses or certified bio-safe crops restores soil biology and maximizes per-acre profits."

        return AiAdvisoryResult(
            locationAnalyzed = input.locationName,
            soilAnalyzed = "${input.soilType.displayName} (${input.soilType.hindiName})",
            seasonAnalyzed = "${input.season.displayName} (${input.season.hindiName})",
            weatherContext = "Temp ${input.currentTemp.toInt()}°C • Rain ${input.rainProbability}%",
            summaryVerdict = summary,
            recommendations = aiRecs,
            isLiveAiGenerated = isLiveAi
        )
    }
}
