package com.danjjak.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danjjak.data.L0.AppDatabase
import com.danjjak.data.L0.SensorData
import com.danjjak.data.remote.ApiService
import com.danjjak.data.remote.dto.MealRequest
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealRegistrationScreen() {
    val db = AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current)
    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
    ) {
        // Chat Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6750A4))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("AI 단짝 식단 비서 🍱", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        // Chat History
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    AssistantMessage("안녕하세요! 오늘 식사하신 내용을 친구에게 말하듯 편하게 알려주세요.\n예: [12시에 사이제리야에서 치킨이랑 도리아를 먹었어]")
                }
            }
            items(messages) { message ->
                if (message.isUser) {
                    UserMessage(message.text)
                } else {
                    AssistantMessage(message.text, message.searchKeywords)
                }
            }
            if (isProcessing) {
                item {
                    Text("단짝이 영양소를 분석하고 있어요...", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Input Area
        Surface(
            tonalElevation = 4.dp,
            containerColor = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("식사 내용을 입력하세요...") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 3
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isProcessing) {
                            val userText = inputText
                            messages.add(ChatMessage(userText, true))
                            inputText = ""
                            isProcessing = true
                            
                            coroutineScope.launch {
                                try {
                                    val response = ApiService.api.saveMeal(MealRequest(userText))
                                    if (response.success && response.data != null) {
                                        val data = response.data
                                        val aiReply = "분석 완료! ✅\n" +
                                                "📍 장소: ${data.location}\n" +
                                                "🍴 메뉴: ${data.menu.joinToString(", ")}\n" +
                                                "🔥 칼로리: 약 ${data.estimatedCalories} kcal\n" +
                                                "🧬 영양: 탄(${data.nutrients.carbs}) 단(${data.nutrients.protein}) 지(${data.nutrients.fat})\n\n" +
                                                "💡 단짝의 한마디: ${data.recommendation}"
                                        messages.add(ChatMessage(aiReply, false))

                                        // Save locally for Timeline and Reminder Sync
                                        db.sensorDao().insert(SensorData(
                                            type = "MEAL",
                                            value = JSONObject().apply {
                                                put("time", data.time)
                                                put("location", data.location)
                                                put("menu", data.menu.joinToString(", "))
                                            }.toString()
                                        ))
                                        
                                        // Store keywords for the UI to use if needed
                                        if (data.searchKeywords.isNotEmpty()) {
                                            messages.add(ChatMessage("💡 추천한 메뉴를 즐기실 수 있는 주변 식당을 찾아보시겠어요?", false, data.searchKeywords))
                                        }
                                    } else {
                                        messages.add(ChatMessage("죄송해요, 식단 분석에 실패했어요. 다시 말씀해 주시겠어요?", false))
                                    }
                                } catch (e: Exception) {
                                    messages.add(ChatMessage("앗, 네트워크가 불안정한 것 같아요. 나중에 다시 시도해 주세요.", false))
                                } finally {
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.clip(CircleShape).background(Color(0xFF6750A4))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

data class ChatMessage(
    val text: String, 
    val isUser: Boolean, 
    val searchKeywords: List<String> = emptyList()
)

@Composable
fun UserMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = Color(0xFF6750A4),
            shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(text, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
        }
    }
}

@Composable
fun AssistantMessage(text: String, keywords: List<String> = emptyList()) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color(0xFFEADDFF),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text, color = Color.Black, fontSize = 14.sp)
                
                if (keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val query = keywords.joinToString(" ")
                            val gmmIntentUri = Uri.parse("geo:37.5665,126.9780?q=$query")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📍 근처 식당 찾기", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
