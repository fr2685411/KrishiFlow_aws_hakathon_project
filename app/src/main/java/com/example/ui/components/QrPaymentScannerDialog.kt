package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.wallet.QrCodeUtility
import com.example.data.wallet.VendorPaymentPayload
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import kotlin.math.roundToInt

@Composable
fun QrPaymentScannerDialog(
    currentWalletBalance: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (payload: VendorPaymentPayload, amount: Double) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var scannedPayload by remember { mutableStateOf<VendorPaymentPayload?>(null) }
    var customAmountText by remember { mutableStateOf("") }
    var isTorchOn by remember { mutableStateOf(false) }
    var showManualInput by remember { mutableStateOf(false) }
    var manualQrText by remember { mutableStateOf("") }
    var paymentReceiptData by remember { mutableStateOf<VendorPaymentPayload?>(null) }

    // Laser Animation for Viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserFloat"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("qr_payment_scanner_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GreenLight,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Scan & Pay Vendor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "क्यूआर स्कैन कर तुरंत भुगतान करें",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_qr_scanner_btn")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Farmer Wallet Balance Strip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Digital Wallet Balance:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "₹${currentWalletBalance.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        )
                    }
                }

                // If receipt is showing, render receipt view
                if (paymentReceiptData != null) {
                    PaymentSuccessReceiptView(
                        payload = paymentReceiptData!!,
                        paidAmount = customAmountText.toDoubleOrNull() ?: paymentReceiptData!!.amountRupees,
                        walletBalanceAfter = currentWalletBalance - (customAmountText.toDoubleOrNull() ?: paymentReceiptData!!.amountRupees),
                        onClose = onDismiss
                    )
                } else if (scannedPayload == null) {
                    // =========================================================================
                    // 1. SCANNER CAMERA VIEWFINDER (Live CameraX Stream with Real ZXing Decoder)
                    // =========================================================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A))
                            .border(2.dp, GreenPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .testTag("qr_camera_viewfinder_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasCameraPermission) {
                            // Real camera feed rendered via CameraX PreviewView
                            CameraQrScannerView(
                                isTorchOn = isTorchOn,
                                onQrCodeDetected = { rawText ->
                                    val parsed = QrCodeUtility.parseVendorPaymentPayload(rawText)
                                    scannedPayload = parsed
                                    customAmountText = parsed.amountRupees.toInt().toString()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Camera Permission Request Fallback State
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Camera Permission Required",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Allow camera access to scan physical vendor QR codes live.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("grant_camera_permission_btn")
                                ) {
                                    Text("Grant Camera Access", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Scanner Overlay Target Frame with Laser
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .border(2.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp))
                        ) {
                            // Corner bracket indicators
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .offset { IntOffset(0, (laserFraction * 175.dp.toPx()).roundToInt()) }
                                    .background(Color(0xFF4ADE80))
                            )
                        }

                        // Viewfinder Controls Overlay Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (hasCameraPermission) Color(0xFF22C55E) else Color(0xFFEF4444))
                                    )
                                    Text(
                                        text = if (hasCameraPermission) "Live Camera Scanning" else "Camera Off",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (hasCameraPermission) {
                                IconButton(
                                    onClick = { isTorchOn = !isTorchOn },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isTorchOn) Color(0xFFFEF08A) else Color.Black.copy(alpha = 0.6f))
                                        .testTag("toggle_torch_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                        contentDescription = "Torch",
                                        tint = if (isTorchOn) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // =========================================================================
                    // 2. QUICK TEST VENDOR QR CODE CHIPS
                    // =========================================================================
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Or Tap a Verified Vendor Terminal (त्वरित विक्रेता चयन):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(QrCodeUtility.DEMO_VENDOR_QRS) { demoVendor ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .clickable {
                                            scannedPayload = demoVendor
                                            customAmountText = demoVendor.amountRupees.toInt().toString()
                                        }
                                        .testTag("vendor_preset_${demoVendor.vendorId}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Store, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                                            Text(
                                                text = demoVendor.vendorName.take(18),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = "₹${demoVendor.amountRupees.toInt()} • ${demoVendor.supplyDescription.take(20)}...",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Manual UPI / QR String Input Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showManualInput = !showManualInput }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showManualInput) "Hide Manual Code Entry" else "Paste / Enter UPI Code Manually",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showManualInput) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualQrText,
                                onValueChange = { manualQrText = it },
                                label = { Text("Paste UPI / KrishiPay Code") },
                                placeholder = { Text("upi://pay?pa=vendor@upi&pn=Vendor&am=1500") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("manual_qr_input_field")
                            )

                            Button(
                                onClick = {
                                    if (manualQrText.isNotBlank()) {
                                        val parsed = QrCodeUtility.parseVendorPaymentPayload(manualQrText)
                                        scannedPayload = parsed
                                        customAmountText = parsed.amountRupees.toInt().toString()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                modifier = Modifier.fillMaxWidth().testTag("parse_manual_qr_btn")
                            ) {
                                Text("Process Scanned Code", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // =========================================================================
                    // 3. PAYMENT CONFIRMATION CARD (AFTER SCAN)
                    // =========================================================================
                    val payload = scannedPayload!!
                    val parsedAmount = customAmountText.toDoubleOrNull() ?: payload.amountRupees
                    val hasEnoughBalance = currentWalletBalance >= parsedAmount

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = payload.vendorName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF14532D)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenLight
                                ) {
                                    Text(
                                        text = "VERIFIED AGRO-VENDOR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = GreenPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "UPI ID: ${payload.vendorUpiId} • Ref: ${payload.referenceOrder}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF166534)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Farming Supplies Included:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(payload.supplyDescription, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1F2937))
                                }
                            }

                            // Payment Amount Input
                            OutlinedTextField(
                                value = customAmountText,
                                onValueChange = { customAmountText = it },
                                label = { Text("Payment Amount (₹)") },
                                leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GreenPrimary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                            )

                            // Balance impact indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wallet Balance After:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = if (hasEnoughBalance) "₹${(currentWalletBalance - parsedAmount).toInt()}" else "Insufficient Balance!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasEnoughBalance) GreenPrimary else Color(0xFFDC2626)
                                    )
                                )
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { scannedPayload = null },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Scan Another")
                                }

                                Button(
                                    onClick = {
                                        if (hasEnoughBalance && parsedAmount > 0.0) {
                                            onConfirmPayment(payload, parsedAmount)
                                            paymentReceiptData = payload
                                        }
                                    },
                                    enabled = hasEnoughBalance && parsedAmount > 0.0,
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.5f).testTag("confirm_wallet_payment_btn")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pay ₹${parsedAmount.toInt()}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Payment Success Receipt View
 */
@Composable
private fun PaymentSuccessReceiptView(
    payload: VendorPaymentPayload,
    paidAmount: Double,
    walletBalanceAfter: Double,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(60.dp)
        )

        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = GreenPrimary
        )

        Text(
            text = "₹${paidAmount.toInt()}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paid To:", style = MaterialTheme.typography.bodySmall)
                    Text(payload.vendorName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("UPI ID:", style = MaterialTheme.typography.bodySmall)
                    Text(payload.vendorUpiId, style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Supply Item:", style = MaterialTheme.typography.bodySmall)
                    Text(payload.supplyDescription.take(24), style = MaterialTheme.typography.bodySmall)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transaction Ref:", style = MaterialTheme.typography.bodySmall)
                    Text("TXN-KF-${System.currentTimeMillis() % 1000000}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Updated Wallet Balance:", style = MaterialTheme.typography.bodySmall)
                    Text("₹${walletBalanceAfter.toInt()}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                }
            }
        }

        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("receipt_done_btn")
        ) {
            Text("Done / Back to Shop", fontWeight = FontWeight.Bold)
        }
    }
}
