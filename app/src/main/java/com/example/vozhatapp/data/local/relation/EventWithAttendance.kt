package com.example.vozhatapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.vozhatapp.data.local.entity.Attendance
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Event

data class EventWithAttendance(
    @Embedded
    val event: Event,

    @Relation(
        entity = Child::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = Attendance::class,
            parentColumn = "eventId",
            entityColumn = "childId"
        )
    )
    val children: List<Child> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "eventId"
    )
    val attendances: List<Attendance> = emptyList()
)