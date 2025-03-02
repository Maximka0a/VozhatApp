package com.example.vozhatapp.presentation.events.utils

import androidx.compose.ui.graphics.Color
import com.example.vozhatapp.data.local.entity.Event
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Форматирует дату в строковое представление (например, "1 марта")
 */
fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    return date.format(formatter)
}

/**
 * Проверяет, есть ли события в указанный день
 */
fun hasEventsOnDay(date: LocalDate, events: List<Event>): Boolean {
    val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    return events.any {
        it.startTime in startOfDay..endOfDay ||
                it.endTime in startOfDay..endOfDay ||
                (it.startTime <= startOfDay && it.endTime >= endOfDay)
    }
}

/**
 * Возвращает цвет в зависимости от статуса события
 */
fun getEventStatusColor(status: Int): Color {
    return when (status) {
        0 -> Color(0xFF2196F3) // Upcoming: Blue
        1 -> Color(0xFF4CAF50) // In progress: Green
        else -> Color(0xFF9E9E9E) // Completed: Gray
    }
}

/**
 * Генерирует список дат для недели, начиная с понедельника, для выбранной даты
 */
fun generateWeekDates(selectedDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()

    // Get the start of the week (Monday) for the selected date
    val startOfWeek = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)

    // Add all 7 days of the week
    for (i in 0..6) {
        dates.add(startOfWeek.plusDays(i.toLong()))
    }

    return dates
}

/**
 * Преобразует первую букву строки в прописную
 */
fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}