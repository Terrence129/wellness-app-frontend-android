// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.history

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import com.example.wellnessapp.ui.log.EditWellnessLogActivity
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Shows the user's historical wellness logs.
 *
 * Author: Member F
 */
class HistoryActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

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
        BottomNavigationController.attach(this, ActiveItem.TRENDS_HISTORY)
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
        adapter = WellnessLogAdapter(
            onItemClick = { log ->
                val intent = Intent(this, WellnessLogDetailActivity::class.java).apply {
                    putExtra(WellnessLogDetailActivity.EXTRA_LOG_ID, log.id)
                    putExtra(WellnessLogDetailActivity.EXTRA_LOG_DATE, log.logDate)
                }
                startActivity(intent)
            },
            onEditClick = { log -> openEdit(log) },
            onDeleteClick = { log -> confirmDelete(log) }
        )

        findViewById<RecyclerView>(R.id.recyclerWellnessLogs).apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = this@HistoryActivity.adapter
        }
    }

    private fun setupActions() {
        startDateInput.setOnClickListener {
            showDatePicker(startDateInput) { showDatePicker(endDateInput, minDate = it) }
        }
        endDateInput.setOnClickListener {
            showDatePicker(endDateInput, minDate = parseDate(startDateInput.text.toString()))
        }

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

    private fun showDatePicker(
        target: EditText,
        minDate: Long? = null,
        onDateSelected: ((Long) -> Unit)? = null
    ) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        runCatching { dateFormat.parse(target.text.toString().trim()) }.getOrNull()?.let {
            calendar.time = it
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                target.setText(date)
                onDateSelected?.invoke(parseDate(date) ?: return@DatePickerDialog)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate?.let { datePicker.minDate = it }
            datePicker.maxDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
        }.show()
    }

    private fun parseDate(value: String): Long? {
        return runCatching { dateFormat.parse(value.trim())?.time }.getOrNull()
    }

    private fun today(): String {
        return dateFormat.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).time)
    }

    private fun daysAgo(days: Int): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }

    private fun openEdit(log: WellnessLogResponse) {
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

    private fun confirmDelete(log: WellnessLogResponse) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_log_title)
            .setMessage(R.string.delete_log_message)
            .setPositiveButton(R.string.delete) { _, _ -> deleteLog(log.id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteLog(id: Long) {
        lifecycleScope.launch {
            try {
                val response = WellnessRepository(applicationContext).deleteWellnessLog(id)
                if (response.success) {
                    loadLogs()
                } else {
                    showError(response.message.ifBlank { "Unable to delete wellness log." })
                }
            } catch (error: Exception) {
                showError(error.message ?: "Unable to delete wellness log.")
            }
        }
    }
}
