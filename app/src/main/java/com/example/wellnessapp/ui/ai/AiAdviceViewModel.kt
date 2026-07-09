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
import com.example.wellnessapp.util.ErrorMessageMapper
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

    private val _historyState =
        MutableLiveData(PagedHistoryUiState<AiAdviceResponse>())

    val historyState: LiveData<PagedHistoryUiState<AiAdviceResponse>>
        get() = _historyState

    private var historyStartDate: String? = null
    private var historyEndDate: String? = null

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
        endDate: String,
        refreshHistoryOnSuccess: Boolean = false
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
            emptyWhenNotFound = false,
            refreshHistoryOnSuccess = refreshHistoryOnSuccess
        ) {
            repository.generateAiAdvice(
                AiAdviceRequest(
                    startDate = startDate,
                    endDate = endDate
                )
            )
        }
    }

    fun loadAdviceHistory(
        startDate: String? = historyStartDate,
        endDate: String? = historyEndDate,
        reset: Boolean = true
    ) {
        val cleanStartDate =
            startDate?.trim()?.takeIf { it.isNotBlank() }
        val cleanEndDate =
            endDate?.trim()?.takeIf { it.isNotBlank() }

        if (
            cleanStartDate != null &&
            cleanEndDate != null &&
            cleanStartDate > cleanEndDate
        ) {
            _historyState.value =
                _historyState.value.orEmpty().copy(
                    isLoading = false,
                    isLoadingMore = false,
                    hasMore = false,
                    errorMessage = "Start date cannot be later than end date."
                )
            return
        }

        val currentState = _historyState.value.orEmpty()

        if (currentState.isLoading || currentState.isLoadingMore) {
            return
        }

        if (!reset && !currentState.hasMore) {
            return
        }

        if (reset) {
            historyStartDate = cleanStartDate
            historyEndDate = cleanEndDate
        }

        val targetPage =
            if (reset) {
                0
            } else {
                currentState.page + 1
            }

        val baseItems =
            if (reset) {
                emptyList()
            } else {
                currentState.items
            }

        _historyState.value =
            if (reset) {
                PagedHistoryUiState(isLoading = true)
            } else {
                currentState.copy(
                    isLoadingMore = true,
                    errorMessage = null
                )
            }

        viewModelScope.launch {
            try {
                val response =
                    repository.getAiAdviceHistory(
                        startDate = historyStartDate,
                        endDate = historyEndDate,
                        page = targetPage,
                        size = HISTORY_PAGE_SIZE,
                        sort = ADVICE_HISTORY_SORT
                    )

                if (!response.success) {
                    _historyState.value =
                        currentState.copy(
                            items = baseItems,
                            isLoading = false,
                            isLoadingMore = false,
                            page =
                                if (reset) {
                                    -1
                                } else {
                                    currentState.page
                                },
                            totalPages =
                                if (reset) {
                                    0
                                } else {
                                    currentState.totalPages
                                },
                            hasMore = !reset && currentState.hasMore,
                            errorMessage =
                                response.message.ifBlank {
                                    "Unable to load AI advice history."
                                }
                        )
                    return@launch
                }

                val pageResponse = response.data

                if (pageResponse == null) {
                    _historyState.value =
                        currentState.copy(
                            items = baseItems,
                            isLoading = false,
                            isLoadingMore = false,
                            page =
                                if (reset) {
                                    -1
                                } else {
                                    currentState.page
                                },
                            totalPages =
                                if (reset) {
                                    0
                                } else {
                                    currentState.totalPages
                                },
                            hasMore = !reset && currentState.hasMore,
                            errorMessage =
                                "Unable to load AI advice history."
                        )
                    return@launch
                }

                val page = pageResponse.page
                val items = baseItems + pageResponse.content

                _historyState.value =
                    PagedHistoryUiState(
                        items = items,
                        page = page.number,
                        totalPages = page.totalPages,
                        hasMore = page.number + 1 < page.totalPages
                    )
            } catch (exception: Exception) {
                _historyState.value =
                    currentState.copy(
                        items = baseItems,
                        isLoading = false,
                        isLoadingMore = false,
                        page =
                            if (reset) {
                                -1
                            } else {
                                currentState.page
                            },
                        totalPages =
                            if (reset) {
                                0
                            } else {
                                currentState.totalPages
                            },
                        hasMore = !reset && currentState.hasMore,
                        errorMessage =
                            mapHistoryError(
                                exception,
                                "Unable to load AI advice history."
                            )
                    )
            }
        }
    }

    fun loadAdviceDetail(advice: AiAdviceResponse) {
        val adviceId = advice.id

        if (adviceId == null) {
            _uiState.value = UiState.Success(advice)
            return
        }

        clearHistoryError()

        viewModelScope.launch {
            try {
                val response = repository.getAiAdviceDetail(adviceId)
                val data = response.data

                when {
                    !response.success ->
                        setHistoryError(
                            response.message.ifBlank {
                                "AI advice not found."
                            }
                        )

                    data == null ->
                        setHistoryError("AI advice not found.")

                    else ->
                        _uiState.value = UiState.Success(data)
                }
            } catch (exception: Exception) {
                setHistoryError(
                    mapHistoryError(
                        exception,
                        "AI advice not found."
                    )
                )
            }
        }
    }

    private fun executeRequest(
        emptyWhenNotFound: Boolean,
        refreshHistoryOnSuccess: Boolean = false,
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

                    else -> {
                        UiState.Success(data)
                            .also {
                                if (refreshHistoryOnSuccess) {
                                    loadAdviceHistory(reset = true)
                                }
                            }
                    }
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

    private fun clearHistoryError() {
        _historyState.value =
            _historyState.value.orEmpty().copy(
                errorMessage = null
            )
    }

    private fun setHistoryError(message: String) {
        _historyState.value =
            _historyState.value.orEmpty().copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = message
            )
    }

    private fun mapHistoryError(
        error: Throwable,
        fallback: String
    ): String {
        if (error is HttpException) {
            val mapped =
                ErrorMessageMapper.fromThrowable(error, fallback)

            return when (error.code()) {
                401 -> "Your login session has expired."
                404 ->
                    if (mapped.contains("Authentication service")) {
                        fallback
                    } else {
                        mapped
                    }

                else -> mapped
            }
        }

        return ErrorMessageMapper.fromThrowable(error, fallback)
    }

    private fun PagedHistoryUiState<AiAdviceResponse>?.orEmpty(): PagedHistoryUiState<AiAdviceResponse> {
        return this ?: PagedHistoryUiState()
    }

    private companion object {
        const val HISTORY_PAGE_SIZE = 20
        const val ADVICE_HISTORY_SORT = "createdAt,desc"
    }
}
