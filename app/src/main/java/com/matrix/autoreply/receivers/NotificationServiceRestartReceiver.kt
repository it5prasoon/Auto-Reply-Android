package com.matrix.autoreply.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.matrix.autoreply.services.KeepAliveService

class NotificationServiceRestartReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if(action?.equals(Intent.ACTION_BOOT_COMPLETED) == true
                || action?.equals("AutoReply-RestartService-Broadcast") == true) {
            context?.let { restartService(context = context) }
        }
    }

    private fun restartService(context: Context) {
        val serviceIntent = Intent(context, KeepAliveService::class.java)
        context.startService(serviceIntent)
    }
}