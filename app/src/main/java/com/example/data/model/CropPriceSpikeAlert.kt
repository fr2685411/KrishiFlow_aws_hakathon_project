package com.example.data.model

enum class PriceSpikeUrgency(val label: String, val hindiLabel: String) {
    SURGE("Market Surge (+15-25%)", "भाव में मध्यम उछाल"),
    PEAK("Prime Peak (+25-40%)", "उच्चतम शिखर भाव"),
    SUPER_SPIKE("Historic High (>+40%)", "ऐतिहासिक रिकॉर्ड भाव")
}

data class CropPriceSpikeAlert(
    val alertId: String,
    val cropName: String,
    val variety: String,
    val matchedBatchCode: String,
    val batchQuantityKg: Double,
    val farmerBaseAskingPrice: Double,
    val currentSpikePricePerKg: Double,
    val spikePercentage: Double,
    val mandiName: String,
    val bestSellingWindowHours: Int = 36,
    val potentialExtraEarningsRupees: Double,
    val marketReason: String,
    val urgency: PriceSpikeUrgency,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDismissed: Boolean = false
) {
    val totalProjectedPayout: Double
        get() = batchQuantityKg * currentSpikePricePerKg
}
