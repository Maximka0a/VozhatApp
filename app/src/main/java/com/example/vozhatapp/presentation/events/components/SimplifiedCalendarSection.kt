package com.example.vozhatapp.presentation.events.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vozhatapp.data.local.entity.Event
import com.example.vozhatapp.presentation.events.utils.formatDate
import com.example.vozhatapp.presentation.events.utils.generateWeekDates
import com.example.vozhatapp.presentation.events.utils.hasEventsOnDay

import java.time.*
import java.time.format.TextStyle
import java.util.*

@Composable
fun SimplifiedCalendarSection(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<Event>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Calendar header with month/year and expand toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedDate.month
                    .getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
                    .capitalize() + " " + selectedDate.year,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        onDateSelected(LocalDate.now())
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Сегодня"
                    )
                }

                IconButton(
                    onClick = {
                        onExpandToggle()
                    }
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Свернуть" else "Развернуть"
                    )
                }
            }
        }

        if (isExpanded) {
            // Simplified month calendar view
            SimpleMonthCalendar(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                events = events
            )
        } else {
            // Week strip view
            val visibleDates = generateWeekDates(selectedDate)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(visibleDates) { date ->
                    val hasEvents = hasEventsOnDay(date, events)
                    DayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        hasEvents = hasEvents,
                        onClick = { onDateSelected(date) }
                    )
                }
            }

            // Current date events count
            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "События на ${formatDate(selectedDate)}: ${events.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleMonthCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<Event>
) {
    // Get first day of month and calculate first day to display
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val dayOfWeek = firstDayOfMonth.dayOfWeek.value
    val daysInMonth = selectedDate.month.length(selectedDate.isLeapYear)

    // Display week days header
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp)) {
        val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        for (day in daysOfWeek) {
            Text(
                text = day,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    // Generate dates for the calendar grid
    val dates = mutableListOf<LocalDate?>()

    // Add empty spaces for days before the first day of month
    for (i in 1 until dayOfWeek) {
        dates.add(null)
    }

    // Add days of the month
    for (i in 1..daysInMonth) {
        dates.add(selectedDate.withDayOfMonth(i))
    }

    // Calculate number of rows needed
    val rows = (dates.size + 6) / 7

    // Ensure we have enough vertical space for the calendar
    Column(modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = (rows * 50).dp) // Ensure minimum height based on number of rows
        .padding(horizontal = 16.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp), // Fixed height for rows
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    if (index < dates.size) {
                        val date = dates[index]
                        if (date != null) {
                            val hasEvents = hasEventsOnDay(date, events)
                            SimpleDayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                hasEvents = hasEvents,
                                onClick = { onDateSelected(date) }
                            )
                        } else {
                            // Empty space for days before/after month
                            Box(modifier = Modifier.size(44.dp))
                        }
                    } else {
                        Box(modifier = Modifier.size(44.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun DayItem(
    date: LocalDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(55.dp)
            .height(80.dp)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isToday) 2.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = dayOfWeek,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (hasEvents) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}


