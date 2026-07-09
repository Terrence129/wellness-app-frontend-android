// Team5
// @author: Wu Aomo

package com.example.wellnessapp.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private const val API_DATE_PATTERN = "yyyy-MM-dd"
    private const val DISPLAY_DATE_PATTERN = "MMM dd, yyyy"

    fun getTodayDate(): String {
        return formatApiDate(Date())
    }

    fun formatApiDate(date: Date): String {
        return SimpleDateFormat(API_DATE_PATTERN, Locale.US).format(date)
    }

    fun formatDisplayDate(apiDate: String): String {
        return try {
            val date = SimpleDateFormat(API_DATE_PATTERN, Locale.US).parse(apiDate)
            if (date == null) apiDate else SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.US).format(date)
        } catch (_: Exception) {
            apiDate
        }
    }

    fun getDateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return formatApiDate(calendar.time)
    }
}
