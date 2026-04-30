/**
 * Memory Service (LICES Architecture)
 * L0: Raw Sensor Data
 * L1: Natural Language Summaries
 * L2: AI-Native Context
 */

export interface RawData {
    timestamp: Date;
    type: 'GPS' | 'ACCEL' | 'APP_USAGE' | 'LIGHT' | 'MEAL';
    value: any;
}

export interface NaturalMemory {
    timestamp: Date;
    event: string;
    description: string;
    tags: string[];
}

export class MemoryService {
    private l0_storage: RawData[] = [];
    private l1_storage: NaturalMemory[] = [];
    private l2_context: string = ""; // AI-Native compressed context (simulated)

    /**
     * Store Raw Data (L0)
     */
    public async storeL0(data: RawData) {
        this.l0_storage.push(data);
        console.log(`[L0] Raw data captured: ${data.type}`);

        // 즉시 L1으로 변환하여 반영되도록 수정 (임계값 제거)
        await this.processL0ToL1();
    }

    /**
     * Store Journal Entry (L1)
     */
    public async storeJournal(text: string, tags: string[]): Promise<NaturalMemory> {
        const memory: NaturalMemory = {
            timestamp: new Date(),
            event: "Journal Entry",
            description: text,
            tags: ["daily", "manual", ...tags]
        };
        this.l1_storage.push(memory);
        console.log(`[Journal] Saved and added to L1: ${text}`);
        await this.updateL2();
        return memory;
    }

    /**
     * Process Raw Data to Natural Language (L1)
     */
    private async processL0ToL1() {
        const lastData = this.l0_storage[this.l0_storage.length - 1];
        if (!lastData) return;

        let description = `사용자가 ${lastData.type} 기반으로 특정 활동을 수행함.`;
        let event = "Activity Detected";

        if (lastData.type === 'MEAL') {
            event = "Meal Captured";
            description = `식사 기록: ${lastData.value.menu?.join(', ') || '알 수 없는 메뉴'} (${lastData.value.estimatedCalories || 0}kcal)`;
        }

        const memory: NaturalMemory = {
            timestamp: new Date(),
            event: event,
            description: description,
            tags: ["daily", lastData.type === 'MEAL' ? "auto-analyzed" : "auto-captured"]
        };
        this.l1_storage.push(memory);
        console.log(`[L1] Natural Language Memory created: ${memory.event}`);

        await this.updateL2();
    }

    /**
     * Update AI-Native Memory (L2)
     */
    private async updateL2() {
        // In a real app, this would be a prompt to an LLM to "compress" the L1 memories
        const recentActivities = this.l1_storage.slice(-3).map(m => m.description).join(". ");
        this.l2_context = `최근 활동 요약: ${recentActivities}. (L2 State Updated)`;
        console.log(`[L2] AI-Native Context updated.`);
    }

    public getContext(): string {
        return this.l2_context + "\nRecent History: " + this.l1_storage.slice(-1).map(m => m.description).join(", ");
    }

    public getHistory(): NaturalMemory[] {
        return [...this.l1_storage].sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    }
}

export const memoryService = new MemoryService();

/**
 * Memory Service Registry for per-user isolation
 */
class MemoryServiceRegistry {
    private instances: Map<string, MemoryService> = new Map();

    public getForUser(userId: string): MemoryService {
        if (!this.instances.has(userId)) {
            this.instances.set(userId, new MemoryService());
        }
        return this.instances.get(userId)!;
    }
}

export const memoryRegistry = new MemoryServiceRegistry();
