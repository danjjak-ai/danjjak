# Danjjak (단짝) - AI-Native Life Context Assistant

![Danjjak Banner](https://img.shields.io/badge/AI--Native-Life--Context-6750A4?style=for-the-badge)
![Frontend](https://img.shields.io/badge/React-Ionic-3880FF?style=for-the-badge&logo=ionic)
![Native Bridge](https://img.shields.io/badge/Capacitor-Android%2FiOS-119EFF?style=for-the-badge&logo=capacitor)
![Backend](https://img.shields.io/badge/Node.js-Express-green?style=for-the-badge&logo=nodedotjs)

**단짝(Danjjak)**은 사용자의 일상을 기록하고 분석하여 웰빙을 향상시키는 AI 네이티브 라이프 컨텍스트 어시스턴트입니다. 이제 웹 기반 크로스 플랫폼 기술을 통해 Android와 iOS 모두에서 동일하게 아름다운 사용자 경험과 강력한 지능형 기능을 제공합니다.

---

## ✨ 주요 기능 (Key Features)

### 📊 디지털 트윈 대시보드 (Dashboard)
- 사용자의 활동 상태를 시각화한 애니메이션 대시보드.
- 오늘 하루의 걸음 수, 에너지 상태, 스트레스 지수 실시간 모니터링.
- **AI 맞춤 조언**: 실시간 데이터를 분석하여 최적의 생활 가이드 제공.

### ✍️ 스마트 기록하기 (Registration)
- **AI 트리거 제안**: 어제의 사진, 방문한 장소 등을 단짝이 먼저 기억하고 기록을 유도합니다.
- **자연어 식사 기록**: "점심에 사이제리야에서 치킨이랑 도리아를 먹었어"처럼 편하게 말하듯 기록하세요.
- **데이터 저장**: 모든 기록은 태깅 시스템을 통해 구조화되어 저장됩니다.

### 📅 라이프 타임라인 (Timeline)
- 일상의 흐름을 시간순으로 정교하게 시각화.
- 카테고리별(건강, 미디어, 장소, 학습 등) 자동 컬러 태깅 및 이벤트 그룹화.
- 월별 캘린더를 통해 과거의 소중한 순간들을 쉽게 탐색.

---

## 🏗️ 아키텍처: LICES (Layered Intelligence)
- **L0 (Raw)**: Capacitor 플러그인을 통한 GPS, Health Connect(걸음 수) 등 원시 센싱.
- **L1 (Natural Language)**: 수집된 원시 데이터를 "공원에서의 산책"과 같은 자연어 이벤트로 변환.
- **L2 (AI-Native)**: 개인화된 컨텍스트 학습 및 지능형 넛지(Nudges) 생성.

---

## 🔒 보안 및 프라이버시 (Privacy by Design)
- **Privacy Consent**: 사용자가 직접 제어하는 단계별(L0/L1/L2) 데이터 수집 동의 시스템.
- **On-Device First**: 민감한 데이터는 기기 내에서 처리하며, 외부 AI 전달 시 익명화 처리를 수행합니다.

---

## 📂 프로젝트 구조 (Project Structure)
```text
Danjjak/
├── app/               # 프론트엔드 (Vite + React + Ionic + Capacitor)
│   ├── android/       # Android 네이티브 프로젝트
│   ├── ios/           # iOS 네이티브 프로젝트
│   └── src/           # React 소스코드 및 서비스 레이어
├── backend/           # 백엔드 서버 (Node.js + Express)
└── run_web_app.bat    # 통합 실행 스크립트 (Windows)
```

---

## 🚀 빠른 시작 (Getting Started)

### 1. 통합 실행 (Windows)
루트 폴더에서 `run_web_app.bat`을 실행하면 백엔드와 프론트엔드 개발 서버가 동시에 구동됩니다.
- **브라우저 접속**: [http://localhost:5173](http://localhost:5173)

### 2. 수동 실행
**Backend:**
```bash
cd backend
npm install
npm run dev
```

**Frontend (Web/Mobile):**
```bash
cd app
npm install
npm run dev        # 브라우저 개발용
npx cap run android # 안드로이드 기기 실행
```

---

## 🎨 디자인 철학 (Design Aesthetic)
- **Premium Dark Mode**: 보라색 계열의 깊이 있는 다크 테마와 세련된 그라데이션.
- **Glassmorphism**: 투명도와 블러 효과를 활용한 모던하고 입체적인 카드 UI.
- **Micro-Animations**: 사용자의 상호작용에 반응하는 부드러운 애니메이션과 트랜지션.

---

## 📄 라이선스
Copyright © 2026 Danjjak Team. All rights reserved.
