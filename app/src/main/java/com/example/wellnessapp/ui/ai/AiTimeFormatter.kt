// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.ai

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object AiTimeFormatter {
    private val utcDateTimeFormats =
        listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        ).onEach {
            it.timeZone = TimeZone.getTimeZone("UTC")
        }

    private val localDateTimeFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun formatDateTime(value: String?): String? {
        val rawValue =
            value?.trim()?.takeIf { it.isNotBlank() }
                ?: return null

        utcDateTimeFormats.forEach { parser ->
            val parsedDate =
                runCatching {
                    parser.parse(rawValue)
                }.getOrNull()

            if (parsedDate != null) {
                return localDateTimeFormat.format(parsedDate)
            }
        }

        return rawValue
    }
}
