import { Router } from 'express';
import { memoryService } from '../services/memory.service.js';

const router = Router();

router.post('/', async (req, res) => {
    try {
        const { text, tags = [] } = req.body;
        
        if (!text) {
            return res.status(400).json({ success: false, error: 'Text is required' });
        }

        const memory = await memoryService.storeJournal(text, tags);
        res.status(200).json({ success: true, id: Date.now().toString(), memory });
    } catch (error) {
        console.error('Failed to save journal:', error);
        res.status(500).json({ success: false, error: 'Failed to save journal' });
    }
});

router.get('/', (req, res) => {
    try {
        const history = memoryService.getHistory();
        res.status(200).json({ success: true, history });
    } catch (error) {
        res.status(500).json({ success: false, error: 'Failed to fetch history' });
    }
});

export default router;
