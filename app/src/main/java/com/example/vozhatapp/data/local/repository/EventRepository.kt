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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao,
    private val eventWithAttendanceDao: EventWithAttendanceDao
) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    fun getEventsForDay(timestamp: Long): Flow<List<Event>> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        // Начало дня (00:00:00)
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Конец дня (23:59:59.999)
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d("EventRepository", "Поиск событий с ${sdf.format(startOfDay)} по ${sdf.format(endOfDay)}")

        return eventDao.getEventsForDay(startOfDay, endOfDay).map { events ->
            Log.d("EventRepository", "Найдено событий за день: ${events.size}")
            events
        }
    }

    fun getEventById(eventId: Long): Flow<Event?> {
        return eventDao.getEventById(eventId)
    }

    fun getEventWithAttendance(eventId: Long): Flow<EventWithAttendance?> {
        return eventWithAttendanceDao.getEventWithAttendance(eventId)
    }

    fun getEventsByDateRange(startDate: Long, endDate: Long): Flow<List<Event>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d("EventRepository", "Поиск событий с ${sdf.format(startDate)} по ${sdf.format(endDate)}")

        return eventDao.getEventsByDateRange(startDate, endDate).map { events ->
            Log.d("EventRepository", "Найдено событий в диапазоне: ${events.size}")
            for (event in events) {
                Log.d("EventRepository", "Событие: ${event.id}, ${event.title}, ${sdf.format(event.startTime)}")
            }
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