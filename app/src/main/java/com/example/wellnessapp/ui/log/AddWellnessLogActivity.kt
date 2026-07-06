// Team5
// Author: Chen Chen
package com.example.wellnessapp.ui.log

import android.app.DatePickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class `AddWellnessLogActivity` : AppCompatActivity() {

    private val viewModel: AddWellnessLogViewModel by viewModels()

    private lateinit var inputLogDate: EditText
    private lateinit var buttonPickDate: Button
    private lateinit var inputSleepHours: EditText
    private lateinit var spinnerMoodScore: Spinner
    private lateinit var inputWaterCups: EditText
    private lateinit var inputSteps: EditText
    private lateinit var inputExerciseMinutes: EditText
    private lateinit var inputNote: EditText
    private lateinit var buttonSubmitLog: Button
    private lateinit var buttonCancelLog: Button
    private lateinit var textError: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_wellness_log)

        bindViews()
        setupMoodSpinner()
        setupInitialDate()
        setupListeners()
        observeState()
    }

    private fun bindViews() {
        inputLogDate = findViewById(R.id.inputLogDate)
        buttonPickDate = findViewById(R.id.buttonPickDate)
        inputSleepHours = findViewById(R.id.inputSleepHours)
        spinnerMoodScore = findViewById(R.id.spinnerMoodScore)
        inputWaterCups = findViewById(R.id.inputWaterCups)
        inputSteps = findViewById(R.id.inputSteps)
        inputExerciseMinutes = findViewById(R.id.inputExerciseMinutes)
        inputNote = findViewById(R.id.inputNote)
        buttonSubmitLog = findViewById(R.id.buttonSubmitLog)
        buttonCancelLog = findViewById(R.id.buttonCancelLog)
        textError = findViewById(R.id.textError)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupMoodSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("1", "2", "3", "4", "5"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMoodScore.adapter = adapter
        spinnerMoodScore.setSelection(2)
    }

    private fun setupInitialDate() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        inputLogDate.setText(today)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        buttonPickDate.setOnClickListener { showDatePicker() }
        inputLogDate.setOnClickListener { showDatePicker() }

        buttonSubmitLog.setOnClickListener {
            viewModel.createLog(
                logDate = inputLogDate.text.toString(),
                sleepHoursText = inputSleepHours.text.toString(),
                moodScoreText = spinnerMoodScore.selectedItem.toString(),
                waterCupsText = inputWaterCups.text.toString(),
                stepsText = inputSteps.text.toString(),
                exerciseMinutesText = inputExerciseMinutes.text.toString(),
                note = inputNote.text.toString()
            )
        }

        buttonCancelLog.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is AddLogUiState.Idle -> {
                    setLoading(false)
                    textError.visibility = View.GONE
                }

                is AddLogUiState.Loading -> {
                    setLoading(true)
                    textError.visibility = View.GONE
                }

                is AddLogUiState.Success -> {
                    setLoading(false)
                    Toast.makeText(this, "Wellness log created", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                is AddLogUiState.Error -> {
                    setLoading(false)
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                inputLogDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        buttonSubmitLog.isEnabled = !isLoading
        buttonCancelLog.isEnabled = !isLoading
        buttonPickDate.isEnabled = !isLoading
    }
}
