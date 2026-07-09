// Team5
// Author: 罗钰翔

package com.example.wellnessapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.UserResponse
import com.example.wellnessapp.data.model.WellnessLogResponse
import com.example.wellnessapp.data.repository.UserRepository
import com.example.wellnessapp.data.repository.WellnessRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Loads the current user and today's wellness log for the home screen.
 *
 * Author: Member F
 */
class HomeViewModel(
    private val userRepository: UserRepository,
    private val wellnessRepository: WellnessRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _uiState = MutableLiveData<HomeUiState>()
    val uiState: LiveData<HomeUiState> = _uiState

    fun loadHome(today: String) {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            try {
                val userResponse = userRepository.getCurrentUser()
                if (!userResponse.success || userResponse.data == null) {
                    _uiState.value = HomeUiState.Error(userResponse.message.ifBlank { "Unable to load user profile." })
                    return@launch
                }

                val todayLog = runCatching {
                    val logResponse = wellnessRepository.getWellnessLogByDate(today)
                    if (logResponse.success) logResponse.data else null
                }.getOrNull()

                val recentLogs = runCatching {
                    wellnessRepository.getWellnessLogs(
                        startDate = daysBefore(today, 7),
                        endDate = daysBefore(today, 1)
                    ).data.orEmpty().sortedByDescending { it.logDate }
                }.getOrDefault(emptyList())

                _uiState.value = HomeUiState.Success(
                    user = userResponse.data,
                    today = today,
                    todayLog = todayLog,
                    recentLogs = recentLogs
                )
            } catch (error: Exception) {
                _uiState.value = HomeUiState.Error(error.message ?: "Unable to load home data.")
            }
        }
    }

    private fun daysBefore(date: String, days: Int): String {
        val calendar = Calendar.getInstance()
        runCatching { dateFormat.parse(date) }.getOrNull()?.let {
            calendar.time = it
        }
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }

    /**
     * Author: Member F
     */
    class Factory(
        private val userRepository: UserRepository,
        private val wellnessRepository: WellnessRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(userRepository, wellnessRepository) as T
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val user: UserResponse,
        val today: String,
        val todayLog: WellnessLogResponse?,
        val recentLogs: List<WellnessLogResponse>
    ) : HomeUiState()

    data class Error(val message: String) : HomeUiState()
}
