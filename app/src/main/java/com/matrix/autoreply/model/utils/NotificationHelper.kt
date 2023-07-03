package com.matrix.autoreply.model.utils


import android.app.NotificationManager
import android.os.Build
import android.app.NotificationChannel
import org.json.JSONException
import android.content.Intent
import com.matrix.autoreply.ui.activity.notification.NotificationIntentActivity
import android.app.PendingIntent
import android.content.Context
import com.matrix.autoreply.R
import androidx.core.app.NotificationCompat
import com.matrix.autoreply.BuildConfig
import org.json.JSONObject

class NotificationHelper private constructor(private val appContext: Context) {
    private fun init() {
        notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
        for ((_, packageName) in Constants.SUPPORTED_APPS) {
            try {
                appsList.put(packageName, false)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    fun sendNotification(title: String, message: String?, packageName: String) {
        var title = title
        for ((name, packageName1) in Constants.SUPPORTED_APPS) {
            if (packageName1.equals(packageName, ignoreCase = true)) {
                title = "$name: $title"
                break
            }
        }
        val intent = Intent(appContext, NotificationIntentActivity::class.java)
        intent.putExtra("package", packageName)

        // Add the required flags for Android 12 and above
        val pIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val flags = PendingIntent.FLAG_IMMUTABLE
            PendingIntent.getActivity(
                appContext, 0, intent, flags
            )
        } else {
            PendingIntent.getActivity(appContext, 0, intent, 0)
        }

        val notificationBuilder = NotificationCompat.Builder(
            appContext, Constants.NOTIFICATION_CHANNEL_ID
        )
            .setGroup("autoreply-$packageName")
            .setGroupSummary(false)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pIntent)

        //logic to detect if notifications exists else generate summary notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notifications = notificationManager!!.activeNotifications
            for ((_, packageName1) in Constants.SUPPORTED_APPS) {
                try {
                    appsList.put(packageName1, false)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            for (notification in notifications) {
                if (notification.packageName.equals(BuildConfig.APPLICATION_ID, ignoreCase = true)) {
                    setNotificationSummaryShown(notification.notification.group)
                }
            }
        }
        val notifId = System.currentTimeMillis().toInt()
        notificationManager!!.notify(notifId, notificationBuilder.build())
        try {
            if (!appsList.getBoolean(packageName)) {
                appsList.put(packageName, true)
                //Need to create one summary notification, this will help group all individual notifications
                val summaryNotificationBuilder = NotificationCompat.Builder(
                    appContext, Constants.NOTIFICATION_CHANNEL_ID
                )
                    .setGroup("autoreply-$packageName")
                    .setGroupSummary(true)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent)
                notificationManager!!.notify(notifId + 1, summaryNotificationBuilder.build())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setNotificationSummaryShown(packageName: String) {
        var packageName = packageName
        packageName = packageName.replace("autoreply-", "")
        try {
            appsList.put(packageName, true)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun markNotificationDismissed(packageName: String) {
        var packageName = packageName
        packageName = packageName.replace("autoreply-", "")
        try {
            appsList.put(packageName, false)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        private var _INSTANCE: NotificationHelper? = null
        private var notificationManager: NotificationManager? = null
        private val appsList = JSONObject()
        @JvmStatic
        fun getInstance(context: Context): NotificationHelper? {
            if (_INSTANCE == null) {
                _INSTANCE = NotificationHelper(context)
            }
            return _INSTANCE
        }
    }

    init {
        init()
    }
}