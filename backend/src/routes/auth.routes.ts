import { Router } from 'express';
import jwt from 'jsonwebtoken';

const router = Router();

/**
 * SSO Login & Profile Creation
 */
router.post('/login', (req, res) => {
    // Simulate SSO success and generate real JWT
    const userInfo = { userId: 'google_12345', name: 'Jaehun', email: 'user@example.com' };
    const jwtToken = jwt.sign(
        userInfo,
        process.env.JWT_SECRET || 'fallback_secret',
        { expiresIn: (process.env.JWT_EXPIRES_IN || '7d') as any }
    );

    res.status(200).json({
        success: true,
        token: jwtToken,
        user: userInfo
    });
});

/**
 * Store Consent Status
 */
router.post('/consent', (req, res) => {
    const { consent_status } = req.body;
    console.log(`[Auth] Consent stored: ${JSON.stringify(consent_status)}`);
    res.status(200).json({ success: true });
});

export default router;
