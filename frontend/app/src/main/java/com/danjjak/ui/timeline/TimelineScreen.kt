package com.danjjak.ui.timeline

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class TimelineEvent(
    val time: String,
    val title: String,
    val description: String,
    val category: String,
    val date: LocalDate
)

@Composable
fun TimelineScreen() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // Mock Data
    val allEvents = remember {
        listOf(
            TimelineEvent("08:30 AM", "아침 운동", "공원에서 30분간 조깅 완료", "Health", LocalDate.now()),
            TimelineEvent("01:00 PM", "점심 식사", "강남구 삼성동 '회사 근처 식당'", "Location", LocalDate.now()),
            TimelineEvent("07:00 PM", "자료 조사", "유튜브 생산성 채널 2시간 시청", "Media", LocalDate.now()),
            TimelineEvent("11:30 PM", "하루 마무리", "AI 단짝과 대화 나눔", "Personal", LocalDate.now()),
            TimelineEvent("10:00 AM", "주말 등산", "관악산 정상 도착!", "Health", LocalDate.now().minusDays(1)),
            TimelineEvent("02:00 PM", "카페 공부", "자바스크립트 프레임워크 학습", "Study", LocalDate.now().minusDays(2))
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selectedDate,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "TimelineTransition"
        ) { date ->
            if (date == null) {
                CalendarView(
                    currentMonth = currentMonth,
                    onDateSelected = { selectedDate = it },
                    onMonthChange = { currentMonth = it }
                )
            } else {
                val dayEvents = allEvents.filter { it.date == date }
                TimelineDetailView(
                    date = date,
                    events = dayEvents,
                    onBack = { selectedDate = null }
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
            .padding(24.dp)
    ) {
        Text(
            text = "타임라인",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1D1B20),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Month Selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }

        // Days of week
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            val days = listOf("일", "월", "화", "수", "목", "금", "토")
            days.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (day == "일") Color.Red else Color.Gray
                )
            }
        }

        // Calendar Grid
        val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Empty slots for days before the 1st
            items(firstDayOfMonth) {
                Spacer(modifier = Modifier.aspectRatio(1f))
            }
            // Actual days
            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day + 1)
                val isToday = date == LocalDate.now()
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isToday) Color(0xFF6750A4) else Color.White
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (day + 1).toString(),
                        fontSize = 16.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) Color.White else Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineDetailView(
    date: LocalDate,
    events: List<TimelineEvent>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6750A4), Color(0xFF917AFF))
                    )
                )
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("M월 d일 타임라인", Locale.KOREAN)),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "이날의 기록이 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(events) { event ->
                    TimelineItem(event)
                }
            }
        }
    }
}

@Composable
fun TimelineItem(event: TimelineEvent) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(70.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = event.time,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Vertical Line & Dot
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFF6750A4), CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF6750A4), Color.LightGray.copy(alpha = 0.3f))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F2FA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = getCategoryColor(event.category),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = event.category,
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = event.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.description, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Health" -> Color(0xFF4CAF50)
        "Location" -> Color(0xFF2196F3)
        "Media" -> Color(0xFFFF9800)
        "Personal" -> Color(0xFF9C27B0)
        "Study" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
