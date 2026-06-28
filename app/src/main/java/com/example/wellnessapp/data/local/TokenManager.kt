package com.example.wellnessapp.data.local

import android.content.Context

class TokenManager(context: Context) {

    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
    }
}
