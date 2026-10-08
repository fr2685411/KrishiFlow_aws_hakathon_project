package com.example.data.datastore

import android.content.Context
import com.example.data.ai.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ApiKeySettingsState(
    val geminiKey: String = "",
    val openAiKey: String = "",
    val claudeKey: String = "",
    val activeProvider: String = "gemini",
    val isDeveloperKeyDisabled: Boolean = false,
    val isAutoOfflineFallback: Boolean = true
)

/**
 * Backward-compatible facade for ApiKeyStore.
 * Ensures all DataStore persistence operations use AES-256-GCM encryption.
 */
object ApiKeyDataStore {

    fun getSettingsFlow(context: Context): Flow<ApiKeySettingsState> {
        return ApiKeyStore.getSettingsFlow(context).map { decrypted ->
            ApiKeySettingsState(
                geminiKey = decrypted.geminiKey,
                openAiKey = decrypted.openAiKey,
                claudeKey = decrypted.claudeKey,
                activeProvider = decrypted.activeProvider,
                isDeveloperKeyDisabled = decrypted.isDeveloperKeyDisabled,
                isAutoOfflineFallback = decrypted.isAutoOfflineFallback
            )
        }
    }

    suspend fun saveGeminiKey(context: Context, key: String) {
        ApiKeyStore.saveGeminiKey(context, key)
    }

    suspend fun saveOpenAiKey(context: Context, key: String) {
        ApiKeyStore.saveOpenAiKey(context, key)
    }

    suspend fun saveClaudeKey(context: Context, key: String) {
        ApiKeyStore.saveClaudeKey(context, key)
    }

    suspend fun saveActiveProvider(context: Context, providerId: String) {
        ApiKeyStore.saveActiveProvider(context, providerId)
    }

    suspend fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) {
        ApiKeyStore.setDeveloperKeyDisabled(context, disabled)
    }

    suspend fun setAutoOfflineFallback(context: Context, enabled: Boolean) {
        ApiKeyStore.setAutoOfflineFallback(context, enabled)
    }

    suspend fun clearKey(context: Context, provider: AiProvider) {
        ApiKeyStore.clearKey(context, provider)
    }

    suspend fun purgeAllKeys(context: Context) {
        ApiKeyStore.purgeAllKeys(context)
    }
}
