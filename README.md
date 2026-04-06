# Danjjak (단짝) - AI-Native Life Context Assistant

![Danjjak Banner](https://img.shields.io/badge/AI--Native-Life--Context-6750A4?style=for-the-badge)
![Tech Stack](https://img.shields.io/badge/Kotlin-Jetpack--Compose-blue?style=for-the-badge&logo=kotlin)
![Backend](https://img.shields.io/badge/Node.js-Express-green?style=for-the-badge&logo=nodedotjs)

**단짝(Danjjak)**은 사용자의 일상을 기록하고 분석하여 웰빙을 향상시키는 AI 네이티브 라이프 컨텍스트 어시스턴트입니다. 온디바이스 머신러닝을 활용하여 맞춤형 성찰 제안을 제공하고, 하루의 흐름을 아름다운 타임라인으로 시각화합니다.

## ✨ 주요 기능 (Key Features)

### 📊 대시보드 (Dashboard)
- 오늘 하루의 활동량(걸음 수) 분석 및 인사이트 제공
- 데이터 기반의 개인화된 건강 및 생활 조언

### ✍️ 기록하기 (Registration)
- **AI 맞춤 제안**: 사진, 장소, 함께한 인물 등 최근 활동을 기반으로 한 글쓰기 영감 제공
- **식사 및 영양 관리**: "12시에 사이제리야에서 치킨이랑 도리아를 먹었어"와 같은 자연어 입력 지원.
- **수동 센싱 Nudge**: 식사 시간이 지나도 기록이 없으면 단짝이 먼저 물어봅니다.

### 🍱 인텔리전트 영양 코칭
- 입력된 식사 내용을 분석하여 칼로리 및 탄/단/지 영양소 추정.
- 부족하거나 과할 경우 다음 식사 메뉴 추천 및 **맞춤형 주변 식당 찾기** 기능 연동.

### 📅 시계열 타임라인 (Timeline)
- 월별 캘린더를 통한 한눈에 보는 일상 기록
- 시간대별 캡처된 이벤트 및 활동의 상세 시각화
- 카테고리별(운동, 장소, 미디어 등) 자동 분류 및 컬러 태깅

## 🏗️ 아키텍처: LICES (Layered Intelligence)
- **L0 (Raw)**: 센서 데이터를 통한 환경 및 활동 감지 (`SensorService.kt`)
- **L1 (Natural Language)**: 감지된 데이터를 기반으로 자연어 이벤트 생성
- **L2 (AI-Native)**: 개인 특화된 컨텍스트 압축 및 지능형 알림(Nudges)

## 🔒 보안: AI Gateway
개인정보 보호를 위해 AI Gateway를 통한 비식별화 처리를 수행합니다.
- **Deterministic Tokenization**: 위치나 인명 등 민감 정보를 `[LOCATION_1]`, `[PERSON_1]`과 같이 토큰화하여 외부 AI 모델에 전달
- **Re-hydration**: AI 결과 수신 후 사용자의 기기에서 원본 정보를 복원하여 개인화된 경험 유지

## 🚀 빠른 시작 (Getting Started)

### Backend (Node.js)
```bash
cd backend
npm install
npm run dev
```

### Frontend (Android)
1. Android Studio에서 `frontend/app` 폴더를 엽니다.
2. `SensorService.kt`의 서버 URL을 본인의 백엔드 IP로 수정합니다.
3. 프로젝트를 빌드하고 장치 또는 에뮬레이터에서 실행합니다.

## 🎨 디자인 철학 (Design Aesthetic)
- **Expensive & Humanistic**: 보라색 계열의 풍부한 그라데이션, 유려한 카드 UI, 부드러운 그림자와 현대적인 타이포그래피를 사용하여 프리미엄한 감성을 제공합니다.
