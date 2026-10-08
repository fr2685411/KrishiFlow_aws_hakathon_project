package com.example.data.model

import java.util.UUID

/**
 * Represents a user-configured price alert threshold for a registered crop.
 * When the live APMC Mandi rate or market price meets or exceeds targetPricePerKg
 * (or optionally drops below a stop-loss floor), a high-priority local Android notification
 * is dispatched to the farmer's device.
 */
data class CropPriceThreshold(
    val id: String = UUID.randomUUID().toString(),
    val cropName: String,
    val variety: String = "",
    val targetPricePerKg: Double, // Price at or above which an alert fires
    val targetMandiName: String = "All Nearby Mandis",
    val triggerCondition: ThresholdTriggerCondition = ThresholdTriggerCondition.GREATER_THAN_EQUAL,
    val isEnabled: Boolean = true,
    val lastTriggeredPrice: Double? = null,
    val lastTriggeredTimestamp: Long? = null,
    val notes: String = ""
)

enum class ThresholdTriggerCondition(val label: String, val description: String) {
    GREATER_THAN_EQUAL("Price Hits or Exceeds (≥)", "Alert when mandi rate hits target selling price"),
    LESS_THAN_EQUAL("Price Drops Below (≤)", "Alert on market drop to protect inventory")
}
