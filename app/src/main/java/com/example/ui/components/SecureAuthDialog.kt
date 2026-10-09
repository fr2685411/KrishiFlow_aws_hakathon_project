package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthUiState
import com.example.ui.AppPersonaMode
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.SpoilageRed
import com.example.ui.viewmodel.KrishiUiState
import com.example.ui.viewmodel.KrishiViewModel

private enum class AuthMethodTab(val label: String) {
    PHONE("📱 Phone OTP"),
    GOOGLE("🌐 Google"),
    EMAIL("✉️ Email"),
    DEMO("⚡ 1-Tap Demo")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SecureAuthDialog(
    uiState: KrishiUiState,
    viewModel: KrishiViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AuthMethodTab.PHONE) }
    var selectedRole by remember { mutableStateOf(AppPersonaMode.KISAN_FARMER) }

    // Phone Auth states
    var phoneNumber by remember { mutableStateOf("9822014829") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    // Google Auth states
    var googleAccountName by remember { mutableStateOf("Ajrudeen Kureshi") }
    var googleEmail by remember { mutableStateOf("ajrudeenkureshi2003@gmail.com") }

    // Email Auth states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary.copy(alpha = 0.15f))
                            .border(1.5.dp, GreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "KrishiFlow Authentication",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Google Identity • Mobile OTP • Firebase",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close dialog")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =============================================================
                // 1. ACTIVE SESSION DISPLAY (IF LOGGED IN)
                // =============================================================
                when (val authState = uiState.authState) {
                    is AuthUiState.Authenticated -> {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = GreenPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = authState.user.displayName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF14532D),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (authState.user.phoneNumber.isNotBlank())
                                                    "📱 ${authState.user.phoneNumber} • ${authState.user.authProvider}"
                                                else
                                                    "✉️ ${authState.user.email} • ${authState.user.authProvider}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF166534),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.signOut() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpoilageRed),
                                        modifier = Modifier.defaultMinSize(minHeight = 30.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    is AuthUiState.OtpSent -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "SMS Sent! Demo 6-digit OTP: ${authState.demoOtp}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Use code ${authState.demoOtp} or 123456 to verify immediately.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        if (verificationId.isBlank()) {
                            verificationId = authState.verificationId
                            isOtpSent = true
                            if (otpCode.isBlank()) otpCode = authState.demoOtp
                        }
                    }

                    is AuthUiState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SpoilageRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Notice: ${authState.message}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF991B1B)),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    is AuthUiState.Loading -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = GreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Authenticating credentials...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    else -> {}
                }

                // =============================================================
                // 2. PROFESSION / ROLE SELECTOR (FARMER, MART BUYER, FLEET, ETC.)
                // =============================================================
                Text(
                    text = "Designated Role for This Session:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppPersonaMode.values().forEach { persona ->
                        val isSelected = selectedRole == persona
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRole = persona },
                            label = { Text(text = persona.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = persona.themeColor.copy(alpha = 0.15f),
                                selectedLabelColor = persona.themeColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = persona.themeColor
                            ),
                            modifier = Modifier.testTag("auth_role_${persona.name.lowercase()}")
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // =============================================================
                // 3. AUTH METHOD SEGMENTED TABS
                // =============================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AuthMethodTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) GreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab }
                                .testTag("auth_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // =============================================================
                // 4. TAB CONTENT
                // =============================================================
                when (selectedTab) {
                    AuthMethodTab.PHONE -> {
                        // PHONE NUMBER OTP FLOW
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Instant SMS OTP Login for Farmers & Mandi Traders",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Mobile Phone Number") },
                                prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = GreenPrimary) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_auth_phone")
                            )

                            if (!isOtpSent) {
                                Button(
                                    onClick = {
                                        viewModel.requestPhoneOtp("+91$phoneNumber") { res ->
                                            if (res.isSuccess) {
                                                verificationId = res.getOrNull() ?: "ver_demo"
                                                isOtpSent = true
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("send_phone_otp_button")
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send 6-Digit OTP SMS", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { otpCode = it },
                                    label = { Text("Enter 6-Digit OTP Code") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GreenPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("input_auth_otp")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.requestPhoneOtp("+91$phoneNumber")
                                        },
                                        modifier = Modifier.weight(1f).testTag("resend_otp_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Resend OTP", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val code = otpCode.ifBlank { "742918" }
                                            viewModel.verifyPhoneOtp(
                                                verificationId = verificationId.ifBlank { "ver_123" },
                                                otpCode = code,
                                                role = selectedRole,
                                                phoneNumber = "+91 $phoneNumber"
                                            ) { res ->
                                                if (res.isSuccess) onDismiss()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        modifier = Modifier.weight(1.5f).testTag("verify_phone_otp_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Verify & Login", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = "Zero password required. Verified against local APMC trader registers.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AuthMethodTab.GOOGLE -> {
                        // GOOGLE SIGN-IN FLOW
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4285F4).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GoogleLogoIcon()
                                        Column {
                                            Text(
                                                text = "Google Identity Services",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = "OAuth 2.0 Credential Manager",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    OutlinedTextField(
                                        value = googleEmail,
                                        onValueChange = { googleEmail = it },
                                        label = { Text("Google Account Email") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_google_email")
                                    )

                                    OutlinedTextField(
                                        value = googleAccountName,
                                        onValueChange = { googleAccountName = it },
                                        label = { Text("Account Name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("input_google_name")
                                    )
                                }
                            }

                            // Prominent Google Button
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.signInWithGoogle(
                                            role = selectedRole,
                                            accountName = googleAccountName,
                                            accountEmail = googleEmail
                                        ) { res ->
                                            if (res.isSuccess) onDismiss()
                                        }
                                    }
                                    .testTag("google_signin_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleLogoIcon()
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Continue with Google",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1F2937)
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "Secure, 1-tap sign in with your verified Google profile.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    AuthMethodTab.EMAIL -> {
                        // EMAIL & PASSWORD
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GreenPrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_email")
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GreenPrimary) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_password")
                            )

                            Button(
                                onClick = {
                                    val inputEmail = email.ifBlank { "partner@krishiflow.in" }
                                    val inputPass = password.ifBlank { "KrishiSafePass2026!" }
                                    viewModel.signInWithEmail(inputEmail, inputPass, selectedRole)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("firebase_email_login_button")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sign In with Email", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    AuthMethodTab.DEMO -> {
                        // 1-TAP DEMO ROLES
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "1-Tap Instant Pilot Roles (No credential input required):",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickAuthCard(
                                    title = "🌾 Kisan Farmer",
                                    subtitle = "Ramesh Patil",
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.signInAsRole(AppPersonaMode.KISAN_FARMER, "Ramesh Patil (Farmer)")
                                        onDismiss()
                                    }
                                )
                                QuickAuthCard(
                                    title = "🏪 Mart Buyer",
                                    subtitle = "D-Mart Sourcing",
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.signInAsRole(AppPersonaMode.VYAPAR_MANDI, "D-Mart Sourcing Lead")
                                        onDismiss()
                                    }
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuickAuthCard(
                                    title = "🚚 Logistics Driver",
                                    subtitle = "Santosh Shinde",
                                    color = Color(0xFF0D9488),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.signInAsRole(AppPersonaMode.SAATHI_FLEET, "Santosh Shinde (Driver)")
                                        onDismiss()
                                    }
                                )
                                QuickAuthCard(
                                    title = "🌐 Central Ops",
                                    subtitle = "Maharashtra Lead",
                                    color = Color(0xFF166534),
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.signInAsRole(AppPersonaMode.CENTRAL_OPS, "Pune Hub Ops Lead")
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("auth_dialog_close_button")) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Custom 4-color Google "G" icon drawn with high fidelity.
 */
@Composable
private fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "G",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                ),
                color = Color(0xFF4285F4)
            )
        }
    }
}

@Composable
private fun QuickAuthCard(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
