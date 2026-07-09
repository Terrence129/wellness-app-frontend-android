package com.example.wellnessapp.ui.ai

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.AiAdviceResponse
import com.example.wellnessapp.util.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Combines generated wellness advice and chatbot support in one AI entry.
 */
class AiCoachActivity : AppCompatActivity() {

    private val adviceViewModel: AiAdviceViewModel by viewModels()
    private val chatViewModel: ChatbotViewModel by viewModels()

    private lateinit var adviceModeButton: Button
    private lateinit var chatModeButton: Button
    private lateinit var advicePanel: ScrollView
    private lateinit var chatPanel: LinearLayout
    private lateinit var requestedRangeText: TextView
    private lateinit var adviceProgressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var adviceContent: LinearLayout
    private lateinit var generateAdviceButton: Button
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatProgressBar: ProgressBar
    private lateinit var chatErrorText: TextView

    private val chatAdapter = ChatMessageAdapter()
    private var startDate = ""
    private var endDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_coach)

        bindViews()
        prepareDefaultDateRange()
        configureChatRecyclerView()
        bindActions()
        observeAdviceState()
        observeChatState()

        adviceViewModel.loadLatestAdvice()
    }

    private fun bindViews() {
        adviceModeButton = findViewById(R.id.btnModeAdvice)
        chatModeButton = findViewById(R.id.btnModeChat)
        advicePanel = findViewById(R.id.advicePanel)
        chatPanel = findViewById(R.id.chatPanel)
        requestedRangeText = findViewById(R.id.tvRequestedRange)
        adviceProgressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.tvStatus)
        adviceContent = findViewById(R.id.adviceContent)
        generateAdviceButton = findViewById(R.id.btnGenerateAdvice)
        chatRecyclerView = findViewById(R.id.rvChatMessages)
        messageInput = findViewById(R.id.etChatMessage)
        sendButton = findViewById(R.id.btnSendMessage)
        chatProgressBar = findViewById(R.id.chatProgressBar)
        chatErrorText = findViewById(R.id.tvChatError)
    }

    private fun prepareDefaultDateRange() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val endCalendar = Calendar.getInstance()
        val startCalendar = endCalendar.clone() as Calendar

        startCalendar.add(Calendar.DAY_OF_YEAR, -6)

        startDate = formatter.format(startCalendar.time)
        endDate = formatter.format(endCalendar.time)
        requestedRangeText.text = "$startDate to $endDate"
    }

    private fun configureChatRecyclerView() {
        chatRecyclerView.layoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = true
            }

        chatRecyclerView.adapter = chatAdapter
    }

    private fun bindActions() {
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        adviceModeButton.setOnClickListener {
            showAdviceMode()
        }

        chatModeButton.setOnClickListener {
            showChatMode()
        }

        generateAdviceButton.setOnClickListener {
            adviceViewModel.generateAdvice(
                startDate = startDate,
                endDate = endDate
            )
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

        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitMessage()
                true
            } else {
                false
            }
        }

        messageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                chatViewModel.clearError()
            }
        }
    }

    private fun showAdviceMode() {
        advicePanel.visibility = View.VISIBLE
        chatPanel.visibility = View.GONE
        adviceModeButton.setBackgroundResource(R.drawable.bg_segment_selected)
        adviceModeButton.setTextColor(getColor(R.color.white))
        chatModeButton.setBackgroundResource(R.drawable.bg_segment_unselected)
        chatModeButton.setTextColor(getColor(R.color.health_text_secondary))
    }

    private fun showChatMode() {
        advicePanel.visibility = View.GONE
        chatPanel.visibility = View.VISIBLE
        adviceModeButton.setBackgroundResource(R.drawable.bg_segment_unselected)
        adviceModeButton.setTextColor(getColor(R.color.health_text_secondary))
        chatModeButton.setBackgroundResource(R.drawable.bg_segment_selected)
        chatModeButton.setTextColor(getColor(R.color.white))
    }

    private fun observeAdviceState() {
        adviceViewModel.uiState.observe(this) { state ->
            when (state) {
                UiState.Idle -> Unit
                UiState.Loading -> showAdviceLoading()
                UiState.Empty -> showAdviceEmpty()
                is UiState.Error -> showAdviceError(state.message)
                is UiState.Success -> showAdvice(state.data)
            }
        }
    }

    private fun showAdviceLoading() {
        adviceProgressBar.visibility = View.VISIBLE
        statusText.visibility = View.GONE
        adviceContent.visibility = View.GONE
        generateAdviceButton.isEnabled = false
    }

    private fun showAdviceEmpty() {
        adviceProgressBar.visibility = View.GONE
        adviceContent.visibility = View.GONE
        statusText.visibility = View.VISIBLE
        generateAdviceButton.isEnabled = true
        statusText.text = "No saved AI advice yet. Generate advice using your recent records."
    }

    private fun showAdviceError(message: String) {
        adviceProgressBar.visibility = View.GONE
        adviceContent.visibility = View.GONE
        statusText.visibility = View.VISIBLE
        generateAdviceButton.isEnabled = true
        statusText.text = message
    }

    private fun showAdvice(data: AiAdviceResponse) {
        adviceProgressBar.visibility = View.GONE
        statusText.visibility = View.GONE
        adviceContent.visibility = View.VISIBLE
        generateAdviceButton.isEnabled = true

        val adviceDate = data.adviceDate ?: "Unknown"
        val returnedStartDate = data.startDate ?: startDate
        val returnedEndDate = data.endDate ?: endDate

        findViewById<TextView>(R.id.tvAdviceDate).text = "Advice date: $adviceDate"
        findViewById<TextView>(R.id.tvAdviceRange).text =
            "Based on records from $returnedStartDate to $returnedEndDate"
        findViewById<TextView>(R.id.tvAdviceText).text =
            data.adviceText.ifBlank {
                "No advice text is available."
            }

        displayModelName(data.modelName)
    }

    private fun displayModelName(modelName: String?) {
        val modelNameText = findViewById<TextView>(R.id.tvModelName)

        if (modelName.isNullOrBlank()) {
            modelNameText.visibility = View.GONE
            modelNameText.text = ""
        } else {
            modelNameText.visibility = View.VISIBLE
            modelNameText.text = "Generated by: $modelName"
        }
    }

    private fun observeChatState() {
        chatViewModel.messages.observe(this) { messages ->
            chatAdapter.submitMessages(messages)

            if (messages.isNotEmpty()) {
                chatRecyclerView.post {
                    chatRecyclerView.scrollToPosition(messages.lastIndex)
                }
            }
        }

        chatViewModel.isLoading.observe(this) { isLoading ->
            chatProgressBar.visibility =
                if (isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            sendButton.isEnabled = !isLoading
            messageInput.isEnabled = !isLoading
        }

        chatViewModel.errorMessage.observe(this) { message ->
            if (message.isNullOrBlank()) {
                chatErrorText.visibility = View.GONE
                chatErrorText.text = ""
            } else {
                chatErrorText.visibility = View.VISIBLE
                chatErrorText.text = message
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
        val message = messageInput.text.toString()

        if (message.isNotBlank()) {
            messageInput.text.clear()
        }

        chatViewModel.sendMessage(message)
    }
}
