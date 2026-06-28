package com.example.wellnessapp

import android.content.Intent
import android.os.Bundle
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

        startActivity(Intent(this, targetActivity))
        finish()
    }
}
