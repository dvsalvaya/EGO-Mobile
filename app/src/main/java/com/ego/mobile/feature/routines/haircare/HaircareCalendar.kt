package com.ego.mobile.feature.routines.haircare

import androidx.compose.foundation.Column
import androidx.compose.foundation.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.app.theme.Colors
import com.ego.mobile.app.theme.Spacing
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircarePresetStep
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.model.HaircarePreset
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import kotlin.math.abs

@Composable
fun HaircareCalendar(
    routine: HaircareRoutine?,
    onStepClick: (HaircareOccurrence) -> Unit,
    onRecordExecution: (Long) -> Unit,
    onReschedule: (HaircareOccurrence) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarState = rememberCalendarState(routine)

    CalendarGrid(
        state = calendarState,
        onDayClick = { day ->
            val occurrences = calendarState.occurrencesForDay(day)
            when {
                occurrences.isEmpty() -> {
                    // Nothing for this day
                }
                occurrences.first().status == HaircareOccurrenceStatus.PENDING ||
                    occurrences.first().status == HaircareOccurrenceStatus.RESCHEDULED -> {
                    onStepClick(occurrences.first())
                }
                occurrences.first().status == HaircareOccurrenceStatus.COMPLETED -> {
                    // Show completed info
                }
            }
        },
        onNavigatePrevious = { calendarState.navigatePrevious() },
        onNavigateNext = { calendarState.navigateNext() },
    )

    // Navigation buttons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(onClick = { onNavigatePrevious() }) {
            Text("Mês anterior")
        }
        Text(
            text = calendarState.formattedMonthYear,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
        )
        OutlinedButton(onClick = { onNavigateNext() }) {
            Text("Próximo mês")
        }
    }
}

@Composable
private fun CalendarGrid(
    state: CalendarState,
    onDayClick: (LocalDate?) -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    val saoPaulo = ZoneId.of("America/Sao_Paulo")
    val now = LocalDate.now(saoPaulo)
    val firstDayOfMonth = now.withDayOfMonth(1)
    val lastDayOfMonth = now.withDayOfMonth(1).plusMonths(1).minusDays(1)
    val totalDaysInMonth = lastDayOfMonth.dayOfMonth
    val firstDayWeekDay = firstDayOfMonth.dayOfWeek.value % 7 // 0=Sunday, convert to Monday-start
    val daysInPrevMonth = firstDayOfMonth.minusDays(1).dayOfMonth()

    // Build the grid days
    val gridDays = mutableListOf<CalendarDay>()

    // Days from previous month to fill starting positions
    for (day in 1..firstDayWeekDay) {
        gridDays.add(CalendarDay(
            date = firstDayOfMonth.minusDays(firstDayWeekDay - day),
            occurrences = null,
            isCurrentMonth = false,
        ))
    }

    // Days of current month
    for (day in 1..totalDaysInMonth) {
        val localDate = firstDayOfMonth.plusDays(day - 1)
        val occurrences = state.occurrencesForDate(localDate)
        gridDays.add(CalendarDay(
            date = localDate,
            occurrences = occurrences,
            isCurrentMonth = true,
        ))
    }

    // Days from next month to fill ending positions
    val totalCells = 42 // 6 weeks * 7 days
    val remaining = totalCells - gridDays.size
    for (day in 1..remaining) {
        gridDays.add(CalendarDay(
            date = firstDayOfMonth.plusMonths(1).plusDays(day - 1),
            occurrences = null,
            isCurrentMonth = false,
        ))
    }

    // Grid header
    Column(modifier = Modifier.padding(Spacing.lg)) {
        // Week day headers
        Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            val weekDays = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = Colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Grid days
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            items(gridDays) { calendarDay ->
                CalendarDayItem(
                    day = calendarDay,
                    onClick = { onDayClick(calendarDay.date) },
                    state = state,
                )
            }
        }
    }
}

private data class CalendarDay(
    val date: LocalDate,
    val occurrences: List<HaircareOccurrence>?,
    val isCurrentMonth: Boolean
)

private data class CalendarState(
    routine: HaircareRoutine?,
    var dayOffset: Int = 0, // 0 = current month, -1 = previous, 1 = next
) {
    val formattedMonthYear get() {
        val targetDate = when (dayOffset) {
            0 -> LocalDate.now(ZoneId.of("America/Sao_Paulo"))
            -1 -> LocalDate.now(ZoneId.of("America/Sao_Paulo")).minusMonths(1)
            1 -> LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusMonths(1)
            else -> LocalDate.now(ZoneId.of("America/Sao_Paulo"))
        }
        return "@${targetDate.format(java.time.format.DateFormat.monthYearDateDurationTimer)}"
    }

    val occurrencesForDate: (LocalDate) -> List<HaircareOccurrence>
        get() { date ->
            routine?.occurrences?.filter { it.scheduledDate == date.toString() } ?: emptyList()
        }
}

private fun navigatePrevious() { /* */ }
private fun navigateNext() { /* */ }

@Composable
private fun CalendarDayItem(
    calendarDay: CalendarDay,
    onDayClick: (LocalDate?) -> Unit,
    state: CalendarState,
) {
    val isSelected = calendarDay.date?.isSameDayAs(LocalDate.now(ZoneId.of("America/Sao_Paulo"))) == true &&
        calendarDay.isCurrentMonth

    // Determine badge info
    val badgeText = when {
        calendarDay.occurrences.isEmpty() -> ""
        calendarDay.occurrences.size == 1 -> calendarDay.occurrences.size.toString()
        else -> calendarDay.occurrences.size.toString()
    }

    val badgeColor = when {
        calendarDay.occurrences.isEmpty() -> Colors.onSurfaceVariant
        calendarDay.occurrences.first().status == HaircareOccurrenceStatus.PENDING -> Colors.primary
        calendarDay.occurrences.first().status == HaircareOccurrenceStatus.RESCHEDULED -> Colors.primaryContainer
        else -> Colors.onSurface
    }

    CalendarDayContainer(
        date = calendarDay.date?.dayOfMonth ?: "",
        isCurrentMonth = calendarDay.isCurrentMonth,
        isToday = isSelected,
        occurrences = calendarDay.occurrences,
        badgeText = badgeText,
        badgeColor = badgeColor,
        onClick = { onDayClick(calendarDay.date) },
    )
}

@Composable
private fun CalendarDayContainer(
    date: String,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    occurrences: List<HaircareOccurrence>?,
    badgeText: String,
    badgeColor: android.graphics.Color,
    onClick: () -> Unit,
) {
    val chipColor = when {
        isToday && occurrences.isNotEmpty() -> badgeColor
        isCurrentMonth && occurrences.isNotEmpty() -> badgeColor.copy(alpha = 0.1f)
        else -> Colors.surface
    }

    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(Spacing.xs),
        backgroundColor = chipColor,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterIn,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isToday && occurrences.isNotEmpty() -> Colors.onPrimary
                        isCurrentMonth && occurrences.isNotEmpty() -> Colors.onSurface
                        else -> Colors.onSurfaceVariant
                    },
                )
                if (occurrences.isNotEmpty()) {
                    Chip(
                        modifier = Modifier.padding(Spacing.xs),
                        backgroundColor = badgeColor,
                        label = {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.caption,
                                color = when (badgeColor) {
                                    isColors.onPrimary -> Colors.onPrimary
                                    else -> Colors.onPrimaryContainer
                                },
                            )
                        },
                    )
                } else {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.onSurfaceVariant,
                    )
                }
            }
        }
    )
}