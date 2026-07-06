package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.WeeklySummaryResponse
import com.example.wellnessapp.data.network.RetrofitClient

class SummaryRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)

    suspend fun getWeeklySummary(
        startDate: String? = null,
        endDate: String? = null
    ): ApiResponse<WeeklySummaryResponse> {
        return apiService.getWeeklySummary(
            startDate?.takeIf { it.isNotBlank() },
            endDate?.takeIf { it.isNotBlank() }
        )
    }
}
