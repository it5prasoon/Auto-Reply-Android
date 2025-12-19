package com.matrix.autoreply.services

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.util.Log
import androidx.core.app.RemoteInput
import kotlinx.coroutines.*
import com.matrix.autoreply.helpers.NotificationHelper
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.utils.DbUtils
import com.matrix.autoreply.utils.NotificationUtils
import com.matrix.autoreply.utils.AiReplyHandler
import com.matrix.autoreply.utils.AnalyticsTracker
import com.matrix.autoreply.utils.ConversationContextManager


class ForegroundNotificationService : NotificationListenerService() {

    private val TAG = ForegroundNotificationService::class.java.simpleName
    private var customRepliesData: CustomRepliesData? = null
    private var dbUtils: DbUtils? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (canReply(sbn)) {
            sendReply(sbn)
        }

        if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            //Ignore the notification
            return
        }

        if (canSaveLogs(sbn)) {
            saveLogs(sbn)
        }
    }

    private fun canSaveLogs(sbn: StatusBarNotification): Boolean {
        return isServiceEnabled &&
                isMessageLogsEnabled &&
                isSupportedPackage(sbn) &&
                NotificationUtils.isNewNotification(sbn) &&
                isGroupMessageAndReplyAllowed(sbn)
    }

    private fun canReply(sbn: StatusBarNotification): Boolean {
        return isServiceEnabled &&
                isAutoReplyEnabled &&
                isSupportedPackage(sbn) &&
                NotificationUtils.isNewNotification(sbn) &&
                isGroupMessageAndReplyAllowed(sbn) &&
                canSendReplyNow(sbn) &&
                isWithinScheduledTime()
    }
    
    private fun isWithinScheduledTime(): Boolean {
        return PreferencesManager.getPreferencesInstance(this)?.isWithinScheduledTime() ?: true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //START_STICKY  to order the system to restart your service as soon as possible when it was killed.
        return START_STICKY
    }

    private fun saveLogs(sbn: StatusBarNotification) {
        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext)
        }
        dbUtils!!.saveLogs(sbn, NotificationUtils.getTitle(sbn), NotificationUtils.getMessage(sbn))
    }

    private fun sendReply(sbn: StatusBarNotification) {
        val (_, pendingIntent, remoteInputs1) = NotificationUtils.extractWearNotification(sbn)
        if (remoteInputs1.isEmpty()) {
            return
        }

        val preferencesManager = PreferencesManager.getPreferencesInstance(this)!!
        val incomingMessage = NotificationUtils.getMessage(sbn) ?: ""
        val contactId = NotificationUtils.getTitle(sbn) ?: ""
        val packageName = sbn.packageName
        
        // Add incoming message to conversation context if context is enabled
        if (preferencesManager.isContextEnabled && contactId.isNotEmpty()) {
            ConversationContextManager.addIncomingMessage(
                this,
                contactId,
                packageName,
                incomingMessage,
                sbn.notification.`when`
            )
        }
        
        // Check if AI is enabled and try AI reply first
        if (preferencesManager.isAiEnabled && preferencesManager.aiApiKey?.isNotEmpty() == true) {
            AiReplyHandler.generateReply(
                this, 
                incomingMessage, 
                object : AiReplyHandler.AiReplyCallback {
                    override fun onReplyGenerated(reply: String) {
                        sendActualReply(sbn, pendingIntent, remoteInputs1.toTypedArray(), reply, isAiReply = true)
                        
                        // Add AI reply to conversation context
                        if (preferencesManager.isContextEnabled && contactId.isNotEmpty()) {
                            ConversationContextManager.addOutgoingReply(
                                this@ForegroundNotificationService,
                                contactId,
                                packageName,
                                reply
                            )
                        }
                    }
                    
                    override fun onError(errorMessage: String) {
                        Log.w(TAG, "AI reply failed: $errorMessage, falling back to custom reply")
                        // Fallback to custom reply
                        val customReply = getCustomReply()
                        sendActualReply(sbn, pendingIntent, remoteInputs1.toTypedArray(), customReply, isAiReply = false)
                        
                        // Add custom reply to conversation context
                        if (preferencesManager.isContextEnabled && contactId.isNotEmpty()) {
                            ConversationContextManager.addOutgoingReply(
                                this@ForegroundNotificationService,
                                contactId,
                                packageName,
                                customReply
                            )
                        }
                    }
                },
                contactId,
                packageName
            )
        } else {
            // Use custom reply directly
            val customReply = getCustomReply()
            sendActualReply(sbn, pendingIntent, remoteInputs1.toTypedArray(), customReply, isAiReply = false)
            
            // Add custom reply to conversation context
            if (preferencesManager.isContextEnabled && contactId.isNotEmpty()) {
                ConversationContextManager.addOutgoingReply(
                    this,
                    contactId,
                    packageName,
                    customReply
                )
            }
        }
    }
    
    private fun getCustomReply(): String {
        customRepliesData = CustomRepliesData.getInstance(this)
        return customRepliesData?.getTextToSendOrElse(null) ?: "Thanks for your message!"
    }
    
    private fun sendActualReply(
        sbn: StatusBarNotification, 
        pendingIntent: PendingIntent?, 
        remoteInputs1: Array<RemoteInput>, 
        replyText: String,
        isAiReply: Boolean = false
    ) {
        // Launch coroutine with configurable delay to make replies feel more natural
        serviceScope.launch {
            val delayMs = PreferencesManager.getPreferencesInstance(this@ForegroundNotificationService)?.replyDelaySeconds?.times(1000)?.toLong() ?: 3000L
            delay(delayMs)
            
            val remoteInputs = arrayOfNulls<RemoteInput>(remoteInputs1.size)
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val localBundle = Bundle()

            for ((i, remoteIn) in remoteInputs1.withIndex()) {
                remoteInputs[i] = remoteIn
                localBundle.putCharSequence(remoteInputs[i]!!.resultKey, replyText)
            }

            RemoteInput.addResultsToIntent(remoteInputs, localIntent, localBundle)
            try {
                if (pendingIntent != null) {
                    if (dbUtils == null) {
                        dbUtils = DbUtils(applicationContext)
                    }
                    dbUtils!!.logReply(sbn, NotificationUtils.getTitle(sbn))
                    pendingIntent.send(this@ForegroundNotificationService, 0, localIntent)
                    
                    // Track analytics
                    val isGroupMsg = sbn.notification.extras.getBoolean("android.isGroupConversation")
                    AnalyticsTracker.trackReplySent(
                        applicationContext,
                        sbn.packageName,
                        isAiReply,
                        isGroupMsg
                    )
                    
                    if (PreferencesManager.getPreferencesInstance(this@ForegroundNotificationService)!!.isShowNotificationEnabled) {
                        sbn.notification?.extras?.getString("android.title")
                            ?.let {
                                NotificationHelper.getInstance(applicationContext)?.sendNotification(
                                    it,
                                    replyText, sbn.packageName
                                )
                            }
                    }
                    cancelNotification(sbn.key)
                    if (canPurgeMessages()) {
                        dbUtils!!.purgeMessageLogs()
                        PreferencesManager.getPreferencesInstance(this@ForegroundNotificationService)
                            ?.setPurgeMessageTime(System.currentTimeMillis())
                    }
                }
            } catch (e: CanceledException) {
                Log.e(TAG, "replyToLastNotification error: " + e.localizedMessage)
            }
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
        return System.currentTimeMillis() - dbUtils!!.getLastRepliedTime(
            sbn.packageName,
            title
        ) >= timeDelay.coerceAtLeast(
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

    private val isAutoReplyEnabled: Boolean
        get() = PreferencesManager.getPreferencesInstance(this)!!.isAutoReplyEnabled

    private val isMessageLogsEnabled: Boolean
        get() = PreferencesManager.getPreferencesInstance(this)!!.isMessageLogsEnabled

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Clean up coroutines when service is destroyed
    }
}
