/**
 * AI Gateway Service
 * Handles PII Protection via Deterministic Tokenization and interaction with LLM
 */

export class AiGatewayService {
    private piiMap: Map<string, string> = new Map();
    private reversePiiMap: Map<string, string> = new Map();

    /**
     * Deterministic Tokenization (Simplified for this phase)
     * In production, this would use Radicalbit AI Gateway integration
     */
    public tokenize(text: string): string {
        let tokenizedText = text;

        // Basic PII Patterns (Simplified)
        const namePattern = /([가-힣]{2,4})/g; // Simplified Korean names
        const locationPattern = /(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)/g;

        // Tokenize locations
        tokenizedText = tokenizedText.replace(locationPattern, (match) => {
            const token = `[LOCATION_${this.getOrCreateToken(match, 'LOC')}]`;
            return token;
        });

        // Tokenize names - this is a bit aggressive for general text, but fits the requirement
        // tokenizedText = tokenizedText.replace(namePattern, (match) => {
        //     const token = `[PERSON_${this.getOrCreateToken(match, 'PER')}]`;
        //     return token;
        // });

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

    /**
     * Re-hydrate tokens back to original values
     */
    public rehydrate(text: string): string {
        let rehydratedText = text;
        this.reversePiiMap.forEach((originalValue, token) => {
            rehydratedText = rehydratedText.split(token).join(originalValue);
        });
        return rehydratedText;
    }

    /**
     * Simulate AI Call with Strong COT
     */
    public async getAdvice(context: string): Promise<string> {
        const tokenizedContext = this.tokenize(context);

        console.log(`[AI Gateway] Sending tokenized context: ${tokenizedContext}`);

        // Mocking AI Response with Strong COT
        const mockResponse = `
[Chain of Thought]
1. 분석: 사용자가 [LOCATION_1]에서 장시간 미디어를 사용하고 있음. 
2. 패턴: 최근 3일간 평소보다 수면 시간이 1시간 감소함.
3. 추론: 현재 행위는 수면 부족으로 인한 피로도를 높일 수 있으며, 내일 오전 루틴에 지장을 줄 것으로 보임.
4. 결론: 사용자의 에너지를 보존하기 위해 활동 중단을 제안함.

[Nudge]
지금 [LOCATION_1]에서의 소셜 미디어 사용 시간이 2시간을 넘었어요. 내일 상쾌한 아침을 위해 이제 휴대폰을 내려두고 휴식을 취하는 건 어떨까요?
        `;

        const rehydratedResponse = this.rehydrate(mockResponse);
        return rehydratedResponse;
    }
}

export const aiGateway = new AiGatewayService();
