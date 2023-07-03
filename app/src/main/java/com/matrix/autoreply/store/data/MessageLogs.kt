package com.matrix.autoreply.store.data

import androidx.room.*

@Entity(
    tableName = "message_logs",
    foreignKeys = [ForeignKey(
        entity = AppPackage::class,
        parentColumns = arrayOf("index"),
        childColumns = arrayOf("index"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = arrayOf("index"))]
)
data class MessageLogs(
    var index: Int,
    @field:ColumnInfo(name = "notif_title") var notifTitle: String,
    @field:ColumnInfo(name = "notif_message") var notifMessage: String?,
    @field:ColumnInfo(name = "notif_arrived_time") var notifArrivedTime: Long,
    @field:ColumnInfo(name = "notif_id") var notifId: Int?,
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
