package com.example.wellnessapp.ui.navigation

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomNavigationControllerTest {
    @Test
    fun topLevelScreensShowBottomNavigation() {
        val screens = mapOf(
            "ui/navigation/TrendsHistoryActivity.kt" to "BottomNavigationController.ActiveItem.TRENDS_HISTORY",
            "ui/summary/WeeklySummaryActivity.kt" to "ActiveItem.TRENDS_HISTORY",
            "ui/history/HistoryActivity.kt" to "ActiveItem.TRENDS_HISTORY",
            "ui/ai/AiCoachActivity.kt" to "ActiveItem.AI"
        )

        screens.forEach { (path, activeItem) ->
            val source = sourceFile(path).readText()

            assertTrue(
                "$path should attach bottom navigation with $activeItem",
                source.contains("BottomNavigationController.attach(this, $activeItem")
            )
        }

        assertTrue(
            "Home layout should keep its bottom navigation",
            File("src/main/res/layout/activity_home.xml").readText()
                .contains("android:id=\"@+id/bottomNavigation\"")
        )
    }

    @Test
    fun aiCoachShowsAdviceAndChatbotCards() {
        val layout = File("src/main/res/layout/activity_ai_coach.xml").readText()

        assertTrue(layout.contains("android:id=\"@+id/cardAdvice\""))
        assertTrue(layout.contains("android:id=\"@+id/cardChatbot\""))
    }

    private fun sourceFile(path: String): File {
        return File("src/main/java/com/example/wellnessapp", path)
    }
}
