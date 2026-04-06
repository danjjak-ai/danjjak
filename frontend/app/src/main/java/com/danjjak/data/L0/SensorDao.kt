package com.danjjak.data.L0

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDao {
    @Insert
    suspend fun insert(data: SensorData)

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT 100")
    fun getRecentData(): Flow<List<SensorData>>

    @Query("SELECT EXISTS(SELECT 1 FROM sensor_data WHERE type = 'MEAL' AND timestamp >= :startTime AND timestamp <= :endTime)")
    suspend fun hasRecentMeal(startTime: Long, endTime: Long): Boolean

    @Query("SELECT * FROM sensor_data WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getEntriesByDateRange(startTime: Long, endTime: Long): Flow<List<SensorData>>

    @Query("DELETE FROM sensor_data")
    suspend fun deleteAll()
}
