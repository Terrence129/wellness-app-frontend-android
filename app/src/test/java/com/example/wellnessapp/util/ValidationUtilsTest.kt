package com.example.wellnessapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {
    @Test
    fun validateLoginReturnsErrorForInvalidEmail() {
        val result = ValidationUtils.validateLogin("bad-email", "Password123")

        assertEquals("Enter a valid email address.", result)
    }

    @Test
    fun validateLoginReturnsErrorForShortPassword() {
        val result = ValidationUtils.validateLogin("dadao@example.com", "1234567")

        assertEquals("Password must be at least 8 characters.", result)
    }

    @Test
    fun validateLoginAcceptsValidInput() {
        val result = ValidationUtils.validateLogin("dadao@example.com", "Password123")

        assertNull(result)
    }

    @Test
    fun validateRegisterReturnsErrorForShortUsername() {
        val result = ValidationUtils.validateRegister(
            username = "Da",
            email = "dadao@example.com",
            password = "Password123",
            confirmPassword = "Password123"
        )

        assertEquals("Username must be at least 3 characters.", result)
    }

    @Test
    fun validateRegisterReturnsErrorForMismatchedPasswords() {
        val result = ValidationUtils.validateRegister(
            username = "Dadao",
            email = "dadao@example.com",
            password = "Password123",
            confirmPassword = "Password456"
        )

        assertEquals("Passwords do not match.", result)
    }

    @Test
    fun validateRegisterAcceptsValidInput() {
        val result = ValidationUtils.validateRegister(
            username = "Dadao",
            email = "dadao@example.com",
            password = "Password123",
            confirmPassword = "Password123"
        )

        assertNull(result)
    }

    @Test
    fun isValidPasswordFollowsBackendMinimumLength() {
        assertFalse(ValidationUtils.isValidPassword("1234567"))
        assertTrue(ValidationUtils.isValidPassword("12345678"))
    }
}
