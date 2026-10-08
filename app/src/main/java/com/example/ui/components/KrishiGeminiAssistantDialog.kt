package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.gemini.GeminiChatMessage
import com.example.data.gemini.GeminiMessageSender
import com.example.data.gemini.KrishiGeminiAssistantClient
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * KrishiMitra Gemini AI Full-Screen / Modal Dialog.
 * Allows the farmer or buyer to:
 * 1. Input or update their custom Gemini API key (persisted securely in app SharedPreferences).
 * 2. See that KrishiFlow explains to the AI how it has to work inside this application environment.
 * 3. Chat with the custom agricultural AI via Text or Voice (STT) with automatic TTS audio read-aloud.
 * 4. Control app features directly through actionable AI suggestions:
 *    - Open Sustainable Shop
 *    - Open Produce / Inventory Dashboard
 *    - Open Crop Planning Advisor
 *    - Open Yield Calculator
 *    - Open Route Map
 *    - Add new Harvest lot
 *    - Trigger Cloud sync
 */
@Composable
fun KrishiGeminiAssistantDialog(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    activeRole: String,
    onDismiss: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProduceDashboard: () -> Unit,
    onNavigateToCropAdvisor: () -> Unit,
    onNavigateToYieldCalculator: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onOpenAddHarvest: () -> Unit,
    onNavigateToSurplus: () -> Unit,
    onNavigateToHarvestLogs: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val assistantClient = remember { KrishiGeminiAssistantClient(context) }

    // State for API Key settings & Publishing security
    var showApiKeyConfigDialog by remember { mutableStateOf(false) }
    var userApiKeyInput by remember { mutableStateOf(assistantClient.getCustomApiKey()) }
    var hasCustomKey by remember { mutableStateOf(assistantClient.getCustomApiKey().isNotBlank()) }
    var isDeveloperKeyDisabled by remember { mutableStateOf(assistantClient.isDeveloperKeyDisabled()) }

    // Chat conversation state
    val messages = remember {
        mutableStateListOf(
            GeminiChatMessage(
                sender = GeminiMessageSender.ASSISTANT,
                text = "राम राम किसान भाई! I am KrishiMitra AI (कृषि मित्र), your personalized Gemini agro-assistant.\n\nI have analyzed KrishiFlow's active features and live data for your farm. Ask me anything about crop planning, market mandi prices, pest control, or tell me to control the app for you!",
                suggestedAction = "EXPLAIN_APP"
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isTtsEnabled by remember { mutableStateOf(true) }
    var isListeningVoice by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Setup Text-To-Speech (TTS)
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var initializedTts: TextToSpeech? = null
        initializedTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Prefer Hindi if available, otherwise default Locale
                val hindiLocale = Locale("hi", "IN")
                val result = initializedTts?.setLanguage(hindiLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    initializedTts?.language = Locale.ENGLISH
                }
            }
        }
        textToSpeech = initializedTts
        onDispose {
            initializedTts?.stop()
            initializedTts?.shutdown()
        }
    }

    fun speakText(text: String) {
        if (!isTtsEnabled) return
        val cleanText = text.replace(Regex("\\[ACTION:[A-Z_]+\\]"), "").trim()
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "KrishiMitraTTS")
    }

    // Voice recognition launcher (Speech to Text)
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningVoice = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
            }
        }
    }

    fun startVoiceInput() {
        try {
            isListeningVoice = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "कृषि मित्र से बोलें / Speak to KrishiMitra AI...")
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            isListeningVoice = false
            Toast.makeText(context, "Voice input not available on this device", Toast.LENGTH_SHORT).show()
        }
    }

    // Live environment parameters from KrishiViewModel
    val weatherData by viewModel.weatherData.collectAsState()
    val totalStockKg = uiState.batches.sumOf { it.availableQuantityKg }
    val batchesSummary = if (uiState.batches.isEmpty()) "No lots listed yet" else uiState.batches.take(4).joinToString { "${it.cropName} (${it.availableQuantityKg.toInt()}kg, ₹${it.farmerAskingPricePerKg}/kg)" }
    val mandiSummary = uiState.mandiRates.take(3).joinToString { "${it.commodity}: ₹${it.maxPricePerKg}/kg" }

    fun handleSend(textToSend: String) {
        if (textToSend.isBlank()) return
        val userMsg = GeminiChatMessage(
            sender = GeminiMessageSender.USER,
            text = textToSend.trim()
        )
        messages.add(userMsg)
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            // Scroll to bottom
            listState.animateScrollToItem(messages.size - 1)

            // Construct full dynamic system directive
            val systemContext = assistantClient.buildSystemContext(
                totalStockKg = totalStockKg,
                activeBatchesSummary = batchesSummary,
                currentLocation = weatherData.locationName,
                temperatureC = weatherData.currentTemp,
                weatherCondition = weatherData.conditionDescription,
                activeRole = activeRole
            )

            val reply = assistantClient.sendMessage(
                conversationHistory = messages,
                userPrompt = userMsg.text,
                systemContext = systemContext
            )

            messages.add(reply)
            isThinking = false
            speakText(reply.text)
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun executeAction(action: String) {
        when (action) {
            "OPEN_SHOP" -> {
                onDismiss()
                onNavigateToShop()
            }
            "OPEN_PRODUCE" -> {
                onDismiss()
                onNavigateToProduceDashboard()
            }
            "OPEN_CROP_ADVISOR" -> {
                onDismiss()
                onNavigateToCropAdvisor()
            }
            "OPEN_CALCULATOR" -> {
                onDismiss()
                onNavigateToYieldCalculator()
            }
            "OPEN_MAPS" -> {
                onDismiss()
                onNavigateToMaps()
            }
            "OPEN_LIST_HARVEST" -> {
                onDismiss()
                onOpenAddHarvest()
            }
            "OPEN_SURPLUS" -> {
                onDismiss()
                onNavigateToSurplus()
            }
            "OPEN_LOGS" -> {
                onDismiss()
                onNavigateToHarvestLogs()
            }
            "OPEN_SETTINGS" -> {
                onDismiss()
                onOpenSettings()
            }
            "TRIGGER_SYNC" -> {
                viewModel.triggerWorkManagerSync()
                messages.add(
                    GeminiChatMessage(
                        sender = GeminiMessageSender.SYSTEM,
                        text = "WorkManager Cloud Sync triggered. Uploading on-device Room cache to Cloud Firestore."
                    )
                )
            }
            "EXPLAIN_APP" -> {
                messages.add(
                    GeminiChatMessage(
                        sender = GeminiMessageSender.ASSISTANT,
                        text = "KrishiFlow App Architecture Explained to AI:\n• Zero-brokerage direct trading between Kisan & Vyapar.\n• Live Mandi price intelligence & profit timing.\n• QR & Barcode harvest tracking with offline Room SQLite caching.\n• Spoilage prediction & dynamic bio-surplus priority.\n• You can ask me to navigate anywhere or give crop advice!"
                    )
                )
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("gemini_assistant_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF15803D),
                                    Color(0xFF166534),
                                    Color(0xFF0F766E)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFEF08A),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "KrishiMitra AI",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF22C55E).copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = if (hasCustomKey) "Custom Key Active" else "Default Model",
                                            color = Color(0xFFBBF7D0),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Voice & Text Assistant • gemini-3.5-flash",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFDCFCE7),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // TTS Toggle Button
                            IconButton(
                                onClick = {
                                    isTtsEnabled = !isTtsEnabled
                                    if (!isTtsEnabled) textToSpeech?.stop()
                                },
                                modifier = Modifier.testTag("gemini_tts_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Toggle Audio Read-Aloud",
                                    tint = Color.White
                                )
                            }

                            // API Key Configuration Button
                            IconButton(
                                onClick = { showApiKeyConfigDialog = true },
                                modifier = Modifier.testTag("gemini_api_key_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Configure Gemini API Key",
                                    tint = if (hasCustomKey) Color(0xFFFEF08A) else Color.White
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("gemini_assistant_close_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Assistant",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // AI Environment Explanation Banner
                Surface(
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🌱 App State Aware: ${weatherData.locationName} (${weatherData.currentTemp.toInt()}°C) • ${totalStockKg.toInt()} kg Stock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "The app explains features, mandi prices, and actions directly to Gemini.",
                                fontSize = 10.sp,
                                color = Color(0xFF15803D)
                            )
                        }

                        TextButton(
                            onClick = {
                                handleSend("Explain how you operate in KrishiFlow and what features you control.")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("explain_app_env_btn")
                        ) {
                            Text(
                                text = "How AI Works",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Quick Prompt Suggestion Chips
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Quick Voice & Text Commands:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickCommandChip(
                            text = "🛍️ Open Shop",
                            onClick = { handleSend("Take me to the sustainable inputs shop to buy bio-fertilizers.") }
                        )
                        QuickCommandChip(
                            text = "🌾 Advisory",
                            onClick = { handleSend("What crop should I plan for my black clay soil this season?") }
                        )
                        QuickCommandChip(
                            text = "📊 Stock",
                            onClick = { handleSend("Show my visual inventory dashboard and tell me spoilage risks.") }
                        )
                        QuickCommandChip(
                            text = "🗺️ Map",
                            onClick = { handleSend("Open the mandi logistics route map.") }
                        )
                    }
                }

                HorizontalDivider()

                // Messages Chat View
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageItem(
                            message = msg,
                            onExecuteAction = { action -> executeAction(action) },
                            onSpeak = { text -> speakText(text) }
                        )
                    }

                    if (isThinking) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("gemini_thinking_indicator")
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = GreenPrimary
                                )
                                Text(
                                    text = "KrishiMitra is reasoning with Gemini AI...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Bottom Input Area (Text & Voice)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Voice Microphone Button
                        IconButton(
                            onClick = { startVoiceInput() },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isListeningVoice) SpoilageRed else GreenLight)
                                .testTag("gemini_voice_mic_btn")
                        ) {
                            Icon(
                                imageVector = if (isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListeningVoice) Color.White else GreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Text Input Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask KrishiMitra in Hindi or English...", fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gemini_chat_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 3,
                            singleLine = false
                        )

                        // Send Button
                        IconButton(
                            onClick = { handleSend(inputText) },
                            enabled = inputText.isNotBlank() && !isThinking,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (inputText.isNotBlank() && !isThinking) GreenPrimary else Color.Gray.copy(alpha = 0.3f))
                                .testTag("gemini_send_prompt_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Prompt",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal to Input or Edit User's Gemini API Key
    if (showApiKeyConfigDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyConfigDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = GreenPrimary)
                    Text("Your Gemini API Key", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your Google Gemini API key to activate custom AI reasoning, voice control, and real-time agronomic analytics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = userApiKeyInput,
                        onValueChange = { userApiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_gemini_api_key_input"),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = {
                            showApiKeyConfigDialog = false
                            onDismiss()
                            onOpenSettings()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_open_full_settings_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure OpenAI, Claude & Gemini Settings", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 How to get a free API key:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "1. Visit aistudio.google.com\n2. Click 'Get API key'\n3. Paste the key here and tap Save.\nOnce saved, KrishiFlow will explain its environment and tools to the AI automatically.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    // Publishing Protection & API Key Purge Section
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = SpoilageRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Publishing & Key Protection",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Text(
                                text = "Before publishing, export, or distribution, remove your keys so public users cannot consume your AI quota.",
                                fontSize = 10.sp,
                                color = Color(0xFF7F1D1D)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Remove User Key
                                if (hasCustomKey) {
                                    OutlinedButton(
                                        onClick = {
                                            assistantClient.clearCustomApiKey()
                                            userApiKeyInput = ""
                                            hasCustomKey = false
                                            Toast.makeText(context, "API Key removed successfully.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpoilageRed),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SpoilageRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("remove_user_api_key_btn")
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove My Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 2. Purge & Block All Keys for Publishing
                                Button(
                                    onClick = {
                                        assistantClient.purgeAllKeysForPublishing()
                                        userApiKeyInput = ""
                                        hasCustomKey = false
                                        isDeveloperKeyDisabled = true
                                        Toast.makeText(context, "All keys purged & blocked for publishing!", Toast.LENGTH_LONG).show()
                                        messages.add(
                                            GeminiChatMessage(
                                                sender = GeminiMessageSender.SYSTEM,
                                                text = "🔒 Publish Mode Active: All stored and bundled keys have been purged. Public users will run in offline synthesis mode or must supply their own key."
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SpoilageRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("purge_all_keys_publish_btn")
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Purge for Publishing", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (isDeveloperKeyDisabled) {
                                Text(
                                    text = "✓ Bundled developer key is blocked. App is safe for store release.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userApiKeyInput.isNotBlank()) {
                            assistantClient.saveCustomApiKey(userApiKeyInput.trim())
                            hasCustomKey = true
                            Toast.makeText(context, "Gemini API key saved securely!", Toast.LENGTH_SHORT).show()
                            showApiKeyConfigDialog = false
                            // Add confirmation message
                            messages.add(
                                GeminiChatMessage(
                                    sender = GeminiMessageSender.SYSTEM,
                                    text = "Your custom Gemini API Key has been applied. KrishiMitra AI is fully primed with KrishiFlow's inventory and navigation system."
                                )
                            )
                        } else {
                            showApiKeyConfigDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    modifier = Modifier.testTag("save_custom_gemini_api_key_btn")
                ) {
                    Text("Save API Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyConfigDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickCommandChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F4F6),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("quick_command_chip_${text.filter { it.isLetterOrDigit() }}")
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: GeminiChatMessage,
    onExecuteAction: (String) -> Unit,
    onSpeak: (String) -> Unit
) {
    val isUser = message.sender == GeminiMessageSender.USER
    val isSystem = message.sender == GeminiMessageSender.SYSTEM

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isSystem) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF3C7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚙️ ${message.text}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF78350F),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            Row(
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF166534)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 16.dp
                    ),
                    color = if (isUser) GreenPrimary else Color(0xFFF1F5F9),
                    tonalElevation = if (isUser) 2.dp else 1.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isUser) Color.White else Color(0xFF1E293B),
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            )
                        )

                        // If AI attached an action button, display actionable trigger
                        if (!isUser && message.suggestedAction != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val (actionLabel, actionColor) = when (message.suggestedAction) {
                                "OPEN_SHOP" -> "🛍️ Open Sustainable Shop" to Color(0xFF166534)
                                "OPEN_PRODUCE" -> "📊 Open Produce Dashboard" to Color(0xFF166534)
                                "OPEN_CROP_ADVISOR" -> "🌾 Open Crop Advisor" to Color(0xFF166534)
                                "OPEN_CALCULATOR" -> "🧮 Open Yield Calculator" to Color(0xFFB45309)
                                "OPEN_MAPS" -> "🗺️ View Mandi Route Map" to Color(0xFF0369A1)
                                "OPEN_LIST_HARVEST" -> "➕ Add Harvest Listing" to Color(0xFF15803D)
                                "OPEN_SURPLUS" -> "⚡ Open Zero-Waste Surplus" to Color(0xFFDC2626)
                                "OPEN_LOGS" -> "📜 View Sales History PDF" to Color(0xFF4338CA)
                                "OPEN_SETTINGS" -> "⚙️ Open AI Key Settings" to Color(0xFF7C3AED)
                                else -> "✨ Take App Action" to Color(0xFF166534)
                            }

                            Button(
                                onClick = { onExecuteAction(message.suggestedAction) },
                                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("ai_action_btn_${message.suggestedAction}")
                            ) {
                                Text(
                                    text = actionLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Audio repeat button for AI messages
                        if (!isUser) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(
                                    onClick = { onSpeak(message.text) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speak aloud",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
