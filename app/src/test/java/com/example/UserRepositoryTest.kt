package com.example

import com.example.data.model.FarmerDetails
import com.example.data.model.MandiManagerDetails
import com.example.data.model.RegionalManagerDetails
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.VendorDetails
import com.example.data.repository.FirestoreUserRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        // Use in-memory mock Firestore provider for fast, deterministic unit testing
        userRepository = FirestoreUserRepository(firestoreProvider = { null })
    }

    @Test
    fun testUserRoleAndProfessionMapping() {
        val farmerRole = UserRole.fromCode("FARMER")
        assertEquals(UserRole.Profession.FARMER, farmerRole.profession)
        assertEquals("Farmer", farmerRole.roleTitle)
        assertTrue(farmerRole.permissions.contains("list_harvest_produce"))

        val vendorRole = UserRole.fromCode("SUPERMARKET")
        assertEquals(UserRole.Profession.VENDOR, vendorRole.profession)
        assertEquals("Vendor", vendorRole.roleTitle)
        assertTrue(vendorRole.permissions.contains("place_bulk_orders"))

        val mandiRole = UserRole.fromCode("MANDI_MANAGER")
        assertEquals(UserRole.Profession.MANDI_MANAGER, mandiRole.profession)
        assertEquals("Mandi Manager", mandiRole.roleTitle)
        assertTrue(mandiRole.permissions.contains("publish_daily_mandi_rates"))

        val regionalRole = UserRole.fromCode("REGIONAL_MANAGER")
        assertEquals(UserRole.Profession.REGIONAL_MANAGER, regionalRole.profession)
        assertEquals("Regional Manager", regionalRole.roleTitle)
        assertTrue(regionalRole.permissions.contains("spoilage_mitigation_dispatch"))
    }

    @Test
    fun testFarmerProfileLifecycle() = runBlocking {
        val farmer = UserProfile(
            userId = "USER_FARMER_001",
            fullName = "Tukaram Shinde",
            email = "tukaram@krishiflow.in",
            phoneNumber = "+91 98221 23456",
            role = UserRole.FARMER,
            district = "Nashik",
            villageOrCity = "Sinnar",
            farmerDetails = FarmerDetails(
                landAreaAcres = 8.0,
                primaryCrops = listOf("Onion", "Soybean"),
                upiId = "tukaram@upi",
                soilType = "Medium Black Soil"
            )
        )

        // Save
        val saveResult = userRepository.saveOrUpdateUserProfile(farmer)
        assertTrue(saveResult.isSuccess)

        // Fetch
        val fetched = userRepository.getUserProfile("USER_FARMER_001").getOrNull()
        assertNotNull(fetched)
        assertEquals("Tukaram Shinde", fetched?.fullName)
        assertEquals(UserRole.Profession.FARMER, fetched?.profession)
        assertEquals(8.0, fetched?.farmerDetails?.landAreaAcres ?: 0.0, 0.01)

        // Update Farmer Details
        val updatedDetails = fetched!!.farmerDetails!!.copy(
            landAreaAcres = 12.5,
            primaryCrops = listOf("Onion", "Soybean", "Pomegranate")
        )
        val updateDetailsResult = userRepository.updateFarmerDetails("USER_FARMER_001", updatedDetails)
        assertTrue(updateDetailsResult.isSuccess)

        val refetched = userRepository.getUserProfile("USER_FARMER_001").getOrNull()
        assertEquals(12.5, refetched?.farmerDetails?.landAreaAcres ?: 0.0, 0.01)
        assertEquals(3, refetched?.farmerDetails?.primaryCrops?.size)

        // Update profession to Regional Manager
        val updateProfResult = userRepository.updateUserProfession("USER_FARMER_001", UserRole.REGIONAL_MANAGER)
        assertTrue(updateProfResult.isSuccess)
        val profileAfterRoleSwitch = userRepository.getUserProfile("USER_FARMER_001").getOrNull()
        assertEquals(UserRole.Profession.REGIONAL_MANAGER, profileAfterRoleSwitch?.profession)
    }

    @Test
    fun testVendorProfileLifecycle() = runBlocking {
        val vendor = UserProfile(
            userId = "USER_VENDOR_002",
            fullName = "Reliance Fresh Procurement",
            email = "procurement@reliancefresh.in",
            phoneNumber = "+91 98222 34567",
            role = UserRole.VENDOR,
            district = "Pune",
            villageOrCity = "Hadapsar",
            vendorDetails = VendorDetails(
                businessName = "Reliance Retail Ltd",
                vendorType = "SUPERMARKET_MART",
                dailyProcurementCapacityKg = 25000.0,
                creditLimitRupees = 1000000.0
            )
        )

        userRepository.saveOrUpdateUserProfile(vendor)

        val vendors = userRepository.getUsersByProfession(UserRole.VENDOR).getOrDefault(emptyList())
        assertTrue(vendors.any { it.userId == "USER_VENDOR_002" })

        // Update Vendor Details
        val updatedDetails = vendor.vendorDetails!!.copy(creditLimitRupees = 1500000.0)
        userRepository.updateVendorDetails("USER_VENDOR_002", updatedDetails)

        val fetched = userRepository.getUserProfile("USER_VENDOR_002").getOrNull()
        assertEquals(1500000.0, fetched?.vendorDetails?.creditLimitRupees ?: 0.0, 0.01)
    }

    @Test
    fun testMandiManagerProfileLifecycle() = runBlocking {
        val manager = UserProfile(
            userId = "USER_MANDI_003",
            fullName = "Sanjay Deshmukh",
            email = "secretary@nashikapmc.in",
            phoneNumber = "+91 98223 45678",
            role = UserRole.MANDI_MANAGER,
            district = "Nashik",
            villageOrCity = "Panchavati",
            mandiManagerDetails = MandiManagerDetails(
                mandiName = "Nashik APMC Yard",
                mandiCode = "MH-NSK-01",
                licensedTradersCount = 310,
                dailyYardCapacityTons = 550.0
            )
        )

        userRepository.saveOrUpdateUserProfile(manager)

        val managers = userRepository.getUsersByProfession(UserRole.MANDI_MANAGER).getOrDefault(emptyList())
        assertTrue(managers.any { it.userId == "USER_MANDI_003" })

        val updatedDetails = manager.mandiManagerDetails!!.copy(dailyYardCapacityTons = 600.0)
        userRepository.updateMandiManagerDetails("USER_MANDI_003", updatedDetails)

        val fetched = userRepository.getUserProfile("USER_MANDI_003").getOrNull()
        assertEquals(600.0, fetched?.mandiManagerDetails?.dailyYardCapacityTons ?: 0.0, 0.01)
    }

    @Test
    fun testRegionalManagerProfileLifecycle() = runBlocking {
        val regManager = UserProfile(
            userId = "USER_REGIONAL_004",
            fullName = "Anand Joshi",
            email = "anand.joshi@krishiflow.in",
            phoneNumber = "+91 98224 56789",
            role = UserRole.REGIONAL_MANAGER,
            district = "Pune",
            regionalManagerDetails = RegionalManagerDetails(
                employeeId = "KF-WEST-02",
                assignedHub = "Pune Regional Hub",
                managedDistricts = listOf("Pune", "Satara", "Solapur")
            )
        )

        userRepository.saveOrUpdateUserProfile(regManager)

        val regionalManagers = userRepository.getUsersByProfession(UserRole.REGIONAL_MANAGER).getOrDefault(emptyList())
        assertTrue(regionalManagers.any { it.userId == "USER_REGIONAL_004" })

        // Delete profile (soft delete)
        val deleteResult = userRepository.deleteUserProfile("USER_REGIONAL_004", softDelete = true)
        assertTrue(deleteResult.isSuccess)
        assertFalse(userRepository.getUserProfile("USER_REGIONAL_004").getOrNull()?.isVerified == true)
    }

    @Test
    fun testSeedSampleProfiles() = runBlocking {
        userRepository.seedSampleProfilesIfEmpty()

        val farmers = userRepository.getUsersByProfession(UserRole.FARMER).getOrDefault(emptyList())
        val vendors = userRepository.getUsersByProfession(UserRole.VENDOR).getOrDefault(emptyList())
        val mandiManagers = userRepository.getUsersByProfession(UserRole.MANDI_MANAGER).getOrDefault(emptyList())
        val regionalManagers = userRepository.getUsersByProfession(UserRole.REGIONAL_MANAGER).getOrDefault(emptyList())

        assertTrue("Farmers seeded", farmers.isNotEmpty())
        assertTrue("Vendors seeded", vendors.isNotEmpty())
        assertTrue("Mandi managers seeded", mandiManagers.isNotEmpty())
        assertTrue("Regional managers seeded", regionalManagers.isNotEmpty())
    }
}
