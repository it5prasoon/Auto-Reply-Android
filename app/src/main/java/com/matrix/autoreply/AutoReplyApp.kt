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
import androidx.appcompat.app.AppCompatDelegate

class AutoReplyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Set default theme to dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
