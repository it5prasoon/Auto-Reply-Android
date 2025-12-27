package com.matrix.autoreply.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.matrix.autoreply.helpers.NotificationHelper
import com.matrix.autoreply.model.BadgeRegistry
import com.matrix.autoreply.preferences.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsTracker {
    
    private var firebaseAnalytics: FirebaseAnalytics? = null
    
    // Achievement milestones
    private val MILESTONES = listOf(50, 100, 250, 500, 750, 1000, 2000, 5000, 10000)
    
    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        }
    }
    
    // Track reply sent
    fun trackReplySent(
        context: Context,
        appPackage: String,
        isAiReply: Boolean,
        isGroupMessage: Boolean
    ) {
        initialize(context)
        
        // Log to Firebase
        val bundle = Bundle().apply {
            putString("app_package", appPackage)
            putString("reply_type", if (isAiReply) "ai" else "custom")
            putBoolean("is_group", isGroupMessage)
            putString("timestamp", System.currentTimeMillis().toString())
        }
        firebaseAnalytics?.logEvent("auto_reply_sent", bundle)
        
        // Update local counters and check for milestones
        val prefsManager = PreferencesManager.getPreferencesInstance(context)
        prefsManager?.let {
            val previousTotal = it.getTotalReplyCount()
            
            it.incrementDailyReplyCount()
            it.incrementTotalReplyCount()
            it.incrementAppSpecificCount(appPackage)
            if (isAiReply) {
                it.incrementAiReplyCount()
            } else {
                it.incrementCustomReplyCount()
            }
            
            // Check if we hit a milestone
            val newTotal = it.getTotalReplyCount()
            checkAndCelebrateMilestone(context, previousTotal, newTotal)
        }
    }
    
    /**
     * Check if a milestone was reached and send celebration notification
     */
    private fun checkAndCelebrateMilestone(context: Context, previousCount: Int, newCount: Int) {
        // Find if we crossed any milestone
        val milestoneCrossed = MILESTONES.firstOrNull { milestone ->
            previousCount < milestone && newCount >= milestone
        }
        
        milestoneCrossed?.let { milestone ->
            // Award the badge
            val badge = BadgeRegistry.getBadgeByThreshold(milestone)
            badge?.let {
                val prefsManager = PreferencesManager.getPreferencesInstance(context)
                prefsManager?.awardBadge(it.id)
            }
            
            // Send celebration notification
            sendMilestoneNotification(context, milestone, badge)
            
            // Log milestone achievement to Firebase
            val bundle = Bundle().apply {
                putInt("milestone", milestone)
                putInt("total_replies", newCount)
                putString("badge_id", badge?.id)
                putString("badge_rarity", badge?.rarity?.name)
            }
            firebaseAnalytics?.logEvent("milestone_achieved", bundle)
        }
    }
    
    /**
     * Send a celebration notification for reaching a milestone
     */
    private fun sendMilestoneNotification(context: Context, milestone: Int, badge: com.matrix.autoreply.model.Badge?) {
        val badgeInfo = badge?.let { "${it.emoji} Badge Unlocked: ${it.title}!" } ?: ""
        
        val (title, message) = when {
            milestone < 100 -> Pair(
                "🎉 Achievement Unlocked!",
                "$badgeInfo You've sent $milestone auto-replies!"
            )
            milestone < 500 -> Pair(
                "🚀 ${badge?.rarity?.name?.lowercase()?.capitalize()} Badge!",
                "$badgeInfo $milestone auto-replies sent! You're saving so much time!"
            )
            milestone < 1000 -> Pair(
                "🏆 ${badge?.rarity?.name?.lowercase()?.capitalize()} Badge!",
                "$badgeInfo $milestone auto-replies! You're a power user!"
            )
            milestone < 5000 -> Pair(
                "👑 ${badge?.rarity?.name?.lowercase()?.capitalize()} Badge!",
                "$badgeInfo $milestone auto-replies! You've mastered automation!"
            )
            else -> Pair(
                "🌟 ${badge?.rarity?.name?.lowercase()?.capitalize()} Badge!",
                "$badgeInfo $milestone auto-replies! You're a legend! 🔥"
            )
        }
        
        NotificationHelper.getInstance(context)?.sendNotification(
            title,
            message,
            "com.matrix.autoreply"
        )
    }
    
    // Track message received
    fun trackMessageReceived(
        context: Context,
        appPackage: String,
        isGroupMessage: Boolean
    ) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putString("app_package", appPackage)
            putBoolean("is_group", isGroupMessage)
        }
        firebaseAnalytics?.logEvent("message_received", bundle)
    }
    
    // Track AI prompt template selected
    fun trackPromptTemplateSelected(context: Context, templateId: String) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putString("template_id", templateId)
        }
        firebaseAnalytics?.logEvent("prompt_template_selected", bundle)
    }
    
    // Track AI prompt generated
    fun trackAiPromptGenerated(context: Context, success: Boolean) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putBoolean("success", success)
        }
        firebaseAnalytics?.logEvent("ai_prompt_generated", bundle)
    }
    
    // Track feature usage
    fun trackFeatureUsage(context: Context, featureName: String) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
        }
        firebaseAnalytics?.logEvent("feature_used", bundle)
    }
    
    // Track app enabled/disabled
    fun trackAppToggle(context: Context, enabled: Boolean, appPackage: String) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putBoolean("enabled", enabled)
            putString("app_package", appPackage)
        }
        firebaseAnalytics?.logEvent("app_toggle", bundle)
    }
    
    // Track AI provider changed
    fun trackAiProviderChanged(context: Context, provider: String) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putString("provider", provider)
        }
        firebaseAnalytics?.logEvent("ai_provider_changed", bundle)
    }
    
    // Track errors
    fun trackError(context: Context, errorType: String, errorMessage: String) {
        initialize(context)
        
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        firebaseAnalytics?.logEvent("app_error", bundle)
    }
}
