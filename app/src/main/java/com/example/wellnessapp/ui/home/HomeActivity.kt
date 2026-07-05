// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.local.TokenManager
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.UserRepository
import com.example.wellnessapp.data.repository.WellnessRepository
import com.example.wellnessapp.ui.auth.LoginActivity
import com.example.wellnessapp.ui.history.HistoryActivity
import com.example.wellnessapp.ui.log.AddWellnessLogActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.wellnessapp.ui.ai.AiAdviceActivity
import com.example.wellnessapp.ui.ai.ChatbotActivity
import com.example.wellnessapp.ui.summary.WeeklySummaryActivity

/**
 * Home screen for the SimpleWell Android app.
 *
 * Author: Member F
 */
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            UserRepository(applicationContext),
            WellnessRepository(applicationContext)
        )
    }

    private lateinit var welcomeText: TextView
    private lateinit var todayDateText: TextView
    private lateinit var statusText: TextView
    private lateinit var sleepText: TextView
    private lateinit var moodText: TextView
    private lateinit var waterText: TextView
    private lateinit var stepsText: TextView
    private lateinit var exerciseText: TextView
    private lateinit var noteText: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupActions()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHome(currentDate())
    }

    private fun bindViews() {
        welcomeText = findViewById(R.id.tvWelcome)
        todayDateText = findViewById(R.id.tvTodayDate)
        statusText = findViewById(R.id.tvTodayStatus)
        sleepText = findViewById(R.id.tvSleepHours)
        moodText = findViewById(R.id.tvMoodScore)
        waterText = findViewById(R.id.tvWaterCups)
        stepsText = findViewById(R.id.tvSteps)
        exerciseText = findViewById(R.id.tvExerciseMinutes)
        noteText = findViewById(R.id.tvNote)
        errorText = findViewById(R.id.tvHomeError)
        progressBar = findViewById(R.id.progressHome)
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnAddLog).setOnClickListener {
            startActivity(Intent(this, AddWellnessLogActivity::class.java))
        }
        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        findViewById<Button>(R.id.btnWeeklySummary).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    WeeklySummaryActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnAiAdvice).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AiAdviceActivity::class.java
                )
            )
        }

        findViewById<Button>(R.id.btnChatbot).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ChatbotActivity::class.java
                )
            )
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }


    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                HomeUiState.Loading -> showLoading()
                is HomeUiState.Success -> showHome(state)
                is HomeUiState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
    }

    private fun showHome(state: HomeUiState.Success) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        welcomeText.text = getString(R.string.member_f_home_welcome_format, state.user.username)
        todayDateText.text = state.today
        renderTodayLog(state.todayLog)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun renderTodayLog(log: WellnessLogResponse?) {
        if (log == null) {
            statusText.text = getString(R.string.member_f_home_no_log_today)
            sleepText.text = getString(R.string.member_f_metric_empty)
            moodText.text = getString(R.string.member_f_metric_empty)
            waterText.text = getString(R.string.member_f_metric_empty)
            stepsText.text = getString(R.string.member_f_metric_empty)
            exerciseText.text = getString(R.string.member_f_metric_empty)
            noteText.text = getString(R.string.member_f_home_no_note)
            return
        }

        statusText.text = getString(R.string.member_f_home_log_ready)
        sleepText.text = getString(R.string.member_f_sleep_hours_format, formatDecimal(log.sleepHours))
        moodText.text = getString(R.string.member_f_mood_score_format, log.moodScore?.toString() ?: "--")
        waterText.text = getString(R.string.member_f_water_cups_format, log.waterCups?.toString() ?: "--")
        stepsText.text = getString(R.string.member_f_steps_format, log.steps?.toString() ?: "--")
        exerciseText.text = getString(R.string.member_f_exercise_minutes_format, log.exerciseMinutes?.toString() ?: "--")
        noteText.text = log.note?.takeIf { it.isNotBlank() } ?: getString(R.string.member_f_home_no_note)
    }

    private fun logout() {
        lifecycleScope.launch {
            TokenManager(this@HomeActivity).clearToken()
            val intent = Intent(this@HomeActivity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun formatDecimal(value: Double?): String {
        if (value == null) return "--"
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }
}
