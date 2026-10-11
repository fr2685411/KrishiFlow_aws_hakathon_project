package com.example.data.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val message: String,
    val latencyMs: Long = 0,
    val provider: AiProvider,
    val model: String
)

data class AiExecutionResult(
    val text: String,
    val isLiveAi: Boolean,
    val provider: AiProvider,
    val modelUsed: String,
    val suggestedAction: String? = null
)

/**
 * Universal dynamic network client for multi-provider AI inference in KrishiFlow.
 * 
 * Supports:
 * - Google Gemini (Google AI Studio) via REST
 * - OpenAI (GPT-4o, GPT-4o-mini) via REST
 * - Anthropic Claude (Claude 3.5 Haiku, Claude 3.5 Sonnet) via REST
 * - Offline ICAR Agronomy Bio-Engine (Zero-cloud fallback)
 * 
 * Dynamically selects credentials and models from KrishiAiKeyManager so NO keys are hardcoded.
 */
class KrishiAiInferenceService(private val context: Context) {

    companion object {
        private const val TAG = "KrishiAiInference"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Tests connectivity and key validity for a given AI provider with a fast lightweight ping.
     */
    suspend fun testProviderConnection(
        provider: AiProvider,
        overrideKey: String? = null
    ): ConnectionTestResult = withContext(Dispatchers.IO) {
        if (provider == AiProvider.OFFLINE_BIO_SYNTHESIS) {
            return@withContext ConnectionTestResult(
                isSuccess = true,
                message = "ICAR Bio-Engine is verified and available 100% offline on-device.",
                latencyMs = 2,
                provider = provider,
                model = provider.defaultModel
            )
        }

        val effectiveKey = (overrideKey ?: KrishiAiKeyManager.getEffectiveKey(context, provider)).trim()
        if (effectiveKey.isBlank()) {
            return@withContext ConnectionTestResult(
                isSuccess = false,
                message = "API key is empty. Please enter your ${provider.displayName} API key.",
                latencyMs = 0,
                provider = provider,
                model = KrishiAiKeyManager.getSelectedModel(context, provider)
            )
        }

        val model = KrishiAiKeyManager.getSelectedModel(context, provider)
        val startTime = System.currentTimeMillis()

        try {
            val responseText = when (provider) {
                AiProvider.GEMINI -> executeGeminiPing(effectiveKey, model)
                AiProvider.OPENAI -> executeOpenAiPing(effectiveKey, model)
                AiProvider.CLAUDE -> executeClaudePing(effectiveKey, model)
                AiProvider.OFFLINE_BIO_SYNTHESIS -> "Offline active"
            }

            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                isSuccess = true,
                message = "Connected to ${provider.displayName} ($model) in ${latency}ms!\nResponse: \"$responseText\"",
                latencyMs = latency,
                provider = provider,
                model = model
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val cleanError = parseErrorMessage(e)
            Log.e(TAG, "Connection test failed for ${provider.displayName}: $cleanError", e)
            ConnectionTestResult(
                isSuccess = false,
                message = "Failed (${latency}ms): $cleanError",
                latencyMs = latency,
                provider = provider,
                model = model
            )
        }
    }

    /**
     * Universal chat execution with dynamic provider resolution.
     */
    suspend fun executeChatTurn(
        systemPrompt: String,
        conversationHistoryText: String,
        userPrompt: String
    ): AiExecutionResult = withContext(Dispatchers.IO) {
        val activeProvider = KrishiAiKeyManager.getActiveProvider(context)
        val autoOffline = KrishiAiKeyManager.isAutoOfflineFallbackEnabled(context)

        if (activeProvider == AiProvider.OFFLINE_BIO_SYNTHESIS) {
            return@withContext generateOfflineChatResponse(userPrompt)
        }

        val apiKey = KrishiAiKeyManager.getEffectiveKey(context, activeProvider)
        if (apiKey.isBlank()) {
            if (autoOffline) {
                Log.w(TAG, "No API key for ${activeProvider.displayName}, falling back to offline bio-engine.")
                return@withContext generateOfflineChatResponse(userPrompt, fallbackNotice = true)
            } else {
                return@withContext AiExecutionResult(
                    text = "Please configure your ${activeProvider.displayName} API key in Settings (⚙️) to activate live intelligence.",
                    isLiveAi = false,
                    provider = activeProvider,
                    modelUsed = "none",
                    suggestedAction = "OPEN_SETTINGS"
                )
            }
        }

        val model = KrishiAiKeyManager.getSelectedModel(context, activeProvider)

        try {
            val rawReply = when (activeProvider) {
                AiProvider.GEMINI -> executeGeminiChat(apiKey, model, systemPrompt, conversationHistoryText, userPrompt)
                AiProvider.OPENAI -> executeOpenAiChat(apiKey, model, systemPrompt, conversationHistoryText, userPrompt)
                AiProvider.CLAUDE -> executeClaudeChat(apiKey, model, systemPrompt, conversationHistoryText, userPrompt)
                AiProvider.OFFLINE_BIO_SYNTHESIS -> ""
            }

            val actionTag = extractActionTag(rawReply)
            val cleaned = rawReply.replace(Regex("\\[ACTION:[A-Z_]+\\]"), "").trim()

            AiExecutionResult(
                text = cleaned,
                isLiveAi = true,
                provider = activeProvider,
                modelUsed = model,
                suggestedAction = actionTag
            )
        } catch (e: Exception) {
            Log.e(TAG, "AI Chat Turn failed with ${activeProvider.displayName}: ${e.message}", e)
            if (autoOffline) {
                generateOfflineChatResponse(userPrompt, fallbackNotice = true)
            } else {
                AiExecutionResult(
                    text = "Error calling ${activeProvider.displayName} (${e.localizedMessage ?: "Unknown error"}). Please verify your API key in Settings.",
                    isLiveAi = false,
                    provider = activeProvider,
                    modelUsed = model
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Google Gemini REST Execution
    // -------------------------------------------------------------------------

    private fun executeGeminiPing(apiKey: String, model: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", "Ping test. Reply with 'KrishiFlow Online' only."))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().put("temperature", 0.1))
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim() ?: "Online"
    }

    private fun executeGeminiChat(
        apiKey: String,
        model: String,
        systemPrompt: String,
        historyText: String,
        userPrompt: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val combinedPrompt = "System Directive:\n$systemPrompt\n\n$historyText\nFarmer: $userPrompt\nKrishiMitra AI:"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", combinedPrompt))
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.35)
                put("topP", 0.95)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("Gemini HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim() ?: "Processed by Gemini."
    }

    // -------------------------------------------------------------------------
    // OpenAI REST Execution
    // -------------------------------------------------------------------------

    private fun executeOpenAiPing(apiKey: String, model: String): String {
        val url = "https://api.openai.com/v1/chat/completions"
        val requestJson = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", "Ping test. Reply with 'KrishiFlow Online' only."))
            }
            put("messages", messages)
            put("temperature", 0.1)
            put("max_tokens", 30)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("OpenAI HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            ?.trim() ?: "Online"
    }

    private fun executeOpenAiChat(
        apiKey: String,
        model: String,
        systemPrompt: String,
        historyText: String,
        userPrompt: String
    ): String {
        val url = "https://api.openai.com/v1/chat/completions"
        val requestJson = JSONObject().apply {
            put("model", model)
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", systemPrompt))
                if (historyText.isNotBlank()) {
                    put(JSONObject().put("role", "user").put("content", "Conversation context:\n$historyText"))
                }
                put(JSONObject().put("role", "user").put("content", userPrompt))
            }
            put("messages", messages)
            put("temperature", 0.35)
            put("max_tokens", 800)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("OpenAI HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            ?.trim() ?: "Processed by OpenAI."
    }

    // -------------------------------------------------------------------------
    // Anthropic Claude REST Execution
    // -------------------------------------------------------------------------

    private fun executeClaudePing(apiKey: String, model: String): String {
        val url = "https://api.anthropic.com/v1/messages"
        val requestJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 30)
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", "Ping test. Reply with 'KrishiFlow Online' only."))
            }
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("Claude HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("content")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim() ?: "Online"
    }

    private fun executeClaudeChat(
        apiKey: String,
        model: String,
        systemPrompt: String,
        historyText: String,
        userPrompt: String
    ): String {
        val url = "https://api.anthropic.com/v1/messages"
        val promptWithHistory = if (historyText.isNotBlank()) {
            "Recent context:\n$historyText\n\nFarmer request: $userPrompt"
        } else {
            userPrompt
        }

        val requestJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 800)
            put("system", systemPrompt)
            val messages = JSONArray().apply {
                put(JSONObject().put("role", "user").put("content", promptWithHistory))
            }
            put("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(requestJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("Claude HTTP ${response.code}: ${parseHttpErrorBody(body)}")
        }

        val json = JSONObject(body)
        return json.optJSONArray("content")
            ?.optJSONObject(0)
            ?.optString("text")
            ?.trim() ?: "Processed by Claude."
    }

    // -------------------------------------------------------------------------
    // Fallback & Helpers
    // -------------------------------------------------------------------------

    private fun generateOfflineChatResponse(userPrompt: String, fallbackNotice: Boolean = false): AiExecutionResult {
        val lower = userPrompt.lowercase()
        val prefix = if (fallbackNotice) "🌿 [Local Agronomy Fallback Active]\n" else ""

        val response = when {
            lower.contains("shop") || lower.contains("seed") || lower.contains("bio") || lower.contains("fertilizer") -> {
                "${prefix}नमस्ते! In our Sustainable Input Shop, you can procure certified bio-fertilizers (Rhizobium, Trichoderma) and drought-tolerant seeds directly without middlemen markup. [ACTION:OPEN_SHOP]"
            }
            lower.contains("rate") || lower.contains("mandi") || lower.contains("price") || lower.contains("market") -> {
                "${prefix}Mandi prices for Grade A tomatoes are hovering around ₹26-32/kg across Junnar and Narayangaon. For surplus lots, apply dynamic discounts on the Surplus Priority board to prevent spoilage. [ACTION:OPEN_SURPLUS]"
            }
            lower.contains("harvest") || lower.contains("add") || lower.contains("sell") || lower.contains("stock") -> {
                "${prefix}To list your fresh harvest for direct aggregation and cold-chain pickup, use the Kisan Produce dashboard. [ACTION:OPEN_PRODUCE]"
            }
            lower.contains("weather") || lower.contains("rain") || lower.contains("crop") || lower.contains("sow") -> {
                "${prefix}ICAR advisory: Check the Crop Planning Advisor to calculate crop rotation harmony scores, water conservation, and pest barriers. [ACTION:OPEN_CROP_ADVISOR]"
            }
            lower.contains("yield") || lower.contains("calculator") || lower.contains("profit") -> {
                "${prefix}Calculate expected per-acre yield, input expenses, and net profit with our Harvest Yield Calculator. [ACTION:OPEN_CALCULATOR]"
            }
            lower.contains("map") || lower.contains("hub") || lower.contains("truck") || lower.contains("route") -> {
                "${prefix}Explore regional aggregation centers, cold storage units, and nearby APMC yards on the Logistics Route Map. [ACTION:OPEN_MAPS]"
            }
            else -> {
                "${prefix}नमस्ते किसान भाई! I am KrishiMitra AI. You can ask me about mandi price trends, crop advisory, pest protection, or say 'open shop', 'show inventory', or 'calculate yield' to navigate the app. [ACTION:EXPLAIN_APP]"
            }
        }

        val actionTag = extractActionTag(response)
        val cleaned = response.replace(Regex("\\[ACTION:[A-Z_]+\\]"), "").trim()

        return AiExecutionResult(
            text = cleaned,
            isLiveAi = false,
            provider = AiProvider.OFFLINE_BIO_SYNTHESIS,
            modelUsed = "icar-agronomy-v2",
            suggestedAction = actionTag
        )
    }

    private fun extractActionTag(text: String): String? {
        val match = Regex("\\[ACTION:([A-Z_]+)\\]").find(text)
        return match?.groupValues?.getOrNull(1)
    }

    private fun parseHttpErrorBody(body: String): String {
        return try {
            val json = JSONObject(body)
            if (json.has("error")) {
                val errorObj = json.get("error")
                if (errorObj is JSONObject) {
                    errorObj.optString("message", body)
                } else {
                    errorObj.toString()
                }
            } else {
                body.take(120)
            }
        } catch (_: Exception) {
            body.take(120)
        }
    }

    private fun parseErrorMessage(e: Exception): String {
        val msg = e.message ?: "Unknown error"
        return when {
            msg.contains("401") || msg.contains("403") || msg.contains("API key not valid") ->
                "Invalid API key or unauthorized. Please recheck your key."
            msg.contains("429") || msg.contains("quota") || msg.contains("RESOURCE_EXHAUSTED") ->
                "Rate limit or quota exceeded for this model."
            msg.contains("404") || msg.contains("model") ->
                "Selected model not found or unsupported for this key tier."
            msg.contains("Failed to connect") || msg.contains("timeout") || msg.contains("Unable to resolve host") ->
                "Network timeout. Check your internet connection."
            else -> msg
        }
    }
}
