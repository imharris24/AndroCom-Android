package com.application.androcom
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView




class ChatAdapter:RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private

    val messages = mutableListOf<Message>()

    fun

            addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = if (viewType == 1) {
            layoutInflater.inflate(R.layout.send_container, parent, false)
        } else {
            layoutInflater.inflate(R.layout.recieved_container, parent, false)
        }
        return MessageViewHolder(view)
    }

    override

    fun

            onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.text
        if (message.isSent) {
            // Sent message formatting
            holder.messageTextView.setTextColor(Color.WHITE)

        } else {
            // Received message formatting
            holder.messageTextView.setTextColor(Color.BLACK)
            holder.messageTextView.setTypeface(null, Typeface.NORMAL)
        }

    }

    override

    fun

            getItemCount(): Int = messages.size

    override

    fun

            getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.isSent) 1 else 0 // 1 for sent, 0 for received
    }

    inner class

    MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.textmessage)
    }
}

