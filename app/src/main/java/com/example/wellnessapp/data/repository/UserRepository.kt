package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.network.RetrofitClient

class UserRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getCurrentUser(): ApiResponse<UserResponse> {
        return apiService.getCurrentUser()
    }
}
