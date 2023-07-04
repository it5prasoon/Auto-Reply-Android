package com.matrix.autoreply.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.matrix.autoreply.services.ForegroundNotificationService

class NotificationListenerUtil(private val activity: Activity) {

    companion object {
        private const val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
        private const val FAILED_TO_GRANT_TOAST = "Failed to grant Notification Listener Service permission"
    }

    fun isNotificationServiceEnabled(): Boolean {
        val componentName = ComponentName(activity, ForegroundNotificationService::class.java)
        val flat = Settings.Secure.getString(activity.contentResolver, ENABLED_NOTIFICATION_LISTENERS)
        return flat != null && flat.contains(componentName.flattenToString())
    }

    fun requestNotificationListenerPermission(launcher: ActivityResultLauncher<Intent>) {
        toggleNotificationListenerService(true)
        launcher.launch(getNotificationListenerSettingsIntent())
    }

    private fun toggleNotificationListenerService(enable: Boolean) {
        val packageManager = activity.packageManager
        packageManager.setComponentEnabledSetting(
            ComponentName(activity, ForegroundNotificationService::class.java),
            if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getNotificationListenerSettingsIntent(): Intent? {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        return if (intent.resolveActivity(activity.packageManager) != null) {
            intent
        } else {
            Toast.makeText(activity, FAILED_TO_GRANT_TOAST, Toast.LENGTH_SHORT).show()
            null
        }
    }
}



