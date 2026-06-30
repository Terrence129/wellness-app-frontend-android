package com.example.wellnessapp.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresAt: String? = null,
    val user: UserResponse
)
