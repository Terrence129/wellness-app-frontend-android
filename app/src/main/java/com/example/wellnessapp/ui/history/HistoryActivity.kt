// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.repository.WellnessRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Shows the user's historical wellness logs.
 *
 * Author: Member F
 */
class HistoryActivity : AppCompatActivity() {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(
            WellnessRepository(applicationContext)
        )
    }

    private lateinit var adapter: WellnessLogAdapter
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var emptyView: View
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        bindViews()
        setupRecyclerView()
        setupActions()
        observeViewModel()
        setDefaultDateRange()
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun bindViews() {
        startDateInput = findViewById(R.id.etStartDate)
        endDateInput = findViewById(R.id.etEndDate)
        emptyView = findViewById(R.id.tvHistoryEmpty)
        errorText = findViewById(R.id.tvHistoryError)
        progressBar = findViewById(R.id.progressHistory)
    }

    private fun setupRecyclerView() {
        adapter = WellnessLogAdapter { log ->
            val intent = Intent(this, WellnessLogDetailActivity::class.java).apply {
                putExtra(WellnessLogDetailActivity.EXTRA_LOG_ID, log.id)
                putExtra(WellnessLogDetailActivity.EXTRA_LOG_DATE, log.logDate)
            }
            startActivity(intent)
        }

        findViewById<RecyclerView>(R.id.recyclerWellnessLogs).apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnApplyFilter).setOnClickListener {
            loadLogs()
        }
        findViewById<View>(R.id.btnHistoryBack).setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                HistoryUiState.Loading -> showLoading()
                HistoryUiState.Empty -> showEmpty()
                is HistoryUiState.Success -> showLogs(state)
                is HistoryUiState.Error -> showError(state.message)
            }
        }
    }

    private fun loadLogs() {
        viewModel.loadLogs(
            startDate = startDateInput.text.toString().trim(),
            endDate = endDateInput.text.toString().trim()
        )
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        errorText.visibility = View.GONE
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        adapter.submitLogs(emptyList())
    }

    private fun showLogs(state: HistoryUiState.Success) {
        progressBar.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorText.visibility = View.GONE
        adapter.submitLogs(state.logs)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        emptyView.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
        adapter.submitLogs(emptyList())
    }

    private fun setDefaultDateRange() {
        startDateInput.setText(daysAgo(30))
        endDateInput.setText(today())
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
    }

    private fun daysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }
}
