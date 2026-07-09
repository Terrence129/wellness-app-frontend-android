// Team5
// @author: Luo Yuxiang

package com.example.wellnessapp.ui.history

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Shows one wellness log selected from History.
 *
 */
class WellnessLogDetailActivity : AppCompatActivity() {

    private val wellnessRepository by lazy {
        WellnessRepository(applicationContext)
    }

    private var currentLogDate: String = ""

    private lateinit var dateText: TextView
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
        setContentView(R.layout.activity_wellness_log_detail)

        bindViews()
        readIntent()
        setupActions()

        if (currentLogDate.isBlank()) {
            showError("Missing wellness log date.")
        } else {
            loadDetail()
        }
    }

    private fun bindViews() {
        dateText = findViewById(R.id.tvDetailDate)
        sleepText = findViewById(R.id.tvDetailSleep)
        moodText = findViewById(R.id.tvDetailMood)
        waterText = findViewById(R.id.tvDetailWater)
        stepsText = findViewById(R.id.tvDetailSteps)
        exerciseText = findViewById(R.id.tvDetailExercise)
        noteText = findViewById(R.id.tvDetailNote)
        errorText = findViewById(R.id.tvDetailError)
        progressBar = findViewById(R.id.progressDetail)
    }

    private fun readIntent() {
        currentLogDate = intent.getStringExtra(EXTRA_LOG_DATE).orEmpty()
    }

    private fun setupActions() {
        findViewById<View>(R.id.btnDetailBack).setOnClickListener {
            openHistory()
        }
    }

    private fun loadDetail() {
        showLoading()

        lifecycleScope.launch {
            try {
                val response = wellnessRepository.getWellnessLogByDate(currentLogDate)
                if (!response.success || response.data == null) {
                    showError(response.message.ifBlank { "Unable to load wellness log detail." })
                    return@launch
                }

                showDetail(response.data)
            } catch (error: Exception) {
                showError(error.message ?: "Unable to load wellness log detail.")
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
    }

    private fun showDetail(log: WellnessLogResponse) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE

        currentLogDate = log.logDate
        dateText.text = log.logDate
        sleepText.text = getString(R.string.sleep_hours_format, formatDecimal(log.sleepHours))
        moodText.text = getString(R.string.mood_score_format, log.moodScore?.toString() ?: "--")
        waterText.text = getString(R.string.water_cups_format, log.waterCups?.toString() ?: "--")
        stepsText.text = getString(R.string.steps_format, log.steps?.toString() ?: "--")
        exerciseText.text = getString(R.string.exercise_minutes_format, log.exerciseMinutes?.toString() ?: "--")
        noteText.text = log.note?.takeIf { it.isNotBlank() } ?: getString(R.string.home_no_note)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun openHistory() {
        finish()
    }

    private fun formatDecimal(value: Double?): String {
        if (value == null) return "--"
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", value)
        }
    }

    companion object {
        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_LOG_DATE = "extra_log_date"
    }
}
