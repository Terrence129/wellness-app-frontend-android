// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.model.WellnessLogResponse
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Renders the pinned carousel on the home screen.
 *
 * Author: Member F
 */
class PinnedCarouselAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var user: UserResponse? = null
    private var todayLog: WellnessLogResponse? = null

    fun submitData(user: UserResponse, todayLog: WellnessLogResponse?) {
        this.user = user
        this.todayLog = todayLog
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = CARD_COUNT

    override fun getItemViewType(position: Int): Int {
        return if (position == PROFILE_POSITION) VIEW_TYPE_PROFILE else VIEW_TYPE_TODAY_LOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_PROFILE) {
            R.layout.item_pinned_profile_card
        } else {
            R.layout.item_pinned_today_log_card
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        view.layoutParams = RecyclerView.LayoutParams(cardWidth(parent), ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginEnd = parent.resources.getDimensionPixelSize(R.dimen.pinned_card_gap)
        }
        return if (viewType == VIEW_TYPE_PROFILE) ProfileViewHolder(view) else TodayLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ProfileViewHolder -> holder.bind(user)
            is TodayLogViewHolder -> holder.bind(todayLog)
        }
    }

    private fun cardWidth(parent: ViewGroup): Int {
        val parentWidth = parent.width.takeIf { it > 0 }
            ?: (parent.resources.displayMetrics.widthPixels - parent.resources.getDimensionPixelSize(R.dimen.home_horizontal_padding_total))
        return parentWidth - parent.resources.getDimensionPixelSize(R.dimen.pinned_card_peek)
    }

    private class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bmiValueText: TextView = itemView.findViewById(R.id.tvBmiValue)
        private val bmiStatusText: TextView = itemView.findViewById(R.id.tvBmiStatus)
        private val heightText: TextView = itemView.findViewById(R.id.tvHeight)
        private val weightText: TextView = itemView.findViewById(R.id.tvWeight)
        private val ageText: TextView = itemView.findViewById(R.id.tvAge)

        fun bind(user: UserResponse?) {
            val context = itemView.context
            val height = user?.heightCm
            val weight = user?.weightKg
            val age = user?.age
            val bmi = calculateBmi(height, weight)

            bmiValueText.text = if (bmi == null) {
                context.getString(R.string.bmi_empty)
            } else {
                context.getString(R.string.bmi_format, String.format(Locale.US, "%.1f", bmi))
            }
            bmiStatusText.text = bmi?.let { bmiStatusText(it) } ?: context.getString(R.string.profile_incomplete)
            heightText.text = height?.takeIf { it > 0.0 }?.let {
                context.getString(R.string.height_format, formatDecimal(it))
            } ?: context.getString(R.string.height_empty)
            weightText.text = weight?.takeIf { it > 0.0 }?.let {
                context.getString(R.string.weight_format, formatDecimal(it))
            } ?: context.getString(R.string.weight_empty)
            ageText.text = age?.takeIf { it > 0 }?.let {
                context.getString(R.string.age_format, it.toString())
            } ?: context.getString(R.string.age_empty)
        }

        private fun calculateBmi(heightCm: Double?, weightKg: Double?): Double? {
            if (heightCm == null || weightKg == null || heightCm <= 0.0 || weightKg <= 0.0) return null
            val heightMeter = heightCm / 100.0
            return weightKg / (heightMeter * heightMeter)
        }

        private fun bmiStatusText(bmi: Double): String {
            val context = itemView.context
            return when {
                bmi < 18.5 -> context.getString(R.string.bmi_underweight)
                bmi < 25.0 -> context.getString(R.string.bmi_normal)
                bmi < 30.0 -> context.getString(R.string.bmi_overweight)
                else -> context.getString(R.string.bmi_obese)
            }
        }
    }

    private class TodayLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusText: TextView = itemView.findViewById(R.id.tvTodayStatus)
        private val sleepText: TextView = itemView.findViewById(R.id.tvSleepHours)
        private val moodText: TextView = itemView.findViewById(R.id.tvMoodScore)
        private val waterText: TextView = itemView.findViewById(R.id.tvWaterCups)
        private val stepsText: TextView = itemView.findViewById(R.id.tvSteps)
        private val exerciseText: TextView = itemView.findViewById(R.id.tvExerciseMinutes)
        private val noteText: TextView = itemView.findViewById(R.id.tvNote)

        fun bind(log: WellnessLogResponse?) {
            val context = itemView.context
            if (log == null) {
                statusText.text = context.getString(R.string.home_no_log_today)
                sleepText.text = context.getString(R.string.sleep_hours_empty)
                moodText.text = context.getString(R.string.mood_score_empty)
                waterText.text = context.getString(R.string.water_cups_empty)
                stepsText.text = context.getString(R.string.steps_empty)
                exerciseText.text = context.getString(R.string.exercise_minutes_empty)
                noteText.text = context.getString(R.string.home_no_note)
                return
            }

            statusText.text = context.getString(R.string.home_log_ready)
            sleepText.text = context.getString(R.string.sleep_hours_format, formatDecimal(log.sleepHours))
            moodText.text = context.getString(R.string.mood_score_format, log.moodScore?.toString() ?: "--")
            waterText.text = context.getString(R.string.water_cups_format, log.waterCups?.toString() ?: "--")
            stepsText.text = context.getString(R.string.steps_format, log.steps?.toString() ?: "--")
            exerciseText.text = context.getString(R.string.exercise_minutes_format, log.exerciseMinutes?.toString() ?: "--")
            noteText.text = log.note?.takeIf { it.isNotBlank() } ?: context.getString(R.string.home_no_note)
        }
    }

    companion object {
        const val PROFILE_POSITION = 0
        private const val CARD_COUNT = 2
        private const val VIEW_TYPE_PROFILE = 1
        private const val VIEW_TYPE_TODAY_LOG = 2

        fun formatDecimal(value: Double?): String {
            if (value == null) return "--"
            return if (value % 1.0 == 0.0) {
                value.roundToInt().toString()
            } else {
                String.format(Locale.US, "%.1f", value)
            }
        }
    }
}
