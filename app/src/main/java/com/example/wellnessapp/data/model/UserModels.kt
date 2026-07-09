package com.example.wellnessapp.data.model

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val age: Int? = null
)
