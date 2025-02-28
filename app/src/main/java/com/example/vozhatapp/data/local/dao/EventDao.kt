package com.example.vozhatapp.data.local.dao

import androidx.room.*
import com.example.vozhatapp.data.local.entity.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY start_time ASC")
    fun getAllEvents(): Flow<List<Event>>

    // Исправляем запрос для корректного поиска событий за день
    @Query("SELECT * FROM events WHERE start_time >= :startOfDay AND start_time <= :endOfDay ORDER BY start_time ASC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>

    @Query("SELECT * FROM events WHERE start_time >= :startDate AND start_time <= :endDate ORDER BY start_time ASC")
    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<Event>>

    @Insert
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
}