package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.local.TokenManager
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.LoginRequest
import com.example.wellnessapp.data.model.LoginResponse
import com.example.wellnessapp.data.model.RegisterRequest
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.network.RetrofitClient
import com.example.wellnessapp.util.ErrorMessageMapper

class AuthRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)
    private val tokenManager = TokenManager(context)

    suspend fun register(request: RegisterRequest): ApiResponse<UserResponse> {
        return apiService.register(request)
    }

    suspend fun registerResult(request: RegisterRequest): Result<UserResponse> {
        return try {
            val response = register(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    IllegalStateException(
                        ErrorMessageMapper.fromBackendMessage(
                            response.message,
                            "Registration failed"
                        )
                    )
                )
            }
        } catch (error: Throwable) {
            Result.failure(
                IllegalStateException(
                    ErrorMessageMapper.fromThrowable(error, "Registration failed"),
                    error
                )
            )
        }
    }

    suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> {
        val response = apiService.login(request)
        if (response.success) {
            response.data?.token?.let { tokenManager.saveToken(it) }
        }
        return response
    }

    suspend fun loginResult(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = login(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(
                    IllegalStateException(
                        ErrorMessageMapper.fromBackendMessage(response.message, "Login failed")
                    )
                )
            }
        } catch (error: Throwable) {
            Result.failure(
                IllegalStateException(
                    ErrorMessageMapper.fromThrowable(error, "Login failed"),
                    error
                )
            )
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}
