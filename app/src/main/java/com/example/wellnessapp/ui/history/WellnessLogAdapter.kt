// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.WellnessLogResponse
import java.util.Locale

/**
 * RecyclerView adapter for wellness history records.
 *
 * Author: Member F
 */
class WellnessLogAdapter(
    private val onItemClick: (WellnessLogResponse) -> Unit,
    private val onEditClick: (WellnessLogResponse) -> Unit,
    private val onDeleteClick: (WellnessLogResponse) -> Unit
) : RecyclerView.Adapter<WellnessLogAdapter.WellnessLogViewHolder>() {

    private val logs = mutableListOf<WellnessLogResponse>()

    fun submitLogs(newLogs: List<WellnessLogResponse>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WellnessLogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wellness_log, parent, false)
        return WellnessLogViewHolder(view, onItemClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: WellnessLogViewHolder, position: Int) {
        holder.bind(logs[position])
    }

    override fun getItemCount(): Int = logs.size

    /**
     * Author: Member F
     */
    class WellnessLogViewHolder(
        itemView: View,
        private val onItemClick: (WellnessLogResponse) -> Unit,
        private val onEditClick: (WellnessLogResponse) -> Unit,
        private val onDeleteClick: (WellnessLogResponse) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val dateText: TextView = itemView.findViewById(R.id.tvItemDate)
        private val editButton: ImageButton = itemView.findViewById(R.id.btnItemEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btnItemDelete)
        private val sleepText: TextView = itemView.findViewById(R.id.tvItemSleep)
        private val moodText: TextView = itemView.findViewById(R.id.tvItemMood)
        private val waterText: TextView = itemView.findViewById(R.id.tvItemWater)
        private val stepsText: TextView = itemView.findViewById(R.id.tvItemSteps)
        private val exerciseText: TextView = itemView.findViewById(R.id.tvItemExercise)

        fun bind(log: WellnessLogResponse) {
            val context = itemView.context
            dateText.text = log.logDate
            sleepText.text = context.getString(R.string.sleep_hours_format, formatDecimal(log.sleepHours))
            moodText.text = context.getString(R.string.mood_score_format, log.moodScore?.toString() ?: "--")
            waterText.text = context.getString(R.string.water_cups_format, log.waterCups?.toString() ?: "--")
            stepsText.text = context.getString(R.string.steps_format, log.steps?.toString() ?: "--")
            exerciseText.text = context.getString(R.string.exercise_minutes_format, log.exerciseMinutes?.toString() ?: "--")
            itemView.setOnClickListener { onItemClick(log) }
            editButton.setOnClickListener { onEditClick(log) }
            deleteButton.setOnClickListener { onDeleteClick(log) }
        }

        private fun formatDecimal(value: Double?): String {
            if (value == null) return "--"
            return if (value % 1.0 == 0.0) {
                value.toInt().toString()
            } else {
                String.format(Locale.US, "%.1f", value)
            }
        }
    }
}
