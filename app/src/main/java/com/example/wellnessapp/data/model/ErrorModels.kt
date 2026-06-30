package com.example.wellnessapp.data.model

data class ErrorResponse(
    val success: Boolean = false,
    val message: String? = null,
    val errorCode: String? = null,
    val errors: List<FieldErrorResponse> = emptyList()
)

data class FieldErrorResponse(
    val field: String,
    val message: String
)
