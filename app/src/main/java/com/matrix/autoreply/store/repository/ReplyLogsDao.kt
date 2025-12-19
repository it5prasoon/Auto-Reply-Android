package com.matrix.autoreply.store.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.matrix.autoreply.store.data.ReplyLogs

@Dao
interface ReplyLogsDao {
    @Query(
        "SELECT reply_logs.notif_reply_time FROM REPLY_LOGS " +
                "INNER JOIN app_packages ON app_packages.`index` = reply_logs.`index` " +
                "WHERE app_packages.package_name=:packageName AND reply_logs.notif_title=:title " +
                "ORDER BY notif_reply_time DESC LIMIT 1"
    )
    fun getLastReplyTimeStamp(title: String?, packageName: String?): Long

    @Insert
    fun logReply(log: ReplyLogs)

    @get:Query("SELECT COUNT(id) FROM MESSAGE_LOGS")
    val numReplies: Long

    //https://stackoverflow.com/questions/11771580/deleting-android-sqlite-rows-older-than-x-days
    @Query("DELETE FROM reply_logs WHERE notif_reply_time <= strftime('%s', datetime('now', '-30 days'));")
    fun purgeMessageLogs()

    // Delete reply logs older than cutoff time (timestamp-based for consistency)
    @Query("DELETE FROM reply_logs WHERE notif_reply_time < :cutoffTime")
    fun purgeOldReplyLogs(cutoffTime: Long)

    @get:Query("SELECT notif_reply_time FROM REPLY_LOGS ORDER BY notif_reply_time DESC LIMIT 1")
    val firstRepliedTime: Long
}
