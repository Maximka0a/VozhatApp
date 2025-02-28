package com.example.vozhatapp.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.vozhatapp.data.local.entity.Achievement
import com.example.vozhatapp.data.local.entity.Child
import com.example.vozhatapp.data.local.entity.Note

data class ChildWithDetails(
    @Embedded val child: Child,
    @Relation(
        parentColumn = "id",
        entityColumn = "child_id"
    )
    val notes: List<Note>,

    @Relation(
        parentColumn = "id",
        entityColumn = "child_id"
    )
    val achievements: List<Achievement>
)