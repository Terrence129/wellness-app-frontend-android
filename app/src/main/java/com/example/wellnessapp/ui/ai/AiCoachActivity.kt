package com.example.wellnessapp.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem

/**
 * Entry page for AI advice and chatbot.
 */
class AiCoachActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_coach)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.cardAdvice).setOnClickListener {
            startActivity(Intent(this, AiAdviceActivity::class.java))
        }
        findViewById<View>(R.id.cardChatbot).setOnClickListener {
            startActivity(Intent(this, ChatbotActivity::class.java))
        }

        BottomNavigationController.attach(this, ActiveItem.AI)
    }
}
