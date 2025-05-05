package com.example.vozhatapp.navigation

import kotlinx.serialization.Serializable

// Main screen routes
@Serializable
object Home

// Events routes
@Serializable
object Events

@Serializable
data class EventDetail(val eventId: Long)

@Serializable
data class EventEdit(
    val eventId: Long = -1L,
    val sourceRoute: String = "events"
)

// Children routes
@Serializable
object Children

@Serializable
data class ChildDetail(val childId: Long)

@Serializable
data class ChildEdit(val childId: Long = -1L)

@Serializable
data class AddChild(val childId: Long = -1L)

// Attendance routes
@Serializable
object Attendance

@Serializable
object AttendanceReports

@Serializable
data class ChildAttendance(val childId: Long)

// Notes routes
@Serializable
object Notes

@Serializable
data class NoteDetail(val noteId: Long)

@Serializable
data class NoteEdit(val noteId: Long = -1L)

@Serializable
data class AddNote(val childId: Long = -1L) // Optional childId for adding note for specific child

// Games routes
@Serializable
object Games

@Serializable
data class GameDetail(val gameId: Long)

@Serializable
data class GameEdit(val gameId: Long = -1L)

// Profile route
@Serializable
object Profile

@Serializable
data class AddAchievement(val childId: Long, val achievementId: Long = -1L)

@Serializable
data class AchievementDetail(val achievementId: Long)