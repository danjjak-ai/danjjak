# Danjjak (단짝) — 서비스 구현 계획서 (plan.md)

> 작성일: 2026-04-03  
> 기준 버전: v1.1 (Gemini API 연동 완료 상태)

---

## 목차

1. [현재 구현 상태 요약](#1-현재-구현-상태-요약)
2. [미구현 항목 및 우선순위](#2-미구현-항목-및-우선순위)
3. [Phase 1: 프론트엔드 핵심 기능 완성](#3-phase-1-프론트엔드-핵심-기능-완성)
4. [Phase 2: 백엔드 아키텍처 강화](#4-phase-2-백엔드-아키텍처-강화)
5. [Phase 3: 실제 센서 데이터 수집 구현](#5-phase-3-실제-센서-데이터-수집-구현)
6. [Phase 4: SSO 로그인 실제 구현](#6-phase-4-sso-로그인-실제-구현)
7. [Phase 5: FCM 푸시 알림 실제 구현](#7-phase-5-fcm-푸시-알림-실제-구현)
8. [Phase 6: AI 개인화 파이프라인 고도화](#8-phase-6-ai-개인화-파이프라인-고도화)
9. [Phase 7: 아키텍처 리팩토링 (MVVM + Hilt)](#9-phase-7-아키텍처-리팩토링-mvvm--hilt)
10. [Phase 8: 테스트 및 품질 보증](#10-phase-8-테스트-및-품질-보증)
11. [파일별 구현 작업 목록](#11-파일별-구현-작업-목록)
12. [구현 우선순위 로드맵](#12-구현-우선순위-로드맵)

---

## 1. 현재 구현 상태 요약

### ✅ 완료된 항목

| 분류 | 항목 | 파일 |
|------|------|------|
| **백엔드** | Gemini API 실제 연동 + PII 마스킹 | `aiGateway.service.ts` |
| **백엔드** | auth, feedback 라우트 마운트 | `index.ts` |
| **백엔드** | dotenv 환경변수 관리 | `.env`, `.env.example` |
| **백엔드** | L0/L1/L2 메모리 파이프라인 (인메모리) | `memory.service.ts` |
| **백엔드** | Strong COT 시스템 프롬프트 | `aiGateway.service.ts` |
| **백엔드** | 모든 API 라우트 (sensor, nudge, feedback, auth) | `routes/*.ts` |
| **프론트** | 전체 UI 화면 구현 (Login, Consent, Dashboard, Registration, Timeline) | `ui/**/*.kt` |
| **프론트** | Room DB L0 데이터 저장 | `data/L0/` |
| **프론트** | MemoryVectorStore L1 (시뮬레이션) | `data/L1/` |
| **프론트** | L2PersonalizationManager (LoRA/DPO 시뮬레이션) | `intelligence/` |
| **프론트** | SensorService 포그라운드 서비스 (mock 데이터) | `service/SensorService.kt` |
| **프론트** | BootReceiver 자동 시작 | `service/BootReceiver.kt` |
| **프론트** | Material 3 테마 시스템 | `ui/theme/` |

### ❌ 미구현 / 시뮬레이션 항목

| 항목 | 현재 상태 |
|------|-----------|
| 기록 저장 버튼 | `onClick = { /* Save Logic */ }` 빈 람다 |
| SSO 로그인 | 버튼 누르면 바로 `onLoginSuccess()` 호출 (Mock) |
| 실제 GPS 수집 | `{\"lat\": 37.5665, \"lng\": 126.9780}` 하드코딩 |
| 실제 앱 사용 기록 | `YouTube, 120분` 하드코딩 |
| FCM 푸시 알림 | `fcm_mock_12345` 반환하는 mock |
| 대시보드 AI 조언 | 하드코딩된 텍스트 |
| NavController 미사용 | `mutableStateOf("login")` 상태 머신 방식 |
| ViewModel 미사용 | UI 상태 모두 Composable 내부에서 직접 관리 |
| DI (Hilt) 미사용 | 의존성 직접 인스턴스화 |
| 백엔드 CORS 설정 없음 | 실제 배포 시 문제 발생 가능 |
| 백엔드 인증 미들웨어 없음 | JWT 검증 없이 모든 요청 허용 |
| 유저별 메모리 분리 없음 | 서버 인메모리 전체 공유 |
| 백엔드 영속성 없음 | 서버 재시작 시 모든 메모리 초기화 |
| Room Compiler KSP 미사용 | `annotationProcessor` 사용 중 (Kotlin은 KSP 권장) |
| 서버 URL 하드코딩 | `http://10.0.2.2:3000` 소스 코드에 직접 박힘 |

---

## 2. 미구현 항목 및 우선순위

우선순위를 **기능 완성도** 기준으로 분류합니다.

| 우선순위 | 항목 | 이유 |
|----------|------|------|
| 🔴 **P0 (즉시)** | 기록 저장 버튼 실제 구현 | 핵심 UX — 저장이 안 되면 앱이 의미 없음 |
| 🔴 **P0 (즉시)** | 대시보드 AI 조언 API 연동 | 핵심 가치 제안 — AI 조언이 실제로 표시돼야 함 |
| 🔴 **P0 (즉시)** | 피드백 (좋아요/싫어요) UI 연동 | DPO 개인화 루프의 핵심 |
| 🟠 **P1 (중요)** | 실제 GPS 데이터 수집 | L0 데이터의 신뢰성 확보 |
| 🟠 **P1 (중요)** | 백엔드 CORS 미들웨어 | 실제 기기 테스트 시 필수 |
| 🟠 **P1 (중요)** | 백엔드 JWT 인증 미들웨어 | 보안 기본기 |
| 🟠 **P1 (중요)** | Room DB KSP 마이그레이션 | Kotlin 프로젝트 권장 빌드 도구 |
| 🟡 **P2 (보통)** | NavController 도입 | 깊은 화면 전환 및 백스택 관리 |
| 🟡 **P2 (보통)** | ViewModel 도입 | 화면 회전, 생명주기 관리 |
| 🟡 **P2 (보통)** | FCM 실제 연동 | 프로액티브 Nudge |
| 🟡 **P2 (보통)** | 앱 사용 기록 실제 수집 | L0 데이터 다양성 확보 |
| 🟢 **P3 (향후)** | SSO 실제 연동 | Kakao SDK, Google Sign-In |
| 🟢 **P3 (향후)** | Hilt DI 적용 | 코드 품질 및 테스트 용이성 |
| 🟢 **P3 (향후)** | 벡터 임베딩 실제 구현 | On-device Text Embedding |
| 🟢 **P3 (향후)** | 백엔드 DB 영속성 (PostgreSQL/MongoDB) | 서버 재시작 내구성 |

---

## 3. Phase 1: 프론트엔드 핵심 기능 완성

### 3.1 기록 저장 버튼 실제 구현

**목표**: `EventRegistrationScreen.kt`의 `onClick = { /* Save Logic */ }` 완성

#### 3.1.1 백엔드 API 추가 (새 엔드포인트)

**파일**: `backend/src/routes/journal.routes.ts` (신규)

```typescript
POST /api/journal
Body: { text: string, tags: string[], timestamp: number }
Response: { success: true, id: string, memory: NaturalMemory }
```

**파일**: `backend/src/services/memory.service.ts` (수정)
- `storeJournal(text, tags)` 메서드 추가
- 저널 텍스트를 L1 NaturalMemory로 직접 변환
- Gemini API를 통한 자동 태그 추출 및 L2 컨텍스트 업데이트

#### 3.1.2 안드로이드 API 클라이언트 추가

**파일**: `frontend/app/src/main/java/com/danjjak/data/remote/ApiService.kt` (신규)

```kotlin
// Retrofit 또는 OkHttp를 사용한 API 클라이언트
object ApiService {
    private const val BASE_URL = BuildConfig.BACKEND_URL  // gradle.properties로 관리
    
    suspend fun saveJournal(text: String, tags: List<String>): JournalResponse
    suspend fun getAdvice(): AdviceResponse
    suspend fun sendFeedback(reaction: String): FeedbackResponse
}
```

**파일**: `frontend/app/build.gradle` (수정)
- Retrofit2 + OkHttp3 의존성 추가
- `buildConfigField` 로 서버 URL injection

```groovy
// 추가할 의존성
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'

// buildConfig
buildConfigField("String", "BACKEND_URL", '"http://10.0.2.2:3000"')
```

#### 3.1.3 EventRegistrationScreen.kt 수정

```kotlin
// 현재 (빈 람다)
Button(onClick = { /* Save Logic */ })

// 완성 후
Button(onClick = {
    coroutineScope.launch {
        val result = ApiService.saveJournal(reflectionText, listOf("daily", "manual"))
        if (result.success) {
            // 저장 완료 Snackbar 표시
            snackbarHostState.showSnackbar("기록이 저장되었습니다 ✨")
            reflectionText = ""
        }
    }
})
```

**추가 UI 구성 요소:**
- `SnackbarHost` 로 저장 완료/실패 피드백
- 저장 중 `CircularProgressIndicator` 로딩 표시
- 저장 버튼 비활성화 (빈 텍스트 입력 시 `enabled = reflectionText.isNotBlank()`)

---

### 3.2 대시보드 AI 조언 실제 API 연동

**목표**: `DigitalTwinDashboard.kt`의 AI 조언 텍스트를 실제 Gemini API 응답으로 교체

#### 현재 상태 (하드코딩)
```kotlin
// DigitalTwinDashboard.kt 내 AI 조언 카드
val advice = "오늘 5,234걸음으로 일일 목표의 52%를 달성하셨어요. ..."
```

#### 구현 방향

**파일**: `frontend/app/src/main/java/com/danjjak/ui/dashboard/DashboardViewModel.kt` (신규)

```kotlin
class DashboardViewModel : ViewModel() {
    private val _advice = MutableStateFlow<String?>(null)
    val advice: StateFlow<String?> = _advice.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadAdvice() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiService.getAdvice()
                _advice.value = response.advice
            } catch (e: Exception) {
                _advice.value = "지금은 단짝이 잠깐 쉬고 있어요. 잠시 후 다시 시도해주세요."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

**파일**: `frontend/app/src/main/java/com/danjjak/ui/dashboard/DigitalTwinDashboard.kt` (수정)

```kotlin
@Composable
fun DigitalTwinDashboard(viewModel: DashboardViewModel = viewModel()) {
    val advice by viewModel.advice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAdvice()
    }
    // ... AI 조언 카드에서 advice 사용
}
```

**AI 조언 카드 UI 개선:**
- 로딩 중: `ShimmerEffect` 또는 `CircularProgressIndicator`
- 조언 표시: COT 파싱 후 `[조언]` 섹션만 추출하여 표시
- 새로고침 버튼: 수동 재호출 가능
- 좋아요/싫어요 버튼: 피드백 전송 후 개인화 반영

---

### 3.3 피드백 UI 연동

**목표**: AI 조언 카드에 👍/👎 버튼 추가 후 `POST /api/feedback` 실제 호출

**파일**: `frontend/app/src/main/java/com/danjjak/ui/dashboard/DigitalTwinDashboard.kt` (수정)

```kotlin
// AI 조언 카드 하단에 추가
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
) {
    IconButton(onClick = {
        coroutineScope.launch {
            ApiService.sendFeedback("LIKE")
        }
    }) {
        Icon(Icons.Default.ThumbUp, contentDescription = "좋아요")
    }
    IconButton(onClick = {
        coroutineScope.launch {
            ApiService.sendFeedback("DISLIKE")
        }
    }) {
        Icon(Icons.Default.ThumbDown, contentDescription = "별로예요")
    }
}
```

---

### 3.4 타임라인에 실제 데이터 표시

**목표**: `TimelineScreen.kt`의 하드코딩된 이벤트를 Room DB 데이터로 교체

#### 현재 상태
```kotlin
// TimelineScreen.kt — 하드코딩된 샘플 데이터
val events = mapOf(
    15 to listOf(
        TimelineEvent("09:30", "아침 러닝", "5km를 완주했어요...", "Health"),
        ...
    )
)
```

#### 구현 방향

**파일**: `frontend/app/src/main/java/com/danjjak/data/repository/EventRepository.kt` (신규)

```kotlin
class EventRepository(private val db: AppDatabase) {
    // Room DB에서 특정 날짜의 이벤트 조회
    fun getEventsByDate(date: LocalDate): Flow<List<TimelineEvent>>
    
    // 저널에서 TimelineEvent로 변환
    fun journalToTimelineEvent(journal: SensorData): TimelineEvent
}
```

**파일**: `frontend/app/src/main/java/com/danjjak/ui/timeline/TimelineViewModel.kt` (신규)

```kotlin
class TimelineViewModel(private val repo: EventRepository) : ViewModel() {
    val selectedDate = MutableStateFlow(LocalDate.now())
    val eventsForDate: StateFlow<List<TimelineEvent>> = selectedDate
        .flatMapLatest { repo.getEventsByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
```

**TimelineScreen.kt 수정 포인트:**
- `remember { mutableStateOf(mapOf(...)) }` → `viewModel.eventsForDate.collectAsState()`
- 이벤트 없는 날짜: 빈 상태 UI (`EmptyStateView`) 표시

**Room DB 스키마 확장 필요:**

```kotlin
// 신규 Entity
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val tags: String,           // JSON 직렬화 ("daily,health")
    val category: String,       // "Health", "Location", "Personal" 등
    val timestamp: Long = System.currentTimeMillis()
)
```

---

## 4. Phase 2: 백엔드 아키텍처 강화

### 4.1 CORS 미들웨어 추가

**파일**: `backend/src/index.ts` (수정)

```typescript
import cors from 'cors';

// 신규 패키지
// npm install cors
// npm install -D @types/cors

app.use(cors({
    origin: '*',  // 개발 환경 — 프로덕션에서는 특정 도메인으로 제한
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));
```

### 4.2 JWT 인증 미들웨어

**파일**: `backend/src/middleware/auth.middleware.ts` (신규)

```typescript
import jwt from 'jsonwebtoken';
import type { Request, Response, NextFunction } from 'express';

export const authenticateToken = (req: Request, res: Response, next: NextFunction) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader?.split(' ')[1];
    
    if (!token) return res.status(401).json({ error: 'Unauthorized' });
    
    jwt.verify(token, process.env.JWT_SECRET!, (err, user) => {
        if (err) return res.status(403).json({ error: 'Forbidden' });
        (req as any).user = user;
        next();
    });
};
```

**파일**: `backend/src/routes/auth.routes.ts` (수정)
- `POST /auth/login` 에서 실제 JWT 토큰 발급
- `userId`를 토큰 payload에 포함

**`.env` 추가 변수:**
```dotenv
JWT_SECRET=your_jwt_secret_here
JWT_EXPIRES_IN=7d
```

### 4.3 유저별 메모리 분리

**파일**: `backend/src/services/memory.service.ts` (수정)

```typescript
// 현재: 단일 인스턴스
export const memoryService = new MemoryService();

// 변경: userId별 독립 인스턴스
class MemoryServiceRegistry {
    private instances: Map<string, MemoryService> = new Map();
    
    getForUser(userId: string): MemoryService {
        if (!this.instances.has(userId)) {
            this.instances.set(userId, new MemoryService());
        }
        return this.instances.get(userId)!;
    }
}
export const memoryRegistry = new MemoryServiceRegistry();
```

**파일**: `backend/src/controllers/advice.controller.ts` (수정)
- `req.user.id` 추출
- `memoryRegistry.getForUser(userId)` 로 교체

### 4.4 입력 유효성 검사 (Validation)

**신규 패키지**: `npm install zod`

**파일**: `backend/src/routes/journal.routes.ts` (신규, §3.1.1 연장)

```typescript
import { z } from 'zod';

const JournalSchema = z.object({
    text: z.string().min(1).max(5000),
    tags: z.array(z.string()).optional(),
    timestamp: z.number().optional()
});

router.post('/journal', authenticateToken, async (req, res) => {
    const parsed = JournalSchema.safeParse(req.body);
    if (!parsed.success) {
        return res.status(400).json({ error: parsed.error.flatten() });
    }
    // ...저장 로직
});
```

### 4.5 에러 핸들링 미들웨어

**파일**: `backend/src/middleware/errorHandler.ts` (신규)

```typescript
export const globalErrorHandler = (
    err: Error, req: Request, res: Response, next: NextFunction
) => {
    console.error(`[Error] ${err.message}`);
    res.status(500).json({
        success: false,
        error: process.env.NODE_ENV === 'production' 
            ? 'Internal Server Error' 
            : err.message
    });
};
```

### 4.6 백엔드 영속성 (선택적: SQLite via better-sqlite3)

> **참고**: 완전한 DB 구성 전, 경량 JSON 파일 기반 영속성을 먼저 도입 가능

**파일**: `backend/src/services/persistence.service.ts` (신규)

```typescript
// 옵션 A: 파일 기반 (빠른 구현)
import fs from 'fs/promises';

export class PersistenceService {
    private filePath = './data/memory.json';
    
    async save(userId: string, data: any) { /* JSON 파일 저장 */ }
    async load(userId: string): Promise<any> { /* JSON 파일 로드 */ }
}

// 옵션 B: better-sqlite3 (권장)
// npm install better-sqlite3
// 테이블: users, journal_entries, sensor_data, ai_contexts
```

---

## 5. Phase 3: 실제 센서 데이터 수집 구현

### 5.1 실제 GPS 수집

**파일**: `frontend/app/src/main/java/com/danjjak/service/LocationManager.kt` (신규)

```kotlin
class DanjjakLocationManager(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
        }
    }
    
    private fun hasLocationPermission() = 
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
```

**파일**: `frontend/app/build.gradle` (수정)

```groovy
// GPS 위치 제공자
implementation 'com.google.android.gms:play-services-location:21.0.1'
```

**파일**: `frontend/app/src/main/java/com/danjjak/service/SensorService.kt` (수정)

```kotlin
// 현재 (하드코딩)
val gpsData = SensorData(type = "GPS", value = "{\"lat\": 37.5665, \"lng\": 126.9780}")

// 변경 후
val location = locationManager.getCurrentLocation()
val gpsData = if (location != null) {
    // Privacy by Design: 도시 수준만 일반화하여 저장
    val cityLevel = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    SensorData(
        type = "GPS",
        value = JSONObject().apply {
            put("lat", location.latitude)
            put("lng", location.longitude)
            put("city", cityLevel?.firstOrNull()?.locality ?: "Unknown")
        }.toString()
    )
} else {
    SensorData(type = "GPS", value = "{\"error\": \"location_unavailable\"}")
}
```

**PbD 원칙 유지:**
- Room DB에는 정확한 좌표 저장 (온디바이스)
- 백엔드 전송 시: 도시명만 (`"Seoul"`, `"강남구"`) 전송

### 5.2 실제 앱 사용 기록 수집

**필요 권한**: `android.permission.PACKAGE_USAGE_STATS` (특수 권한 — 설정 앱에서 수동 허용 필요)

**파일**: `frontend/app/src/main/java/com/danjjak/service/UsageStatsCollector.kt` (신규)

```kotlin
class UsageStatsCollector(private val context: Context) {
    
    fun getRecentAppUsage(hours: Int = 1): List<AppUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - hours * 3600000L,
            now
        )
        
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
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

data class AppUsageData(val packageName: String, val durationMinutes: Long)
```

**AndroidManifest.xml 추가:**
```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />
```

### 5.3 걸음 수 (Step Counter) 수집

**필요 권한**: `android.permission.ACTIVITY_RECOGNITION` (Android 10+)

**파일**: `frontend/app/src/main/java/com/danjjak/service/StepCounterService.kt` (신규)

```kotlin
class StepCounterSensor(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var stepCount = 0
    
    fun startListening() {
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                stepCount = it.values[0].toInt()
            }
        }
    }
    
    fun getStepCount() = stepCount
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

**DigitalTwinDashboard 연결:**
- 현재 대시보드의 "5,234 걸음" 하드코딩 → StepCounterSensor 실시간 값으로 대체

---

## 6. Phase 4: SSO 로그인 실제 구현

### 6.1 Google Sign-In 구현

**필요 패키지:**

```groovy
// build.gradle (app)
implementation 'com.google.android.gms:play-services-auth:20.7.0'
```

**파일**: `frontend/app/src/main/java/com/danjjak/ui/auth/LoginScreen.kt` (수정)

```kotlin
// GoogleSignIn 클라이언트 설정
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)  // google-services.json에서 자동
    .requestEmail()
    .build()

val googleSignInClient = GoogleSignIn.getClient(context, gso)

// 버튼 클릭 시
val signInIntent = googleSignInClient.signInIntent
launcher.launch(signInIntent)

// 결과 처리
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    val account = task.getResult(ApiException::class.java)
    // account.idToken → 백엔드에 전송 → JWT 교환
    viewModel.loginWithGoogle(account.idToken!!)
}
```

**Firebase 설정 필요:**
- Firebase Console에서 프로젝트 생성
- `google-services.json` 다운로드 후 `frontend/app/` 디렉토리에 배치
- `build.gradle (project)`: `classpath 'com.google.gms:google-services:4.4.0'`
- `build.gradle (app)`: `apply plugin: 'com.google.gms.google-services'`

### 6.2 Kakao 로그인 구현

**필요 패키지:**

```groovy
// settings.gradle
maven { url 'https://devrepo.kakao.com/nexus/content/groups/public/' }

// build.gradle (app)
implementation "com.kakao.sdk:v2-user:2.19.0"
```

**AndroidManifest.xml 추가:**
```xml
<meta-data android:name="com.kakao.sdk.AppKey" android:value="@string/kakao_app_key" />
```

**파일**: `frontend/app/src/main/res/values/strings.xml` (수정)
```xml
<string name="kakao_app_key">카카오 앱 키</string>
```

### 6.3 백엔드 auth.routes.ts 수정 (JWT 실제 발급)

```typescript
import jwt from 'jsonwebtoken';

router.post('/login', async (req, res) => {
    const { provider, idToken } = req.body;  // 'google' | 'kakao'
    
    let userId: string;
    let userInfo: { name: string; email: string };
    
    if (provider === 'google') {
        // Google ID Token 검증
        const ticket = await googleOAuthClient.verifyIdToken({
            idToken,
            audience: process.env.GOOGLE_CLIENT_ID
        });
        const payload = ticket.getPayload()!;
        userId = `google_${payload.sub}`;
        userInfo = { name: payload.name!, email: payload.email! };
    }
    
    const jwtToken = jwt.sign(
        { userId, ...userInfo },
        process.env.JWT_SECRET!,
        { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );
    
    res.status(200).json({ success: true, token: jwtToken, user: userInfo });
});
```

---

## 7. Phase 5: FCM 푸시 알림 실제 구현

### 7.1 Firebase Admin SDK 설정 (백엔드)

```bash
npm install firebase-admin
```

**파일**: `backend/src/services/push.service.ts` (수정)

```typescript
import admin from 'firebase-admin';
import serviceAccount from '../../firebase-service-account.json';

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount as admin.ServiceAccount)
});

export class PushService {
    async sendNudge(fcmToken: string, title: string, body: string): Promise<void> {
        // 현재 (mock)
        // return { success: true, messageId: 'fcm_mock_12345' }
        
        // 변경 후 (실제 FCM)
        await admin.messaging().send({
            token: fcmToken,
            notification: { title, body },
            android: {
                priority: 'high',
                notification: {
                    channelId: 'danjjak_nudge',
                    color: '#6750A4'
                }
            }
        });
    }
}
```

> **보안 주의**: `firebase-service-account.json` → `.gitignore`에 반드시 추가

### 7.2 FCM Token 관리 (안드로이드)

**필요 패키지:**

```groovy
implementation 'com.google.firebase:firebase-messaging-ktx:23.3.1'
```

**파일**: `frontend/app/src/main/java/com/danjjak/service/DanjjakFirebaseService.kt` (신규)

```kotlin
class DanjjakFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        // 백엔드에 FCM 토큰 등록
        CoroutineScope(Dispatchers.IO).launch {
            ApiService.registerFcmToken(token)
        }
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title ?: "단짝", it.body ?: "")
        }
    }
    
    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, "danjjak_nudge")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(0xFF6750A4.toInt())
            .setAutoCancel(true)
            .build()
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
```

**AndroidManifest.xml 추가:**
```xml
<service android:name=".service.DanjjakFirebaseService" android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**백엔드 user 테이블에 fcmToken 필드 추가:**
```typescript
// memory.service.ts 또는 별도 UserService
interface User {
    id: string;
    name: string;
    email: string;
    fcmToken?: string;  // FCM 토큰 저장
    consentStatus: { l0: boolean; l1: boolean; l2: boolean };
}
```

---

## 8. Phase 6: AI 개인화 파이프라인 고도화

### 8.1 L1 메모리 서비스 — Gemini 기반 자연어 변환

**파일**: `backend/src/services/memory.service.ts` (수정)

```typescript
// 현재: 단순 하드코딩 변환
private async processL0ToL1() {
    const memory = {
        event: "Activity Detected",
        description: `사용자가 ${lastData.type} 기반으로 특정 활동을 수행함.`
    };
}

// 변경: Gemini API를 활용한 자연어 변환
private async processL0ToL1() {
    const recentL0 = this.l0_storage.slice(-5);
    const prompt = `다음 센서 데이터를 사용자 친화적인 자연어 이벤트 설명으로 변환하세요:
${JSON.stringify(recentL0, null, 2)}

응답 형식 (JSON):
{
  "event": "이벤트 제목",
  "description": "2-3문장 자연어 설명",
  "tags": ["태그1", "태그2"],
  "category": "Health|Location|Media|Personal|Study"
}`;
    
    const l1Memory = await aiGateway.generateL1Memory(prompt);
    this.l1_storage.push(l1Memory);
}
```

### 8.2 L2 컨텍스트 압축 — Gemini 기반 요약

```typescript
// 변경: L1 메모리들을 압축하여 L2 컨텍스트 생성
private async updateL2() {
    if (this.l1_storage.length < 3) return;
    
    const recentL1 = this.l1_storage.slice(-10);
    const prompt = `다음 사용자의 최근 활동 기억들을 핵심 패턴 중심으로 압축하세요:
${recentL1.map(m => `- ${m.event}: ${m.description}`).join('\n')}

압축 결과 (100자 이내, 핵심 패턴 중심):`;
    
    this.l2_context = await aiGateway.compress(prompt);
}
```

### 8.3 프로액티브 Nudge 트리거 로직 개선

**파일**: `backend/src/controllers/advice.controller.ts` (수정)

```typescript
// 현재: 단순 문자열 포함 여부
if (context.includes("활동적")) { ... }

// 변경: 패턴 기반 트리거
private shouldTriggerNudge(context: string, l0Data: RawData): boolean {
    const triggers = [
        // 앱 사용 시간 과다 (2시간 이상)
        l0Data.type === 'APP_USAGE' && l0Data.value?.duration > 120,
        // 이동 감지
        l0Data.type === 'GPS' && context.includes('새로운 장소'),
        // 저녁 시간 운동 부재
        new Date().getHours() === 18 && !context.includes('운동'),
    ];
    return triggers.some(Boolean);
}
```

### 8.4 LoRA 가중치 온디바이스 개선 (장기)

**파일**: `frontend/app/src/main/java/com/danjjak/intelligence/L2PersonalizationManager.kt` (수정)

```kotlin
// 현재: 단순 LIKE/DISLIKE 이진 분류
fun onFeedback(reaction: FeedbackType) {
    when (reaction) {
        FeedbackType.LIKE -> loraWeights["proactivity"] = 
            (loraWeights["proactivity"]!! + 0.1f).coerceAtMost(1.0f)
        FeedbackType.DISLIKE -> loraWeights["proactivity"] = 
            (loraWeights["proactivity"]!! - 0.1f).coerceAtLeast(0.0f)
    }
    // 영속성: SharedPreferences에 저장
    persistWeights()
}

// 추가: LoRA 가중치 영속화
private fun persistWeights() {
    val prefs = context.getSharedPreferences("danjjak_lora", Context.MODE_PRIVATE)
    loraWeights.forEach { (key, value) ->
        prefs.edit().putFloat(key, value).apply()
    }
}

// 추가: 시간대별 패턴 기반 페르소나 조정
fun getContextualPersona(hour: Int): String {
    return when {
        hour in 6..9 -> "아침을 활기차게 시작하도록 격려하는 에너지 코치"
        hour in 22..23 -> "하루를 차분히 마무리하도록 돕는 따뜻한 친구"
        else -> getPersonaPrompt()
    }
}
```

---

## 9. Phase 7: 아키텍처 리팩토링 (MVVM + Hilt)

### 9.1 Hilt 의존성 주입 도입

**파일**: `frontend/app/build.gradle` (수정)

```groovy
apply plugin: 'kotlin-kapt'
apply plugin: 'dagger.hilt.android.plugin'

dependencies {
    implementation 'com.google.dagger:hilt-android:2.48'
    kapt 'com.google.dagger:hilt-compiler:2.48'
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
}
```

**파일**: `frontend/build.gradle` (수정)
```groovy
classpath 'com.google.dagger:hilt-android-gradle-plugin:2.48'
```

**파일**: `frontend/app/src/main/java/com/danjjak/DanjjakApplication.kt` (신규)

```kotlin
@HiltAndroidApp
class DanjjakApplication : Application()
```

**Room KSP 마이그레이션:**

```groovy
// build.gradle (app)
// 제거: annotationProcessor "androidx.room:room-compiler:$room_version"
// 추가:
apply plugin: 'com.google.devtools.ksp'
ksp "androidx.room:room-compiler:$room_version"
```

**신규 Module 구성:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "danjjak_database").build()
    
    @Provides fun provideSensorDao(db: AppDatabase): SensorDao = db.sensorDao()
    @Provides fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideApiService(): ApiService = ApiService.create(BuildConfig.BACKEND_URL)
}
```

### 9.2 NavController 도입

**파일**: `frontend/app/src/main/java/com/danjjak/navigation/AppNavGraph.kt` (신규)

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Consent : Screen("consent")
    object Dashboard : Screen("dashboard")
    object Registration : Screen("registration")
    object Timeline : Screen("timeline")
    object JournalDetail : Screen("journal/{id}") {
        fun createRoute(id: Int) = "journal/$id"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = { navController.navigate(Screen.Consent.route) })
        }
        composable(Screen.Consent.route) {
            ConsentScreen(onConsentComplete = { navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }})
        }
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.Registration.route) {
            EventRegistrationScreen(
                onSaved = { navController.navigate(Screen.Timeline.route) }
            )
        }
        composable(Screen.Timeline.route) { TimelineScreen() }
    }
}
```

**MainActivity.kt 수정:**

```kotlin
// 현재: 수동 상태 머신
var currentNavState by remember { mutableStateOf("login") }

// 변경: NavController
val navController = rememberNavController()
AppNavGraph(navController = navController)
```

---

## 10. Phase 8: 테스트 및 품질 보증

### 10.1 백엔드 단위 테스트

**패키지**: `npm install -D jest @types/jest ts-jest supertest @types/supertest`

**파일**: `backend/src/services/__tests__/aiGateway.service.test.ts` (신규)

```typescript
describe('AiGatewayService', () => {
    it('should tokenize PII correctly', () => {
        const service = new AiGatewayService();
        const result = service.tokenize('김철수가 서울에서 전화했습니다');
        expect(result).not.toContain('김철수');
        expect(result).not.toContain('서울');
        expect(result).toMatch(/\[PER_[A-Z0-9]+\]/);
    });
    
    it('should rehydrate tokens correctly', () => {
        const service = new AiGatewayService();
        const tokenized = service.tokenize('김철수');
        const rehydrated = service.rehydrate(tokenized);
        expect(rehydrated).toBe('김철수');
    });
    
    it('should update persona on feedback', () => {
        const service = new AiGatewayService();
        service.updatePersona('LIKE');
        // 페르소나가 적극적 코치로 변경되었는지 확인
    });
});
```

**파일**: `backend/src/routes/__tests__/advice.routes.test.ts` (신규)

```typescript
describe('POST /api/sensor', () => {
    it('should accept valid sensor data', async () => {
        const res = await request(app).post('/api/sensor')
            .set('Authorization', `Bearer ${mockJwt}`)
            .send({ type: 'GPS', value: 'Seoul' });
        expect(res.status).toBe(200);
        expect(res.body.success).toBe(true);
    });
});
```

### 10.2 안드로이드 단위 테스트

**파일**: `frontend/app/src/test/java/com/danjjak/L2PersonalizationManagerTest.kt` (신규)

```kotlin
@Test
fun `proactivity increases on LIKE feedback`() {
    val manager = L2PersonalizationManager(mockContext)
    val initial = manager.getPersonaPrompt()
    manager.onFeedback(FeedbackType.LIKE)
    // proactivity > 0.6 이면 코치 스타일
    assertTrue(manager.getPersonaPrompt().contains("코치"))
}
```

**파일**: `frontend/app/src/androidTest/java/com/danjjak/EventRegistrationScreenTest.kt` (신규)

```kotlin
@Test
fun `save button disabled when text is empty`() {
    composeTestRule.setContent { EventRegistrationScreen() }
    composeTestRule.onNodeWithText("기록 저장하기").assertIsNotEnabled()
}

@Test
fun `save button enabled when text is not empty`() {
    composeTestRule.setContent { EventRegistrationScreen() }
    composeTestRule.onNodeWithText("오늘 어떤 좋은 일이 있었나요?").performTextInput("좋은 하루였어요")
    composeTestRule.onNodeWithText("기록 저장하기").assertIsEnabled()
}
```

---

## 11. 파일별 구현 작업 목록

### 백엔드 (backend/src/)

| 파일 | 작업 유형 | 작업 내용 |
|------|-----------|-----------|
| `index.ts` | 수정 | CORS 미들웨어, 전역 에러 핸들러, journal 라우트 추가 |
| `routes/journal.routes.ts` | **신규** | 저널 저장/조회 엔드포인트 |
| `routes/auth.routes.ts` | 수정 | JWT 실제 발급, Google ID Token 검증 |
| `middleware/auth.middleware.ts` | **신규** | JWT 검증 미들웨어 |
| `middleware/errorHandler.ts` | **신규** | 전역 에러 핸들링 |
| `middleware/rateLimit.ts` | **신규** | API 요청 속도 제한 |
| `services/memory.service.ts` | 수정 | 유저별 분리, Gemini L1 변환, persist |
| `services/push.service.ts` | 수정 | Firebase Admin SDK 실제 FCM |
| `services/user.service.ts` | **신규** | 유저 관리 (FCM 토큰, 동의 상태) |
| `services/persistence.service.ts` | **신규** | 파일/DB 기반 영속성 |

### 안드로이드 프론트엔드 (frontend/app/src/main/java/com/danjjak/)

| 파일 | 작업 유형 | 작업 내용 |
|------|-----------|-----------|
| `MainActivity.kt` | 수정 | NavController로 교체, HiltAndroidApp |
| `DanjjakApplication.kt` | **신규** | `@HiltAndroidApp` 앱 클래스 |
| `navigation/AppNavGraph.kt` | **신규** | NavHost + 모든 화면 라우트 정의 |
| `data/remote/ApiService.kt` | **신규** | Retrofit API 클라이언트 |
| `data/remote/dto/*.kt` | **신규** | Request/Response DTO 클래스들 |
| `data/repository/JournalRepository.kt` | **신규** | 저널 CRUD, 로컬↔원격 동기화 |
| `data/repository/EventRepository.kt` | **신규** | 타임라인 이벤트 조회 |
| `data/L0/JournalEntry.kt` | **신규** | Room Entity (저널 엔트리) |
| `data/L0/JournalDao.kt` | **신규** | Room DAO (저널 CRUD) |
| `data/L0/AppDatabase.kt` | 수정 | JournalEntry 엔트리 추가, KSP |
| `di/DatabaseModule.kt` | **신규** | Hilt DB 제공 모듈 |
| `di/NetworkModule.kt` | **신규** | Hilt 네트워크 제공 모듈 |
| `ui/dashboard/DashboardViewModel.kt` | **신규** | AI 조언 로딩, 피드백 전송 |
| `ui/dashboard/DigitalTwinDashboard.kt` | 수정 | API 연동, 피드백 버튼, 걸음 수 실시간 |
| `ui/registration/EventRegistrationScreen.kt` | 수정 | 저장 버튼 실제 구현, 로딩/성공 UI |
| `ui/registration/RegistrationViewModel.kt` | **신규** | 저장 로직, 상태 관리 |
| `ui/timeline/TimelineScreen.kt` | 수정 | Room DB에서 실제 데이터 표시 |
| `ui/timeline/TimelineViewModel.kt` | **신규** | 날짜별 이벤트 조회 |
| `ui/auth/LoginScreen.kt` | 수정 | Google Sign-In, Kakao 로그인 실제 연동 |
| `ui/auth/LoginViewModel.kt` | **신규** | 로그인 처리, JWT 저장 |
| `service/SensorService.kt` | 수정 | 실제 GPS, UsageStats 수집 |
| `service/LocationManager.kt` | **신규** | FusedLocationProvider 래퍼 |
| `service/UsageStatsCollector.kt` | **신규** | UsageStatsManager 래퍼 |
| `service/StepCounterSensor.kt` | **신규** | TYPE_STEP_COUNTER 수집 |
| `service/DanjjakFirebaseService.kt` | **신규** | FCM 토큰 갱신, 푸시 수신 |
| `intelligence/L2PersonalizationManager.kt` | 수정 | 가중치 영속화, 시간대별 페르소나 |

---

## 12. 구현 우선순위 로드맵

```
Week 1 (즉시 실행 — P0)
├── [백엔드] journal.routes.ts 신규 → 저널 저장 API
├── [프론트] EventRegistrationScreen 저장 버튼 구현
├── [프론트] ApiService.kt (Retrofit) 기초 구성
├── [프론트] DashboardViewModel + AI 조언 API 연동
└── [프론트] 피드백 버튼 (👍/👎) UI + API 연동

Week 2 (핵심 인프라 — P1)
├── [백엔드] CORS 미들웨어 추가
├── [백엔드] JWT 인증 미들웨어 + auth.routes.ts 수정
├── [백엔드] 유저별 메모리 서비스 분리
├── [프론트] Room DB JournalEntry 엔트리 추가
├── [프론트] TimelineScreen 실제 DB 연동
└── [프론트] build.gradle KSP + Retrofit 의존성 추가

Week 3 (센서 실제 구현 — P1)
├── [프론트] LocationManager.kt (FusedLocation)
├── [프론트] SensorService.kt 실제 GPS 수집
├── [프론트] UsageStatsCollector.kt
├── [프론트] StepCounterSensor.kt
└── [백엔드] memory.service.ts Gemini L1 변환 개선

Week 4 (아키텍처 리팩토링 — P2)
├── [프론트] Hilt DI 도입 (DanjjakApplication, Module들)
├── [프론트] NavController + AppNavGraph
├── [프론트] 모든 화면에 ViewModel 적용
└── [백엔드] Zod 입력 유효성 검사 추가

Week 5 (SSO + FCM — P2)
├── [프론트] Firebase 프로젝트 설정 + google-services.json
├── [프론트] Google Sign-In 실제 연동
├── [백엔드] Google ID Token 검증 + JWT 발급
├── [백엔드] Firebase Admin SDK + 실제 FCM 발송
└── [프론트] DanjjakFirebaseService.kt 구현

Week 6 (Kakao + 테스트 — P3)
├── [프론트] Kakao SDK 연동
├── [백엔드] 단위 테스트 작성 (aiGateway, memory)
├── [프론트] Compose UI 테스트 작성
└── [전체] 통합 테스트 및 버그 수정
```

---

## 부록: 환경 설정 보완 사항

### 백엔드 .env.example 추가 변수

```dotenv
# 기존
GEMINI_API_KEY=your_gemini_api_key_here
PORT=3000
GEMINI_MODEL=gemini-1.5-flash

# 추가 필요
JWT_SECRET=your_jwt_secret_min_32_chars
JWT_EXPIRES_IN=7d
GOOGLE_CLIENT_ID=your_google_oauth_client_id
NODE_ENV=development
CORS_ORIGIN=*
FIREBASE_PROJECT_ID=your_firebase_project_id
```

### gradle.properties 서버 URL 관리

```properties
# 에뮬레이터 환경
BACKEND_URL_EMULATOR=http://10.0.2.2:3000
# 실제 기기 테스트 환경 (같은 WiFi)
BACKEND_URL_LOCAL=http://192.168.1.100:3000
# 프로덕션
BACKEND_URL_PROD=https://api.danjjak.com
```

```groovy
// build.gradle (app)
buildConfigField("String", "BACKEND_URL", "\"${BACKEND_URL_EMULATOR}\"")
```

### .gitignore 추가 항목

```gitignore
# Firebase 서비스 계정 키
backend/firebase-service-account.json
# Android google-services
frontend/app/google-services.json
# Kakao 키
frontend/app/src/main/res/values/kakao_keys.xml
# 대용량 heap dump (이미 무시되고 있지 않다면)
frontend/*.hprof
```

---

*본 문서는 v1.1 기준 미구현 항목을 중심으로 작성한 구현 계획서입니다.*  
*각 Phase는 독립적으로 진행 가능하며, P0 → P1 순서로 우선 진행을 권장합니다.*
