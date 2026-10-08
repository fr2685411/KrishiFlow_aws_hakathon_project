package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiProvider
import com.example.data.ai.ConnectionTestResult
import com.example.data.ai.KrishiAiInferenceService
import com.example.data.ai.KrishiAiKeyManager
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.viewmodel.KrishiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppSettingsScreen(
    viewModel: KrishiViewModel,
    onNavigateBack: () -> Unit,
    onOpenThemeSelector: () -> Unit = {},
    onOpenAwsHackathonLab: () -> Unit = {},
    onOpenCloudArchitecture: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val inferenceService = remember { KrishiAiInferenceService(context) }

    // Active AI Engine selection state
    var activeProvider by remember { mutableStateOf(KrishiAiKeyManager.getActiveProvider(context)) }

    // Input States for the 3 major cloud providers
    var geminiKeyInput by remember { mutableStateOf(KrishiAiKeyManager.getApiKey(context, AiProvider.GEMINI)) }
    var openAiKeyInput by remember { mutableStateOf(KrishiAiKeyManager.getApiKey(context, AiProvider.OPENAI)) }
    var claudeKeyInput by remember { mutableStateOf(KrishiAiKeyManager.getApiKey(context, AiProvider.CLAUDE)) }

    // Mask toggle states
    var showGeminiKeyText by remember { mutableStateOf(false) }
    var showOpenAiKeyText by remember { mutableStateOf(false) }
    var showClaudeKeyText by remember { mutableStateOf(false) }

    // Selected models
    var selectedGeminiModel by remember { mutableStateOf(KrishiAiKeyManager.getSelectedModel(context, AiProvider.GEMINI)) }
    var selectedOpenAiModel by remember { mutableStateOf(KrishiAiKeyManager.getSelectedModel(context, AiProvider.OPENAI)) }
    var selectedClaudeModel by remember { mutableStateOf(KrishiAiKeyManager.getSelectedModel(context, AiProvider.CLAUDE)) }

    // Fallback & Safety Toggles
    var isAutoOfflineFallbackEnabled by remember { mutableStateOf(KrishiAiKeyManager.isAutoOfflineFallbackEnabled(context)) }
    var isDeveloperKeyDisabled by remember { mutableStateOf(KrishiAiKeyManager.isDeveloperKeyDisabled(context)) }

    // Live Connection Test States
    var testingProvider by remember { mutableStateOf<AiProvider?>(null) }
    var connectionTestResult by remember { mutableStateOf<ConnectionTestResult?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings & AI Inference (BYOK)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Bring Your Own Key • Safe Open-Source Mode",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            activeProvider = KrishiAiKeyManager.getActiveProvider(context)
                            geminiKeyInput = KrishiAiKeyManager.getApiKey(context, AiProvider.GEMINI)
                            openAiKeyInput = KrishiAiKeyManager.getApiKey(context, AiProvider.OPENAI)
                            claudeKeyInput = KrishiAiKeyManager.getApiKey(context, AiProvider.CLAUDE)
                            selectedGeminiModel = KrishiAiKeyManager.getSelectedModel(context, AiProvider.GEMINI)
                            selectedOpenAiModel = KrishiAiKeyManager.getSelectedModel(context, AiProvider.OPENAI)
                            selectedClaudeModel = KrishiAiKeyManager.getSelectedModel(context, AiProvider.CLAUDE)
                            isDeveloperKeyDisabled = KrishiAiKeyManager.isDeveloperKeyDisabled(context)
                            isAutoOfflineFallbackEnabled = KrishiAiKeyManager.isAutoOfflineFallbackEnabled(context)
                            Toast.makeText(context, "Settings refreshed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("settings_top_app_bar")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Feedback Banner
            AnimatedVisibility(visible = statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary)
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF166534),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Block, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Top Hero Card: BYOK & Open Source Explanation
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Bring Your Own Key (BYOK)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Local inference • Zero hardcoded credentials",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "To ensure 100% compliance with open-source hackathons and zero cloud bill leaks, all AI assistants (KrishiMitra AI, Crop Planning Advisor, Sell/Store Optimizer) support custom user API keys. Keys stay strictly encrypted in local Android app storage.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            // =========================================================================
            // SECTION 1: ACTIVE AI PROVIDER SELECTOR
            // =========================================================================
            Text(
                text = "1. Active AI Intelligence Engine",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose which model orchestrates live advice, voice commands, and market decisions:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AiProvider.values().forEach { provider ->
                            val isSelected = activeProvider == provider
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    activeProvider = provider
                                    KrishiAiKeyManager.setActiveProvider(context, provider)
                                    statusMessage = "Active engine switched to ${provider.displayName}"
                                },
                                label = {
                                    Text(
                                        text = provider.displayName,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (provider) {
                                            AiProvider.GEMINI -> Icons.Default.AutoAwesome
                                            AiProvider.OPENAI -> Icons.Default.CloudDone
                                            AiProvider.CLAUDE -> Icons.Default.Security
                                            AiProvider.OFFLINE_BIO_SYNTHESIS -> Icons.Default.Eco
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = GreenPrimary
                                ),
                                modifier = Modifier.testTag("provider_chip_${provider.id}")
                            )
                        }
                    }

                    // Active Engine Status Summary
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                            val keyStatus = when (activeProvider) {
                                AiProvider.OFFLINE_BIO_SYNTHESIS -> "Verified Offline ICAR Model (Zero API quota)"
                                else -> {
                                    val effKey = KrishiAiKeyManager.getEffectiveKey(context, activeProvider)
                                    if (effKey.isNotBlank()) "Key Configured: ${KrishiAiKeyManager.getMaskedKeyPreview(effKey)}"
                                    else "No key set (Will auto-fallback to ICAR Bio-Engine)"
                                }
                            }
                            Text(
                                text = "Current Engine: ${activeProvider.displayName} • $keyStatus",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // SECTION 2: INDIVIDUAL PROVIDER CARDS
            // =========================================================================
            Text(
                text = "2. Provider API Keys & Model Tiers",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // --- 2A. GOOGLE GEMINI (AI STUDIO) ---
            ProviderKeyCard(
                provider = AiProvider.GEMINI,
                title = "Google Gemini (Google AI Studio)",
                subtitle = "Fast, multimodal agronomy, Mandi maps & voice",
                keyInput = geminiKeyInput,
                onKeyInputChange = { geminiKeyInput = it },
                showKeyText = showGeminiKeyText,
                onToggleShowKey = { showGeminiKeyText = !showGeminiKeyText },
                selectedModel = selectedGeminiModel,
                onSelectModel = { model ->
                    selectedGeminiModel = model
                    KrishiAiKeyManager.setSelectedModel(context, AiProvider.GEMINI, model)
                },
                onSaveKey = {
                    KrishiAiKeyManager.saveApiKey(context, AiProvider.GEMINI, geminiKeyInput)
                    statusMessage = "Gemini API key saved securely!"
                },
                onClearKey = {
                    KrishiAiKeyManager.removeApiKey(context, AiProvider.GEMINI)
                    geminiKeyInput = ""
                    statusMessage = "Gemini API key removed."
                },
                onTestConnection = {
                    testingProvider = AiProvider.GEMINI
                    connectionTestResult = null
                    coroutineScope.launch {
                        val res = inferenceService.testProviderConnection(AiProvider.GEMINI, geminiKeyInput.ifBlank { null })
                        testingProvider = null
                        connectionTestResult = res
                    }
                },
                isTesting = testingProvider == AiProvider.GEMINI,
                testResult = if (connectionTestResult?.provider == AiProvider.GEMINI) connectionTestResult else null,
                testTagPrefix = "gemini"
            )

            // --- 2B. OPENAI (CHATGPT / GPT-4o) ---
            ProviderKeyCard(
                provider = AiProvider.OPENAI,
                title = "OpenAI (GPT-4o / GPT-4o-mini)",
                subtitle = "Advanced reasoning for agronomic decisions",
                keyInput = openAiKeyInput,
                onKeyInputChange = { openAiKeyInput = it },
                showKeyText = showOpenAiKeyText,
                onToggleShowKey = { showOpenAiKeyText = !showOpenAiKeyText },
                selectedModel = selectedOpenAiModel,
                onSelectModel = { model ->
                    selectedOpenAiModel = model
                    KrishiAiKeyManager.setSelectedModel(context, AiProvider.OPENAI, model)
                },
                onSaveKey = {
                    KrishiAiKeyManager.saveApiKey(context, AiProvider.OPENAI, openAiKeyInput)
                    statusMessage = "OpenAI API key saved securely!"
                },
                onClearKey = {
                    KrishiAiKeyManager.removeApiKey(context, AiProvider.OPENAI)
                    openAiKeyInput = ""
                    statusMessage = "OpenAI API key removed."
                },
                onTestConnection = {
                    testingProvider = AiProvider.OPENAI
                    connectionTestResult = null
                    coroutineScope.launch {
                        val res = inferenceService.testProviderConnection(AiProvider.OPENAI, openAiKeyInput.ifBlank { null })
                        testingProvider = null
                        connectionTestResult = res
                    }
                },
                isTesting = testingProvider == AiProvider.OPENAI,
                testResult = if (connectionTestResult?.provider == AiProvider.OPENAI) connectionTestResult else null,
                testTagPrefix = "openai"
            )

            // --- 2C. ANTHROPIC CLAUDE ---
            ProviderKeyCard(
                provider = AiProvider.CLAUDE,
                title = "Anthropic Claude (Claude 3.5)",
                subtitle = "Nuanced biological analysis & crop pathology",
                keyInput = claudeKeyInput,
                onKeyInputChange = { claudeKeyInput = it },
                showKeyText = showClaudeKeyText,
                onToggleShowKey = { showClaudeKeyText = !showClaudeKeyText },
                selectedModel = selectedClaudeModel,
                onSelectModel = { model ->
                    selectedClaudeModel = model
                    KrishiAiKeyManager.setSelectedModel(context, AiProvider.CLAUDE, model)
                },
                onSaveKey = {
                    KrishiAiKeyManager.saveApiKey(context, AiProvider.CLAUDE, claudeKeyInput)
                    statusMessage = "Anthropic Claude API key saved securely!"
                },
                onClearKey = {
                    KrishiAiKeyManager.removeApiKey(context, AiProvider.CLAUDE)
                    claudeKeyInput = ""
                    statusMessage = "Claude API key removed."
                },
                onTestConnection = {
                    testingProvider = AiProvider.CLAUDE
                    connectionTestResult = null
                    coroutineScope.launch {
                        val res = inferenceService.testProviderConnection(AiProvider.CLAUDE, claudeKeyInput.ifBlank { null })
                        testingProvider = null
                        connectionTestResult = res
                    }
                },
                isTesting = testingProvider == AiProvider.CLAUDE,
                testResult = if (connectionTestResult?.provider == AiProvider.CLAUDE) connectionTestResult else null,
                testTagPrefix = "claude"
            )

            // =========================================================================
            // SECTION 3: OFFLINE INFERENCE & RESILIENCY
            // =========================================================================
            Text(
                text = "3. Offline Resiliency & Fallback",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Fallback to Local Bio-Engine",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "If cloud API rate limits, timeouts, or empty keys occur, automatically answer using local ICAR agricultural heuristics.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isAutoOfflineFallbackEnabled,
                            onCheckedChange = { checked ->
                                isAutoOfflineFallbackEnabled = checked
                                KrishiAiKeyManager.setAutoOfflineFallbackEnabled(context, checked)
                                statusMessage = if (checked) "Auto-fallback enabled" else "Strict cloud mode enabled"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GreenPrimary
                            ),
                            modifier = Modifier.testTag("settings_auto_fallback_switch")
                        )
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "ICAR Agronomy Ruleset: Bundled & 100% active on-device",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // =========================================================================
            // SECTION 4: OPEN-SOURCE SUBMISSION & PUBLISHING SAFEGUARDS
            // =========================================================================
            Text(
                text = "4. Open-Source Submission & Privacy Protection",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFFDC2626)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                        Text(
                            text = "Publishing & Hackathon Safety Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Text(
                        text = "When preparing a video demo, submitting your code to GitHub, or exporting an APK, purge all stored keys to ensure NO personal tokens or developer quotas are leaked.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF7F1D1D)
                    )

                    // Toggle Developer Fallback Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Disable Bundled Developer Key Fallback",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Guarantees public APK never touches developer quota.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Switch(
                            checked = isDeveloperKeyDisabled,
                            onCheckedChange = { checked ->
                                isDeveloperKeyDisabled = checked
                                KrishiAiKeyManager.setDeveloperKeyDisabled(context, checked)
                                statusMessage = if (checked) "Developer key fallback blocked." else "Developer key fallback permitted."
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFDC2626)
                            ),
                            modifier = Modifier.testTag("settings_disable_dev_key_switch")
                        )
                    }

                    // Nuclear Purge Button
                    Button(
                        onClick = {
                            KrishiAiKeyManager.purgeAllKeysForOpenSourcePublishing(context)
                            geminiKeyInput = ""
                            openAiKeyInput = ""
                            claudeKeyInput = ""
                            isDeveloperKeyDisabled = true
                            statusMessage = "All keys wiped from device! Safe for public GitHub submission."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_purge_all_keys_btn")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Purge All Keys from Device", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (isDeveloperKeyDisabled && geminiKeyInput.isBlank() && openAiKeyInput.isBlank() && claudeKeyInput.isBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Status: Device sanitized. 100% Safe for public repository submission.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // SECTION 5: QUICK APP NAVIGATION & SHORTCUTS
            // =========================================================================
            Text(
                text = "5. Additional App Configuration",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenThemeSelector,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("settings_open_theme_btn")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Theme", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenAwsHackathonLab,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("settings_open_aws_lab_btn")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AWS Lab", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenCloudArchitecture,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("settings_open_cloud_guide_btn")
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Arch", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Reusable Card component for individual AI Model Providers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderKeyCard(
    provider: AiProvider,
    title: String,
    subtitle: String,
    keyInput: String,
    onKeyInputChange: (String) -> Unit,
    showKeyText: Boolean,
    onToggleShowKey: () -> Unit,
    selectedModel: String,
    onSelectModel: (String) -> Unit,
    onSaveKey: () -> Unit,
    onClearKey: () -> Unit,
    onTestConnection: () -> Unit,
    isTesting: Boolean,
    testResult: ConnectionTestResult?,
    testTagPrefix: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val hasKey = keyInput.isNotBlank()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasKey) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (hasKey) "Key Set" else "No Key",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasKey) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Model Tier Selection
            Text(
                text = "Model Tier:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                provider.supportedModels.forEach { model ->
                    val isSelected = selectedModel == model
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectModel(model) },
                        label = { Text(text = model, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = GreenPrimary
                        ),
                        modifier = Modifier.testTag("${testTagPrefix}_model_$model")
                    )
                }
            }

            // Secure Key Input Field
            OutlinedTextField(
                value = keyInput,
                onValueChange = onKeyInputChange,
                label = { Text("${provider.displayName} API Key") },
                placeholder = { Text(provider.keyPrefixHint) },
                singleLine = true,
                visualTransformation = if (showKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onToggleShowKey) {
                        Icon(
                            imageVector = if (showKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showKeyText) "Hide key" else "Show key"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("${testTagPrefix}_key_input")
            )

            // Action Buttons: Save, Test, Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Key
                Button(
                    onClick = onSaveKey,
                    enabled = keyInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}_save_key_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Test Connection
                OutlinedButton(
                    onClick = onTestConnection,
                    enabled = !isTesting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("${testTagPrefix}_test_key_btn")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testing...", fontSize = 11.5.sp)
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Connection", fontSize = 11.5.sp)
                    }
                }

                // Clear Key
                OutlinedButton(
                    onClick = onClearKey,
                    enabled = keyInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("${testTagPrefix}_clear_key_btn")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                }
            }

            // Test Connection Result Display
            AnimatedVisibility(visible = testResult != null) {
                testResult?.let { result ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (result.isSuccess) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (result.isSuccess) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (result.isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (result.isSuccess) Color(0xFF166534) else Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (result.isSuccess) "Success (${result.latencyMs}ms)" else "Connection Failed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (result.isSuccess) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = result.message,
                                    fontSize = 11.sp,
                                    color = if (result.isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
