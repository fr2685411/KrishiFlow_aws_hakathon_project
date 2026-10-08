package com.example.data.model

/**
 * Domain model representing a registered Farmer in KrishiFlow.
 */
data class FarmerProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val village: String = "",
    val district: String = "",
    val state: String = "Maharashtra",
    val landAreaAcres: Double = 0.0,
    val primaryCrops: List<String> = emptyList(),
    val upiId: String = "",
    val rating: Double = 5.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "village" to village,
        "district" to district,
        "state" to state,
        "landAreaAcres" to landAreaAcres,
        "primaryCrops" to primaryCrops,
        "upiId" to upiId,
        "rating" to rating,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): FarmerProfile = FarmerProfile(
            id = (map["id"] as? String) ?: id,
            name = (map["name"] as? String) ?: "",
            phone = (map["phone"] as? String) ?: "",
            village = (map["village"] as? String) ?: "",
            district = (map["district"] as? String) ?: "",
            state = (map["state"] as? String) ?: "Maharashtra",
            landAreaAcres = (map["landAreaAcres"] as? Number)?.toDouble() ?: 0.0,
            primaryCrops = (map["primaryCrops"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            upiId = (map["upiId"] as? String) ?: "",
            rating = (map["rating"] as? Number)?.toDouble() ?: 5.0,
            isActive = (map["isActive"] as? Boolean) ?: true,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

/**
 * Domain model representing a registered Buyer / Commercial Vendor in KrishiFlow.
 */
data class VendorProfile(
    val id: String = "",
    val businessName: String = "",
    val contactPerson: String = "",
    val phone: String = "",
    val email: String = "",
    val vendorType: String = "SUPERMARKET_MART", // LOCAL_VENDOR, MANDI_WHOLESALER, SUPERMARKET_MART, RETAIL_CONSUMER
    val destinationLocation: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val gstin: String = "",
    val creditLimitRupees: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "businessName" to businessName,
        "contactPerson" to contactPerson,
        "phone" to phone,
        "email" to email,
        "vendorType" to vendorType,
        "destinationLocation" to destinationLocation,
        "city" to city,
        "state" to state,
        "gstin" to gstin,
        "creditLimitRupees" to creditLimitRupees,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): VendorProfile = VendorProfile(
            id = (map["id"] as? String) ?: id,
            businessName = (map["businessName"] as? String) ?: "",
            contactPerson = (map["contactPerson"] as? String) ?: "",
            phone = (map["phone"] as? String) ?: "",
            email = (map["email"] as? String) ?: "",
            vendorType = (map["vendorType"] as? String) ?: "SUPERMARKET_MART",
            destinationLocation = (map["destinationLocation"] as? String) ?: "",
            city = (map["city"] as? String) ?: "",
            state = (map["state"] as? String) ?: "Maharashtra",
            gstin = (map["gstin"] as? String) ?: "",
            creditLimitRupees = (map["creditLimitRupees"] as? Number)?.toDouble() ?: 0.0,
            isActive = (map["isActive"] as? Boolean) ?: true,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

/**
 * Domain model representing an APMC Mandi Manager / Yard Operations Lead in KrishiFlow.
 */
data class MandiManagerProfile(
    val id: String = "",
    val managerName: String = "",
    val phone: String = "",
    val email: String = "",
    val mandiName: String = "",
    val mandiCode: String = "",
    val district: String = "",
    val state: String = "Maharashtra",
    val licensedTradersCount: Int = 0,
    val dailyCapacityTons: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "managerName" to managerName,
        "phone" to phone,
        "email" to email,
        "mandiName" to mandiName,
        "mandiCode" to mandiCode,
        "district" to district,
        "state" to state,
        "licensedTradersCount" to licensedTradersCount,
        "dailyCapacityTons" to dailyCapacityTons,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): MandiManagerProfile = MandiManagerProfile(
            id = (map["id"] as? String) ?: id,
            managerName = (map["managerName"] as? String) ?: "",
            phone = (map["phone"] as? String) ?: "",
            email = (map["email"] as? String) ?: "",
            mandiName = (map["mandiName"] as? String) ?: "",
            mandiCode = (map["mandiCode"] as? String) ?: "",
            district = (map["district"] as? String) ?: "",
            state = (map["state"] as? String) ?: "Maharashtra",
            licensedTradersCount = (map["licensedTradersCount"] as? Number)?.toInt() ?: 0,
            dailyCapacityTons = (map["dailyCapacityTons"] as? Number)?.toDouble() ?: 0.0,
            isActive = (map["isActive"] as? Boolean) ?: true,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
