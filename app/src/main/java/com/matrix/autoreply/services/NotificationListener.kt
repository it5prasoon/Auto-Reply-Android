package com.matrix.autoreply.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.io.File
import java.text.DateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        if (sbn != null) {
            if (sbn.packageName == "com.whatsapp") {

                val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date())
                val sender = sbn.notification.extras.getString("android.title")
                val msg = sbn.notification.extras.getString("android.text")

                if (msg != null) {
                    if (msg != "This message was deleted" && !msg.contains("new messages") && msg != "\uD83D\uDCF7 Photo"
                            && msg != "Calling…" && msg != "Ringing…" && msg != "Missed voice call" && msg != "Incoming voice call"
                            && msg != "Ongoing video call" && !msg.contains("Sticker")
                            && !msg.contains("missed calls") && msg != "\uD83D\uDCF9 Incoming video call"
                            && msg.substring(2) != "GIF" && msg.substring(2) != "Video ("
                            && !msg.contains("Sending video to") && !msg.contains("Sending file to")
                            && !msg.contains("files to") && !msg.contains("videos to")
                            && !msg.contains("Sending GIF to")) {
                        File(this.filesDir, "msgLog.txt").appendText("$date | $sender: $msg\n")
                    }
                }
            } else if (sbn.packageName == "org.thoughtcrime.securesms") {

                val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date())
                val sender = sbn.notification.extras.getString("android.title")
                val msg = sbn.notification.extras.getCharSequence("android.text")?.toString()

                File(this.filesDir, "signalMsgLog.txt").appendText("$date | $sender: $msg\n")

            } else if (sbn.packageName == "com.whatsapp.w4b") {
                val date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date())
                val sender = sbn.notification.extras.getString("android.title")
                val msg = sbn.notification.extras.getString("android.text")

                if (msg != null) {
                    if (msg != "This message was deleted" && !msg.contains("new messages") && msg != "\uD83D\uDCF7 Photo"
                            && msg != "Calling…" && msg != "Ringing…" && msg != "Missed voice call" && msg != "Incoming voice call"
                            && msg != "Ongoing video call" && !msg.contains("Sticker")
                            && !msg.contains("missed calls") && msg != "\uD83D\uDCF9 Incoming video call"
                            && msg.substring(2) != "GIF" && msg.substring(2) != "Video ("
                            && !msg.contains("Sending video to") && !msg.contains("Sending file to")
                            && !msg.contains("files to") && !msg.contains("videos to")
                            && !msg.contains("Sending GIF to")) {
                        File(this.filesDir, "waBusMsgLog.txt").appendText("$date | $sender: $msg\n")
                    }
                }
            }
        }
    }
}

