package com.matrix.autoreply.ui.activity.notification

import com.matrix.autoreply.model.utils.NotificationHelper.Companion.getInstance
import com.matrix.autoreply.ui.activity.BaseActivity
import android.os.Bundle
import com.matrix.autoreply.R
import android.content.Intent

class NotificationIntentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_intent_activity) //dummy layout
        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras?.getString("package") != null) {
                val packageName = extras.getString("package")
                getInstance(applicationContext)!!.markNotificationDismissed(packageName!!)
                launchApp(packageName)
            }
        }
    }

    private fun launchApp(packageName: String?) {
        val intent: Intent
        val pm = packageManager
        intent = pm.getLaunchIntentForPackage(packageName!!)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = NotificationIntentActivity::class.java.simpleName
    }
}