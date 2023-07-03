package com.matrix.autoreply.store.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlin.jvm.Synchronized
import androidx.room.Room
import com.matrix.autoreply.store.data.AppPackage
import com.matrix.autoreply.store.data.ReplyLogs
import com.matrix.autoreply.store.repository.AppPackageDao
import com.matrix.autoreply.store.repository.ReplyLogsDao
import com.matrix.autoreply.constants.Constants
import com.matrix.autoreply.store.data.MessageLogs
import com.matrix.autoreply.store.repository.MessageLogsDao

@Database(entities = [MessageLogs::class, ReplyLogs::class, AppPackage::class], version = 6)
abstract class MessageLogsDB : RoomDatabase() {

    abstract fun messageLogsDao(): MessageLogsDao?
    abstract fun replyLogsDao(): ReplyLogsDao?
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