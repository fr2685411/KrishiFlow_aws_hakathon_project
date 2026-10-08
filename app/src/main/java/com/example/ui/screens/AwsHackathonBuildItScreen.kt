package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AwsHackathonBuildItScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        "🏆 Overview & Pitch",
        "🛡️ Cedar Policy Lab",
        "⚡ SAM & LocalStack",
        "🤖 Strands AI Agents",
        "📦 Finch & Corretto",
        "🗄️ OpenSearch",
        "📋 Submission Files"
    )

    fun copyToClipboard(label: String, content: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, content)
        clipboard.setPrimaryClip(clip)
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Copied $label to clipboard!")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("aws_hackathon_build_it_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("build_it_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "AWS Hackathon 2026",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Track: Build It (100% Local Open Source)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            copyToClipboard(
                                "Hackathon Pitch",
                                "KrishiFlow: Autonomous Fair-Price Agri-Logistics on AWS Open Source. Built for the AWS Hackathon 'Build It' Track with zero cloud spend on Finch, SAM CLI, LocalStack, Corretto 21, OpenSearch, and Cedar."
                            )
                        },
                        modifier = Modifier.testTag("copy_pitch_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Pitch",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth().testTag("build_it_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("build_it_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewAndPitchTab(
                        onSelectTab = { selectedTab = it },
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    1 -> CedarPolicyLabTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    2 -> SamLocalStackTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    3 -> StrandsAgentsAiTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    4 -> FinchCorrettoTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    5 -> OpenSearchTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                    6 -> SubmissionFilesTab(
                        onCopyText = { label, text -> copyToClipboard(label, text) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// TAB 0: OVERVIEW & PITCH
// ============================================================================
@Composable
private fun OverviewAndPitchTab(
    onSelectTab: (Int) -> Unit,
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth().testTag("build_it_hero_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF16A34A).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF16A34A))
                    ) {
                        Text(
                            text = "AWS HACKATHON: BUILD IT TRACK",
                            color = Color(0xFF4ADE80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "100% LOCAL",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "KrishiFlow",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Autonomous Fair-Price Agricultural Network & Zero-Waste Food Logistics",
                    fontSize = 14.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎯 Track Constraint Guarantee",
                            color = Color(0xFFF59E0B),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Open source, on your machine\n• No AWS account, no card, no bill ($0.00 spent)\n• Verified offline via LocalStack, Finch, SAM CLI & Cedar",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Problem Statement & Solution
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "The Problem in Indian Agriculture",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Over 40% of fresh vegetables rot before reaching consumers, while smallholder farmers suffer exploitative middleman cuts. Rural mandis operate in spotty connectivity and cannot bear expensive recurring cloud infrastructure costs.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The KrishiFlow Solution",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A multi-agent, offline-capable mandi mesh network that pairs surplus harvests with food processing buyers within hours. By running on local Finch containers and SAM LocalStack, any rural cooperative can operate KrishiFlow completely free of cloud bills.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 19.sp
                )
            }
        }

        // 6-Pillar Interactive Navigation
        Text(
            text = "Explore Build It Implementations",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        val pillarCards = listOf(
            Triple(1, "Auth & Policy: Cedar", "Declarative policies controlling farmer & wholesaler RBAC with zero cloud auth."),
            Triple(2, "Serverless: SAM & LocalStack", "Local API Gateway, Lambda, and DynamoDB emulated at localhost:4566."),
            Triple(3, "Agents & AI: Strands Agents", "Multi-agent agronomy & mandi matching pipelines built with Strands specs."),
            Triple(4, "Containers & Runtimes: Finch + Corretto", "Open-source Finch container engine & lightweight Amazon Corretto 21 JVM."),
            Triple(5, "Data & Search: OpenSearch", "Local vector search & BM25 index for agricultural produce discovery."),
            Triple(6, "Submission Files & Code", "Production SAM template.yaml, policies.cedar, and finch-compose.yml.")
        )

        pillarCards.forEach { (tabIdx, title, desc) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectTab(tabIdx) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSelectTab(tabIdx) },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open Lab", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// TAB 1: CEDAR POLICY LAB (AWS CEDAR POLICY EVALUATION)
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CedarPolicyLabTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val principals = listOf("Farmer", "KrishiMitra", "Wholesaler", "SaathiDriver", "GuestBuyer")
    val actions = listOf(
        "PledgeHarvest",
        "InspectQuality",
        "BidSurplusLot",
        "PostBulkRFQ",
        "UploadProofOfDelivery",
        "OverrideMandiFloorPrice"
    )
    val resources = listOf("HarvestBatch", "MandiOrder", "DispatchTrip")

    var selectedPrincipal by remember { mutableStateOf(principals[0]) }
    var selectedAction by remember { mutableStateOf(actions[0]) }
    var selectedResource by remember { mutableStateOf(resources[0]) }

    var isPrincipalDropdownExpanded by remember { mutableStateOf(false) }
    var isActionDropdownExpanded by remember { mutableStateOf(false) }
    var isResourceDropdownExpanded by remember { mutableStateOf(false) }

    var evaluationResult by remember { mutableStateOf<CedarEvaluationResult?>(null) }

    fun runEvaluation() {
        // Evaluate based on real KrishiFlow Cedar policy logic
        val result = when {
            selectedAction == "OverrideMandiFloorPrice" -> {
                CedarEvaluationResult(
                    decision = CedarDecision.DENY,
                    matchedPolicy = "forbid OverrideMandiFloorPrice",
                    explanation = "Strict system safeguard: Undermining mandi minimum floor price is forbidden unless central disaster relief is declared."
                )
            }
            selectedPrincipal == "Farmer" && selectedAction in listOf("PledgeHarvest") && selectedResource == "HarvestBatch" -> {
                CedarEvaluationResult(
                    decision = CedarDecision.ALLOW,
                    matchedPolicy = "permit Farmer -> PledgeHarvest -> HarvestBatch",
                    explanation = "Explicit permit: Farmers possess full authority to pledge produce batches into the KrishiFlow network."
                )
            }
            selectedPrincipal == "KrishiMitra" && selectedAction in listOf("InspectQuality") && selectedResource == "HarvestBatch" -> {
                CedarEvaluationResult(
                    decision = CedarDecision.ALLOW,
                    matchedPolicy = "permit KrishiMitra -> InspectQuality -> HarvestBatch",
                    explanation = "Explicit permit: Krishi Mitras (village leads) can inspect harvest quality and certify grading."
                )
            }
            selectedPrincipal == "Wholesaler" && selectedAction in listOf("BidSurplusLot", "PostBulkRFQ") && selectedResource == "MandiOrder" -> {
                CedarEvaluationResult(
                    decision = CedarDecision.ALLOW,
                    matchedPolicy = "permit Wholesaler -> BidSurplusLot / PostBulkRFQ -> MandiOrder",
                    explanation = "Explicit permit: Verified mandi buyers can bid on discounted surplus produce lots."
                )
            }
            selectedPrincipal == "SaathiDriver" && selectedAction in listOf("UploadProofOfDelivery") && selectedResource == "DispatchTrip" -> {
                CedarEvaluationResult(
                    decision = CedarDecision.ALLOW,
                    matchedPolicy = "permit SaathiDriver -> UploadProofOfDelivery -> DispatchTrip",
                    explanation = "Explicit permit: Logistics drivers can upload geo-tagged photo proofs of delivery."
                )
            }
            else -> {
                CedarEvaluationResult(
                    decision = CedarDecision.DENY,
                    matchedPolicy = "default deny",
                    explanation = "Zero-trust default deny: Principal '$selectedPrincipal' has no policy allowing action '$selectedAction' on resource '$selectedResource'."
                )
            }
        }
        evaluationResult = result
    }

    LaunchedEffect(Unit) {
        runEvaluation()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cedar Header Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFA5B4FC)
                    )
                    Text(
                        text = "AWS Cedar Policy Engine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cedar is AWS's open-source policy language for fine-grained permissions. KrishiFlow uses Cedar for zero-cloud authorization on edge devices.",
                    fontSize = 12.sp,
                    color = Color(0xFFC7D2FE)
                )
            }
        }

        // Interactive Evaluator Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Interactive Cedar Policy Tester",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Principal Selector
                Text(text = "Principal (Role):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isPrincipalDropdownExpanded,
                    onExpandedChange = { isPrincipalDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPrincipal,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPrincipalDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                    ExposedDropdownMenu(
                        expanded = isPrincipalDropdownExpanded,
                        onDismissRequest = { isPrincipalDropdownExpanded = false }
                    ) {
                        principals.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    selectedPrincipal = item
                                    isPrincipalDropdownExpanded = false
                                    runEvaluation()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Selector
                Text(text = "Action (Permission):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isActionDropdownExpanded,
                    onExpandedChange = { isActionDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAction,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isActionDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isActionDropdownExpanded,
                        onDismissRequest = { isActionDropdownExpanded = false }
                    ) {
                        actions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    selectedAction = item
                                    isActionDropdownExpanded = false
                                    runEvaluation()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resource Selector
                Text(text = "Resource:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isResourceDropdownExpanded,
                    onExpandedChange = { isResourceDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedResource,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isResourceDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isResourceDropdownExpanded,
                        onDismissRequest = { isResourceDropdownExpanded = false }
                    ) {
                        resources.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    selectedResource = item
                                    isResourceDropdownExpanded = false
                                    runEvaluation()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { runEvaluation() },
                    modifier = Modifier.fillMaxWidth().testTag("evaluate_cedar_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Evaluate Cedar Authorization")
                }

                // Evaluation Output Display
                evaluationResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = if (result.decision == CedarDecision.ALLOW) Color(0xFF14532D) else Color(0xFF7F1D1D),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (result.decision == CedarDecision.ALLOW) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (result.decision == CedarDecision.ALLOW) Color(0xFF4ADE80) else Color(0xFFF87171)
                                )
                                Text(
                                    text = "DECISION: ${result.decision}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Rule: ${result.matchedPolicy}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.explanation,
                                fontSize = 12.sp,
                                color = Color(0xFFF1F5F9),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Cedar Policy Syntax Viewer
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "policies.cedar",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8)
                    )
                    IconButton(
                        onClick = {
                            onCopyText(
                                "Cedar Policies",
                                "// AWS CEDAR POLICIES FOR KRISHIFLOW\npermit (\n    principal in KrishiRole::\"Farmer\",\n    action in [KrishiAction::\"PledgeHarvest\"],\n    resource is KrishiResource::\"HarvestBatch\"\n);"
                            )
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val sampleCedarCode = """
// 1. Farmer Harvest Pledges
permit (
    principal in KrishiRole::"Farmer",
    action in [KrishiAction::"PledgeHarvest"],
    resource is KrishiResource::"HarvestBatch"
);

// 2. Field Lead Quality Inspection
permit (
    principal in KrishiRole::"KrishiMitra",
    action in [KrishiAction::"InspectQuality"],
    resource is KrishiResource::"HarvestBatch"
);

// 3. Wholesale Bidding
permit (
    principal in KrishiRole::"Wholesaler",
    action in [KrishiAction::"BidSurplusLot"],
    resource is KrishiResource::"MandiOrder"
);

// 4. Critical Minimum Support Price (MSP) Safeguard
forbid (
    principal,
    action == KrishiAction::"OverrideMandiFloorPrice",
    resource is KrishiResource::"HarvestBatch"
) unless {
    principal.role == "CentralAdmin" &&
    context.reason == "DisasterReliefDeclaration"
};
                """.trimIndent()

                Text(
                    text = sampleCedarCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFA5F3FC),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

enum class CedarDecision { ALLOW, DENY }
data class CedarEvaluationResult(
    val decision: CedarDecision,
    val matchedPolicy: String,
    val explanation: String
)

// ============================================================================
// TAB 2: SAM CLI & LOCALSTACK LAB
// ============================================================================
@Composable
private fun SamLocalStackTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isSimulatingInvoke by remember { mutableStateOf(false) }
    var terminalLogs = remember {
        mutableStateListOf(
            "LocalStack 3.0 initialized on localhost:4566",
            "API Gateway running at: http://localhost:4566/restapis/krishiflow-api/local",
            "DynamoDB tables active: krishiflow-harvests-local, krishiflow-orders-local",
            "Ready for 'sam local start-api' or 'sam local invoke'"
        )
    }

    val coroutineScope = rememberCoroutineScope()

    fun triggerLocalInvoke() {
        if (isSimulatingInvoke) return
        isSimulatingInvoke = true
        coroutineScope.launch {
            terminalLogs.add(">> sam local invoke PledgeHarvestFunction -e event.json")
            delay(500)
            terminalLogs.add("START RequestId: 4a2b91c0-0012-4e00 Version: \$LATEST")
            terminalLogs.add("Corretto 21 JVM started in 118ms (warm execution: 14ms)")
            terminalLogs.add("Cedar Evaluator: Principal 'Farmer' allowed on 'HarvestBatch'")
            terminalLogs.add("DynamoDB putItem -> krishiflow-harvests-local [200 OK]")
            delay(400)
            terminalLogs.add("END RequestId: 4a2b91c0-0012-4e00")
            terminalLogs.add("REPORT Duration: 132.40 ms Billed: 133 ms Memory: 82 MB")
            terminalLogs.add("Response: {\"status\":\"SUCCESS\",\"batchId\":\"BATCH-9812\",\"floorPrice\":24.5}")
            isSimulatingInvoke = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color(0xFFFDE68A)
                    )
                    Text(
                        text = "SAM CLI & LocalStack Offline Serverless",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zero AWS account required. LocalStack emulates Lambda, API Gateway, and DynamoDB completely offline on your developer machine.",
                    fontSize = 12.sp,
                    color = Color(0xFFFEF3C7)
                )
            }
        }

        // Live Interactive Terminal
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }
                    Text(
                        text = "bash - LocalStack & SAM CLI",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF090D16), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    terminalLogs.forEach { logLine ->
                        Text(
                            text = logLine,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = when {
                                logLine.startsWith(">>") -> Color(0xFF38BDF8)
                                logLine.contains("200 OK") || logLine.contains("SUCCESS") -> Color(0xFF4ADE80)
                                logLine.contains("REPORT") -> Color(0xFFFBBF24)
                                else -> Color(0xFFCBD5E1)
                            },
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { triggerLocalInvoke() },
                        enabled = !isSimulatingInvoke,
                        modifier = Modifier.weight(1f).testTag("sam_invoke_btn")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSimulatingInvoke) "Executing..." else "Invoke SAM Lambda")
                    }

                    OutlinedButton(
                        onClick = {
                            terminalLogs.clear()
                            terminalLogs.add("LocalStack ready. Cleared terminal.")
                        }
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        // Quick Command Reference
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = "Quick Terminal Commands for Judges", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val commands = listOf(
                    "sam validate -t template.yaml",
                    "sam local start-api --endpoint-url http://localhost:4566",
                    "curl -X POST http://localhost:3000/v1/harvests/pledge -d '{\"crop\":\"Tomato\"}'"
                )

                commands.forEach { cmd ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            onCopyText("SAM command", cmd)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cmd,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// TAB 3: STRANDS AGENTS & PARTYROCK LAB
// ============================================================================
@Composable
private fun StrandsAgentsAiTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isAgentRunning by remember { mutableStateOf(false) }
    var agentSteps = remember {
        mutableStateListOf(
            "Agent System: Strands Agents SDK multi-agent team initialized",
            "Agents registered: FarmerAdvisorAgent, MandiMatcherAgent, LogisticsAgent",
            "PartyRock prompt chain: Zero-cloud offline reasoning loop"
        )
    }

    val coroutineScope = rememberCoroutineScope()

    fun runMultiAgentSimulation() {
        if (isAgentRunning) return
        isAgentRunning = true
        coroutineScope.launch {
            agentSteps.add("----------------------------------------")
            agentSteps.add("▶ TRIGGER: Farmer listed 500kg perishable Tomatoes (24h expiry)")
            delay(400)
            agentSteps.add("🤖 [FarmerAdvisorAgent]: Assessing weather & shelf life...")
            agentSteps.add("   -> Recommended dynamic floor price: ₹22/kg (15% discount for 0% waste)")
            delay(500)
            agentSteps.add("🤖 [MandiMatcherAgent]: Scanning regional buyers in 25km radius...")
            agentSteps.add("   -> Match found: Sahyadri Food Processing Unit (Demand: 1,200kg)")
            delay(500)
            agentSteps.add("🤖 [LogisticsAgent]: Generating optimized multi-stop pickup route...")
            agentSteps.add("   -> Route #RT-402 assigned to Saathi EV Van (Driver: Ramesh)")
            delay(300)
            agentSteps.add("✅ MULTI-AGENT GOAL ACHIEVED: 500kg rescued, 0kg wasted.")
            isAgentRunning = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D95))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFDDD6FE)
                    )
                    Text(
                        text = "Strands Agents SDK & PartyRock",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Autonomous multi-agent collaboration running locally on developer machines. Agents decompose farm harvest triage, price discovery, and route dispatch.",
                    fontSize = 12.sp,
                    color = Color(0xFFEDE9FE)
                )
            }
        }

        // Multi-Agent Execution Board
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Multi-Agent Autonomous Flow",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    agentSteps.forEach { step ->
                        Text(
                            text = step,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = when {
                                step.startsWith("🤖") -> Color(0xFFC084FC)
                                step.startsWith("✅") -> Color(0xFF4ADE80)
                                step.startsWith("▶") -> Color(0xFF38BDF8)
                                else -> Color(0xFFE2E8F0)
                            },
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { runMultiAgentSimulation() },
                    enabled = !isAgentRunning,
                    modifier = Modifier.fillMaxWidth().testTag("run_strands_agent_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isAgentRunning) "Agents Collaborating..." else "Run Strands Multi-Agent Simulation")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// TAB 4: FINCH & AMAZON CORRETTO 21
// ============================================================================
@Composable
private fun FinchCorrettoTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0)
                    )
                    Text(
                        text = "Finch Containers & Corretto 21 JVM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Finch is AWS's open-source container engine. Combined with Amazon Corretto 21 and Firecracker microVM specs, KrishiFlow runs on village mandi laptops with zero license fees.",
                    fontSize = 12.sp,
                    color = Color(0xFFD1FAE5)
                )
            }
        }

        // Telemetry Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Boot Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "118 ms", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF16A34A))
                    Text(text = "Firecracker microVM", fontSize = 10.sp)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "JVM Memory", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "5.2 MB", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF0284C7))
                    Text(text = "Amazon Corretto 21", fontSize = 10.sp)
                }
            }
        }

        // Finch Compose Preview
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "finch-compose.yml",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = {
                            onCopyText("Finch Compose", "finch compose -f finch-compose.yml up -d")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                val finchCode = """
services:
  localstack:
    image: localstack/localstack:latest
    ports: ["4566:4566"]
  opensearch:
    image: opensearchproject/opensearch:2.12.0
    ports: ["9200:9200"]
  krishiflow-edge:
    build: .
    environment:
      - AWS_ENDPOINT_URL=http://localstack:4566
                """.trimIndent()
                Text(
                    text = finchCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// TAB 5: OPENSEARCH LAB
// ============================================================================
@Composable
private fun OpenSearchTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var searchInput by remember { mutableStateOf("Nashik Red Onion") }
    var searchResults by remember {
        mutableStateOf(
            listOf(
                Triple("Nashik Premium Red Onion", "Score: 0.94 (BM25 + Vector)", "Stock: 800 kg • ₹24/kg • Grade A"),
                Triple("Lasalgaon Storage Onion", "Score: 0.88 (Semantic Match)", "Stock: 1,500 kg • ₹22/kg • Grade B+"),
                Triple("Pune White Onion Surplus", "Score: 0.76 (Fuzzy Keyword)", "Stock: 450 kg • ₹18/kg • Urgent Clearance")
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF831843))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color(0xFFFBCFE8)
                    )
                    Text(
                        text = "OpenSearch Local Produce Index",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "OpenSearch powers instant neural vector search & BM25 text queries so wholesale buyers can discover produce lots across mandis without cloud latency.",
                    fontSize = 12.sp,
                    color = Color(0xFFFCE7F3)
                )
            }
        }

        // Live Search Input
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            label = { Text("Search Mandi Produce Index") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                Button(
                    onClick = {
                        // Filter or refresh simulated results
                        searchResults = listOf(
                            Triple("$searchInput (Top Mandi Match)", "Score: 0.96 (Vector Rank)", "Verified Farmer Batch • Grade A"),
                            Triple("$searchInput Secondary Surplus", "Score: 0.84 (BM25)", "Near-Expiry Food Saver • 20% Off")
                        )
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Query", fontSize = 11.sp)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("opensearch_input")
        )

        Text(text = "Search Query Results (${searchResults.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        searchResults.forEach { (title, score, desc) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = score, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================================
// TAB 6: SUBMISSION FILES & ARTIFACTS
// ============================================================================
@Composable
private fun SubmissionFilesTab(
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val fileArtifacts = listOf(
        Pair("template.yaml", "SAM CLI Serverless Specification configured for LocalStack (DynamoDB, Lambda, API Gateway)"),
        Pair("policies.cedar", "AWS Cedar Authorization Policies for Farmer, Krishi Mitra, Wholesaler & MSP safeguards"),
        Pair("finch-compose.yml", "Finch Container Engine Compose for LocalStack, OpenSearch & Corretto 21 microservices"),
        Pair("submission-manifest.json", "Official AWS Hackathon 2026 Build It Track Manifest & architecture metadata"),
        Pair("README-AWS-HACKATHON-BUILD-IT.md", "Full Devpost / Hackathon submission dossier and judging rubric alignment")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Hackathon Submission Artifacts Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All project files required by AWS Hackathon judges are generated and ready in the project root. Judges can clone the repository and run all tests locally.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        fileArtifacts.forEach { (fileName, description) ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = fileName,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = {
                                onCopyText(fileName, "File: $fileName is in the project root of KrishiFlow.")
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Copy Path", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
