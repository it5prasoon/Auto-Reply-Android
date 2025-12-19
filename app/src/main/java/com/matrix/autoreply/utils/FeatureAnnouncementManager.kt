package com.matrix.autoreply.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.matrix.autoreply.preferences.PreferencesManager
import java.io.File
import java.io.IOException

/**
 * Manages feature announcements from JSON configuration
 * Reads from assets/feature_announcements.json (gitignored for dynamic updates)
 */
object FeatureAnnouncementManager {
    private const val TAG = "FeatureAnnouncementManager"
    private const val JSON_FILE_NAME = "feature_announcements.json"
    
    data class FeatureAnnouncement(
        val version: String,
        val title: String,
        val subtitle: String,
        val features: List<Feature>,
        val isEnabled: Boolean = true,
        val priority: Int = 1,
        val requiresAI: Boolean = false // Only show if AI is enabled
    )
    
    data class Feature(
        val icon: String,               // Emoji
        val title: String,
        val description: String,
        val actionText: String? = null, // Optional action button
        val actionTarget: String? = null // "ai_settings", "main", etc.
    )
    
    /**
     * Load feature announcements from JSON file
     */
    fun loadAnnouncements(context: Context): List<FeatureAnnouncement> {
        return try {
            val jsonString = context.assets.open(JSON_FILE_NAME).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<FeatureAnnouncement>>() {}.type
            Gson().fromJson<List<FeatureAnnouncement>>(jsonString, type) ?: emptyList()
        } catch (e: IOException) {
            Log.w(TAG, "Feature announcements JSON not found: $JSON_FILE_NAME")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing feature announcements JSON", e)
            emptyList()
        }
    }
    
    /**
     * Get announcement to show user (if any)
     */
    fun getAnnouncementToShow(context: Context): FeatureAnnouncement? {
        val prefsManager = PreferencesManager.getPreferencesInstance(context) ?: return null
        val announcements = loadAnnouncements(context)
        
        // Don't show if user disabled announcements
        if (prefsManager.dontShowFeatureAnnouncements) return null
        
        if (announcements.isEmpty()) return null
        
        // Find highest priority enabled announcement
        return announcements
            .filter { announcement ->
                announcement.isEnabled && 
                (!announcement.requiresAI || prefsManager.isAiEnabled)
            }
            .maxByOrNull { it.priority }
    }
    
    /**
     * Mark announcements as disabled (don't show again)
     */
    fun markDontShowAgain(context: Context) {
        val prefsManager = PreferencesManager.getPreferencesInstance(context)
        prefsManager?.dontShowFeatureAnnouncements = true
    }
    
    /**
     * Check if should show announcement
     */
    fun shouldShowAnnouncement(context: Context): Boolean {
        return getAnnouncementToShow(context) != null
    }
}
