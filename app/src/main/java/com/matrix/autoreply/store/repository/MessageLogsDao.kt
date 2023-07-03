package com.matrix.autoreply.store.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.matrix.autoreply.store.data.MessageLogs

@Dao
interface MessageLogsDao {

    @Insert
    fun logMessage(log: MessageLogs?)

    // retrieve the distinct notif_title values from the message_logs
    @Query("SELECT DISTINCT notif_title FROM message_logs")
    fun getDistinctNotificationTitles(): List<String>

    // retrieve the notif_message items for a given notif_title
    @Query("SELECT * FROM message_logs WHERE notif_title = :notifTitle")
    fun getMessageLogsWithTitle(notifTitle: String): List<MessageLogs>

}