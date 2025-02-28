package com.example.vozhatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "children")
data class Child(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lastName: String,
    val age: Int,
    val squadName: String,
    @ColumnInfo(name = "photo_url") val photoUrl: String? = null,
    @ColumnInfo(name = "parent_phone") val parentPhone: String? = null,
    @ColumnInfo(name = "parent_email") val parentEmail: String? = null,
    val address: String? = null,
    @ColumnInfo(name = "medical_notes") val medicalNotes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)