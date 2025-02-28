package com.example.vozhatapp.data.repository

import android.util.Log
import com.example.vozhatapp.data.local.dao.EventDao
import com.example.vozhatapp.data.local.dao.EventWithAttendanceDao
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.data.local.relation.EventWithAttendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val eventWithAttendanceDao: EventWithAttendanceDao
) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    fun getEventsForDay(date: Date): Flow<List<Event>> {
        Log.d("EventRepository", "Запрос событий на день: $date")
        return eventDao.getEventsForDay(date).map { events ->
            Log.d("EventRepository", "Получено событий: ${events.size}")
            events
        }
    }

    fun getEventById(eventId: Long): Flow<Event?> {
        return eventDao.getEventById(eventId)
    }

    fun getEventWithAttendance(eventId: Long): Flow<EventWithAttendance?> {
        return eventWithAttendanceDao.getEventWithAttendance(eventId)
    }

    fun getEventsByDateRange(startDate: Date, endDate: Date): Flow<List<Event>> {
        Log.d("EventRepository", "Запрос событий в диапазоне: $startDate - $endDate")
        return eventDao.getEventsByDateRange(startDate, endDate).map { events ->
            Log.d("EventRepository", "Получено событий в диапазоне: ${events.size}")
            events
        }
    }

    suspend fun insertEvent(event: Event): Long {
        return withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }
    }

    suspend fun updateEvent(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.updateEvent(event)
        }
    }

    suspend fun deleteEvent(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(event)
        }
    }
}