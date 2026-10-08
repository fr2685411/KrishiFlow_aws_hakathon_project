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
import com.example.data.security.KeyStoreCryptoHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// Shared singleton Preferences DataStore for Encrypted API Keys
val Context.encryptedApiKeysDataStore: DataStore<Preferences> by preferencesDataStore(name = "krishiflow_encrypted_byok_prefs")

data class DecryptedApiKeySettings(
    val geminiKey: String = "",
    val openAiKey: String = "",
    val claudeKey: String = "",
    val activeProvider: String = "gemini",
    val isDeveloperKeyDisabled: Boolean = false,
    val isAutoOfflineFallback: Boolean = true,
    // Ciphertext previews to verify on-device encryption in the UI
    val encryptedGeminiCiphertext: String = "",
    val encryptedOpenAiCiphertext: String = "",
    val encryptedClaudeCiphertext: String = ""
)

/**
 * DataStore utility class for persisting encrypted API keys locally on Android.
 * 
 * Flow:
 * Plaintext Key -> AES-256-GCM Encryption (AndroidKeyStore) -> Stored in Preferences DataStore.
 * Preferences DataStore -> AES-256-GCM Decryption (AndroidKeyStore) -> Plaintext Key in Memory.
 * 
 * Never writes raw API keys to plaintext persistent storage.
 */
object EncryptedApiKeyDataStore {

    val KEY_ENC_GEMINI = stringPreferencesKey("enc_gemini_api_key")
    val KEY_ENC_OPENAI = stringPreferencesKey("enc_openai_api_key")
    val KEY_ENC_CLAUDE = stringPreferencesKey("enc_claude_api_key")
    val KEY_ACTIVE_PROVIDER = stringPreferencesKey("active_ai_provider")
    val KEY_DEV_KEY_DISABLED = booleanPreferencesKey("developer_key_disabled")
    val KEY_AUTO_OFFLINE = booleanPreferencesKey("auto_offline_fallback")

    /**
     * Observes reactive DataStore changes and decrypts stored ciphertexts automatically.
     */
    fun getDecryptedSettingsFlow(context: Context): Flow<DecryptedApiKeySettings> {
        return context.encryptedApiKeysDataStore.data
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

                DecryptedApiKeySettings(
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
    }

    /**
     * Encrypts and persists Google Gemini API key into DataStore.
     */
    suspend fun saveGeminiKey(context: Context, plainKey: String) {
        val trimmed = plainKey.trim()
        val cipherText = if (trimmed.isNotBlank()) KeyStoreCryptoHelper.encrypt(trimmed) else ""
        context.encryptedApiKeysDataStore.edit { prefs ->
            if (cipherText.isNotBlank()) {
                prefs[KEY_ENC_GEMINI] = cipherText
            } else {
                prefs.remove(KEY_ENC_GEMINI)
            }
        }
        // Mirror to memory / preferences for immediate runtime inference
        KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, trimmed)
    }

    /**
     * Encrypts and persists OpenAI API key into DataStore.
     */
    suspend fun saveOpenAiKey(context: Context, plainKey: String) {
        val trimmed = plainKey.trim()
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
     * Encrypts and persists Anthropic Claude API key into DataStore.
     */
    suspend fun saveClaudeKey(context: Context, plainKey: String) {
        val trimmed = plainKey.trim()
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
     * Persists active AI provider preference.
     */
    suspend fun saveActiveProvider(context: Context, providerId: String) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_PROVIDER] = providerId
        }
        KrishiAiKeyManager.setActiveProvider(context, AiProvider.fromId(providerId))
    }

    /**
     * Toggles whether developer bundled key fallback is disabled.
     */
    suspend fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_DEV_KEY_DISABLED] = disabled
        }
        KrishiAiKeyManager.setDeveloperKeyDisabled(context, disabled)
    }

    /**
     * Toggles whether auto-fallback to local bio-engine is enabled.
     */
    suspend fun setAutoOfflineFallback(context: Context, enabled: Boolean) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs[KEY_AUTO_OFFLINE] = enabled
        }
        KrishiAiKeyManager.setAutoOfflineFallbackEnabled(context, enabled)
    }

    /**
     * Removes a single provider key from DataStore.
     */
    suspend fun clearKey(context: Context, provider: AiProvider) {
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
     * Purges all keys and locks developer fallback for public open-source submission.
     */
    suspend fun purgeAllKeys(context: Context) {
        context.encryptedApiKeysDataStore.edit { prefs ->
            prefs.remove(KEY_ENC_GEMINI)
            prefs.remove(KEY_ENC_OPENAI)
            prefs.remove(KEY_ENC_CLAUDE)
            prefs[KEY_DEV_KEY_DISABLED] = true
        }
        KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
    }

    /**
     * Synchronously resolves a decrypted key on-demand.
     */
    suspend fun getDecryptedKey(context: Context, provider: AiProvider): String {
        val prefs = context.encryptedApiKeysDataStore.data.first()
        val cipherText = when (provider) {
            AiProvider.GEMINI -> prefs[KEY_ENC_GEMINI] ?: ""
            AiProvider.OPENAI -> prefs[KEY_ENC_OPENAI] ?: ""
            AiProvider.CLAUDE -> prefs[KEY_ENC_CLAUDE] ?: ""
            AiProvider.OFFLINE_BIO_SYNTHESIS -> ""
        }
        return KeyStoreCryptoHelper.decrypt(cipherText)
    }
}
