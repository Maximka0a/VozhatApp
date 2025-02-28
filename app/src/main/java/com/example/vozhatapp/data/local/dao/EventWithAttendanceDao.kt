package com.example.vozhatapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.vozhatapp.data.local.relation.EventWithAttendance
import kotlinx.coroutines.flow.Flow

@Dao
interface EventWithAttendanceDao {
    @Transaction
    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventWithAttendance(eventId: Long): Flow<EventWithAttendance?>

    @Transaction
    @Query("SELECT * FROM events")
    fun getAllEventsWithAttendance(): Flow<List<EventWithAttendance>>
}