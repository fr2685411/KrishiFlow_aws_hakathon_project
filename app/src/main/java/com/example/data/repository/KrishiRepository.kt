package com.example.data.repository

import android.util.Log
import com.example.data.local.BuyerDemandDao
import com.example.data.local.HarvestHistoryLogDao
import com.example.data.local.KrishiDatabase
import com.example.data.local.MandiRateDao
import com.example.data.local.OfflineInventoryQueueDao
import com.example.data.local.OrderTransactionDao
import com.example.data.local.ProduceBatchDao
import com.example.data.local.SpoilagePreventionDao
import com.example.data.model.BatchStatus
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.BuyerType
import com.example.data.model.CropCategory
import com.example.data.model.HarvestHistoryLogEntity
import com.example.data.model.MandiRateEntity
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.OrderStatus
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.QualityGrade
import com.example.data.model.SpoilagePreventionRecord
import com.example.data.model.StorageCondition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class KrishiRepository(
    private val database: KrishiDatabase
) {
    private val batchDao: ProduceBatchDao = database.produceBatchDao()
    private val demandDao: BuyerDemandDao = database.buyerDemandDao()
    private val orderDao: OrderTransactionDao = database.orderTransactionDao()
    private val mandiDao: MandiRateDao = database.mandiRateDao()
    private val spoilageDao: SpoilagePreventionDao = database.spoilagePreventionDao()
    private val harvestLogDao: HarvestHistoryLogDao = database.harvestHistoryLogDao()
    private val queueDao: OfflineInventoryQueueDao = database.offlineInventoryQueueDao()

    val allBatches: Flow<List<ProduceBatchEntity>> = batchDao.getAllBatches()
    val activeBatches: Flow<List<ProduceBatchEntity>> = batchDao.getActiveBatches()
    val allDemands: Flow<List<BuyerDemandEntity>> = demandDao.getAllDemands()
    val openDemands: Flow<List<BuyerDemandEntity>> = demandDao.getOpenDemands()
    val allOrders: Flow<List<OrderTransactionEntity>> = orderDao.getAllOrders()
    val allMandiRates: Flow<List<MandiRateEntity>> = mandiDao.getAllRates()
    val spoilageRecords: Flow<List<SpoilagePreventionRecord>> = spoilageDao.getAllRecords()
    val allHarvestLogs: Flow<List<HarvestHistoryLogEntity>> = harvestLogDao.getAllLogs()
    val allQueuedInventory: Flow<List<OfflineInventoryQueueEntity>> = queueDao.getAllQueuedItems()
    val pendingQueueCount: Flow<Int> = queueDao.getPendingSyncCountFlow()

    suspend fun seedInitialDataIfEmpty() {
        try {
            val existingBatches = allBatches.firstOrNull()
            if (existingBatches.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 3600 * 1000L

            val initialBatches = listOf(
                ProduceBatchEntity(
                    batchCode = "KF-TOM-101",
                    cropName = "Hybrid Tomato",
                    category = CropCategory.VEGETABLES,
                    variety = "Abhinav / S-312",
                    qualityGrade = QualityGrade.GRADE_A,
                    farmerName = "Ramesh Patil",
                    farmerPhone = "+91 98220 14829",
                    farmLocation = "Niphad, Nashik",
                    distanceKm = 42,
                    totalQuantityKg = 800.0,
                    availableQuantityKg = 500.0,
                    reservedQuantityKg = 300.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 18.0,
                    hubProcuredPricePerKg = 20.0,
                    deliveredPricePerKg = 26.0,
                    discountPercent = 0,
                    harvestTimestamp = now - (1 * dayMs),
                    expiryTimestamp = now + (3 * dayMs),
                    expectedFreshnessDays = 4,
                    storageCondition = StorageCondition.VENTILATED_CRATE,
                    status = BatchStatus.PROCURED_IN_HUB,
                    notes = "Plucked early morning, bright red, firm texture, ideal for supermarkets."
                ),
                ProduceBatchEntity(
                    batchCode = "KF-CAP-102",
                    cropName = "Green Capsicum",
                    category = CropCategory.VEGETABLES,
                    variety = "Indra Polyhouse",
                    qualityGrade = QualityGrade.GRADE_B,
                    farmerName = "Vitthal Jagtap",
                    farmerPhone = "+91 94231 88920",
                    farmLocation = "Manchar, Pune",
                    distanceKm = 28,
                    totalQuantityKg = 400.0,
                    availableQuantityKg = 350.0,
                    reservedQuantityKg = 50.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 36.0,
                    hubProcuredPricePerKg = 39.0,
                    deliveredPricePerKg = 48.0,
                    discountPercent = 20, // 20% discount applied for surplus clearance
                    harvestTimestamp = now - (3 * dayMs),
                    expiryTimestamp = now + (18 * 3600 * 1000L), // 18 hours left
                    expectedFreshnessDays = 4,
                    storageCondition = StorageCondition.COLD_STORAGE,
                    status = BatchStatus.PROCURED_IN_HUB,
                    notes = "Surplus priority queue: Approaching optimal freshness window."
                ),
                ProduceBatchEntity(
                    batchCode = "KF-ONI-103",
                    cropName = "Red Onion",
                    category = CropCategory.VEGETABLES,
                    variety = "Nashik Garwa",
                    qualityGrade = QualityGrade.GRADE_A,
                    farmerName = "Suresh Shinde",
                    farmerPhone = "+91 97654 32109",
                    farmLocation = "Lasalgaon, Nashik",
                    distanceKm = 65,
                    totalQuantityKg = 3000.0,
                    availableQuantityKg = 2400.0,
                    reservedQuantityKg = 600.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 24.0,
                    hubProcuredPricePerKg = 26.5,
                    deliveredPricePerKg = 33.0,
                    discountPercent = 0,
                    harvestTimestamp = now - (7 * dayMs),
                    expiryTimestamp = now + (60 * dayMs),
                    expectedFreshnessDays = 75,
                    storageCondition = StorageCondition.AMBIENT_SHED,
                    status = BatchStatus.PROCURED_IN_HUB,
                    notes = "Dry cured, tight skin, high pungency, long shelf life."
                ),
                ProduceBatchEntity(
                    batchCode = "KF-WHT-104",
                    cropName = "Sharbati Wheat",
                    category = CropCategory.GRAINS,
                    variety = "MP Golden C-306",
                    qualityGrade = QualityGrade.GRADE_A,
                    farmerName = "Balram Yadav",
                    farmerPhone = "+91 98932 77412",
                    farmLocation = "Sehore, Madhya Pradesh",
                    distanceKm = 180,
                    totalQuantityKg = 6000.0,
                    availableQuantityKg = 4500.0,
                    reservedQuantityKg = 1500.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 33.0,
                    hubProcuredPricePerKg = 35.0,
                    deliveredPricePerKg = 42.0,
                    discountPercent = 0,
                    harvestTimestamp = now - (15 * dayMs),
                    expiryTimestamp = now + (180 * dayMs),
                    expectedFreshnessDays = 210,
                    storageCondition = StorageCondition.AMBIENT_SHED,
                    status = BatchStatus.PROCURED_IN_HUB,
                    notes = "Moisture < 11%, machine cleaned, zero stones, premium roti quality."
                ),
                ProduceBatchEntity(
                    batchCode = "KF-POT-105",
                    cropName = "Jyoti Potato",
                    category = CropCategory.VEGETABLES,
                    variety = "Kufri Jyoti",
                    qualityGrade = QualityGrade.GRADE_B,
                    farmerName = "Kailash Verma",
                    farmerPhone = "+91 94066 51234",
                    farmLocation = "Indore Rural",
                    distanceKm = 140,
                    totalQuantityKg = 2500.0,
                    availableQuantityKg = 2500.0,
                    reservedQuantityKg = 0.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 16.0,
                    hubProcuredPricePerKg = 18.0,
                    deliveredPricePerKg = 24.0,
                    discountPercent = 0,
                    harvestTimestamp = now - (5 * dayMs),
                    expiryTimestamp = now + (40 * dayMs),
                    expectedFreshnessDays = 50,
                    storageCondition = StorageCondition.COLD_STORAGE,
                    status = BatchStatus.LISTED,
                    notes = "Freshly harvested, awaiting KrishiFlow collection van pickup."
                ),
                ProduceBatchEntity(
                    batchCode = "KF-COR-106",
                    cropName = "Fresh Coriander & Herbs",
                    category = CropCategory.VEGETABLES,
                    variety = "Desi Sugandhi",
                    qualityGrade = QualityGrade.GRADE_A,
                    farmerName = "Sunita Bai",
                    farmerPhone = "+91 99234 11098",
                    farmLocation = "Otur, Junnar",
                    distanceKm = 35,
                    totalQuantityKg = 200.0,
                    availableQuantityKg = 120.0,
                    reservedQuantityKg = 80.0,
                    soldQuantityKg = 0.0,
                    farmerAskingPricePerKg = 30.0,
                    hubProcuredPricePerKg = 33.0,
                    deliveredPricePerKg = 46.0,
                    discountPercent = 25,
                    harvestTimestamp = now - (2 * dayMs),
                    expiryTimestamp = now + (12 * 3600 * 1000L), // 12 hours left
                    expectedFreshnessDays = 3,
                    storageCondition = StorageCondition.VENTILATED_CRATE,
                    status = BatchStatus.PROCURED_IN_HUB,
                    notes = "Highly perishable: Flash discounted for quick delivery to nearby salad vendors."
                )
            )
            batchDao.insertAll(initialBatches)

            val initialDemands = listOf(
                BuyerDemandEntity(
                    demandCode = "RFQ-DMR-01",
                    buyerName = "Dmart Regional DC",
                    buyerType = BuyerType.SUPERMARKET_MART,
                    buyerPhone = "+91 99887 76655",
                    destinationLocation = "Chakan Warehouse Hub, Pune",
                    cropRequested = "Hybrid Tomato",
                    minGrade = QualityGrade.GRADE_A,
                    requiredQuantityKg = 500.0,
                    maxOfferedPricePerKg = 28.0,
                    status = "MATCHED"
                ),
                BuyerDemandEntity(
                    demandCode = "RFQ-SBB-02",
                    buyerName = "Sharma Subji Bhandar",
                    buyerType = BuyerType.LOCAL_VENDOR,
                    buyerPhone = "+91 98112 33445",
                    destinationLocation = "Hadapsar Mandi, Pune",
                    cropRequested = "Red Onion",
                    minGrade = QualityGrade.GRADE_B,
                    requiredQuantityKg = 300.0,
                    maxOfferedPricePerKg = 34.0,
                    status = "OPEN"
                ),
                BuyerDemandEntity(
                    demandCode = "RFQ-JIO-03",
                    buyerName = "JioMart Fresh Distribution",
                    buyerType = BuyerType.SUPERMARKET_MART,
                    buyerPhone = "+91 97001 23456",
                    destinationLocation = "Navi Mumbai Hub",
                    cropRequested = "Sharbati Wheat",
                    minGrade = QualityGrade.GRADE_A,
                    requiredQuantityKg = 1500.0,
                    maxOfferedPricePerKg = 43.0,
                    status = "MATCHED"
                ),
                BuyerDemandEntity(
                    demandCode = "RFQ-APMC-04",
                    buyerName = "Gultekdi Wholesaler Union",
                    buyerType = BuyerType.MANDI_WHOLESALER,
                    buyerPhone = "+91 98900 12389",
                    destinationLocation = "Gultekdi Market Yard",
                    cropRequested = "Green Capsicum",
                    minGrade = QualityGrade.GRADE_B,
                    requiredQuantityKg = 250.0,
                    maxOfferedPricePerKg = 42.0,
                    status = "OPEN"
                ),
                BuyerDemandEntity(
                    demandCode = "RFQ-VND-05",
                    buyerName = "Aman Retail Greens",
                    buyerType = BuyerType.LOCAL_VENDOR,
                    buyerPhone = "+91 98555 44332",
                    destinationLocation = "Kothrud Subji Line",
                    cropRequested = "Fresh Coriander & Herbs",
                    minGrade = QualityGrade.GRADE_A,
                    requiredQuantityKg = 100.0,
                    maxOfferedPricePerKg = 38.0,
                    status = "MATCHED"
                )
            )
            demandDao.insertAll(initialDemands)

            val initialOrders = listOf(
                OrderTransactionEntity(
                    orderCode = "ORD-2026-081",
                    batchId = 1,
                    buyerName = "Dmart Regional DC",
                    buyerType = BuyerType.SUPERMARKET_MART,
                    farmerName = "Ramesh Patil",
                    cropName = "Hybrid Tomato",
                    quantityKg = 300.0,
                    farmerPayoutTotal = 6000.0,
                    transportCostTotal = 750.0,
                    buyerPaymentTotal = 7800.0,
                    grossMarginTotal = 1050.0,
                    status = OrderStatus.OUT_FOR_DELIVERY,
                    destinationAddress = "Chakan Distribution Center, Gate 3, Pune",
                    createdTimestamp = now - (6 * 3600 * 1000L)
                ),
                OrderTransactionEntity(
                    orderCode = "ORD-2026-080",
                    batchId = 3,
                    buyerName = "Reliable Greens Mart",
                    buyerType = BuyerType.SUPERMARKET_MART,
                    farmerName = "Suresh Shinde",
                    cropName = "Red Onion",
                    quantityKg = 600.0,
                    farmerPayoutTotal = 15900.0,
                    transportCostTotal = 1400.0,
                    buyerPaymentTotal = 19800.0,
                    grossMarginTotal = 2500.0,
                    status = OrderStatus.DELIVERED_PAID,
                    destinationAddress = "Viman Nagar Supermarket, Pune",
                    createdTimestamp = now - (24 * 3600 * 1000L),
                    deliveredTimestamp = now - (8 * 3600 * 1000L)
                ),
                OrderTransactionEntity(
                    orderCode = "ORD-2026-079",
                    batchId = 6,
                    buyerName = "Aman Retail Greens",
                    buyerType = BuyerType.LOCAL_VENDOR,
                    farmerName = "Sunita Bai",
                    cropName = "Fresh Coriander & Herbs",
                    quantityKg = 80.0,
                    farmerPayoutTotal = 2640.0,
                    transportCostTotal = 250.0,
                    buyerPaymentTotal = 2760.0, // Discounted surplus sale
                    grossMarginTotal = -130.0, // Rescued at cost to prevent total food spoilage!
                    status = OrderStatus.DELIVERED_PAID,
                    destinationAddress = "Shop 14, Kothrud Market Line",
                    createdTimestamp = now - (14 * 3600 * 1000L),
                    deliveredTimestamp = now - (2 * 3600 * 1000L)
                )
            )
            orderDao.insertAll(initialOrders)

            val initialMandiRates = listOf(
                MandiRateEntity(
                    mandiName = "Vashi APMC",
                    state = "Maharashtra",
                    commodity = "Tomato (Hybrid)",
                    variety = "Regular Red",
                    modalPricePerKg = 21.0,
                    minPricePerKg = 16.0,
                    maxPricePerKg = 25.0,
                    middlemanCommissionCutPercent = 12.0,
                    krishiFlowNetFarmerRate = 20.0, // Direct payout with 0 broker cut
                    priceTrend = "UP",
                    arrivalTonnes = 145.0
                ),
                MandiRateEntity(
                    mandiName = "Lasalgaon Mandi",
                    state = "Maharashtra",
                    commodity = "Red Onion",
                    variety = "Garwa",
                    modalPricePerKg = 24.5,
                    minPricePerKg = 20.0,
                    maxPricePerKg = 29.0,
                    middlemanCommissionCutPercent = 10.0,
                    krishiFlowNetFarmerRate = 26.5,
                    priceTrend = "STABLE",
                    arrivalTonnes = 420.0
                ),
                MandiRateEntity(
                    mandiName = "Azadpur APMC",
                    state = "Delhi NCR",
                    commodity = "Jyoti Potato",
                    variety = "Cold Storage Clean",
                    modalPricePerKg = 17.5,
                    minPricePerKg = 14.0,
                    maxPricePerKg = 21.0,
                    middlemanCommissionCutPercent = 11.5,
                    krishiFlowNetFarmerRate = 18.5,
                    priceTrend = "DOWN",
                    arrivalTonnes = 580.0
                ),
                MandiRateEntity(
                    mandiName = "Sehore Krishi Mandi",
                    state = "Madhya Pradesh",
                    commodity = "Sharbati Wheat",
                    variety = "C-306 Mill Grade",
                    modalPricePerKg = 33.0,
                    minPricePerKg = 30.0,
                    maxPricePerKg = 36.5,
                    middlemanCommissionCutPercent = 8.5,
                    krishiFlowNetFarmerRate = 35.0,
                    priceTrend = "UP",
                    arrivalTonnes = 260.0
                ),
                MandiRateEntity(
                    mandiName = "Enumamula Market",
                    state = "Telangana",
                    commodity = "Green Chilli",
                    variety = "Guntur Special",
                    modalPricePerKg = 44.0,
                    minPricePerKg = 38.0,
                    maxPricePerKg = 52.0,
                    middlemanCommissionCutPercent = 14.0,
                    krishiFlowNetFarmerRate = 47.0,
                    priceTrend = "UP",
                    arrivalTonnes = 85.0
                )
            )
            mandiDao.insertAll(initialMandiRates)

            val initialSpoilage = listOf(
                SpoilagePreventionRecord(
                    batchCode = "KF-COR-106",
                    cropName = "Fresh Coriander & Herbs",
                    quantityKg = 80.0,
                    originalSpoilageRiskPercent = 85,
                    discountAppliedPercent = 25,
                    buyerName = "Aman Retail Greens",
                    rescuedValueRupees = 2760.0,
                    co2SavedKg = 152.0,
                    timestamp = now - (2 * 3600 * 1000L)
                ),
                SpoilagePreventionRecord(
                    batchCode = "KF-CAP-095",
                    cropName = "Green Capsicum",
                    quantityKg = 220.0,
                    originalSpoilageRiskPercent = 70,
                    discountAppliedPercent = 18,
                    buyerName = "Joshi Caterers Pune",
                    rescuedValueRupees = 8580.0,
                    co2SavedKg = 418.0,
                    timestamp = now - (48 * 3600 * 1000L)
                )
            )
            spoilageDao.insertAll(initialSpoilage)
        }

        val existingLogs = harvestLogDao.getAllLogs().firstOrNull()
        if (existingLogs.isNullOrEmpty()) {
            val dayMs = 24 * 3600 * 1000L
            val now = System.currentTimeMillis()
            val sampleLogs = listOf(
                HarvestHistoryLogEntity(
                    cropName = "Tomato",
                    category = CropCategory.VEGETABLES,
                    variety = "Hybrid Abhinav",
                    harvestSeason = "Kharif 2025",
                    plantedAreaAcres = 2.0,
                    harvestQuantityKg = 18400.0,
                    harvestDateTimestamp = now - (90 * dayMs),
                    saleDateTimestamp = now - (87 * dayMs),
                    quantitySoldKg = 18000.0,
                    soldRatePerKg = 24.5,
                    totalRevenueRupees = 441000.0,
                    buyerOrMandiName = "Pimpalgaon Baswant APMC",
                    storageLossKg = 400.0,
                    cycleDurationDays = 75,
                    notes = "Excellent early picking rate. Zero disease incidence with drip fertigation."
                ),
                HarvestHistoryLogEntity(
                    cropName = "Tomato",
                    category = CropCategory.VEGETABLES,
                    variety = "Hybrid Vaishnavi",
                    harvestSeason = "Rabi 2024-25",
                    plantedAreaAcres = 1.5,
                    harvestQuantityKg = 14200.0,
                    harvestDateTimestamp = now - (240 * dayMs),
                    saleDateTimestamp = now - (238 * dayMs),
                    quantitySoldKg = 13900.0,
                    soldRatePerKg = 28.0,
                    totalRevenueRupees = 389200.0,
                    buyerOrMandiName = "Direct Supermarket Mart",
                    storageLossKg = 300.0,
                    cycleDurationDays = 80,
                    notes = "Winter crop yielded firm, deep red fruits. Supermarket procurement gave 15% premium."
                ),
                HarvestHistoryLogEntity(
                    cropName = "Red Onion",
                    category = CropCategory.VEGETABLES,
                    variety = "Nashik Garwa Red",
                    harvestSeason = "Rabi 2025",
                    plantedAreaAcres = 3.0,
                    harvestQuantityKg = 26500.0,
                    harvestDateTimestamp = now - (150 * dayMs),
                    saleDateTimestamp = now - (135 * dayMs),
                    quantitySoldKg = 25800.0,
                    soldRatePerKg = 26.0,
                    totalRevenueRupees = 670800.0,
                    buyerOrMandiName = "Lasalgaon Mandi",
                    storageLossKg = 700.0,
                    cycleDurationDays = 115,
                    notes = "Stored in well-ventilated dry shed for 2 weeks prior to sale for tight skin cure."
                ),
                HarvestHistoryLogEntity(
                    cropName = "Red Onion",
                    category = CropCategory.VEGETABLES,
                    variety = "Fursungi Light Red",
                    harvestSeason = "Late Kharif 2024",
                    plantedAreaAcres = 2.0,
                    harvestQuantityKg = 16800.0,
                    harvestDateTimestamp = now - (380 * dayMs),
                    saleDateTimestamp = now - (370 * dayMs),
                    quantitySoldKg = 16200.0,
                    soldRatePerKg = 22.0,
                    totalRevenueRupees = 356400.0,
                    buyerOrMandiName = "Dindori Local Mandi",
                    storageLossKg = 600.0,
                    cycleDurationDays = 105,
                    notes = "Monsoon humidity required prompt post-harvest drying."
                ),
                HarvestHistoryLogEntity(
                    cropName = "Sharbati Wheat",
                    category = CropCategory.GRAINS,
                    variety = "C-306 Mill Grade",
                    harvestSeason = "Rabi 2025-26",
                    plantedAreaAcres = 4.0,
                    harvestQuantityKg = 9200.0,
                    harvestDateTimestamp = now - (200 * dayMs),
                    saleDateTimestamp = now - (195 * dayMs),
                    quantitySoldKg = 9100.0,
                    soldRatePerKg = 34.0,
                    totalRevenueRupees = 309400.0,
                    buyerOrMandiName = "Sehore Krishi Mandi",
                    storageLossKg = 100.0,
                    cycleDurationDays = 125,
                    notes = "Sharbati golden grain fetched top price above APMC benchmark."
                ),
                HarvestHistoryLogEntity(
                    cropName = "Soybean",
                    category = CropCategory.OILSEEDS,
                    variety = "JS-335 Certified",
                    harvestSeason = "Kharif 2025",
                    plantedAreaAcres = 2.5,
                    harvestQuantityKg = 3600.0,
                    harvestDateTimestamp = now - (120 * dayMs),
                    saleDateTimestamp = now - (116 * dayMs),
                    quantitySoldKg = 3550.0,
                    soldRatePerKg = 48.0,
                    totalRevenueRupees = 170400.0,
                    buyerOrMandiName = "Solapur Oilseed Hub",
                    storageLossKg = 50.0,
                    cycleDurationDays = 95,
                    notes = "Good pod filling; rhizobium inoculation increased nodulation."
                )
            )
            harvestLogDao.insertAllLogs(sampleLogs)
        }
    } catch (e: Exception) {
        Log.e("KrishiRepository", "Error seeding initial database data: ${e.message}", e)
    }
}

    suspend fun insertBatch(batch: ProduceBatchEntity): Long {
        return batchDao.insertBatch(batch)
    }

    suspend fun updateBatch(batch: ProduceBatchEntity) {
        batchDao.updateBatch(batch)
    }

    suspend fun updateBatchStatus(batchId: Long, newStatus: BatchStatus) {
        val batch = batchDao.getBatchByIdDirect(batchId) ?: return
        batchDao.updateBatch(batch.copy(status = newStatus))
    }

    suspend fun applySurplusDiscount(batchId: Long, discountPercent: Int) {
        val batch = batchDao.getBatchByIdDirect(batchId) ?: return
        batchDao.updateBatch(batch.copy(discountPercent = discountPercent))
    }

    suspend fun insertDemand(demand: BuyerDemandEntity): Long {
        return demandDao.insertDemand(demand)
    }

    suspend fun insertHarvestLog(log: HarvestHistoryLogEntity): Long {
        return harvestLogDao.insertLog(log)
    }

    suspend fun deleteHarvestLog(id: Long) {
        harvestLogDao.deleteLogById(id)
    }

    suspend fun updateDemand(demand: BuyerDemandEntity) {
        demandDao.updateDemand(demand)
    }

    suspend fun matchDemandWithBatch(
        demand: BuyerDemandEntity,
        batch: ProduceBatchEntity,
        quantityKg: Double,
        destinationAddress: String
    ): Boolean {
        val allocatableQty = quantityKg.coerceAtMost(batch.availableQuantityKg)
        if (allocatableQty <= 0) return false

        val updatedAvailable = batch.availableQuantityKg - allocatableQty
        val updatedReserved = batch.reservedQuantityKg + allocatableQty
        batchDao.updateBatch(
            batch.copy(
                availableQuantityKg = updatedAvailable,
                reservedQuantityKg = updatedReserved,
                status = if (updatedAvailable <= 0) BatchStatus.ALLOCATED else batch.status
            )
        )

        val farmerPayout = allocatableQty * batch.hubProcuredPricePerKg
        val transportCost = (batch.distanceKm * 15.0).coerceAtLeast(350.0)
        val buyerPayment = allocatableQty * batch.effectivePricePerKg
        val margin = buyerPayment - farmerPayout - transportCost

        val newOrder = OrderTransactionEntity(
            orderCode = "ORD-${System.currentTimeMillis() % 100000}",
            batchId = batch.id,
            buyerName = demand.buyerName,
            buyerType = demand.buyerType,
            farmerName = batch.farmerName,
            cropName = batch.cropName,
            quantityKg = allocatableQty,
            farmerPayoutTotal = farmerPayout,
            transportCostTotal = transportCost,
            buyerPaymentTotal = buyerPayment,
            grossMarginTotal = margin,
            status = OrderStatus.RESERVED,
            destinationAddress = destinationAddress
        )
        orderDao.insertOrder(newOrder)

        val updatedDemandStatus = if (demand.requiredQuantityKg <= allocatableQty) "FULFILLED" else "MATCHED"
        demandDao.updateDemand(demand.copy(status = updatedDemandStatus))
        return true
    }

    suspend fun createDirectOrder(
        batch: ProduceBatchEntity,
        buyerName: String,
        buyerType: BuyerType,
        quantityKg: Double,
        destinationAddress: String
    ): Boolean {
        val allocatableQty = quantityKg.coerceAtMost(batch.availableQuantityKg)
        if (allocatableQty <= 0) return false

        val updatedAvailable = batch.availableQuantityKg - allocatableQty
        val updatedReserved = batch.reservedQuantityKg + allocatableQty
        batchDao.updateBatch(
            batch.copy(
                availableQuantityKg = updatedAvailable,
                reservedQuantityKg = updatedReserved,
                status = if (updatedAvailable <= 0) BatchStatus.ALLOCATED else batch.status
            )
        )

        val farmerPayout = allocatableQty * batch.hubProcuredPricePerKg
        val transportCost = (batch.distanceKm * 14.0).coerceAtLeast(300.0)
        val buyerPayment = allocatableQty * batch.effectivePricePerKg
        val margin = buyerPayment - farmerPayout - transportCost

        val newOrder = OrderTransactionEntity(
            orderCode = "ORD-${System.currentTimeMillis() % 100000}",
            batchId = batch.id,
            buyerName = buyerName,
            buyerType = buyerType,
            farmerName = batch.farmerName,
            cropName = batch.cropName,
            quantityKg = allocatableQty,
            farmerPayoutTotal = farmerPayout,
            transportCostTotal = transportCost,
            buyerPaymentTotal = buyerPayment,
            grossMarginTotal = margin,
            status = OrderStatus.RESERVED,
            destinationAddress = destinationAddress
        )
        orderDao.insertOrder(newOrder)

        // If this was a surplus/discounted batch, log food loss rescue record
        if (batch.isSurplusUrgent || batch.discountPercent > 0) {
            val rescuedKg = allocatableQty
            val rescuedVal = buyerPayment
            val co2Saved = rescuedKg * 1.9 // ~1.9kg CO2e per kg avoided vegetable/grain waste
            spoilageDao.insertRecord(
                SpoilagePreventionRecord(
                    batchCode = batch.batchCode,
                    cropName = batch.cropName,
                    quantityKg = rescuedKg,
                    originalSpoilageRiskPercent = 100 - batch.freshnessPercentage,
                    discountAppliedPercent = batch.discountPercent,
                    buyerName = buyerName,
                    rescuedValueRupees = rescuedVal,
                    co2SavedKg = co2Saved
                )
            )
        }
        return true
    }

    suspend fun createSampleOrder(
        cropName: String = "Nashik Tomato (Grade A)",
        buyerName: String = "Reliance Fresh Mart",
        quantityKg: Double = 1200.0,
        destinationAddress: String = "Kalyani Nagar, Pune"
    ): OrderTransactionEntity {
        val newOrder = OrderTransactionEntity(
            orderCode = "KF-ORD-${(System.currentTimeMillis() % 100000)}",
            batchId = 1L,
            buyerName = buyerName,
            buyerType = BuyerType.SUPERMARKET_MART,
            farmerName = "Ramesh Patil",
            cropName = cropName,
            quantityKg = quantityKg,
            farmerPayoutTotal = quantityKg * 22.0,
            transportCostTotal = 850.0,
            buyerPaymentTotal = quantityKg * 24.5,
            grossMarginTotal = (quantityKg * 24.5) - (quantityKg * 22.0) - 850.0,
            status = OrderStatus.RESERVED,
            destinationAddress = destinationAddress
        )
        orderDao.insertOrder(newOrder)
        return newOrder
    }

    suspend fun updateOrderStatus(order: OrderTransactionEntity, newStatus: OrderStatus) {
        val now = System.currentTimeMillis()
        val updatedOrder = order.copy(
            status = newStatus,
            deliveredTimestamp = if (newStatus == OrderStatus.DELIVERED_PAID) now else order.deliveredTimestamp
        )
        orderDao.updateOrder(updatedOrder)

        // If delivered, mark batch sold quantity
        if (newStatus == OrderStatus.DELIVERED_PAID) {
            val batch = batchDao.getBatchByIdDirect(order.batchId)
            if (batch != null) {
                val newReserved = (batch.reservedQuantityKg - order.quantityKg).coerceAtLeast(0.0)
                val newSold = batch.soldQuantityKg + order.quantityKg
                val finalStatus = if (batch.availableQuantityKg <= 0 && newReserved <= 0) {
                    BatchStatus.COMPLETED
                } else {
                    batch.status
                }
                batchDao.updateBatch(
                    batch.copy(
                        reservedQuantityKg = newReserved,
                        soldQuantityKg = newSold,
                        status = finalStatus
                    )
                )
            }
        }
    }

    // =========================================================================
    // OFFLINE INVENTORY SCANNING & ROOM-FIRESTORE CACHE SYNC LAYER
    // =========================================================================

    suspend fun queueOfflineScannedInventory(item: OfflineInventoryQueueEntity): Long {
        return queueDao.insertQueueItem(item)
    }

    suspend fun getPendingSyncQueueItems(): List<OfflineInventoryQueueEntity> {
        return queueDao.getPendingSyncItemsDirect()
    }

    suspend fun markQueueItemSynced(queueId: Long) {
        queueDao.updateStatus(queueId, com.example.data.model.SyncQueueStatus.SYNCED, System.currentTimeMillis())
    }

    suspend fun deleteQueueItem(queueId: Long) {
        queueDao.deleteQueueItem(queueId)
    }

    suspend fun clearCompletedSyncItems() {
        queueDao.clearCompletedSyncedItems()
    }
}
