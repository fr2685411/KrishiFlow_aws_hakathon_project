package com.example.data.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeUtilityTest {

    @Test
    fun testGenerateQrBitmapProducesValidBitmap() {
        val payload = "upi://pay?pa=krishiseva.nashik@upi&pn=Krishi%20Seva%20Kendra&am=1450.00&cu=INR"
        val bitmap = QrCodeUtility.generateQrBitmap(payload, sizePx = 256)

        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }

    @Test
    fun testBuildUpiQrString() {
        val upiString = QrCodeUtility.buildUpiQrString(
            vpa = "agro.vendor@upi",
            vendorName = "Shivaji Agri Inputs",
            amountRupees = 2400.0,
            supplyItem = "Trichoderma Organic Culture",
            refId = "REF-9912"
        )

        assertTrue(upiString.startsWith("upi://pay?"))
        assertTrue(upiString.contains("pa=agro.vendor@upi"))
        assertTrue(upiString.contains("am=2400.00"))
        assertTrue(upiString.contains("cu=INR"))
        assertTrue(upiString.contains("tr=REF-9912"))
    }

    @Test
    fun testParseUpiVendorPaymentPayload() {
        val rawUpi = "upi://pay?pa=krishiseva.nashik@upi&pn=Krishi+Seva+Kendra&am=1450.00&cu=INR&tn=Certified+Seeds&tr=ORD-8921"
        val payload = QrCodeUtility.parseVendorPaymentPayload(rawUpi)

        assertEquals("krishiseva.nashik@upi", payload.vendorUpiId)
        assertEquals("Krishi Seva Kendra", payload.vendorName)
        assertEquals(1450.0, payload.amountRupees, 0.01)
        assertEquals("Certified Seeds", payload.supplyDescription)
        assertEquals("ORD-8921", payload.referenceOrder)
    }

    @Test
    fun testParseKrishiPayCustomProtocol() {
        val krishiPay = "KRISHIPAY:VEND-501:Kisan Bio Tech:3200:Drip Irrigation Laterals"
        val payload = QrCodeUtility.parseVendorPaymentPayload(krishiPay)

        assertEquals("VEND-501", payload.vendorId)
        assertEquals("Kisan Bio Tech", payload.vendorName)
        assertEquals(3200.0, payload.amountRupees, 0.01)
        assertEquals("Drip Irrigation Laterals", payload.supplyDescription)
    }
}
