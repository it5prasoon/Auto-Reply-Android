package com.matrix.autoreply.store.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.matrix.autoreply.store.data.MessageLog

@Dao
interface MessageLogsDao {
    @Query(
        "SELECT message_logs.notif_reply_time FROM MESSAGE_LOGS " +
                "INNER JOIN app_packages ON app_packages.`index` = message_logs.`index` " +
                "WHERE app_packages.package_name=:packageName AND message_logs.notif_title=:title " +
                "ORDER BY notif_reply_time DESC LIMIT 1"
    )
    fun getLastReplyTimeStamp(title: String?, packageName: String?): Long

    @Insert
    fun logReply(log: MessageLog?)

    @get:Query("SELECT COUNT(id) FROM MESSAGE_LOGS")
    val numReplies: Long

    // retrieve the distinct notif_title values from the message_logs
    @Query("SELECT DISTINCT notif_title FROM message_logs")
    fun getDistinctNotificationTitles(): List<String>

    // retrieve the notif_message items for a given notif_title
    @Query("SELECT * FROM message_logs WHERE notif_title = :notifTitle")
    fun getMessageLogsWithTitle(notifTitle: String): List<MessageLog>

    //https://stackoverflow.com/questions/11771580/deleting-android-sqlite-rows-older-than-x-days
    @Query("DELETE FROM message_logs WHERE notif_reply_time <= strftime('%s', datetime('now', '-30 days'));")
    fun purgeMessageLogs()

    @get:Query("SELECT notif_reply_time FROM MESSAGE_LOGS ORDER BY notif_reply_time DESC LIMIT 1")
    val firstRepliedTime: Long
}