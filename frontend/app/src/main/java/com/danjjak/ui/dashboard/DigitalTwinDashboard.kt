package com.danjjak.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Digital Twin Dashboard
 * "Expensive Looking" UI with 3D-like visuals and health graphs.
 */
@Composable
fun DigitalTwinDashboard(viewModel: DashboardViewModel = hiltViewModel()) {
    val advice by viewModel.advice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF0F0F15)),
                    center = androidx.compose.ui.geometry.Offset(500f, 500f),
                    radius = 2000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "나의 디지털 트윈",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
            )

            // 3D Human Model Simulation (Glassmorphism card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF))
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x33FFFFFF), Color(0x00FFFFFF))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle background glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x15BB86FC), Color.Transparent),
                            radius = size.width / 2
                        )
                    )
                }

                AnimatedHumanSilhouette()
                
                // Status Badges
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    StatusBadge("에너지 85%", Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusBadge("스트레스 낮음", Color(0xFF2196F3))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Activity Graph
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "활동 인사이트",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "지난 7일",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            ActivityGraph()

            Spacer(modifier = Modifier.height(40.dp))

            // On-device AI Insights (Premium Card Design)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFBB86FC).copy(0.4f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26).copy(0.8f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFBB86FC).copy(0.2f),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI 단짝의 맞춤 조언",
                            color = Color(0xFFBB86FC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFFBB86FC), modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(
                            text = advice ?: "\"AI 분석을 기다리는 중입니다...\"",
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { viewModel.loadAdvice() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "새로고침", tint = Color.White.copy(alpha=0.7f))
                            }
                            IconButton(onClick = { viewModel.sendFeedback("LIKE") }) {
                                Icon(Icons.Default.ThumbUp, contentDescription = "좋아요", tint = Color.White.copy(alpha=0.7f))
                            }
                            IconButton(onClick = { viewModel.sendFeedback("DISLIKE") }) {
                                Icon(Icons.Default.ThumbDown, contentDescription = "별로예요", tint = Color.White.copy(alpha=0.7f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🔒 개인정보 보호: 모든 활동 분석은 기기 내에서 안전하게 처리됩니다.",
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AnimatedHumanSilhouette() {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Breathing & Glow Animation
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier
        .size(240.dp)
        .graphicsLayer(scaleX = breathingScale, scaleY = breathingScale)
    ) {
        val width = size.width
        val height = size.height
        val center = androidx.compose.ui.geometry.Offset(width / 2, height / 2)
        val mainColor = Color(0xFFBB86FC)
        val secondaryColor = Color(0xFF03DAC6)

        // 1. Orbital Ring (Giving 3D depth)
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(mainColor.copy(0f), mainColor, mainColor.copy(0f)),
                center = center
            ),
            radius = width * 0.45f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
            alpha = 0.3f
        )
        
        // Rotating tech bits on orbit
        val orbitRadius = width * 0.45f
        val angleRad = Math.toRadians(rotation.toDouble()).toFloat()
        val orbitPoint = androidx.compose.ui.geometry.Offset(
            center.x + orbitRadius * Math.cos(angleRad.toDouble()).toFloat(),
            center.y + orbitRadius * Math.sin(angleRad.toDouble()).toFloat()
        )
        drawCircle(secondaryColor, radius = 4.dp.toPx(), center = orbitPoint)

        // 2. Stylized Human Silhouette (Using Paths for more detail)
        val silhouettePath = Path().apply {
            val cx = width / 2
            val cy = height / 2
            
            // Head
            addOval(androidx.compose.ui.geometry.Rect(cx - 15.dp.toPx(), cy - 90.dp.toPx(), cx + 15.dp.toPx(), cy - 60.dp.toPx()))
            
            // Neck & Torso
            moveTo(cx - 10.dp.toPx(), cy - 60.dp.toPx())
            lineTo(cx + 10.dp.toPx(), cy - 60.dp.toPx())
            lineTo(cx + 25.dp.toPx(), cy - 45.dp.toPx()) // Shoulders
            lineTo(cx + 25.dp.toPx(), cy + 20.dp.toPx()) // Hips
            lineTo(cx - 25.dp.toPx(), cy + 20.dp.toPx())
            lineTo(cx - 25.dp.toPx(), cy - 45.dp.toPx())
            close()
            
            // Arms
            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                rect = androidx.compose.ui.geometry.Rect(cx - 45.dp.toPx(), cy - 45.dp.toPx(), cx - 30.dp.toPx(), cy + 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            ))
            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                rect = androidx.compose.ui.geometry.Rect(cx + 30.dp.toPx(), cy - 45.dp.toPx(), cx + 45.dp.toPx(), cy + 10.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            ))
            
            // Legs
            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                rect = androidx.compose.ui.geometry.Rect(cx - 22.dp.toPx(), cy + 25.dp.toPx(), cx - 7.dp.toPx(), cy + 90.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            ))
            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                rect = androidx.compose.ui.geometry.Rect(cx + 7.dp.toPx(), cy + 25.dp.toPx(), cx + 22.dp.toPx(), cy + 90.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
            ))
        }

        // Draw outer glow for silhouette
        drawPath(
            path = silhouettePath,
            brush = Brush.radialGradient(
                colors = listOf(mainColor.copy(alpha = glowAlpha * 0.5f), Color.Transparent),
                center = center,
                radius = width / 2
            )
        )

        // Draw main silhouette
        drawPath(
            path = silhouettePath,
            color = mainColor.copy(alpha = 0.2f)
        )
        
        // Draw Mesh lines (Horizontal)
        clipPath(silhouettePath) {
            val meshCount = 15
            for (i in 0..meshCount) {
                val y = (height / meshCount) * i
                drawLine(
                    color = mainColor.copy(alpha = 0.15f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Scan line effect
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, secondaryColor.copy(0.6f), Color.Transparent),
                    startY = scanLineY * height - 20.dp.toPx(),
                    endY = scanLineY * height + 20.dp.toPx()
                ),
                start = androidx.compose.ui.geometry.Offset(0f, scanLineY * height),
                end = androidx.compose.ui.geometry.Offset(width, scanLineY * height),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Silhouette Outline
        drawPath(
            path = silhouettePath,
            color = mainColor.copy(alpha = 0.4f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
fun ActivityGraph() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .padding(horizontal = 8.dp)
    ) {
        val pathColor = Color(0xFF03DAC6)
        val fillColor = Color(0xFF03DAC6).copy(alpha = 0.2f)
        val data = listOf(0.2f, 0.4f, 0.35f, 0.7f, 0.55f, 0.9f, 0.85f)
        
        val widthStep = size.width / (data.size - 1)
        val curvePath = Path()
        val fillPath = Path()
        
        // Move to start
        val startX = 0f
        val startY = size.height * (1 - data[0])
        curvePath.moveTo(startX, startY)
        fillPath.moveTo(startX, size.height)
        fillPath.lineTo(startX, startY)

        for (i in 0 until data.size - 1) {
            val x1 = i * widthStep
            val y1 = size.height * (1 - data[i])
            val x2 = (i + 1) * widthStep
            val y2 = size.height * (1 - data[i + 1])
            
            // Control points for smooth Bezier
            val controlX1 = x1 + (x2 - x1) / 2
            val controlX2 = x1 + (x2 - x1) / 2
            
            curvePath.cubicTo(controlX1, y1, controlX2, y2, x2, y2)
            fillPath.cubicTo(controlX1, y1, controlX2, y2, x2, y2)
        }
        
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        // 1. Draw Grid Lines (Professional HUD feel)
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = (size.height / gridCount) * i
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Draw Gradient Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor, Color.Transparent),
                startY = 0f,
                endY = size.height
            )
        )

        // 3. Draw the Curve Path
        drawPath(
            path = curvePath,
            color = pathColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 4. Draw pulsing indicator on the last point
        val lastX = size.width
        val lastY = size.height * (1 - data.last())
        
        drawCircle(
            color = pathColor.copy(alpha = pulseAlpha * 0.3f),
            radius = 12.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
        drawCircle(
            color = pathColor,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(lastX, lastY)
        )
    }
}
