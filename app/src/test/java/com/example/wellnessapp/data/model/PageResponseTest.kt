package com.example.wellnessapp.data.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Test

class PageResponseTest {
    @Test
    fun parsesWellnessLogPageEnvelope() {
        val json = """
            {
              "success": true,
              "message": "Success",
              "data": {
                "content": [
                  {
                    "id": 101,
                    "logDate": "2026-06-20",
                    "sleepHours": 7.5,
                    "moodScore": 4,
                    "waterCups": 6,
                    "steps": 8200,
                    "exerciseMinutes": 30,
                    "note": "Felt good overall."
                  }
                ],
                "page": {
                  "number": 0,
                  "size": 20,
                  "totalElements": 1,
                  "totalPages": 1,
                  "sort": ["logDate,desc"]
                }
              }
            }
        """.trimIndent()

        val type = object : TypeToken<ApiResponse<PageResponse<WellnessLogResponse>>>() {}.type
        val response: ApiResponse<PageResponse<WellnessLogResponse>> = Gson().fromJson(json, type)

        assertEquals("2026-06-20", response.data?.content?.single()?.logDate)
    }
}
