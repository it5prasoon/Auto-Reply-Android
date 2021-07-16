package com.matrix.autoreply.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.matrix.autoreply.R


/**
Edited and written by Prasoon
 */
class SplashActivity : AppCompatActivity() {
    var context: Context? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext
        setContentView(R.layout.activity_splash)
        window.statusBarColor = resources.getColor(R.color.colorPrimary);

        // Creates instance of the manager.

        Handler(Looper.getMainLooper()).postDelayed({
            val i = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_TIME_OUT.toLong())

    }


    companion object {
        private const val SPLASH_TIME_OUT = 500
    }
}