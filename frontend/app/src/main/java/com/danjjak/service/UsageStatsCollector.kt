package com.danjjak.service

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context

data class AppUsageData(val packageName: String, val durationMinutes: Long)

class UsageStatsCollector(private val context: Context) {
    
    fun getRecentAppUsage(hours: Int = 1): List<AppUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - hours * 3600000L,
            now
        )
        
        if (stats == null) return emptyList()

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(5)  // 상위 5개 앱만
            .map { AppUsageData(
                packageName = it.packageName,
                durationMinutes = it.totalTimeInForeground / 60000
            )}
    }
    
    fun hasUsagePermission(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
