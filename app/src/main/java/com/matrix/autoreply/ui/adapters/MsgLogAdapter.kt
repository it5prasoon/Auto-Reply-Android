package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.databinding.MsgLogItemBinding

class MsgLogAdapter(private val msgList: List<String>) : RecyclerView.Adapter<MsgLogAdapter.MsgLogViewHolder>() {

    class MsgLogViewHolder(binding: MsgLogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val msgTextView: TextView = binding.msgTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MsgLogItemBinding.inflate(inflater, parent, false)
        return MsgLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MsgLogViewHolder, position: Int) {
        val msg = msgList[position]
        holder.msgTextView.text = msg
    }

    override fun getItemCount(): Int = msgList.size
}