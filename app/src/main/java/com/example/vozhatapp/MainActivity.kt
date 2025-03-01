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
import com.example.vozhatapp.presentation.childprofile.ChildProfileScreen
import com.example.vozhatapp.presentation.children.AddChildScreen
import com.example.vozhatapp.presentation.children.ChildrenListScreen

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
                    onNavigateToEvents = { TODO() },
                    onNavigateToChildren = { TODO() },
                    onNavigateToGames = { TODO() },
                    onNavigateToProfile = { TODO() },
                    onNavigateToChildDetails = { TODO() },
                    onNavigateToEventDetails = {},
                )
            }
        }
    }
}
