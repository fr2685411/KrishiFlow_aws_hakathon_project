package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.data.model.BuyerDemandEntity
import com.example.data.model.HarvestHistoryLogEntity
import com.example.data.model.KrishiConverters
import com.example.data.model.MandiRateEntity
import com.example.data.model.OfflineInventoryQueueEntity
import com.example.data.model.OrderTransactionEntity
import com.example.data.model.ProduceBatchEntity
import com.example.data.model.SpoilagePreventionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestHistoryLogDao {
    @Query("SELECT * FROM harvest_history_logs ORDER BY saleDateTimestamp DESC")
    fun getAllLogs(): Flow<List<HarvestHistoryLogEntity>>

    @Query("SELECT * FROM harvest_history_logs WHERE cropName = :cropName ORDER BY saleDateTimestamp DESC")
    fun getLogsByCrop(cropName: String): Flow<List<HarvestHistoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HarvestHistoryLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<HarvestHistoryLogEntity>)

    @Query("DELETE FROM harvest_history_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

@Dao
interface ProduceBatchDao {
    @Query("SELECT * FROM produce_batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<ProduceBatchEntity>>

    @Query("SELECT * FROM produce_batches WHERE availableQuantityKg > 0 AND status != 'COMPLETED' AND status != 'SPOILED' ORDER BY expiryTimestamp ASC")
    fun getActiveBatches(): Flow<List<ProduceBatchEntity>>

    @Query("SELECT * FROM produce_batches WHERE id = :id")
    fun getBatchById(id: Long): Flow<ProduceBatchEntity?>

    @Query("SELECT * FROM produce_batches WHERE id = :id")
    suspend fun getBatchByIdDirect(id: Long): ProduceBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ProduceBatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(batches: List<ProduceBatchEntity>)

    @Update
    suspend fun updateBatch(batch: ProduceBatchEntity)

    @Delete
    suspend fun deleteBatch(batch: ProduceBatchEntity)
}

@Dao
interface BuyerDemandDao {
    @Query("SELECT * FROM buyer_demands ORDER BY createdAt DESC")
    fun getAllDemands(): Flow<List<BuyerDemandEntity>>

    @Query("SELECT * FROM buyer_demands WHERE status = 'OPEN' ORDER BY createdAt DESC")
    fun getOpenDemands(): Flow<List<BuyerDemandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDemand(demand: BuyerDemandEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(demands: List<BuyerDemandEntity>)

    @Update
    suspend fun updateDemand(demand: BuyerDemandEntity)

    @Delete
    suspend fun deleteDemand(demand: BuyerDemandEntity)
}

@Dao
interface OrderTransactionDao {
    @Query("SELECT * FROM order_transactions ORDER BY createdTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<OrderTransactionEntity>)

    @Update
    suspend fun updateOrder(order: OrderTransactionEntity)
}

@Dao
interface MandiRateDao {
    @Query("SELECT * FROM mandi_rates ORDER BY arrivalTonnes DESC")
    fun getAllRates(): Flow<List<MandiRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<MandiRateEntity>)
}

@Dao
interface SpoilagePreventionDao {
    @Query("SELECT * FROM spoilage_prevention_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<SpoilagePreventionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SpoilagePreventionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<SpoilagePreventionRecord>)
}

@Database(
    entities = [
        ProduceBatchEntity::class,
        BuyerDemandEntity::class,
        OrderTransactionEntity::class,
        MandiRateEntity::class,
        SpoilagePreventionRecord::class,
        HarvestHistoryLogEntity::class,
        OfflineInventoryQueueEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(KrishiConverters::class)
abstract class KrishiDatabase : RoomDatabase() {
    abstract fun harvestHistoryLogDao(): HarvestHistoryLogDao
    abstract fun produceBatchDao(): ProduceBatchDao
    abstract fun buyerDemandDao(): BuyerDemandDao
    abstract fun orderTransactionDao(): OrderTransactionDao
    abstract fun mandiRateDao(): MandiRateDao
    abstract fun spoilagePreventionDao(): SpoilagePreventionDao
    abstract fun offlineInventoryQueueDao(): OfflineInventoryQueueDao

    companion object {
        @Volatile
        private var INSTANCE: KrishiDatabase? = null

        fun getInstance(context: Context): KrishiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KrishiDatabase::class.java,
                    "krishiflow_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
