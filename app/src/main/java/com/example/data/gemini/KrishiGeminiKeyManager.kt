package com.example.data.gemini

import android.content.Context
import com.example.data.ai.AiProvider
import com.example.data.ai.KrishiAiKeyManager

/**
 * Backward-compatible bridge to KrishiAiKeyManager.
 * 
 * Protects developer credentials before app release & publishing:
 * 1. Allows users / farmers to input, inspect, and remove their own Gemini API key anytime.
 * 2. Allows developers / admins to purge or disable bundled keys, ensuring published APKs
 *    do not expose or consume developer quota.
 * 3. Gracefully supports autonomous offline synthesis and ICAR agronomy bio-models
 *    whenever no API key is provided.
 */
object KrishiGeminiKeyManager {

    fun getUserApiKey(context: Context): String {
        return KrishiAiKeyManager.getApiKey(context, AiProvider.GEMINI)
    }

    fun saveUserApiKey(context: Context, key: String) {
        KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, key)
    }

    fun removeUserApiKey(context: Context) {
        KrishiAiKeyManager.removeApiKey(context, AiProvider.GEMINI)
    }

    fun isDeveloperKeyDisabled(context: Context): Boolean {
        return KrishiAiKeyManager.isDeveloperKeyDisabled(context)
    }

    fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) {
        KrishiAiKeyManager.setDeveloperKeyDisabled(context, disabled)
    }

    fun purgeAllKeysForPublishing(context: Context) {
        KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
    }

    fun getEffectiveApiKey(context: Context): String {
        return KrishiAiKeyManager.getEffectiveKey(context, AiProvider.GEMINI)
    }

    fun hasActiveApiKey(context: Context): Boolean {
        return KrishiAiKeyManager.hasActiveKeyForProvider(context, AiProvider.GEMINI)
    }

    fun isUsingUserKey(context: Context): Boolean {
        return getUserApiKey(context).isNotBlank()
    }
}
