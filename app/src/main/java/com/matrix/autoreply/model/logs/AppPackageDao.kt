package com.matrix.autoreply.model.logs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppPackageDao {
    
    @Query("SELECT [index] FROM app_packages WHERE package_name=:packageName")
    fun getPackageIndex(packageName: String?): Int

    @Insert
    fun insertAppPackage(appPackage: AppPackage?)
}