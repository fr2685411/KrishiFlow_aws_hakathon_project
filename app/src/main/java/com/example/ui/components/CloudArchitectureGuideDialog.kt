package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

/**
 * Data specification for Cloud vs Local Open-Source Architecture Matrix.
 */
data class ArchitectureCategory(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val buildItTools: List<String>,
    val shipItTools: List<String>,
    val krishiFlowContext: String
)

val architectureCategories = listOf(
    ArchitectureCategory(
        id = "agents_ai",
        title = "Agents and AI",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFF8B5CF6),
        buildItTools = listOf("Strands Agents SDK", "PartyRock"),
        shipItTools = listOf("SageMaker AI"),
        krishiFlowContext = "Rapid offline prompt prototyping & autonomous agronomy synthesis on laptop vs scalable enterprise AI model hosting for real-time crop disease diagnosis."
    ),
    ArchitectureCategory(
        id = "containers_k8s",
        title = "Containers and Kubernetes",
        icon = Icons.Default.ViewInAr,
        accentColor = Color(0xFF0284C7),
        buildItTools = listOf("Finch", "EKS Distro", "EKS Anywhere"),
        shipItTools = listOf("EKS", "ECS", "Fargate"),
        krishiFlowContext = "Zero-cost local microservices with open-source Finch & EKS Distro on workstation vs automated elastic container execution for logistics dispatch."
    ),
    ArchitectureCategory(
        id = "serverless",
        title = "Serverless",
        icon = Icons.Default.ElectricBolt,
        accentColor = Color(0xFFF59E0B),
        buildItTools = listOf("SAM CLI", "LocalStack"),
        shipItTools = listOf("Lambda", "API Gateway", "Step Functions"),
        krishiFlowContext = "LocalStack emulating AWS services locally without AWS credentials or bills vs serverless lambda handlers executing UPI settlements & order reservations."
    ),
    ArchitectureCategory(
        id = "servers_runtimes",
        title = "Servers and runtimes",
        icon = Icons.Default.Dns,
        accentColor = Color(0xFF10B981),
        buildItTools = listOf("Firecracker", "Corretto"),
        shipItTools = listOf("EC2", "Lightsail", "App Runner", "Amplify Hosting"),
        krishiFlowContext = "Lightweight local microVMs with open-source Firecracker & Corretto JVM vs managed App Runner and Amplify hosting for public web and API endpoints."
    ),
    ArchitectureCategory(
        id = "data_search",
        title = "Data and search",
        icon = Icons.Default.Storage,
        accentColor = Color(0xFFEC4899),
        buildItTools = listOf("OpenSearch"),
        shipItTools = listOf("S3", "DynamoDB", "RDS", "Aurora"),
        krishiFlowContext = "Self-hosted local search index vs managed DynamoDB single-digit millisecond mandi price lookup, S3 harvest photos, and Aurora relational transactions."
    ),
    ArchitectureCategory(
        id = "auth_policy",
        title = "Auth and policy",
        icon = Icons.Default.Lock,
        accentColor = Color(0xFF6366F1),
        buildItTools = listOf("Cedar"),
        shipItTools = listOf("Cognito"),
        krishiFlowContext = "Decoupled open-source policy engine (Cedar) for role-based access rules vs AWS Cognito managing farmer phone numbers, passwords, and tokens."
    ),
    ArchitectureCategory(
        id = "plumbing",
        title = "The plumbing",
        icon = Icons.Default.Hub,
        accentColor = Color(0xFF0D9488),
        buildItTools = listOf("- (Local loopback & in-memory bus)"),
        shipItTools = listOf("CloudFront", "Route 53", "EventBridge", "SQS", "SNS", "CloudWatch"),
        krishiFlowContext = "EventBridge connects mandi spikes to farmer SMS (SNS); SQS queues vehicle dispatch; CloudWatch monitors 0% food-waste throughput."
    )
)

/**
 * Interactive Dialog showcasing "What you need: Build It vs Ship It"
 * across all engineering layers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CloudArchitectureGuideDialog(
    onDismiss: () -> Unit,
    onOpenBuildItLab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedViewMode by rememberSaveable { mutableIntStateOf(0) } // 0: Side-by-Side, 1: Build It, 2: Ship It
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val filteredList = architectureCategories.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.buildItTools.any { it.contains(searchQuery, ignoreCase = true) } ||
            item.shipItTools.any { it.contains(searchQuery, ignoreCase = true) } ||
            item.krishiFlowContext.contains(searchQuery, ignoreCase = true)

        val matchesFilter = selectedCategoryFilter == null || item.id == selectedCategoryFilter
        matchesSearch && matchesFilter
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("cloud_architecture_guide_dialog"),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF16A34A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "What You Need: Build It vs Ship It",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Local Open-Source Development vs Cloud Production Stacks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dual Pillars Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Build It Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedViewMode = 1 }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Computer, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                Text("Build It", fontWeight = FontWeight.Bold, color = GreenPrimary, fontSize = 13.sp)
                            }
                            Text(
                                text = "Open source, on your machine",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF14532D)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "No AWS account, no card, no bill.",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Ship It Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF93C5FD)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedViewMode = 2 }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                Text("Ship It", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB), fontSize = 13.sp)
                            }
                            Text(
                                text = "Deployed, with a URL",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color(0xFF1E3A8A)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = "Free tier: up to $200 credits",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // View Mode Tabs
                TabRow(
                    selectedTabIndex = selectedViewMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedViewMode == 0,
                        onClick = { selectedViewMode = 0 },
                        text = { Text("Side-by-Side", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedViewMode == 1,
                        onClick = { selectedViewMode = 1 },
                        text = { Text("Build It Focus", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedViewMode == 2,
                        onClick = { selectedViewMode = 2 },
                        text = { Text("Ship It Focus", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Search & Filter
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter components (e.g. Lambda, LocalStack, Cedar)...", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_arch_search_field"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category Quick Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (7)", fontSize = 10.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    architectureCategories.forEach { category ->
                        FilterChip(
                            selected = selectedCategoryFilter == category.id,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == category.id) null else category.id
                            },
                            label = { Text(category.title, fontSize = 10.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.accentColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Architecture Categories List
                filteredList.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Category Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(item.accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Content Rendering Based on View Mode
                            when (selectedViewMode) {
                                0 -> {
                                    // Side-by-Side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Build It Box
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF0FDF4),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "Build It (Local)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = GreenPrimary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                item.buildItTools.forEach { tool ->
                                                    Text(
                                                        text = "• $tool",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF14532D)
                                                    )
                                                }
                                            }
                                        }

                                        // Ship It Box
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEFF6FF),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "Ship It (Cloud)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF2563EB)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                item.shipItTools.forEach { tool ->
                                                    Text(
                                                        text = "• $tool",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF1E3A8A)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    // Build It Focus
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF0FDF4),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Code, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                                                Text("Build It (Open source on your machine)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = GreenPrimary)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            item.buildItTools.forEach { tool ->
                                                Text("✓ $tool", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF14532D))
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    // Ship It Focus
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                                Text("Ship It (Deployed, with a URL)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF2563EB))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            item.shipItTools.forEach { tool ->
                                                Text("✓ $tool", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E3A8A))
                                            }
                                        }
                                    }
                                }
                            }

                            // Application Mapping Note
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                                    )
                                    Text(
                                        text = item.krishiFlowContext,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Security & Publishing Key Protection Callout
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Zero-Bill Safe Publishing Protection",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Using LocalStack / SAM CLI for building locally ensures zero cards or AWS bills. When shipping, the built-in 'Purge All Keys for Publishing' prevents your API credentials from being distributed with the APK.",
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onOpenBuildItLab()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("cloud_arch_open_build_it_lab_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Build It Lab", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("cloud_arch_got_it_btn")
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    )
}
