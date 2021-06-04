package com.matrix.autoreply.model.logs;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MessageLogsDB_Impl extends MessageLogsDB {
  private volatile MessageLogsDao _messageLogsDao;

  private volatile AppPackageDao _appPackageDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `message_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `index` INTEGER NOT NULL, `notif_id` TEXT, `notif_title` TEXT, `notif_arrived_time` INTEGER NOT NULL, `notif_is_replied` INTEGER NOT NULL, `notif_replied_msg` TEXT, `notif_reply_time` INTEGER NOT NULL, FOREIGN KEY(`index`) REFERENCES `app_packages`(`index`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_logs_index` ON `message_logs` (`index`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `app_packages` (`index` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `package_name` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '15f2970ddd80a2326858a17ebda632b9')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `message_logs`");
        _db.execSQL("DROP TABLE IF EXISTS `app_packages`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        _db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsMessageLogs = new HashMap<String, TableInfo.Column>(8);
        _columnsMessageLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("index", new TableInfo.Column("index", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_id", new TableInfo.Column("notif_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_title", new TableInfo.Column("notif_title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_arrived_time", new TableInfo.Column("notif_arrived_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_is_replied", new TableInfo.Column("notif_is_replied", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_replied_msg", new TableInfo.Column("notif_replied_msg", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessageLogs.put("notif_reply_time", new TableInfo.Column("notif_reply_time", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessageLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMessageLogs.add(new TableInfo.ForeignKey("app_packages", "CASCADE", "NO ACTION",Arrays.asList("index"), Arrays.asList("index")));
        final HashSet<TableInfo.Index> _indicesMessageLogs = new HashSet<TableInfo.Index>(1);
        _indicesMessageLogs.add(new TableInfo.Index("index_message_logs_index", false, Arrays.asList("index")));
        final TableInfo _infoMessageLogs = new TableInfo("message_logs", _columnsMessageLogs, _foreignKeysMessageLogs, _indicesMessageLogs);
        final TableInfo _existingMessageLogs = TableInfo.read(_db, "message_logs");
        if (! _infoMessageLogs.equals(_existingMessageLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "message_logs(com.matrix.autoreply.model.logs.MessageLog).\n"
                  + " Expected:\n" + _infoMessageLogs + "\n"
                  + " Found:\n" + _existingMessageLogs);
        }
        final HashMap<String, TableInfo.Column> _columnsAppPackages = new HashMap<String, TableInfo.Column>(2);
        _columnsAppPackages.put("index", new TableInfo.Column("index", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppPackages.put("package_name", new TableInfo.Column("package_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppPackages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppPackages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppPackages = new TableInfo("app_packages", _columnsAppPackages, _foreignKeysAppPackages, _indicesAppPackages);
        final TableInfo _existingAppPackages = TableInfo.read(_db, "app_packages");
        if (! _infoAppPackages.equals(_existingAppPackages)) {
          return new RoomOpenHelper.ValidationResult(false, "app_packages(com.matrix.autoreply.model.logs.AppPackage).\n"
                  + " Expected:\n" + _infoAppPackages + "\n"
                  + " Found:\n" + _existingAppPackages);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "15f2970ddd80a2326858a17ebda632b9", "bdb6b185b5f8369a155c182c04436ff7");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "message_logs","app_packages");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `message_logs`");
      _db.execSQL("DELETE FROM `app_packages`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MessageLogsDao.class, MessageLogsDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppPackageDao.class, AppPackageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public MessageLogsDao logsDao() {
    if (_messageLogsDao != null) {
      return _messageLogsDao;
    } else {
      synchronized(this) {
        if(_messageLogsDao == null) {
          _messageLogsDao = new MessageLogsDao_Impl(this);
        }
        return _messageLogsDao;
      }
    }
  }

  @Override
  public AppPackageDao appPackageDao() {
    if (_appPackageDao != null) {
      return _appPackageDao;
    } else {
      synchronized(this) {
        if(_appPackageDao == null) {
          _appPackageDao = new AppPackageDao_Impl(this);
        }
        return _appPackageDao;
      }
    }
  }
}
