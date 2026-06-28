package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.local.TokenManager
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.LoginRequest
import com.example.wellnessapp.data.model.LoginResponse
import com.example.wellnessapp.data.model.RegisterRequest
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.network.RetrofitClient

class AuthRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    suspend fun register(request: RegisterRequest): ApiResponse<UserResponse> {
        return apiService.register(request)
    }

    suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        val response = apiService.login(request)
        if (response.success) {
            response.data?.token?.let { tokenManager.saveToken(it) }
        }
        return response
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}
