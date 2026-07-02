package com.example.wellnessapp.util

import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ErrorMessageMapperTest {
    @Test
    fun fromThrowableMapsNetworkError() {
        val result = ErrorMessageMapper.fromThrowable(
            error = IOException("connection refused"),
            fallback = "Login failed"
        )

        assertEquals(
            "Unable to connect to the server. Please check that the backend is running and try again.",
            result
        )
    }

    @Test
    fun fromThrowableUsesBackendErrorMessage() {
        val result = ErrorMessageMapper.fromThrowable(
            error = httpException(409, """{"success":false,"message":"Email already exists","errorCode":"EMAIL_ALREADY_EXISTS"}"""),
            fallback = "Registration failed"
        )

        assertEquals("Email already exists", result)
    }

    @Test
    fun fromThrowableMapsUnauthorizedWhenBodyIsEmpty() {
        val result = ErrorMessageMapper.fromThrowable(
            error = httpException(401, ""),
            fallback = "Login failed"
        )

        assertEquals("Invalid email or password.", result)
    }

    private fun httpException(code: Int, body: String): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
