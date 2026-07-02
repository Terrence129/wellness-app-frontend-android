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

/**
 * Request sent to the wellness chatbot.
 *
 * The first request may use a null conversationId.
 * Later requests should reuse the returned conversationId.
 *
 * @author Yunke Deng
 */
data class ChatRequest(
    val conversationId: String? = null,
    val message: String
)

/**
 * One message returned in the chatbot conversation history.
 *
 * @author Yunke Deng
 */
data class ChatMessageResponse(
    val role: String,
    val content: String,
    val modelName: String? = null,
    val createdAt: String? = null
)

/**
 * Chatbot response returned by the Spring Boot backend.
 *
 * @author Yunke Deng
 */
data class ChatResponse(
    val conversationId: String = "",
    val reply: String = "",
    val requestId: String? = null,
    val modelName: String? = null,
    val createdAt: String? = null,
    val messages: List<ChatMessageResponse> = emptyList()
)