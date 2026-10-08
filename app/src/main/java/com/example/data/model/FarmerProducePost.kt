package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Domain & Cloud Firestore data model for agricultural produce posted by farmers.
 * Captures crop taxonomy, variety, harvest quantity, farmer asking price,
 * APMC mandi benchmark comparison, quality grades, storage conditions, and fulfillment state.
 */
data class FarmerProducePost(
    val postId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val farmerPhone: String = "",
    val farmLocation: String = "",
    val district: String = "Nashik",
    val state: String = "Maharashtra",
    val cropName: String = "",
    val category: CropCategory = CropCategory.VEGETABLES,
    val variety: String = "",
    val quantityKg: Double = 0.0,
    val quantityUnit: String = "KG", // "KG", "QUINTAL", "TONNE"
    val askingPricePerKg: Double = 0.0,
    val mandiBenchmarkPricePerKg: Double = 0.0,
    val qualityGrade: QualityGrade = QualityGrade.GRADE_A,
    val storageCondition: StorageCondition = StorageCondition.VENTILATED_CRATE,
    val expectedFreshnessDays: Int = 7,
    val harvestDate: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
    val isOrganicOrResidueFree: Boolean = false,
    val readyForImmediatePickup: Boolean = true,
    val notes: String = "",
    val status: String = "LISTED", // LISTED, PROCURED_IN_HUB, COMPLETED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Total gross valuation expected by the farmer (Quantity in kg * Asking Price/kg)
     */
    val totalEstimatedPayout: Double
        get() = quantityKg * askingPricePerKg

    /**
     * Estimated extra revenue earned by bypassing traditional mandi middlemen commission (8-12%)
     */
    val middlemanSavingsRupees: Double
        get() {
            val benchmarkRate = if (mandiBenchmarkPricePerKg > 0.0) mandiBenchmarkPricePerKg else askingPricePerKg * 0.88
            val diffPerKg = (askingPricePerKg - (benchmarkRate * 0.90)).coerceAtLeast(0.0)
            return diffPerKg * quantityKg
        }

    /**
     * Formats the quantity for user display depending on chosen unit
     */
    val formattedQuantity: String
        get() = when (quantityUnit.uppercase()) {
            "QUINTAL" -> "${String.format(Locale.getDefault(), "%.1f", quantityKg / 100.0)} Quintals (${quantityKg.toInt()} kg)"
            "TONNE" -> "${String.format(Locale.getDefault(), "%.2f", quantityKg / 1000.0)} Tonnes (${quantityKg.toInt()} kg)"
            else -> "${quantityKg.toInt()} kg"
        }

    /**
     * Serialize to Firestore document map for Cloud Firestore persistence
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "postId" to postId,
        "farmerId" to farmerId,
        "farmerName" to farmerName,
        "farmerPhone" to farmerPhone,
        "farmLocation" to farmLocation,
        "district" to district,
        "state" to state,
        "cropName" to cropName,
        "category" to category.name,
        "variety" to variety,
        "quantityKg" to quantityKg,
        "quantityUnit" to quantityUnit,
        "askingPricePerKg" to askingPricePerKg,
        "mandiBenchmarkPricePerKg" to mandiBenchmarkPricePerKg,
        "qualityGrade" to qualityGrade.name,
        "storageCondition" to storageCondition.name,
        "expectedFreshnessDays" to expectedFreshnessDays,
        "harvestDate" to harvestDate,
        "isOrganicOrResidueFree" to isOrganicOrResidueFree,
        "readyForImmediatePickup" to readyForImmediatePickup,
        "notes" to notes,
        "status" to status,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        /**
         * Deserialize from Cloud Firestore document map
         */
        fun fromMap(id: String, map: Map<String, Any?>): FarmerProducePost {
            val categoryStr = (map["category"] as? String) ?: CropCategory.VEGETABLES.name
            val gradeStr = (map["qualityGrade"] as? String) ?: QualityGrade.GRADE_A.name
            val storageStr = (map["storageCondition"] as? String) ?: StorageCondition.VENTILATED_CRATE.name

            return FarmerProducePost(
                postId = (map["postId"] as? String) ?: id,
                farmerId = (map["farmerId"] as? String) ?: "",
                farmerName = (map["farmerName"] as? String) ?: "",
                farmerPhone = (map["farmerPhone"] as? String) ?: "",
                farmLocation = (map["farmLocation"] as? String) ?: "",
                district = (map["district"] as? String) ?: "Nashik",
                state = (map["state"] as? String) ?: "Maharashtra",
                cropName = (map["cropName"] as? String) ?: "",
                category = runCatching { CropCategory.valueOf(categoryStr) }.getOrDefault(CropCategory.VEGETABLES),
                variety = (map["variety"] as? String) ?: "",
                quantityKg = (map["quantityKg"] as? Number)?.toDouble() ?: 0.0,
                quantityUnit = (map["quantityUnit"] as? String) ?: "KG",
                askingPricePerKg = (map["askingPricePerKg"] as? Number)?.toDouble() ?: 0.0,
                mandiBenchmarkPricePerKg = (map["mandiBenchmarkPricePerKg"] as? Number)?.toDouble() ?: 0.0,
                qualityGrade = runCatching { QualityGrade.valueOf(gradeStr) }.getOrDefault(QualityGrade.GRADE_A),
                storageCondition = runCatching { StorageCondition.valueOf(storageStr) }.getOrDefault(StorageCondition.VENTILATED_CRATE),
                expectedFreshnessDays = (map["expectedFreshnessDays"] as? Number)?.toInt() ?: 7,
                harvestDate = (map["harvestDate"] as? String) ?: SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                isOrganicOrResidueFree = (map["isOrganicOrResidueFree"] as? Boolean) ?: false,
                readyForImmediatePickup = (map["readyForImmediatePickup"] as? Boolean) ?: true,
                notes = (map["notes"] as? String) ?: "",
                status = (map["status"] as? String) ?: "LISTED",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
