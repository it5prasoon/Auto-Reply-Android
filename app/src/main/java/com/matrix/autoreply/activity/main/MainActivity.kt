package com.matrix.autoreply.activity.main

import android.os.Bundle
import com.matrix.autoreply.R
import com.matrix.autoreply.activity.BaseActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = resources.getColor(R.color.colorPrimary)
    }
}