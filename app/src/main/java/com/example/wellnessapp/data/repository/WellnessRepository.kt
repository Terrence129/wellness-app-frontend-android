package com.example.wellnessapp.data.repository

import android.content.Context
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.model.WellnessLogRequest
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.model.WellnessLogUpdateRequest
import com.example.wellnessapp.data.network.RetrofitClient

class WellnessRepository(context: Context) {

    private val apiService = RetrofitClient.getApiService(context)

    suspend fun createWellnessLog(
        request: WellnessLogRequest
    ): ApiResponse<WellnessLogResponse> {
        return apiService.createWellnessLog(request)
    }

    suspend fun getWellnessLogs(
        startDate: String? = null,
        endDate: String? = null
    ): ApiResponse<List<WellnessLogResponse>> {
        return apiService.getWellnessLogs(startDate, endDate)
    }

    suspend fun getWellnessLogByDate(logDate: String): ApiResponse<WellnessLogResponse> {
        return apiService.getWellnessLogByDate(logDate)
    }

    suspend fun updateWellnessLog(
        id: Long,
        request: WellnessLogUpdateRequest
    ): ApiResponse<WellnessLogResponse> {
        return apiService.updateWellnessLog(id, request)
    }

    suspend fun deleteWellnessLog(id: Long): ApiResponse<Unit?> {
        return apiService.deleteWellnessLog(id)
    }
}
