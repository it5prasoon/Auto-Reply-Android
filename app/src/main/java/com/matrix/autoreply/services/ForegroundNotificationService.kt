package com.matrix.autoreply.services

import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.util.Log
import androidx.core.app.RemoteInput
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.utils.DbUtils
import com.matrix.autoreply.helpers.NotificationHelper
import com.matrix.autoreply.utils.NotificationUtils

class ForegroundNotificationService : NotificationListenerService() {

    private val TAG = ForegroundNotificationService::class.java.simpleName
    private var customRepliesData: CustomRepliesData? = null
    private var dbUtils: DbUtils? = null
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (canReply(sbn)) {
            sendReply(sbn)
        }
    }

    private fun canReply(sbn: StatusBarNotification): Boolean {
        return isServiceEnabled &&
                isSupportedPackage(sbn) &&
                NotificationUtils.isNewNotification(sbn) &&
                isGroupMessageAndReplyAllowed(sbn) &&
                canSendReplyNow(sbn)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //START_STICKY  to order the system to restart your service as soon as possible when it was killed.
        return START_STICKY
    }

    private fun sendReply(sbn: StatusBarNotification) {
        val (_, pendingIntent, remoteInputs1) = NotificationUtils.extractWearNotification(sbn)
        if (remoteInputs1.isEmpty()) {
            return
        }

        customRepliesData = CustomRepliesData.getInstance(this)
        val remoteInputs = arrayOfNulls<RemoteInput>(remoteInputs1.size)
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val localBundle = Bundle()

        for ((i, remoteIn) in remoteInputs1.withIndex()) {
            remoteInputs[i] = remoteIn
            localBundle.putCharSequence(
                remoteInputs[i]!!.resultKey, customRepliesData
                    ?.getTextToSendOrElse(null)
            )
        }

        RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle)
        try {
            if (pendingIntent != null) {
                if (dbUtils == null) {
                    dbUtils = DbUtils(applicationContext)
                }
                dbUtils!!.logReply(sbn, NotificationUtils.getTitle(sbn), NotificationUtils.getMessage(sbn))
                pendingIntent.send(this, 0, localIntent)
                if (PreferencesManager.getPreferencesInstance(this)!!.isShowNotificationEnabled) {
                    sbn.notification?.extras?.getString("android.title")
                        ?.let {
                            NotificationHelper.getInstance(applicationContext)?.sendNotification(
                                it,
                                sbn.notification.extras.getString("android.text"), sbn.packageName
                            )
                        }
                }
                cancelNotification(sbn.key)
                if (canPurgeMessages()) {
                    dbUtils!!.purgeMessageLogs()
                    PreferencesManager.getPreferencesInstance(this)
                        ?.setPurgeMessageTime(System.currentTimeMillis())
                }
            }
        } catch (e: CanceledException) {
            Log.e(TAG, "replyToLastNotification error: " + e.localizedMessage)
        }
    }

    private fun canPurgeMessages(): Boolean {
        val daysBeforePurgeInMS = 30 * 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() -
                (PreferencesManager.getPreferencesInstance(this)?.lastPurgedTime!!) > daysBeforePurgeInMS
    }

    private fun isSupportedPackage(sbn: StatusBarNotification): Boolean {
        return PreferencesManager.getPreferencesInstance(this)!!
            .enabledApps
            .contains(sbn.packageName)
    }

    private fun canSendReplyNow(sbn: StatusBarNotification): Boolean {

        // Time between consecutive replies is 10 secs
        val DELAY_BETWEEN_REPLY_IN_MILLISEC = 10 * 1000
        val title = NotificationUtils.getTitle(sbn)
        val selfDisplayName = sbn.notification.extras.getString("android.selfDisplayName")

        if (title != null && selfDisplayName != null && title.equals(selfDisplayName, ignoreCase = true)) {
            return false
        }

        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext)
        }

        val timeDelay = PreferencesManager.getPreferencesInstance(this)!!.autoReplyDelay
        return System.currentTimeMillis() - dbUtils!!.getLastRepliedTime(sbn.packageName, title) >= Math.max(
            timeDelay,
            DELAY_BETWEEN_REPLY_IN_MILLISEC.toLong()
        )
    }

    private fun isGroupMessageAndReplyAllowed(sbn: StatusBarNotification): Boolean {
        val rawTitle = NotificationUtils.getTitleRaw(sbn)
        val rawText = SpannableString.valueOf("" + sbn.notification.extras["android.text"])
        val isPossiblyAnImageGrpMsg = (rawTitle != null && ": ".contains(rawTitle)
                && rawText != null && rawText.toString().startsWith("\uD83D\uDCF7"))
        return if (!sbn.notification.extras.getBoolean("android.isGroupConversation")) {
            !isPossiblyAnImageGrpMsg
        } else {
            PreferencesManager.getPreferencesInstance(this)!!.isGroupReplyEnabled
        }
    }

    private val isServiceEnabled: Boolean
        get() = PreferencesManager.getPreferencesInstance(this)!!.isServiceEnabled
}