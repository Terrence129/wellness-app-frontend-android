package com.example.wellnessapp.ui.summary

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WeeklySummaryResponse
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem
import com.example.wellnessapp.util.UiState
import java.util.Locale

/**
 * Displays the authenticated user's weekly wellness summary.
 *
 * @author Yunke Deng
 */
class WeeklySummaryActivity : AppCompatActivity() {

    private val viewModel: WeeklySummaryViewModel by viewModels()

    private lateinit var tvDateRange: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var summaryContent: LinearLayout
    private lateinit var btnRefresh: Button

    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_summary)


        bindViews()
        prepareDefaultDateRange()
        observeSummaryState()
        BottomNavigationController.attach(this, ActiveItem.TRENDS_HISTORY)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnRefresh.setOnClickListener {
            viewModel.loadWeeklySummary(startDate, endDate)
        }

        viewModel.loadWeeklySummary(startDate, endDate)
    }

    private fun bindViews() {
        tvDateRange = findViewById(R.id.tvDateRange)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        summaryContent = findViewById(R.id.summaryContent)
        btnRefresh = findViewById(R.id.btnRefresh)
    }

    private fun prepareDefaultDateRange() {
        startDate = ""
        endDate = ""
        tvDateRange.text = "Latest available week"
    }

    private fun observeSummaryState() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit

                UiState.Loading -> showLoading()

                UiState.Empty -> showEmpty()

                is UiState.Error -> showError(state.message)

                is UiState.Success -> showSummary(state.data)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        summaryContent.visibility = View.GONE
        btnRefresh.isEnabled = false
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        summaryContent.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        btnRefresh.isEnabled = true

        tvError.text =
            "No wellness records were found for this period."
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        summaryContent.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        btnRefresh.isEnabled = true

        tvError.text = message
    }

    private fun showSummary(data: WeeklySummaryResponse) {
        progressBar.visibility = View.GONE
        tvError.visibility = View.GONE
        summaryContent.visibility = View.VISIBLE
        btnRefresh.isEnabled = true

        val returnedStartDate = data.startDate ?: startDate
        val returnedEndDate = data.endDate ?: endDate

        tvDateRange.text =
            "$returnedStartDate to $returnedEndDate"

        findViewById<TextView>(R.id.tvDaysWithLogs).text =
            "Days with records: ${data.daysWithLogs}"

        findViewById<TextView>(R.id.tvAverageSleep).text =
            String.format(
                Locale.US,
                "Average sleep: %.1f hours",
                data.averageSleepHours
            )

        findViewById<TextView>(R.id.tvAverageMood).text =
            String.format(
                Locale.US,
                "Average mood: %.1f / 5",
                data.averageMoodScore
            )

        findViewById<TextView>(R.id.tvAverageWater).text =
            String.format(
                Locale.US,
                "Average water: %.1f cups",
                data.averageWaterCups
            )

        findViewById<TextView>(R.id.tvTotalSteps).text =
            "Total steps: ${data.totalSteps}"

        findViewById<TextView>(R.id.tvTotalExercise).text =
            "Total exercise: ${data.totalExerciseMinutes} minutes"

        findViewById<TextView>(R.id.tvSummary).text =
            if (data.summary.isBlank()) {
                "No summary text is available."
            } else {
                data.summary
            }
    }
}
