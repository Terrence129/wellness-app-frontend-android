// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.summary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.WeeklySummaryResponse
import com.example.wellnessapp.data.repository.SummaryRepository
import com.example.wellnessapp.util.UiState
import kotlinx.coroutines.launch

/**
 * Manages weekly wellness summary data and screen state.
 *
 */
class WeeklySummaryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SummaryRepository(application)

    private val _uiState =
        MutableLiveData<UiState<WeeklySummaryResponse>>(UiState.Idle)

    val uiState: LiveData<UiState<WeeklySummaryResponse>>
        get() = _uiState

    fun loadWeeklySummary(
        startDate: String,
        endDate: String
    ) {
        if (startDate > endDate) {
            _uiState.value =
                UiState.Error("Start date cannot be later than end date.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val response = repository.getWeeklySummary(
                    startDate = startDate,
                    endDate = endDate
                )

                val data = response.data

                _uiState.value = when {
                    !response.success ->
                        UiState.Error(response.message)

                    data == null ->
                        UiState.Empty

                    data.daysWithLogs == 0 ->
                        UiState.Empty

                    else ->
                        UiState.Success(data)
                }
            } catch (exception: Exception) {
                _uiState.value = UiState.Error(
                    exception.message
                        ?: "Unable to load weekly summary."
                )
            }
        }
    }
}