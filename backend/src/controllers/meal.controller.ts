import type { Request, Response } from 'express';
import { aiGateway } from '../services/aiGateway.service.js';
import { memoryRegistry } from '../services/memory.service.js';

export class MealController {
    /**
     * 자연어 식사 데이터 처리
     * 예: "12시에 사이제리야에서 치킨이랑 도리아를 먹었어"
     */
    public async processMealInput(req: Request, res: Response) {
        const { text, userId } = req.body;

        if (!text) {
            return res.status(400).json({ error: 'Text input is required' });
        }

        try {
            // 1. Gemini를 이용한 자연어 파싱
            const prompt = `당신은 영양사 AI입니다. 다음 문장을 분석하여 JSON 형식으로 응답하세요.
문장: "${text}"

응답 형식:
{
  "time": "HH:mm 형식",
  "location": "식사 장소",
  "menu": ["메뉴1", "메뉴2"],
  "estimatedCalories": 숫자,
  "nutrients": {
    "carbs": "그램(g)",
    "protein": "그램(g)",
    "fat": "그램(g)"
  },
  "recommendation": "이 식사 이후 다음 식사에 대한 조언 (1문장)",
  "searchKeywords": ["검색어1", "검색어2"]
}
JSON만 응답하세요.`;

            const rawResponse = await aiGateway.getAdvice(prompt);
            
            // JSON 추출 (Markdown backticks 제거 등)
            const jsonStr = rawResponse.replace(/```json|```/g, '').trim();
            const mealData = JSON.parse(jsonStr);

            // 2. 메모리 서비스에 저장
            const memoryService = memoryRegistry.getForUser(userId || 'default_user');
            memoryService.storeL0({
                timestamp: new Date(),
                type: 'MEAL',
                value: mealData
            });

            return res.status(200).json({
                success: true,
                data: mealData
            });
        } catch (error: any) {
            console.error('[MealController] Error processing meal input:', error);
            return res.status(500).json({ error: 'Failed to process meal input' });
        }
    }
}

export const mealController = new MealController();
