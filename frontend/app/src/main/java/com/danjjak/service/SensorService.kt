package com.danjjak.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/**
 * Background Sensor Service
 * Continuously captures L0 data and pumps it to the Backend
 */
class SensorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("DanjjakService", "Sensor capturing started...")
        startDataCollection()
        return START_STICKY
    }

    private fun startDataCollection() {
        serviceScope.launch {
            while (isActive) {
                // Simulate capturing GPS & App Usage
                val sensorData = JSONObject().apply {
                    put("type", "APP_USAGE")
                    put("value", mapOf("app" to "Social Media", "duration" to 120))
                }
                
                sendDataToBackend(sensorData)
                
                delay(60000) // Every 1 minute
            }
        }
    }

    private fun sendDataToBackend(data: JSONObject) {
        try {
            val url = URL("http://your-backend-ip:3000/api/sensor")
            with(url.openConnection() as HttpURLConnection) {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                
                outputStream.use { os ->
                    os.write(data.toString().toByteArray())
                }
                
                val responseCode = responseCode
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
