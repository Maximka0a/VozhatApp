package com.example.vozhatapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Child::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["childId"]),
        Index(value = ["eventId", "childId"], unique = true)
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // В DAO используется "eventId" как имя столбца
    val eventId: Long,

    // В DAO используется "childId" как имя столбца
    val childId: Long,

    // В DAO используется "isPresent" как имя столбца
    val isPresent: Boolean = true,

    val note: String? = null,

    @ColumnInfo(name = "marked_by")
    val markedBy: Long? = null,

    @ColumnInfo(name = "marked_at")
    val markedAt: Date = Date()
)