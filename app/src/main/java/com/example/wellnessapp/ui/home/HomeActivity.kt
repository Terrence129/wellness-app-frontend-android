// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.UserRepository
import com.example.wellnessapp.data.repository.WellnessRepository
import com.example.wellnessapp.ui.ai.AiCoachActivity
import com.example.wellnessapp.ui.history.WellnessLogAdapter
import com.example.wellnessapp.ui.history.WellnessLogDetailActivity
import com.example.wellnessapp.ui.log.EditWellnessLogActivity
import com.example.wellnessapp.ui.log.AddWellnessLogActivity
import com.example.wellnessapp.ui.profile.ProfileActivity
import com.example.wellnessapp.ui.summary.WeeklySummaryActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Home screen for the SimpleWell Android app.
 *
 * Author: Member F
 */
class HomeActivity : AppCompatActivity() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            UserRepository(applicationContext),
            WellnessRepository(applicationContext)
        )
    }

    private lateinit var welcomeText: TextView
    private lateinit var todayDateText: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var pinnedCards: RecyclerView
    private lateinit var recentLogsList: RecyclerView
    private lateinit var recentLogsEmptyText: TextView
    private lateinit var profileDot: View
    private lateinit var todayDot: View
    private val pinnedAdapter = PinnedCarouselAdapter()
    private lateinit var recentLogsAdapter: WellnessLogAdapter
    private val snapHelper = PagerSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bindViews()
        setupPinnedCarousel()
        setupRecentLogs()
        setupActions()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadHome(currentDate())
    }

    private fun bindViews() {
        welcomeText = findViewById(R.id.tvWelcome)
        todayDateText = findViewById(R.id.tvTodayDate)
        errorText = findViewById(R.id.tvHomeError)
        progressBar = findViewById(R.id.progressHome)
        pinnedCards = findViewById(R.id.rvPinnedCards)
        recentLogsList = findViewById(R.id.rvRecentLogs)
        recentLogsEmptyText = findViewById(R.id.tvRecentLogsEmpty)
        profileDot = findViewById(R.id.dotProfile)
        todayDot = findViewById(R.id.dotToday)
    }

    private fun setupPinnedCarousel() {
        pinnedCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        pinnedCards.adapter = pinnedAdapter
        pinnedCards.isNestedScrollingEnabled = false
        snapHelper.attachToRecyclerView(pinnedCards)
        pinnedCards.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updatePinnedIndicator(currentPinnedPosition())
                }
            }
        })
    }

    private fun setupRecentLogs() {
        recentLogsAdapter = WellnessLogAdapter(
            onItemClick = { log ->
                startActivity(
                    Intent(this, WellnessLogDetailActivity::class.java)
                        .putExtra(WellnessLogDetailActivity.EXTRA_LOG_ID, log.id)
                        .putExtra(WellnessLogDetailActivity.EXTRA_LOG_DATE, log.logDate)
                )
            },
            onEditClick = { log -> openEdit(log) },
            onDeleteClick = { log -> confirmDelete(log) }
        )
        recentLogsList.layoutManager = LinearLayoutManager(this)
        recentLogsList.adapter = recentLogsAdapter
        recentLogsList.isNestedScrollingEnabled = false
    }

    private fun setupActions() {
        findViewById<View>(R.id.navHome).setOnClickListener {
            pinnedCards.smoothScrollToPosition(PinnedCarouselAdapter.PROFILE_POSITION)
        }

        findViewById<View>(R.id.navAddLog).setOnClickListener {
            startActivity(Intent(this, AddWellnessLogActivity::class.java))
        }

        findViewById<View>(R.id.navTrends).setOnClickListener {
            startActivity(Intent(this, WeeklySummaryActivity::class.java))
        }

        findViewById<View>(R.id.navAi).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AiCoachActivity::class.java
                )
            )
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }


    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                HomeUiState.Loading -> showLoading()
                is HomeUiState.Success -> showHome(state)
                is HomeUiState.Error -> showError(state.message)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
    }

    private fun showHome(state: HomeUiState.Success) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.GONE
        welcomeText.text = getString(R.string.home_welcome_format, state.user.username)
        todayDateText.text = state.today
        pinnedAdapter.submitData(state.user, state.todayLog)
        recentLogsAdapter.submitLogs(state.recentLogs)
        recentLogsList.visibility = if (state.recentLogs.isEmpty()) View.GONE else View.VISIBLE
        recentLogsEmptyText.visibility = if (state.recentLogs.isEmpty()) View.VISIBLE else View.GONE
        updatePinnedIndicator(currentPinnedPosition())
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        errorText.text = message
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
                    viewModel.loadHome(currentDate())
                } else {
                    showError(response.message.ifBlank { "Unable to delete wellness log." })
                }
            } catch (error: Exception) {
                showError(error.message ?: "Unable to delete wellness log.")
            }
        }
    }

    private fun currentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun currentPinnedPosition(): Int {
        val layoutManager = pinnedCards.layoutManager ?: return PinnedCarouselAdapter.PROFILE_POSITION
        val snapView = snapHelper.findSnapView(layoutManager) ?: return PinnedCarouselAdapter.PROFILE_POSITION
        return layoutManager.getPosition(snapView)
    }

    private fun updatePinnedIndicator(position: Int) {
        if (position == PinnedCarouselAdapter.PROFILE_POSITION) {
            setIndicatorState(profileDot, true)
            setIndicatorState(todayDot, false)
        } else {
            setIndicatorState(profileDot, false)
            setIndicatorState(todayDot, true)
        }
    }

    private fun setIndicatorState(dot: View, isActive: Boolean) {
        dot.setBackgroundResource(if (isActive) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive)
        dot.layoutParams = dot.layoutParams.apply {
            width = resources.getDimensionPixelSize(
                if (isActive) {
                    R.dimen.pinned_indicator_active_width
                } else {
                    R.dimen.pinned_indicator_inactive_width
                }
            )
        }
    }
}
