// Team5
// @author: Deng Yunke

package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.AiAdviceRequest
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.ChatConversationResponse
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.model.ChatResponse
import com.example.wellnessapp.data.model.PageResponse
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

    suspend fun getAiAdviceHistory(
        startDate: String?,
        endDate: String?,
        page: Int,
        size: Int,
        sort: String
    ): ApiResponse<PageResponse<AiAdviceResponse>> {
        return apiService.getAiAdviceHistory(
            startDate = startDate,
            endDate = endDate,
            page = page,
            size = size,
            sort = sort
        )
    }

    suspend fun getAiAdviceDetail(id: Long): ApiResponse<AiAdviceResponse> {
        return apiService.getAiAdviceDetail(id)
    }

    suspend fun sendChatMessage(request: ChatRequest): ApiResponse<ChatResponse> {
        return apiService.sendChatMessage(request)
    }

    suspend fun getChatConversations(
        page: Int,
        size: Int
    ): ApiResponse<PageResponse<ChatConversationResponse>> {
        return apiService.getChatConversations(page, size)
    }

    suspend fun getChatConversationMessages(
        conversationId: String,
        page: Int,
        size: Int,
        sort: String
    ): ApiResponse<PageResponse<ChatMessageResponse>> {
        return apiService.getChatConversationMessages(
            conversationId = conversationId,
            page = page,
            size = size,
            sort = sort
        )
    }

}
