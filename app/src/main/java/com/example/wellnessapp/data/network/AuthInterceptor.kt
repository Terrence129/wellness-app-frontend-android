package com.example.wellnessapp.data.network

import android.content.Context
import com.example.wellnessapp.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val tokenManager = TokenManager(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        if (token.isNullOrBlank() || isAuthEndpoint(originalRequest.url.encodedPath)) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun isAuthEndpoint(path: String): Boolean {
        return path.endsWith("/auth/login") || path.endsWith("/auth/register")
    }

}
