// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.ChatConversationResponse
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.repository.AiRepository
import com.example.wellnessapp.util.ErrorMessageMapper
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Manages chatbot messages, conversation state and API requests.
 *
 */
class ChatbotViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AiRepository(application.applicationContext)

    private val welcomeMessage =
        ChatMessageResponse(
            role = "ASSISTANT",
            content = "Hi! Ask me about sleep, exercise, hydration, mood, or daily wellness habits."
        )

    private val _messages =
        MutableLiveData<List<ChatMessageResponse>>(
            listOf(welcomeMessage)
        )

    val messages: LiveData<List<ChatMessageResponse>>
        get() = _messages

    private val _isLoading =
        MutableLiveData(false)

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _errorMessage =
        MutableLiveData<String?>(null)

    val errorMessage: LiveData<String?>
        get() = _errorMessage

    private val _conversationHistoryState =
        MutableLiveData(PagedHistoryUiState<ChatConversationResponse>())

    val conversationHistoryState: LiveData<PagedHistoryUiState<ChatConversationResponse>>
        get() = _conversationHistoryState

    private var conversationId: String? = null

    fun sendMessage(rawInput: String) {
        val message = rawInput.trim()

        if (message.isBlank()) {
            _errorMessage.value =
                "Please enter a message."
            return
        }

        if (message.length > MAX_MESSAGE_LENGTH) {
            _errorMessage.value =
                "The message must not exceed " +
                        "$MAX_MESSAGE_LENGTH characters."
            return
        }

        if (_isLoading.value == true) {
            return
        }

        appendMessage(
            ChatMessageResponse(
                role = "USER",
                content = message
            )
        )

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response =
                    repository.sendChatMessage(
                        ChatRequest(
                            conversationId = conversationId,
                            message = message
                        )
                    )

                val data = response.data

                when {
                    !response.success -> {
                        _errorMessage.value =
                            response.message
                    }

                    data == null -> {
                        _errorMessage.value =
                            "The chatbot returned no response."
                    }

                    else -> {
                        conversationId =
                            data.conversationId

                        if (data.messages.isNotEmpty()) {
                            _messages.value =
                                data.messages
                        } else {
                            appendMessage(
                                ChatMessageResponse(
                                    role = "ASSISTANT",
                                    content = data.reply,
                                    modelName = data.modelName,
                                    createdAt = data.createdAt
                                )
                            )
                        }

                        loadConversationHistory(reset = true)
                    }
                }
            } catch (exception: HttpException) {
                _errorMessage.value =
                    when (exception.code()) {
                        400 ->
                            "Please check your message."

                        401 ->
                            "Your login session has expired."

                        503 ->
                            "The AI service is currently unavailable."

                        else ->
                            "Request failed " +
                                    "(${exception.code()})."
                    }
            } catch (exception: IOException) {
                _errorMessage.value =
                    "Unable to connect to the server."
            } catch (exception: Exception) {
                _errorMessage.value =
                    exception.message
                        ?: "Unable to send the message."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadConversationHistory(reset: Boolean = true) {
        val currentState =
            _conversationHistoryState.value.orEmpty()

        if (currentState.isLoading || currentState.isLoadingMore) {
            return
        }

        if (!reset && !currentState.hasMore) {
            return
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

        _conversationHistoryState.value =
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
                    repository.getChatConversations(
                        page = targetPage,
                        size = CONVERSATION_PAGE_SIZE
                    )

                if (!response.success) {
                    _conversationHistoryState.value =
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
                                    "Unable to load chat history."
                                }
                        )
                    return@launch
                }

                val pageResponse = response.data

                if (pageResponse == null) {
                    _conversationHistoryState.value =
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
                                "Unable to load chat history."
                        )
                    return@launch
                }

                val page = pageResponse.page
                val items = baseItems + pageResponse.content

                _conversationHistoryState.value =
                    PagedHistoryUiState(
                        items = items,
                        page = page.number,
                        totalPages = page.totalPages,
                        hasMore = page.number + 1 < page.totalPages
                    )
            } catch (exception: Exception) {
                _conversationHistoryState.value =
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
                            mapChatHistoryError(
                                exception,
                                "Unable to load chat history."
                            )
                    )
            }
        }
    }

    fun loadConversationMessages(
        conversation: ChatConversationResponse
    ) {
        if (conversation.conversationId.isBlank()) {
            _errorMessage.value =
                "Unable to open this conversation."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response =
                    repository.getChatConversationMessages(
                        conversationId = conversation.conversationId,
                        page = 0,
                        size = MESSAGE_PAGE_SIZE,
                        sort = MESSAGE_HISTORY_SORT
                    )

                val pageResponse = response.data

                when {
                    !response.success ->
                        _errorMessage.value =
                            response.message.ifBlank {
                                "Unable to open this conversation."
                            }

                    pageResponse == null ->
                        _errorMessage.value =
                            "Unable to open this conversation."

                    else -> {
                        conversationId = conversation.conversationId
                        _messages.value = pageResponse.content
                    }
                }
            } catch (exception: Exception) {
                _errorMessage.value =
                    mapChatHistoryError(
                        exception,
                        "Unable to open this conversation."
                    )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startNewChat() {
        conversationId = null
        _errorMessage.value = null
        _messages.value = listOf(welcomeMessage)
    }

    private fun appendMessage(
        message: ChatMessageResponse
    ) {
        _messages.value =
            _messages.value.orEmpty() + message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun mapChatHistoryError(
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

    private fun PagedHistoryUiState<ChatConversationResponse>?.orEmpty(): PagedHistoryUiState<ChatConversationResponse> {
        return this ?: PagedHistoryUiState()
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 2000
        const val CONVERSATION_PAGE_SIZE = 20
        const val MESSAGE_PAGE_SIZE = 100
        const val MESSAGE_HISTORY_SORT = "createdAt,asc"
    }
}
