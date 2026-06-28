package com.example.wellnessapp.util

import android.util.Patterns

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun isValidUsername(username: String): Boolean {
        return username.trim().length >= MIN_USERNAME_LENGTH
    }

    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isValidMoodScore(moodScore: Int): Boolean {
        return moodScore in MIN_MOOD_SCORE..MAX_MOOD_SCORE
    }

    fun isNonNegative(value: Int): Boolean {
        return value >= 0
    }

    fun isNonNegative(value: Double): Boolean {
        return value >= 0.0
    }

    private const val MIN_PASSWORD_LENGTH = 6
    private const val MIN_USERNAME_LENGTH = 2
    private const val MIN_MOOD_SCORE = 1
    private const val MAX_MOOD_SCORE = 10
}
