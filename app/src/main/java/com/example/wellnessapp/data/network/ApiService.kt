package com.example.wellnessapp.data.network

import com.example.wellnessapp.data.model.AiAdviceRequest
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.model.ChatResponse
import com.example.wellnessapp.data.model.LoginRequest
import com.example.wellnessapp.data.model.LoginResponse
import com.example.wellnessapp.data.model.RegisterRequest
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.model.WeeklySummaryResponse
import com.example.wellnessapp.data.model.WellnessLogRequest
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.model.WellnessLogUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<UserResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): ApiResponse<UserResponse>

    @POST("wellness-logs")
    suspend fun createWellnessLog(
        @Body request: WellnessLogRequest
    ): ApiResponse<WellnessLogResponse>

    @GET("wellness-logs")
    suspend fun getWellnessLogs(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): ApiResponse<List<WellnessLogResponse>>

    @GET("wellness-logs/date/{logDate}")
    suspend fun getWellnessLogByDate(
        @Path("logDate") logDate: String
    ): ApiResponse<WellnessLogResponse>

    @PUT("wellness-logs/{id}")
    suspend fun updateWellnessLog(
        @Path("id") id: Long,
        @Body request: WellnessLogUpdateRequest
    ): ApiResponse<WellnessLogResponse>

    @DELETE("wellness-logs/{id}")
    suspend fun deleteWellnessLog(
        @Path("id") id: Long
    ): ApiResponse<Unit?>

    @GET("wellness-summary/weekly")
    suspend fun getWeeklySummary(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): ApiResponse<WeeklySummaryResponse>

    @POST("ai/advice")
    suspend fun generateAiAdvice(
        @Body request: AiAdviceRequest
    ): ApiResponse<AiAdviceResponse>

    @GET("ai/advice/latest")
    suspend fun getLatestAiAdvice(): ApiResponse<AiAdviceResponse>

    @POST("ai/chat")
    suspend fun sendChatMessage(
        @Body request: ChatRequest
    ): ApiResponse<ChatResponse>

    @GET("ai/chat/history")
    suspend fun getChatHistory(
        @Query("limit") limit: Int?
    ): ApiResponse<List<ChatMessageResponse>>
}
