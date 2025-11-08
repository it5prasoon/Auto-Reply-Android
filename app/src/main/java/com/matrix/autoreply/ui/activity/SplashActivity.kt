package com.matrix.autoreply.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.matrix.autoreply.R


/**
Edited and written by Prasoon
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val intent = Intent(this@SplashActivity, TabbedActivity::class.java)
                startActivity(intent)
                finish()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }, SPLASH_TIME_OUT.toLong())

    }

    companion object {
        private const val SPLASH_TIME_OUT = 500
    }
}