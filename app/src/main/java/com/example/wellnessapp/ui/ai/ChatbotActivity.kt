package com.example.wellnessapp.ui.ai

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        findViewById<View>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        updateSendButtonTint()

        messageInput.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    updateSendButtonTint()
                }

                override fun afterTextChanged(s: Editable?) = Unit
            }
        )

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

    private fun updateSendButtonTint() {
        val color =
            if (messageInput.text.isNullOrBlank()) {
                R.color.classic_gray
            } else {
                R.color.send_active_green
            }

        sendButton.backgroundTintList =
            ColorStateList.valueOf(getColor(color))
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
