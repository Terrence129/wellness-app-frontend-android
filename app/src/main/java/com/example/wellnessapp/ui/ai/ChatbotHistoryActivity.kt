// Team5
// @author: Deng Yunke

package com.example.wellnessapp.ui.ai

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R

class ChatbotHistoryActivity : AppCompatActivity() {

    private val viewModel: ChatbotViewModel by viewModels()
    private val conversationAdapter = ChatConversationAdapter { conversation ->
        selectedConversationTitle.visibility = View.VISIBLE
        messageList.visibility = View.VISIBLE
        messageAdapter.submitMessages(emptyList())
        viewModel.loadConversationMessages(conversation)
    }
    private val messageAdapter = ChatMessageAdapter()

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var conversationList: RecyclerView
    private lateinit var loadMoreButton: Button
    private lateinit var selectedConversationTitle: TextView
    private lateinit var messageList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot_history)

        bindViews()
        bindActions()
        configureLists()
        observeViewModel()
        viewModel.loadConversationHistory()
    }

    private fun bindViews() {
        progressBar = findViewById(R.id.progressChatHistory)
        statusText = findViewById(R.id.tvChatHistoryStatus)
        conversationList = findViewById(R.id.rvChatConversations)
        loadMoreButton = findViewById(R.id.btnLoadMoreChatHistory)
        selectedConversationTitle = findViewById(R.id.tvSelectedConversationTitle)
        messageList = findViewById(R.id.rvChatMessages)
    }

    private fun bindActions() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        loadMoreButton.setOnClickListener {
            viewModel.loadConversationHistory(reset = false)
        }
    }

    private fun configureLists() {
        conversationList.layoutManager = LinearLayoutManager(this)
        conversationList.adapter = conversationAdapter
        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = messageAdapter
    }

    private fun observeViewModel() {
        viewModel.conversationHistoryState.observe(this) { state ->
            conversationAdapter.submitItems(state.items)
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            when {
                !state.errorMessage.isNullOrBlank() -> {
                    statusText.visibility = View.VISIBLE
                    statusText.text = state.errorMessage
                    statusText.setTextColor(getColor(R.color.health_error))
                }

                state.isEmpty -> {
                    statusText.visibility = View.VISIBLE
                    statusText.text = "No saved chat history found."
                    statusText.setTextColor(getColor(R.color.health_text_secondary))
                }

                else -> {
                    statusText.visibility = View.GONE
                    statusText.text = ""
                    statusText.setTextColor(getColor(R.color.health_text_secondary))
                }
            }

            loadMoreButton.visibility =
                if (state.hasMore || state.isLoadingMore) View.VISIBLE else View.GONE
            loadMoreButton.isEnabled = !state.isLoadingMore
            loadMoreButton.text = if (state.isLoadingMore) "Loading..." else "Load More"
        }

        viewModel.messages.observe(this) { messages ->
            if (messageList.visibility == View.VISIBLE) {
                messageAdapter.submitMessages(messages)
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                statusText.visibility = View.VISIBLE
                statusText.text = message
                statusText.setTextColor(getColor(R.color.health_error))
            }
        }
    }
}
