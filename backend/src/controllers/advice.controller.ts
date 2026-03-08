import type { Request, Response } from 'express';
import { memoryService } from '../services/memory.service.js';
import { aiGateway } from '../services/aiGateway.service.js';
import { pushService } from '../services/push.service.js';

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

            // Proactive Logic: If a certain pattern is detected, push advice immediately
            const context = memoryService.getContext();
            if (context.includes("활동적")) { // Example trigger
                const advice = await aiGateway.getAdvice(context);
                await pushService.sendNudge("user_123", "단짝의 조언", advice.split("\n").pop() || "");
            }

            res.status(200).json({ success: true, message: 'Sensor data captured' });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Failed to capture sensor data' });
        }
    }

    /**
     * Get personalized advice with Strong COT (Manual Request)
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
