package com.matrix.autoreply.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matrix.autoreply.R
import com.matrix.autoreply.ui.activity.AiSettingsActivity
import com.matrix.autoreply.utils.FeatureAnnouncementManager

/**
 * Dynamic "What's New" dialog that reads from JSON configuration
 * Shows new features after app updates
 */
class WhatsNewDialog(private val context: Context) {
    
    fun show(announcement: FeatureAnnouncementManager.FeatureAnnouncement) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_whats_new, null)
        
        // Set title and subtitle from JSON
        dialogView.findViewById<TextView>(R.id.whatsNewTitle).text = announcement.title
        dialogView.findViewById<TextView>(R.id.whatsNewSubtitle).text = announcement.subtitle
        
        // Build features list dynamically from JSON
        val featuresContainer = dialogView.findViewById<LinearLayout>(R.id.featuresContainer)
        buildFeaturesFromJson(featuresContainer, announcement.features)
        
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Set dialog size to ensure proper 75/25 split
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            (context.resources.displayMetrics.heightPixels * 0.8).toInt() // 80% of screen height
        )
        
        // Handle action buttons
        dialogView.findViewById<View>(R.id.closeButton).setOnClickListener {
            // Quick dismiss via close button - will show again next time
            dialog.dismiss()
        }
        
        dialogView.findViewById<MaterialButton>(R.id.gotItButton).setOnClickListener {
            // Just dismiss - will show again next time until "Don't show again" is clicked
            dialog.dismiss()
        }
        
        dialogView.findViewById<MaterialButton>(R.id.exploreAiSettingsButton).setOnClickListener {
            dialog.dismiss()
            
            // Navigate to AI Settings
            val intent = Intent(context, AiSettingsActivity::class.java)
            context.startActivity(intent)
        }
        
        dialogView.findViewById<MaterialButton>(R.id.dontShowAgainButton).setOnClickListener {
            // Permanently disable feature announcements
            FeatureAnnouncementManager.markDontShowAgain(context)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    /**
     * Dynamically build features list from JSON configuration
     */
    private fun buildFeaturesFromJson(
        container: LinearLayout, 
        features: List<FeatureAnnouncementManager.Feature>
    ) {
        container.removeAllViews()
        
        features.forEach { feature ->
            val featureView = LayoutInflater.from(context).inflate(
                R.layout.item_whats_new_feature, container, false
            )
            
            featureView.findViewById<TextView>(R.id.featureIcon).text = feature.icon
            featureView.findViewById<TextView>(R.id.featureTitle).text = feature.title
            featureView.findViewById<TextView>(R.id.featureDescription).text = feature.description
            
            // Handle action button if specified in JSON
            val actionButton = featureView.findViewById<MaterialButton>(R.id.featureActionButton)
            if (!feature.actionText.isNullOrEmpty() && !feature.actionTarget.isNullOrEmpty()) {
                actionButton.text = feature.actionText
                actionButton.visibility = View.VISIBLE
                actionButton.setOnClickListener {
                    handleFeatureAction(feature.actionTarget)
                }
            } else {
                actionButton.visibility = View.GONE
            }
            
            container.addView(featureView)
        }
    }
    
    /**
     * Handle feature-specific actions from JSON configuration
     */
    private fun handleFeatureAction(actionTarget: String) {
        when (actionTarget) {
            "ai_settings" -> {
                val intent = Intent(context, AiSettingsActivity::class.java)
                context.startActivity(intent)
            }
            "main_schedule" -> {
                // Schedule controls are already visible in the main UI
                // Just dismiss dialog to let user see the schedule section
            }
            // Add more action targets as needed in future JSON configs
            else -> {
                // Unknown action target - could log for debugging
            }
        }
    }
}
