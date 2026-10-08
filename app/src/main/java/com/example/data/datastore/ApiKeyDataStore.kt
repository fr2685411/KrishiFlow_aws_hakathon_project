package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.ai.AiProvider
import com.example.data.ai.KrishiAiKeyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// Singleton DataStore instance on Android Context
val Context.apiKeysDataStore: DataStore<Preferences> by preferencesDataStore(name = "krishiflow_api_keys_prefs")

data class ApiKeySettingsState(
    val geminiKey: String = "",
    val openAiKey: String = "",
    val claudeKey: String = "",
    val activeProvider: String = "gemini",
    val isDeveloperKeyDisabled: Boolean = false,
    val isAutoOfflineFallback: Boolean = true
)

/**
 * DataStore repository for persisting Bring-Your-Own-Key (BYOK) credentials locally.
 * Ensures API keys are never hardcoded in the codebase, with asynchronous Flow-based reactive updates.
 */
object ApiKeyDataStore {

    val KEY_GEMINI = stringPreferencesKey("gemini_api_key")
    val KEY_OPENAI = stringPreferencesKey("openai_api_key")
    val KEY_CLAUDE = stringPreferencesKey("claude_api_key")
    val KEY_ACTIVE_PROVIDER = stringPreferencesKey("active_ai_provider")
    val KEY_DEV_KEY_DISABLED = booleanPreferencesKey("developer_key_disabled")
    val KEY_AUTO_OFFLINE = booleanPreferencesKey("auto_offline_fallback")

    fun getSettingsFlow(context: Context): Flow<ApiKeySettingsState> {
        return context.apiKeysDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                ApiKeySettingsState(
                    geminiKey = preferences[KEY_GEMINI] ?: "",
                    openAiKey = preferences[KEY_OPENAI] ?: "",
                    claudeKey = preferences[KEY_CLAUDE] ?: "",
                    activeProvider = preferences[KEY_ACTIVE_PROVIDER] ?: "gemini",
                    isDeveloperKeyDisabled = preferences[KEY_DEV_KEY_DISABLED] ?: false,
                    isAutoOfflineFallback = preferences[KEY_AUTO_OFFLINE] ?: true
                )
            }
    }

    suspend fun saveGeminiKey(context: Context, key: String) {
        val trimmed = key.trim()
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_GEMINI] = trimmed
        }
        // Mirror to KrishiAiKeyManager for immediate synchronous access
        KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, trimmed)
    }

    suspend fun saveOpenAiKey(context: Context, key: String) {
        val trimmed = key.trim()
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_OPENAI] = trimmed
        }
        KrishiAiKeyManager.saveApiKey(context, AiProvider.OPENAI, trimmed)
    }

    suspend fun saveClaudeKey(context: Context, key: String) {
        val trimmed = key.trim()
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_CLAUDE] = trimmed
        }
        KrishiAiKeyManager.saveApiKey(context, AiProvider.CLAUDE, trimmed)
    }

    suspend fun saveActiveProvider(context: Context, providerId: String) {
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_PROVIDER] = providerId
        }
        KrishiAiKeyManager.setActiveProvider(context, AiProvider.fromId(providerId))
    }

    suspend fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) {
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_DEV_KEY_DISABLED] = disabled
        }
        KrishiAiKeyManager.setDeveloperKeyDisabled(context, disabled)
    }

    suspend fun setAutoOfflineFallback(context: Context, enabled: Boolean) {
        context.apiKeysDataStore.edit { prefs ->
            prefs[KEY_AUTO_OFFLINE] = enabled
        }
        KrishiAiKeyManager.setAutoOfflineFallbackEnabled(context, enabled)
    }

    suspend fun clearKey(context: Context, provider: AiProvider) {
        context.apiKeysDataStore.edit { prefs ->
            when (provider) {
                AiProvider.GEMINI -> prefs.remove(KEY_GEMINI)
                AiProvider.OPENAI -> prefs.remove(KEY_OPENAI)
                AiProvider.CLAUDE -> prefs.remove(KEY_CLAUDE)
                AiProvider.OFFLINE_BIO_SYNTHESIS -> {}
            }
        }
        KrishiAiKeyManager.removeApiKey(context, provider)
    }

    suspend fun purgeAllKeys(context: Context) {
        context.apiKeysDataStore.edit { prefs ->
            prefs.remove(KEY_GEMINI)
            prefs.remove(KEY_OPENAI)
            prefs.remove(KEY_CLAUDE)
            prefs[KEY_DEV_KEY_DISABLED] = true
        }
        KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
    }
}
