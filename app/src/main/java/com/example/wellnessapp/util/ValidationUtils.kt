// Team5
// @author: Wu Aomo

package com.example.wellnessapp.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun isValidUsername(username: String): Boolean {
        return username.trim().length >= MIN_USERNAME_LENGTH
    }

    fun validateLogin(email: String, password: String): String? {
        return when {
            email.isBlank() -> "Email is required."
            !isValidEmail(email) -> "Enter a valid email address."
            password.isBlank() -> "Password is required."
            !isValidPassword(password) -> "Password must be at least $MIN_PASSWORD_LENGTH characters."
            else -> null
        }
    }

    fun validateRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            username.isBlank() -> "Username is required."
            !isValidUsername(username) -> "Username must be at least $MIN_USERNAME_LENGTH characters."
            email.isBlank() -> "Email is required."
            !isValidEmail(email) -> "Enter a valid email address."
            password.isBlank() -> "Password is required."
            !isValidPassword(password) -> "Password must be at least $MIN_PASSWORD_LENGTH characters."
            confirmPassword.isBlank() -> "Confirm your password."
            !doPasswordsMatch(password, confirmPassword) -> "Passwords do not match."
            else -> null
        }
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

    fun validatePersonalInfo(
        heightCm: String,
        weightKg: String,
        gender: String,
        dateOfBirth: String,
        activityLevel: String,
        today: String
    ): String? {
        val heightValue = heightCm.trim().toDoubleOrNull()
        if (heightValue == null || heightValue < 50.0 || heightValue > 250.0) {
            return "Height must be between 50 and 250 cm."
        }

        val weightValue = weightKg.trim().toDoubleOrNull()
        if (weightValue == null || weightValue < 2.0 || weightValue > 500.0) {
            return "Weight must be between 2 and 500 kg."
        }

        if (gender.isBlank()) {
            return "Please select your gender."
        }

        val dateOfBirthValue = parseIsoDate(dateOfBirth) ?: return "Please choose a valid date of birth."
        val todayValue = parseIsoDate(today) ?: LocalDate.now()
        if (!dateOfBirthValue.isBefore(todayValue)) {
            return "Date of birth must be in the past."
        }

        if (activityLevel.isBlank()) {
            return "Please select your activity level."
        }

        return null
    }

    private fun parseIsoDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value.trim())
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MIN_USERNAME_LENGTH = 3
    private const val MIN_MOOD_SCORE = 1
    private const val MAX_MOOD_SCORE = 10
}
