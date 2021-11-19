package com.matrix.autoreply.model.logs

import com.matrix.autoreply.model.logs.MessageLogsDao_Impl.Companion.requiredConverters
import com.matrix.autoreply.model.logs.AppPackageDao_Impl.Companion.requiredConverters
import com.matrix.autoreply.model.logs.MessageLogsDB
import kotlin.jvm.Volatile
import com.matrix.autoreply.model.logs.MessageLogsDao
import com.matrix.autoreply.model.logs.AppPackageDao
import androidx.room.DatabaseConfiguration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import android.annotation.SuppressLint
import android.os.Build
import androidx.room.RoomOpenHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.util.DBUtil
import androidx.room.RoomOpenHelper.ValidationResult
import androidx.room.util.TableInfo.Column
import androidx.room.util.TableInfo
import androidx.room.InvalidationTracker
import com.matrix.autoreply.model.logs.MessageLogsDao_Impl
import com.matrix.autoreply.model.logs.AppPackageDao_Impl
import java.util.*

class MessageLogsDB_Impl : MessageLogsDB() {

    @Volatile
    private var _messageLogsDao: MessageLogsDao? = null

    @Volatile
    private var _appPackageDao: AppPackageDao? = null
    override fun createOpenHelper(configuration: DatabaseConfiguration): SupportSQLiteOpenHelper {
        @SuppressLint("RestrictedApi") val _openCallback: SupportSQLiteOpenHelper.Callback =
            RoomOpenHelper(configuration, object : RoomOpenHelper.Delegate(2) {
                public override fun createAllTables(_db: SupportSQLiteDatabase) {
                    _db.execSQL("CREATE TABLE IF NOT EXISTS `message_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `index` INTEGER NOT NULL, `notif_id` TEXT, `notif_title` TEXT, `notif_arrived_time` INTEGER NOT NULL, `notif_is_replied` INTEGER NOT NULL, `notif_replied_msg` TEXT, `notif_reply_time` INTEGER NOT NULL, FOREIGN KEY(`index`) REFERENCES `app_packages`(`index`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                    _db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_logs_index` ON `message_logs` (`index`)")
                    _db.execSQL("CREATE TABLE IF NOT EXISTS `app_packages` (`index` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `package_name` TEXT)")
                    _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
                    _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15f2970ddd80a2326858a17ebda632b9')")
                }

                public override fun dropAllTables(_db: SupportSQLiteDatabase) {
                    _db.execSQL("DROP TABLE IF EXISTS `message_logs`")
                    _db.execSQL("DROP TABLE IF EXISTS `app_packages`")
                    if (mCallbacks != null) {
                        var _i = 0
                        val _size = mCallbacks!!.size
                        while (_i < _size) {
                            mCallbacks!![_i].onDestructiveMigration(_db)
                            _i++
                        }
                    }
                }

                override fun onCreate(_db: SupportSQLiteDatabase) {
                    if (mCallbacks != null) {
                        var _i = 0
                        val _size = mCallbacks!!.size
                        while (_i < _size) {
                            mCallbacks!![_i].onCreate(_db)
                            _i++
                        }
                    }
                }

                public override fun onOpen(_db: SupportSQLiteDatabase) {
                    mDatabase = _db
                    _db.execSQL("PRAGMA foreign_keys = ON")
                    internalInitInvalidationTracker(_db)
                    if (mCallbacks != null) {
                        var _i = 0
                        val _size = mCallbacks!!.size
                        while (_i < _size) {
                            mCallbacks!![_i].onOpen(_db)
                            _i++
                        }
                    }
                }

                public override fun onPreMigrate(_db: SupportSQLiteDatabase) {
                    DBUtil.dropFtsSyncTriggers(_db)
                }

                public override fun onPostMigrate(_db: SupportSQLiteDatabase) {}
                override fun onValidateSchema(_db: SupportSQLiteDatabase): ValidationResult {
                    val _columnsMessageLogs =
                        HashMap<String, Column>(8)
                    _columnsMessageLogs["id"] = Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["index"] =
                        Column("index", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_id"] =
                        Column("notif_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_title"] =
                        Column("notif_title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_arrived_time"] =
                        Column("notif_arrived_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_is_replied"] =
                        Column("notif_is_replied", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_replied_msg"] =
                        Column("notif_replied_msg", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsMessageLogs["notif_reply_time"] =
                        Column("notif_reply_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    val _foreignKeysMessageLogs =
                        HashSet<TableInfo.ForeignKey>(1)
                    _foreignKeysMessageLogs.add(
                        TableInfo.ForeignKey(
                            "app_packages",
                            "CASCADE",
                            "NO ACTION",
                            Arrays.asList("index"),
                            Arrays.asList("index")
                        )
                    )
                    val _indicesMessageLogs =
                        HashSet<TableInfo.Index>(1)
                    _indicesMessageLogs.add(
                        TableInfo.Index(
                            "index_message_logs_index",
                            false,
                            Arrays.asList("index")
                        )
                    )
                    val _infoMessageLogs =
                        TableInfo("message_logs", _columnsMessageLogs, _foreignKeysMessageLogs, _indicesMessageLogs)
                    val _existingMessageLogs = TableInfo.read(_db, "message_logs")
                    if (_infoMessageLogs != _existingMessageLogs) {
                        return ValidationResult(
                            false, """message_logs(com.matrix.autoreply.model.logs.MessageLog).
 Expected:
$_infoMessageLogs
 Found:
$_existingMessageLogs"""
                        )
                    }
                    val _columnsAppPackages =
                        HashMap<String, Column>(2)
                    _columnsAppPackages["index"] =
                        Column("index", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY)
                    _columnsAppPackages["package_name"] =
                        Column("package_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY)
                    val _foreignKeysAppPackages =
                        HashSet<TableInfo.ForeignKey>(0)
                    val _indicesAppPackages =
                        HashSet<TableInfo.Index>(0)
                    val _infoAppPackages =
                        TableInfo("app_packages", _columnsAppPackages, _foreignKeysAppPackages, _indicesAppPackages)
                    val _existingAppPackages = TableInfo.read(_db, "app_packages")
                    return if (_infoAppPackages != _existingAppPackages) {
                        ValidationResult(
                            false, """app_packages(com.matrix.autoreply.model.logs.AppPackage).
 Expected:
$_infoAppPackages
 Found:
$_existingAppPackages"""
                        )
                    } else ValidationResult(true, null)
                }
            }, "15f2970ddd80a2326858a17ebda632b9", "bdb6b185b5f8369a155c182c04436ff7")
        val _sqliteConfig =
            SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
                .name(configuration.name)
                .callback(_openCallback)
                .build()
        return configuration.sqliteOpenHelperFactory.create(_sqliteConfig)
    }

    @SuppressLint("RestrictedApi")
    override fun createInvalidationTracker(): InvalidationTracker {
        val _shadowTablesMap = HashMap<String, String>(0)
        val _viewTables = HashMap<String, Set<String>>(0)
        return InvalidationTracker(this, _shadowTablesMap, _viewTables, "message_logs", "app_packages")
    }

    @SuppressLint("RestrictedApi")
    override fun clearAllTables() {
        super.assertNotMainThread()
        val _db = super.getOpenHelper().writableDatabase
        val _supportsDeferForeignKeys = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        try {
            if (!_supportsDeferForeignKeys) {
                _db.execSQL("PRAGMA foreign_keys = FALSE")
            }
            super.beginTransaction()
            if (_supportsDeferForeignKeys) {
                _db.execSQL("PRAGMA defer_foreign_keys = TRUE")
            }
            _db.execSQL("DELETE FROM `message_logs`")
            _db.execSQL("DELETE FROM `app_packages`")
            super.setTransactionSuccessful()
        } finally {
            super.endTransaction()
            if (!_supportsDeferForeignKeys) {
                _db.execSQL("PRAGMA foreign_keys = TRUE")
            }
            _db.query("PRAGMA wal_checkpoint(FULL)").close()
            if (!_db.inTransaction()) {
                _db.execSQL("VACUUM")
            }
        }
    }

    override fun getRequiredTypeConverters(): Map<Class<*>, List<Class<*>>> {
        val _typeConvertersMap = HashMap<Class<*>, List<Class<*>>>()
        _typeConvertersMap[MessageLogsDao::class.java] = MessageLogsDao_Impl.requiredConverters
        _typeConvertersMap[AppPackageDao::class.java] = AppPackageDao_Impl.requiredConverters
        return _typeConvertersMap
    }

    override fun logsDao(): MessageLogsDao? {
        return if (_messageLogsDao != null) {
            _messageLogsDao
        } else {
            synchronized(this) {
                if (_messageLogsDao == null) {
                    _messageLogsDao = MessageLogsDao_Impl(this)
                }
                return _messageLogsDao
            }
        }
    }

    override fun appPackageDao(): AppPackageDao? {
        return if (_appPackageDao != null) {
            _appPackageDao
        } else {
            synchronized(this) {
                if (_appPackageDao == null) {
                    _appPackageDao = AppPackageDao_Impl(this)
                }
                return _appPackageDao
            }
        }
    }
}