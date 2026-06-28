package com.example.wellnessapp.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

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

    companion object {
        const val PREFS_NAME = "auth_prefs"
        const val KEY_TOKEN = "jwt_token"
    }
}
