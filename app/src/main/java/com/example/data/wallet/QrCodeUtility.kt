package com.example.data.wallet

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object QrCodeUtility {

    /**
     * Generates a high-contrast standard QR Code bitmap.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
            }
        }
        return bitmap
    }

    /**
     * Encodes payment details into a standard Indian BharatQR / UPI link payload.
     */
    fun buildUpiQrString(
        vpa: String,
        vendorName: String,
        amountRupees: Double,
        supplyItem: String,
        refId: String = "TXN${System.currentTimeMillis() % 1000000}"
    ): String {
        val encodedName = URLEncoder.encode(vendorName, StandardCharsets.UTF_8.name())
        val encodedNote = URLEncoder.encode(supplyItem, StandardCharsets.UTF_8.name())
        val amountStr = if (amountRupees > 0) "&am=%.2f".format(amountRupees) else ""
        return "upi://pay?pa=$vpa&pn=$encodedName$amountStr&cu=INR&tn=$encodedNote&tr=$refId"
    }

    /**
     * Parses UPI, KrishiPay, or raw text vendor payment payloads.
     */
    fun parseVendorPaymentPayload(rawText: String): VendorPaymentPayload {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("upi://pay", ignoreCase = true)) {
            val uri = Uri.parse(trimmed)
            val pa = uri.getQueryParameter("pa") ?: "vendor.krishi@upi"
            val pn = uri.getQueryParameter("pn")?.let {
                try {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.name())
                } catch (e: Exception) {
                    it
                }
            } ?: "Krishi Agro Center"
            val amStr = uri.getQueryParameter("am") ?: "0.0"
            val tn = uri.getQueryParameter("tn")?.let {
                try {
                    URLDecoder.decode(it, StandardCharsets.UTF_8.name())
                } catch (e: Exception) {
                    it
                }
            } ?: "Farming Supplies & Seeds"
            val tr = uri.getQueryParameter("tr") ?: "REF-${System.currentTimeMillis() % 100000}"

            val parsedAmount = amStr.toDoubleOrNull() ?: 0.0
            return VendorPaymentPayload(
                vendorId = pa.substringBefore("@"),
                vendorName = pn,
                vendorUpiId = pa,
                amountRupees = parsedAmount,
                supplyDescription = tn,
                referenceOrder = tr
            )
        }

        if (trimmed.startsWith("KRISHIPAY:", ignoreCase = true)) {
            val parts = trimmed.split(":")
            val vendorId = parts.getOrNull(1) ?: "VEND-101"
            val vendorName = parts.getOrNull(2) ?: "Krishi Seva Kendra"
            val amount = parts.getOrNull(3)?.toDoubleOrNull() ?: 1200.0
            val supplyDesc = parts.getOrNull(4) ?: "Certified Inputs & Seeds"
            return VendorPaymentPayload(
                vendorId = vendorId,
                vendorName = vendorName,
                vendorUpiId = "$vendorId@krishipay",
                amountRupees = amount,
                supplyDescription = supplyDesc,
                referenceOrder = "ORDER-${System.currentTimeMillis() % 10000}"
            )
        }

        return VendorPaymentPayload(
            vendorId = "VEND-GENERAL",
            vendorName = "Authorized Agro Vendor",
            vendorUpiId = "agrivendor@upi",
            amountRupees = 1500.0,
            supplyDescription = if (trimmed.length > 5) trimmed else "Agricultural Bio-Supplies",
            referenceOrder = "TXN-${System.currentTimeMillis() % 100000}"
        )
    }

    val DEMO_VENDOR_QRS = listOf(
        VendorPaymentPayload(
            vendorId = "VEND-NSK-102",
            vendorName = "Krishi Seva Kendra, Nashik",
            vendorUpiId = "krishiseva.nashik@upi",
            amountRupees = 1450.0,
            supplyDescription = "Rhizobium Bio-fertilizer (5 Kg) + Neem Extract 10,000 ppm",
            referenceOrder = "ORD-BIO-8921"
        ),
        VendorPaymentPayload(
            vendorId = "VEND-PUN-304",
            vendorName = "Kisan Seeds & Agri Implements",
            vendorUpiId = "kisanseeds.punjab@upi",
            amountRupees = 2850.0,
            supplyDescription = "Certified Sharbati Wheat Lok-1 Seeds (40 Kg)",
            referenceOrder = "ORD-SEED-4109"
        ),
        VendorPaymentPayload(
            vendorId = "VEND-MAH-509",
            vendorName = "Mahalaxmi Micro-Irrigation Spares",
            vendorUpiId = "mahalaxmi.drip@upi",
            amountRupees = 3200.0,
            supplyDescription = "16mm Drip Lateral Pipe (500m Roll) + Venturi Injector",
            referenceOrder = "ORD-DRIP-7712"
        ),
        VendorPaymentPayload(
            vendorId = "VEND-BLR-812",
            vendorName = "Organic Earth Vermicompost Hub",
            vendorUpiId = "organicearth@upi",
            amountRupees = 950.0,
            supplyDescription = "Trichoderma Viride Organic Culture (2 Kg)",
            referenceOrder = "ORD-ORG-5023"
        )
    )
}
