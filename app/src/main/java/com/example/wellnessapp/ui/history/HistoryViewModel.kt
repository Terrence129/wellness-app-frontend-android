// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.WellnessRepository
import kotlinx.coroutines.launch

/**
 * Loads wellness history records for the History screen.
 *
 * Author: Member F
 */
class HistoryViewModel(
    private val wellnessRepository: WellnessRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HistoryUiState>()
    val uiState: LiveData<HistoryUiState> = _uiState

    fun loadLogs(startDate: String?, endDate: String?) {
        _uiState.value = HistoryUiState.Loading

        viewModelScope.launch {
            try {
                val response = wellnessRepository.getWellnessLogs(
                    startDate = startDate?.takeIf { it.isNotBlank() },
                    endDate = endDate?.takeIf { it.isNotBlank() }
                )

                if (!response.success) {
                    _uiState.value = HistoryUiState.Error(response.message.ifBlank { "Unable to load wellness history." })
                    return@launch
                }

                val logs = response.data.orEmpty().sortedByDescending { it.logDate }
                _uiState.value = if (logs.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    HistoryUiState.Success(logs)
                }
            } catch (error: Exception) {
                _uiState.value = HistoryUiState.Error(error.message ?: "Unable to load wellness history.")
            }
        }
    }

    /**
     * Author: Member F
     */
    class Factory(
        private val wellnessRepository: WellnessRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(wellnessRepository) as T
        }
    }
}

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    object Empty : HistoryUiState()
    data class Success(val logs: List<WellnessLogResponse>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
