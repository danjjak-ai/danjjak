package com.danjjak.data.L0

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sensor Data Entity (L0)
 * Stores raw hardware and software sensor readings locally.
 */
@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // GPS, ACCEL, LIGHT, APP_USAGE, BATTERY, etc.
    val value: String, // Stringified JSON or specific value
    val timestamp: Long = System.currentTimeMillis()
)
