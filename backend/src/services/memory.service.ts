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

        // Trigger background analysis to L1 if threshold met (simulated)
        if (this.l0_storage.length % 5 === 0) {
            await this.processL0ToL1();
        }
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

        const memory: NaturalMemory = {
            timestamp: new Date(),
            event: "Activity Detected",
            description: `사용자가 ${lastData.type} 기반으로 특정 활동을 수행함.`,
            tags: ["daily", "auto-captured"]
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
        this.l2_context = `사용자는 주로 저녁 시간에 활동적이며, 특정 장소에서 앱 사용량이 높음. (L2 State Updated)`;
        console.log(`[L2] AI-Native Context updated.`);
    }

    public getContext(): string {
        return this.l2_context + "\nRecent History: " + this.l1_storage.slice(-1).map(m => m.description).join(", ");
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
