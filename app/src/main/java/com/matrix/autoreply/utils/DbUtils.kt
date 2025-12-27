package com.matrix.autoreply.utils

import android.content.Context
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.store.database.MessageLogsDB
import android.service.notification.StatusBarNotification
import com.matrix.autoreply.store.data.AppPackage
import com.matrix.autoreply.store.data.MessageLogs
import com.matrix.autoreply.store.data.ReplyLogs
import com.matrix.autoreply.preferences.PreferencesManager

class DbUtils(private val mContext: Context) {
    private val TAG = "DbUtils"
    private var customRepliesData: CustomRepliesData? = null
    
    // Lazy database instance - Room guarantees non-null after proper initialization
    private val messageLogsDB: MessageLogsDB by lazy {
        MessageLogsDB.getInstance(mContext.applicationContext)
            ?: throw IllegalStateException("MessageLogsDB failed to initialize")
    }
    
    val nunReplies: Long
        get() = messageLogsDB.replyLogsDao()?.numReplies ?: 0L

    fun purgeMessageLogs() {
        val preferencesManager = PreferencesManager.getPreferencesInstance(mContext)
        val retentionDays = preferencesManager?.messageLogRetentionDays ?: 30
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        
        messageLogsDB.messageLogsDao()?.purgeOldMessageLogs(cutoffTime)
        messageLogsDB.replyLogsDao()?.purgeOldReplyLogs(cutoffTime)
    }

    fun logReply(sbn: StatusBarNotification, title: String?) {
        // Title is required - skip if null (legitimate case for some notifications)
        if (title.isNullOrEmpty()) return
        
        customRepliesData = CustomRepliesData.getInstance(mContext)
        
        var packageIndex = messageLogsDB.appPackageDao()?.getPackageIndex(sbn.packageName) ?: 0
        if (packageIndex <= 0) {
            messageLogsDB.appPackageDao()?.insertAppPackage(AppPackage(sbn.packageName))
            packageIndex = messageLogsDB.appPackageDao()?.getPackageIndex(sbn.packageName) ?: 0
        }

        val replyText = customRepliesData?.getTextToSendOrElse(null) ?: "Thanks for your message!"
        val logs = ReplyLogs(
            packageIndex,
            title,
            sbn.notification.`when`,
            replyText,
            System.currentTimeMillis()
        )
        messageLogsDB.replyLogsDao()?.logReply(logs)
    }

    fun saveLogs(sbn: StatusBarNotification, title: String?, message: String?) {
        // Title is required - skip if null (legitimate case for some notifications)
        if (title.isNullOrEmpty()) return
        
        var packageIndex = messageLogsDB.appPackageDao()?.getPackageIndex(sbn.packageName) ?: 0
        if (packageIndex <= 0) {
            messageLogsDB.appPackageDao()?.insertAppPackage(AppPackage(sbn.packageName))
            packageIndex = messageLogsDB.appPackageDao()?.getPackageIndex(sbn.packageName) ?: 0
        }
        
        val logs = MessageLogs(
            packageIndex,
            title,
            message,
            sbn.notification.`when`,
            sbn.id
        )
        messageLogsDB.messageLogsDao()?.logMessage(logs)
    }

    fun getLastRepliedTime(packageName: String?, title: String?): Long {
        if (packageName.isNullOrEmpty() || title.isNullOrEmpty()) return 0L
        return messageLogsDB.replyLogsDao()?.getLastReplyTimeStamp(title, packageName) ?: 0L
    }

    val firstRepliedTime: Long
        get() = messageLogsDB.replyLogsDao()?.firstRepliedTime ?: 0L
}
