package com.example.wellnessapp.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WellnessLogRequestTest {
    @Test
    fun toUpdateRequestKeepsAllFields() {
        val request = WellnessLogRequest(
            logDate = "2026-07-08",
            sleepHours = 8.0,
            moodScore = 4,
            waterCups = 6,
            steps = 9000,
            exerciseMinutes = 45,
            note = "Good day"
        )

        val updateRequest = request.toUpdateRequest()

        assertEquals("2026-07-08", updateRequest.logDate)
        assertEquals(8.0, updateRequest.sleepHours)
        assertEquals(4, updateRequest.moodScore)
        assertEquals(6, updateRequest.waterCups)
        assertEquals(9000, updateRequest.steps)
        assertEquals(45, updateRequest.exerciseMinutes)
        assertEquals("Good day", updateRequest.note)
    }
}
