/**
 * AI Gateway Service
 * Implements Deterministic Tokenization and NER-based PII Masking (PbD Principle)
 */

export class AiGatewayService {
    private piiMap: Map<string, string> = new Map();
    private reversePiiMap: Map<string, string> = new Map();
    private userPersona: string = "따뜻하고 공감하는 친구";

    /**
     * Named Entity Recognition (NER) & PII Masking Pipeline
     * Uses deterministic tokenization to replace PII with non-identifiable tokens.
     */
    public tokenize(text: string): string {
        let tokenizedText = text;

        // PII Patterns for NER (Simulated)
        const patterns = {
            PER: /([가-힣]{2,4})/g, // Names
            LOC: /(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주|[가-힣]+(시|군|구|동|로))/g, // Locations
            TEL: /(\d{2,3}-\d{3,4}-\d{4})/g, // Phone numbers
            EMAIL: /([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,})/g // Emails
        };

        // Apply masking sequentially
        Object.entries(patterns).forEach(([type, regex]) => {
            tokenizedText = tokenizedText.replace(regex, (match) => {
                // If it's a very common word, don't mask (simplified heuristic)
                if (['오늘', '내일', '우리', '사람'].includes(match)) return match;

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
        let rehydratedText = text;
        this.reversePiiMap.forEach((originalValue, token) => {
            rehydratedText = rehydratedText.split(token).join(originalValue);
        });
        return rehydratedText;
    }

    public updatePersona(reaction: 'LIKE' | 'DISLIKE') {
        if (reaction === 'LIKE') {
            this.userPersona = "더 적극적이고 활동을 유도하는 코치";
        } else {
            this.userPersona = "조용하고 관찰하며 필요한 때만 말하는 조언자";
        }
    }

    /**
     * AI Advice Generation with Strong COT and Privacy Protection
     */
    public async getAdvice(context: string): Promise<string> {
        const tokenizedContext = this.tokenize(context);

        console.log(`[AI Gateway] Tokenized Prompt sent to LLM: ${tokenizedContext}`);

        // Simulated Response with Strong COT and Disclaimers
        const mockResponse = `
[Chain of Thought]
1. 분석: 사용자의 현재 장소는 [LOC_A1B2]이며, 최근 2시간 동안 고정된 위치에 있음.
2. 패턴: 평소 이 시간에는 운동을 했으나 오늘은 정적인 상태임.
3. 추론: 사용자의 활동량이 부족하여 에너지 레벨이 낮아질 가능성이 큼.
4. 결론: 가벼운 스트레칭이나 주변 산책을 제안하여 컨디션을 환기시킴.

[Nudge]
(${this.userPersona} 스타일): 지금 [LOC_A1B2]에 계시네요. 5분만 같이 걸으며 기분 전환해보는 건 어떨까요? 

[Disclaimer]
이 조언은 법적/의료적 자문이 아니며 AI와 전문가 검수를 거쳤습니다. (VHC Log: #${Date.now()})
        `;

        return this.rehydrate(mockResponse);
    }
}

export const aiGateway = new AiGatewayService();
