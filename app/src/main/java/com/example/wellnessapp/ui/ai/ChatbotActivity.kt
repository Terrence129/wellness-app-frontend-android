package com.example.wellnessapp.ui.ai

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R

/**
 * Displays the wellness chatbot conversation.
 *
 * @author Yunke Deng
 */
class ChatbotActivity : AppCompatActivity() {

    private val viewModel: ChatbotViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

    private val adapter =
        ChatMessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        bindViews()
        configureRecyclerView()
        bindActions()
        observeViewModel()
    }

    private fun bindViews() {
        recyclerView =
            findViewById(R.id.rvChatMessages)

        messageInput =
            findViewById(R.id.etChatMessage)

        sendButton =
            findViewById(R.id.btnSendMessage)

        progressBar =
            findViewById(R.id.chatProgressBar)

        errorText =
            findViewById(R.id.tvChatError)
    }

    private fun configureRecyclerView() {
        recyclerView.layoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = true
            }

        recyclerView.adapter = adapter
    }

    private fun bindActions() {
        findViewById<Button>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        sendButton.setOnClickListener {
            submitMessage()
        }

        messageInput.setOnEditorActionListener {
                _,
                actionId,
                _ ->

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitMessage()
                true
            } else {
                false
            }
        }

        messageInput.setOnFocusChangeListener {
                _,
                hasFocus ->

            if (hasFocus) {
                viewModel.clearError()
            }
        }
    }

    private fun submitMessage() {
        val message =
            messageInput.text.toString()

        if (message.isNotBlank()) {
            messageInput.text.clear()
        }

        viewModel.sendMessage(message)
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) {
                messages ->

            adapter.submitMessages(messages)

            if (messages.isNotEmpty()) {
                recyclerView.post {
                    recyclerView.scrollToPosition(
                        messages.lastIndex
                    )
                }
            }
        }

        viewModel.isLoading.observe(this) {
                isLoading ->

            progressBar.visibility =
                if (isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            sendButton.isEnabled = !isLoading
            messageInput.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) {
                message ->

            if (message.isNullOrBlank()) {
                errorText.visibility = View.GONE
                errorText.text = ""
            } else {
                errorText.visibility = View.VISIBLE
                errorText.text = message
            }
        }
    }
}