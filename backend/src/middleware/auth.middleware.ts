import jwt from 'jsonwebtoken';
import type { Request, Response, NextFunction } from 'express';

export const authenticateToken = (req: Request, res: Response, next: NextFunction) => {
    // 임시로 헤더 없이 개발/테스트가 가능하도록 mock 통과 로직을 추가
    // 실 서비스 시 삭제 권장
    const authHeader = req.headers['authorization'];
    const token = authHeader?.split(' ')[1];
    
    if (!token) {
        // 토큰이 없을 경우 Mock user (개발 편의성)
        (req as any).user = { userId: "mock_user", name: "Mock User", email: "mock@example.com" };
        return next();
        // return res.status(401).json({ error: 'Unauthorized' });
    }
    
    jwt.verify(token, process.env.JWT_SECRET || 'fallback_secret', (err, user) => {
        if (err) return res.status(403).json({ error: 'Forbidden' });
        (req as any).user = user;
        next();
    });
};
