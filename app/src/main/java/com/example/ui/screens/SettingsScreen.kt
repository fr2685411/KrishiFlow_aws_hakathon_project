package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.AiProvider
import com.example.data.ai.ConnectionTestResult
import com.example.data.ai.KrishiAiInferenceService
import com.example.data.datastore.ApiKeyDataStore
import com.example.data.datastore.ApiKeySettingsState
import com.example.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

/**
 * Compose SettingsScreen that provides dedicated TextField components for:
 * - Google Gemini API Key
 * - OpenAI API Key
 * - Anthropic Claude API Key
 * 
 * Persists all keys locally using AndroidX DataStore (Preferences DataStore)
 * so they are never hardcoded in the codebase.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val inferenceService = remember { KrishiAiInferenceService(context) }

    // Collect reactive state from AndroidX DataStore
    val dataStoreState by ApiKeyDataStore.getSettingsFlow(context)
        .collectAsStateWithLifecycle(initialValue = ApiKeySettingsState())

    // Local mutable state for TextField inputs
    var geminiKeyInput by remember { mutableStateOf("") }
    var openAiKeyInput by remember { mutableStateOf("") }
    var claudeKeyInput by remember { mutableStateOf("") }

    // Sync DataStore values to local input states upon initialization
    LaunchedEffect(dataStoreState) {
        if (geminiKeyInput.isEmpty() && dataStoreState.geminiKey.isNotEmpty()) {
            geminiKeyInput = dataStoreState.geminiKey
        }
        if (openAiKeyInput.isEmpty() && dataStoreState.openAiKey.isNotEmpty()) {
            openAiKeyInput = dataStoreState.openAiKey
        }
        if (claudeKeyInput.isEmpty() && dataStoreState.claudeKey.isNotEmpty()) {
            claudeKeyInput = dataStoreState.claudeKey
        }
    }

    // Password visibility toggles
    var showGeminiKeyText by remember { mutableStateOf(false) }
    var showOpenAiKeyText by remember { mutableStateOf(false) }
    var showClaudeKeyText by remember { mutableStateOf(false) }

    // Test connection states
    var testingProvider by remember { mutableStateOf<AiProvider?>(null) }
    var testResult by remember { mutableStateOf<ConnectionTestResult?>(null) }
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
                            text = "Settings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Local DataStore • BYOK Inference",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
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
                            geminiKeyInput = dataStoreState.geminiKey
                            openAiKeyInput = dataStoreState.openAiKey
                            claudeKeyInput = dataStoreState.claudeKey
                            Toast.makeText(context, "Loaded from DataStore", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("settings_reload_datastore_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload from DataStore")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("settings_screen_top_bar")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Status Notice
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

            // Hero Banner: DataStore Persistence
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
                                text = "Zero Hardcoded Credentials",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Persisted securely in AndroidX Preferences DataStore",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Enter your personal API keys below for local inference. Keys are written asynchronously to your device's local DataStore and are never committed to Git or hardcoded in the codebase.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            }

            // =========================================================================
            // ACTIVE ENGINE SELECTOR
            // =========================================================================
            Text(
                text = "Active AI Provider",
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
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AiProvider.GEMINI to "Google Gemini",
                            AiProvider.OPENAI to "OpenAI GPT",
                            AiProvider.CLAUDE to "Anthropic Claude",
                            AiProvider.OFFLINE_BIO_SYNTHESIS to "Offline Bio-Model"
                        ).forEach { (provider, label) ->
                            val isSelected = dataStoreState.activeProvider == provider.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    coroutineScope.launch {
                                        ApiKeyDataStore.saveActiveProvider(context, provider.id)
                                        statusMessage = "Switched active provider to $label"
                                    }
                                },
                                label = { Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) },
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
                                modifier = Modifier.testTag("datastore_provider_chip_${provider.id}")
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // TEXTFIELDS FOR GEMINI, OPENAI, AND CLAUDE API KEYS
            // =========================================================================
            Text(
                text = "API Key Configuration (DataStore)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // 1. Google Gemini TextField Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            Text("Google Gemini API Key", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        DataStorePill(isStored = dataStoreState.geminiKey.isNotEmpty())
                    }

                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Gemini Key (AIzaSy...)") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (showGeminiKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showGeminiKeyText = !showGeminiKeyText }) {
                                Icon(
                                    imageVector = if (showGeminiKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showGeminiKeyText) "Hide" else "Show"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("datastore_gemini_api_key_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.saveGeminiKey(context, geminiKeyInput)
                                    statusMessage = "Gemini API key saved to DataStore!"
                                    Toast.makeText(context, "Saved to DataStore", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = geminiKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_save_gemini_key_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to DataStore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                testingProvider = AiProvider.GEMINI
                                testResult = null
                                coroutineScope.launch {
                                    val res = inferenceService.testProviderConnection(AiProvider.GEMINI, geminiKeyInput.ifBlank { null })
                                    testingProvider = null
                                    testResult = res
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_test_gemini_key_btn")
                        ) {
                            if (testingProvider == AiProvider.GEMINI) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Ping", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.clearKey(context, AiProvider.GEMINI)
                                    geminiKeyInput = ""
                                    statusMessage = "Gemini key removed from DataStore"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("datastore_clear_gemini_key_btn")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 2. OpenAI TextField Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10A37F), modifier = Modifier.size(20.dp))
                            Text("OpenAI API Key", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        DataStorePill(isStored = dataStoreState.openAiKey.isNotEmpty())
                    }

                    OutlinedTextField(
                        value = openAiKeyInput,
                        onValueChange = { openAiKeyInput = it },
                        label = { Text("OpenAI Key (sk-proj...)") },
                        placeholder = { Text("sk-proj-...") },
                        singleLine = true,
                        visualTransformation = if (showOpenAiKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showOpenAiKeyText = !showOpenAiKeyText }) {
                                Icon(
                                    imageVector = if (showOpenAiKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showOpenAiKeyText) "Hide" else "Show"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("datastore_openai_api_key_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.saveOpenAiKey(context, openAiKeyInput)
                                    statusMessage = "OpenAI API key saved to DataStore!"
                                    Toast.makeText(context, "Saved to DataStore", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = openAiKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_save_openai_key_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to DataStore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                testingProvider = AiProvider.OPENAI
                                testResult = null
                                coroutineScope.launch {
                                    val res = inferenceService.testProviderConnection(AiProvider.OPENAI, openAiKeyInput.ifBlank { null })
                                    testingProvider = null
                                    testResult = res
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_test_openai_key_btn")
                        ) {
                            if (testingProvider == AiProvider.OPENAI) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Ping", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.clearKey(context, AiProvider.OPENAI)
                                    openAiKeyInput = ""
                                    statusMessage = "OpenAI key removed from DataStore"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("datastore_clear_openai_key_btn")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 3. Anthropic Claude TextField Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Text("Anthropic Claude API Key", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        DataStorePill(isStored = dataStoreState.claudeKey.isNotEmpty())
                    }

                    OutlinedTextField(
                        value = claudeKeyInput,
                        onValueChange = { claudeKeyInput = it },
                        label = { Text("Claude Key (sk-ant...)") },
                        placeholder = { Text("sk-ant-...") },
                        singleLine = true,
                        visualTransformation = if (showClaudeKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showClaudeKeyText = !showClaudeKeyText }) {
                                Icon(
                                    imageVector = if (showClaudeKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showClaudeKeyText) "Hide" else "Show"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("datastore_claude_api_key_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.saveClaudeKey(context, claudeKeyInput)
                                    statusMessage = "Claude API key saved to DataStore!"
                                    Toast.makeText(context, "Saved to DataStore", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = claudeKeyInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_save_claude_key_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to DataStore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                testingProvider = AiProvider.CLAUDE
                                testResult = null
                                coroutineScope.launch {
                                    val res = inferenceService.testProviderConnection(AiProvider.CLAUDE, claudeKeyInput.ifBlank { null })
                                    testingProvider = null
                                    testResult = res
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("datastore_test_claude_key_btn")
                        ) {
                            if (testingProvider == AiProvider.CLAUDE) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Ping", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    ApiKeyDataStore.clearKey(context, AiProvider.CLAUDE)
                                    claudeKeyInput = ""
                                    statusMessage = "Claude key removed from DataStore"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("datastore_clear_claude_key_btn")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Connection Test Result Banner
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
                            modifier = Modifier.padding(12.dp),
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
                                    text = if (result.isSuccess) "${result.provider.displayName} Connected (${result.latencyMs}ms)" else "Connection Failed",
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

            // =========================================================================
            // ACTION: SAVE ALL KEYS AT ONCE
            // =========================================================================
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (geminiKeyInput.isNotBlank()) ApiKeyDataStore.saveGeminiKey(context, geminiKeyInput)
                        if (openAiKeyInput.isNotBlank()) ApiKeyDataStore.saveOpenAiKey(context, openAiKeyInput)
                        if (claudeKeyInput.isNotBlank()) ApiKeyDataStore.saveClaudeKey(context, claudeKeyInput)
                        statusMessage = "All configured keys successfully saved into DataStore!"
                        Toast.makeText(context, "Saved all keys to DataStore", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("datastore_save_all_keys_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All Configured Keys to DataStore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // =========================================================================
            // PUBLISHING / OPEN SOURCE SAFETY MODE
            // =========================================================================
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        Text(
                            text = "Open-Source Repository Publishing Safety",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF991B1B)
                        )
                    }

                    Text(
                        text = "Wipes all DataStore entries from device storage before sharing screen recordings or submitting the repository, ensuring zero quota consumption or credential leaks.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF7F1D1D)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                ApiKeyDataStore.purgeAllKeys(context)
                                geminiKeyInput = ""
                                openAiKeyInput = ""
                                claudeKeyInput = ""
                                statusMessage = "DataStore purged! Device safe for open-source submission."
                                Toast.makeText(context, "DataStore wiped", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("datastore_purge_all_btn")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Purge All Keys from DataStore", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DataStorePill(isStored: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isStored) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = if (isStored) "In DataStore" else "Empty",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isStored) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
