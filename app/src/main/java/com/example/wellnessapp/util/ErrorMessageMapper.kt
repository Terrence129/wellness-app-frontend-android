package com.example.wellnessapp.util

import com.example.wellnessapp.data.model.ErrorResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import retrofit2.HttpException

object ErrorMessageMapper {
    private val gson = Gson()

    fun fromThrowable(error: Throwable, fallback: String): String {
        return when (error) {
            is IOException -> "Unable to connect to the server. Please check that the backend is running and try again."
            is HttpException -> fromHttpException(error, fallback)
            else -> error.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    fun fromBackendMessage(message: String?, fallback: String): String {
        return message?.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun fromHttpException(error: HttpException, fallback: String): String {
        val backendMessage = parseErrorBody(error)
        if (!backendMessage.isNullOrBlank()) {
            return backendMessage
        }

        return when (error.code()) {
            400 -> "Please check your input and try again."
            401 -> "Invalid email or password."
            403 -> "You do not have permission to perform this action."
            404 -> "Authentication service was not found. Please confirm the backend API path."
            409 -> "This account already exists."
            in 500..599 -> "Server error. Please try again after the backend is fixed."
            else -> fallback
        }
    }

    private fun parseErrorBody(error: HttpException): String? {
        return try {
            val rawBody = error.response()?.errorBody()?.string()
            if (rawBody.isNullOrBlank()) {
                null
            } else {
                gson.fromJson(rawBody, ErrorResponse::class.java).message
            }
        } catch (_: IOException) {
            null
        } catch (_: JsonSyntaxException) {
            null
        }
    }
}
