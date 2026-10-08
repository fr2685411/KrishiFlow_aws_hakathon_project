package com.example.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.data.ai.AiProvider
import com.example.data.ai.KrishiAiKeyManager
import com.example.data.security.KeyStoreCryptoHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Data representation of all API key settings and credentials retrieved from DataStore.
 */
data class ApiKeySettings(
    val geminiKey: String = "",
    val openAiKey: String = "",
    val claudeKey: String = "",
    val activeProvider: String = "gemini",
    val isDeveloperKeyDisabled: Boolean = false,
    val isAutoOfflineFallback: Boolean = true,
    val encryptedGeminiCiphertext: String = "",
    val encryptedOpenAiCiphertext: String = "",
    val encryptedClaudeCiphertext: String = ""
)

/**
 * Jetpack DataStore utility class for securely persisting and retrieving user-provided
 * Gemini, OpenAI, and Claude API keys.
 *
 * Security Architecture:
 * - Credentials are encrypted with hardware-backed AES-256-GCM via [KeyStoreCryptoHelper]
 *   prior to persistence in Preferences DataStore.
 * - Raw plaintext keys are never stored on disk.
 * - Flow emissions automatically decrypt ciphertexts into usable keys in-memory.
 * - Supports both object/static access patterns and instance-based dependency injection.
 */
class ApiKeyStore(private val context: Context) {

    companion object {
        val KEY_ENC_GEMINI: Preferences.Key<String> = stringPreferencesKey("enc_gemini_api_key")
        val KEY_ENC_OPENAI: Preferences.Key<String> = stringPreferencesKey("enc_openai_api_key")
        val KEY_ENC_CLAUDE: Preferences.Key<String> = stringPreferencesKey("enc_claude_api_key")
        val KEY_ACTIVE_PROVIDER: Preferences.Key<String> = stringPreferencesKey("active_ai_provider")
        val KEY_DEV_KEY_DISABLED: Preferences.Key<Boolean> = booleanPreferencesKey("developer_key_disabled")
        val KEY_AUTO_OFFLINE: Preferences.Key<Boolean> = booleanPreferencesKey("auto_offline_fallback")

        @Volatile
        private var INSTANCE: ApiKeyStore? = null

        fun getInstance(context: Context): ApiKeyStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiKeyStore(context.applicationContext).also { INSTANCE = it }
            }
        }

        // Static convenience methods
        fun getSettingsFlow(context: Context): Flow<ApiKeySettings> =
            getInstance(context).settingsFlow

        suspend fun getGeminiKey(context: Context): String =
            getInstance(context).getGeminiKey()

        suspend fun getOpenAiKey(context: Context): String =
            getInstance(context).getOpenAiKey()

        suspend fun getClaudeKey(context: Context): String =
            getInstance(context).getClaudeKey()

        suspend fun getApiKey(context: Context, provider: AiProvider): String =
            getInstance(context).getApiKey(provider)

        suspend fun saveGeminiKey(context: Context, key: String) =
            getInstance(context).saveGeminiKey(key)

        suspend fun saveOpenAiKey(context: Context, key: String) =
            getInstance(context).saveOpenAiKey(key)

        suspend fun saveClaudeKey(context: Context, key: String) =
            getInstance(context).saveClaudeKey(key)

        suspend fun saveApiKey(context: Context, provider: AiProvider, key: String) =
            getInstance(context).saveApiKey(provider, key)

        suspend fun saveActiveProvider(context: Context, providerId: String) =
            getInstance(context).saveActiveProvider(providerId)

        suspend fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) =
            getInstance(context).setDeveloperKeyDisabled(disabled)

        suspend fun setAutoOfflineFallback(context: Context, enabled: Boolean) =
            getInstance(context).setAutoOfflineFallback(enabled)

        suspend fun clearKey(context: Context, provider: AiProvider) =
            getInstance(context).clearKey(provider)

        suspend fun purgeAllKeys(context: Context) =
            getInstance(context).purgeAllKeys()
    }

    /**
     * Observable flow of decrypted settings and raw ciphertexts for debugging/inspection.
     */
    val settingsFlow: Flow<ApiKeySettings> = context.encryptedApiKeysDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val encGemini = preferences[KEY_ENC_GEMINI] ?: ""
            val encOpenAi = preferences[KEY_ENC_OPENAI] ?: ""
            val encClaude = preferences[KEY_ENC_CLAUDE] ?: ""

            ApiKeySettings(
                geminiKey = KeyStoreCryptoHelper.decrypt(encGemini),
                openAiKey = KeyStoreCryptoHelper.decrypt(encOpenAi),
                claudeKey = KeyStoreCryptoHelper.decrypt(encClaude),
                activeProvider = preferences[KEY_ACTIVE_PROVIDER] ?: "gemini",
                isDeveloperKeyDisabled = preferences[KEY_DEV_KEY_DISABLED] ?: false,
                isAutoOfflineFallback = preferences[KEY_AUTO_OFFLINE] ?: true,
                encryptedGeminiCiphertext = encGemini,
                encryptedOpenAiCiphertext = encOpenAi,
                encryptedClaudeCiphertext = encClaude
            )
        }

    /**
     * Flow specifically for the decrypted Gemini API key.
     */
    val geminiKeyFlow: Flow<String> = settingsFlow.map { it.geminiKey }

    /**
     * Flow specifically for the decrypted OpenAI API key.
     */
    val openAiKeyFlow: Flow<String> = settingsFlow.map { it.openAiKey }

    /**
     * Flow specifically for the decrypted Claude API key.
     */
    val claudeKeyFlow: Flow<String> = settingsFlow.map { it.claudeKey }

    /**
     * Flow for the currently selected active AI provider ID.
     */
    val activeProviderFlow: Flow<String> = settingsFlow.map { it.activeProvider }

    /**
     * Flow for any provider's decrypted key.
     */
    fun getKeyFlow(provider: AiProvider): Flow<String> = settingsFlow.map { settings ->
        when (provider) {
            AiProvider.GEMINI -> settings.geminiKey
            AiProvider.OPENAI -> settings.openAiKey
            AiProvider.CLAUDE -> settings.claudeKey
            AiProvider.OFFLINE_BIO_SYNTHESIS -> ""
        }
    }

    // ------------------------------------------------------------------------
    // Read Operations (Suspend)
    // ------------------------------------------------------------------------

    suspend fun getGeminiKey(): String {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        val enc = prefs[KEY_ENC_GEMINI] ?: ""
        return KeyStoreCryptoHelper.decrypt(enc)
    }

    suspend fun getOpenAiKey(): String {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        val enc = prefs[KEY_ENC_OPENAI] ?: ""
        return KeyStoreCryptoHelper.decrypt(enc)
    }

    suspend fun getClaudeKey(): String {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        val enc = prefs[KEY_ENC_CLAUDE] ?: ""
        return KeyStoreCryptoHelper.decrypt(enc)
    }

    suspend fun getApiKey(provider: AiProvider): String {
        return when (provider) {
            AiProvider.GEMINI -> getGeminiKey()
            AiProvider.OPENAI -> getOpenAiKey()
            AiProvider.CLAUDE -> getClaudeKey()
            AiProvider.OFFLINE_BIO_SYNTHESIS -> ""
        }
    }

    suspend fun getActiveProvider(): String {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        return prefs[KEY_ACTIVE_PROVIDER] ?: "gemini"
    }

    suspend fun isDeveloperKeyDisabled(): Boolean {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        return prefs[KEY_DEV_KEY_DISABLED] ?: false
    }

    suspend fun isAutoOfflineFallback(): Boolean {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        return prefs[KEY_AUTO_OFFLINE] ?: true
    }

    // ------------------------------------------------------------------------
    // Write Operations (Suspend)
    // ------------------------------------------------------------------------

    /**
     * Encrypts and saves user-provided Gemini API key into DataStore.
     */
    suspend fun saveGeminiKey(key: String) {
        val trimmed = key.trim()
        val cipherText = if (trimmed.isNotBlank()) KeyStoreCryptoHelper.encrypt(trimmed) else ""
        context.encryptedApiKeysDataStore.edit { prefs ->
            if (cipherText.isNotBlank()) {
                prefs[KEY_ENC_GEMINI] = cipherText
            } else {
                prefs.remove(KEY_ENC_GEMINI)
            }
        }
        KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, trimmed)
    }

    /**
     * Encrypts and saves user-provided OpenAI API key into DataStore.
     */
    suspend fun saveOpenAiKey(key: String) {
        val trimmed = key.trim()
        val cipherText = if (trimmed.isNotBlank()) KeyStoreCryptoHelper.encrypt(trimmed) else ""
        context.encryptedApiKeysDataStore.edit { prefs ->
            if (cipherText.isNotBlank()) {
                prefs[KEY_ENC_OPENAI] = cipherText
            } else {
                prefs.remove(KEY_ENC_OPENAI)
            }
        }
        KrishiAiKeyManager.saveApiKey(context, AiProvider.OPENAI, trimmed)
    }

    /**
     * Encrypts and saves user-provided Claude API key into DataStore.
     */
    suspend fun saveClaudeKey(key: String) {
        val trimmed = key.trim()
        val cipherText = if (trimmed.isNotBlank()) KeyStoreCryptoHelper.encrypt(trimmed) else ""
        context.encryptedApiKeysDataStore.edit { prefs ->
            if (cipherText.isNotBlank()) {
                prefs[KEY_ENC_CLAUDE] = cipherText
            } else {
                prefs.remove(KEY_ENC_CLAUDE)
            }
        }
        KrishiAiKeyManager.saveApiKey(context, AiProvider.CLAUDE, trimmed)
    }

    /**
     * Saves key for a specific AI provider.
     */
    suspend fun saveApiKey(provider: AiProvider, key: String) {
        when (provider) {
            AiProvider.GEMINI -> saveGeminiKey(key)
            AiProvider.OPENAI -> saveOpenAiKey(key)
            AiProvider.CLAUDE -> saveClaudeKey(key)
            AiProvider.OFFLINE_BIO_SYNTHESIS -> { /* No key needed for offline mode */ }
        }
    }

    /**
     * Saves active AI provider.
     */
    suspend fun saveActiveProvider(providerId: String) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_PROVIDER] = providerId
        }
        KrishiAiKeyManager.setActiveProvider(context, AiProvider.fromId(providerId))
    }

    /**
     * Toggles whether the bundled developer fallback key is disabled.
     */
    suspend fun setDeveloperKeyDisabled(disabled: Boolean) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_DEV_KEY_DISABLED] = disabled
        }
        KrishiAiKeyManager.setDeveloperKeyDisabled(context, disabled)
    }

    /**
     * Toggles automatic offline fallback when remote API calls fail.
     */
    suspend fun setAutoOfflineFallback(enabled: Boolean) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_AUTO_OFFLINE] = enabled
        }
        KrishiAiKeyManager.setAutoOfflineFallbackEnabled(context, enabled)
    }

    /**
     * Clears a single provider's key.
     */
    suspend fun clearKey(provider: AiProvider) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            when (provider) {
                AiProvider.GEMINI -> prefs.remove(KEY_ENC_GEMINI)
                AiProvider.OPENAI -> prefs.remove(KEY_ENC_OPENAI)
                AiProvider.CLAUDE -> prefs.remove(KEY_ENC_CLAUDE)
                AiProvider.OFFLINE_BIO_SYNTHESIS -> {}
            }
        }
        KrishiAiKeyManager.removeApiKey(context, provider)
    }

    /**
     * Purges all keys and secures the app for open-source / hackathon submission.
     */
    suspend fun purgeAllKeys() {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs.remove(KEY_ENC_GEMINI)
            prefs.remove(KEY_ENC_OPENAI)
            prefs.remove(KEY_ENC_CLAUDE)
            prefs[KEY_DEV_KEY_DISABLED] = true
        }
        KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
    }
}
