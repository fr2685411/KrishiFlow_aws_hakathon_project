package com.example.data.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ApiKeyStoreTest {

    private lateinit var context: Context
    private lateinit var apiKeyStore: ApiKeyStore

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext<Context>()
        apiKeyStore = ApiKeyStore.getInstance(context)
        apiKeyStore.purgeAllKeys()
    }

    @Test
    fun testSaveAndRetrieveGeminiKey() = runTest {
        val testGeminiKey = "AIzaSyFakeGeminiKey123456789"
        apiKeyStore.saveGeminiKey(testGeminiKey)

        val retrieved = apiKeyStore.getGeminiKey()
        assertEquals(testGeminiKey, retrieved)

        val settings = apiKeyStore.settingsFlow.first()
        assertEquals(testGeminiKey, settings.geminiKey)
        // Check that ciphertext is encrypted and non-empty
        assertTrue(settings.encryptedGeminiCiphertext.isNotEmpty())
    }

    @Test
    fun testSaveAndRetrieveOpenAiKey() = runTest {
        val testOpenAiKey = "sk-proj-testOpenAiSecretKey998877"
        apiKeyStore.saveOpenAiKey(testOpenAiKey)

        val retrieved = apiKeyStore.getOpenAiKey()
        assertEquals(testOpenAiKey, retrieved)

        val settings = apiKeyStore.settingsFlow.first()
        assertEquals(testOpenAiKey, settings.openAiKey)
        assertTrue(settings.encryptedOpenAiCiphertext.isNotEmpty())
    }

    @Test
    fun testSaveAndRetrieveClaudeKey() = runTest {
        val testClaudeKey = "sk-ant-api03-claudeSecretKey554433"
        apiKeyStore.saveClaudeKey(testClaudeKey)

        val retrieved = apiKeyStore.getClaudeKey()
        assertEquals(testClaudeKey, retrieved)

        val settings = apiKeyStore.settingsFlow.first()
        assertEquals(testClaudeKey, settings.claudeKey)
        assertTrue(settings.encryptedClaudeCiphertext.isNotEmpty())
    }

    @Test
    fun testClearKeyAndPurgeAllKeys() = runTest {
        apiKeyStore.saveGeminiKey("AIzaSyGemini")
        apiKeyStore.saveOpenAiKey("sk-OpenAI")
        apiKeyStore.saveClaudeKey("sk-ant-Claude")

        assertEquals("AIzaSyGemini", apiKeyStore.getGeminiKey())
        assertEquals("sk-OpenAI", apiKeyStore.getOpenAiKey())
        assertEquals("sk-ant-Claude", apiKeyStore.getClaudeKey())

        apiKeyStore.clearKey(AiProvider.GEMINI)
        assertEquals("", apiKeyStore.getGeminiKey())
        assertEquals("sk-OpenAI", apiKeyStore.getOpenAiKey())

        apiKeyStore.purgeAllKeys()
        assertEquals("", apiKeyStore.getGeminiKey())
        assertEquals("", apiKeyStore.getOpenAiKey())
        assertEquals("", apiKeyStore.getClaudeKey())
    }
}
