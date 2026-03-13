# Danjjak (단짝) — 구현 내용 상세 분석 문서

> 최종 업데이트: 2026-03-13

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [아키텍처 전체 구조](#2-아키텍처-전체-구조)
3. [프론트엔드 (Android 앱)](#3-프론트엔드-android-앱)
   - 3.1 [프로젝트 설정 (build.gradle)](#31-프로젝트-설정-buildgradle)
   - 3.2 [AndroidManifest.xml — 권한 및 컴포넌트 선언](#32-androidmanifestxml--권한-및-컴포넌트-선언)
   - 3.3 [앱 진입점 — MainActivity.kt](#33-앱-진입점--mainactivitykt)
   - 3.4 [테마 시스템 — Theme.kt / Typography.kt](#34-테마-시스템--themekt--typographykt)
   - 3.5 [화면: 로그인 (LoginScreen.kt)](#35-화면-로그인-loginscreenkt)
   - 3.6 [화면: 동의 (ConsentScreen.kt)](#36-화면-동의-consentscreenkt)
   - 3.7 [화면: 대시보드 (DashboardScreen / DigitalTwinDashboard)](#37-화면-대시보드-dashboardscreen--digitaltwIndashboard)
   - 3.8 [화면: 기록하기 (EventRegistrationScreen.kt)](#38-화면-기록하기-eventregistrationscreenkt)
   - 3.9 [화면: 타임라인 (TimelineScreen.kt)](#39-화면-타임라인-timelinescreenkt)
   - 3.10 [온디바이스 데이터 계층 — L0, L1, L2](#310-온디바이스-데이터-계층--l0-l1-l2)
   - 3.11 [백그라운드 서비스 — SensorService.kt / BootReceiver.kt](#311-백그라운드-서비스--sensorservicekt--bootreceiverkt)
4. [백엔드 (Node.js + TypeScript)](#4-백엔드-nodejs--typescript)
   - 4.1 [서버 진입점 — index.ts](#41-서버-진입점--indexts)
   - 4.2 [라우팅 구조](#42-라우팅-구조)
   - 4.3 [서비스 층](#43-서비스-층)
   - 4.4 [컨트롤러](#44-컨트롤러)
5. [개인정보 보호 아키텍처 (Privacy by Design)](#5-개인정보-보호-아키텍처-privacy-by-design)
6. [AI 개인화 파이프라인](#6-ai-개인화-파이프라인)
7. [데이터 흐름 다이어그램](#7-데이터-흐름-다이어그램)
8. [미구현 / 시뮬레이션 항목](#8-미구현--시뮬레이션-항목)
9. [파일 트리 요약](#9-파일-트리-요약)

---

## 1. 프로젝트 개요

**단짝(Danjjak)**은 사용자의 일상 데이터를 온디바이스(On-Device)에서 분석하여,  
개인화된 AI 조언(Nudge)을 제공하는 프라이버시 우선(Privacy-First) 개인 AI 동반자 앱이다.

| 구분 | 기술 스택 |
|------|-----------|
| **모바일 앱** | Android (Kotlin, Jetpack Compose, Material 3) |
| **온디바이스 DB** | Room Database (SQLite) |
| **온디바이스 벡터 저장소** | MemoryVectorStore (In-memory, FAISS 시뮬레이션) |
| **백엔드** | Node.js + Express.js (TypeScript, ES Module) |
| **푸시 알림** | Firebase Cloud Messaging (FCM) — mock 구현 |
| **AI 개인화** | LoRA + DPO 시뮬레이션 (L2 Personalization Manager) |
| **프라이버시 보호** | NER 기반 PII 마스킹 (Deterministic Tokenization) |

---

## 2. 아키텍처 전체 구조

```
[ Android App (Frontend) ]
        │
        ├── UI Layer (Jetpack Compose)
        │       ├── LoginScreen
        │       ├── ConsentScreen
        │       ├── DashboardScreen (DigitalTwinDashboard)
        │       ├── EventRegistrationScreen
        │       └── TimelineScreen
        │
        ├── Data Layer (On-Device)
        │       ├── L0: Room DB (SensorData)
        │       ├── L1: MemoryVectorStore (SemanticMemory)
        │       └── L2: L2PersonalizationManager (LoRA/DPO 시뮬레이션)
        │
        └── Service Layer
                ├── SensorService (Foreground Service)
                └── BootReceiver (자동 시작)
                        │
                        └──[ HTTP POST ]──→ [ Node.js Backend ]
                                                │
                                                ├── /api/sensor     → AdviceController.captureSensor()
                                                ├── /api/nudge      → AdviceController.getAdvice()
                                                ├── /api/feedback   → AiGateway.updatePersona()
                                                └── /auth/login, /auth/consent
```

---

## 3. 프론트엔드 (Android 앱)

### 3.1 프로젝트 설정 (build.gradle)

`frontend/app/build.gradle`

| 항목 | 값 |
|------|----|
| `applicationId` | `com.danjjak` |
| `minSdk` | 24 (Android 7.0+) |
| `targetSdk` / `compileSdk` | 34 (Android 14) |
| `versionCode` / `versionName` | 1 / "1.0" |
| Compose BOM | `2023.08.00` |
| Room | `2.6.1` |

**주요 의존성:**
- `androidx.compose.material3` — Material You 디자인 시스템
- `androidx.compose.material:material-icons-extended` — 확장 아이콘 팩
- `androidx.room:room-runtime`, `room-ktx` — 온디바이스 SQLite
- `androidx.lifecycle:lifecycle-runtime-ktx` — 코루틴 Lifecycle 연동
- `androidx.activity:activity-compose` — Compose Entry Point

---

### 3.2 AndroidManifest.xml — 권한 및 컴포넌트 선언

`frontend/app/src/main/AndroidManifest.xml`

**선언된 권한:**

| 권한 | 목적 |
|------|------|
| `INTERNET` | 백엔드 서버 통신 |
| `FOREGROUND_SERVICE` | 백그라운드 센서 수집 서비스 유지 |
| `RECEIVE_BOOT_COMPLETED` | 기기 부팅 시 서비스 자동 시작 |
| `ACCESS_FINE_LOCATION` | 정확한 GPS 위치 수집 |
| `ACCESS_COARSE_LOCATION` | 대략적 위치 수집 |
| `POST_NOTIFICATIONS` | Android 13+ 푸시 알림 권한 |

**선언된 컴포넌트:**
- `MainActivity` — 앱의 진입점 Activity (MAIN / LAUNCHER)
- `SensorService` — Foreground Service (exported=false, 내부 전용)
- `BootReceiver` — BroadcastReceiver for `BOOT_COMPLETED`

---

### 3.3 앱 진입점 — MainActivity.kt

`frontend/app/src/main/java/com/danjjak/MainActivity.kt`

`ComponentActivity`를 상속하며, `setContent` 안에서 전체 네비게이션 상태를 직접 관리한다.
별도의 Navigation 컴포넌트(NavController) 없이 `remember { mutableStateOf("login") }` 방식으로 단순 상태 머신을 구현했다.

**네비게이션 상태 흐름:**
```
"login" → "consent" → "main"
```

**"main" 상태:**
- `Scaffold` + `NavigationBar`(하단 탭)로 구성
- 탭 3개: 대시보드(`dashboard`), 기록하기(`registration`), 타임라인(`timeline`)
- 선택된 탭 아이콘 컬러: `#6750A4` (Material 3 Primary Purple)
- Indicator Color: `#EADDFF`

---

### 3.4 테마 시스템 — Theme.kt / Typography.kt

`frontend/app/src/main/java/com/danjjak/ui/theme/Theme.kt`

Material 3 `lightColorScheme` 기반. 다크모드는 기본 Material 3 dark scheme만 적용.

| 토큰 | 색상값 | 용도 |
|------|--------|------|
| `primary` | `#6750A4` | 주 브랜드 컬러 (보라색) |
| `secondary` | `#625B71` | 보조 컬러 |
| `tertiary` | `#7D5260` | 강조 컬러 (분홍-보라) |
| `background` | `#FBF8FF` | 화면 배경 (따뜻한 흰색) |
| `surface` | `#FFFFFF` | 카드/표면 컬러 |
| `onBackground` | `#1C1B1F` | 텍스트 기본색 |

---

### 3.5 화면: 로그인 (LoginScreen.kt)

`frontend/app/src/main/java/com/danjjak/ui/auth/LoginScreen.kt`

**주요 구성:**
- 배경: `#6750A4` → `#917AFF` 수직 그라디언트
- 로고 영역: `단짝` 텍스트가 담긴 반투명 화이트 카드 (`alpha=0.2f`)
- 슬로건 텍스트: "나만의 AI 단짝과 함께하는 일상의 기록"
- 부연 텍스트: "당신의 삶을 이해하고 따뜻한 조언을 건네는 동반자입니다."

**SSO 버튼 (현재는 Mock):**
- 카카오로 시작하기: `#FEE500` 배경 (카카오 공식 노란색)
- Google 계정으로 시작하기: 흰색 배경
- 두 버튼 모두 `onLoginSuccess()` 콜백 호출 → `ConsentScreen`으로 이동

---

### 3.6 화면: 동의 (ConsentScreen.kt)

`frontend/app/src/main/java/com/danjjak/ui/auth/ConsentScreen.kt`

LICES 3계층(L0/L1/L2)에 대한 세분화된 동의를 사용자에게 요청한다.
각 항목은 `Switch` 토글로 개별 동의를 받는 `ConsentItem` Composable로 구성된다.

| 동의 항목 | 내용 | 필수 여부 |
|-----------|------|-----------|
| L0: 센서 데이터 수집 | 위치, 가속도, 앱 사용 기록 수집 | 필수 |
| L1: 자연어 활동 요약 | 수집 데이터를 텍스트로 기록 | 필수 |
| L2: AI 개인화 분석 | 사용자 성향 파악 및 맞춤 조언 생성 | 필수 |

- **"동의하고 시작하기" 버튼**: 세 항목 모두 `true`일 때만 활성화 (`enabled = consentL0 && consentL1 && consentL2`)
- 완료 시 `onConsentComplete()` → `MainActivity`에서 `"main"` 상태로 전환

---

### 3.7 화면: 대시보드 (DashboardScreen / DigitalTwinDashboard)

`frontend/app/src/main/java/com/danjjak/ui/dashboard/DigitalTwinDashboard.kt`

**"고급스러운(Expensive Looking)" UI**가 핵심 목표인 화면.
전체 배경: 방사형 그라디언트 (`#1E1E2E` → `#0F0F15` — 딥 다크 퍼플)

#### 3.7.1 디지털 트윈 섹션 (3D Glassmorphism 카드)

- `RoundedCornerShape(28.dp)` + 반투명 화이트 배경 (glassmorphism)
- 테두리: 수직 그라디언트 border (`55%→0% 흰색`)
- 내부: `AnimatedHumanSilhouette()` composable 렌더링

**`AnimatedHumanSilhouette` — Canvas 기반 실시간 애니메이션:**

| 애니메이션 | 파라미터 | 설명 |
|------------|---------|------|
| 호흡 (Breathing) | 3000ms, FastOutSlowInEasing | `scaleX/Y: 1.0f ~ 1.05f` 반복 |
| 글로우 (Glow) | 2000ms, LinearEasing | `alpha: 0.3f ~ 0.7f` 반복 |
| 스캔 라인 (Scan Line) | 4000ms, LinearEasing | Y축 0→1 반복 (기기 분석 느낌) |
| 궤도 회전 (Orbit) | 10000ms, LinearEasing | 0→360도 반복 |

**실루엣 Path 구성 (Canvas drawPath):**
- 머리: `addOval`
- 목 + 몸통: `moveTo / lineTo / close`
- 팔 (좌/우): `addRoundRect`
- 다리 (좌/우): `addRoundRect`
- 메시 라인: `clipPath` 내부에 수평 15줄 (`alpha=0.15f`)
- 스캔 라인: 수직 그라디언트 브러시 라인 (`secondaryColor = #03DAC6`)
- 외곽선 Stroke: `alpha=0.4f`, 궤도 점(teal dot)

**Status Badge 2개 (우측 상단):**
- 에너지 85% — `#4CAF50` (초록)
- 스트레스 낮음 — `#2196F3` (파랑)

#### 3.7.2 활동 인사이트 그래프 (ActivityGraph)

`ActivityGraph()` — Canvas로 직접 그린 베지어 곡선 그래프 (7일치)

- 데이터: `listOf(0.2f, 0.4f, 0.35f, 0.7f, 0.55f, 0.9f, 0.85f)` (정규화 값)
- 베지어 곡선: `cubicTo`로 부드러운 S-Curve 렌더링
- 그라디언트 채우기: `#03DAC6 → 투명` 수직 그라디언트
- 그리드 라인: 4개 수평선 (`alpha=0.05f`)
- 마지막 포인트: 펄스 애니메이션 (1000ms, `alpha: 0.4f ~ 1.0f`)

#### 3.7.3 AI 단짝 조언 카드

- 배경: `#1E1E26 (80% 불투명)` + 보라색 좌측 border
- 상단: 보라색 점(`#BB86FC`) + "AI 단짝의 맞춤 조언" 레이블
- 본문: 맞춤 조언 텍스트 (현재 하드코딩 mock)
- 하단: `🔒 개인정보 보호` 면책 문구

---

### 3.8 화면: 기록하기 (EventRegistrationScreen.kt)

`frontend/app/src/main/java/com/danjjak/ui/registration/EventRegistrationScreen.kt`

**헤더:** `#6750A4 → #917AFF` 보라색 그라디언트 배경 + "나를 위한 기록" 타이틀

**단짝의 추천 제안 (AI Suggestions) — `LazyRow`:**

| 카드 | 제목 | 아이콘 | 배경색 |
|------|------|--------|--------|
| 1 | 어제의 사진 | `Icons.Default.Image` | `#EADDFF` |
| 2 | 누구와 함께? | `Icons.Default.Person` | `#D0BCFF` |
| 3 | 운동 완료 | `Icons.Default.DirectionsRun` | `#B69DF8` |
| 4 | 새로운 장소 | `Icons.Default.Place` | `#A285F4` |

카드 클릭 시 `OutlinedTextField`에 해당 제목 기반 텍스트 자동 삽입.

**기록 저장 버튼:** `fillMaxWidth()`, `height(56.dp)`, `RoundedCornerShape(16.dp)` — 현재 클릭 동작은 `{}` (미구현)

---

### 3.9 화면: 타임라인 (TimelineScreen.kt)

`frontend/app/src/main/java/com/danjjak/ui/timeline/TimelineScreen.kt`

**2개의 하위 뷰를 AnimatedContent로 전환:**

#### CalendarView

- 월 이동: `ChevronLeft` / `ChevronRight` 아이콘 버튼
- 요일 헤더: `listOf("일", "월", "화", "수", "목", "금", "토")` — 일요일은 빨간색
- 날짜 그리드: `LazyVerticalGrid(GridCells.Fixed(7))`
- 오늘 날짜: `#6750A4` 배경 + 흰색 굵은 텍스트

#### TimelineDetailView

클릭한 날짜의 이벤트 목록을 `LazyColumn`으로 표시.  
`BackHandler`로 뒤로가기 시 캘린더로 복귀.

**`TimelineItem` 구조:**
- 시간 (왼쪽, `70.dp` 컬럼)
- 타임라인 점 + 수직 세로선 (보라→회색 그라디언트)
- 이벤트 카드 (`RoundedCornerShape(16.dp)`, `#F7F2FA` 배경)

**카테고리 색상:**

| 카테고리 | 색상 |
|----------|------|
| Health | `#4CAF50` (초록) |
| Location | `#2196F3` (파랑) |
| Media | `#FF9800` (주황) |
| Personal | `#9C27B0` (보라) |
| Study | `#F44336` (빨강) |

**화면 전환 애니메이션:**
- 캘린더 → 상세: 오른쪽 슬라이드 + 페이드인
- 상세 → 캘린더: 왼쪽 슬라이드 + 페이드인

---

### 3.10 온디바이스 데이터 계층 — L0, L1, L2

#### L0: Room DB (SensorData)

`com.danjjak.data.L0`

```kotlin
@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,   // "GPS", "ACCEL", "LIGHT", "APP_USAGE", "BATTERY" 등
    val value: String,  // JSON 직렬화된 값
    val timestamp: Long = System.currentTimeMillis()
)
```

**DAO 쿼리:**
- `insert(data)` — suspend fun
- `getRecentData()` — `Flow<List<SensorData>>` (최근 100개)
- `getDataByType(type)` — `Flow<List<SensorData>>`
- `deleteAll()` — suspend fun

**AppDatabase:** `Room.databaseBuilder("danjjak_database")`, Singleton 패턴

#### L1: MemoryVectorStore (SemanticMemory)

`com.danjjak.data.L1.MemoryVectorStore`

인메모리 리스트를 사용하는 FAISS 시뮬레이션 벡터 저장소.

```kotlin
data class SemanticMemory(
    val id: String,           // UUID
    val text: String,         // 자연어 기억 텍스트
    val vector: List<Float>,  // 시뮬레이션 임베딩 (문자 코드 기반)
    val tags: List<String>,
    val timestamp: Long
)
```

- `storeMemory(text, tags)`: 텍스트를 `take(10)` 문자 코드 기반으로 벡터화 후 저장
- `search(query, limit)`: 텍스트 포함 여부 + 태그 매칭으로 검색 (실제 코사인 유사도 아님)
- `LocalVectorProvider` — object 싱글톤

#### L2: L2PersonalizationManager

`com.danjjak.intelligence.L2PersonalizationManager`

LoRA 가중치와 DPO를 시뮬레이션하는 온디바이스 AI 개인화 매니저.

**LoRA 가중치 맵:**
```kotlin
private var loraWeights = mapOf(
    "politeness" to 0.5f,
    "proactivity" to 0.5f,
    "detail" to 0.5f
)
```

**DPO 피드백 반응:**
- `LIKE` → `proactivity += 0.1f` (더 적극적인 코치 스타일)
- `DISLIKE` → `proactivity -= 0.1f` (더 조용한 조언자 스타일)

**`getPersonaPrompt()` 출력:**

| proactivity 값 | 페르소나 |
|----------------|----------|
| > 0.6f | "적극적으로 행동을 권장하고 동기를 부여하는 코치 스타일" |
| < 0.4f | "조용하고 차분하게 관찰하며 필요한 핵심만 말하는 조언자 스타일" |
| 0.4f ~ 0.6f | "따뜻하고 공감하며 대화하는 친구 스타일" (기본값) |

---

### 3.11 백그라운드 서비스 — SensorService.kt / BootReceiver.kt

#### SensorService.kt

`com.danjjak.service.SensorService`

- `Foreground Service` — 알림을 통해 시스템 종료로부터 보호
- 알림 채널: `SensorServiceChannel` (IMPORTANCE_LOW)
- 알림 텍스트: "단짝이 당신의 일상을 살피는 중"
- `START_STICKY` — 서비스 재시작 보장

**`startDataCollection()` — 코루틴 루프 (5분 주기):**
1. GPS 데이터: `{"lat": 37.5665, "lng": 126.9780}` → Room DB 저장
2. App Usage 데이터: `{"app": "YouTube", "duration": 120}` → Room DB 저장
3. Privacy by Design: 일반화된 데이터 (`"Seoul"`)만 백엔드로 전송
4. `sendDataToBackend(sensorJson)` → `POST http://10.0.2.2:3000/api/sensor`
   - `10.0.2.2`: Android 에뮬레이터에서 로컬호스트를 가리키는 주소

**`HttpURLConnection`으로 직접 HTTP 통신** (Retrofit 미사용)

#### BootReceiver.kt

- `ACTION_BOOT_COMPLETED` 수신
- Android 8.0+: `startForegroundService(serviceIntent)`
- Android 7.x 이하: `startService(serviceIntent)`

---

## 4. 백엔드 (Node.js + TypeScript)

### 4.1 서버 진입점 — index.ts

`backend/src/index.ts`

- Express 5.x 사용
- 포트: `process.env.PORT || 3000`
- Health Check 엔드포인트: `GET /health` → `{ status: 'OK', message: 'Danjjak Backend is running' }`
- 라우터 마운트: `/api` → `adviceRoutes`

**실행 스크립트:**
| 명령 | 동작 |
|------|------|
| `npm run dev` | `tsx watch src/index.ts` (Hot Reload 개발) |
| `npm run build` | `tsc` (TypeScript 컴파일) |
| `npm start` | `node dist/index.js` (프로덕션 실행) |

**key 의존성:** `express@5.x`, `typescript@5.9.x`, `tsx@4.x` (dev)

---

### 4.2 라우팅 구조

| 경로 | HTTP Method | 컨트롤러 / 핸들러 | 설명 |
|------|-------------|-------------------|------|
| `/api/sensor` | POST | `adviceController.captureSensor` | 센서 데이터 수신 → L0 저장 + 조건부 푸시 |
| `/api/nudge` | GET | `adviceController.getAdvice` | 맞춤 조언 생성 요청 |
| `/api/feedback` | POST | (inline handler) | 이모지 피드백 → DPO 페르소나 업데이트 |
| `/auth/login` | POST | (inline handler) | SSO 로그인 시뮬레이션 |
| `/auth/consent` | POST | (inline handler) | 동의 상태 저장 |
| `/health` | GET | (inline handler) | 서버 상태 확인 |

> **참고**: `auth.routes.ts`와 `feedback.routes.ts`는 `index.ts`에 아직 마운트되지 않음. 현재는 `/api` 하위에 `adviceRoutes`만 연결되어 있다.

---

### 4.3 서비스 층

#### AiGatewayService (`aiGateway.service.ts`)

Privacy by Design의 핵심 구현체. 데이터가 외부 LLM으로 전송되기 전에 PII를 제거한다.

**`tokenize(text)` — NER 기반 PII 마스킹:**

```
입력: "김철수가 서울 강남구에서 010-1234-5678로 전화했다"
출력: "[PER_A3B2C]가 [LOC_X9Y8Z]에서 [TEL_D4E5F]로 전화했다"
```

| 패턴 타입 | 정규식 | 대상 |
|-----------|--------|------|
| `PER` | `/([가-힣]{2,4})/g` | 한국어 이름 |
| `LOC` | `/(서울\|부산\|...\|[가-힣]+(시\|군\|구\|동\|로))/g` | 지역명 |
| `TEL` | `/(\d{2,3}-\d{3,4}-\d{4})/g` | 전화번호 |
| `EMAIL` | `/([a-zA-Z0-9._%+-]+@...)/g` | 이메일 |

- `getOrCreateToken()`: `Math.random().toString(36)` 기반 결정론적 ID 생성, `piiMap` / `reversePiiMap`에 양방향 저장
- `rehydrate(text)`: AI 응답에서 토큰을 원래 값으로 복원
- `updatePersona(reaction)`: `LIKE` → 적극적 코치, `DISLIKE` → 조용한 조언자

**`getAdvice(context)` — Strong Chain-of-Thought 패턴:**
```
[Chain of Thought]
1. 분석: ...
2. 패턴: ...
3. 추론: ...
4. 결론: ...

[Nudge] (페르소나 스타일)

[Disclaimer] 법적/의료적 자문 아님 + VHC Log ID
```

#### MemoryService (`memory.service.ts`)

백엔드 측 LICES 계층 시뮬레이션 (인메모리).

| 계층 | 타입 | 설명 |
|------|------|------|
| `l0_storage` | `RawData[]` | 원시 센서 배열 |
| `l1_storage` | `NaturalMemory[]` | 자연어 기억 배열 |
| `l2_context` | `string` | AI 압축 컨텍스트 |

```typescript
interface RawData {
    timestamp: Date;
    type: 'GPS' | 'ACCEL' | 'APP_USAGE' | 'LIGHT';
    value: any;
}

interface NaturalMemory {
    timestamp: Date;
    event: string;
    description: string;
    tags: string[];
}
```

**파이프라인:**
1. `storeL0(data)` → L0 저장
2. 5개 단위마다 `processL0ToL1()` 트리거 → L1 자연어 기억 생성
3. L1 생성 후 `updateL2()` → L2 AI 컨텍스트 업데이트
4. `getContext()` → L2 + L1 최신 1개 조합 반환

#### PushService (`push.service.ts`)

FCM 연동 mock. 실제 FCM은 `admin.messaging().send()` 주석으로 남겨놓음.

```typescript
sendNudge(userId, title, body) → { success: true, messageId: 'fcm_mock_12345' }
```

---

### 4.4 컨트롤러

#### AdviceController (`advice.controller.ts`)

**`captureSensor(req, res)` 흐름:**
```
1. req.body에서 { type, value } 추출
2. memoryService.storeL0() 호출 → L0 저장
3. memoryService.getContext() → 컨텍스트 가져오기
4. 컨텍스트에 "활동적" 포함 시:
   → aiGateway.getAdvice(context) → 조언 생성
   → pushService.sendNudge("user_123", "단짝의 조언", 조언 마지막 줄)
5. 200 OK 응답
```

**`getAdvice(req, res)` 흐름:**
```
1. memoryService.getContext()
2. aiGateway.getAdvice(context)
3. 200 OK + { success: true, advice: "..." }
```

---

## 5. 개인정보 보호 아키텍처 (Privacy by Design)

Danjjak의 PbD는 **4단계 보호 레이어**로 구성된다:

```
[ 원시 데이터 ]
      │
      ▼
[ L0 온디바이스 저장 ] ← 원시 데이터는 기기 밖으로 나가지 않음
      │ (5개 단위 배치 처리)
      ▼
[ L1 자연어 요약 ] ← 식별 불가한 형태로 의미만 추출
      │
      ▼
[ AI Gateway (NER/PII 마스킹) ] ← 외부 전송 전 또 한 번 필터링
      │ (토큰화된 컨텍스트만 전송)
      ▼
[ 외부 LLM API ] (현재 mock)
      │ (토큰이 포함된 응답)
      ▼
[ rehydrate() ] ← 응답에서 토큰 → 원래값 복원 (앱 내에서만)
```

**사용자 데이터 흐름 원칙:**
1. 원시 GPS/센서 데이터는 반드시 기기 내 Room DB에만 저장
2. 백엔드로 전송 시 일반화된 값만 전송 (예: 정확한 좌표 → "Seoul")
3. 외부 AI 호출 시 PII를 NER로 마스킹 후 전송
4. AI 응답은 앱 내에서 `rehydrate()`로 복원
5. 모든 동의는 L0/L1/L2 별로 세분화하여 수집

---

## 6. AI 개인화 파이프라인

**온디바이스 LoRA + DPO 시뮬레이션:**

```
사용자 행동 로그
      │
      ▼
[ SensorService ] — 5분 주기 수집
      │
      ▼
[ Room DB (L0) ] → [ MemoryVectorStore (L1) ] → [ L2PersonalizationManager ]
                                                           │
                                              DPO 피드백 반영 (LIKE/DISLIKE)
                                                           │
                                              loraWeights["proactivity"] 조정
                                                           │
                                              getPersonaPrompt() → AI Gateway
                                                           │
                                                  맞춤 조언 생성 (Strong COT)
                                                           │
                                              FCM Push Notification → 사용자
```

**피드백 루프 (`/api/feedback`):**
- POST body: `{ "reaction": "LIKE" | "DISLIKE" }`
- `aiGateway.updatePersona(reaction)` → 글로벌 페르소나 상태 변경
- 다음 조언 생성 시 변경된 페르소나 스타일 적용

---

## 7. 데이터 흐름 다이어그램

```
┌─────────────────────────────────────────────────────┐
│                   Android App                        │
│                                                      │
│  SensorService (5min loop)                          │
│      └── GPS, APP_USAGE → Room DB (L0)              │
│      └── 일반화 data → POST /api/sensor             │
│                                                      │
│  UI (사용자 직접 요청)                              │
│      └── GET /api/nudge → 조언 표시                 │
│      └── POST /api/feedback → 페르소나 업데이트     │
└─────────────────────────────────────────────────────┘
         │                        ▲
         │ HTTP                   │ JSON Response
         ▼                        │
┌──────────────────────────────────────────────────────┐
│               Node.js Backend (port 3000)             │
│                                                       │
│  AdviceController                                     │
│      ├── captureSensor() → memoryService.storeL0()   │
│      │   └── 조건부: aiGateway.getAdvice()           │
│      │       └── pushService.sendNudge()             │
│      └── getAdvice() → aiGateway.getAdvice()         │
│                                                       │
│  AiGatewayService                                     │
│      ├── tokenize() — PII 마스킹                     │
│      ├── getAdvice() — COT 조언 생성 (mock LLM)      │
│      └── rehydrate() — 토큰 복원                     │
│                                                       │
│  MemoryService                                        │
│      ├── L0 → L1 → L2 파이프라인                    │
│      └── getContext() — AI 컨텍스트 조합             │
└──────────────────────────────────────────────────────┘
```

---

## 8. 미구현 / 시뮬레이션 항목

현재 코드는 MVP 단계로, 다음 항목은 **시뮬레이션 또는 미구현** 상태이다:

| 항목 | 현재 상태 | 실제 구현 시 필요 내용 |
|------|-----------|------------------------|
| FCM 푸시 알림 | mock (`fcm_mock_12345`) | Firebase Admin SDK + FCM Token 관리 |
| LLM API 연동 | 하드코딩된 COT 응답 | Gemini API / OpenAI API 연동 |
| 실제 LoRA 파인튜닝 | `loraWeights` Map 시뮬레이션 | MediaPipe LLM Inference API / TFLite |
| 벡터 임베딩 | 문자 코드 기반 mock 벡터 | on-device Text Embedding 모델 |
| Semantic Search | 텍스트 포함 검색 | 코사인 유사도 + FAISS/Annoy 라이브러리 |
| SSO 로그인 | `onLoginSuccess()` 직접 호출 | Kakao SDK, Google Sign-In 연동 |
| 기록 저장 버튼 | `{}` 빈 람다 | Room DB 또는 Backend API 저장 로직 |
| 백엔드 auth/feedback 라우트 | 정의되었으나 index.ts에 미마운트 | `app.use('/auth', authRoutes)` 추가 필요 |
| PII 마스킹 정확도 | 단순 regex | NER 딥러닝 모델 (spaCy, BERT-NER 등) |
| 실제 센서 수집 | mock GPS/앱사용 JSON 하드코딩 | `LocationManager`, `UsageStatsManager` API |

---

## 9. 파일 트리 요약

```
Danjjak/
├── README.md
├── research.md                          ← 이 문서
│
├── backend/                             ← Node.js + TypeScript 백엔드
│   ├── package.json
│   ├── tsconfig.json
│   └── src/
│       ├── index.ts                     ← Express 서버 진입점
│       ├── controllers/
│       │   └── advice.controller.ts     ← 센서 수신 + 조언 생성 컨트롤러
│       ├── routes/
│       │   ├── advice.routes.ts         ← /api/sensor, /api/nudge
│       │   ├── auth.routes.ts           ← /auth/login, /auth/consent (미마운트)
│       │   └── feedback.routes.ts       ← /api/feedback (미마운트)
│       └── services/
│           ├── aiGateway.service.ts     ← NER PII 마스킹 + COT 조언 생성
│           ├── memory.service.ts        ← L0/L1/L2 메모리 파이프라인
│           └── push.service.ts          ← FCM Push Notification (mock)
│
└── frontend/                            ← Android 앱 (Kotlin + Jetpack Compose)
    ├── build.gradle
    ├── settings.gradle
    └── app/
        ├── build.gradle
        └── src/main/
            ├── AndroidManifest.xml
            └── java/com/danjjak/
                ├── MainActivity.kt      ← 앱 진입점, 내비게이션 상태 머신
                ├── data/
                │   ├── L0/
                │   │   ├── SensorData.kt    ← Room Entity
                │   │   ├── SensorDao.kt     ← Room DAO
                │   │   └── AppDatabase.kt   ← Room DB Singleton
                │   └── L1/
                │       └── MemoryVectorStore.kt  ← FAISS 시뮬레이션 벡터 저장소
                ├── intelligence/
                │   └── L2PersonalizationManager.kt  ← LoRA/DPO 시뮬레이션
                ├── service/
                │   ├── SensorService.kt     ← Foreground Service (5분 주기 수집)
                │   └── BootReceiver.kt      ← 부팅 시 서비스 자동 시작
                └── ui/
                    ├── theme/
                    │   ├── Theme.kt         ← Material 3 컬러 시스템
                    │   └── Typography.kt
                    ├── auth/
                    │   ├── LoginScreen.kt   ← SSO 로그인 화면
                    │   └── ConsentScreen.kt ← L0/L1/L2 동의 화면
                    ├── dashboard/
                    │   ├── DashboardScreen.kt        ← 대시보드 진입점
                    │   └── DigitalTwinDashboard.kt   ← 3D 실루엣 + 그래프 UI
                    ├── registration/
                    │   └── EventRegistrationScreen.kt ← 일상 기록 화면
                    └── timeline/
                        └── TimelineScreen.kt         ← 달력 + 타임라인 뷰
```

---

*본 문서는 현재 구현된 코드를 기반으로 작성된 분석 문서이며,*
*미구현 항목은 향후 개발 로드맵에서 순차적으로 완성될 예정입니다.*
