package com.example.wellnessapp.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.wellnessapp.data.model.ChatMessageResponse
import com.example.wellnessapp.data.model.ChatRequest
import com.example.wellnessapp.data.repository.AiRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Manages chatbot messages, conversation state and API requests.
 *
 * @author Yunke Deng
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

    private fun appendMessage(
        message: ChatMessageResponse
    ) {
        _messages.value =
            _messages.value.orEmpty() + message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 2000
    }
}