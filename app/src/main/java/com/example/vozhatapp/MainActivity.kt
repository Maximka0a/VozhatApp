package com.example.vozhatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vozhatapp.presentation.home.HomeScreen
import com.example.vozhatapp.ui.theme.VozhatAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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