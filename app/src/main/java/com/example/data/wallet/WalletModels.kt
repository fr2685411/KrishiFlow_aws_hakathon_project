package com.example.data.wallet

enum class TransactionType(val displayName: String, val hindiName: String) {
    SUPPLIES_PAYMENT("Farming Supplies Payment", "कृषि सामग्री भुगतान"),
    WALLET_TOPUP("Digital Wallet Top-Up", "वॉलेट टॉप-अप"),
    PRODUCE_SALE_CREDIT("Produce Sale Credit", "फसल बिक्री जमा")
}

data class WalletTransaction(
    val id: String,
    val vendorName: String,
    val vendorUpiId: String,
    val amountRupees: Double,
    val supplyDescription: String,
    val type: TransactionType,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String,
    val balanceAfterRupees: Double
)

data class VendorPaymentPayload(
    val vendorId: String,
    val vendorName: String,
    val vendorUpiId: String,
    val amountRupees: Double,
    val supplyDescription: String,
    val referenceOrder: String
)
