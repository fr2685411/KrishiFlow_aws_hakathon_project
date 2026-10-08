package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KrishiDatabase
import com.example.data.model.BatchStatus
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.BuyerType
import com.example.data.model.CropCategory
import com.example.data.model.CropPriceSpikeAlert
import com.example.data.model.CropPriceThreshold
import com.example.data.model.PriceSpikeUrgency
import com.example.data.model.HarvestHistoryLogEntity
import com.example.data.model.MandiRateEntity
import com.example.data.model.OfflineActionType
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.SyncQueueStatus
import com.example.data.notification.CropPriceAlertNotificationHelper
import com.example.data.notification.PriceSpikeDetectorEngine
import com.example.data.model.OrderStatus
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.QualityGrade
import com.example.data.model.SpoilagePreventionRecord
import com.example.data.model.StorageCondition
import com.example.data.model.FarmerProducePost
import com.example.data.model.FarmerProfile
import com.example.data.model.VendorProfile
import com.example.data.model.MandiManagerProfile
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.KrishiRepository
import com.example.data.repository.KrishiFirestoreRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.FirestoreUserRepository
import com.example.data.advisory.AiAdvisoryResult
import com.example.data.advisory.CropAdvisoryAiInput
import com.example.data.advisory.GeminiCropAdvisoryService
import com.example.data.advisory.GeminiSellOrStoreAdvisorService
import com.example.data.advisory.GeminiSellOrStoreInput
import com.example.data.advisory.GeminiSellOrStoreRecommendation
import com.example.data.auth.AuthUiState
import com.example.data.auth.KrishiAuthManager
import com.example.data.auth.KrishiUser
import com.example.data.model.AgroSeason
import com.example.data.model.CartItem
import com.example.data.model.IrrigationSource
import com.example.data.model.PreviousCropType
import com.example.data.model.ShopOrder
import com.example.data.model.ShopProduct
import com.example.data.model.SoilType
import com.example.data.repository.SustainableShopCatalog
import com.example.data.sync.CloudSyncState
import com.example.data.sync.KrishiFirestoreSyncService
import com.example.data.sync.KrishiWorkManagerScheduler
import androidx.work.WorkInfo
import com.example.data.wallet.TransactionType
import com.example.data.wallet.VendorPaymentPayload
import com.example.data.wallet.WalletTransaction
import com.example.ui.AppPersonaMode
import com.example.ui.theme.AppThemeOption
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KrishiUiState(
    val batches: List<ProduceBatchEntity> = emptyList(),
    val surplusBatches: List<ProduceBatchEntity> = emptyList(),
    val openDemands: List<BuyerDemandEntity> = emptyList(),
    val allDemands: List<BuyerDemandEntity> = emptyList(),
    val orders: List<OrderTransactionEntity> = emptyList(),
    val mandiRates: List<MandiRateEntity> = emptyList(),
    val spoilageRecords: List<SpoilagePreventionRecord> = emptyList(),
    val selectedCategory: CropCategory? = null,
    val searchQuery: String = "",
    val activeTab: Int = 0,
    val marketplaceRole: String = "BUYER", // "FARMER" or "BUYER"
    val totalInventoryKg: Double = 0.0,
    val totalRescuedKg: Double = 0.0,
    val totalLossPreventedRupees: Double = 0.0,
    val totalCo2SavedKg: Double = 0.0,
    val totalFarmerPayoutRupees: Double = 0.0,
    val isSeeding: Boolean = false,
    val userNotice: String? = null,
    val cloudSyncState: CloudSyncState = CloudSyncState.Idle,
    val authState: AuthUiState = AuthUiState.Idle,
    val producePosts: List<FarmerProducePost> = emptyList(),
    val shopProducts: List<ShopProduct> = SustainableShopCatalog.PRODUCTS,
    val cartItems: List<CartItem> = emptyList(),
    val shopOrders: List<ShopOrder> = emptyList(),
    val priceSpikeAlerts: List<CropPriceSpikeAlert> = emptyList(),
    val priceThresholds: List<CropPriceThreshold> = emptyList(),
    val harvestLogs: List<HarvestHistoryLogEntity> = emptyList(),
    val queuedInventory: List<OfflineInventoryQueueEntity> = emptyList(),
    val pendingOfflineSyncCount: Int = 0,
    val workManagerSyncState: String? = null,
    val lastWorkManagerSyncTime: Long? = null
)

private data class RepoData(
    val batches: List<ProduceBatchEntity> = emptyList(),
    val demands: List<BuyerDemandEntity> = emptyList(),
    val orders: List<OrderTransactionEntity> = emptyList(),
    val mandiRates: List<MandiRateEntity> = emptyList(),
    val spoilage: List<SpoilagePreventionRecord> = emptyList(),
    val harvestLogs: List<HarvestHistoryLogEntity> = emptyList(),
    val queuedInventory: List<OfflineInventoryQueueEntity> = emptyList(),
    val pendingSyncCount: Int = 0
)

private data class FilterData(
    val category: CropCategory? = null,
    val query: String = "",
    val tab: Int = 0,
    val role: String = "BUYER",
    val notice: String? = null,
    val cartItems: List<CartItem> = emptyList(),
    val shopOrders: List<ShopOrder> = emptyList(),
    val dismissedSpikes: Set<String> = emptySet(),
    val simulatedSpikes: List<CropPriceSpikeAlert> = emptyList(),
    val priceThresholds: List<CropPriceThreshold> = emptyList()
)

class KrishiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KrishiRepository
    private val syncService = KrishiFirestoreSyncService()
    private val authManager = KrishiAuthManager(application)
    val firestoreRepo = KrishiFirestoreRepository()
    val userRepository: UserRepository = FirestoreUserRepository()
    val weatherRepository = com.example.data.weather.WeatherRepository(application, viewModelScope)
    val weatherData = weatherRepository.weatherState
    val isWeatherRefreshing = weatherRepository.isRefreshing

    private val _workManagerSyncState = MutableStateFlow<String?>("Cloud Sync Ready (WorkManager)")
    private val _lastWorkManagerSyncTime = MutableStateFlow<Long?>(null)

    private val themePrefs = application.getSharedPreferences("krishi_app_theme_prefs", Context.MODE_PRIVATE)

    private val _appThemeMode = MutableStateFlow(
        runCatching {
            val savedName = themePrefs.getString("app_theme_mode", null)
            if (savedName != null) AppThemeOption.valueOf(savedName) else AppThemeOption.LIGHT
        }.getOrDefault(AppThemeOption.LIGHT)
    )
    val appThemeMode: StateFlow<AppThemeOption> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(theme: AppThemeOption) {
        _appThemeMode.value = theme
        themePrefs.edit().putString("app_theme_mode", theme.name).apply()
        _userNotice.value = "Theme switched to ${theme.label}"
    }

    private val geminiCropAdvisoryService = GeminiCropAdvisoryService()
    private val geminiSellOrStoreService = GeminiSellOrStoreAdvisorService()

    private val _aiAdvisoryResult = MutableStateFlow<AiAdvisoryResult?>(null)
    val aiAdvisoryResult: StateFlow<AiAdvisoryResult?> = _aiAdvisoryResult.asStateFlow()

    private val _isAiAdvisoryLoading = MutableStateFlow(false)
    val isAiAdvisoryLoading: StateFlow<Boolean> = _isAiAdvisoryLoading.asStateFlow()

    private val _sellOrStoreRecommendation = MutableStateFlow<GeminiSellOrStoreRecommendation?>(null)
    val sellOrStoreRecommendation: StateFlow<GeminiSellOrStoreRecommendation?> = _sellOrStoreRecommendation.asStateFlow()

    private val _isSellOrStoreLoading = MutableStateFlow(false)
    val isSellOrStoreLoading: StateFlow<Boolean> = _isSellOrStoreLoading.asStateFlow()

    fun runGeminiSellOrStoreAnalysis(
        cropName: String,
        variety: String,
        quantityKg: Double,
        askingPricePerKg: Double,
        storageCondition: String,
        freshnessDaysRemaining: Int = 4
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isSellOrStoreLoading.value = true
            try {
                val weather = weatherData.value
                val matchingRate = uiState.value.mandiRates.firstOrNull { rate ->
                    rate.commodity.contains(cropName, ignoreCase = true) ||
                    cropName.contains(rate.commodity, ignoreCase = true)
                }

                val currentMandiRate = matchingRate?.maxPricePerKg ?: (askingPricePerKg + 5.0)
                val trend = matchingRate?.priceTrend ?: "UP"

                val input = GeminiSellOrStoreInput(
                    cropName = cropName,
                    variety = variety,
                    quantityKg = quantityKg,
                    currentAskingPricePerKg = askingPricePerKg,
                    mandiRatePerKg = currentMandiRate,
                    currentMandiTrend = trend,
                    daysSinceHarvest = 1,
                    estimatedFreshnessDaysRemaining = freshnessDaysRemaining,
                    storageCondition = storageCondition,
                    locationName = weather.locationName,
                    currentTemp = weather.currentTemp,
                    humidityPercent = weather.humidity,
                    rainProbabilityToday = weather.rainProbabilityToday,
                    upcoming3DayRainSumMm = weather.weeklyForecast.take(3).sumOf { it.precipitationMm },
                    weatherCondition = weather.conditionDescription
                )

                val effectiveKey = com.example.data.gemini.KrishiGeminiKeyManager.getEffectiveApiKey(getApplication())
                val recommendation = geminiSellOrStoreService.analyzeHarvestAndWeather(input, effectiveKey)
                _sellOrStoreRecommendation.value = recommendation
                _userNotice.value = "Gemini AI: Suggested ${recommendation.timingDecision.label} for $cropName (+${recommendation.potentialProfitGainPercent.toInt()}% profit)!"
            } catch (e: Exception) {
                Log.e("KrishiViewModel", "Gemini Sell/Store analysis failed: ${e.message}", e)
                _userNotice.value = "Market timing analysis generated."
            } finally {
                _isSellOrStoreLoading.value = false
            }
        }
    }

    fun refreshWeather(requestGps: Boolean = true) {
        weatherRepository.refreshWeather(requestGps)
    }

    fun runGeminiCropAnalysis(
        soilType: SoilType,
        season: AgroSeason,
        irrigation: IrrigationSource,
        previousCrop: PreviousCropType,
        farmAcres: Double
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            _isAiAdvisoryLoading.value = true
            try {
                val weather = weatherData.value
                val input = CropAdvisoryAiInput(
                    soilType = soilType,
                    season = season,
                    irrigation = irrigation,
                    previousCrop = previousCrop,
                    farmAcres = farmAcres,
                    locationName = weather.locationName,
                    latitude = weather.latitude,
                    longitude = weather.longitude,
                    currentTemp = weather.currentTemp,
                    humidity = weather.humidity,
                    rainProbability = weather.rainProbabilityToday
                )
                val effectiveKey = com.example.data.gemini.KrishiGeminiKeyManager.getEffectiveApiKey(getApplication())
                val result = geminiCropAdvisoryService.generateAdvisory(input, effectiveKey)
                _aiAdvisoryResult.value = result
                _userNotice.value = "Gemini AI Agro-Analysis Completed for ${input.locationName}!"
            } catch (e: Exception) {
                Log.e("KrishiViewModel", "Gemini Crop Analysis failed: ${e.message}", e)
                _userNotice.value = "AI Analysis completed using local agronomy models."
            } finally {
                _isAiAdvisoryLoading.value = false
            }
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("KrishiViewModel", "Background task exception: ${throwable.message}", throwable)
    }

    private val _selectedCategory = MutableStateFlow<CropCategory?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _activeTab = MutableStateFlow(0)
    private val _marketplaceRole = MutableStateFlow("BUYER")
    private val _userNotice = MutableStateFlow<String?>(null)
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _shopOrders = MutableStateFlow<List<ShopOrder>>(emptyList())
    private val _dismissedAlertIds = MutableStateFlow<Set<String>>(emptySet())
    private val _simulatedSpikeAlerts = MutableStateFlow<List<CropPriceSpikeAlert>>(emptyList())
    private val _cropPriceThresholds = MutableStateFlow<List<CropPriceThreshold>>(
        listOf(
            CropPriceThreshold(
                id = "THRESH-TOM-01",
                cropName = "Hybrid Tomato",
                variety = "Abhinav / S-312",
                targetPricePerKg = 32.0,
                targetMandiName = "Pimpalgaon Baswant APMC",
                isEnabled = true,
                notes = "Alert when APMC wholesale price breaches ₹32/kg for quick harvesting"
            ),
            CropPriceThreshold(
                id = "THRESH-ONION-02",
                cropName = "Red Onion",
                variety = "Nashik Red Garwa",
                targetPricePerKg = 28.0,
                targetMandiName = "Lasalgaon APMC",
                isEnabled = true,
                notes = "Target peak festive demand rate"
            ),
            CropPriceThreshold(
                id = "THRESH-WHEAT-03",
                cropName = "Sharbati Wheat",
                variety = "Sharbati Golden 306",
                targetPricePerKg = 35.0,
                targetMandiName = "Nashik APMC",
                isEnabled = true,
                notes = "Premium export quality pricing"
            )
        )
    )
    val cropPriceThresholds: StateFlow<List<CropPriceThreshold>> = _cropPriceThresholds.asStateFlow()
    private val lastNotifiedPriceThresholds = mutableMapOf<String, Long>()

    private val _walletBalance = MutableStateFlow(18500.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _walletTransactions = MutableStateFlow<List<WalletTransaction>>(
        listOf(
            WalletTransaction(
                id = "TXN-INIT-1",
                vendorName = "Kisan Direct APMC Sales",
                vendorUpiId = "apmc.nashik@upi",
                amountRupees = 25000.0,
                supplyDescription = "Produce Harvest Advance Proceeds",
                type = TransactionType.PRODUCE_SALE_CREDIT,
                timestamp = System.currentTimeMillis() - 86400000L * 2,
                referenceId = "APMC-CREDIT-7192",
                balanceAfterRupees = 25000.0
            ),
            WalletTransaction(
                id = "TXN-INIT-2",
                vendorName = "Krishi Seva Kendra, Nashik",
                vendorUpiId = "krishiseva.nashik@upi",
                amountRupees = 6500.0,
                supplyDescription = "Trichoderma & Neem Bio-Formulations",
                type = TransactionType.SUPPLIES_PAYMENT,
                timestamp = System.currentTimeMillis() - 86400000L,
                referenceId = "UPI-REF-89218",
                balanceAfterRupees = 18500.0
            )
        )
    )
    val walletTransactions: StateFlow<List<WalletTransaction>> = _walletTransactions.asStateFlow()

    fun deleteHarvestSaleLog(id: Long) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.deleteHarvestLog(id)
            KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
            _userNotice.value = "Harvest history log removed."
        }
    }

    fun recordHarvestSaleLog(
        cropName: String,
        category: CropCategory = CropCategory.VEGETABLES,
        variety: String,
        harvestSeason: String,
        plantedAreaAcres: Double,
        harvestQuantityKg: Double,
        harvestDateTimestamp: Long,
        saleDateTimestamp: Long,
        quantitySoldKg: Double,
        soldRatePerKg: Double,
        buyerOrMandiName: String,
        storageLossKg: Double = 0.0,
        cycleDurationDays: Int = 90,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val totalRev = quantitySoldKg * soldRatePerKg
            val log = HarvestHistoryLogEntity(
                cropName = cropName,
                category = category,
                variety = variety,
                harvestSeason = harvestSeason,
                plantedAreaAcres = plantedAreaAcres,
                harvestQuantityKg = harvestQuantityKg,
                harvestDateTimestamp = harvestDateTimestamp,
                saleDateTimestamp = saleDateTimestamp,
                quantitySoldKg = quantitySoldKg,
                soldRatePerKg = soldRatePerKg,
                totalRevenueRupees = totalRev,
                buyerOrMandiName = buyerOrMandiName,
                storageLossKg = storageLossKg,
                cycleDurationDays = cycleDurationDays,
                notes = notes
            )
            repository.insertHarvestLog(log)
            KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
            _userNotice.value = "Recorded harvest sale log for $cropName ($harvestSeason)! Revenue: ₹${totalRev.toInt()}"
        }
    }

    fun dismissPriceSpikeAlert(alertId: String) {
        _dismissedAlertIds.value = _dismissedAlertIds.value + alertId
    }

    fun triggerMarketPriceSpikeSimulation(cropName: String = "Tomato", spikePercent: Double = 35.0) {
        val matchingBatch = uiState.value.batches.firstOrNull { it.cropName.contains(cropName, ignoreCase = true) }
        val basePrice = matchingBatch?.farmerAskingPricePerKg ?: 24.0
        val spikedPrice = basePrice * (1.0 + spikePercent / 100.0)
        val alert = CropPriceSpikeAlert(
            alertId = "SIM-SPIKE-${System.currentTimeMillis() % 10000}",
            cropName = matchingBatch?.cropName ?: cropName,
            variety = matchingBatch?.variety ?: "Hybrid Super Red",
            matchedBatchCode = matchingBatch?.batchCode ?: "BATCH-SIM-01",
            batchQuantityKg = matchingBatch?.availableQuantityKg ?: 1200.0,
            farmerBaseAskingPrice = basePrice,
            currentSpikePricePerKg = spikedPrice,
            spikePercentage = spikePercent,
            mandiName = "Pimpalgaon Baswant APMC",
            bestSellingWindowHours = 24,
            potentialExtraEarningsRupees = (matchingBatch?.availableQuantityKg ?: 1200.0) * (spikedPrice - basePrice),
            marketReason = "Wholesale terminal rush! Unseasonal rainfall in southern states triggered massive price rally.",
            urgency = PriceSpikeUrgency.PEAK,
            timestamp = System.currentTimeMillis()
        )
        _simulatedSpikeAlerts.value = listOf(alert) + _simulatedSpikeAlerts.value
        _userNotice.value = "🚀 Market Price Spike Alert: $cropName surged +${spikePercent.toInt()}% in local APMC!"
    }

    fun executeSpikeSaleOptimization(alert: CropPriceSpikeAlert) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val matchingBatch = uiState.value.batches.firstOrNull { it.batchCode == alert.matchedBatchCode }
            if (matchingBatch != null) {
                repository.updateBatchStatus(matchingBatch.id, BatchStatus.ALLOCATED)
            }
            dismissPriceSpikeAlert(alert.alertId)
            _userNotice.value = "🎯 Peak Sale Executed! Locked in ₹${alert.currentSpikePricePerKg.toInt()}/kg (+₹${alert.potentialExtraEarningsRupees.toInt()} extra profit) for ${alert.cropName}!"
            triggerCloudSync()
        }
    }

    // =========================================================================
    // CROP MARKET PRICE THRESHOLD LOCAL ALERT NOTIFICATION SYSTEM
    // =========================================================================

    fun savePriceThreshold(threshold: CropPriceThreshold) {
        val currentList = _cropPriceThresholds.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == threshold.id }
        if (index >= 0) {
            currentList[index] = threshold
            _userNotice.value = "Updated price alert for ${threshold.cropName} (Target: ₹${threshold.targetPricePerKg.toInt()}/kg)"
        } else {
            currentList.add(threshold)
            _userNotice.value = "Active price alert set for ${threshold.cropName} at ₹${threshold.targetPricePerKg.toInt()}/kg!"
        }
        _cropPriceThresholds.value = currentList

        // Immediately evaluate against current APMC Mandi rates
        evaluatePriceThresholdsAgainstMandiRates(currentList, uiState.value.mandiRates)
    }

    fun deletePriceThreshold(thresholdId: String) {
        _cropPriceThresholds.value = _cropPriceThresholds.value.filterNot { it.id == thresholdId }
        _userNotice.value = "Removed crop price alert threshold."
    }

    fun togglePriceThreshold(thresholdId: String, isEnabled: Boolean) {
        val updated = _cropPriceThresholds.value.map {
            if (it.id == thresholdId) it.copy(isEnabled = isEnabled) else it
        }
        _cropPriceThresholds.value = updated
        if (isEnabled) {
            evaluatePriceThresholdsAgainstMandiRates(updated, uiState.value.mandiRates)
        }
    }

    fun triggerTestPriceAlertNotification(
        cropName: String,
        targetPrice: Double,
        currentPrice: Double,
        mandiName: String
    ) {
        CropPriceAlertNotificationHelper.sendTestPriceAlert(
            context = getApplication(),
            cropName = cropName,
            targetPrice = targetPrice,
            currentPrice = currentPrice,
            mandiName = mandiName
        )
        _userNotice.value = "Sent test notification alert for $cropName (₹${currentPrice.toInt()}/kg)!"
    }

    /**
     * Checks all enabled farmer thresholds against live APMC Mandi rates.
     * When a rate meets or exceeds the target, a local notification alert is posted.
     */
    private fun evaluatePriceThresholdsAgainstMandiRates(
        thresholds: List<CropPriceThreshold>,
        mandiRates: List<MandiRateEntity>
    ) {
        if (mandiRates.isEmpty()) return
        val now = System.currentTimeMillis()
        val context = getApplication<Application>()

        for (threshold in thresholds.filter { it.isEnabled }) {
            val matchingRate = mandiRates.firstOrNull { rate ->
                rate.commodity.contains(threshold.cropName, ignoreCase = true) ||
                threshold.cropName.contains(rate.commodity, ignoreCase = true)
            }

            if (matchingRate != null && matchingRate.maxPricePerKg >= threshold.targetPricePerKg) {
                // Rate threshold breached! Check 30-minute debouncing to prevent notification spam
                val lastTime = lastNotifiedPriceThresholds[threshold.id] ?: 0L
                val timeSinceLast = now - lastTime

                if (timeSinceLast > 30 * 60 * 1000L) { // 30 minutes cooldown
                    lastNotifiedPriceThresholds[threshold.id] = now
                    CropPriceAlertNotificationHelper.sendPriceThresholdAlert(
                        context = context,
                        threshold = threshold,
                        currentMandiRate = matchingRate
                    )
                    Log.d("KrishiViewModel", "Dispatched local price threshold alert for ${threshold.cropName}")
                }
            }
        }
    }

    // =========================================================================
    // OFFLINE INVENTORY SCANNING & ROOM-FIRESTORE CACHE SYNC LAYER
    // =========================================================================

    fun scanAndQueueOfflineInventory(
        batchCode: String,
        cropName: String,
        category: String,
        variety: String,
        qualityGrade: String,
        quantityKg: Double,
        farmerName: String,
        farmLocation: String,
        askingPricePerKg: Double,
        storageCondition: String,
        freshnessDays: Int = 5
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val queueEntity = OfflineInventoryQueueEntity(
                batchCode = batchCode.ifBlank { "SCAN-BATCH-${System.currentTimeMillis() % 100000}" },
                cropName = cropName,
                category = category,
                variety = variety,
                qualityGrade = qualityGrade,
                scannedQuantityKg = quantityKg,
                farmerName = farmerName.ifBlank { "Kisan User" },
                farmLocation = farmLocation.ifBlank { "Nashik Hub" },
                askingPricePerKg = askingPricePerKg,
                storageCondition = storageCondition,
                expectedFreshnessDays = freshnessDays,
                actionType = OfflineActionType.SCAN_HARVEST_BATCH,
                syncStatus = SyncQueueStatus.QUEUED,
                scannedTimestamp = System.currentTimeMillis()
            )

            // 1. Immediately cache in local Room database
            repository.queueOfflineScannedInventory(queueEntity)

            // 2. Also register in local produce batches so farmer sees it instantly in UI even offline
            val localBatch = ProduceBatchEntity(
                batchCode = queueEntity.batchCode,
                cropName = queueEntity.cropName,
                category = try { CropCategory.valueOf(category) } catch (_: Exception) { CropCategory.VEGETABLES },
                variety = queueEntity.variety,
                qualityGrade = try { QualityGrade.valueOf(qualityGrade) } catch (_: Exception) { QualityGrade.GRADE_A },
                farmerName = queueEntity.farmerName,
                farmerPhone = "+91 98220 14829",
                farmLocation = queueEntity.farmLocation,
                distanceKm = 12,
                totalQuantityKg = quantityKg,
                availableQuantityKg = quantityKg,
                reservedQuantityKg = 0.0,
                farmerAskingPricePerKg = askingPricePerKg,
                hubProcuredPricePerKg = askingPricePerKg,
                deliveredPricePerKg = askingPricePerKg + 3.0,
                discountPercent = 0,
                harvestTimestamp = System.currentTimeMillis(),
                expiryTimestamp = System.currentTimeMillis() + (freshnessDays * 24 * 3600 * 1000L),
                expectedFreshnessDays = freshnessDays,
                storageCondition = try { StorageCondition.valueOf(storageCondition) } catch (_: Exception) { StorageCondition.VENTILATED_CRATE },
                status = BatchStatus.PROCURED_IN_HUB,
                notes = "Scanned offline; cached in Room queue"
            )
            repository.insertBatch(localBatch)

            _userNotice.value = "📦 Scanned ${queueEntity.cropName} (${quantityKg.toInt()} kg)! Cached in Room DB queue for auto-sync."

            // 3. Trigger WorkManager to flush immediately if internet is present or wait for connectivity
            KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
        }
    }

    fun deleteOfflineQueueItem(queueId: Long) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.deleteQueueItem(queueId)
            _userNotice.value = "Removed item from offline sync queue."
        }
    }

    fun clearCompletedOfflineSyncedItems() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.clearCompletedSyncItems()
            _userNotice.value = "Cleared completed synced items."
        }
    }

    fun triggerOfflineQueueManualSync() {
        KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
        _userNotice.value = "Flushing offline inventory queue to Firestore via WorkManager..."
    }

    fun payVendorViaWallet(payload: VendorPaymentPayload, customAmount: Double? = null): Boolean {
        val amount = customAmount ?: payload.amountRupees
        if (amount <= 0.0) {
            _userNotice.value = "Invalid payment amount!"
            return false
        }
        if (_walletBalance.value < amount) {
            _userNotice.value = "Insufficient wallet balance (₹${_walletBalance.value.toInt()}) to pay ₹${amount.toInt()}!"
            return false
        }

        val newBalance = _walletBalance.value - amount
        _walletBalance.value = newBalance

        val txn = WalletTransaction(
            id = "TXN-${System.currentTimeMillis() % 1000000}",
            vendorName = payload.vendorName,
            vendorUpiId = payload.vendorUpiId,
            amountRupees = amount,
            supplyDescription = payload.supplyDescription,
            type = TransactionType.SUPPLIES_PAYMENT,
            timestamp = System.currentTimeMillis(),
            referenceId = "UPI-KP-${System.currentTimeMillis() % 100000}",
            balanceAfterRupees = newBalance
        )
        _walletTransactions.value = listOf(txn) + _walletTransactions.value
        _userNotice.value = "Paid ₹${amount.toInt()} to ${payload.vendorName} via Digital Wallet!"
        return true
    }

    fun topUpWallet(amountRupees: Double) {
        if (amountRupees <= 0) return
        val newBalance = _walletBalance.value + amountRupees
        _walletBalance.value = newBalance
        val txn = WalletTransaction(
            id = "TXN-TOP-${System.currentTimeMillis() % 1000000}",
            vendorName = "UPI Bank Direct Top-Up",
            vendorUpiId = "bank.gateway@upi",
            amountRupees = amountRupees,
            supplyDescription = "Bank Account to Digital Wallet Transfer",
            type = TransactionType.WALLET_TOPUP,
            timestamp = System.currentTimeMillis(),
            referenceId = "BANK-TOPUP-${System.currentTimeMillis() % 100000}",
            balanceAfterRupees = newBalance
        )
        _walletTransactions.value = listOf(txn) + _walletTransactions.value
        _userNotice.value = "Added ₹${amountRupees.toInt()} to your digital wallet!"
    }

    val firestoreFarmers: StateFlow<List<FarmerProfile>> = firestoreRepo.observeFarmers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreVendors: StateFlow<List<VendorProfile>> = firestoreRepo.observeVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreMandiManagers: StateFlow<List<MandiManagerProfile>> = firestoreRepo.observeMandiManagers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreProducePosts: StateFlow<List<FarmerProducePost>> = firestoreRepo.observeProducePosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<KrishiUiState>

    init {
        val db = KrishiDatabase.getInstance(application)
        repository = KrishiRepository(db)

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.seedInitialDataIfEmpty()
            firestoreRepo.seedInitialActorsIfEmpty()
            userRepository.seedSampleProfilesIfEmpty()
            seedInitialProducePostsIfEmpty()
            triggerCloudSync()
            KrishiWorkManagerScheduler.schedulePeriodicSync(application)
        }

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            KrishiWorkManagerScheduler.observeOneTimeSyncStatus(application).collect { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.RUNNING -> _workManagerSyncState.value = "WorkManager Syncing to Firestore..."
                        WorkInfo.State.ENQUEUED -> _workManagerSyncState.value = "Queued (Waiting for Internet)"
                        WorkInfo.State.SUCCEEDED -> {
                            _workManagerSyncState.value = "Synced to Cloud via WorkManager"
                            _lastWorkManagerSyncTime.value = System.currentTimeMillis()
                        }
                        WorkInfo.State.FAILED -> _workManagerSyncState.value = "Sync Failed (Auto-Retrying)"
                        else -> {}
                    }
                }
            }
        }

        val baseRepoFlow = combine(
            repository.allBatches,
            repository.allDemands,
            repository.allOrders,
            repository.allMandiRates,
            repository.spoilageRecords
        ) { batches, demands, orders, mandiRates, spoilage ->
            RepoData(batches, demands, orders, mandiRates, spoilage)
        }
        val repoFlow = combine(
            baseRepoFlow,
            repository.allHarvestLogs,
            repository.allQueuedInventory,
            repository.pendingQueueCount
        ) { repo, logs, queued, pendingCount ->
            repo.copy(
                harvestLogs = logs,
                queuedInventory = queued,
                pendingSyncCount = pendingCount
            )
        }

        val shopFlow = combine(_cartItems, _shopOrders) { cart, orders -> Pair(cart, orders) }
        val spikeFlow = combine(_dismissedAlertIds, _simulatedSpikeAlerts) { dismissed, simulated ->
            Pair(dismissed, simulated)
        }
        val rawFilterFlow = combine(
            _selectedCategory,
            _searchQuery,
            _activeTab,
            _marketplaceRole,
            _userNotice
        ) { category, query, tab, role, notice ->
            FilterData(category, query, tab, role, notice)
        }
        val filterFlow = combine(rawFilterFlow, shopFlow, spikeFlow, _cropPriceThresholds) { filter, shop, spikes, thresholds ->
            filter.copy(
                cartItems = shop.first,
                shopOrders = shop.second,
                dismissedSpikes = spikes.first,
                simulatedSpikes = spikes.second,
                priceThresholds = thresholds
            )
        }

        uiState = combine(repoFlow, filterFlow, syncService.syncState, authManager.authState, firestoreRepo.observeProducePosts()) { repo, filter, syncState, authState, producePosts ->
            val filteredBatches = repo.batches.filter { b ->
                val matchesCategory = (filter.category == null || b.category == filter.category)
                val matchesQuery = filter.query.isBlank() ||
                        b.cropName.contains(filter.query, ignoreCase = true) ||
                        b.farmerName.contains(filter.query, ignoreCase = true) ||
                        b.farmLocation.contains(filter.query, ignoreCase = true) ||
                        b.batchCode.contains(filter.query, ignoreCase = true)
                matchesCategory && matchesQuery
            }

            // Surplus priority batches: ranked strictly by freshness urgency & remaining quantity
            val surplus = repo.batches
                .filter { it.availableQuantityKg > 0 && it.status != BatchStatus.COMPLETED && it.freshnessPercentage <= 50 }
                .sortedBy { it.freshnessPercentage }

            val totalInv = repo.batches.filter { it.status == BatchStatus.PROCURED_IN_HUB }.sumOf { it.availableQuantityKg }
            val rescuedKg = repo.spoilage.sumOf { it.quantityKg }
            val lossPreventedRs = repo.spoilage.sumOf { it.rescuedValueRupees }
            val co2Saved = repo.spoilage.sumOf { it.co2SavedKg }
            val farmerPayout = repo.orders.filter { it.status == OrderStatus.DELIVERED_PAID }.sumOf { it.farmerPayoutTotal }

            val detectedSpikes = PriceSpikeDetectorEngine.detectSpikes(repo.batches, repo.mandiRates)
            val activeSpikes = (filter.simulatedSpikes + detectedSpikes)
                .filter { it.alertId !in filter.dismissedSpikes }
                .distinctBy { it.alertId }

            KrishiUiState(
                batches = filteredBatches,
                surplusBatches = surplus,
                openDemands = repo.demands.filter { it.status == "OPEN" },
                allDemands = repo.demands,
                orders = repo.orders,
                mandiRates = repo.mandiRates,
                spoilageRecords = repo.spoilage,
                selectedCategory = filter.category,
                searchQuery = filter.query,
                activeTab = filter.tab,
                marketplaceRole = filter.role,
                totalInventoryKg = totalInv,
                totalRescuedKg = rescuedKg,
                totalLossPreventedRupees = lossPreventedRs,
                totalCo2SavedKg = co2Saved,
                totalFarmerPayoutRupees = farmerPayout,
                userNotice = filter.notice,
                cloudSyncState = syncState,
                authState = authState,
                producePosts = producePosts,
                cartItems = filter.cartItems,
                shopOrders = filter.shopOrders,
                priceSpikeAlerts = activeSpikes,
                priceThresholds = filter.priceThresholds,
                harvestLogs = repo.harvestLogs,
                queuedInventory = repo.queuedInventory,
                pendingOfflineSyncCount = repo.pendingSyncCount,
                workManagerSyncState = _workManagerSyncState.value,
                lastWorkManagerSyncTime = _lastWorkManagerSyncTime.value
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            KrishiUiState()
        )
    }

    private suspend fun seedInitialProducePostsIfEmpty() {
        val existing = firestoreRepo.getAllProducePosts().getOrDefault(emptyList())
        if (existing.isEmpty()) {
            val samplePosts = listOf(
                FarmerProducePost(
                    postId = "PRODUCE_TOMATO_01",
                    farmerId = "FARMER_RAMESH_PATIL",
                    farmerName = "Ramesh Patil",
                    farmerPhone = "+91 98220 14829",
                    farmLocation = "Niphad, Nashik",
                    district = "Nashik",
                    state = "Maharashtra",
                    cropName = "Tomato",
                    category = CropCategory.VEGETABLES,
                    variety = "Himsona / Hybrid",
                    quantityKg = 2500.0,
                    quantityUnit = "KG",
                    askingPricePerKg = 22.0,
                    mandiBenchmarkPricePerKg = 18.0,
                    qualityGrade = QualityGrade.GRADE_A,
                    storageCondition = StorageCondition.VENTILATED_CRATE,
                    expectedFreshnessDays = 8,
                    isOrganicOrResidueFree = true,
                    readyForImmediatePickup = true,
                    notes = "Grade A sorting completed in 20kg crates.",
                    status = "LISTED"
                ),
                FarmerProducePost(
                    postId = "PRODUCE_ONION_02",
                    farmerId = "FARMER_SUNITA_MORE",
                    farmerName = "Sunita More",
                    farmerPhone = "+91 94231 88921",
                    farmLocation = "Malegaon Khurd, Baramati",
                    district = "Pune",
                    state = "Maharashtra",
                    cropName = "Red Onion",
                    category = CropCategory.VEGETABLES,
                    variety = "Nashik Red Garwa",
                    quantityKg = 4000.0,
                    quantityUnit = "QUINTAL",
                    askingPricePerKg = 28.5,
                    mandiBenchmarkPricePerKg = 24.0,
                    qualityGrade = QualityGrade.GRADE_A,
                    storageCondition = StorageCondition.AMBIENT_SHED,
                    expectedFreshnessDays = 30,
                    isOrganicOrResidueFree = false,
                    readyForImmediatePickup = true,
                    notes = "Cured summer Garwa crop with low moisture content.",
                    status = "LISTED"
                ),
                FarmerProducePost(
                    postId = "PRODUCE_WHEAT_03",
                    farmerId = "FARMER_GOPAL_JOSHI",
                    farmerName = "Gopal Joshi",
                    farmerPhone = "+91 97654 32109",
                    farmLocation = "Dindori, Nashik",
                    district = "Nashik",
                    state = "Maharashtra",
                    cropName = "Sharbati Wheat",
                    category = CropCategory.GRAINS,
                    variety = "Sharbati Golden 306",
                    quantityKg = 6000.0,
                    quantityUnit = "QUINTAL",
                    askingPricePerKg = 34.0,
                    mandiBenchmarkPricePerKg = 30.5,
                    qualityGrade = QualityGrade.GRADE_A,
                    storageCondition = StorageCondition.AMBIENT_SHED,
                    expectedFreshnessDays = 180,
                    isOrganicOrResidueFree = true,
                    readyForImmediatePickup = true,
                    notes = "Double cleaned, lustre golden grains, moisture below 10%.",
                    status = "LISTED"
                ),
                FarmerProducePost(
                    postId = "PRODUCE_POMEGRANATE_04",
                    farmerId = "FARMER_BALASAHEB_SHINDE",
                    farmerName = "Balasaheb Shinde",
                    farmerPhone = "+91 98810 54321",
                    farmLocation = "Sangola, Solapur",
                    district = "Solapur",
                    state = "Maharashtra",
                    cropName = "Pomegranate",
                    category = CropCategory.FRUITS,
                    variety = "Bhagwa Super Red",
                    quantityKg = 3200.0,
                    quantityUnit = "TONNE",
                    askingPricePerKg = 110.0,
                    mandiBenchmarkPricePerKg = 95.0,
                    qualityGrade = QualityGrade.GRADE_A,
                    storageCondition = StorageCondition.COLD_STORAGE,
                    expectedFreshnessDays = 25,
                    isOrganicOrResidueFree = true,
                    readyForImmediatePickup = true,
                    notes = "Export grade, deep red arils, brix 15+, packed in corrugated boxes.",
                    status = "LISTED"
                ),
                FarmerProducePost(
                    postId = "PRODUCE_CHANA_05",
                    farmerId = "FARMER_KESHAV_DESHMUKH",
                    farmerName = "Keshav Deshmukh",
                    farmerPhone = "+91 93700 88219",
                    farmLocation = "Ausa, Latur",
                    district = "Latur",
                    state = "Maharashtra",
                    cropName = "Desi Chana",
                    category = CropCategory.PULSES,
                    variety = "Vijay Bold Chickpea",
                    quantityKg = 3500.0,
                    quantityUnit = "QUINTAL",
                    askingPricePerKg = 68.0,
                    mandiBenchmarkPricePerKg = 62.0,
                    qualityGrade = QualityGrade.GRADE_A,
                    storageCondition = StorageCondition.AMBIENT_SHED,
                    expectedFreshnessDays = 150,
                    isOrganicOrResidueFree = false,
                    readyForImmediatePickup = true,
                    notes = "High-protein unpolished whole gram, 100% sortex clean.",
                    status = "LISTED"
                )
            )
            for (p in samplePosts) {
                firestoreRepo.createProducePost(p)
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, role: AppPersonaMode) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = authManager.signInWithEmail(email, pass, role)
            if (result.isSuccess) {
                _userNotice.value = "Welcome back, ${result.getOrNull()?.displayName}!"
            } else {
                _userNotice.value = "Sign in issue: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun signInWithGoogle(
        role: AppPersonaMode = AppPersonaMode.CENTRAL_OPS,
        accountName: String? = "Ajrudeen Kureshi",
        accountEmail: String? = "ajrudeenkureshi2003@gmail.com",
        idToken: String? = null,
        onComplete: (Result<KrishiUser>) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = authManager.signInWithGoogle(idToken, role, accountName, accountEmail)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _userNotice.value = "Google login verified: ${user?.displayName} (${user?.email})"
            } else {
                _userNotice.value = "Google login failed: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun requestPhoneOtp(phoneNumber: String, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = authManager.requestPhoneOtp(phoneNumber)
            if (result.isSuccess) {
                _userNotice.value = "OTP sent to $phoneNumber"
            } else {
                _userNotice.value = "Failed to send OTP: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String,
        role: AppPersonaMode,
        phoneNumber: String,
        onComplete: (Result<KrishiUser>) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = authManager.verifyPhoneOtp(verificationId, otpCode, role, phoneNumber)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _userNotice.value = "Phone login verified for ${user?.phoneNumber} as ${role.title}"
            } else {
                _userNotice.value = "OTP verification failed: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun resetAuthState() {
        authManager.resetAuthState()
    }

    fun signInAsRole(role: AppPersonaMode, name: String) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = authManager.signInAsGuestOrRole(role, name)
            _userNotice.value = "Authenticated session active as ${result.getOrNull()?.displayName} (${role.title})"
        }
    }

    fun signOut() {
        authManager.signOut()
        _userNotice.value = "Signed out of secure session."
    }

    fun triggerCloudSync() {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val batches = repository.allBatches.firstOrNull() ?: emptyList()
            val demands = repository.allDemands.firstOrNull() ?: emptyList()
            val orders = repository.allOrders.firstOrNull() ?: emptyList()
            val rates = repository.allMandiRates.firstOrNull() ?: emptyList()
            val harvestLogs = repository.allHarvestLogs.firstOrNull() ?: emptyList()
            syncService.syncAllData(batches, demands, orders, rates, harvestLogs)
            KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
        }
    }

    fun triggerWorkManagerSync() {
        KrishiWorkManagerScheduler.triggerImmediateSync(getApplication())
        _workManagerSyncState.value = "Queued WorkManager Sync"
        _userNotice.value = "WorkManager background sync enqueued (Network: Connected constraint)"
    }

    fun setCategoryFilter(category: CropCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    fun setMarketplaceRole(role: String) {
        _marketplaceRole.value = role
    }

    fun clearNotice() {
        _userNotice.value = null
    }

    fun addNewBatch(
        cropName: String,
        category: CropCategory,
        variety: String,
        grade: QualityGrade,
        farmerName: String,
        farmerPhone: String,
        farmLocation: String,
        distanceKm: Int,
        quantityKg: Double,
        askingPricePerKg: Double,
        storageCondition: StorageCondition,
        expectedFreshnessDays: Int,
        notes: String
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val now = System.currentTimeMillis()
            val hubPrice = askingPricePerKg + (askingPricePerKg * 0.10)
            val deliveredPrice = hubPrice + (hubPrice * 0.28)
            val codeSuffix = (System.currentTimeMillis() % 1000).toString().padStart(3, '0')
            val batchCode = "KF-${cropName.take(3).uppercase()}-$codeSuffix"

            val batch = ProduceBatchEntity(
                batchCode = batchCode,
                cropName = cropName,
                category = category,
                variety = variety,
                qualityGrade = grade,
                farmerName = farmerName,
                farmerPhone = farmerPhone,
                farmLocation = farmLocation,
                distanceKm = distanceKm,
                totalQuantityKg = quantityKg,
                availableQuantityKg = quantityKg,
                reservedQuantityKg = 0.0,
                soldQuantityKg = 0.0,
                farmerAskingPricePerKg = askingPricePerKg,
                hubProcuredPricePerKg = hubPrice,
                deliveredPricePerKg = deliveredPrice,
                discountPercent = 0,
                harvestTimestamp = now,
                expiryTimestamp = now + (expectedFreshnessDays * 24 * 3600 * 1000L),
                expectedFreshnessDays = expectedFreshnessDays,
                storageCondition = storageCondition,
                status = BatchStatus.LISTED,
                notes = notes
            )
            repository.insertBatch(batch)
            _userNotice.value = "Batch $batchCode listed! KrishiFlow procurement check initiated."
            triggerCloudSync()
        }
    }

    /**
     * Posts farmer agricultural produce, calculates mandi benchmark margins,
     * writes directly to Cloud Firestore collection "krishiflow_produce_posts",
     * and syncs to local Room database for instantaneous UI reactivity.
     */
    fun postFarmerProduce(
        cropName: String,
        category: CropCategory,
        variety: String,
        grade: QualityGrade,
        farmerName: String,
        farmerPhone: String,
        farmLocation: String,
        district: String,
        quantityKg: Double,
        quantityUnit: String,
        askingPricePerKg: Double,
        mandiBenchmarkPricePerKg: Double,
        storageCondition: StorageCondition,
        expectedFreshnessDays: Int,
        isOrganicOrResidueFree: Boolean,
        readyForImmediatePickup: Boolean,
        notes: String,
        onComplete: ((Result<String>) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val codeSuffix = (System.currentTimeMillis() % 10000).toString().padStart(4, '0')
            val postId = "PRODUCE_${cropName.take(3).uppercase()}_$codeSuffix"

            val authUser = (authManager.authState.value as? AuthUiState.Authenticated)?.user
            val post = FarmerProducePost(
                postId = postId,
                farmerId = authUser?.uid ?: "FARMER_RAMESH_PATIL",
                farmerName = farmerName.ifBlank { authUser?.displayName ?: "Ramesh Patil" },
                farmerPhone = farmerPhone.ifBlank { authUser?.phoneNumber?.ifBlank { "+91 98220 14829" } ?: "+91 98220 14829" },
                farmLocation = farmLocation.ifBlank { "Niphad, Nashik" },
                district = district.ifBlank { "Nashik" },
                state = "Maharashtra",
                cropName = cropName,
                category = category,
                variety = variety,
                quantityKg = quantityKg,
                quantityUnit = quantityUnit,
                askingPricePerKg = askingPricePerKg,
                mandiBenchmarkPricePerKg = mandiBenchmarkPricePerKg,
                qualityGrade = grade,
                storageCondition = storageCondition,
                expectedFreshnessDays = expectedFreshnessDays,
                isOrganicOrResidueFree = isOrganicOrResidueFree,
                readyForImmediatePickup = readyForImmediatePickup,
                notes = notes,
                status = "LISTED"
            )

            // 1. Direct Cloud Firestore write
            val firestoreResult = firestoreRepo.createProducePost(post)

            // 2. Local Room DB insertion so marketplace and inventory instantly update
            val hubPrice = askingPricePerKg + (askingPricePerKg * 0.10)
            val deliveredPrice = hubPrice + (hubPrice * 0.28)
            val batch = ProduceBatchEntity(
                batchCode = "KF-${cropName.take(3).uppercase()}-$codeSuffix",
                cropName = cropName,
                category = category,
                variety = variety,
                qualityGrade = grade,
                farmerName = post.farmerName,
                farmerPhone = post.farmerPhone,
                farmLocation = post.farmLocation,
                distanceKm = 18,
                totalQuantityKg = quantityKg,
                availableQuantityKg = quantityKg,
                farmerAskingPricePerKg = askingPricePerKg,
                hubProcuredPricePerKg = hubPrice,
                deliveredPricePerKg = deliveredPrice,
                discountPercent = 0,
                harvestTimestamp = System.currentTimeMillis(),
                expiryTimestamp = System.currentTimeMillis() + (expectedFreshnessDays * 24 * 3600 * 1000L),
                expectedFreshnessDays = expectedFreshnessDays,
                storageCondition = storageCondition,
                status = BatchStatus.LISTED,
                notes = notes
            )
            repository.insertBatch(batch)

            _userNotice.value = "Produce '$cropName' (${quantityKg.toInt()} kg @ ₹$askingPricePerKg/kg) saved to Cloud Firestore & live for buyers!"
            triggerCloudSync()
            onComplete?.invoke(firestoreResult)
        }
    }

    fun verifyAndProcureBatch(batch: ProduceBatchEntity) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.updateBatchStatus(batch.id, BatchStatus.PROCURED_IN_HUB)
            _userNotice.value = "Batch ${batch.batchCode} verified & collected into Hub inventory!"
            triggerCloudSync()
        }
    }

    fun applySurplusDiscount(batchId: Long, discountPercent: Int) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.applySurplusDiscount(batchId, discountPercent)
            _userNotice.value = "Surplus alert broadcast: $discountPercent% discount activated for quick sale!"
            triggerCloudSync()
        }
    }

    fun addNewDemand(
        buyerName: String,
        buyerType: BuyerType,
        buyerPhone: String,
        destination: String,
        cropRequested: String,
        minGrade: QualityGrade,
        quantityKg: Double,
        maxPricePerKg: Double
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val demandCode = "RFQ-${(System.currentTimeMillis() % 1000).toString().padStart(3, '0')}"
            val demand = BuyerDemandEntity(
                demandCode = demandCode,
                buyerName = buyerName,
                buyerType = buyerType,
                buyerPhone = buyerPhone,
                destinationLocation = destination,
                cropRequested = cropRequested,
                minGrade = minGrade,
                requiredQuantityKg = quantityKg,
                maxOfferedPricePerKg = maxPricePerKg,
                status = "OPEN"
            )
            repository.insertDemand(demand)
            _userNotice.value = "RFQ $demandCode posted for $quantityKg kg $cropRequested!"
            triggerCloudSync()
        }
    }

    fun matchDemandWithBatch(demand: BuyerDemandEntity, batch: ProduceBatchEntity, qtyKg: Double) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val success = repository.matchDemandWithBatch(
                demand = demand,
                batch = batch,
                quantityKg = qtyKg,
                destinationAddress = demand.destinationLocation
            )
            if (success) {
                _userNotice.value = "Demand matched! Order generated and $qtyKg kg reserved."
                triggerCloudSync()
            } else {
                _userNotice.value = "Insufficient available stock to match demand."
            }
        }
    }

    fun placeDirectOrder(
        batch: ProduceBatchEntity,
        buyerName: String,
        buyerType: BuyerType,
        qtyKg: Double,
        deliveryAddress: String
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val success = repository.createDirectOrder(
                batch = batch,
                buyerName = buyerName,
                buyerType = buyerType,
                quantityKg = qtyKg,
                destinationAddress = deliveryAddress
            )
            if (success) {
                _userNotice.value = "Order placed! $qtyKg kg ${batch.cropName} reserved for delivery."
                triggerCloudSync()
            } else {
                _userNotice.value = "Cannot place order: Requested quantity exceeds available stock."
            }
        }
    }

    fun updateOrderStatus(order: OrderTransactionEntity, nextStatus: OrderStatus) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            repository.updateOrderStatus(order, nextStatus)
            if (nextStatus == OrderStatus.DELIVERED_PAID) {
                _userNotice.value = "Order ${order.orderCode} delivered! Farmer instant UPI payout of ₹${order.farmerPayoutTotal.toInt()} released."
            } else {
                _userNotice.value = "Order ${order.orderCode} updated to ${nextStatus.displayName}."
            }
            triggerCloudSync()
        }
    }

    fun createSampleProcurementOrder(
        cropName: String = "Nashik Tomato (Grade A)",
        qtyKg: Double = 1200.0
    ) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val newOrder = repository.createSampleOrder(
                cropName = cropName,
                quantityKg = qtyKg
            )
            _userNotice.value = "New procurement order ${newOrder.orderCode} placed in 'Pending' state!"
            triggerCloudSync()
        }
    }

    // =========================================================================
    // SUSTAINABLE INPUT SHOPPING & CART METHODS
    // =========================================================================

    fun addToCart(product: ShopProduct) {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            current.add(CartItem(product = product, quantity = 1))
        }
        _cartItems.value = current
        _userNotice.value = "Added '${product.name}' to Cart"
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun checkoutCart(deliveryAddress: String, farmerName: String = "Ramesh Patil"): Result<ShopOrder> {
        val items = _cartItems.value
        if (items.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty"))
        }
        val total = items.sumOf { it.product.priceRupees * it.quantity }
        val order = ShopOrder(
            orderId = "ECO-INP-${(System.currentTimeMillis() % 100000)}",
            items = items,
            totalAmountRupees = total,
            deliveryAddress = deliveryAddress.ifBlank { "Village Farmgate Hub, Dindori, Nashik" }
        )
        _shopOrders.value = listOf(order) + _shopOrders.value
        _cartItems.value = emptyList()
        _userNotice.value = "Order #${order.orderId} Confirmed! Dispatching eco-friendly inputs to ${order.deliveryAddress}."
        triggerCloudSync()
        return Result.success(order)
    }

    // =========================================================================
    // FIRESTORE CRUD OPERATIONS FOR FARMERS, VENDORS, AND MANDI MANAGERS
    // =========================================================================

    fun createFarmerProfile(farmer: FarmerProfile, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.createFarmer(farmer)
            if (result.isSuccess) {
                _userNotice.value = "Farmer profile for ${farmer.name} created successfully in Firestore."
            } else {
                _userNotice.value = "Failed to create farmer profile: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun updateFarmerProfile(farmer: FarmerProfile, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.updateFarmer(farmer)
            if (result.isSuccess) {
                _userNotice.value = "Farmer ${farmer.name} updated in Firestore."
            } else {
                _userNotice.value = "Failed to update farmer: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun deleteFarmerProfile(farmerId: String, softDelete: Boolean = true, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.deleteFarmer(farmerId, softDelete)
            if (result.isSuccess) {
                _userNotice.value = "Farmer record $farmerId removed."
            }
            onComplete(result)
        }
    }

    fun createVendorProfile(vendor: VendorProfile, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.createVendor(vendor)
            if (result.isSuccess) {
                _userNotice.value = "Vendor ${vendor.businessName} registered in Firestore."
            } else {
                _userNotice.value = "Failed to register vendor: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun updateVendorProfile(vendor: VendorProfile, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.updateVendor(vendor)
            if (result.isSuccess) {
                _userNotice.value = "Vendor ${vendor.businessName} updated."
            }
            onComplete(result)
        }
    }

    fun deleteVendorProfile(vendorId: String, softDelete: Boolean = true, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.deleteVendor(vendorId, softDelete)
            if (result.isSuccess) {
                _userNotice.value = "Vendor $vendorId deactivated."
            }
            onComplete(result)
        }
    }

    fun createMandiManagerProfile(manager: MandiManagerProfile, onComplete: (Result<String>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.createMandiManager(manager)
            if (result.isSuccess) {
                _userNotice.value = "Mandi Manager ${manager.managerName} (${manager.mandiName}) saved in Firestore."
            } else {
                _userNotice.value = "Failed to save Mandi Manager: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun updateMandiManagerProfile(manager: MandiManagerProfile, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.updateMandiManager(manager)
            if (result.isSuccess) {
                _userNotice.value = "Mandi Manager ${manager.managerName} updated."
            }
            onComplete(result)
        }
    }

    fun deleteMandiManagerProfile(managerId: String, softDelete: Boolean = true, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = firestoreRepo.deleteMandiManager(managerId, softDelete)
            if (result.isSuccess) {
                _userNotice.value = "Mandi Manager $managerId removed."
            }
            onComplete(result)
        }
    }

    // =========================================================================
    // USER REPOSITORY OPERATIONS (PROFESSION PROFILES)
    // =========================================================================

    fun saveUserProfile(profile: UserProfile, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = userRepository.saveOrUpdateUserProfile(profile)
            if (result.isSuccess) {
                _userNotice.value = "Profile saved for ${profile.fullName} as ${profile.role.roleTitle}."
            } else {
                _userNotice.value = "Failed to save profile: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun updateUserProfession(userId: String, newRole: UserRole, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = userRepository.updateUserProfession(userId, newRole)
            if (result.isSuccess) {
                _userNotice.value = "Switched profession to ${newRole.roleTitle}."
            } else {
                _userNotice.value = "Failed to switch role: ${result.exceptionOrNull()?.message}"
            }
            onComplete(result)
        }
    }

    fun fetchUserProfile(userId: String, onComplete: (UserProfile?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val profile = userRepository.getUserProfile(userId).getOrNull()
            onComplete(profile)
        }
    }
}
