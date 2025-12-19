package com.matrix.autoreply.constants

import com.matrix.autoreply.model.App


object Constants {

    const val PERMISSION_DIALOG_TITLE = "permission_dialog_title"
    const val PERMISSION_DIALOG_MSG = "permission_dialog_msg"
    const val PERMISSION_DIALOG_DENIED_TITLE = "permission_dialog_denied_title"
    const val PERMISSION_DIALOG_DENIED_MSG = "permission_dialog_denied_msg"
    const val PERMISSION_DIALOG_DENIED = "permission_dialog_denied"
    const val LOGS_DB_NAME = "logs_messages_db"
    const val NOTIFICATION_CHANNEL_ID = "autoreply"
    const val NOTIFICATION_CHANNEL_NAME = "autoreply_channel"

    val SUPPORTED_APPS: List<App> = mutableListOf(
            App("WhatsApp", "com.whatsapp"),
            App("WhatsApp Business", "com.whatsapp.w4b"),
            App("Facebook Messenger", "com.facebook.orca"),
            App("Instagram", "com.instagram.android")
    )

    const val MIN_DAYS = 0
    const val MAX_DAYS = 7

    // Notification Listener Service
    const val PERMISSION_DENIED = "Permission Denied"
    const val PERMISSION_GRANTED = "Permission Granted"
    const val RESTART_SERVICE_TOAST = "Service not enabled! Please restart the service from setting."
}
