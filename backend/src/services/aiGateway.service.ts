import { GoogleGenerativeAI, GenerativeModel } from '@google/generative-ai';
import 'dotenv/config';

/**
 * AI Gateway Service
 * Implements:
 *   1. Deterministic Tokenization & NER-based PII Masking (PbD Principle)
 *   2. Real Gemini API integration with Strong Chain-of-Thought prompting
 *   3. DPO-style persona update based on user feedback
 */

export class AiGatewayService {
    private piiMap: Map<string, string> = new Map();
    private reversePiiMap: Map<string, string> = new Map();
    private userPersona: string = '따뜻하고 공감하며 대화하는 친구';

    // Gemini API 클라이언트
    private genAI: GoogleGenerativeAI | null = null;
    private model: GenerativeModel | null = null;

    constructor() {
        const apiKey = process.env.GEMINI_API_KEY;
        if (apiKey && apiKey !== 'your_gemini_api_key_here') {
            this.genAI = new GoogleGenerativeAI(apiKey);
            const modelName = process.env.GEMINI_MODEL || 'gemini-1.5-flash';
            this.model = this.genAI.getGenerativeModel({ model: modelName });
            console.log(`[AI Gateway] Gemini API 연결 완료 (모델: ${modelName})`);
        } else {
            console.warn('[AI Gateway] GEMINI_API_KEY가 설정되지 않았습니다. Mock 모드로 동작합니다.');
        }
    }

    // ─────────────────────────────────────────────────────────
    // 1. NER 기반 PII 마스킹 파이프라인
    // ─────────────────────────────────────────────────────────

    /**
     * Named Entity Recognition(NER) & PII Masking
     * 개인 식별 정보를 결정론적 토큰으로 치환합니다.
     */
    public tokenize(text: string): string {
        let tokenizedText = text;

        const patterns: Record<string, RegExp> = {
            TEL: /(\d{2,3}-\d{3,4}-\d{4})/g,
            EMAIL: /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g,
            LOC: /(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주|[가-힣]+(시|군|구|동|로))/g,
            PER: /([가-힣]{2,4})/g,
        };

        // 순서 중요: TEL, EMAIL을 먼저 처리해야 PER 패턴이 오탐하지 않음
        Object.entries(patterns).forEach(([type, regex]) => {
            tokenizedText = tokenizedText.replace(regex, (match) => {
                if (['오늘', '내일', '우리', '사람', '이번', '지금', '어제', '모두'].includes(match)) {
                    return match;
                }
                const token = `[${type}_${this.getOrCreateToken(match, type)}]`;
                return token;
            });
        });

        return tokenizedText;
    }

    private getOrCreateToken(value: string, type: string): string {
        if (this.piiMap.has(value)) {
            return this.piiMap.get(value)!;
        }
        const id = Math.random().toString(36).substring(2, 7).toUpperCase();
        this.piiMap.set(value, id);
        this.reversePiiMap.set(`[${type}_${id}]`, value);
        return id;
    }

    public rehydrate(text: string): string {
        let result = text;
        this.reversePiiMap.forEach((originalValue, token) => {
            result = result.split(token).join(originalValue);
        });
        return result;
    }

    // ─────────────────────────────────────────────────────────
    // 2. DPO 기반 페르소나 업데이트
    // ─────────────────────────────────────────────────────────

    public updatePersona(reaction: 'LIKE' | 'DISLIKE') {
        if (reaction === 'LIKE') {
            this.userPersona = '적극적으로 행동을 권장하고 동기를 부여하는 코치';
        } else {
            this.userPersona = '조용하게 관찰하며 꼭 필요한 핵심만 말하는 조언자';
        }
        console.log(`[AI Gateway] 페르소나 업데이트: "${this.userPersona}"`);
    }

    // ─────────────────────────────────────────────────────────
    // 3. Gemini API를 통한 Strong COT 조언 생성
    // ─────────────────────────────────────────────────────────

    /**
     * 실제 Gemini API를 호출하여 Strong Chain-of-Thought 방식의 조언을 생성합니다.
     * API Key가 없을 경우 Mock 응답을 반환합니다.
     */
    public async getAdvice(context: string): Promise<string> {
        // PII 마스킹
        const tokenizedContext = this.tokenize(context);
        console.log(`[AI Gateway] 토큰화된 컨텍스트:\n${tokenizedContext}`);

        let rawResponse: string;

        if (this.model) {
            rawResponse = await this.callGemini(tokenizedContext);
        } else {
            rawResponse = this.getMockResponse();
        }

        // AI 응답의 토큰을 원래 값으로 복원
        return this.rehydrate(rawResponse);
    }

    /**
     * Gemini API 실제 호출
     * Strong COT + 페르소나 + 면책 조항을 포함한 시스템 프롬프트 사용
     */
    private async callGemini(tokenizedContext: string): Promise<string> {
        const systemPrompt = `당신은 '단짝'이라는 개인 AI 동반자입니다.
당신의 현재 스타일: "${this.userPersona}"

다음 규칙을 반드시 지켜 응답하세요:

1. **Chain-of-Thought (COT) 형식**으로 반드시 아래 구조를 따르세요:
   [분석] 사용자의 현재 상황을 객관적으로 분석
   [패턴] 발견된 행동/활동 패턴
   [추론] 사용자에게 필요한 것에 대한 추론
   [결론] 제안할 행동이나 조언
   [조언] 위 분석을 바탕으로 "${this.userPersona}" 스타일로 전달하는 따뜻하고 실용적인 조언 (2-3문장, 구체적이고 실행 가능)
   [면책] "(이 조언은 의료/법률 자문이 아니며, AI가 생성한 일반적인 제안입니다.)"

2. 응답은 한국어로 작성하세요.
3. [조언] 섹션은 반드시 "${this.userPersona}" 페르소나를 유지하세요.
4. 주어진 컨텍스트에 없는 내용을 지어내지 마세요.
5. 컨텍스트가 부족하다면 일반적인 웰빙 조언을 제공하세요.`;

        const userMessage = `사용자 컨텍스트:
${tokenizedContext}

위 컨텍스트를 분석하여 COT 형식으로 맞춤 조언을 작성해주세요.`;

        try {
            const result = await this.model!.generateContent([
                { text: systemPrompt },
                { text: userMessage },
            ]);
            const response = result.response;
            const text = response.text();
            console.log(`[AI Gateway] Gemini 응답 수신 완료 (${text.length}자)`);
            return text;
        } catch (error: any) {
            console.error('[AI Gateway] Gemini API 호출 실패:', error.message);
            // API 오류 시 mock으로 폴백
            return this.getMockResponse();
        }
    }

    /**
     * API Key 없거나 API 오류 시 사용하는 Mock 응답
     */
    private getMockResponse(): string {
        return `[분석]
사용자의 현재 활동 데이터가 수집되고 있습니다. 주로 저녁 시간대에 활동량이 집중되어 있으며, 특정 장소에서의 앱 사용 시간이 증가하는 추세입니다.

[패턴]
평소 운동 루틴이 정해진 시간에 이루어지고 있으나, 오늘은 해당 패턴에서 벗어난 상태입니다. 앱 사용 빈도가 평균보다 높습니다.

[추론]
신체 활동이 부족한 상태에서 스크린 타임이 늘어나면 에너지 소진과 집중력 저하로 이어질 수 있습니다.

[결론]
짧은 스트레칭이나 산책으로 기분을 환기하는 것이 현재 가장 필요합니다.

[조언]
지금 5분만 자리에서 일어나 간단한 스트레칭을 해보는 건 어떨까요? 작은 움직임이 오늘의 에너지를 다시 채워줄 거예요. 몸이 가벼워지면 나머지 일도 훨씬 잘 될 거예요! 💪

[면책]
(이 조언은 의료/법률 자문이 아니며, AI가 생성한 일반적인 제안입니다.)`;
    }
}

export const aiGateway = new AiGatewayService();
