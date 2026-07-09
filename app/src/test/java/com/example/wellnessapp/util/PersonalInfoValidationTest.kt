package com.example.wellnessapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalInfoValidationTest {
    @Test
    fun validatePersonalInfoRequiresGenderSelection() {
        val result = ValidationUtils.validatePersonalInfo(
            heightCm = "172",
            weightKg = "68",
            gender = "",
            dateOfBirth = "1995-04-12",
            activityLevel = "MODERATELY_ACTIVE",
            today = "2026-07-09"
        )

        assertEquals("Please select your gender.", result)
    }

    @Test
    fun validatePersonalInfoRequiresDateOfBirthInThePast() {
        val result = ValidationUtils.validatePersonalInfo(
            heightCm = "172",
            weightKg = "68",
            gender = "MALE",
            dateOfBirth = "2026-07-09",
            activityLevel = "MODERATELY_ACTIVE",
            today = "2026-07-09"
        )

        assertEquals("Date of birth must be in the past.", result)
    }

    @Test
    fun validatePersonalInfoAcceptsCompleteBackendCompatiblePayload() {
        val result = ValidationUtils.validatePersonalInfo(
            heightCm = "172.5",
            weightKg = "68.4",
            gender = "MALE",
            dateOfBirth = "1995-04-12",
            activityLevel = "MODERATELY_ACTIVE",
            today = "2026-07-09"
        )

        assertNull(result)
    }
}
