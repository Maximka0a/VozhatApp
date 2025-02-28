package com.example.vozhatapp.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    // Эти методы оставлены для обратной совместимости, если где-то
    // в коде могут остаться вызовы, использующие Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}