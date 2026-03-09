package com.danjjak.intelligence

import android.util.Log

/**
 * L2 Personalization Manager
 * Manages On-device Learning (LoRA) and Preference Optimization (DPO).
 * This layer represents the AI's "internal state" or "Persona".
 */
class L2PersonalizationManager {
    // Simulated LoRA weights representing the user's specific persona
    private var loraWeights: Map<String, Float> = mutableMapOf(
        "politeness" to 0.5f,
        "proactivity" to 0.5f,
        "detail" to 0.5f
    )

    /**
     * Update weights based on DPO (User Feedback)
     */
    fun applyDPO(feedback: FeedbackType) {
        val updatedWeights = loraWeights.toMutableMap()
        when (feedback) {
            FeedbackType.LIKE -> {
                updatedWeights["proactivity"] = (updatedWeights["proactivity"] ?: 0.5f) + 0.1f
                Log.d("L2Personalisation", "DPO: Increasing proactivity based on positive feedback.")
            }
            FeedbackType.DISLIKE -> {
                updatedWeights["proactivity"] = (updatedWeights["proactivity"] ?: 0.5f) - 0.1f
                Log.d("L2Personalisation", "DPO: Adjusting persona for more cautious advice.")
            }
        }
        loraWeights = updatedWeights
    }

    /**
     * Get the current persona parameters to inject into prompts
     */
    fun getPersonaPrompt(): String {
        val proactivity = loraWeights["proactivity"] ?: 0.5f
        return if (proactivity > 0.6f) {
            "적극적으로 행동을 권장하고 동기를 부여하는 코치 스타일"
        } else if (proactivity < 0.4f) {
            "조용하고 차분하게 관찰하며 필요한 핵심만 말하는 조언자 스타일"
        } else {
            "따뜻하고 공감하며 대화하는 친구 스타일"
        }
    }
}

enum class FeedbackType { LIKE, DISLIKE }

object PersonalizationProvider {
    val manager = L2PersonalizationManager()
}
