import { Router } from 'express';
import { aiGateway } from '../services/aiGateway.service.js';

const router = Router();

/**
 * Handle User Feedback (Emoji reactions)
 * This feeds into the DPO (Direct Preference Optimization) flow
 */
router.post('/', async (req, res) => {
    try {
        const { reaction } = req.body;

        console.log(`[Feedback] Received reaction: ${reaction}`);

        // Dynamic Personalization (DPO style)
        if (reaction === 'LIKE' || reaction === 'DISLIKE') {
            aiGateway.updatePersona(reaction);
        }

        res.status(200).json({ success: true, message: 'Feedback recorded' });
    } catch (error) {
        res.status(500).json({ success: false, error: 'Failed to record feedback' });
    }
});

export default router;
