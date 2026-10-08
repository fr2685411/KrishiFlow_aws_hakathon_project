package com.example.data.repository

import android.util.Log
import com.example.data.model.FarmerProducePost
import com.example.data.model.FarmerProfile
import com.example.data.model.MandiManagerProfile
import com.example.data.model.VendorProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository providing complete CRUD operations for Farmers, Commercial Vendors,
 * and APMC Mandi Managers using Cloud Firestore with resilient offline/local-first caching.
 */
class KrishiFirestoreRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            val instance = FirebaseFirestore.getInstance()
            try {
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder().build()
                    )
                    .build()
                instance.firestoreSettings = settings
                Log.d(TAG, "Enforced PersistentCacheSettings for rural offline caching")
            } catch (e: Exception) {
                Log.d(TAG, "Firestore cache settings active: ${e.message}")
            }
            instance
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }
    }
) {
    private val db: FirebaseFirestore? by lazy { firestoreProvider() }

    // In-memory fallback caches for seamless operation during offline / pending credentials
    private val localFarmers = mutableMapOf<String, FarmerProfile>()
    private val localVendors = mutableMapOf<String, VendorProfile>()
    private val localManagers = mutableMapOf<String, MandiManagerProfile>()
    private val localProducePosts = mutableMapOf<String, FarmerProducePost>()

    // Track if remote permission is denied to prevent noisy repeated write failures
    @Volatile
    private var isRemotePermissionDenied = false

    private fun isPermissionDenied(e: Throwable): Boolean {
        if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            return true
        }
        val msg = e.message ?: ""
        return msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
               msg.contains("insufficient permissions", ignoreCase = true)
    }

    // =========================================================================
    // FARMER CRUD OPERATIONS
    // =========================================================================

    /**
     * Create a new Farmer profile in Firestore.
     * @return Generated or existing farmer ID.
     */
    suspend fun createFarmer(farmer: FarmerProfile): Result<String> = withContext(Dispatchers.IO) {
        val targetId = if (farmer.id.isNotBlank()) farmer.id else "FARMER_${UUID.randomUUID().toString().take(8).uppercase()}"
        val record = farmer.copy(
            id = targetId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        localFarmers[targetId] = record
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(targetId)
        }

        try {
            firestore.collection(COLLECTION_FARMERS)
                .document(targetId)
                .set(record.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced farmer $targetId in Firestore")
            Result.success(targetId)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                isRemotePermissionDenied = true
                Log.w(TAG, "Firestore permission pending (PERMISSION_DENIED). Farmer $targetId cached locally.")
            } else {
                Log.w(TAG, "Firestore write notice for farmer $targetId: ${e.message}")
            }
            Result.success(targetId)
        }
    }

    /**
     * Retrieve a Farmer by unique ID.
     */
    suspend fun getFarmer(farmerId: String): Result<FarmerProfile?> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localFarmers[farmerId])
        }

        try {
            val snapshot = firestore.collection(COLLECTION_FARMERS)
                .document(farmerId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = FarmerProfile.fromMap(snapshot.id, data)
                localFarmers[farmerId] = profile
                Result.success(profile)
            } else {
                Result.success(localFarmers[farmerId])
            }
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching farmer $farmerId: ${e.message}. Using local cache.")
            Result.success(localFarmers[farmerId])
        }
    }

    /**
     * Read all active Farmers.
     */
    suspend fun getAllFarmers(): Result<List<FarmerProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localFarmers.values.toList())
        }

        try {
            val snapshot = firestore.collection(COLLECTION_FARMERS)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FarmerProfile.fromMap(doc.id, it) }
            }
            list.forEach { localFarmers[it.id] = it }
            Result.success(if (list.isNotEmpty()) list else localFarmers.values.toList())
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching farmers from Firestore: ${e.message}. Using local cache.")
            Result.success(localFarmers.values.toList())
        }
    }

    /**
     * Filter Farmers by District.
     */
    suspend fun getFarmersByDistrict(district: String): Result<List<FarmerProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localFarmers.values.filter { it.district.equals(district, ignoreCase = true) })
        }

        try {
            val snapshot = firestore.collection(COLLECTION_FARMERS)
                .whereEqualTo("district", district)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FarmerProfile.fromMap(doc.id, it) }
            }
            Result.success(list)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice querying farmers for district $district: ${e.message}")
            Result.success(localFarmers.values.filter { it.district.equals(district, ignoreCase = true) })
        }
    }

    /**
     * Update an existing Farmer profile.
     */
    suspend fun updateFarmer(farmer: FarmerProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val updated = farmer.copy(updatedAt = System.currentTimeMillis())
        localFarmers[farmer.id] = updated

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_FARMERS)
                .document(farmer.id)
                .set(updated.toMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating farmer ${farmer.id}: ${e.message}. Cached locally.")
            Result.success(Unit)
        }
    }

    /**
     * Soft delete / deactivate a Farmer profile.
     */
    suspend fun deleteFarmer(farmerId: String, softDelete: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (softDelete) {
            val existing = localFarmers[farmerId]
            if (existing != null) {
                localFarmers[farmerId] = existing.copy(isActive = false, updatedAt = System.currentTimeMillis())
            }
        } else {
            localFarmers.remove(farmerId)
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            if (softDelete) {
                firestore.collection(COLLECTION_FARMERS)
                    .document(farmerId)
                    .update(mapOf("isActive" to false, "updatedAt" to System.currentTimeMillis()))
                    .await()
            } else {
                firestore.collection(COLLECTION_FARMERS)
                    .document(farmerId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice deleting farmer $farmerId: ${e.message}. Updated locally.")
            Result.success(Unit)
        }
    }

    /**
     * Real-time reactive flow observing Farmers collection changes.
     */
    fun observeFarmers(): Flow<List<FarmerProfile>> = callbackFlow {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localFarmers.values.toList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_FARMERS)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "Farmers snapshot listener: ${error.message}. Serving local cache.")
                    trySend(localFarmers.values.toList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { FarmerProfile.fromMap(doc.id, it) }
                    }
                    list.forEach { localFarmers[it.id] = it }
                    trySend(if (list.isNotEmpty()) list else localFarmers.values.toList())
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // VENDOR / BUYER CRUD OPERATIONS
    // =========================================================================

    /**
     * Create a new Vendor profile in Firestore.
     */
    suspend fun createVendor(vendor: VendorProfile): Result<String> = withContext(Dispatchers.IO) {
        val targetId = if (vendor.id.isNotBlank()) vendor.id else "VENDOR_${UUID.randomUUID().toString().take(8).uppercase()}"
        val record = vendor.copy(
            id = targetId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        localVendors[targetId] = record
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(targetId)
        }

        try {
            firestore.collection(COLLECTION_VENDORS)
                .document(targetId)
                .set(record.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced vendor $targetId in Firestore")
            Result.success(targetId)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                isRemotePermissionDenied = true
                Log.w(TAG, "Firestore permission pending (PERMISSION_DENIED). Vendor $targetId cached locally.")
            } else {
                Log.w(TAG, "Firestore write notice for vendor $targetId: ${e.message}")
            }
            Result.success(targetId)
        }
    }

    /**
     * Retrieve a Vendor by unique ID.
     */
    suspend fun getVendor(vendorId: String): Result<VendorProfile?> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localVendors[vendorId])
        }

        try {
            val snapshot = firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = VendorProfile.fromMap(snapshot.id, data)
                localVendors[vendorId] = profile
                Result.success(profile)
            } else {
                Result.success(localVendors[vendorId])
            }
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching vendor $vendorId: ${e.message}. Using local cache.")
            Result.success(localVendors[vendorId])
        }
    }

    /**
     * Read all active Commercial Vendors.
     */
    suspend fun getAllVendors(): Result<List<VendorProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localVendors.values.toList())
        }

        try {
            val snapshot = firestore.collection(COLLECTION_VENDORS)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { VendorProfile.fromMap(doc.id, it) }
            }
            list.forEach { localVendors[it.id] = it }
            Result.success(if (list.isNotEmpty()) list else localVendors.values.toList())
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching vendors from Firestore: ${e.message}. Using local cache.")
            Result.success(localVendors.values.toList())
        }
    }

    /**
     * Query Vendors by Type (e.g. SUPERMARKET_MART, MANDI_WHOLESALER).
     */
    suspend fun getVendorsByType(vendorType: String): Result<List<VendorProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localVendors.values.filter { it.vendorType.equals(vendorType, ignoreCase = true) })
        }

        try {
            val snapshot = firestore.collection(COLLECTION_VENDORS)
                .whereEqualTo("vendorType", vendorType)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { VendorProfile.fromMap(doc.id, it) }
            }
            Result.success(list)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice querying vendors for type $vendorType: ${e.message}")
            Result.success(localVendors.values.filter { it.vendorType.equals(vendorType, ignoreCase = true) })
        }
    }

    /**
     * Update an existing Vendor profile.
     */
    suspend fun updateVendor(vendor: VendorProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val updated = vendor.copy(updatedAt = System.currentTimeMillis())
        localVendors[vendor.id] = updated

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_VENDORS)
                .document(vendor.id)
                .set(updated.toMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating vendor ${vendor.id}: ${e.message}. Cached locally.")
            Result.success(Unit)
        }
    }

    /**
     * Soft delete / deactivate a Vendor profile.
     */
    suspend fun deleteVendor(vendorId: String, softDelete: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (softDelete) {
            val existing = localVendors[vendorId]
            if (existing != null) {
                localVendors[vendorId] = existing.copy(isActive = false, updatedAt = System.currentTimeMillis())
            }
        } else {
            localVendors.remove(vendorId)
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            if (softDelete) {
                firestore.collection(COLLECTION_VENDORS)
                    .document(vendorId)
                    .update(mapOf("isActive" to false, "updatedAt" to System.currentTimeMillis()))
                    .await()
            } else {
                firestore.collection(COLLECTION_VENDORS)
                    .document(vendorId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice deleting vendor $vendorId: ${e.message}. Updated locally.")
            Result.success(Unit)
        }
    }

    /**
     * Real-time reactive flow observing Vendors collection changes.
     */
    fun observeVendors(): Flow<List<VendorProfile>> = callbackFlow {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localVendors.values.toList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_VENDORS)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "Vendors snapshot listener: ${error.message}. Serving local cache.")
                    trySend(localVendors.values.toList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { VendorProfile.fromMap(doc.id, it) }
                    }
                    list.forEach { localVendors[it.id] = it }
                    trySend(if (list.isNotEmpty()) list else localVendors.values.toList())
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // MANDI MANAGER CRUD OPERATIONS
    // =========================================================================

    /**
     * Create a new APMC Mandi Manager profile in Firestore.
     */
    suspend fun createMandiManager(manager: MandiManagerProfile): Result<String> = withContext(Dispatchers.IO) {
        val targetId = if (manager.id.isNotBlank()) manager.id else "MANDI_${UUID.randomUUID().toString().take(8).uppercase()}"
        val record = manager.copy(
            id = targetId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        localManagers[targetId] = record
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(targetId)
        }

        try {
            firestore.collection(COLLECTION_MANDI_MANAGERS)
                .document(targetId)
                .set(record.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced Mandi Manager $targetId in Firestore")
            Result.success(targetId)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                isRemotePermissionDenied = true
                Log.w(TAG, "Firestore permission pending (PERMISSION_DENIED). Manager $targetId cached locally.")
            } else {
                Log.w(TAG, "Firestore write notice for manager $targetId: ${e.message}")
            }
            Result.success(targetId)
        }
    }

    /**
     * Retrieve an APMC Mandi Manager by unique ID.
     */
    suspend fun getMandiManager(managerId: String): Result<MandiManagerProfile?> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localManagers[managerId])
        }

        try {
            val snapshot = firestore.collection(COLLECTION_MANDI_MANAGERS)
                .document(managerId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = MandiManagerProfile.fromMap(snapshot.id, data)
                localManagers[managerId] = profile
                Result.success(profile)
            } else {
                Result.success(localManagers[managerId])
            }
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching Mandi Manager $managerId: ${e.message}. Using local cache.")
            Result.success(localManagers[managerId])
        }
    }

    /**
     * Retrieve an APMC Mandi Manager by Mandi Name.
     */
    suspend fun getMandiManagerByMandiName(mandiName: String): Result<MandiManagerProfile?> = withContext(Dispatchers.IO) {
        val local = localManagers.values.firstOrNull { it.mandiName.equals(mandiName, ignoreCase = true) }
        if (local != null) return@withContext Result.success(local)

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(local)
        }

        try {
            val snapshot = firestore.collection(COLLECTION_MANDI_MANAGERS)
                .whereEqualTo("mandiName", mandiName)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val doc = snapshot.documents.firstOrNull()
            val profile = doc?.data?.let { MandiManagerProfile.fromMap(doc.id, it) }
            if (profile != null) localManagers[profile.id] = profile
            Result.success(profile ?: local)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice querying Mandi Manager by name $mandiName: ${e.message}")
            Result.success(local)
        }
    }

    /**
     * Read all active APMC Mandi Managers.
     */
    suspend fun getAllMandiManagers(): Result<List<MandiManagerProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localManagers.values.toList())
        }

        try {
            val snapshot = firestore.collection(COLLECTION_MANDI_MANAGERS)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { MandiManagerProfile.fromMap(doc.id, it) }
            }
            list.forEach { localManagers[it.id] = it }
            Result.success(if (list.isNotEmpty()) list else localManagers.values.toList())
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching Mandi Managers from Firestore: ${e.message}. Using local cache.")
            Result.success(localManagers.values.toList())
        }
    }

    /**
     * Query Mandi Managers by District.
     */
    suspend fun getMandiManagersByDistrict(district: String): Result<List<MandiManagerProfile>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localManagers.values.filter { it.district.equals(district, ignoreCase = true) })
        }

        try {
            val snapshot = firestore.collection(COLLECTION_MANDI_MANAGERS)
                .whereEqualTo("district", district)
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { MandiManagerProfile.fromMap(doc.id, it) }
            }
            Result.success(list)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice querying Mandi Managers for district $district: ${e.message}")
            Result.success(localManagers.values.filter { it.district.equals(district, ignoreCase = true) })
        }
    }

    /**
     * Update an existing Mandi Manager profile.
     */
    suspend fun updateMandiManager(manager: MandiManagerProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val updated = manager.copy(updatedAt = System.currentTimeMillis())
        localManagers[manager.id] = updated

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_MANDI_MANAGERS)
                .document(manager.id)
                .set(updated.toMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating Mandi Manager ${manager.id}: ${e.message}. Cached locally.")
            Result.success(Unit)
        }
    }

    /**
     * Soft delete / deactivate an APMC Mandi Manager profile.
     */
    suspend fun deleteMandiManager(managerId: String, softDelete: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        if (softDelete) {
            val existing = localManagers[managerId]
            if (existing != null) {
                localManagers[managerId] = existing.copy(isActive = false, updatedAt = System.currentTimeMillis())
            }
        } else {
            localManagers.remove(managerId)
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            if (softDelete) {
                firestore.collection(COLLECTION_MANDI_MANAGERS)
                    .document(managerId)
                    .update(mapOf("isActive" to false, "updatedAt" to System.currentTimeMillis()))
                    .await()
            } else {
                firestore.collection(COLLECTION_MANDI_MANAGERS)
                    .document(managerId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice deleting Mandi Manager $managerId: ${e.message}. Updated locally.")
            Result.success(Unit)
        }
    }

    /**
     * Real-time reactive flow observing Mandi Managers collection changes.
     */
    fun observeMandiManagers(): Flow<List<MandiManagerProfile>> = callbackFlow {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localManagers.values.toList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_MANDI_MANAGERS)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "Mandi Managers snapshot listener: ${error.message}. Serving local cache.")
                    trySend(localManagers.values.toList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { MandiManagerProfile.fromMap(doc.id, it) }
                    }
                    list.forEach { localManagers[it.id] = it }
                    trySend(if (list.isNotEmpty()) list else localManagers.values.toList())
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    // =========================================================================
    // SEED INITIAL SAMPLE PROFILES
    // =========================================================================

    /**
     * Seeds initial verified actors for Farmers, Supermarket Marts/Vendors,
     * and APMC Mandi Managers if collections are newly provisioned and accessible.
     */
    suspend fun seedInitialActorsIfEmpty() = withContext(Dispatchers.IO) {
        val defaultFarmers = listOf(
            FarmerProfile(
                id = "FARMER_RAMESH_PATIL",
                name = "Ramesh Patil",
                phone = "+91 98220 14829",
                village = "Niphad",
                district = "Nashik",
                state = "Maharashtra",
                landAreaAcres = 4.5,
                primaryCrops = listOf("Tomato", "Onion", "Pomegranate"),
                upiId = "rameshpatil@okaxis",
                rating = 4.9
            ),
            FarmerProfile(
                id = "FARMER_SUNITA_MORE",
                name = "Sunita More",
                phone = "+91 94231 88921",
                village = "Malegaon Khurd",
                district = "Baramati",
                state = "Maharashtra",
                landAreaAcres = 6.0,
                primaryCrops = listOf("Potato", "Green Chilli", "Sweet Corn"),
                upiId = "sunitamore@okhdfcbank",
                rating = 4.8
            ),
            FarmerProfile(
                id = "FARMER_BABURAO_KALE",
                name = "Baburao Kale",
                phone = "+91 97654 32110",
                village = "Dindori",
                district = "Nashik",
                state = "Maharashtra",
                landAreaAcres = 3.2,
                primaryCrops = listOf("Cauliflower", "Cabbage", "Spinach"),
                upiId = "baburaokale@ybl",
                rating = 4.7
            )
        )

        val defaultVendors = listOf(
            VendorProfile(
                id = "VENDOR_DMART_PUNE",
                businessName = "Avenue Supermarts (D-Mart)",
                contactPerson = "Amit Deshpande",
                phone = "+91 98112 33445",
                email = "procurement.pune@dmartindia.com",
                vendorType = "SUPERMARKET_MART",
                destinationLocation = "Hinjawadi Phase 2 Mega Hub",
                city = "Pune",
                state = "Maharashtra",
                gstin = "27AAACA1234D1Z5",
                creditLimitRupees = 500000.0
            ),
            VendorProfile(
                id = "VENDOR_SHREE_GANESH_SABZI",
                businessName = "Shree Ganesh Sabzi Bhandar",
                contactPerson = "Vikas Gupta",
                phone = "+91 99201 55678",
                email = "vikas.gupta.mandi@gmail.com",
                vendorType = "MANDI_WHOLESALER",
                destinationLocation = "Gultekdi Market Yard, Pune",
                city = "Pune",
                state = "Maharashtra",
                gstin = "27BBBCB5678E1Z9",
                creditLimitRupees = 150000.0
            ),
            VendorProfile(
                id = "VENDOR_FRESH_CART",
                businessName = "Krishi Fresh Cart Network",
                contactPerson = "Pooja Jadhav",
                phone = "+91 98334 77123",
                email = "pooja@freshcart.in",
                vendorType = "LOCAL_VENDOR",
                destinationLocation = "Kothrud Depot",
                city = "Pune",
                state = "Maharashtra",
                creditLimitRupees = 40000.0
            )
        )

        val defaultManagers = listOf(
            MandiManagerProfile(
                id = "MANDI_MGR_PUNE",
                managerName = "Vijayrao Suryavanshi",
                phone = "+91 98230 45678",
                email = "secretary@punemandi.gov.in",
                mandiName = "Pune APMC Market Yard",
                mandiCode = "MH-PUN-01",
                district = "Pune",
                state = "Maharashtra",
                licensedTradersCount = 280,
                dailyCapacityTons = 450.0
            ),
            MandiManagerProfile(
                id = "MANDI_MGR_NASHIK",
                managerName = "Kailash Sonawane",
                phone = "+91 98224 89012",
                email = "incharge@nashikmandi.gov.in",
                mandiName = "Nashik APMC Mandi",
                mandiCode = "MH-NSK-02",
                district = "Nashik",
                state = "Maharashtra",
                licensedTradersCount = 340,
                dailyCapacityTons = 620.0
            ),
            MandiManagerProfile(
                id = "MANDI_MGR_VASHI",
                managerName = "Dr. Rajendra Jagtap",
                phone = "+91 98199 11223",
                email = "commissioner@vashimarket.gov.in",
                mandiName = "Vashi APMC (Navi Mumbai)",
                mandiCode = "MH-MUM-01",
                district = "Thane",
                state = "Maharashtra",
                licensedTradersCount = 750,
                dailyCapacityTons = 1200.0
            )
        )

        // Seed in local in-memory store
        defaultFarmers.forEach { localFarmers[it.id] = it }
        defaultVendors.forEach { localVendors[it.id] = it }
        defaultManagers.forEach { localManagers[it.id] = it }

        // Attempt remote write if Firestore is active and permissions are valid
        val firestore = db
        if (firestore != null && !isRemotePermissionDenied) {
            try {
                for (f in defaultFarmers) {
                    if (isRemotePermissionDenied) break
                    createFarmer(f)
                }
                for (v in defaultVendors) {
                    if (isRemotePermissionDenied) break
                    createVendor(v)
                }
                for (m in defaultManagers) {
                    if (isRemotePermissionDenied) break
                    createMandiManager(m)
                }
            } catch (e: Exception) {
                if (isPermissionDenied(e)) isRemotePermissionDenied = true
                Log.w(TAG, "Firestore seed notice: ${e.message}")
            }
        }
    }

    // =========================================================================
    // FARMER PRODUCE POST CRUD OPERATIONS (FIRESTORE)
    // =========================================================================

    /**
     * Create or publish a new agricultural produce listing to Cloud Firestore.
     */
    suspend fun createProducePost(post: FarmerProducePost): Result<String> = withContext(Dispatchers.IO) {
        val targetId = if (post.postId.isNotBlank()) post.postId else "PRODUCE_${UUID.randomUUID().toString().take(8).uppercase()}"
        val record = post.copy(
            postId = targetId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        localProducePosts[targetId] = record
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(targetId)
        }

        try {
            firestore.collection(COLLECTION_PRODUCE_POSTS)
                .document(targetId)
                .set(record.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully saved produce post $targetId to Cloud Firestore")
            Result.success(targetId)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                isRemotePermissionDenied = true
                Log.w(TAG, "Firestore permission notice for produce post $targetId: ${e.message}. Saved locally.")
            } else {
                Log.w(TAG, "Firestore produce write notice: ${e.message}")
            }
            Result.success(targetId)
        }
    }

    suspend fun getProducePost(postId: String): Result<FarmerProducePost?> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localProducePosts[postId])
        }

        try {
            val snapshot = firestore.collection(COLLECTION_PRODUCE_POSTS)
                .document(postId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = FarmerProducePost.fromMap(snapshot.id, data)
                localProducePosts[postId] = profile
                Result.success(profile)
            } else {
                Result.success(localProducePosts[postId])
            }
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching produce post $postId: ${e.message}")
            Result.success(localProducePosts[postId])
        }
    }

    suspend fun getAllProducePosts(): Result<List<FarmerProducePost>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localProducePosts.values.toList())
        }

        try {
            val snapshot = firestore.collection(COLLECTION_PRODUCE_POSTS)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FarmerProducePost.fromMap(doc.id, it) }
            }
            list.forEach { localProducePosts[it.postId] = it }
            Result.success(if (list.isNotEmpty()) list else localProducePosts.values.toList())
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching produce posts from Firestore: ${e.message}")
            Result.success(localProducePosts.values.toList())
        }
    }

    suspend fun getProducePostsByFarmer(farmerId: String): Result<List<FarmerProducePost>> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localProducePosts.values.filter { it.farmerId == farmerId })
        }

        try {
            val snapshot = firestore.collection(COLLECTION_PRODUCE_POSTS)
                .whereEqualTo("farmerId", farmerId)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FarmerProducePost.fromMap(doc.id, it) }
            }
            list.forEach { localProducePosts[it.postId] = it }
            Result.success(list)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching produce posts for farmer $farmerId: ${e.message}")
            Result.success(localProducePosts.values.filter { it.farmerId == farmerId })
        }
    }

    fun observeProducePosts(): Flow<List<FarmerProducePost>> = callbackFlow {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localProducePosts.values.toList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_PRODUCE_POSTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "Produce posts snapshot listener: ${error.message}. Serving local cache.")
                    trySend(localProducePosts.values.toList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { FarmerProducePost.fromMap(doc.id, it) }
                    }
                    list.forEach { localProducePosts[it.postId] = it }
                    trySend(if (list.isNotEmpty()) list else localProducePosts.values.toList())
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun updateProducePost(post: FarmerProducePost): Result<Unit> = withContext(Dispatchers.IO) {
        val updated = post.copy(updatedAt = System.currentTimeMillis())
        localProducePosts[post.postId] = updated

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_PRODUCE_POSTS)
                .document(post.postId)
                .set(updated.toMap(), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating produce post ${post.postId}: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun deleteProducePost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        localProducePosts.remove(postId)
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_PRODUCE_POSTS)
                .document(postId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice deleting produce post $postId: ${e.message}")
            Result.success(Unit)
        }
    }

    companion object {
        private const val TAG = "KrishiFirestoreRepo"
        const val COLLECTION_FARMERS = "krishiflow_farmers"
        const val COLLECTION_VENDORS = "krishiflow_vendors"
        const val COLLECTION_MANDI_MANAGERS = "krishiflow_mandi_managers"
        const val COLLECTION_PRODUCE_POSTS = "krishiflow_produce_posts"
    }
}
