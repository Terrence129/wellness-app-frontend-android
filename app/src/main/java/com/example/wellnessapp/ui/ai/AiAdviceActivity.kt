package com.example.wellnessapp.ui.ai

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var historyStartInput: EditText
    private lateinit var historyEndInput: EditText
    private lateinit var historyProgressBar: ProgressBar
    private lateinit var historyStatusText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var loadMoreHistoryButton: Button

    private val historyAdapter = AiAdviceHistoryAdapter { advice ->
        viewModel.loadAdviceDetail(advice)
    }

    private var startDate = ""
    private var endDate = ""
    private val historyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_advice)

        bindViews()
        prepareDefaultDateRange()
        configureHistoryRecyclerView()
        observeAdviceState()
        observeHistoryState()
        setClickListeners()

        // Load the most recently saved advice when the page opens.
        viewModel.loadLatestAdvice()
        viewModel.loadAdviceHistory()
    }

    private fun bindViews() {
        tvRequestedRange = findViewById(R.id.tvRequestedRange)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        adviceContent = findViewById(R.id.adviceContent)
        btnGenerateAdvice = findViewById(R.id.btnGenerateAdvice)
        historyStartInput = findViewById(R.id.etAdviceHistoryStartDate)
        historyEndInput = findViewById(R.id.etAdviceHistoryEndDate)
        historyProgressBar = findViewById(R.id.progressAdviceHistory)
        historyStatusText = findViewById(R.id.tvAdviceHistoryStatus)
        historyRecyclerView = findViewById(R.id.rvAdviceHistory)
        loadMoreHistoryButton = findViewById(R.id.btnLoadMoreAdviceHistory)
    }

    private fun configureHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setClickListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnGenerateAdvice.setOnClickListener {
            viewModel.generateAdvice(
                startDate = startDate,
                endDate = endDate,
                refreshHistoryOnSuccess = true
            )
        }

        historyStartInput.setOnClickListener {
            showHistoryDatePicker(historyStartInput) { selectedDate ->
                val selectedEndDate = parseHistoryDate(historyEndInput.text.toString())
                if (selectedEndDate != null && selectedEndDate < selectedDate) {
                    historyEndInput.text.clear()
                }
            }
        }
        historyEndInput.setOnClickListener {
            showHistoryDatePicker(
                target = historyEndInput,
                minDate = parseHistoryDate(historyStartInput.text.toString())
            )
        }
        findViewById<Button>(R.id.btnApplyAdviceHistoryFilter).setOnClickListener {
            viewModel.loadAdviceHistory(
                startDate = historyStartInput.text.toString(),
                endDate = historyEndInput.text.toString(),
                reset = true
            )
        }
        findViewById<Button>(R.id.btnClearAdviceHistoryFilter).setOnClickListener {
            historyStartInput.text.clear()
            historyEndInput.text.clear()
            viewModel.loadAdviceHistory(startDate = null, endDate = null, reset = true)
        }
        loadMoreHistoryButton.setOnClickListener {
            viewModel.loadAdviceHistory(reset = false)
        }
    }

    private fun showHistoryDatePicker(
        target: EditText,
        minDate: Long? = null,
        onDateSelected: ((Long) -> Unit)? = null
    ) {
        val calendar = Calendar.getInstance()
        parseHistoryDate(target.text.toString())?.let {
            calendar.timeInMillis = it
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                target.setText(date)
                onDateSelected?.invoke(parseHistoryDate(date) ?: return@DatePickerDialog)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate?.let { datePicker.minDate = it }
            datePicker.maxDate = Calendar.getInstance().timeInMillis
        }.show()
    }

    private fun parseHistoryDate(value: String): Long? {
        return runCatching {
            historyDateFormat.parse(value.trim())?.time
        }.getOrNull()
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

    private fun observeHistoryState() {
        viewModel.historyState.observe(this) { state ->
            historyAdapter.submitItems(state.items)
            historyProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            when {
                !state.errorMessage.isNullOrBlank() -> {
                    historyStatusText.visibility = View.VISIBLE
                    historyStatusText.text = state.errorMessage
                    historyStatusText.setTextColor(getColor(R.color.health_error))
                }

                state.isEmpty -> {
                    historyStatusText.visibility = View.VISIBLE
                    historyStatusText.text = "No saved AI advice found."
                    historyStatusText.setTextColor(getColor(R.color.health_text_secondary))
                }

                else -> {
                    historyStatusText.visibility = View.GONE
                    historyStatusText.text = ""
                    historyStatusText.setTextColor(getColor(R.color.health_text_secondary))
                }
            }

            loadMoreHistoryButton.visibility =
                if (state.hasMore || state.isLoadingMore) View.VISIBLE else View.GONE
            loadMoreHistoryButton.isEnabled = !state.isLoadingMore
            loadMoreHistoryButton.text =
                if (state.isLoadingMore) "Loading..." else "Load More"
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
