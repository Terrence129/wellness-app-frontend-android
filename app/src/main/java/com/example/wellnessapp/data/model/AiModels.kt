// Team5
// @author: Deng Yunke

package com.example.wellnessapp.data.model

/**
 * Request for generating AI wellness advice.
 *
 */
data class AiAdviceRequest(
    val startDate: String,
    val endDate: String
)

/**
 * AI wellness advice returned by the backend.
 *
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
 */
data class ChatRequest(
    val conversationId: String? = null,
    val message: String
)

/**
 * One message returned in the chatbot conversation history.
 *
 */
data class ChatMessageResponse(
    val role: String,
    val content: String,
    val modelName: String? = null,
    val createdAt: String? = null
)

/**
 * Chat conversation summary returned by the chatbot history endpoint.
 */
data class ChatConversationResponse(
    val conversationId: String = "",
    val startedAt: String? = null,
    val lastMessageAt: String? = null,
    val messageCount: Int = 0,
    val lastRole: String? = null,
    val lastMessagePreview: String? = null
)

/**
 * Chatbot response returned by the Spring Boot backend.
 *
 */
data class ChatResponse(
    val conversationId: String = "",
    val reply: String = "",
    val requestId: String? = null,
    val modelName: String? = null,
    val createdAt: String? = null,
    val messages: List<ChatMessageResponse> = emptyList()
)
