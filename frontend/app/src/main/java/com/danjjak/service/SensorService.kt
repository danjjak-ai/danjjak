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
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Background Sensor Service (Foreground Service)
 * Continuously captures L0 data and pumps it to the Backend
 * Designed to survive background restrictions.
 */
class SensorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private val CHANNEL_ID = "SensorServiceChannel"

    override fun onCreate() {
        super.onCreate()
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
            .setContentText("백그라운드에서 센서 데이터를 안전하게 수집하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    private fun startDataCollection() {
        serviceScope.launch {
            while (isActive) {
                // Simulate capturing GPS & App Usage
                val sensorData = JSONObject().apply {
                    put("type", "GPS")
                    put("value", mapOf("lat" to 37.5665, "lng" to 126.9780)) // Seoul
                }
                
                sendDataToBackend(sensorData)
                
                delay(60000 * 5) // Every 5 minutes to conserve battery
            }
        }
    }

    private fun sendDataToBackend(data: JSONObject) {
        try {
            // In real scenario, use Retrofit or similar
            val url = URL("http://localhost:3000/api/sensor")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                
                outputStream.use { os ->
                    os.write(data.toString().toByteArray())
                }
                
                Log.d("DanjjakService", "L0 Data sent. Response: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("DanjjakService", "Failed to send L0 data", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
