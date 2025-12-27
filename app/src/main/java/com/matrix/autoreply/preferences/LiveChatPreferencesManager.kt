package com.matrix.autoreply.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Preferences Manager for Live Chat Feature
 * Manages all settings and configurations for the Live Chat Accessibility Service
 */
class LiveChatPreferencesManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val appContext = context.applicationContext

    companion object {
        @Volatile
        private var INSTANCE: LiveChatPreferencesManager? = null

        // Preference Keys
        private const val KEY_LIVE_CHAT_ENABLED = "pref_live_chat_enabled"
        private const val KEY_LIVE_CHAT_APPS = "pref_live_chat_apps"
        private const val KEY_LIVE_CHAT_AI_ENABLED = "pref_live_chat_ai_enabled"
        private const val KEY_LIVE_CHAT_AI_API_KEY = "pref_live_chat_ai_api_key"
        private const val KEY_LIVE_CHAT_REPLY_DELAY = "pref_live_chat_reply_delay"
        private const val KEY_LIVE_CHAT_USE_CUSTOM_REPLY = "pref_live_chat_use_custom_reply"
        private const val KEY_LIVE_CHAT_GROUP_CHAT_ENABLED = "pref_live_chat_group_enabled"
        private const val KEY_ACCESSIBILITY_PERMISSION_GRANTED = "pref_accessibility_granted"

        fun getInstance(context: Context): LiveChatPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LiveChatPreferencesManager(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Check if Live Chat feature is enabled
     */
    var isLiveChatEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_LIVE_CHAT_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_LIVE_CHAT_ENABLED, value).apply()

    /**
     * Get list of enabled apps for Live Chat
     */
    var enabledApps: Set<String>
        get() = sharedPreferences.getStringSet(KEY_LIVE_CHAT_APPS, emptySet()) ?: emptySet()
        set(value) = sharedPreferences.edit().putStringSet(KEY_LIVE_CHAT_APPS, value).apply()

    /**
     * Check if AI replies are enabled for Live Chat
     */
    var isAiEnabled: Boolean
        get() {
            // Check if main AI is enabled from main PreferencesManager
            val mainPrefs = PreferencesManager.getPreferencesInstance(appContext)
            val mainAiEnabled = mainPrefs?.isAiEnabled ?: false
            
            // Use main AI setting as default, but allow override
            return sharedPreferences.getBoolean(KEY_LIVE_CHAT_AI_ENABLED, mainAiEnabled)
        }
        set(value) = sharedPreferences.edit().putBoolean(KEY_LIVE_CHAT_AI_ENABLED, value).apply()

    /**
     * Get AI API Key (shared with main app)
     */
    var aiApiKey: String?
        get() {
            // Try to get from Live Chat settings first
            val liveChatKey = sharedPreferences.getString(KEY_LIVE_CHAT_AI_API_KEY, null)
            if (!liveChatKey.isNullOrEmpty()) {
                return liveChatKey
            }
            
            // Fall back to main app's AI API key
            val mainPrefs = PreferencesManager.getPreferencesInstance(appContext)
            return mainPrefs?.aiApiKey
        }
        set(value) = sharedPreferences.edit().putString(KEY_LIVE_CHAT_AI_API_KEY, value).apply()

    /**
     * Reply delay in seconds before sending automatic reply
     */
    var replyDelaySeconds: Long
        get() = sharedPreferences.getLong(KEY_LIVE_CHAT_REPLY_DELAY, 2L)
        set(value) = sharedPreferences.edit().putLong(KEY_LIVE_CHAT_REPLY_DELAY, value).apply()

    /**
     * Whether to use custom reply text (vs AI)
     */
    var useCustomReply: Boolean
        get() = sharedPreferences.getBoolean(KEY_LIVE_CHAT_USE_CUSTOM_REPLY, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_LIVE_CHAT_USE_CUSTOM_REPLY, value).apply()

    /**
     * Enable/disable group chat replies
     */
    var isGroupChatEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_LIVE_CHAT_GROUP_CHAT_ENABLED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_LIVE_CHAT_GROUP_CHAT_ENABLED, value).apply()

    /**
     * Track if accessibility permission was granted
     */
    var isAccessibilityPermissionGranted: Boolean
        get() = sharedPreferences.getBoolean(KEY_ACCESSIBILITY_PERMISSION_GRANTED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ACCESSIBILITY_PERMISSION_GRANTED, value).apply()

    /**
     * Helper method to check if all required setup is complete
     */
    fun isSetupComplete(): Boolean {
        return isAccessibilityPermissionGranted && enabledApps.isNotEmpty()
    }

    /**
     * Get setup completion percentage (for UI display)
     */
    fun getSetupProgress(): Int {
        var progress = 0
        
        if (isAccessibilityPermissionGranted) progress += 50
        if (enabledApps.isNotEmpty()) progress += 30
        if (isAiEnabled || useCustomReply) progress += 20
        
        return progress
    }

    /**
     * Add an app to enabled apps list
     */
    fun addEnabledApp(packageName: String) {
        val currentApps = enabledApps.toMutableSet()
        currentApps.add(packageName)
        enabledApps = currentApps
    }

    /**
     * Remove an app from enabled apps list
     */
    fun removeEnabledApp(packageName: String) {
        val currentApps = enabledApps.toMutableSet()
        currentApps.remove(packageName)
        enabledApps = currentApps
    }

    /**
     * Check if specific app is enabled
     */
    fun isAppEnabled(packageName: String): Boolean {
        return enabledApps.contains(packageName)
    }

    /**
     * Clear all Live Chat settings
     */
    fun clearAllSettings() {
        sharedPreferences.edit()
            .remove(KEY_LIVE_CHAT_ENABLED)
            .remove(KEY_LIVE_CHAT_APPS)
            .remove(KEY_LIVE_CHAT_AI_ENABLED)
            .remove(KEY_LIVE_CHAT_AI_API_KEY)
            .remove(KEY_LIVE_CHAT_REPLY_DELAY)
            .remove(KEY_LIVE_CHAT_USE_CUSTOM_REPLY)
            .remove(KEY_LIVE_CHAT_GROUP_CHAT_ENABLED)
            .apply()
    }
}
