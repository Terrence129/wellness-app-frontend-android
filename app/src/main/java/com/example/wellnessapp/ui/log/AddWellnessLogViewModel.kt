// Team5
// Author: Chen Chen
package com.example.wellnessapp.ui.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.WellnessLogRequest
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import kotlinx.coroutines.launch

sealed class AddLogUiState {
    object Idle : AddLogUiState()
    object Loading : AddLogUiState()
    data class Success(val log: WellnessLogResponse) : AddLogUiState()
    data class Error(val message: String) : AddLogUiState()
}

class AddWellnessLogViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = WellnessRepository(application.applicationContext)

    private val _state = MutableLiveData<AddLogUiState>(AddLogUiState.Idle)
    val state: LiveData<AddLogUiState> = _state

    fun createLog(
        logDate: String,
        sleepHoursText: String,
        moodScoreText: String,
        waterCupsText: String,
        stepsText: String,
        exerciseMinutesText: String,
        note: String
    ) {
        val request = buildRequestOrShowError(
            logDate = logDate,
            sleepHoursText = sleepHoursText,
            moodScoreText = moodScoreText,
            waterCupsText = waterCupsText,
            stepsText = stepsText,
            exerciseMinutesText = exerciseMinutesText,
            note = note
        ) ?: return

        viewModelScope.launch {
            _state.value = AddLogUiState.Loading

            runCatching {
                repository.createWellnessLog(request)
            }.onSuccess { response ->
                val data = response.data
                if (response.success && data != null) {
                    _state.value = AddLogUiState.Success(data)
                } else {
                    _state.value = AddLogUiState.Error(
                        response.message.ifBlank { "Create failed" }
                    )
                }
            }.onFailure { throwable ->
                _state.value = AddLogUiState.Error(
                    throwable.message ?: "Network error"
                )
            }
        }
    }

    private fun buildRequestOrShowError(
        logDate: String,
        sleepHoursText: String,
        moodScoreText: String,
        waterCupsText: String,
        stepsText: String,
        exerciseMinutesText: String,
        note: String
    ): WellnessLogRequest? {
        val date = logDate.trim()
        if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            _state.value = AddLogUiState.Error("Please choose a valid date")
            return null
        }

        val sleepHours = sleepHoursText.trim().toDoubleOrNull()
        if (sleepHours == null || sleepHours < 0.0 || sleepHours > 24.0) {
            _state.value = AddLogUiState.Error("Sleep hours must be between 0 and 24")
            return null
        }

        val moodScore = moodScoreText.trim().toIntOrNull()
        if (moodScore == null || moodScore !in 1..5) {
            _state.value = AddLogUiState.Error("Mood score must be between 1 and 5")
            return null
        }

        val waterCups = waterCupsText.trim().toIntOrNull()
        if (waterCups == null || waterCups < 0 || waterCups > 50) {
            _state.value = AddLogUiState.Error("Water cups must be between 0 and 50")
            return null
        }

        val steps = stepsText.trim().toIntOrNull()
        if (steps == null || steps < 0 || steps > 100000) {
            _state.value = AddLogUiState.Error("Steps must be between 0 and 100000")
            return null
        }

        val exerciseMinutes = exerciseMinutesText.trim().toIntOrNull()
        if (exerciseMinutes == null || exerciseMinutes < 0 || exerciseMinutes > 1440) {
            _state.value = AddLogUiState.Error("Exercise minutes must be between 0 and 1440")
            return null
        }

        val cleanNote = note.trim().ifBlank { null }

        return WellnessLogRequest(
            logDate = date,
            sleepHours = sleepHours,
            moodScore = moodScore,
            waterCups = waterCups,
            steps = steps,
            exerciseMinutes = exerciseMinutes,
            note = cleanNote
        )
    }
}