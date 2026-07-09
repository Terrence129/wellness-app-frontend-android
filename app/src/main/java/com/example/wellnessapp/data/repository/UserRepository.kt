// Team5
// @author: Wu Aomo

package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.PersonalInfoResponse
import com.example.wellnessapp.data.model.PersonalInfoUpsertRequest
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.network.RetrofitClient

class UserRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getCurrentUser(): ApiResponse<UserResponse> {
        return apiService.getCurrentUser()
    }

    suspend fun getPersonalInfo(): ApiResponse<PersonalInfoResponse> {
        return apiService.getPersonalInfo()
    }

    suspend fun upsertPersonalInfo(
        request: PersonalInfoUpsertRequest
    ): ApiResponse<PersonalInfoResponse> {
        return apiService.upsertPersonalInfo(request)
    }
}
