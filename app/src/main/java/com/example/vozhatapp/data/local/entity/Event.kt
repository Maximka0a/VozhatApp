package com.example.vozhatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "start_time") val startTime: Date,
    @ColumnInfo(name = "end_time") val endTime: Date,
    val location: String? = null,
    // Статус: 0 - предстоит, 1 - в процессе, 2 - завершено
    val status: Int = 0,
    @ColumnInfo(name = "created_by") val createdBy: Long,
    @ColumnInfo(name = "created_at") val createdAt: Date = Date()
)