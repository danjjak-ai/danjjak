import { Router } from 'express';
import { adviceController } from '../controllers/advice.controller.js';

const router = Router();

router.post('/sensor', adviceController.captureSensor);
router.get('/nudge', adviceController.getAdvice);

export default router;
