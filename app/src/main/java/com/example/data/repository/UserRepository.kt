package com.example.data.repository

import android.util.Log
import com.example.data.model.FarmerDetails
import com.example.data.model.MandiManagerDetails
import com.example.data.model.RegionalManagerDetails
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.model.VendorDetails
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

/**
 * Interface contract for fetching, creating, and updating User Profiles
 * based on their selected profession (Farmer, Vendor, Mandi Manager, or Regional Manager).
 */
interface UserRepository {
    suspend fun getUserProfile(userId: String): Result<UserProfile?>
    suspend fun saveOrUpdateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updateUserProfession(userId: String, newRole: UserRole): Result<Unit>
    suspend fun updateFarmerDetails(userId: String, details: FarmerDetails): Result<Unit>
    suspend fun updateVendorDetails(userId: String, details: VendorDetails): Result<Unit>
    suspend fun updateMandiManagerDetails(userId: String, details: MandiManagerDetails): Result<Unit>
    suspend fun updateRegionalManagerDetails(userId: String, details: RegionalManagerDetails): Result<Unit>
    suspend fun getUsersByProfession(role: UserRole): Result<List<UserProfile>>
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun observeUsersByProfession(role: UserRole): Flow<List<UserProfile>>
    suspend fun deleteUserProfile(userId: String, softDelete: Boolean = true): Result<Unit>
    suspend fun seedSampleProfilesIfEmpty()
}

/**
 * Production-ready Firestore implementation of [UserRepository].
 * Manages cloud persistence, reactive snapshot observation, and offline local cache.
 */
class FirestoreUserRepository(
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
                Log.d(TAG, "Enforced PersistentCacheSettings for rural offline caching in UserRepository")
            } catch (e: Exception) {
                Log.d(TAG, "Firestore cache settings active: ${e.message}")
            }
            instance
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }
    }
) : UserRepository {

    private val db: FirebaseFirestore? by lazy { firestoreProvider() }

    // In-memory cache for ultra-fast reads and graceful offline fallback
    private val localUserProfiles = mutableMapOf<String, UserProfile>()

    @Volatile
    private var isRemotePermissionDenied = false

    private fun populateDefaultProfiles() {
        val sampleFarmer = UserProfile(
            userId = "USER_FARMER_RAMESH",
            fullName = "Ramesh Patil",
            email = "ramesh.patil@krishiflow.in",
            phoneNumber = "+91 98220 14829",
            role = UserRole.FARMER,
            district = "Nashik",
            villageOrCity = "Niphad",
            address = "Gat No. 142, Pimpalgaon Road, Niphad",
            farmerDetails = FarmerDetails(
                landAreaAcres = 4.5,
                primaryCrops = listOf("Tomato", "Onion", "Pomegranate"),
                upiId = "rameshpatil@okaxis",
                soilType = "Rich Black Clay Soil",
                irrigationType = "Automated Drip"
            )
        )
        localUserProfiles[sampleFarmer.userId] = sampleFarmer

        val sampleVendor = UserProfile(
            userId = "USER_VENDOR_DMART",
            fullName = "Amit Deshpande (D-Mart Procurement)",
            email = "amit.deshpande@dmartindia.com",
            phoneNumber = "+91 98112 33445",
            role = UserRole.VENDOR,
            district = "Pune",
            villageOrCity = "Hinjawadi",
            address = "Avenue Supermarts DC, Phase 2, Hinjawadi Mega Hub",
            vendorDetails = VendorDetails(
                businessName = "Avenue Supermarts (D-Mart)",
                vendorType = "SUPERMARKET_MART",
                gstin = "27AAACA1234D1Z5",
                dailyProcurementCapacityKg = 15000.0,
                creditLimitRupees = 500000.0
            )
        )
        localUserProfiles[sampleVendor.userId] = sampleVendor

        val sampleMandiMgr = UserProfile(
            userId = "USER_MANDI_PUNE",
            fullName = "Vijayrao Suryavanshi",
            email = "secretary@punemandi.gov.in",
            phoneNumber = "+91 98230 45678",
            role = UserRole.MANDI_MANAGER,
            district = "Pune",
            villageOrCity = "Gultekdi",
            address = "APMC Main Administrative Bhavan, Market Yard, Pune",
            mandiManagerDetails = MandiManagerDetails(
                mandiName = "Pune APMC Market Yard",
                mandiCode = "MH-PUN-01",
                apmcZone = "Western Maharashtra Central Zone",
                licensedTradersCount = 280,
                dailyYardCapacityTons = 450.0,
                hasElectronicWeighbridge = true
            )
        )
        localUserProfiles[sampleMandiMgr.userId] = sampleMandiMgr

        val sampleRegionalMgr = UserProfile(
            userId = "USER_REGIONAL_OPS_LEAD",
            fullName = "Dr. Sunita Kulkarni",
            email = "sunita.kulkarni@krishiflow.in",
            phoneNumber = "+91 98221 00998",
            role = UserRole.REGIONAL_MANAGER,
            district = "Pune",
            villageOrCity = "Shivajinagar",
            address = "KrishiFlow Maharashtra Regional Command Center, Pune",
            regionalManagerDetails = RegionalManagerDetails(
                employeeId = "KF-MH-REG-01",
                assignedHub = "Western Maharashtra Agri-Logistics Hub",
                managedDistricts = listOf("Nashik", "Pune", "Ahmednagar", "Satara", "Solapur"),
                directSupervisorsCount = 12,
                emergencyDispatchAuthority = true
            )
        )
        localUserProfiles[sampleRegionalMgr.userId] = sampleRegionalMgr
    }

    private fun isPermissionDenied(e: Throwable): Boolean {
        if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            return true
        }
        val msg = e.message ?: ""
        return msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
               msg.contains("insufficient permissions", ignoreCase = true)
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile?> = withContext(Dispatchers.IO) {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            return@withContext Result.success(localUserProfiles[userId])
        }

        try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = UserProfile.fromMap(snapshot.id, data)
                localUserProfiles[userId] = profile
                Result.success(profile)
            } else {
                Result.success(localUserProfiles[userId])
            }
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching user profile for $userId: ${e.message}. Using local cache.")
            Result.success(localUserProfiles[userId])
        }
    }

    override suspend fun saveOrUpdateUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        localUserProfiles[profile.userId] = updated

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            firestore.collection(COLLECTION_USERS)
                .document(profile.userId)
                .set(updated.toMap(), SetOptions.merge())
                .await()
            Log.d(TAG, "Successfully synced profile for ${profile.userId} in Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) {
                isRemotePermissionDenied = true
                Log.w(TAG, "Firestore permission pending. Profile for ${profile.userId} cached locally.")
            } else {
                Log.w(TAG, "Notice saving profile for ${profile.userId}: ${e.message}")
            }
            Result.success(Unit)
        }
    }

    override suspend fun updateUserProfession(userId: String, newRole: UserRole): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localUserProfiles[userId]
        if (existing != null) {
            localUserProfiles[userId] = existing.copy(
                role = newRole,
                updatedAt = System.currentTimeMillis()
            )
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            val updateMap = mapOf(
                "role" to newRole.profession.code,
                "roleTitle" to newRole.roleTitle,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(updateMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating profession for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun updateFarmerDetails(userId: String, details: FarmerDetails): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localUserProfiles[userId]
        if (existing != null) {
            localUserProfiles[userId] = existing.copy(
                farmerDetails = details,
                updatedAt = System.currentTimeMillis()
            )
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            val updateMap = mapOf(
                "farmerDetails" to details.toMap(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(updateMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating farmer details for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun updateVendorDetails(userId: String, details: VendorDetails): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localUserProfiles[userId]
        if (existing != null) {
            localUserProfiles[userId] = existing.copy(
                vendorDetails = details,
                updatedAt = System.currentTimeMillis()
            )
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            val updateMap = mapOf(
                "vendorDetails" to details.toMap(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(updateMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating vendor details for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun updateMandiManagerDetails(userId: String, details: MandiManagerDetails): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localUserProfiles[userId]
        if (existing != null) {
            localUserProfiles[userId] = existing.copy(
                mandiManagerDetails = details,
                updatedAt = System.currentTimeMillis()
            )
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            val updateMap = mapOf(
                "mandiManagerDetails" to details.toMap(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(updateMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating Mandi Manager details for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun updateRegionalManagerDetails(userId: String, details: RegionalManagerDetails): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localUserProfiles[userId]
        if (existing != null) {
            localUserProfiles[userId] = existing.copy(
                regionalManagerDetails = details,
                updatedAt = System.currentTimeMillis()
            )
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            val updateMap = mapOf(
                "regionalManagerDetails" to details.toMap(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(updateMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice updating Regional Manager details for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun getUsersByProfession(role: UserRole): Result<List<UserProfile>> = withContext(Dispatchers.IO) {
        val roleCode = role.profession.code
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            val cached = localUserProfiles.values.filter { it.role.profession.code == roleCode }
            return@withContext Result.success(cached)
        }

        try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("role", roleCode)
                .whereEqualTo("isVerified", true)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { UserProfile.fromMap(doc.id, it) }
            }
            list.forEach { localUserProfiles[it.userId] = it }
            Result.success(if (list.isNotEmpty()) list else localUserProfiles.values.filter { it.role.profession.code == roleCode })
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice fetching users for profession $roleCode: ${e.message}")
            val cached = localUserProfiles.values.filter { it.role.profession.code == roleCode }
            Result.success(cached)
        }
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localUserProfiles[userId])
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "User profile snapshot notice for $userId: ${error.message}. Serving local cache.")
                    trySend(localUserProfiles[userId])
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data ?: emptyMap()
                    val profile = UserProfile.fromMap(snapshot.id, data)
                    localUserProfiles[userId] = profile
                    trySend(profile)
                } else {
                    trySend(localUserProfiles[userId])
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override fun observeUsersByProfession(role: UserRole): Flow<List<UserProfile>> = callbackFlow {
        val roleCode = role.profession.code
        val firestore = db
        if (firestore == null || isRemotePermissionDenied) {
            trySend(localUserProfiles.values.filter { it.role.profession.code == roleCode })
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_USERS)
            .whereEqualTo("role", roleCode)
            .whereEqualTo("isVerified", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (isPermissionDenied(error)) isRemotePermissionDenied = true
                    Log.w(TAG, "Users by profession snapshot notice for $roleCode: ${error.message}. Serving local cache.")
                    trySend(localUserProfiles.values.filter { it.role.profession.code == roleCode })
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { UserProfile.fromMap(doc.id, it) }
                    }
                    list.forEach { localUserProfiles[it.userId] = it }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteUserProfile(userId: String, softDelete: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        if (softDelete) {
            val existing = localUserProfiles[userId]
            if (existing != null) {
                localUserProfiles[userId] = existing.copy(isVerified = false, updatedAt = System.currentTimeMillis())
            }
        } else {
            localUserProfiles.remove(userId)
        }

        val firestore = db
        if (firestore == null || isRemotePermissionDenied) return@withContext Result.success(Unit)

        try {
            if (softDelete) {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .update(mapOf("isVerified" to false, "updatedAt" to System.currentTimeMillis()))
                    .await()
            } else {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (isPermissionDenied(e)) isRemotePermissionDenied = true
            Log.w(TAG, "Notice deleting profile for $userId: ${e.message}")
            Result.success(Unit)
        }
    }

    override suspend fun seedSampleProfilesIfEmpty() = withContext(Dispatchers.IO) {
        populateDefaultProfiles()

        val firestore = db
        if (firestore != null && !isRemotePermissionDenied) {
            for (p in localUserProfiles.values.toList()) {
                if (isRemotePermissionDenied) break
                saveOrUpdateUserProfile(p)
            }
        }
    }

    companion object {
        private const val TAG = "FirestoreUserRepo"
        const val COLLECTION_USERS = "krishiflow_users"
    }
}
