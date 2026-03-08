/**
 * AI Gateway Service
 * Handles PII Protection and dynamic personalization based on feedback (DPO style)
 */

export class AiGatewayService {
    private piiMap: Map<string, string> = new Map();
    private reversePiiMap: Map<string, string> = new Map();
    private userPersona: string = "따뜻하고 공감하는 친구"; // Default persona

    /**
     * Update user persona based on feedback
     * Simulates DPO by adjusting the "style" of the AI
     */
    public updatePersona(reaction: 'LIKE' | 'DISLIKE') {
        if (reaction === 'LIKE') {
            this.userPersona = "더 적극적이고 활동을 유도하는 코치";
        } else {
            this.userPersona = "조용하고 관찰하며 필요한 때만 말하는 조언자";
        }
    }

    public tokenize(text: string): string {
        let tokenizedText = text;
        const locationPattern = /(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)/g;

        tokenizedText = tokenizedText.replace(locationPattern, (match) => {
            const token = `[LOCATION_${this.getOrCreateToken(match, 'LOC')}]`;
            return token;
        });

        return tokenizedText;
    }

    private getOrCreateToken(value: string, type: string): string {
        if (this.piiMap.has(value)) {
            return this.piiMap.get(value)!;
        }
        const id = (this.piiMap.size + 1).toString();
        this.piiMap.set(value, id);
        this.reversePiiMap.set(`[${type}_${id}]`, value);
        return id;
    }

    public rehydrate(text: string): string {
        let rehydratedText = text;
        this.reversePiiMap.forEach((originalValue, token) => {
            rehydratedText = rehydratedText.split(token).join(originalValue);
        });
        return rehydratedText;
    }

    /**
     * AI Call with Dynamic Persona & Strong COT
     */
    public async getAdvice(context: string): Promise<string> {
        const tokenizedContext = this.tokenize(context);

        console.log(`[AI Gateway] Persona: ${this.userPersona}`);
        console.log(`[AI Gateway] Context: ${tokenizedContext}`);

        // Mocking AI Response with Persona-driven Strong COT
        const mockResponse = `
[Chain of Thought]
1. 분석: 사용자의 현재 컨텍스트는 "${tokenizedContext}" 임.
2. 페르소나 적용: 현재 설정된 "${this.userPersona}" 모드로 응답 생성.
3. 추론: 사용자의 에너지를 관리하기 위한 조언이 필요함.

[Nudge]
(${this.userPersona} 스타일): 오늘 조금 더 움직여보는 건 어떨까요? 근처 공원 산책을 추천해요!
        `;

        return this.rehydrate(mockResponse);
    }
}

export const aiGateway = new AiGatewayService();
