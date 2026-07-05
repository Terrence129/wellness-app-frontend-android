package com.example.wellnessapp.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.AiAdviceRequest
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.data.model.ApiResponse
import com.example.wellnessapp.data.repository.AiRepository
import com.example.wellnessapp.util.UiState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Manages AI wellness advice requests and UI state.
 *
 * @author Yunke Deng
 */
class AiAdviceViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AiRepository(application.applicationContext)

    private val _uiState =
        MutableLiveData<UiState<AiAdviceResponse>>(UiState.Idle)

    val uiState: LiveData<UiState<AiAdviceResponse>>
        get() = _uiState

    /**
     * Loads the most recently saved AI advice.
     */
    fun loadLatestAdvice() {
        executeRequest(
            emptyWhenNotFound = true
        ) {
            repository.getLatestAiAdvice()
        }
    }

    /**
     * Generates advice using wellness records in the selected range.
     */
    fun generateAdvice(
        startDate: String,
        endDate: String
    ) {
        if (startDate.isBlank() || endDate.isBlank()) {
            _uiState.value =
                UiState.Error("Please select a valid date range.")
            return
        }

        if (startDate > endDate) {
            _uiState.value =
                UiState.Error(
                    "Start date cannot be later than end date."
                )
            return
        }

        executeRequest(
            emptyWhenNotFound = false
        ) {
            repository.generateAiAdvice(
                AiAdviceRequest(
                    startDate = startDate,
                    endDate = endDate
                )
            )
        }
    }

    private fun executeRequest(
        emptyWhenNotFound: Boolean,
        request: suspend () -> ApiResponse<AiAdviceResponse>
    ) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val response = request()
                val data = response.data

                _uiState.value = when {
                    !response.success ->
                        UiState.Error(response.message)

                    data == null ->
                        UiState.Empty

                    else ->
                        UiState.Success(data)
                }
            } catch (exception: HttpException) {
                _uiState.value = when {
                    emptyWhenNotFound &&
                            exception.code() == 404 ->
                        UiState.Empty

                    exception.code() == 400 ->
                        UiState.Error(
                            "No wellness records were found " +
                                    "for the selected date range."
                        )

                    exception.code() == 401 ->
                        UiState.Error(
                            "Your login session has expired."
                        )

                    exception.code() == 503 ->
                        UiState.Error(
                            "The AI service is currently unavailable."
                        )

                    else ->
                        UiState.Error(
                            "Request failed (${exception.code()})."
                        )
                }
            } catch (exception: IOException) {
                _uiState.value =
                    UiState.Error(
                        "Unable to connect to the server."
                    )
            } catch (exception: Exception) {
                _uiState.value =
                    UiState.Error(
                        exception.message
                            ?: "Unable to load AI advice."
                    )
            }
        }
    }
}