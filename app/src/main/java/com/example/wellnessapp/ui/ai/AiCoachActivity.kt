package com.example.wellnessapp.ui.ai

import android.app.DatePickerDialog
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
    private lateinit var adviceHistoryStartInput: EditText
    private lateinit var adviceHistoryEndInput: EditText
    private lateinit var adviceHistoryProgressBar: ProgressBar
    private lateinit var adviceHistoryStatusText: TextView
    private lateinit var adviceHistoryRecyclerView: RecyclerView
    private lateinit var loadMoreAdviceHistoryButton: Button
    private lateinit var newChatButton: Button
    private lateinit var toggleChatHistoryButton: Button
    private lateinit var chatHistoryContainer: LinearLayout
    private lateinit var chatConversationsRecyclerView: RecyclerView
    private lateinit var chatHistoryProgressBar: ProgressBar
    private lateinit var chatHistoryStatusText: TextView
    private lateinit var loadMoreChatConversationsButton: Button
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatProgressBar: ProgressBar
    private lateinit var chatErrorText: TextView

    private val chatAdapter = ChatMessageAdapter()
    private val adviceHistoryAdapter =
        AiAdviceHistoryAdapter { advice ->
            adviceViewModel.loadAdviceDetail(advice)
        }
    private val chatConversationAdapter =
        ChatConversationAdapter { conversation ->
            chatViewModel.loadConversationMessages(conversation)
            chatHistoryContainer.visibility = View.GONE
        }
    private var startDate = ""
    private var endDate = ""
    private val historyDateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_coach)

        bindViews()
        prepareDefaultDateRange()
        configureAdviceHistoryRecyclerView()
        configureChatRecyclerView()
        configureChatHistoryRecyclerView()
        bindActions()
        observeAdviceState()
        observeAdviceHistoryState()
        observeChatState()
        observeChatHistoryState()

        adviceViewModel.loadLatestAdvice()
        adviceViewModel.loadAdviceHistory()
        chatViewModel.loadConversationHistory()
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
        adviceHistoryStartInput = findViewById(R.id.etAdviceHistoryStartDate)
        adviceHistoryEndInput = findViewById(R.id.etAdviceHistoryEndDate)
        adviceHistoryProgressBar = findViewById(R.id.progressAdviceHistory)
        adviceHistoryStatusText = findViewById(R.id.tvAdviceHistoryStatus)
        adviceHistoryRecyclerView = findViewById(R.id.rvAdviceHistory)
        loadMoreAdviceHistoryButton =
            findViewById(R.id.btnLoadMoreAdviceHistory)
        newChatButton = findViewById(R.id.btnNewChat)
        toggleChatHistoryButton = findViewById(R.id.btnToggleChatHistory)
        chatHistoryContainer = findViewById(R.id.chatHistoryContainer)
        chatConversationsRecyclerView = findViewById(R.id.rvChatConversations)
        chatHistoryProgressBar = findViewById(R.id.progressChatHistory)
        chatHistoryStatusText = findViewById(R.id.tvChatHistoryStatus)
        loadMoreChatConversationsButton =
            findViewById(R.id.btnLoadMoreChatConversations)
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

    private fun showHistoryDatePicker(
        target: EditText,
        minDate: Long? = null,
        onDateSelected: ((Long) -> Unit)? = null
    ) {
        val calendar = Calendar.getInstance()

        parseHistoryDate(target.text.toString())?.let {
            calendar.timeInMillis = it
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val date =
                    String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        dayOfMonth
                    )

                target.setText(date)
                onDateSelected?.invoke(
                    parseHistoryDate(date)
                        ?: return@DatePickerDialog
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate?.let {
                datePicker.minDate = it
            }

            datePicker.maxDate =
                Calendar.getInstance().timeInMillis
        }.show()
    }

    private fun parseHistoryDate(value: String): Long? {
        return runCatching {
            historyDateFormat.parse(value.trim())?.time
        }.getOrNull()
    }

    private fun configureAdviceHistoryRecyclerView() {
        adviceHistoryRecyclerView.layoutManager =
            LinearLayoutManager(this)

        adviceHistoryRecyclerView.adapter =
            adviceHistoryAdapter
    }

    private fun configureChatRecyclerView() {
        chatRecyclerView.layoutManager =
            LinearLayoutManager(this).apply {
                stackFromEnd = true
            }

        chatRecyclerView.adapter = chatAdapter
    }

    private fun configureChatHistoryRecyclerView() {
        chatConversationsRecyclerView.layoutManager =
            LinearLayoutManager(this)

        chatConversationsRecyclerView.adapter =
            chatConversationAdapter
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
                endDate = endDate,
                refreshHistoryOnSuccess = true
            )
        }

        adviceHistoryStartInput.setOnClickListener {
            showHistoryDatePicker(adviceHistoryStartInput) { selectedDate ->
                val selectedEndDate =
                    parseHistoryDate(
                        adviceHistoryEndInput.text.toString()
                    )

                if (
                    selectedEndDate != null &&
                    selectedEndDate < selectedDate
                ) {
                    adviceHistoryEndInput.text.clear()
                }
            }
        }

        adviceHistoryEndInput.setOnClickListener {
            showHistoryDatePicker(
                target = adviceHistoryEndInput,
                minDate =
                    parseHistoryDate(
                        adviceHistoryStartInput.text.toString()
                    )
            )
        }

        findViewById<Button>(R.id.btnApplyAdviceHistoryFilter)
            .setOnClickListener {
                adviceViewModel.loadAdviceHistory(
                    startDate =
                        adviceHistoryStartInput.text.toString(),
                    endDate =
                        adviceHistoryEndInput.text.toString(),
                    reset = true
                )
            }

        findViewById<Button>(R.id.btnClearAdviceHistoryFilter)
            .setOnClickListener {
                adviceHistoryStartInput.text.clear()
                adviceHistoryEndInput.text.clear()
                adviceViewModel.loadAdviceHistory(
                    startDate = null,
                    endDate = null,
                    reset = true
                )
            }

        loadMoreAdviceHistoryButton.setOnClickListener {
            adviceViewModel.loadAdviceHistory(reset = false)
        }

        newChatButton.setOnClickListener {
            messageInput.text.clear()
            chatViewModel.startNewChat()
            chatHistoryContainer.visibility = View.GONE
        }

        toggleChatHistoryButton.setOnClickListener {
            val shouldShow =
                chatHistoryContainer.visibility != View.VISIBLE

            chatHistoryContainer.visibility =
                if (shouldShow) {
                    chatViewModel.loadConversationHistory(reset = true)
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        loadMoreChatConversationsButton.setOnClickListener {
            chatViewModel.loadConversationHistory(reset = false)
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

    private fun observeAdviceHistoryState() {
        adviceViewModel.historyState.observe(this) { state ->
            adviceHistoryAdapter.submitItems(state.items)

            adviceHistoryProgressBar.visibility =
                if (state.isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            when {
                !state.errorMessage.isNullOrBlank() -> {
                    adviceHistoryStatusText.visibility = View.VISIBLE
                    adviceHistoryStatusText.text = state.errorMessage
                    adviceHistoryStatusText.setTextColor(
                        getColor(R.color.health_error)
                    )
                }

                state.isEmpty -> {
                    adviceHistoryStatusText.visibility = View.VISIBLE
                    adviceHistoryStatusText.text =
                        "No saved AI advice found."
                    adviceHistoryStatusText.setTextColor(
                        getColor(R.color.health_text_secondary)
                    )
                }

                else -> {
                    adviceHistoryStatusText.visibility = View.GONE
                    adviceHistoryStatusText.text = ""
                    adviceHistoryStatusText.setTextColor(
                        getColor(R.color.health_text_secondary)
                    )
                }
            }

            loadMoreAdviceHistoryButton.visibility =
                if (state.hasMore || state.isLoadingMore) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            loadMoreAdviceHistoryButton.isEnabled =
                !state.isLoadingMore

            loadMoreAdviceHistoryButton.text =
                if (state.isLoadingMore) {
                    "Loading..."
                } else {
                    "Load More"
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

    private fun observeChatHistoryState() {
        chatViewModel.conversationHistoryState.observe(this) { state ->
            chatConversationAdapter.submitItems(state.items)

            chatHistoryProgressBar.visibility =
                if (state.isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            when {
                !state.errorMessage.isNullOrBlank() -> {
                    chatHistoryStatusText.visibility = View.VISIBLE
                    chatHistoryStatusText.text = state.errorMessage
                    chatHistoryStatusText.setTextColor(
                        getColor(R.color.health_error)
                    )
                }

                state.isEmpty -> {
                    chatHistoryStatusText.visibility = View.VISIBLE
                    chatHistoryStatusText.text =
                        "No saved chat conversations yet."
                    chatHistoryStatusText.setTextColor(
                        getColor(R.color.health_text_secondary)
                    )
                }

                else -> {
                    chatHistoryStatusText.visibility = View.GONE
                    chatHistoryStatusText.text = ""
                    chatHistoryStatusText.setTextColor(
                        getColor(R.color.health_text_secondary)
                    )
                }
            }

            loadMoreChatConversationsButton.visibility =
                if (state.hasMore || state.isLoadingMore) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            loadMoreChatConversationsButton.isEnabled =
                !state.isLoadingMore

            loadMoreChatConversationsButton.text =
                if (state.isLoadingMore) {
                    "Loading..."
                } else {
                    "Load More Conversations"
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
