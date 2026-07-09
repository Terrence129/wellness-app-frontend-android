// Team5
// @author: Deng Yunke

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

class AiAdviceHistoryActivity : AppCompatActivity() {

    private val viewModel: AiAdviceViewModel by viewModels()
    private val historyDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val historyAdapter = AiAdviceHistoryAdapter { advice ->
        viewModel.loadAdviceDetail(advice)
    }

    private lateinit var detailContent: LinearLayout
    private lateinit var historyStartInput: EditText
    private lateinit var historyEndInput: EditText
    private lateinit var historyProgressBar: ProgressBar
    private lateinit var historyStatusText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var loadMoreHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_advice_history)

        bindViews()
        bindActions()
        configureHistoryRecyclerView()
        observeAdviceState()
        observeHistoryState()
        viewModel.loadAdviceHistory()
    }

    private fun bindViews() {
        detailContent = findViewById(R.id.adviceDetailContent)
        historyStartInput = findViewById(R.id.etAdviceHistoryStartDate)
        historyEndInput = findViewById(R.id.etAdviceHistoryEndDate)
        historyProgressBar = findViewById(R.id.progressAdviceHistory)
        historyStatusText = findViewById(R.id.tvAdviceHistoryStatus)
        historyRecyclerView = findViewById(R.id.rvAdviceHistory)
        loadMoreHistoryButton = findViewById(R.id.btnLoadMoreAdviceHistory)
    }

    private fun bindActions() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
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

    private fun configureHistoryRecyclerView() {
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun observeAdviceState() {
        viewModel.uiState.observe(this) { state ->
            if (state is UiState.Success) {
                showAdvice(state.data)
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
            loadMoreHistoryButton.text = if (state.isLoadingMore) "Loading..." else "Load More"
        }
    }

    private fun showAdvice(data: AiAdviceResponse) {
        detailContent.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvAdviceDate).text =
            "Advice date: ${data.adviceDate ?: "Unknown"}"
        findViewById<TextView>(R.id.tvAdviceRange).text =
            "Based on records from ${data.startDate ?: "--"} to ${data.endDate ?: "--"}"
        findViewById<TextView>(R.id.tvAdviceText).text =
            data.adviceText.ifBlank { "No advice text is available." }

        findViewById<TextView>(R.id.tvModelName).apply {
            if (data.modelName.isNullOrBlank()) {
                visibility = View.GONE
                text = ""
            } else {
                visibility = View.VISIBLE
                text = "Generated by: ${data.modelName}"
            }
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
}
