package com.example.vozhatapp.utils

import java.util.*

object DateUtils {
    /**
     * Gets the start of the day (00:00:00.000) for the given timestamp
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Gets the end of the day (23:59:59.999) for the given timestamp
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Gets a formatted date string in the specified format
     */
    fun formatDate(timestamp: Long, format: String): String {
        val dateFormat = java.text.SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Gets the timestamp for the start of a given month
     */
    fun getStartOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Gets the timestamp for the end of a given month
     */
    fun getEndOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.timeInMillis
    }

    /**
     * Gets a list of day names (e.g., "Mon", "Tue", etc.) in the current locale
     */
    fun getDayNames(abbreviated: Boolean = true): List<String> {
        val calendar = Calendar.getInstance()
        val dayNames = mutableListOf<String>()
        val dateFormat = if (abbreviated)
            java.text.SimpleDateFormat("EEE", Locale.getDefault())
        else
            java.text.SimpleDateFormat("EEEE", Locale.getDefault())

        // Start with Monday (Calendar.MONDAY = 2)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        for (i in 0 until 7) {
            dayNames.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }
        return dayNames
    }

    /**
     * Gets a list of month names in the current locale
     */
    fun getMonthNames(abbreviated: Boolean = false): List<String> {
        val calendar = Calendar.getInstance()
        val monthNames = mutableListOf<String>()
        val dateFormat = if (abbreviated)
            java.text.SimpleDateFormat("MMM", Locale.getDefault())
        else
            java.text.SimpleDateFormat("MMMM", Locale.getDefault())

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        for (i in 0 until 12) {
            calendar.set(Calendar.MONTH, i)
            monthNames.add(dateFormat.format(calendar.time))
        }
        return monthNames
    }


}