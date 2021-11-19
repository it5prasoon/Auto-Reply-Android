package com.matrix.autoreply.model.logs


import androidx.room.RoomDatabase
import androidx.room.EntityInsertionAdapter
import androidx.room.SharedSQLiteStatement
import android.annotation.SuppressLint
import androidx.sqlite.db.SupportSQLiteStatement
import androidx.room.RoomSQLiteQuery
import androidx.room.util.DBUtil

class MessageLogsDao_Impl(private val __db: RoomDatabase) : MessageLogsDao {

    private val __insertionAdapterOfMessageLog: EntityInsertionAdapter<MessageLog?>
    private val __preparedStmtOfPurgeMessageLogs: SharedSQLiteStatement
    @SuppressLint("RestrictedApi")
    override fun logReply(log: MessageLog?) {
        __db.assertNotSuspendingTransaction()
        __db.beginTransaction()
        try {
            __insertionAdapterOfMessageLog.insert(log)
            __db.setTransactionSuccessful()
        } finally {
            __db.endTransaction()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun purgeMessageLogs() {
        __db.assertNotSuspendingTransaction()
        val _stmt = __preparedStmtOfPurgeMessageLogs.acquire()
        __db.beginTransaction()
        try {
            _stmt.executeUpdateDelete()
            __db.setTransactionSuccessful()
        } finally {
            __db.endTransaction()
            __preparedStmtOfPurgeMessageLogs.release(_stmt)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun getLastReplyTimeStamp(title: String?, packageName: String?): Long {
        val _sql =
            "SELECT message_logs.notif_reply_time FROM MESSAGE_LOGS " +
                    "INNER JOIN app_packages ON app_packages.`index` = message_logs.`index` " +
                    "WHERE app_packages.package_name=? AND message_logs.notif_title=? ORDER BY notif_reply_time DESC LIMIT 1"
        val _statement = RoomSQLiteQuery.acquire(_sql, 2)
        var _argIndex = 1
        if (packageName == null) {
            _statement.bindNull(_argIndex)
        } else {
            _statement.bindString(_argIndex, packageName)
        }
        _argIndex = 2
        if (title == null) {
            _statement.bindNull(_argIndex)
        } else {
            _statement.bindString(_argIndex, title)
        }
        __db.assertNotSuspendingTransaction()
        val _cursor = DBUtil.query(__db, _statement, false, null)
        return try {
            val _result: Long
            _result = if (_cursor.moveToFirst()) {
                _cursor.getLong(0)
            } else {
                0
            }
            _result
        } finally {
            _cursor.close()
            _statement.release()
        }
    }

    @get:SuppressLint("RestrictedApi")
    override val numReplies: Long
        get() {
            val _sql = "SELECT COUNT(id) FROM MESSAGE_LOGS"
            val _statement = RoomSQLiteQuery.acquire(_sql, 0)
            __db.assertNotSuspendingTransaction()
            val _cursor = DBUtil.query(__db, _statement, false, null)
            return try {
                val _result: Long
                _result = if (_cursor.moveToFirst()) {
                    _cursor.getLong(0)
                } else {
                    0
                }
                _result
            } finally {
                _cursor.close()
                _statement.release()
            }
        }

    @get:SuppressLint("RestrictedApi")
    override val firstRepliedTime: Long
        get() {
            val _sql = "SELECT notif_reply_time FROM MESSAGE_LOGS ORDER BY notif_reply_time DESC LIMIT 1"
            val _statement = RoomSQLiteQuery.acquire(_sql, 0)
            __db.assertNotSuspendingTransaction()
            val _cursor = DBUtil.query(__db, _statement, false, null)
            return try {
                val _result: Long = if (_cursor.moveToFirst()) {
                    _cursor.getLong(0)
                } else {
                    0
                }
                _result
            } finally {
                _cursor.close()
                _statement.release()
            }
        }

    companion object {
        @JvmStatic
        val requiredConverters: List<Class<*>>
            get() = emptyList()
    }

    init {
        __insertionAdapterOfMessageLog = object : EntityInsertionAdapter<MessageLog?>(__db) {
            public override fun createQuery(): String {
                return "INSERT OR ABORT INTO `message_logs` (`id`,`index`,`notif_id`," +
                        "`notif_title`,`notif_arrived_time`,`notif_is_replied`,`notif_replied_msg`," +
                        "`notif_reply_time`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"
            }

            public override fun bind(stmt: SupportSQLiteStatement?, value: MessageLog?) {
                stmt?.bindLong(1, value?.id!!.toLong())
                stmt?.bindLong(2, value?.index!!.toLong())
                if (value?.notifId == null) {
                    stmt?.bindNull(3)
                } else {
                    stmt?.bindString(3, value.notifId)
                }
                if (value?.notifTitle == null) {
                    stmt?.bindNull(4)
                } else {
                    stmt?.bindString(4, value.notifTitle)
                }
                stmt?.bindLong(5, value?.notifArrivedTime!!)
                val _tmp: Int = if (value?.isNotifIsReplied!!) 1 else 0
                stmt?.bindLong(6, _tmp.toLong())
                stmt?.bindString(7, value.notifRepliedMsg)
                stmt?.bindLong(8, value.notifReplyTime)
            }
        }
        __preparedStmtOfPurgeMessageLogs = object : SharedSQLiteStatement(__db) {
            public override fun createQuery(): String {
                return "DELETE FROM message_logs WHERE notif_reply_time <= strftime('%s', datetime('now', '-30 days'));"
            }
        }
    }
}