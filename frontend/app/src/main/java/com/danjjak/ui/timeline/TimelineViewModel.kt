package com.danjjak.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.danjjak.data.L0.AppDatabase
import com.danjjak.data.L0.SensorData
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import org.json.JSONObject

@HiltViewModel
class TimelineViewModel @Inject constructor(private val db: AppDatabase) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val events: StateFlow<List<TimelineEvent>> = _selectedDate
        .flatMapLatest { date ->
            db.sensorDao().getRecentData().map { list ->
                list.filter { data ->
                    val eventDate = Instant.ofEpochMilli(data.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    eventDate == date
                }.map { data ->
                    mapSensorDataToEvent(data)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    private fun mapSensorDataToEvent(data: SensorData): TimelineEvent {
        val time = Instant.ofEpochMilli(data.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))
            
        return when (data.type) {
            "MEAL" -> {
                val json = JSONObject(data.value)
                TimelineEvent(
                    time = data.value.let { JSONObject(it).optString("time", time) },
                    title = "식사 기록 🍱",
                    description = "${json.optString("location")}에서 ${json.optJSONArray("menu")?.join(", ") ?: ""} 식사",
                    category = "Meal",
                    date = Instant.ofEpochMilli(data.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }
            "GPS" -> TimelineEvent(time, "위치 감지", "현재 위치: ${data.value}", "Location", Instant.ofEpochMilli(data.timestamp).atZone(ZoneId.systemDefault()).toLocalDate())
            else -> TimelineEvent(time, "활동 기록", "자동 수집된 정보", "Personal", Instant.ofEpochMilli(data.timestamp).atZone(ZoneId.systemDefault()).toLocalDate())
        }
    }
}
