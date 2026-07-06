package com.example.wellnessapp.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PageResponse<T>(
    val content: List<T>,
    val page: PageMetadata
)

data class PageMetadata(
    val number: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val sort: List<String>
)
