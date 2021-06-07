package com.matrix.autoreply.model.logs;

import android.annotation.SuppressLint;
import android.database.Cursor;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppPackageDao_Impl implements AppPackageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppPackage> __insertionAdapterOfAppPackage;

  public AppPackageDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppPackage = new EntityInsertionAdapter<AppPackage>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `app_packages` (`index`,`package_name`) VALUES (nullif(?, 0),?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AppPackage value) {
        stmt.bindLong(1, value.getIndex());
        if (value.getPackageName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getPackageName());
        }
      }
    };
  }

  @SuppressLint("RestrictedApi")
  @Override
  public void insertAppPackage(final AppPackage appPackage) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfAppPackage.insert(appPackage);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @SuppressLint("RestrictedApi")
  @Override
  public int getPackageIndex(final String packageName) {
    final String _sql = "SELECT [index] FROM app_packages WHERE package_name=?";
    @SuppressLint("RestrictedApi") final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (packageName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, packageName);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if(_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
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
