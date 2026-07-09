// Team5
// @author: Deng Yunke

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Displays the authenticated user's weekly wellness summary.
 *
 */
class WeeklySummaryActivity : AppCompatActivity() {

    private val viewModel: WeeklySummaryViewModel by viewModels()
    private val wellnessRepository by lazy { WellnessRepository(applicationContext) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

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
        val calendar = Calendar.getInstance()
        endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        startDate = dateFormat.format(calendar.time)
        tvDateRange.text = "$startDate to $endDate"
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
            showCharts(data, logs, weekDates(returnedEndDate))
        }
    }

    private fun showCharts(
        data: WeeklySummaryResponse,
        logs: List<WellnessLogResponse>,
        dates: List<String>
    ) {
        chartList.removeAllViews()
        val logsByDate = logs.associateBy { it.logDate }
        val todayLog = logsByDate[dates.lastOrNull().orEmpty()]

        addChartCard(
            title = "Sleep",
            todayText = "${formatOne(todayLog?.sleepHours ?: 0.0)} h",
            averageText = "${formatOne(data.averageSleepHours)} h",
            dates = dates,
            logsByDate = logsByDate
        ) {
            it.sleepHours
        }
        addChartCard(
            title = "Mood",
            todayText = "${formatOne(todayLog?.moodScore?.toDouble() ?: 0.0)} / 5",
            averageText = "${formatOne(data.averageMoodScore)} / 5",
            dates = dates,
            logsByDate = logsByDate
        ) {
            it.moodScore?.toDouble()
        }
        addChartCard(
            title = "Water",
            todayText = "${formatOne(todayLog?.waterCups?.toDouble() ?: 0.0)} cups",
            averageText = "${formatOne(data.averageWaterCups)} cups",
            dates = dates,
            logsByDate = logsByDate
        ) {
            it.waterCups?.toDouble()
        }
        addChartCard(
            title = "Steps",
            todayText = "${todayLog?.steps ?: 0}",
            averageText = "${data.totalSteps / data.daysWithLogs.coerceAtLeast(1)}",
            dates = dates,
            logsByDate = logsByDate
        ) {
            it.steps?.toDouble()
        }
        addChartCard(
            title = "Exercise",
            todayText = "${todayLog?.exerciseMinutes ?: 0} min",
            averageText = "${data.totalExerciseMinutes / data.daysWithLogs.coerceAtLeast(1)} min",
            dates = dates,
            logsByDate = logsByDate
        ) {
            it.exerciseMinutes?.toDouble()
        }
    }

    private fun addChartCard(
        title: String,
        todayText: String,
        averageText: String,
        dates: List<String>,
        logsByDate: Map<String, WellnessLogResponse>,
        valueOf: (WellnessLogResponse) -> Double?
    ) {
        val values = dates.map { it to (logsByDate[it]?.let(valueOf) ?: 0.0) }
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
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL

            addView(LinearLayout(this@WeeklySummaryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 94.dp)
                gravity = Gravity.BOTTOM
                orientation = LinearLayout.HORIZONTAL
                values.forEach { (_, value) ->
                    addView(barColumn(value, maxValue))
                }
            })

            addView(View(this@WeeklySummaryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1.dp)
                background = solidDrawable(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.classic_gray))
            })

            addView(LinearLayout(this@WeeklySummaryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                values.forEach { (date, _) ->
                    addView(axisLabel(date))
                }
            })
        })

        row.addView(LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(104), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(12)
            }
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.VERTICAL
            addView(metricText("· today: $todayText"))
            addView(metricText("· average: $averageText"))
        })

        card.addView(row)
        chartList.addView(card)
    }

    private fun barColumn(value: Double, maxValue: Double): FrameLayout {
        return FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            addView(View(this@WeeklySummaryActivity).apply {
                layoutParams = FrameLayout.LayoutParams(
                    12.dp,
                    ((value / maxValue) * 88).toInt().coerceAtLeast(if (value > 0.0) 6 else 0).dp,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                )
                background = topRoundedDrawable(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_orange), 8.dp)
            })
        }
    }

    private fun axisLabel(date: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
            text = date.takeLast(5)
            setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_text_secondary))
            textSize = 9f
        }
    }

    private fun metricText(textValue: String): TextView {
        return TextView(this).apply {
            text = textValue
            setTextColor(ContextCompat.getColor(this@WeeklySummaryActivity, R.color.health_orange))
            textSize = 13f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
    }

    private fun solidDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
        }
    }

    private fun topRoundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = floatArrayOf(
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                0f,
                0f,
                0f,
                0f
            )
        }
    }

    private fun weekDates(lastDate: String): List<String> {
        val calendar = Calendar.getInstance()
        runCatching { dateFormat.parse(lastDate) }.getOrNull()?.let {
            calendar.time = it
        }
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        return List(7) {
            dateFormat.format(calendar.time).also {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun formatOne(value: Double): String {
        return String.format(Locale.US, "%.1f", value)
    }

    private val Int.dp: Int
        get() = dp(this)

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
