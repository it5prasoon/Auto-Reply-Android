package com.matrix.autoreply.store.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_packages")
class AppPackage(@field:ColumnInfo(name = "package_name") var packageName: String) {
    @PrimaryKey(autoGenerate = true)
    var index = 0
}