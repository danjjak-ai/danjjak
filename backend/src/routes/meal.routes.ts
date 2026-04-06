import { Router } from 'express';
import { mealController } from '../controllers/meal.controller.js';

const router = Router();

/**
 * @route POST /api/meal
 * @desc 자연어 식사 입력 처리
 */
router.post('/', (req, res) => mealController.processMealInput(req, res));

export default router;
