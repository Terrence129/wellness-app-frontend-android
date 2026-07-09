package com.example.wellnessapp.data.network

import com.example.wellnessapp.data.model.AiAdviceRequest
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.ChatConversationResponse
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.model.ChatResponse
import com.example.wellnessapp.data.model.LoginRequest
import com.example.wellnessapp.data.model.LoginResponse
import com.example.wellnessapp.data.model.PageResponse
import com.example.wellnessapp.data.model.PersonalInfoResponse
import com.example.wellnessapp.data.model.PersonalInfoUpsertRequest
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
    ): ApiResponse<PageResponse<WellnessLogResponse>>

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

    @GET("ai/advice")
    suspend fun getAiAdviceHistory(
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String
    ): ApiResponse<PageResponse<AiAdviceResponse>>

    @GET("ai/advice/{id}")
    suspend fun getAiAdviceDetail(
        @Path("id") id: Long
    ): ApiResponse<AiAdviceResponse>

    @POST("ai/chat")
    suspend fun sendChatMessage(
        @Body request: ChatRequest
    ): ApiResponse<ChatResponse>

    @GET("ai/chat/conversations")
    suspend fun getChatConversations(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<PageResponse<ChatConversationResponse>>

    @GET("ai/chat/conversations/{conversationId}/messages")
    suspend fun getChatConversationMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: String
    ): ApiResponse<PageResponse<ChatMessageResponse>>

    // --- Personal Info ---

    @GET("users/me/personal-info")
    suspend fun getPersonalInfo(): ApiResponse<PersonalInfoResponse>

    @PUT("users/me/personal-info")
    suspend fun upsertPersonalInfo(
        @Body request: PersonalInfoUpsertRequest
    ): ApiResponse<PersonalInfoResponse>

}
