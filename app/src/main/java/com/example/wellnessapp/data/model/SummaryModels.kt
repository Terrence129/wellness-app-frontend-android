package com.example.wellnessapp.data.model

/**
 * Weekly wellness summary returned by the backend.
 *
 * @author Yunke Deng
 */
data class WeeklySummaryResponse(
    val startDate: String? = null,
    val endDate: String? = null,
    val daysWithLogs: Int = 0,
    val averageSleepHours: Double = 0.0,
    val averageMoodScore: Double = 0.0,
    val averageWaterCups: Double = 0.0,
    val totalSteps: Long = 0L,
    val totalExerciseMinutes: Long = 0L,
    val summary: String = ""
)