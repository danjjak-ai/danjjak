package com.danjjak.data.remote.dto

data class JournalRequest(val text: String, val tags: List<String>)
data class JournalResponse(val success: Boolean, val id: String? = null, val error: String? = null)

data class AdviceResponse(val success: Boolean, val advice: String? = null, val error: String? = null)

data class FeedbackRequest(val reaction: String)
data class FeedbackResponse(val success: Boolean, val message: String? = null, val error: String? = null)

data class MealRequest(val text: String, val userId: String = "user_123")
data class MealResponse(val success: Boolean, val data: MealDataContent? = null, val error: String? = null)
data class MealDataContent(
    val time: String,
    val location: String,
    val menu: List<String>,
    val estimatedCalories: Int,
    val nutrients: Nutrients,
    val recommendation: String,
    val searchKeywords: List<String>
)
data class Nutrients(val carbs: String, val protein: String, val fat: String)
