package com.matrix.autoreply.activity.settings

import android.os.Bundle
import com.matrix.autoreply.R
import com.matrix.autoreply.activity.BaseActivity

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
    }
}