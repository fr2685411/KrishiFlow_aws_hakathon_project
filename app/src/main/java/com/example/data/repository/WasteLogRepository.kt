package com.example.data.repository

import com.example.data.local.WasteLogDao
import com.example.data.model.CropCategory
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import com.example.data.model.WasteReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Repository abstracting data access for agricultural surplus and waste logging.
 * Enables farmers to log, filter, and track quantities diverted for donation
 * or composting.
 */
class WasteLogRepository(
    private val wasteLogDao: WasteLogDao
) {
    /**
     * All agricultural surplus and waste logs ordered by date descending.
     */
    val allWasteLogs: Flow<List<WasteLog>> = wasteLogDao.getAllLogs()

    /**
     * Recent logs for summary dashboard displays.
     */
    fun getRecentLogs(limit: Int = 10): Flow<List<WasteLog>> = wasteLogDao.getRecentLogs(limit)

    /**
     * Logs filtered by destination (e.g. DONATION or COMPOSTING).
     */
    fun getLogsByDestination(destination: WasteDestination): Flow<List<WasteLog>> =
        wasteLogDao.getLogsByDestination(destination)

    /**
     * Logs filtered by crop name.
     */
    fun getLogsByCrop(cropName: String): Flow<List<WasteLog>> =
        wasteLogDao.getLogsByCrop(cropName)

    /**
     * Total quantity (kg) diverted for donation to charities and food banks.
     */
    val totalDonatedKg: Flow<Double> = wasteLogDao.getTotalDonatedKg()

    /**
     * Total quantity (kg) diverted for composting into organic fertilizer.
     */
    val totalCompostedKg: Flow<Double> = wasteLogDao.getTotalCompostedKg()

    /**
     * Total quantity (kg) of all logged agricultural surplus and waste.
     */
    val totalWasteLoggedKg: Flow<Double> = wasteLogDao.getTotalWasteLoggedKg()

    /**
     * Total count of waste logs.
     */
    val totalCount: Flow<Int> = wasteLogDao.getLogCount()

    /**
     * Insert a new waste/surplus log.
     */
    suspend fun insertLog(log: WasteLog): Long = wasteLogDao.insertLog(log)

    /**
     * Convenience method to log daily agricultural surplus or waste.
     */
    suspend fun logSurplusOrWaste(
        cropName: String,
        category: CropCategory,
        quantityKg: Double,
        destination: WasteDestination,
        reason: WasteReason = WasteReason.MARKET_SURPLUS,
        destinationPartner: String = "",
        estimatedValueRupees: Double = 0.0,
        notes: String = "",
        logDateTimestamp: Long = System.currentTimeMillis()
    ): Long {
        val log = WasteLog(
            cropName = cropName.trim(),
            category = category,
            quantityKg = quantityKg,
            destination = destination,
            reason = reason,
            destinationPartner = destinationPartner.trim(),
            estimatedValueRupees = estimatedValueRupees,
            notes = notes.trim(),
            logDateTimestamp = logDateTimestamp
        )
        return wasteLogDao.insertLog(log)
    }

    /**
     * Update an existing waste log.
     */
    suspend fun updateLog(log: WasteLog) = wasteLogDao.updateLog(log)

    /**
     * Delete a log by id.
     */
    suspend fun deleteLogById(id: Long) = wasteLogDao.deleteLogById(id)

    /**
     * Delete a waste log entity.
     */
    suspend fun deleteLog(log: WasteLog) = wasteLogDao.deleteLog(log)

    /**
     * Seed initial realistic sample surplus and waste records if empty.
     */
    suspend fun seedSampleWasteLogsIfEmpty() {
        val existing = allWasteLogs.firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            val sampleLogs = listOf(
                WasteLog(
                    cropName = "Tomato (Hybrid)",
                    category = CropCategory.VEGETABLES,
                    quantityKg = 120.0,
                    destination = WasteDestination.DONATION,
                    reason = WasteReason.MARKET_SURPLUS,
                    destinationPartner = "Roti Bank & Akshaya Patra Kitchen",
                    estimatedValueRupees = 2400.0,
                    notes = "Ripe harvest surplus donated to community meal kitchen instead of mandi discard",
                    logDateTimestamp = now - 1 * dayMs
                ),
                WasteLog(
                    cropName = "Cabbage & Cauliflower Leaves",
                    category = CropCategory.VEGETABLES,
                    quantityKg = 85.0,
                    destination = WasteDestination.COMPOSTING,
                    reason = WasteReason.HARVEST_DAMAGE,
                    destinationPartner = "Farm Aerobic Vermicompost Unit",
                    estimatedValueRupees = 425.0,
                    notes = "Outer leaves and field trimmings shredded for organic bio-fertilizer bed",
                    logDateTimestamp = now - 2 * dayMs
                ),
                WasteLog(
                    cropName = "Alphonso Mango",
                    category = CropCategory.FRUITS,
                    quantityKg = 60.0,
                    destination = WasteDestination.DONATION,
                    reason = WasteReason.OVER_RIPE,
                    destinationPartner = "Navjeevan Children's Home",
                    estimatedValueRupees = 4800.0,
                    notes = "Soft table-grade ripe mangoes expedited to local children orphanage",
                    logDateTimestamp = now - 4 * dayMs
                ),
                WasteLog(
                    cropName = "Red Onion Trimmings",
                    category = CropCategory.VEGETABLES,
                    quantityKg = 110.0,
                    destination = WasteDestination.COMPOSTING,
                    reason = WasteReason.STORAGE_LOSS,
                    destinationPartner = "Gram Panchayat Community Compost Pit",
                    estimatedValueRupees = 1320.0,
                    notes = "Sprouted outer layers sorted during grading, transferred to compost",
                    logDateTimestamp = now - 5 * dayMs
                ),
                WasteLog(
                    cropName = "Green Chilli (Grade B)",
                    category = CropCategory.VEGETABLES,
                    quantityKg = 45.0,
                    destination = WasteDestination.DONATION,
                    reason = WasteReason.COSMETIC_BLEMISH,
                    destinationPartner = "Gurudwara Langar Community Kitchen",
                    estimatedValueRupees = 1800.0,
                    notes = "Slightly bent chillies rejected by export mandi, perfect for langar cooking",
                    logDateTimestamp = now - 7 * dayMs
                ),
                WasteLog(
                    cropName = "Sweet Corn Stalks",
                    category = CropCategory.GRAINS,
                    quantityKg = 250.0,
                    destination = WasteDestination.ANIMAL_FEED,
                    reason = WasteReason.HARVEST_DAMAGE,
                    destinationPartner = "Shri Krishna Gaushala",
                    estimatedValueRupees = 1250.0,
                    notes = "Fresh green stalks chopped for dairy cow fodder",
                    logDateTimestamp = now - 8 * dayMs
                )
            )
            wasteLogDao.insertAll(sampleLogs)
        }
    }
}
