package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.databinding.MessageItemBinding
import java.text.SimpleDateFormat
import java.util.*

class ContactMessageAdapter :
    ListAdapter<Pair<String?, Long>, ContactMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(private val binding: MessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Pair<String?, Long>) {
            binding.messageTextView.text = message.first
            binding.timestamp.text = convertTimestampToTime(message.second)
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Pair<String?, Long>>() {
        override fun areItemsTheSame(oldItem: Pair<String?, Long>, newItem: Pair<String?, Long>): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Pair<String?, Long>, newItem: Pair<String?, Long>): Boolean {
            return oldItem == newItem
        }
    }


    private fun convertTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(date)
    }
}
