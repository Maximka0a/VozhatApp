package com.example.vozhatapp.presentation.home.model

import java.util.Date

data class ReminderItem(
    val id: Long,
    val title: String,
    val description: String,
    val date: Date
)