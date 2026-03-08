package com.danjjak.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(onConsentComplete: () -> Unit) {
    var consentL0 by remember { mutableStateOf(false) }
    var consentL1 by remember { mutableStateOf(false) }
    var consentL2 by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "개인정보 수집 및 활용 동의",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "단짝 AI가 당신을 더 잘 이해하기 위해\n필요한 정보를 선택해 주세요.",
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        ConsentItem(
            title = "L0: 센서 데이터 수집 (필수)",
            description = "위치, 가속도, 앱 사용 기록 등을 수집하여 현재 상황을 인식합니다.",
            checked = consentL0,
            onCheckedChange = { consentL0 = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ConsentItem(
            title = "L1: 자연어 활동 요약",
            description = "수집된 데이터를 기반으로 일상의 순간들을 텍스트로 기록합니다.",
            checked = consentL1,
            onCheckedChange = { consentL1 = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ConsentItem(
            title = "L2: AI 개인화 분석",
            description = "당신의 선호와 성향을 파악하여 맞춤형 조언을 생성합니다.",
            checked = consentL2,
            onCheckedChange = { consentL2 = it }
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConsentComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = consentL0 && consentL1 && consentL2, // All required for full experience
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
        ) {
            Text("동의하고 시작하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConsentItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = description, fontSize = 13.sp, color = Color.Gray)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF6750A4)
                )
            )
        }
    }
}
