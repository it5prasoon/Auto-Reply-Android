package com.matrix.autoreply.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.matrix.autoreply.R
import com.matrix.autoreply.model.Badge

class BadgeAdapter(
    private val allBadges: List<Badge>,
    private val earnedBadgeIds: Set<String>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = allBadges[position]
        val isEarned = earnedBadgeIds.contains(badge.id)
        holder.bind(badge, isEarned)
    }

    override fun getItemCount() = allBadges.size

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val emojiTextView: TextView = itemView.findViewById(R.id.badgeEmoji)
        private val titleTextView: TextView = itemView.findViewById(R.id.badgeTitle)
        private val thresholdTextView: TextView = itemView.findViewById(R.id.badgeThreshold)

        fun bind(badge: Badge, isEarned: Boolean) {
            if (isEarned) {
                // Earned badge - show in full color
                emojiTextView.text = badge.emoji
                emojiTextView.alpha = 1.0f
                titleTextView.text = badge.title
                titleTextView.alpha = 1.0f
                thresholdTextView.text = badge.threshold.toString()
                thresholdTextView.alpha = 1.0f
            } else {
                // Locked badge - show greyed out
                emojiTextView.text = "🔒"
                emojiTextView.alpha = 0.3f
                titleTextView.text = badge.title
                titleTextView.alpha = 0.3f
                thresholdTextView.text = badge.threshold.toString()
                thresholdTextView.alpha = 0.3f
            }
        }
    }
}
