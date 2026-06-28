package com.example.wellnessapp.data.model

data class AiAdviceRequest(
    val startDate: String,
    val endDate: String
)

data class AiAdviceResponse(
    val adviceDate: String,
    val adviceText: String
)

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
