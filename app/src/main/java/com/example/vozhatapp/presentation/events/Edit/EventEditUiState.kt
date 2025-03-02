package com.example.vozhatapp.presentation.events.Edit

import java.time.LocalDate
import java.time.LocalTime

data class EventEditUiState(
    // Идентификатор события (null для нового события)
    val id: Long? = null,

    // Основные поля события
    val title: String = "",
    val titleError: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val endDate: LocalDate = LocalDate.now(),
    val endTime: LocalTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0),
    val dateTimeError: String? = null,
    val location: String? = null,
    val description: String? = null,
    val status: Int = 0,

    // Данные о создании
    val createdAt: Long? = null,
    val createdBy: Long? = null,

    // Состояние UI
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null,
    val savedEventId: Long? = null,

    // Состояние диалогов выбора даты и времени
    val showStartDatePicker: Boolean = false,
    val showStartTimePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    val showEndTimePicker: Boolean = false
)