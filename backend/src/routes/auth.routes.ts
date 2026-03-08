import { Router } from 'express';

const router = Router();

/**
 * SSO Login & Profile Creation
 */
router.post('/login', (req, res) => {
    // Simulate SSO success
    res.status(200).json({
        success: true,
        token: 'eyHabcd1234...',
        user: { name: 'Jaehun', email: 'user@example.com' }
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
