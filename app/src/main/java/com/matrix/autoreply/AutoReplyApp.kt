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