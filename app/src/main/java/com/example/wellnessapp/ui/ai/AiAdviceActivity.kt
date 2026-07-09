package com.example.wellnessapp.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.util.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Displays and generates personalised AI wellness advice.
 *
 * @author Yunke Deng
 */
class AiAdviceActivity : AppCompatActivity() {

    private val viewModel: AiAdviceViewModel by viewModels()

    private lateinit var tvRequestedRange: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var adviceContent: LinearLayout
    private lateinit var btnGenerateAdvice: Button

    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_advice)

        bindViews()
        prepareDefaultDateRange()
        observeAdviceState()
        setClickListeners()

        // Load the most recently saved advice when the page opens.
        viewModel.loadLatestAdvice()
    }

    private fun bindViews() {
        tvRequestedRange = findViewById(R.id.tvRequestedRange)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        adviceContent = findViewById(R.id.adviceContent)
        btnGenerateAdvice = findViewById(R.id.btnGenerateAdvice)
    }

    private fun setClickListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnGenerateAdvice.setOnClickListener {
            viewModel.generateAdvice(
                startDate = startDate,
                endDate = endDate
            )
        }

        findViewById<View>(R.id.btnAdviceHistory).setOnClickListener {
            startActivity(Intent(this, AiAdviceHistoryActivity::class.java))
        }
    }

    /**
     * Uses the most recent seven days as the default analysis period.
     */
    private fun prepareDefaultDateRange() {
        val formatter = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        val endCalendar = Calendar.getInstance()
        val startCalendar = endCalendar.clone() as Calendar

        // Today and the previous six days make a seven-day period.
        startCalendar.add(Calendar.DAY_OF_YEAR, -6)

        startDate = formatter.format(startCalendar.time)
        endDate = formatter.format(endCalendar.time)

        tvRequestedRange.text = "$startDate to $endDate"
    }

    private fun observeAdviceState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit

                UiState.Loading -> {
                    showLoading()
                }

                UiState.Empty -> {
                    showEmpty()
                }

                is UiState.Error -> {
                    showError(state.message)
                }

                is UiState.Success -> {
                    showAdvice(state.data)
                }
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvStatus.visibility = View.GONE
        adviceContent.visibility = View.GONE
        btnGenerateAdvice.isEnabled = false
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        adviceContent.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        btnGenerateAdvice.isEnabled = true

        tvStatus.text =
            "No saved AI advice yet. " +
                    "Generate advice using your recent records."
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        adviceContent.visibility = View.GONE
        tvStatus.visibility = View.VISIBLE
        btnGenerateAdvice.isEnabled = true

        tvStatus.text = message
    }

    private fun showAdvice(data: AiAdviceResponse) {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.GONE
        adviceContent.visibility = View.VISIBLE
        btnGenerateAdvice.isEnabled = true

        val adviceDate = data.adviceDate ?: "Unknown"

        findViewById<TextView>(R.id.tvAdviceDate).text =
            "Advice date: $adviceDate"

        val returnedStartDate = data.startDate ?: startDate
        val returnedEndDate = data.endDate ?: endDate

        findViewById<TextView>(R.id.tvAdviceRange).text =
            "Based on records from " +
                    "$returnedStartDate to $returnedEndDate"

        findViewById<TextView>(R.id.tvAdviceText).text =
            if (data.adviceText.isBlank()) {
                "No advice text is available."
            } else {
                data.adviceText
            }

        displayModelName(data.modelName)
    }

    private fun displayModelName(modelName: String?) {
        val tvModelName =
            findViewById<TextView>(R.id.tvModelName)

        if (modelName.isNullOrBlank()) {
            tvModelName.visibility = View.GONE
            tvModelName.text = ""
        } else {
            tvModelName.visibility = View.VISIBLE
            tvModelName.text = "Generated by: $modelName"
        }
    }
}
