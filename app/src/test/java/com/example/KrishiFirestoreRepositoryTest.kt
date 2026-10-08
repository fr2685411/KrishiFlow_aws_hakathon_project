package com.example

import com.example.data.model.CropCategory
import com.example.data.model.FarmerProducePost
import com.example.data.model.FarmerProfile
import com.example.data.model.MandiManagerProfile
import com.example.data.model.QualityGrade
import com.example.data.model.StorageCondition
import com.example.data.model.VendorProfile
import com.example.data.repository.KrishiFirestoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KrishiFirestoreRepositoryTest {

    private lateinit var repository: KrishiFirestoreRepository

    @Before
    fun setUp() {
        // Provide null Firestore instance so test operates against fast, deterministic in-memory local cache
        repository = KrishiFirestoreRepository(firestoreProvider = { null })
    }

    @Test
    fun testFarmerCrudOperations() = runBlocking {
        // 1. CREATE
        val initialFarmer = FarmerProfile(
            id = "FARMER_TEST_01",
            name = "Suresh Patil",
            phone = "+91 98220 99999",
            village = "Niphad",
            district = "Nashik",
            landAreaAcres = 5.0,
            primaryCrops = listOf("Tomato", "Onion"),
            upiId = "suresh@okhdfc"
        )
        val createdIdResult = repository.createFarmer(initialFarmer)
        assertTrue(createdIdResult.isSuccess)
        assertEquals("FARMER_TEST_01", createdIdResult.getOrNull())

        // 2. READ
        val fetchedResult = repository.getFarmer("FARMER_TEST_01")
        assertTrue(fetchedResult.isSuccess)
        val fetchedFarmer = fetchedResult.getOrNull()
        assertNotNull(fetchedFarmer)
        assertEquals("Suresh Patil", fetchedFarmer?.name)
        assertEquals(5.0, fetchedFarmer?.landAreaAcres ?: 0.0, 0.01)

        val districtListResult = repository.getFarmersByDistrict("Nashik")
        assertTrue(districtListResult.isSuccess)
        assertEquals(1, districtListResult.getOrDefault(emptyList()).size)

        // 3. UPDATE
        val updatedFarmer = fetchedFarmer!!.copy(
            landAreaAcres = 7.5,
            primaryCrops = listOf("Tomato", "Onion", "Grapes"),
            rating = 4.95
        )
        val updateResult = repository.updateFarmer(updatedFarmer)
        assertTrue(updateResult.isSuccess)

        val readAfterUpdate = repository.getFarmer("FARMER_TEST_01").getOrNull()
        assertEquals(7.5, readAfterUpdate?.landAreaAcres ?: 0.0, 0.01)
        assertTrue(readAfterUpdate?.primaryCrops?.contains("Grapes") == true)

        // 4. DELETE (Soft delete)
        val deleteResult = repository.deleteFarmer("FARMER_TEST_01", softDelete = true)
        assertTrue(deleteResult.isSuccess)

        val readAfterDelete = repository.getFarmer("FARMER_TEST_01").getOrNull()
        assertFalse(readAfterDelete?.isActive == true)
    }

    @Test
    fun testVendorCrudOperations() = runBlocking {
        // 1. CREATE
        val initialVendor = VendorProfile(
            id = "VENDOR_TEST_01",
            businessName = "Metro Cash & Carry",
            contactPerson = "Rahul Sharma",
            phone = "+91 99887 76655",
            email = "rahul@metro.in",
            vendorType = "SUPERMARKET_MART",
            city = "Pune",
            creditLimitRupees = 400000.0
        )
        val createResult = repository.createVendor(initialVendor)
        assertTrue(createResult.isSuccess)
        assertEquals("VENDOR_TEST_01", createResult.getOrNull())

        // 2. READ
        val fetchedResult = repository.getVendor("VENDOR_TEST_01")
        assertTrue(fetchedResult.isSuccess)
        val fetched = fetchedResult.getOrNull()
        assertNotNull(fetched)
        assertEquals("Metro Cash & Carry", fetched?.businessName)

        val supermarketList = repository.getVendorsByType("SUPERMARKET_MART").getOrDefault(emptyList())
        assertEquals(1, supermarketList.size)

        // 3. UPDATE
        val updatedVendor = fetched!!.copy(creditLimitRupees = 600000.0)
        val updateResult = repository.updateVendor(updatedVendor)
        assertTrue(updateResult.isSuccess)

        val readUpdated = repository.getVendor("VENDOR_TEST_01").getOrNull()
        assertEquals(600000.0, readUpdated?.creditLimitRupees ?: 0.0, 0.01)

        // 4. DELETE
        val deleteResult = repository.deleteVendor("VENDOR_TEST_01", softDelete = true)
        assertTrue(deleteResult.isSuccess)
        assertFalse(repository.getVendor("VENDOR_TEST_01").getOrNull()?.isActive == true)
    }

    @Test
    fun testMandiManagerCrudOperations() = runBlocking {
        // 1. CREATE
        val initialManager = MandiManagerProfile(
            id = "MANDI_MGR_TEST_01",
            managerName = "Arun Patil",
            phone = "+91 94220 11223",
            email = "arun@apmcbaramati.gov.in",
            mandiName = "Baramati APMC Yard",
            mandiCode = "MH-BAR-01",
            district = "Pune",
            licensedTradersCount = 120,
            dailyCapacityTons = 250.0
        )
        val createResult = repository.createMandiManager(initialManager)
        assertTrue(createResult.isSuccess)
        assertEquals("MANDI_MGR_TEST_01", createResult.getOrNull())

        // 2. READ
        val byId = repository.getMandiManager("MANDI_MGR_TEST_01").getOrNull()
        assertNotNull(byId)
        assertEquals("Baramati APMC Yard", byId?.mandiName)

        val byName = repository.getMandiManagerByMandiName("Baramati APMC Yard").getOrNull()
        assertNotNull(byName)
        assertEquals("Arun Patil", byName?.managerName)

        // 3. UPDATE
        val updated = byId!!.copy(dailyCapacityTons = 320.0, licensedTradersCount = 145)
        val updateResult = repository.updateMandiManager(updated)
        assertTrue(updateResult.isSuccess)

        val readUpdated = repository.getMandiManager("MANDI_MGR_TEST_01").getOrNull()
        assertEquals(320.0, readUpdated?.dailyCapacityTons ?: 0.0, 0.01)
        assertEquals(145, readUpdated?.licensedTradersCount)

        // 4. DELETE
        val deleteResult = repository.deleteMandiManager("MANDI_MGR_TEST_01", softDelete = true)
        assertTrue(deleteResult.isSuccess)
        assertFalse(repository.getMandiManager("MANDI_MGR_TEST_01").getOrNull()?.isActive == true)
    }

    @Test
    fun testSeedInitialActors() = runBlocking {
        repository.seedInitialActorsIfEmpty()

        val allFarmers = repository.getAllFarmers().getOrDefault(emptyList())
        val allVendors = repository.getAllVendors().getOrDefault(emptyList())
        val allManagers = repository.getAllMandiManagers().getOrDefault(emptyList())

        assertTrue("Farmers should be seeded", allFarmers.isNotEmpty())
        assertTrue("Vendors should be seeded", allVendors.isNotEmpty())
        assertTrue("Mandi Managers should be seeded", allManagers.isNotEmpty())

        assertTrue(allFarmers.any { it.name == "Ramesh Patil" })
        assertTrue(allVendors.any { it.businessName.contains("D-Mart") })
        assertTrue(allManagers.any { it.mandiName.contains("Pune APMC") })
    }

    @Test
    fun testFarmerProducePostCrudAndCalculations() = runBlocking {
        val producePost = FarmerProducePost(
            postId = "PRODUCE_TEST_TOMATO",
            farmerId = "FARMER_TEST_01",
            farmerName = "Suresh Patil",
            farmerPhone = "+91 98220 99999",
            farmLocation = "Niphad, Nashik",
            district = "Nashik",
            cropName = "Tomato",
            category = CropCategory.VEGETABLES,
            variety = "Himsona Hybrid",
            quantityKg = 2000.0,
            quantityUnit = "KG",
            askingPricePerKg = 25.0,
            mandiBenchmarkPricePerKg = 20.0,
            qualityGrade = QualityGrade.GRADE_A,
            storageCondition = StorageCondition.VENTILATED_CRATE,
            expectedFreshnessDays = 7,
            isOrganicOrResidueFree = true,
            readyForImmediatePickup = true,
            notes = "Harvested from automated drip plot."
        )

        // Verify domain calculations
        assertEquals(50000.0, producePost.totalEstimatedPayout, 0.01)
        assertTrue("Middleman savings should be positive", producePost.middlemanSavingsRupees > 0.0)

        // 1. CREATE
        val createResult = repository.createProducePost(producePost)
        assertTrue(createResult.isSuccess)
        assertEquals("PRODUCE_TEST_TOMATO", createResult.getOrNull())

        // 2. READ by ID
        val fetchedResult = repository.getProducePost("PRODUCE_TEST_TOMATO")
        assertTrue(fetchedResult.isSuccess)
        val fetched = fetchedResult.getOrNull()
        assertNotNull(fetched)
        assertEquals("Tomato", fetched?.cropName)
        assertEquals(2000.0, fetched?.quantityKg ?: 0.0, 0.01)
        assertEquals(25.0, fetched?.askingPricePerKg ?: 0.0, 0.01)

        // 3. READ by Farmer
        val farmerPosts = repository.getProducePostsByFarmer("FARMER_TEST_01").getOrDefault(emptyList())
        assertEquals(1, farmerPosts.size)
        assertEquals("Himsona Hybrid", farmerPosts[0].variety)

        // 4. MAP Serialization & Deserialization
        val mapped = producePost.toMap()
        assertEquals("PRODUCE_TEST_TOMATO", mapped["postId"])
        assertEquals(2000.0, mapped["quantityKg"])
        assertEquals(25.0, mapped["askingPricePerKg"])

        val reconstructed = FarmerProducePost.fromMap("PRODUCE_TEST_TOMATO", mapped)
        assertEquals(producePost.cropName, reconstructed.cropName)
        assertEquals(producePost.askingPricePerKg, reconstructed.askingPricePerKg, 0.01)
        assertEquals(producePost.qualityGrade, reconstructed.qualityGrade)

        // 5. UPDATE
        val updated = fetched!!.copy(askingPricePerKg = 26.5)
        val updateResult = repository.updateProducePost(updated)
        assertTrue(updateResult.isSuccess)
        assertEquals(26.5, repository.getProducePost("PRODUCE_TEST_TOMATO").getOrNull()?.askingPricePerKg ?: 0.0, 0.01)

        // 6. DELETE
        val deleteResult = repository.deleteProducePost("PRODUCE_TEST_TOMATO")
        assertTrue(deleteResult.isSuccess)
        assertEquals(null, repository.getProducePost("PRODUCE_TEST_TOMATO").getOrNull())
    }
}
