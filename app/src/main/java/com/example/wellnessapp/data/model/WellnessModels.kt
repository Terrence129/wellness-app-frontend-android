// Team5
// @author: Chen Chen

package com.example.wellnessapp.data.model

data class WellnessLogRequest(
    val logDate: String,
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?
)

data class WellnessLogUpdateRequest(
    val logDate: String,
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?
)

data class WellnessLogResponse(
    val id: Long,
    val logDate: String,
    val sleepHours: Double?,
    val moodScore: Int?,
    val waterCups: Int?,
    val steps: Int?,
    val exerciseMinutes: Int?,
    val note: String?,
    val createdAt: String? = null
)

fun WellnessLogRequest.toUpdateRequest() = WellnessLogUpdateRequest(
    logDate = logDate,
    sleepHours = sleepHours,
    moodScore = moodScore,
    waterCups = waterCups,
    steps = steps,
    exerciseMinutes = exerciseMinutes,
    note = note
)
