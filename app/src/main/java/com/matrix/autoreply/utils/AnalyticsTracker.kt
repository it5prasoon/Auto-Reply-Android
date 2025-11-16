package com.matrix.autoreply.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.matrix.autoreply.preferences.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsTracker {
    
    private var firebaseAnalytics: FirebaseAnalytics? = null
    
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
        
        // Update local counters
        val prefsManager = PreferencesManager.getPreferencesInstance(context)
        prefsManager?.let {
            it.incrementDailyReplyCount()
            it.incrementTotalReplyCount()
            it.incrementAppSpecificCount(appPackage)
            if (isAiReply) {
                it.incrementAiReplyCount()
            } else {
                it.incrementCustomReplyCount()
            }
        }
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
