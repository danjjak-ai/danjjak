package com.danjjak.data.L1

import android.util.Log

/**
 * Memory Vector Store Simulator (L1)
 * Simulates semantic search and storage of natural language memories on-device.
 */
data class SemanticMemory(
    val id: String,
    val text: String,
    val vector: List<Float>, // Simulated embedding
    val tags: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

class MemoryVectorStore {
    private val memoryStorage = mutableListOf<SemanticMemory>()

    /**
     * Store a semantic memory (L1)
     */
    fun storeMemory(text: String, tags: List<String>) {
        val simulatedVector = text.map { it.code.toFloat() / 1000f }.take(10) // Mock embedding
        val memory = SemanticMemory(
            id = java.util.UUID.randomUUID().toString(),
            text = text,
            vector = simulatedVector,
            tags = tags
        )
        memoryStorage.add(memory)
        Log.d("VectorStore", "Memory stored locally: ${text.take(20)}...")
    }

    /**
     * Search most similar memories (Simulated FAISS search)
     */
    fun search(query: String, limit: Int = 3): List<SemanticMemory> {
        // Simplified semantic search: match tags or text content
        return memoryStorage.filter { memory ->
            memory.text.contains(query, ignoreCase = true) || 
            memory.tags.any { it.contains(query, ignoreCase = true) }
        }.take(limit)
    }

    fun getAllMemories(): List<SemanticMemory> = memoryStorage
}

// Global Singleton for simplicity in this phase
object LocalVectorProvider {
    val store = MemoryVectorStore()
}
