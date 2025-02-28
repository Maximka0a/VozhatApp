package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Event
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY start_time ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date(start_time/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime') ORDER BY start_time ASC")
    fun getEventsForDay(date: Date): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>


    @Query("SELECT * FROM events WHERE start_time >= :startDate AND start_time <= :endDate ORDER BY start_time ASC")
    fun getEventsByDateRange(startDate: Date, endDate: Date): Flow<List<Event>>

    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
}