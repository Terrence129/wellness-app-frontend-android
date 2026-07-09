package com.example.wellnessapp.data.model

/**
 * User profile returned by GET /api/users/me.
 */
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val age: Int? = null
)

/**
 * Personal health info returned by GET /api/users/me/personal-info.
 * Contains BMI (calculated by backend, not persisted).
 */
data class PersonalInfoResponse(
    val id: Long? = null,
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val gender: String = "",
    val dateOfBirth: String? = null,
    val activityLevel: String = "",
    val bmi: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Request body for PUT /api/users/me/personal-info.
 */
data class PersonalInfoUpsertRequest(
    val heightCm: Double,
    val weightKg: Double,
    val gender: String,
    val dateOfBirth: String,
    val activityLevel: String
)
