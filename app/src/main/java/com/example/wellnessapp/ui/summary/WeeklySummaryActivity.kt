package com.example.wellnessapp.ui.summary

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WeeklySummaryResponse
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import com.example.wellnessapp.ui.navigation.BottomNavigationController
import com.example.wellnessapp.ui.navigation.BottomNavigationController.ActiveItem
import com.example.wellnessapp.util.UiState
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Displays the authenticated user's weekly wellness summary.
 *
 * @author Yunke Deng
 */
class WeeklySummaryActivity : AppCompatActivity() {

    private val viewModel: WeeklySummaryViewModel by viewModels()
    private val wellnessRepository by lazy { WellnessRepository(applicationContext) }

    private lateinit var tvDateRange: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var summaryContent: LinearLayout
    private lateinit var btnRefresh: Button
    private lateinit var tipsText: TextView
    private lateinit var chartList: LinearLayout

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
        tipsText = findViewById(R.id.tvTips)
        chartList = findViewById(R.id.chartList)
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
            if (returnedStartDate.isBlank() || returnedEndDate.isBlank()) {
                "Latest available week"
            } else {
                "$returnedStartDate to $returnedEndDate"
            }

        tipsText.text = "Tips\n${data.summary.ifBlank { "No summary text is available." }}"
        chartList.removeAllViews()

        lifecycleScope.launch {
            val logs = runCatching {
                wellnessRepository.getWellnessLogs(
                    returnedStartDate.takeIf { it.isNotBlank() },
                    returnedEndDate.takeIf { it.isNotBlank() }
                ).data.orEmpty()
            }.getOrDefault(emptyList()).sortedBy { it.logDate }
            showCharts(data, logs)
        }
    }

    private fun showCharts(data: WeeklySummaryResponse, logs: List<WellnessLogResponse>) {
        chartList.removeAllViews()
        if (logs.isEmpty()) {
            chartList.addView(messageCard("No daily log data is available for this week."))
            return
        }

        addChartCard("Sleep", String.format(Locale.US, "%.1f h avg", data.averageSleepHours), logs) {
            it.sleepHours
        }
        addChartCard("Mood", String.format(Locale.US, "%.1f / 5 avg", data.averageMoodScore), logs) {
            it.moodScore?.toDouble()
        }
        addChartCard("Water", String.format(Locale.US, "%.1f cups avg", data.averageWaterCups), logs) {
            it.waterCups?.toDouble()
        }
        addChartCard("Steps", "${data.totalSteps / data.daysWithLogs.coerceAtLeast(1)} avg", logs) {
            it.steps?.toDouble()
        }
        addChartCard("Exercise", "${data.totalExerciseMinutes / data.daysWithLogs.coerceAtLeast(1)} min avg", logs) {
            it.exerciseMinutes?.toDouble()
        }
    }

    private fun addChartCard(
        title: String,
        valueText: String,
        logs: List<WellnessLogResponse>,
        valueOf: (WellnessLogResponse) -> Double?
    ) {
        val values = logs.map { it.logDate to (valueOf(it) ?: 0.0) }
        val maxValue = values.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0
        val card = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
            background = ContextCompat.getDrawable(this@WeeklySummaryActivity, R.drawable.bg_card)
            elevation = dp(2).toFloat()
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        card.addView(TextView(this).apply {
            text = title
            setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_text_primary))
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(122), 1f)
            gravity = Gravity.BOTTOM
            orientation = LinearLayout.HORIZONTAL
            values.forEach { (date, value) ->
                addView(barColumn(date, value, maxValue))
            }
        })

        row.addView(TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(88), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12)
            }
            gravity = Gravity.CENTER
            text = valueText
            setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_orange))
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        card.addView(row)
        chartList.addView(card)
    }

    private fun barColumn(date: String, value: Double, maxValue: Double): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL

            addView(FrameLayout(this@WeeklySummaryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 94.dp)
                addView(View(this@WeeklySummaryActivity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        12.dp,
                        ((value / maxValue) * 88).toInt().coerceAtLeast(if (value > 0.0) 6 else 1).dp,
                        Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    )
                    background = roundedDrawable(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_orange), 8.dp)
                })
            })

            addView(TextView(this@WeeklySummaryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 6.dp
                }
                gravity = Gravity.CENTER
                text = date.takeLast(5)
                setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_text_secondary))
                textSize = 10f
            })
        }
    }

    private fun messageCard(message: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(14)
            }
            background = ContextCompat.getDrawable(this@WeeklySummaryActivity, R.drawable.bg_card)
            elevation = dp(2).toFloat()
            setPadding(dp(16), dp(16), dp(16), dp(16))
            text = message
            setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_text_secondary))
            textSize = 15f
        }
    }

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private val Int.dp: Int
        get() = dp(this)

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
