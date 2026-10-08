package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig

/**
 * Supported AI / LLM Providers for KrishiFlow inference.
 */
enum class AiProvider(
    val id: String,
    val displayName: String,
    val defaultModel: String,
    val supportedModels: List<String>,
    val keyPrefixHint: String,
    val docsUrl: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        defaultModel = "gemini-1.5-flash",
        supportedModels = listOf("gemini-1.5-flash", "gemini-2.0-flash", "gemini-1.5-pro"),
        keyPrefixHint = "AIzaSy...",
        docsUrl = "https://aistudio.google.com/app/apikey"
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI",
        defaultModel = "gpt-4o-mini",
        supportedModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-3.5-turbo"),
        keyPrefixHint = "sk-proj-...",
        docsUrl = "https://platform.openai.com/api-keys"
    ),
    CLAUDE(
        id = "claude",
        displayName = "Anthropic Claude",
        defaultModel = "claude-3-5-haiku-20241022",
        supportedModels = listOf("claude-3-5-haiku-20241022", "claude-3-5-sonnet-20241022"),
        keyPrefixHint = "sk-ant-...",
        docsUrl = "https://console.anthropic.com/settings/keys"
    ),
    OFFLINE_BIO_SYNTHESIS(
        id = "offline",
        displayName = "ICAR Bio-Engine (Offline)",
        defaultModel = "icar-agronomy-v2",
        supportedModels = listOf("icar-agronomy-v2"),
        keyPrefixHint = "No key required",
        docsUrl = "https://icar.org.in"
    );

    companion object {
        fun fromId(id: String): AiProvider =
            values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GEMINI
    }
}

/**
 * Centralized, secure storage for Bring-Your-Own-Key (BYOK) AI inference in KrishiFlow.
 * 
 * Provides:
 * 1. Independent secure storage for Gemini, OpenAI, and Claude API keys.
 * 2. Active provider and model selection.
 * 3. Complete publishing safety controls (disabling developer key fallback and purging keys)
 *    to prepare the application for open-source submission and public distribution.
 * 4. Automatic offline fallback configuration.
 */
object KrishiAiKeyManager {

    private const val PREFS_NAME = "krishi_ai_byok_prefs"

    // SharedPreferences Keys
    private const val KEY_GEMINI_API_KEY = "user_gemini_api_key"
    private const val KEY_OPENAI_API_KEY = "user_openai_api_key"
    private const val KEY_CLAUDE_API_KEY = "user_claude_api_key"

    private const val KEY_ACTIVE_PROVIDER = "active_ai_provider"
    private const val KEY_GEMINI_MODEL = "selected_gemini_model"
    private const val KEY_OPENAI_MODEL = "selected_openai_model"
    private const val KEY_CLAUDE_MODEL = "selected_claude_model"

    private const val KEY_DEVELOPER_KEY_DISABLED = "developer_bundled_key_disabled"
    private const val KEY_AUTO_OFFLINE_FALLBACK = "auto_offline_fallback_enabled"

    // ---------------------------------------------------------
    // API Key Getters & Setters
    // ---------------------------------------------------------

    fun getApiKey(context: Context, provider: AiProvider): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (provider) {
            AiProvider.GEMINI -> prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
            AiProvider.OPENAI -> prefs.getString(KEY_OPENAI_API_KEY, "") ?: ""
            AiProvider.CLAUDE -> prefs.getString(KEY_CLAUDE_API_KEY, "") ?: ""
            AiProvider.OFFLINE_BIO_SYNTHESIS -> ""
        }
    }

    fun saveApiKey(context: Context, provider: AiProvider, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefKey = when (provider) {
            AiProvider.GEMINI -> KEY_GEMINI_API_KEY
            AiProvider.OPENAI -> KEY_OPENAI_API_KEY
            AiProvider.CLAUDE -> KEY_CLAUDE_API_KEY
            AiProvider.OFFLINE_BIO_SYNTHESIS -> return
        }
        prefs.edit().putString(prefKey, key.trim()).apply()
        Log.i("KrishiAiKeyManager", "Saved API key for provider ${provider.displayName}")
    }

    fun removeApiKey(context: Context, provider: AiProvider) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefKey = when (provider) {
            AiProvider.GEMINI -> KEY_GEMINI_API_KEY
            AiProvider.OPENAI -> KEY_OPENAI_API_KEY
            AiProvider.CLAUDE -> KEY_CLAUDE_API_KEY
            AiProvider.OFFLINE_BIO_SYNTHESIS -> return
        }
        prefs.edit().remove(prefKey).apply()
        Log.i("KrishiAiKeyManager", "Removed API key for provider ${provider.displayName}")
    }

    // ---------------------------------------------------------
    // Active Provider & Model Selection
    // ---------------------------------------------------------

    fun getActiveProvider(context: Context): AiProvider {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_ACTIVE_PROVIDER, AiProvider.GEMINI.id) ?: AiProvider.GEMINI.id
        return AiProvider.fromId(id)
    }

    fun setActiveProvider(context: Context, provider: AiProvider) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_PROVIDER, provider.id).apply()
        Log.i("KrishiAiKeyManager", "Set active AI provider to ${provider.displayName}")
    }

    fun getSelectedModel(context: Context, provider: AiProvider): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (provider) {
            AiProvider.GEMINI -> prefs.getString(KEY_GEMINI_MODEL, AiProvider.GEMINI.defaultModel) ?: AiProvider.GEMINI.defaultModel
            AiProvider.OPENAI -> prefs.getString(KEY_OPENAI_MODEL, AiProvider.OPENAI.defaultModel) ?: AiProvider.OPENAI.defaultModel
            AiProvider.CLAUDE -> prefs.getString(KEY_CLAUDE_MODEL, AiProvider.CLAUDE.defaultModel) ?: AiProvider.CLAUDE.defaultModel
            AiProvider.OFFLINE_BIO_SYNTHESIS -> AiProvider.OFFLINE_BIO_SYNTHESIS.defaultModel
        }
    }

    fun setSelectedModel(context: Context, provider: AiProvider, model: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefKey = when (provider) {
            AiProvider.GEMINI -> KEY_GEMINI_MODEL
            AiProvider.OPENAI -> KEY_OPENAI_MODEL
            AiProvider.CLAUDE -> KEY_CLAUDE_MODEL
            AiProvider.OFFLINE_BIO_SYNTHESIS -> return
        }
        prefs.edit().putString(prefKey, model.trim()).apply()
    }

    // ---------------------------------------------------------
    // Offline Fallback Configuration
    // ---------------------------------------------------------

    fun isAutoOfflineFallbackEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_OFFLINE_FALLBACK, true)
    }

    fun setAutoOfflineFallbackEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_OFFLINE_FALLBACK, enabled).apply()
    }

    // ---------------------------------------------------------
    // Open-Source Submission & Publishing Safeguards
    // ---------------------------------------------------------

    fun isDeveloperKeyDisabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEVELOPER_KEY_DISABLED, false)
    }

    fun setDeveloperKeyDisabled(context: Context, disabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DEVELOPER_KEY_DISABLED, disabled).apply()
    }

    /**
     * Complete wipe of all user-entered keys and locking of developer fallback.
     * Perfect for open-source code submission, demo recording, and public GitHub pushes.
     */
    fun purgeAllKeysForOpenSourcePublishing(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_GEMINI_API_KEY)
            .remove(KEY_OPENAI_API_KEY)
            .remove(KEY_CLAUDE_API_KEY)
            .putBoolean(KEY_DEVELOPER_KEY_DISABLED, true)
            .apply()
        Log.i("KrishiAiKeyManager", "All AI API keys purged for open-source submission safety.")
    }

    // ---------------------------------------------------------
    // Effective Key Resolution
    // ---------------------------------------------------------

    fun getEffectiveKey(context: Context, provider: AiProvider): String {
        val userKey = getApiKey(context, provider)
        if (userKey.isNotBlank()) {
            return userKey
        }

        // Only Gemini has an optional build-time developer fallback
        if (provider == AiProvider.GEMINI) {
            if (isDeveloperKeyDisabled(context)) {
                return ""
            }
            return try {
                val buildKey = BuildConfig.GEMINI_API_KEY
                if (buildKey.isBlank() ||
                    buildKey == "DEFAULT_GEMINI_API_KEY" ||
                    buildKey == "MY_GEMINI_API_KEY" ||
                    buildKey == "DEFAULT_KEY" ||
                    buildKey == "null"
                ) {
                    ""
                } else {
                    buildKey
                }
            } catch (_: Exception) {
                ""
            }
        }

        return ""
    }

    fun hasActiveKeyForProvider(context: Context, provider: AiProvider): Boolean {
        return getEffectiveKey(context, provider).isNotBlank()
    }

    fun hasAnyActiveKey(context: Context): Boolean {
        return hasActiveKeyForProvider(context, AiProvider.GEMINI) ||
                hasActiveKeyForProvider(context, AiProvider.OPENAI) ||
                hasActiveKeyForProvider(context, AiProvider.CLAUDE)
    }

    fun getMaskedKeyPreview(key: String): String {
        if (key.isBlank()) return "Not configured"
        return if (key.length <= 8) {
            "****"
        } else {
            "${key.take(4)}...${key.takeLast(4)}"
        }
    }
}
