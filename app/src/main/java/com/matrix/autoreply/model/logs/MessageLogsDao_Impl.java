package com.matrix.autoreply.model.logs;

import android.annotation.SuppressLint;
import android.database.Cursor;

import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;

import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MessageLogsDao_Impl implements MessageLogsDao {
    private final RoomDatabase __db;

    private final EntityInsertionAdapter<MessageLog> __insertionAdapterOfMessageLog;

    private final SharedSQLiteStatement __preparedStmtOfPurgeMessageLogs;

    public MessageLogsDao_Impl(RoomDatabase __db) {
        this.__db = __db;
        this.__insertionAdapterOfMessageLog = new EntityInsertionAdapter<MessageLog>(__db) {
            @Override
            public String createQuery() {
                return "INSERT OR ABORT INTO `message_logs` (`id`,`index`,`notif_id`,`notif_title`,`notif_arrived_time`,`notif_is_replied`,`notif_replied_msg`,`notif_reply_time`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
            }

            @Override
            public void bind(SupportSQLiteStatement stmt, MessageLog value) {
                stmt.bindLong(1, value.getId());
                stmt.bindLong(2, value.getIndex());
                if (value.getNotifId() == null) {
                    stmt.bindNull(3);
                } else {
                    stmt.bindString(3, value.getNotifId());
                }
                if (value.getNotifTitle() == null) {
                    stmt.bindNull(4);
                } else {
                    stmt.bindString(4, value.getNotifTitle());
                }
                stmt.bindLong(5, value.getNotifArrivedTime());
                final int _tmp;
                _tmp = value.isNotifIsReplied() ? 1 : 0;
                stmt.bindLong(6, _tmp);
                if (value.getNotifRepliedMsg() == null) {
                    stmt.bindNull(7);
                } else {
                    stmt.bindString(7, value.getNotifRepliedMsg());
                }
                stmt.bindLong(8, value.getNotifReplyTime());
            }
        };
        this.__preparedStmtOfPurgeMessageLogs = new SharedSQLiteStatement(__db) {
            @Override
            public String createQuery() {
                final String _query = "DELETE FROM message_logs WHERE notif_reply_time <= strftime('%s', datetime('now', '-30 days'));";
                return _query;
            }
        };
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void logReply(final MessageLog log) {
        __db.assertNotSuspendingTransaction();
        __db.beginTransaction();
        try {
            __insertionAdapterOfMessageLog.insert(log);
            __db.setTransactionSuccessful();
        } finally {
            __db.endTransaction();
        }
    }

    @Override
    public void purgeMessageLogs() {
        __db.assertNotSuspendingTransaction();
        final SupportSQLiteStatement _stmt = __preparedStmtOfPurgeMessageLogs.acquire();
        __db.beginTransaction();
        try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
        } finally {
            __db.endTransaction();
            __preparedStmtOfPurgeMessageLogs.release(_stmt);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public long getLastReplyTimeStamp(final String title, final String packageName) {
        final String _sql = "SELECT message_logs.notif_reply_time FROM MESSAGE_LOGS INNER JOIN app_packages ON app_packages.`index` = message_logs.`index` WHERE app_packages.package_name=? AND message_logs.notif_title=? ORDER BY notif_reply_time DESC LIMIT 1";
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
        int _argIndex = 1;
        if (packageName == null) {
            _statement.bindNull(_argIndex);
        } else {
            _statement.bindString(_argIndex, packageName);
        }
        _argIndex = 2;
        if (title == null) {
            _statement.bindNull(_argIndex);
        } else {
            _statement.bindString(_argIndex, title);
        }
        __db.assertNotSuspendingTransaction();
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
            final long _result;
            if (_cursor.moveToFirst()) {
                _result = _cursor.getLong(0);
            } else {
                _result = 0;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public long getNumReplies() {
        final String _sql = "SELECT COUNT(id) FROM MESSAGE_LOGS";
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
        __db.assertNotSuspendingTransaction();
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
            final long _result;
            if (_cursor.moveToFirst()) {
                _result = _cursor.getLong(0);
            } else {
                _result = 0;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public long getFirstRepliedTime() {
        final String _sql = "SELECT notif_reply_time FROM MESSAGE_LOGS ORDER BY notif_reply_time DESC LIMIT 1";
        final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
        __db.assertNotSuspendingTransaction();
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
            final long _result;
            if (_cursor.moveToFirst()) {
                _result = _cursor.getLong(0);
            } else {
                _result = 0;
            }
            return _result;
        } finally {
            _cursor.close();
            _statement.release();
        }
    }

    public static List<Class<?>> getRequiredConverters() {
        return Collections.emptyList();
    }
}
