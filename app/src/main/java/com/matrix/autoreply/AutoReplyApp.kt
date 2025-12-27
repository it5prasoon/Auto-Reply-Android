/*
 * AutoReply - AI-Powered Smart Auto Reply App
 * Copyright (c) 2024 Prasoon Kumar
 * 
 * This file is part of AutoReply, licensed under GPL v3.
 * Commercial distribution on app stores requires explicit permission.
 * 
 * Contact: prasoonkumar008@gmail.com
 * GitHub: https://github.com/it5prasoon/Auto-Reply-Android
 * 
 * WARNING: Unauthorized commercial distribution will result in DMCA takedown requests.
 */

package com.matrix.autoreply

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.matrix.autoreply.preferences.PreferencesManager

class AutoReplyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize PreferencesManager early to ensure it's available for all services
        PreferencesManager.initialize(this)
        
        // Set default theme to dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    
    companion object {
        private lateinit var instance: AutoReplyApp
        
        val appContext: Context
            get() = instance.applicationContext
    }
    
    init {
        instance = this
    }
}
