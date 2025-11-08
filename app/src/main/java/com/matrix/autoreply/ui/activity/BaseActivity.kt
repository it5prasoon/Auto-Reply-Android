package com.matrix.autoreply.ui.activity

import android.content.Context
import com.matrix.autoreply.preferences.PreferencesManager.Companion.getPreferencesInstance
import com.matrix.autoreply.helpers.ContextWrapper.Companion.wrap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import android.os.Build

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = getPreferencesInstance(newBase)
        val contextWrapper = wrap(newBase, prefs!!.selectedLocale)
        super.attachBaseContext(contextWrapper)


        if (Build.VERSION.SDK_INT
            <= Build.VERSION_CODES.N_MR1
        ) applyOverrideConfiguration(
            contextWrapper.resources.configuration
        )
    }
}