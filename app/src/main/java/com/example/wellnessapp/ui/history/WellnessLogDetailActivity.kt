// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import com.example.wellnessapp.ui.log.EditWellnessLogActivity
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Shows one wellness log selected from History.
 *
 * Author: Member F
 */
class WellnessLogDetailActivity : AppCompatActivity() {

    private val wellnessRepository by lazy {
        WellnessRepository(applicationContext)
    }

    private var currentLogId: Long = -1L
    private var currentLogDate: String = ""
    private var currentLog: WellnessLogResponse? = null

    private lateinit var dateText: TextView
    private lateinit var sleepText: TextView
    private lateinit var moodText: TextView
    private lateinit var waterText: TextView
    private lateinit var stepsText: TextView
    private lateinit var exerciseText: TextView
    private lateinit var noteText: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button

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
        editButton = findViewById(R.id.btnEditLog)
        deleteButton = findViewById(R.id.btnDeleteLog)
    }

    private fun readIntent() {
        currentLogId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        currentLogDate = intent.getStringExtra(EXTRA_LOG_DATE).orEmpty()
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnDetailBack).setOnClickListener {
            openHistory()
        }

        editButton.setOnClickListener {
            val log = currentLog ?: return@setOnClickListener
            val intent = Intent(this, EditWellnessLogActivity::class.java).apply {
                putExtra(EditWellnessLogActivity.EXTRA_LOG_ID, log.id)
                putExtra(EditWellnessLogActivity.EXTRA_LOG_DATE, log.logDate)
                log.sleepHours?.let { putExtra(EditWellnessLogActivity.EXTRA_SLEEP_HOURS, it) }
                log.moodScore?.let { putExtra(EditWellnessLogActivity.EXTRA_MOOD_SCORE, it) }
                log.waterCups?.let { putExtra(EditWellnessLogActivity.EXTRA_WATER_CUPS, it) }
                log.steps?.let { putExtra(EditWellnessLogActivity.EXTRA_STEPS, it) }
                log.exerciseMinutes?.let { putExtra(EditWellnessLogActivity.EXTRA_EXERCISE_MINUTES, it) }
                putExtra(EditWellnessLogActivity.EXTRA_NOTE, log.note.orEmpty())
            }
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            confirmDelete()
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
        editButton.isEnabled = false
        deleteButton.isEnabled = false
    }

    private fun showDetail(log: WellnessLogResponse) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        editButton.isEnabled = true
        deleteButton.isEnabled = true

        currentLogId = log.id
        currentLogDate = log.logDate
        currentLog = log
        dateText.text = log.logDate
        sleepText.text = getString(R.string.member_f_sleep_hours_format, formatDecimal(log.sleepHours))
        moodText.text = getString(R.string.member_f_mood_score_format, log.moodScore?.toString() ?: "--")
        waterText.text = getString(R.string.member_f_water_cups_format, log.waterCups?.toString() ?: "--")
        stepsText.text = getString(R.string.member_f_steps_format, log.steps?.toString() ?: "--")
        exerciseText.text = getString(R.string.member_f_exercise_minutes_format, log.exerciseMinutes?.toString() ?: "--")
        noteText.text = log.note?.takeIf { it.isNotBlank() } ?: getString(R.string.member_f_home_no_note)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
        currentLog = null
        editButton.isEnabled = false
        deleteButton.isEnabled = false
    }

    private fun confirmDelete() {
        if (currentLogId <= 0L) {
            showError("Missing wellness log id.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.member_f_delete_log_title)
            .setMessage(R.string.member_f_delete_log_message)
            .setPositiveButton(R.string.member_f_delete) { _, _ -> deleteLog() }
            .setNegativeButton(R.string.member_f_cancel, null)
            .show()
    }

    private fun deleteLog() {
        showLoading()

        lifecycleScope.launch {
            try {
                val response = wellnessRepository.deleteWellnessLog(currentLogId)
                if (response.success) {
                    openHistory()
                } else {
                    showError(response.message.ifBlank { "Unable to delete wellness log." })
                }
            } catch (error: Exception) {
                showError(error.message ?: "Unable to delete wellness log.")
            }
        }
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
