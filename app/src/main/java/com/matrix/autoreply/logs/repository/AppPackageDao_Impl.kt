package com.matrix.autoreply.logs.repository


import androidx.room.RoomDatabase
import androidx.room.EntityInsertionAdapter
import android.annotation.SuppressLint
import androidx.room.RoomSQLiteQuery
import androidx.room.util.DBUtil
import androidx.sqlite.db.SupportSQLiteStatement
import com.matrix.autoreply.logs.data.AppPackage

class AppPackageDao_Impl(private val __db: RoomDatabase) : AppPackageDao {

    private val __insertionAdapterOfAppPackage: EntityInsertionAdapter<AppPackage?>

    @SuppressLint("RestrictedApi")
    override fun insertAppPackage(appPackage: AppPackage?) {
        __db.assertNotSuspendingTransaction()
        __db.beginTransaction()
        try {
            __insertionAdapterOfAppPackage.insert(appPackage)
            __db.setTransactionSuccessful()
        } finally {
            __db.endTransaction()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun getPackageIndex(packageName: String?): Int {
        val _sql = "SELECT [index] FROM app_packages WHERE package_name=?"
        @SuppressLint("RestrictedApi") val _statement = RoomSQLiteQuery.acquire(_sql, 1)
        val _argIndex = 1
        if (packageName == null) {
            _statement.bindNull(_argIndex)
        } else {
            _statement.bindString(_argIndex, packageName)
        }
        __db.assertNotSuspendingTransaction()
        val _cursor = DBUtil.query(__db, _statement, false, null)
        return try {
            val _result: Int = if (_cursor.moveToFirst()) {
                _cursor.getInt(0)
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
        __insertionAdapterOfAppPackage = object : EntityInsertionAdapter<AppPackage?>(__db) {
            public override fun createQuery(): String {
                return "INSERT OR ABORT INTO `app_packages` (`index`,`package_name`) VALUES (nullif(?, 0),?)"
            }

            public override fun bind(stmt: SupportSQLiteStatement?, entity: AppPackage?) {
                stmt?.bindLong(1, entity?.index!!.toLong())
                if (entity?.packageName == null) {
                    stmt?.bindNull(2)
                } else {
                    stmt?.bindString(2, entity.packageName)
                }
            }
        }
    }
}