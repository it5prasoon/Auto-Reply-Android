package com.matrix.autoreply.activity

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.matrix.autoreply.R
import com.matrix.autoreply.activity.main.MainActivity


/**
Edited and written by Prasoon
 */
class SplashActivity : AppCompatActivity() {
    var context: Context? = null
    lateinit var springForce: SpringForce

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext
        setContentView(R.layout.activity_splash)
        window.statusBarColor = resources.getColor(R.color.colorPrimary);

        val relative_layout: ImageView = findViewById(R.id.app_image_view)

        Handler(Looper.getMainLooper()).postDelayed({
            springForce = SpringForce(0f)
            relative_layout.pivotX = 0f
            relative_layout.pivotY = 0f
            val springAnim = SpringAnimation(relative_layout, DynamicAnimation.ROTATION).apply {
                springForce.dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                springForce.stiffness = SpringForce.STIFFNESS_VERY_LOW
            }
            springAnim.spring = springForce
            springAnim.setStartValue(80f)
            springAnim.addEndListener { animation, canceled, value, velocity ->
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val height = displayMetrics.heightPixels.toFloat()
                val width = displayMetrics.widthPixels
                relative_layout.animate()
                        .setStartDelay(1)
                        .translationXBy(width.toFloat() / 2)
                        .translationYBy(height)
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(p0: Animator?) {

                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                val i = Intent(this@SplashActivity, MainActivity::class.java)
                                startActivity(i)
                                finish()
                                overridePendingTransition(0, 0)
                            }

                            override fun onAnimationCancel(p0: Animator?) {

                            }

                            override fun onAnimationStart(p0: Animator?) {

                            }

                        })
                        .setInterpolator(DecelerateInterpolator(1f))
                        .start()
            }
            springAnim.start()
        }, SPLASH_TIME_OUT.toLong())

    }

    companion object {
        private const val SPLASH_TIME_OUT = 1000
    }
}