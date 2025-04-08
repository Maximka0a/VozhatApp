package com.example.vozhatapp.presentation.notes.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun CalendarView(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateSelected: (Int, Int, Int) -> Unit
) {
    // Simplified calendar implementation
    Text(
        text = "Выбрана дата: $selectedDay/${selectedMonth + 1}/$selectedYear",
        modifier = Modifier.padding(bottom = 8.dp)
    )

    // Year selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Год:", modifier = Modifier.width(50.dp))

        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { year ->
                    if (year in 2020..2100) {
                        onDateSelected(year, selectedMonth, selectedDay)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Month selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Месяц:", modifier = Modifier.width(50.dp))

        val monthNames = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

        OutlinedTextField(
            value = monthNames[selectedMonth],
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            val newMonth = if (selectedMonth > 0) selectedMonth - 1 else 11
                            onDateSelected(selectedYear, newMonth, selectedDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий")
                    }

                    IconButton(
                        onClick = {
                            val newMonth = if (selectedMonth < 11) selectedMonth + 1 else 0
                            onDateSelected(selectedYear, newMonth, selectedDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий")
                    }
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Day selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("День:", modifier = Modifier.width(50.dp))

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        OutlinedTextField(
            value = selectedDay.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { day ->
                    if (day in 1..daysInMonth) {
                        onDateSelected(selectedYear, selectedMonth, day)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            val newDay = if (selectedDay > 1) selectedDay - 1 else daysInMonth
                            onDateSelected(selectedYear, selectedMonth, newDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий")
                    }

                    IconButton(
                        onClick = {
                            val newDay = if (selectedDay < daysInMonth) selectedDay + 1 else 1
                            onDateSelected(selectedYear, selectedMonth, newDay)
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий")
                    }
                }
            }
        )
    }
}

@Composable
fun TimePickerView(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    // Simple time picker
    Text(
        text = "Выбрано время: ${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hour picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Часы")

            IconButton(
                onClick = {
                    val newHour = if (selectedHour < 23) selectedHour + 1 else 0
                    onTimeSelected(newHour, selectedMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить")
            }

            Text(
                text = selectedHour.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(
                onClick = {
                    val newHour = if (selectedHour > 0) selectedHour - 1 else 23
                    onTimeSelected(newHour, selectedMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить")
            }
        }

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Minute picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Минуты")

            IconButton(
                onClick = {
                    val newMinute = if (selectedMinute < 59) selectedMinute + 1 else 0
                    onTimeSelected(selectedHour, newMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Увеличить")
            }

            Text(
                text = selectedMinute.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(
                onClick = {
                    val newMinute = if (selectedMinute > 0) selectedMinute - 1 else 59
                    onTimeSelected(selectedHour, newMinute)
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Уменьшить")
            }
        }
    }
}