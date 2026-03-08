import type { Request, Response } from 'express';
import { memoryService } from '../services/memory.service.js';
import { aiGateway } from '../services/aiGateway.service.js';

export class AdviceController {
    /**
     * Handle sensor data from mobile
     */
    public async captureSensor(req: Request, res: Response) {
        try {
            const { type, value } = req.body;
            await memoryService.storeL0({
                timestamp: new Date(),
                type,
                value
            });
            res.status(200).json({ success: true, message: 'Sensor data captured' });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Failed to capture sensor data' });
        }
    }

    /**
     * Get personalized advice with Strong COT
     */
    public async getAdvice(req: Request, res: Response) {
        try {
            const context = memoryService.getContext();
            const advice = await aiGateway.getAdvice(context);
            res.status(200).json({ success: true, advice });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Failed to get advice' });
        }
    }
}

export const adviceController = new AdviceController();
