package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.databinding.ContactNameItemBinding

class ContactNameAdapter(private val onItemClick: (String) -> Unit) :
    ListAdapter<String, ContactNameAdapter.ContactNameViewHolder>(NotificationTitleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactNameViewHolder {
        val binding = ContactNameItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactNameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactNameViewHolder, position: Int) {
        val contactName = getItem(position)
        holder.bind(contactName)
    }

    inner class ContactNameViewHolder(private val binding: ContactNameItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contactName: String) {
            binding.titleTextView.text = contactName
            binding.contactNameCardView.setOnClickListener {
                onItemClick(contactName)
            }
        }
    }

    private class NotificationTitleDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
