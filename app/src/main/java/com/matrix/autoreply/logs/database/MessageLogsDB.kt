package com.matrix.autoreply.logs.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlin.jvm.Synchronized
import androidx.room.Room
import com.matrix.autoreply.logs.data.AppPackage
import com.matrix.autoreply.logs.data.MessageLog
import com.matrix.autoreply.logs.repository.AppPackageDao
import com.matrix.autoreply.logs.repository.MessageLogsDao
import com.matrix.autoreply.model.utils.Constants

@Database(entities = [MessageLog::class, AppPackage::class], version = 2)
abstract class MessageLogsDB : RoomDatabase() {
    abstract fun logsDao(): MessageLogsDao?
    abstract fun appPackageDao(): AppPackageDao?

    companion object {
        private const val DB_NAME = Constants.LOGS_DB_NAME
        private var _instance: MessageLogsDB? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): MessageLogsDB? {
            if (_instance == null) {
                _instance = Room.databaseBuilder(context.applicationContext, MessageLogsDB::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return _instance
        }
    }
}