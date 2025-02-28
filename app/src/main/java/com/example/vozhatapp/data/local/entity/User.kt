package com.example.vozhatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val password: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    @ColumnInfo(name = "theme_mode") val themeMode: Int = 0, // 0 - светлая, 1 - тёмная
    @ColumnInfo(name = "notifications_enabled") val notificationsEnabled: Boolean = true,
    @ColumnInfo(name = "last_login") val lastLogin: Date? = null,
    @ColumnInfo(name = "created_at") val createdAt: Date = Date()
)