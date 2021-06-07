package com.matrix.autoreply.services

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.matrix.autoreply.receivers.NotificationServiceRestartReceiver

class KeepAliveService : Service() {
    override fun onCreate() {
        Log.d("DEBUG", "KeepAliveService onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId);
        Log.d("DEBUG", "KeepAliveService onStartCommand")
        startNotificationService()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("DEBUG", "KeepAliveService onBind")
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("DEBUG", "KeepAliveService onUnbind")
        tryReconnectService()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DEBUG", "KeepAliveService onDestroy")
        tryReconnectService()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        Log.d("DEBUG", "KeepAliveService onTaskRemoved")
        tryReconnectService()
    }

    private fun tryReconnectService() {
        Log.d("DEBUG", "KeepAliveService tryReconnectService")
        //Send broadcast to restart service
        val broadcastIntent = Intent(applicationContext, NotificationServiceRestartReceiver::class.java)
        broadcastIntent.action = "AutoReply-RestartService-Broadcast"
        sendBroadcast(broadcastIntent)
    }

    private fun startNotificationService() {
        if (!isMyServiceRunning) {
            Log.d("DEBUG", "KeepAliveService startNotificationService")
            val mServiceIntent = Intent(this, ForegroundNotificationService::class.java)
            startService(mServiceIntent)
        }
    }

    private val isMyServiceRunning: Boolean
        get() {
            val manager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (ForegroundNotificationService::class.java.equals(service.service.className)) {
                    Log.i("isMyServiceRunning?", true.toString() + "")
                    return true
                }
            }
            Log.i("isMyServiceRunning?", false.toString() + "")
            return false
        }
}