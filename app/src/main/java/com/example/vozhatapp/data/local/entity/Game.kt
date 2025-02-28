package com.example.vozhatapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    @ColumnInfo(name = "min_age") val minAge: Int? = null,
    @ColumnInfo(name = "max_age") val maxAge: Int? = null,
    @ColumnInfo(name = "min_players") val minPlayers: Int? = null,
    @ColumnInfo(name = "max_players") val maxPlayers: Int? = null,
    val duration: Int? = null, // Длительность в минутах
    val materials: String? = null
)