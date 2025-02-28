package com.example.vozhatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.presentation.home.HomeScreen
import com.example.vozhatapp.ui.theme.VozhatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debugDateIssue()
        enableEdgeToEdge()
        setContent {
            VozhatAppTheme {
                HomeScreen(
                    onNavigateToEvents = {  },
                    onNavigateToChildren = {},
                    onNavigateToGames = {},
                    onNavigateToProfile = {},
                    onNavigateToChildDetails = {},
                    onNavigateToEventDetails = {},
                )
            }
        }
    }
}
// Добавьте этот метод в onCreate после setContent
private fun debugDateIssue() {
    val calendar = Calendar.getInstance()
    val currentDate = calendar.time

    Log.d("DateDebug", "Текущая системная дата: $currentDate")

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    val todayEnd = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time

    Log.d("DateDebug", "Сегодня начало: $todayStart, конец: $todayEnd")

    // Создаём тестовое событие на сегодня
    val testEvent = Event(
        id = 100,
        title = "Тестовое событие",
        description = "Тестирование отображения событий",
        startTime = Date(),
        endTime = Date(System.currentTimeMillis() + 3600000),  // через час
        location = "Тестовая локация",
        status = 0,
        createdBy = 1,
        createdAt = Date()
    )

    Log.d("DateDebug", "Тестовое событие: время начала=${testEvent.startTime}")
    val isToday = testEvent.startTime.time >= todayStart.time &&
            testEvent.startTime.time <= todayEnd.time
    Log.d("DateDebug", "Событие сегодня? $isToday")
}