package com.example.wellnessapp.data.model

/**
 * Request for generating AI wellness advice.
 *
 * @author Yunke Deng
 */
data class AiAdviceRequest(
    val startDate: String,
    val endDate: String
)

/**
 * AI wellness advice returned by the backend.
 *
 * @author Yunke Deng
 */
data class AiAdviceResponse(
    val id: Long? = null,
    val adviceDate: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val adviceText: String = "",
    val modelName: String? = null,
    val createdAt: String? = null
)

// 暂时保留原有 Chatbot models
data class ChatRequest(
    val message: String
)

data class ChatResponse(
    val reply: String,
    val createdAt: String?
)

data class ChatMessageResponse(
    val id: Long,
    val sender: String,
    val message: String,
    val createdAt: String
)