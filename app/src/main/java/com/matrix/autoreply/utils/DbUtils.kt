package com.matrix.autoreply.utils

import android.content.Context
import com.matrix.autoreply.model.CustomRepliesData
import com.matrix.autoreply.store.database.MessageLogsDB
import android.service.notification.StatusBarNotification
import com.matrix.autoreply.store.data.AppPackage
import com.matrix.autoreply.store.data.MessageLogs
import com.matrix.autoreply.store.data.ReplyLogs

class DbUtils(private val mContext: Context) {
    private var customRepliesData: CustomRepliesData? = null
    val nunReplies: Long
        get() {
            val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
            return messageLogsDB!!.replyLogsDao()!!.numReplies
        }

    fun purgeMessageLogs() {
        val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
        messageLogsDB!!.replyLogsDao()!!.purgeMessageLogs()
    }

    fun logReply(sbn: StatusBarNotification, title: String?) {
        customRepliesData = CustomRepliesData.getInstance(mContext)
        val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
        var packageIndex = messageLogsDB!!.appPackageDao()!!.getPackageIndex(sbn.packageName)
        if (packageIndex <= 0) {
            val appPackage = AppPackage(sbn.packageName)
            messageLogsDB.appPackageDao()!!.insertAppPackage(appPackage)
            packageIndex = messageLogsDB.appPackageDao()!!.getPackageIndex(sbn.packageName)
        }

        val logs = ReplyLogs(
            packageIndex,
            title!!,
            sbn.notification.`when`,
            customRepliesData!!.getTextToSendOrElse(null),
            System.currentTimeMillis()
        )
        messageLogsDB.replyLogsDao()!!.logReply(logs)
    }

    fun saveLogs(sbn: StatusBarNotification, title: String?, message: String?) {
        val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
        var packageIndex = messageLogsDB!!.appPackageDao()!!.getPackageIndex(sbn.packageName)
        if (packageIndex <= 0) {
            val appPackage = AppPackage(sbn.packageName)
            messageLogsDB.appPackageDao()!!.insertAppPackage(appPackage)
            packageIndex = messageLogsDB.appPackageDao()!!.getPackageIndex(sbn.packageName)
        }
        val logs = MessageLogs(
            packageIndex,
            title!!,
            message,
            sbn.notification.`when`,
            sbn.id
        )
        messageLogsDB.messageLogsDao()!!.logMessage(logs)
    }

    fun getLastRepliedTime(packageName: String?, title: String?): Long {
        val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
        return messageLogsDB!!.replyLogsDao()!!.getLastReplyTimeStamp(title, packageName)
    }

    val firstRepliedTime: Long
        get() {
            val messageLogsDB = MessageLogsDB.getInstance(mContext.applicationContext)
            return messageLogsDB!!.replyLogsDao()!!.firstRepliedTime
        }
}