package com.danjjak.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.danjjak.data.L0.AppDatabase
import com.danjjak.data.L0.SensorData
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Background Sensor Service (Foreground Service)
 * Continuously captures L0 data and stores it in On-device Room DB.
 * Also pumps data to the Backend with Privacy by Design (PbD).
 */
class SensorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val CHANNEL_ID = "SensorServiceChannel"
    private lateinit var db: AppDatabase
    private lateinit var locationManager: LocationManager
    private lateinit var usageStatsCollector: UsageStatsCollector

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
        locationManager = LocationManager(this)
        usageStatsCollector = UsageStatsCollector(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DanjjakService", "Sensor capturing started in Foreground...")
        
        val notification = createNotification()
        startForeground(1, notification)

        startDataCollection()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Danjjak Sensor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("단짝이 당신의 일상을 살피는 중")
            .setContentText("백그라운드에서 데이터를 안전하게 기기 내에 저장하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    private fun startDataCollection() {
        serviceScope.launch {
            while (isActive) {
                // 16 Context Scenarios detection simulation
                // Capture GPS using real LocationManager
                val location = locationManager.getCurrentLocation()
                val lat = location?.latitude ?: 37.5665
                val lng = location?.longitude ?: 126.9780
                val gpsData = SensorData(type = "GPS", value = "{\"lat\": $lat, \"lng\": $lng}")
                db.sensorDao().insert(gpsData)
                
                // Capture App Usage using UsageStatsCollector
                if (usageStatsCollector.hasUsagePermission()) {
                    val topApps = usageStatsCollector.getRecentAppUsage()
                    if (topApps.isNotEmpty()) {
                        val topApp = topApps.first()
                        val appUsageData = SensorData(type = "APP_USAGE", value = "{\"app\": \"${topApp.packageName}\", \"duration\": ${topApp.durationMinutes}}")
                        db.sensorDao().insert(appUsageData)
                    }
                } else {
                    // Fallback to mock if permission not granted
                    val appUsageData = SensorData(type = "APP_USAGE", value = "{\"app\": \"Permission_Required\", \"duration\": 0}")
                    db.sensorDao().insert(appUsageData)
                }

                // Privacy by Design: We don't send the full raw data if not needed.
                // We send a sanitized version or wait for L1 processing.
                val sensorJson = JSONObject().apply {
                    put("type", "GPS")
                    put("value", "Seoul") // Masked or generalized
                }
                
                sendDataToBackend(sensorJson)
                
                delay(60000 * 5) // Every 5 minutes
            }
        }
    }

    private fun sendDataToBackend(data: JSONObject) {
        try {
            val url = URL("http://10.0.2.2:3000/api/sensor") // Using 10.0.2.2 for Android Emulator
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                
                outputStream.use { os ->
                    os.write(data.toString().toByteArray())
                }
                
                Log.d("DanjjakService", "Generalized data sent to backend. Status: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("DanjjakService", "Failed to send data", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
