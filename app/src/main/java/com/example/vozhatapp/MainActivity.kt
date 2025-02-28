package com.example.vozhatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vozhatapp.presentation.home.HomeScreen
import com.example.vozhatapp.ui.theme.VozhatAppTheme
import dagger.hilt.android.AndroidEntryPoint
import android.os.SystemClock

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val currentTime = System.currentTimeMillis() // Получаем текущее время в миллисекундах
        Log.d("MainActivity", "Current time: $currentTime")

        setContent {
            VozhatAppTheme {
                HomeScreen(
                    onNavigateToEvents = {},
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
