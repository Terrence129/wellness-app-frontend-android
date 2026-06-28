package com.example.wellnessapp.data.model

data class WeeklySummaryResponse(
    val averageSleepHours: Double,
    val averageMoodScore: Double,
    val averageWaterCups: Double,
    val totalSteps: Int,
    val totalExerciseMinutes: Int,
    val summary: String
)
