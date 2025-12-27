/*
 * AutoReply - AI-Powered Smart Auto Reply App
 * Copyright (c) 2024 Prasoon Kumar
 * 
 * This file contains the core notification service that powers AutoReply's functionality.
 * Licensed under GPL v3. Commercial distribution on app stores requires explicit permission.
 * 
 * Contact: prasoonkumar008@gmail.com
 * GitHub: https://github.com/it5prasoon/Auto-Reply-Android
 * 
 * WARNING: This is proprietary core functionality. Unauthorized commercial use will
 * result in DMCA takedown requests and legal action for copyright violation.
 */

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import com.matrix.autoreply.helpers.NotificationHelper
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.preferences.PreferencesManager
import com.matrix.autoreply.utils.DbUtils
import com.matrix.autoreply.utils.NotificationUtils
import com.matrix.autoreply.utils.AiReplyHandler
import com.matrix.autoreply.utils.AnalyticsTracker
import com.matrix.autoreply.utils.ConversationContextManager

/**
 * Data class to hold notification processing request
 */
private data class NotificationRequest(
    val sbn: StatusBarNotification,
    val shouldReply: Boolean,
    val shouldLog: Boolean
)

class ForegroundNotificationService : NotificationListenerService() {

    private val TAG = ForegroundNotificationService::class.java.simpleName
    private var customRepliesData: CustomRepliesData? = null
    private var dbUtils: DbUtils? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Queue-based notification processing system
    private val notificationQueue = Channel<NotificationRequest>(Channel.UNLIMITED)
    
    // Use lazy initialization to ensure PreferencesManager is available
    private val preferencesManager: PreferencesManager by lazy {
        PreferencesManager.getPreferencesInstance(this) 
            ?: throw IllegalStateException("PreferencesManager not initialized")
    }
    
    init {
        // Start queue processor coroutine
        serviceScope.launch {
            processNotificationQueue()
        }
    }
    
    /**
     * Process notifications from queue sequentially to avoid race conditions
     */
    private suspend fun processNotificationQueue() {
        notificationQueue.consumeEach { request ->
            try {
                if (request.shouldLog) {
                    saveLogs(request.sbn)
                }
                
                if (request.shouldReply) {
                    val title = NotificationUtils.getTitle(request.sbn)
                    Log.d(TAG, "Processing reply for notification from $title")
                    
                    // Check if we should respect the timing delay
                    if (canSendReplyNow(request.sbn)) {
                        processReply(request.sbn)
                    } else {
                        // Wait until the delay period passes, then process
                        val timeToWait = calculateWaitTime(request.sbn)
                        if (timeToWait > 0) {
                            Log.d(TAG, "Waiting ${timeToWait}ms before replying to $title")
                            delay(timeToWait)
                        }
                        processReply(request.sbn)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing queued notification: ${e.message}", e)
            }
        }
    }
    
    /**
     * Calculate how long to wait before replying to respect timing constraints
     */
    private fun calculateWaitTime(sbn: StatusBarNotification): Long {
        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext)
        }
        
        val title = NotificationUtils.getTitle(sbn)
        val timeDelay = preferencesManager.autoReplyDelay.coerceAtLeast(10000L)
        val lastRepliedTime = dbUtils?.getLastRepliedTime(sbn.packageName, title) ?: 0L
        val timeSinceLastReply = System.currentTimeMillis() - lastRepliedTime
        
        return if (timeSinceLastReply < timeDelay) {
            timeDelay - timeSinceLastReply
        } else {
            0L
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        try {
            // Skip group summary notifications
            if ((sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
                return
            }
            
            // Perform basic checks before queuing (don't check timing yet - that's done in queue)
            val passesBasicReplyChecks = preferencesManager.isServiceEnabled &&
                    preferencesManager.isAutoReplyEnabled &&
                    isSupportedPackage(sbn) &&
                    NotificationUtils.isNewNotification(sbn) &&
                    isGroupMessageAndReplyAllowed(sbn) &&
                    preferencesManager.isWithinScheduledTime()
            
            val shouldLog = canSaveLogs(sbn)
            
            // Add to queue for sequential processing
            if (passesBasicReplyChecks || shouldLog) {
                serviceScope.launch {
                    notificationQueue.send(NotificationRequest(sbn, passesBasicReplyChecks, shouldLog))
                }
                Log.d(TAG, "Notification queued. Basic reply check: $passesBasicReplyChecks, Log: $shouldLog")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error queueing notification: ${e.message}", e)
        }
    }

    private fun canSaveLogs(sbn: StatusBarNotification): Boolean {
        return preferencesManager.isServiceEnabled &&
                preferencesManager.isMessageLogsEnabled &&
                isSupportedPackage(sbn) &&
                NotificationUtils.isNewNotification(sbn) &&
                isGroupMessageAndReplyAllowed(sbn)
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun saveLogs(sbn: StatusBarNotification) {
        val title = NotificationUtils.getTitle(sbn)
        val message = NotificationUtils.getMessage(sbn)
        
        // Skip logging if title is null (can't identify the conversation)
        if (title.isNullOrEmpty()) {
            Log.d(TAG, "Skipping log - no title available")
            return
        }
        
        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext)
        }
        dbUtils?.saveLogs(sbn, title, message)
    }

    /**
     * Process reply for a notification from the queue
     */
    private suspend fun processReply(sbn: StatusBarNotification) {
        val (_, pendingIntent, remoteInputs1) = NotificationUtils.extractWearNotification(sbn)
        if (remoteInputs1.isEmpty()) {
            return
        }

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
        if (preferencesManager.isAiEnabled && !preferencesManager.aiApiKey.isNullOrEmpty()) {
            // Use suspendCancellableCoroutine to make async callback synchronous
            val replyText = suspendCancellableCoroutine<String> { continuation ->
                AiReplyHandler.generateReply(
                    this, 
                    incomingMessage, 
                    object : AiReplyHandler.AiReplyCallback {
                        override fun onReplyGenerated(reply: String) {
                            if (continuation.isActive) {
                                continuation.resume(reply) {}
                            }
                        }
                        
                        override fun onError(errorMessage: String) {
                            Log.w(TAG, "AI reply failed: $errorMessage, falling back to custom reply")
                            if (continuation.isActive) {
                                continuation.resume(getCustomReply()) {}
                            }
                        }
                    },
                    contactId,
                    packageName
                )
            }
            
            val isAiReply = replyText != getCustomReply()
            sendActualReply(sbn, pendingIntent, remoteInputs1.toTypedArray(), replyText, isAiReply)
            
            if (preferencesManager.isContextEnabled && contactId.isNotEmpty()) {
                ConversationContextManager.addOutgoingReply(
                    this@ForegroundNotificationService,
                    contactId,
                    packageName,
                    replyText
                )
            }
        } else {
            val customReply = getCustomReply()
            sendActualReply(sbn, pendingIntent, remoteInputs1.toTypedArray(), customReply, isAiReply = false)
            
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
    
    private suspend fun sendActualReply(
        sbn: StatusBarNotification, 
        pendingIntent: PendingIntent?, 
        remoteInputs1: Array<RemoteInput>, 
        replyText: String,
        isAiReply: Boolean = false
    ) {
        val delayMs = preferencesManager.replyDelaySeconds * 1000L
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
                
                val title = NotificationUtils.getTitle(sbn)
                if (!title.isNullOrEmpty()) {
                    dbUtils?.logReply(sbn, title)
                }
                
                pendingIntent.send(this@ForegroundNotificationService, 0, localIntent)
                
                val isGroupMsg = sbn.notification.extras.getBoolean("android.isGroupConversation")
                AnalyticsTracker.trackReplySent(
                    applicationContext,
                    sbn.packageName,
                    isAiReply,
                    isGroupMsg
                )
                
                if (preferencesManager.isShowNotificationEnabled) {
                    sbn.notification?.extras?.getString("android.title")?.let {
                        NotificationHelper.getInstance(applicationContext)?.sendNotification(
                            it,
                            replyText, 
                            sbn.packageName
                        )
                    }
                }
                
                cancelNotification(sbn.key)
                
                if (canPurgeMessages()) {
                    dbUtils?.purgeMessageLogs()
                    preferencesManager.setPurgeMessageTime(System.currentTimeMillis())
                }
            }
        } catch (e: CanceledException) {
            Log.e(TAG, "replyToLastNotification error: ${e.localizedMessage}")
        }
    }

    private fun canPurgeMessages(): Boolean {
        val retentionDays = preferencesManager.messageLogRetentionDays
        val daysBeforePurgeInMS = retentionDays * 24 * 60 * 60 * 1000L
        val lastPurgedTime = preferencesManager.lastPurgedTime
        return System.currentTimeMillis() - lastPurgedTime > daysBeforePurgeInMS
    }

    private fun isSupportedPackage(sbn: StatusBarNotification): Boolean {
        return preferencesManager.enabledApps.contains(sbn.packageName)
    }

    private fun canSendReplyNow(sbn: StatusBarNotification): Boolean {
        val DELAY_BETWEEN_REPLY_IN_MILLISEC = 10 * 1000
        val title = NotificationUtils.getTitle(sbn)
        val selfDisplayName = sbn.notification.extras.getString("android.selfDisplayName")

        if (title != null && selfDisplayName != null && title.equals(selfDisplayName, ignoreCase = true)) {
            return false
        }

        if (dbUtils == null) {
            dbUtils = DbUtils(applicationContext)
        }

        val timeDelay = preferencesManager.autoReplyDelay
        val lastRepliedTime = dbUtils?.getLastRepliedTime(sbn.packageName, title) ?: 0L
        
        return System.currentTimeMillis() - lastRepliedTime >= timeDelay.coerceAtLeast(
            DELAY_BETWEEN_REPLY_IN_MILLISEC.toLong()
        )
    }

    private fun isGroupMessageAndReplyAllowed(sbn: StatusBarNotification): Boolean {
        val rawTitle = NotificationUtils.getTitleRaw(sbn)
        val rawText = SpannableString.valueOf("" + sbn.notification.extras["android.text"])
        val isPossiblyAnImageGrpMsg = (rawTitle != null && ": ".contains(rawTitle)
                && rawText.toString().startsWith("\uD83D\uDCF7"))
        
        return if (!sbn.notification.extras.getBoolean("android.isGroupConversation")) {
            !isPossiblyAnImageGrpMsg
        } else {
            preferencesManager.isGroupReplyEnabled
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationQueue.close()
        serviceScope.cancel()
    }
}
