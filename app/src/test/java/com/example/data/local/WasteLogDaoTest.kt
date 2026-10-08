package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CropCategory
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import com.example.data.model.WasteReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WasteLogDaoTest {

    private lateinit var database: KrishiDatabase
    private lateinit var wasteLogDao: WasteLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KrishiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wasteLogDao = database.wasteLogDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveWasteLogs() = runTest {
        val donationLog = WasteLog(
            cropName = "Tomato",
            category = CropCategory.VEGETABLES,
            quantityKg = 75.0,
            destination = WasteDestination.DONATION,
            reason = WasteReason.MARKET_SURPLUS,
            destinationPartner = "Roti Bank Foundation",
            estimatedValueRupees = 1500.0,
            notes = "Fresh surplus donated to local community kitchen"
        )
        val insertedId = wasteLogDao.insertLog(donationLog)
        assertTrue(insertedId > 0)

        val compostingLog = WasteLog(
            cropName = "Cabbage",
            category = CropCategory.VEGETABLES,
            quantityKg = 40.0,
            destination = WasteDestination.COMPOSTING,
            reason = WasteReason.HARVEST_DAMAGE,
            destinationPartner = "Farm Vermicompost Pit",
            estimatedValueRupees = 200.0,
            notes = "Outer leaves trimmed and composted"
        )
        wasteLogDao.insertLog(compostingLog)

        val allLogs = wasteLogDao.getAllLogs().first()
        assertEquals(2, allLogs.size)

        // Verify donations filtering
        val donations = wasteLogDao.getLogsByDestination(WasteDestination.DONATION).first()
        assertEquals(1, donations.size)
        assertEquals("Tomato", donations[0].cropName)
        assertEquals(75.0, donations[0].quantityKg, 0.01)
        assertEquals("Roti Bank Foundation", donations[0].destinationPartner)

        // Verify composting filtering
        val composting = wasteLogDao.getLogsByDestination(WasteDestination.COMPOSTING).first()
        assertEquals(1, composting.size)
        assertEquals("Cabbage", composting[0].cropName)
        assertEquals(40.0, composting[0].quantityKg, 0.01)

        // Verify aggregated totals
        val totalDonated = wasteLogDao.getTotalDonatedKg().first()
        assertEquals(75.0, totalDonated, 0.01)

        val totalComposted = wasteLogDao.getTotalCompostedKg().first()
        assertEquals(40.0, totalComposted, 0.01)

        val totalWaste = wasteLogDao.getTotalWasteLoggedKg().first()
        assertEquals(115.0, totalWaste, 0.01)
    }

    @Test
    fun testDeleteWasteLog() = runTest {
        val log = WasteLog(
            cropName = "Alphonso Mango",
            category = CropCategory.FRUITS,
            quantityKg = 25.0,
            destination = WasteDestination.DONATION,
            reason = WasteReason.OVER_RIPE,
            destinationPartner = "Orphanage"
        )
        val id = wasteLogDao.insertLog(log)
        val countBefore = wasteLogDao.getLogCount().first()
        assertEquals(1, countBefore)

        wasteLogDao.deleteLogById(id)
        val countAfter = wasteLogDao.getLogCount().first()
        assertEquals(0, countAfter)
    }

    @Test
    fun testCo2DivertedCalculation() {
        val log = WasteLog(
            cropName = "Tomato",
            quantityKg = 100.0,
            destination = WasteDestination.DONATION
        )
        // 100 kg * 1.9 kg CO2e saved
        assertEquals(190.0, log.co2DivertedKg, 0.01)
        assertEquals("190.0 kg CO₂e saved", log.formattedCo2Diverted)
    }
}
