// Team5
// Author: Chen Chen
package com.example.wellnessapp.ui.log

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.ui.home.HomeActivity

class `EditWellnessLogActivity` : AppCompatActivity() {

    private val viewModel: EditWellnessLogViewModel by viewModels()

    private lateinit var textLogDate: TextView
    private lateinit var inputSleepHours: EditText
    private lateinit var spinnerMoodScore: Spinner
    private lateinit var inputWaterCups: EditText
    private lateinit var inputSteps: EditText
    private lateinit var inputExerciseMinutes: EditText
    private lateinit var inputNote: EditText
    private lateinit var buttonUpdateLog: Button
    private lateinit var buttonDeleteLog: Button
    private lateinit var buttonCancelEdit: Button
    private lateinit var textError: TextView
    private lateinit var progressBar: ProgressBar

    private var logId: Long = -1L
    private var logDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_wellness_log)

        bindViews()
        setupMoodSpinner()
        readIntentData()
        setupListeners()
        observeState()
    }

    private fun bindViews() {
        textLogDate = findViewById(R.id.textLogDate)
        inputSleepHours = findViewById(R.id.inputSleepHours)
        spinnerMoodScore = findViewById(R.id.spinnerMoodScore)
        inputWaterCups = findViewById(R.id.inputWaterCups)
        inputSteps = findViewById(R.id.inputSteps)
        inputExerciseMinutes = findViewById(R.id.inputExerciseMinutes)
        inputNote = findViewById(R.id.inputNote)
        buttonUpdateLog = findViewById(R.id.buttonUpdateLog)
        buttonDeleteLog = findViewById(R.id.buttonDeleteLog)
        buttonCancelEdit = findViewById(R.id.buttonCancelEdit)
        textError = findViewById(R.id.textError)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupMoodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("1", "2", "3", "4", "5"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMoodScore.adapter = adapter
    }

    private fun readIntentData() {
        logId = intent.getLongExtra(EXTRA_LOG_ID, -1L)
        logDate = intent.getStringExtra(EXTRA_LOG_DATE).orEmpty()

        if (logId <= 0L) {
            Toast.makeText(this, "Missing wellness log id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textLogDate.text = logDate.ifBlank { "Unknown date" }
        inputSleepHours.setText(intent.getDoubleExtra(EXTRA_SLEEP_HOURS, 0.0).toString())
        inputWaterCups.setText(intent.getIntExtra(EXTRA_WATER_CUPS, 0).toString())
        inputSteps.setText(intent.getIntExtra(EXTRA_STEPS, 0).toString())
        inputExerciseMinutes.setText(intent.getIntExtra(EXTRA_EXERCISE_MINUTES, 0).toString())
        inputNote.setText(intent.getStringExtra(EXTRA_NOTE).orEmpty())

        val moodScore = intent.getIntExtra(EXTRA_MOOD_SCORE, 3)
        spinnerMoodScore.setSelection(moodScore.coerceIn(1, 5) - 1)
    }

    private fun setupListeners() {
        buttonUpdateLog.setOnClickListener {
            viewModel.updateLog(
                id = logId,
                sleepHoursText = inputSleepHours.text.toString(),
                moodScoreText = spinnerMoodScore.selectedItem.toString(),
                waterCupsText = inputWaterCups.text.toString(),
                stepsText = inputSteps.text.toString(),
                exerciseMinutesText = inputExerciseMinutes.text.toString(),
                note = inputNote.text.toString()
            )
        }

        buttonDeleteLog.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete log")
                .setMessage("Are you sure you want to delete this wellness log?")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteLog(logId) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        buttonCancelEdit.setOnClickListener {
            finish()
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is EditLogUiState.Idle -> {
                    setLoading(false)
                    textError.visibility = View.GONE
                }

                is EditLogUiState.Loading -> {
                    setLoading(true)
                    textError.visibility = View.GONE
                }

                is EditLogUiState.Updated -> {
                    setLoading(false)
                    Toast.makeText(this, "Wellness log updated", Toast.LENGTH_SHORT).show()
                    goBackAfterSuccess()
                }

                is EditLogUiState.Deleted -> {
                    setLoading(false)
                    Toast.makeText(this, "Wellness log deleted", Toast.LENGTH_SHORT).show()
                    goBackAfterSuccess()
                }

                is EditLogUiState.Error -> {
                    setLoading(false)
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun goBackAfterSuccess() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonUpdateLog.isEnabled = !isLoading
        buttonDeleteLog.isEnabled = !isLoading
        buttonCancelEdit.isEnabled = !isLoading
    }

    companion object {
        const val EXTRA_LOG_ID = "extra_log_id"
        const val EXTRA_LOG_DATE = "extra_log_date"
        const val EXTRA_SLEEP_HOURS = "extra_sleep_hours"
        const val EXTRA_MOOD_SCORE = "extra_mood_score"
        const val EXTRA_WATER_CUPS = "extra_water_cups"
        const val EXTRA_STEPS = "extra_steps"
        const val EXTRA_EXERCISE_MINUTES = "extra_exercise_minutes"
        const val EXTRA_NOTE = "extra_note"
    }
}