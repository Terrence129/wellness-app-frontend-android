// Team5
// @author: Wu Aomo

package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.data.local.TokenManager
import com.example.wellnessapp.ui.auth.LoginActivity
import com.example.wellnessapp.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val targetActivity = if (TokenManager(this).hasToken()) {
            HomeActivity::class.java
        } else {
            LoginActivity::class.java
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, targetActivity))
            finish()
        }, SPLASH_DELAY_MS)
    }

    companion object {
        private const val SPLASH_DELAY_MS = 2000L
    }
}
