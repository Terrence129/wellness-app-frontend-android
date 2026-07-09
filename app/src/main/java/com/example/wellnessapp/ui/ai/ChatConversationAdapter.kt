package com.example.wellnessapp.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessapp.R
import com.example.wellnessapp.data.model.ChatConversationResponse

class ChatConversationAdapter(
    private val onItemClick: (ChatConversationResponse) -> Unit
) : RecyclerView.Adapter<ChatConversationAdapter.ConversationViewHolder>() {

    private val items =
        mutableListOf<ChatConversationResponse>()

    fun submitItems(newItems: List<ChatConversationResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConversationViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_chat_conversation,
                    parent,
                    false
                )

        return ConversationViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(
        holder: ConversationViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int =
        items.size

    class ConversationViewHolder(
        itemView: View,
        private val onItemClick: (ChatConversationResponse) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val timeText: TextView =
            itemView.findViewById(R.id.tvConversationTime)

        private val metaText: TextView =
            itemView.findViewById(R.id.tvConversationMeta)

        private val previewText: TextView =
            itemView.findViewById(R.id.tvConversationPreview)

        fun bind(conversation: ChatConversationResponse) {
            timeText.text =
                AiTimeFormatter.formatDateTime(
                    conversation.lastMessageAt
                        ?: conversation.startedAt
                )
                    ?: "Unknown time"

            val role = conversation.lastRole ?: "--"
            val messageLabel =
                if (conversation.messageCount == 1) {
                    "message"
                } else {
                    "messages"
                }

            metaText.text =
                "${conversation.messageCount} $messageLabel | Last role: $role"

            previewText.text =
                preview(
                    conversation.lastMessagePreview
                        ?.takeIf { it.isNotBlank() }
                        ?: "No message preview is available."
                )

            itemView.setOnClickListener {
                onItemClick(conversation)
            }
        }

        private fun preview(value: String): String {
            val collapsed =
                value.replace(Regex("\\s+"), " ").trim()

            return if (collapsed.length > MAX_PREVIEW_LENGTH) {
                collapsed.take(MAX_PREVIEW_LENGTH - 3) + "..."
            } else {
                collapsed
            }
        }
    }

    private companion object {
        const val MAX_PREVIEW_LENGTH = 120
    }
}
