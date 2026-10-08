package com.example.data.model

/**
 * Data class representing a user's selected role and permissions in KrishiFlow.
 * Supports the 4 core professions:
 * - Farmer
 * - Vendor
 * - Mandi Manager
 * - Regional Manager
 */
data class UserRole(
    val profession: Profession = Profession.FARMER,
    val roleTitle: String = profession.displayTitle,
    val permissions: List<String> = profession.defaultPermissions
) {
    enum class Profession(
        val code: String,
        val displayTitle: String,
        val defaultPermissions: List<String>
    ) {
        FARMER(
            code = "FARMER",
            displayTitle = "Farmer",
            defaultPermissions = listOf(
                "list_harvest_produce",
                "view_market_demand",
                "accept_direct_orders",
                "receive_instant_upi_payout"
            )
        ),
        VENDOR(
            code = "VENDOR",
            displayTitle = "Vendor",
            defaultPermissions = listOf(
                "post_procurement_rfq",
                "browse_farm_marketplace",
                "place_bulk_orders",
                "track_temp_controlled_fleet"
            )
        ),
        MANDI_MANAGER(
            code = "MANDI_MANAGER",
            displayTitle = "Mandi Manager",
            defaultPermissions = listOf(
                "publish_daily_mandi_rates",
                "manage_yard_arrivals",
                "inspect_produce_grade",
                "trader_clearance_audit"
            )
        ),
        REGIONAL_MANAGER(
            code = "REGIONAL_MANAGER",
            displayTitle = "Regional Manager",
            defaultPermissions = listOf(
                "view_regional_logistics_grid",
                "spoilage_mitigation_dispatch",
                "price_arbitrage_matching",
                "hub_storage_allocation",
                "system_audit_logs"
            )
        )
    }

    companion object {
        val FARMER = UserRole(Profession.FARMER)
        val VENDOR = UserRole(Profession.VENDOR)
        val MANDI_MANAGER = UserRole(Profession.MANDI_MANAGER)
        val REGIONAL_MANAGER = UserRole(Profession.REGIONAL_MANAGER)

        fun fromCode(code: String?): UserRole {
            val prof = when (code?.uppercase()?.trim()?.replace(" ", "_")?.replace("-", "_")) {
                "FARMER", "KISAN" -> Profession.FARMER
                "VENDOR", "BUYER", "VYAPAR", "SUPERMARKET", "MART" -> Profession.VENDOR
                "MANDI_MANAGER", "MANDI", "APMC_MANAGER", "YARD_MANAGER" -> Profession.MANDI_MANAGER
                "REGIONAL_MANAGER", "REGIONAL", "OPS_LEAD", "CENTRAL_OPS" -> Profession.REGIONAL_MANAGER
                else -> Profession.FARMER
            }
            return UserRole(prof)
        }
    }
}

/**
 * Detailed attributes specific to a Farmer profile.
 */
data class FarmerDetails(
    val landAreaAcres: Double = 0.0,
    val primaryCrops: List<String> = emptyList(),
    val upiId: String = "",
    val soilType: String = "Black Cotton / Loamy",
    val irrigationType: String = "Drip / Canal",
    val kisanCreditCardNo: String = "",
    val farmLatitude: Double = 0.0,
    val farmLongitude: Double = 0.0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "landAreaAcres" to landAreaAcres,
        "primaryCrops" to primaryCrops,
        "upiId" to upiId,
        "soilType" to soilType,
        "irrigationType" to irrigationType,
        "kisanCreditCardNo" to kisanCreditCardNo,
        "farmLatitude" to farmLatitude,
        "farmLongitude" to farmLongitude
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>?): FarmerDetails? {
            if (map == null) return null
            return FarmerDetails(
                landAreaAcres = (map["landAreaAcres"] as? Number)?.toDouble() ?: 0.0,
                primaryCrops = (map["primaryCrops"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                upiId = (map["upiId"] as? String) ?: "",
                soilType = (map["soilType"] as? String) ?: "Black Cotton / Loamy",
                irrigationType = (map["irrigationType"] as? String) ?: "Drip / Canal",
                kisanCreditCardNo = (map["kisanCreditCardNo"] as? String) ?: "",
                farmLatitude = (map["farmLatitude"] as? Number)?.toDouble() ?: 0.0,
                farmLongitude = (map["farmLongitude"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

/**
 * Detailed attributes specific to a Commercial Vendor / Buyer profile.
 */
data class VendorDetails(
    val businessName: String = "",
    val vendorType: String = "SUPERMARKET_MART", // LOCAL_VENDOR, MANDI_WHOLESALER, SUPERMARKET_MART
    val gstin: String = "",
    val deliveryAddress: String = "",
    val dailyProcurementCapacityKg: Double = 0.0,
    val creditLimitRupees: Double = 0.0,
    val preferredPaymentTerms: String = "Instant / T+1"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "businessName" to businessName,
        "vendorType" to vendorType,
        "gstin" to gstin,
        "deliveryAddress" to deliveryAddress,
        "dailyProcurementCapacityKg" to dailyProcurementCapacityKg,
        "creditLimitRupees" to creditLimitRupees,
        "preferredPaymentTerms" to preferredPaymentTerms
    )

    companion object {
        fun fromMap(map: Map<String, Any?>?): VendorDetails? {
            if (map == null) return null
            return VendorDetails(
                businessName = (map["businessName"] as? String) ?: "",
                vendorType = (map["vendorType"] as? String) ?: "SUPERMARKET_MART",
                gstin = (map["gstin"] as? String) ?: "",
                deliveryAddress = (map["deliveryAddress"] as? String) ?: "",
                dailyProcurementCapacityKg = (map["dailyProcurementCapacityKg"] as? Number)?.toDouble() ?: 0.0,
                creditLimitRupees = (map["creditLimitRupees"] as? Number)?.toDouble() ?: 0.0,
                preferredPaymentTerms = (map["preferredPaymentTerms"] as? String) ?: "Instant / T+1"
            )
        }
    }
}

/**
 * Detailed attributes specific to an APMC Mandi Yard Manager profile.
 */
data class MandiManagerDetails(
    val mandiName: String = "",
    val mandiCode: String = "",
    val apmcZone: String = "",
    val licensedTradersCount: Int = 0,
    val dailyYardCapacityTons: Double = 0.0,
    val hasElectronicWeighbridge: Boolean = true,
    val coldStorageAvailableTons: Double = 0.0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "mandiName" to mandiName,
        "mandiCode" to mandiCode,
        "apmcZone" to apmcZone,
        "licensedTradersCount" to licensedTradersCount,
        "dailyYardCapacityTons" to dailyYardCapacityTons,
        "hasElectronicWeighbridge" to hasElectronicWeighbridge,
        "coldStorageAvailableTons" to coldStorageAvailableTons
    )

    companion object {
        fun fromMap(map: Map<String, Any?>?): MandiManagerDetails? {
            if (map == null) return null
            return MandiManagerDetails(
                mandiName = (map["mandiName"] as? String) ?: "",
                mandiCode = (map["mandiCode"] as? String) ?: "",
                apmcZone = (map["apmcZone"] as? String) ?: "",
                licensedTradersCount = (map["licensedTradersCount"] as? Number)?.toInt() ?: 0,
                dailyYardCapacityTons = (map["dailyYardCapacityTons"] as? Number)?.toDouble() ?: 0.0,
                hasElectronicWeighbridge = (map["hasElectronicWeighbridge"] as? Boolean) ?: true,
                coldStorageAvailableTons = (map["coldStorageAvailableTons"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

/**
 * Detailed attributes specific to a KrishiFlow Regional Operations Manager.
 */
data class RegionalManagerDetails(
    val employeeId: String = "",
    val assignedHub: String = "",
    val managedDistricts: List<String> = emptyList(),
    val directSupervisorsCount: Int = 0,
    val emergencyDispatchAuthority: Boolean = true
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "employeeId" to employeeId,
        "assignedHub" to assignedHub,
        "managedDistricts" to managedDistricts,
        "directSupervisorsCount" to directSupervisorsCount,
        "emergencyDispatchAuthority" to emergencyDispatchAuthority
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>?): RegionalManagerDetails? {
            if (map == null) return null
            return RegionalManagerDetails(
                employeeId = (map["employeeId"] as? String) ?: "",
                assignedHub = (map["assignedHub"] as? String) ?: "",
                managedDistricts = (map["managedDistricts"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                directSupervisorsCount = (map["directSupervisorsCount"] as? Number)?.toInt() ?: 0,
                emergencyDispatchAuthority = (map["emergencyDispatchAuthority"] as? Boolean) ?: true
            )
        }
    }
}

/**
 * Complete UserProfile document stored in Firestore collection 'krishiflow_users'.
 */
data class UserProfile(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.FARMER,
    val state: String = "Maharashtra",
    val district: String = "",
    val villageOrCity: String = "",
    val address: String = "",
    val profileImageUrl: String = "",
    val isVerified: Boolean = true,
    val farmerDetails: FarmerDetails? = null,
    val vendorDetails: VendorDetails? = null,
    val mandiManagerDetails: MandiManagerDetails? = null,
    val regionalManagerDetails: RegionalManagerDetails? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val profession: UserRole.Profession
        get() = role.profession

    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "fullName" to fullName,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "role" to role.profession.code,
        "roleTitle" to role.roleTitle,
        "state" to state,
        "district" to district,
        "villageOrCity" to villageOrCity,
        "address" to address,
        "profileImageUrl" to profileImageUrl,
        "isVerified" to isVerified,
        "farmerDetails" to farmerDetails?.toMap(),
        "vendorDetails" to vendorDetails?.toMap(),
        "mandiManagerDetails" to mandiManagerDetails?.toMap(),
        "regionalManagerDetails" to regionalManagerDetails?.toMap(),
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): UserProfile = UserProfile(
            userId = (map["userId"] as? String) ?: id,
            fullName = (map["fullName"] as? String) ?: "",
            email = (map["email"] as? String) ?: "",
            phoneNumber = (map["phoneNumber"] as? String) ?: "",
            role = UserRole.fromCode(map["role"] as? String),
            state = (map["state"] as? String) ?: "Maharashtra",
            district = (map["district"] as? String) ?: "",
            villageOrCity = (map["villageOrCity"] as? String) ?: "",
            address = (map["address"] as? String) ?: "",
            profileImageUrl = (map["profileImageUrl"] as? String) ?: "",
            isVerified = (map["isVerified"] as? Boolean) ?: true,
            farmerDetails = FarmerDetails.fromMap(map["farmerDetails"] as? Map<String, Any?>),
            vendorDetails = VendorDetails.fromMap(map["vendorDetails"] as? Map<String, Any?>),
            mandiManagerDetails = MandiManagerDetails.fromMap(map["mandiManagerDetails"] as? Map<String, Any?>),
            regionalManagerDetails = RegionalManagerDetails.fromMap(map["regionalManagerDetails"] as? Map<String, Any?>),
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
