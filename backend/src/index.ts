import 'dotenv/config';
import express from 'express';
import adviceRoutes from './routes/advice.routes.js';
import authRoutes from './routes/auth.routes.js';
import feedbackRoutes from './routes/feedback.routes.js';
import journalRoutes from './routes/journal.routes.js';
import cors from 'cors';

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors({
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

// ─────────────────────────
// Health Check
// ─────────────────────────
app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'OK',
        message: 'Danjjak Backend is running',
        gemini: process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'your_gemini_api_key_here'
            ? 'connected'
            : 'mock mode',
        model: process.env.GEMINI_MODEL || 'gemini-1.5-flash'
    });
});

// ─────────────────────────
// API 라우터 마운트
// ─────────────────────────
app.use('/api', adviceRoutes);          // POST /api/sensor, GET /api/nudge
app.use('/api/feedback', feedbackRoutes); // POST /api/feedback
app.use('/api/journal', journalRoutes);   // POST /api/journal
app.use('/auth', authRoutes);           // POST /auth/login, POST /auth/consent

app.listen(PORT, () => {
    console.log(`[Danjjak] 서버가 포트 ${PORT}에서 실행 중입니다.`);
    console.log(`[Danjjak] Gemini 모드: ${
        process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'your_gemini_api_key_here'
            ? `실제 API (${process.env.GEMINI_MODEL || 'gemini-1.5-flash'})`
            : 'Mock 모드 (GEMINI_API_KEY를 .env에 설정하세요)'
    }`);
});
