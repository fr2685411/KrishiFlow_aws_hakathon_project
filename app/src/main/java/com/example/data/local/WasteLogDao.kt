package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WasteDestination
import com.example.data.model.WasteLog
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for agricultural surplus and waste logging.
 * Returns reactive Flows for Compose UI and provides suspend methods
 * for background mutations.
 */
@Dao
interface WasteLogDao {

    @Query("SELECT * FROM waste_logs ORDER BY logDateTimestamp DESC")
    fun getAllLogs(): Flow<List<WasteLog>>

    @Query("SELECT * FROM waste_logs WHERE destination = :destination ORDER BY logDateTimestamp DESC")
    fun getLogsByDestination(destination: WasteDestination): Flow<List<WasteLog>>

    @Query("SELECT * FROM waste_logs WHERE cropName = :cropName ORDER BY logDateTimestamp DESC")
    fun getLogsByCrop(cropName: String): Flow<List<WasteLog>>

    @Query("SELECT * FROM waste_logs ORDER BY logDateTimestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 10): Flow<List<WasteLog>>

    @Query("SELECT * FROM waste_logs WHERE id = :id")
    fun getLogById(id: Long): Flow<WasteLog?>

    @Query("SELECT * FROM waste_logs WHERE id = :id")
    suspend fun getLogByIdDirect(id: Long): WasteLog?

    @Query("SELECT * FROM waste_logs WHERE logDateTimestamp BETWEEN :startTimestamp AND :endTimestamp ORDER BY logDateTimestamp DESC")
    fun getLogsBetweenDates(startTimestamp: Long, endTimestamp: Long): Flow<List<WasteLog>>

    @Query("SELECT COALESCE(SUM(quantityKg), 0.0) FROM waste_logs WHERE destination = 'DONATION'")
    fun getTotalDonatedKg(): Flow<Double>

    @Query("SELECT COALESCE(SUM(quantityKg), 0.0) FROM waste_logs WHERE destination = 'COMPOSTING'")
    fun getTotalCompostedKg(): Flow<Double>

    @Query("SELECT COALESCE(SUM(quantityKg), 0.0) FROM waste_logs")
    fun getTotalWasteLoggedKg(): Flow<Double>

    @Query("SELECT COUNT(*) FROM waste_logs")
    fun getLogCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WasteLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<WasteLog>)

    @Update
    suspend fun updateLog(log: WasteLog)

    @Delete
    suspend fun deleteLog(log: WasteLog)

    @Query("DELETE FROM waste_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM waste_logs")
    suspend fun clearAllLogs()
}
