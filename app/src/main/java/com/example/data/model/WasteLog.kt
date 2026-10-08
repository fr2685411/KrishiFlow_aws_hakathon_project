package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Destination options for agricultural surplus and waste.
 * Allows farmers to log quantities diverted for donation, composting,
 * animal feed, or biogas production instead of dumping into landfills.
 */
enum class WasteDestination(val displayName: String, val badgeColorHex: Long) {
    DONATION("Donation (Food Banks / NGOs)", 0xFF10B981), // Green
    COMPOSTING("Composting (Organic Fertilizer)", 0xFFD97706), // Amber
    ANIMAL_FEED("Animal Feed (Gaushala / Cattle)", 0xFF3B82F6), // Blue
    BIOGAS("Biogas Digester (Clean Energy)", 0xFF8B5CF6); // Purple

    companion object {
        fun fromString(name: String?): WasteDestination {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DONATION
        }
    }
}

/**
 * Underlying root causes or reasons for surplus and agricultural waste.
 */
enum class WasteReason(val displayName: String) {
    MARKET_SURPLUS("Unsold Market Surplus"),
    OVER_RIPE("Over-Ripe / Near Expiry"),
    STORAGE_LOSS("Cold Storage / Ambient Spoilage"),
    HARVEST_DAMAGE("Harvest / Handling Damage"),
    COSMETIC_BLEMISH("Cosmetic Imperfection / Grade B/C"),
    WEATHER_DAMAGE("Rain / Hail / Heat Damage");

    companion object {
        fun fromString(name: String?): WasteReason {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MARKET_SURPLUS
        }
    }
}

/**
 * Room Database entity recording daily agricultural surplus and waste.
 * Enables farmers to log quantities earmarked for donation to local charities
 * or for on-farm/community composting.
 */
@Entity(tableName = "waste_logs")
data class WasteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropName: String,
    val category: CropCategory = CropCategory.VEGETABLES,
    val quantityKg: Double,
    val destination: WasteDestination = WasteDestination.DONATION,
    val reason: WasteReason = WasteReason.MARKET_SURPLUS,
    val logDateTimestamp: Long = System.currentTimeMillis(),
    val destinationPartner: String = "", // e.g. "Akshaya Patra Foundation", "Village Vermicompost Pit"
    val estimatedValueRupees: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(logDateTimestamp))

    val formattedQuantity: String
        get() = String.format(Locale.getDefault(), "%.1f kg", quantityKg)

    val formattedValue: String
        get() = String.format(Locale.getDefault(), "₹%.0f", estimatedValueRupees)

    /**
     * Estimated CO2 equivalent emissions diverted from landfills.
     * Organic food rotting anaerobically in open landfills produces methane (~1.9 kg CO2e per kg of food waste).
     * Composting aerobically or donating for direct human consumption saves this footprint.
     */
    val co2DivertedKg: Double
        get() = quantityKg * 1.9

    val formattedCo2Diverted: String
        get() = String.format(Locale.getDefault(), "%.1f kg CO₂e saved", co2DivertedKg)
}

/**
 * Typealias for naming consistency across entity representations.
 */
typealias WasteLogEntity = WasteLog
