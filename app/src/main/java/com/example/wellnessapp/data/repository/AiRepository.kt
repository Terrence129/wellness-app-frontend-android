package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.AiAdviceRequest
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.model.ChatResponse
import com.example.wellnessapp.data.network.RetrofitClient

class AiRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)

    suspend fun generateAiAdvice(
        request: AiAdviceRequest
    ): ApiResponse<AiAdviceResponse> {
        return apiService.generateAiAdvice(request)
    }

    suspend fun getLatestAiAdvice(): ApiResponse<AiAdviceResponse> {
        return apiService.getLatestAiAdvice()
    }

    suspend fun sendChatMessage(request: ChatRequest): ApiResponse<ChatResponse> {
        return apiService.sendChatMessage(request)
    }

    suspend fun getChatHistory(limit: Int? = null): ApiResponse<List<ChatMessageResponse>> {
        return apiService.getChatHistory(limit)
    }
}
