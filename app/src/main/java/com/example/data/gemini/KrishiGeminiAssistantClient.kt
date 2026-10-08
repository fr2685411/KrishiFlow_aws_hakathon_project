package com.example.data.gemini

import android.content.Context
import android.util.Log
import com.example.data.ai.AiProvider
import com.example.data.ai.KrishiAiInferenceService
import com.example.data.ai.KrishiAiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

enum class GeminiMessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

data class GeminiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: GeminiMessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedAction: String? = null,
    val executedActionTitle: String? = null,
    val providerUsed: String? = null,
    val modelUsed: String? = null
)

/**
 * Intelligent AI Assistant client for KrishiFlow.
 * 
 * Supports dynamic Bring-Your-Own-Key (BYOK) for:
 * - Google Gemini (Google AI Studio)
 * - OpenAI (GPT-4o / GPT-4o-mini)
 * - Anthropic Claude (Claude 3.5 Haiku / Sonnet)
 * - Offline ICAR Agronomy Bio-Engine
 * 
 * No hardcoded keys are required or exposed.
 */
class KrishiGeminiAssistantClient(private val context: Context) {

    private val inferenceService = KrishiAiInferenceService(context)

    fun getCustomApiKey(): String {
        return KrishiAiKeyManager.getApiKey(context, AiProvider.GEMINI)
    }

    fun saveCustomApiKey(key: String) {
        KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, key)
    }

    fun clearCustomApiKey() {
        KrishiAiKeyManager.removeApiKey(context, AiProvider.GEMINI)
    }

    fun purgeAllKeysForPublishing() {
        KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
    }

    fun isDeveloperKeyDisabled(): Boolean {
        return KrishiAiKeyManager.isDeveloperKeyDisabled(context)
    }

    fun setDeveloperKeyDisabled(disabled: Boolean) {
        KrishiAiKeyManager.setDeveloperKeyDisabled(context, disabled)
    }

    fun getEffectiveApiKey(): String {
        return KrishiAiKeyManager.getEffectiveKey(context, AiProvider.GEMINI)
    }

    fun hasValidApiKey(): Boolean {
        return KrishiAiKeyManager.hasActiveKeyForProvider(context, KrishiAiKeyManager.getActiveProvider(context))
    }

    fun getActiveProvider(): AiProvider {
        return KrishiAiKeyManager.getActiveProvider(context)
    }

    /**
     * System prompt that explains to the AI how it operates inside the KrishiFlow environment,
     * including live data awareness, supported voice/text commands, and actionable navigation triggers.
     */
    fun buildSystemContext(
        totalStockKg: Double,
        activeBatchesSummary: String,
        currentLocation: String,
        temperatureC: Double,
        weatherCondition: String,
        activeRole: String
    ): String {
        return """
            You are 'KrishiMitra AI' (कृषि मित्र AI), the custom intelligent voice & text assistant embedded inside the KrishiFlow Android application.
            
            APP ENVIRONMENT KNOWLEDGE & CAPABILITIES:
            1. App Purpose: KrishiFlow connects Indian farmers (किसान), village micro-aggregation hubs (मंडी हब), and wholesale/retail buyers (व्यापारी) with zero commission, dynamic pricing, and spoilage prevention.
            2. Current App State:
               - Active App Role: $activeRole
               - Live Regional Location: $currentLocation
               - Weather: ${temperatureC.toInt()}°C, $weatherCondition
               - Current Hub Stock: ${totalStockKg.toInt()} kg
               - Registered Harvest Lots: $activeBatchesSummary
            3. Navigation & Features you can control or guide the user to:
               - NAV_KISAN: Farmer Dashboard (list harvest, mandi rates, advisory)
               - NAV_PRODUCE: Produce Inventory & Shelf-life Dashboard
               - NAV_SURPLUS: Spoilage Priority Board & Dynamic Discounting
               - NAV_MAPS: Regional Logistics & Nearby Mandi Route Map
               - NAV_SHOP: Sustainable Input Shop (Bio-pesticides, certified seeds, trichoderma)
               - NAV_CALCULATOR: Harvest Yield & Revenue Calculator
               - NAV_LOGS: Harvest & Sales History Logs (PDF export)
               - NAV_SCANNER: Offline QR/Barcode Scanner for Room SQLite cache
               - NAV_WALLET: Digital Wallet & UPI Vendor Payment
               - NAV_SETTINGS: API Key & LLM Model Settings (BYOK)
            4. Command & Action Execution Syntax:
               If the user asks to open or do something in the app, append an ACTION tag at the end of your response:
               - To open shop: [ACTION:OPEN_SHOP]
               - To open produce dashboard: [ACTION:OPEN_PRODUCE]
               - To open yield calculator: [ACTION:OPEN_CALCULATOR]
               - To open maps: [ACTION:OPEN_MAPS]
               - To list new harvest: [ACTION:OPEN_LIST_HARVEST]
               - To open surplus discount board: [ACTION:OPEN_SURPLUS]
               - To open harvest logs: [ACTION:OPEN_LOGS]
               - To open crop advisor: [ACTION:OPEN_CROP_ADVISOR]
               - To open settings/API keys: [ACTION:OPEN_SETTINGS]
               - To explain features: [ACTION:EXPLAIN_APP]
            5. Tone and Language:
               - Warm, respectful, agricultural expert tone suitable for Indian farmers and mandi traders.
               - Respond in Hindi, Hinglish, Marathi, or English matching the user's preferred language.
               - Provide concise, practical agronomy guidance (soil nutrition, mandi timing, pest control).
        """.trimIndent()
    }

    /**
     * Sends message with conversational context and system instructions to the active AI engine dynamically.
     */
    suspend fun sendMessage(
        conversationHistory: List<GeminiChatMessage>,
        userPrompt: String,
        systemContext: String
    ): GeminiChatMessage = withContext(Dispatchers.IO) {
        val activeProvider = KrishiAiKeyManager.getActiveProvider(context)

        // Build recent conversational turn context
        val historyTurns = conversationHistory.takeLast(4).joinToString("\n") { msg ->
            val speaker = if (msg.sender == GeminiMessageSender.USER) "Farmer" else "KrishiMitra AI"
            "$speaker: ${msg.text}"
        }

        try {
            val result = inferenceService.executeChatTurn(
                systemPrompt = systemContext,
                conversationHistoryText = historyTurns,
                userPrompt = userPrompt
            )

            GeminiChatMessage(
                sender = GeminiMessageSender.ASSISTANT,
                text = result.text,
                suggestedAction = result.suggestedAction,
                providerUsed = result.provider.displayName,
                modelUsed = result.modelUsed
            )
        } catch (e: Exception) {
            Log.e("KrishiGeminiClient", "Dynamic chat turn error: ${e.message}", e)
            GeminiChatMessage(
                sender = GeminiMessageSender.ASSISTANT,
                text = "कृषि मित्र AI: Error contacting ${activeProvider.displayName} (${e.localizedMessage ?: "Unknown error"}). You can switch providers or update your key in Settings ⚙️."
            )
        }
    }
}
