package com.example.vozhatapp.presentation.notes.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialDateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Текущая дата и время для проверки
    val currentTime = System.currentTimeMillis()

    // Если начальное время в прошлом, используем текущее + 5 минут
    val safeInitialTime = if (initialDateTimeMillis <= currentTime) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, 5)
        cal.timeInMillis
    } else {
        initialDateTimeMillis
    }

    // Инициализируем календарь с безопасным временем
    val calendar = Calendar.getInstance().apply {
        timeInMillis = safeInitialTime
    }

    // Состояния для даты и времени
    var dateSelection by remember { mutableStateOf(calendar.timeInMillis) }
    var showDatePicker by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Форматтеры для отображения даты и времени
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Выбранные часы и минуты для TimePicker
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    // Функция для проверки валидности даты/времени
    fun isValidDateTime(timeMillis: Long): Boolean {
        return timeMillis > currentTime
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (showDatePicker) "Выберите дату напоминания" else "Выберите время напоминания",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (showDatePicker) {
                    // Календарь для выбора даты
                    DatePicker(
                        state = rememberDatePickerState(
                            initialSelectedDateMillis = dateSelection,
                            selectableDates = object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                                    val today = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    return utcTimeMillis >= today.timeInMillis
                                }
                            }
                        ),
                        showModeToggle = false,
                        title = null,
                        headline = null,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Отображаем выбранную дату
                    val selectedDate = Calendar.getInstance().apply {
                        timeInMillis = dateSelection
                    }

                    Text(
                        text = "Выбрано: ${dateFormatter.format(Date(dateSelection))}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Отображение ошибки если есть
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Кнопки для навигации
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Отмена")
                        }

                        TextButton(onClick = {
                            showDatePicker = false
                            errorMessage = null

                            // Если выбран сегодняшний день, обновляем время
                            val today = Calendar.getInstance()
                            val selected = Calendar.getInstance().apply { timeInMillis = dateSelection }

                            val isToday = selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                                    selected.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

                            if (isToday) {
                                // Для текущего дня устанавливаем время на текущее + 5 минут
                                val futureTime = Calendar.getInstance().apply {
                                    add(Calendar.MINUTE, 5)
                                }
                                selectedHour = futureTime.get(Calendar.HOUR_OF_DAY)
                                selectedMinute = futureTime.get(Calendar.MINUTE)
                            }
                        }) {
                            Text("Далее")
                        }
                    }
                } else {
                    // Time picker screen
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Показываем выбранную дату
                        Text(
                            text = "Дата: ${dateFormatter.format(Date(dateSelection))}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TimePicker(
                            state = rememberTimePickerState(
                                initialHour = selectedHour,
                                initialMinute = selectedMinute
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // Отображение ошибки если есть
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Кнопки для навигации
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                showDatePicker = true
                                errorMessage = null
                            }) {
                                Text("Назад")
                            }

                            TextButton(
                                onClick = {
                                    // Создаем финальную дату
                                    val finalCalendar = Calendar.getInstance().apply {
                                        timeInMillis = dateSelection
                                        set(Calendar.HOUR_OF_DAY, selectedHour)
                                        set(Calendar.MINUTE, selectedMinute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    if (finalCalendar.timeInMillis <= currentTime) {
                                        errorMessage = "Нельзя выбрать прошедшее время"
                                        // Корректируем время, если оно в прошлом
                                        val validTime = Calendar.getInstance().apply {
                                            add(Calendar.MINUTE, 5)
                                        }
                                        selectedHour = validTime.get(Calendar.HOUR_OF_DAY)
                                        selectedMinute = validTime.get(Calendar.MINUTE)
                                    } else {
                                        onDateTimeSelected(finalCalendar.timeInMillis)
                                        onDismiss()
                                    }
                                }
                            ) {
                                Text("Готово")
                            }
                        }
                    }
                }
            }
        }
    }
}