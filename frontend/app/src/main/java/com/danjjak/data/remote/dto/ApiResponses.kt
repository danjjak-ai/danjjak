package com.danjjak.data.remote.dto

data class JournalRequest(val text: String, val tags: List<String>)
data class JournalResponse(val success: Boolean, val id: String? = null, val error: String? = null)

data class AdviceResponse(val success: Boolean, val advice: String? = null, val error: String? = null)

data class FeedbackRequest(val reaction: String)
data class FeedbackResponse(val success: Boolean, val message: String? = null, val error: String? = null)
