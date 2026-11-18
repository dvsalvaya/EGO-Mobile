package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.model.HabitDay
import com.ego.mobile.domain.model.HabitPeriod
import com.ego.mobile.domain.model.HistoryItem
import com.ego.mobile.domain.model.habitZone
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

internal fun habitDates(clock: Clock = Clock.system(habitZone)) = flow {
    while (true) {
        val now = clock.instant()
        val today = now.atZone(clock.zone).toLocalDate()
        emit(today)
        val midnight = today.plusDays(1).atStartOfDay(clock.zone).toInstant()
        delay(Duration.between(clock.instant(), midnight).toMillis().coerceAtLeast(1L))
    }
}

internal fun HabitCalendar.history(today: LocalDate): List<HistoryItem> {
    val start = firstDate ?: return emptyList()
    val weeks = days(start, today).groupBy { it.date.with(DayOfWeek.MONDAY) }
    return buildList {
        for ((monday, week) in weeks) {
            for (day in week.filter { it.weeklyTarget == null }) {
                if (!day.completed && !day.missedOn(today)) continue
                add(HistoryItem(
                    occurredAt = day.occurrence?.completedAt ?: day.date.startMillis(),
                    type = ActivityType.HABIT, title = habit.name, subtitle = day.occurrence?.notes,
                    status = if (day.completed) "COMPLETED" else "Não fiz",
                    habitPeriod = HabitPeriod(habit.id, day.date, null, listOf(day), today),
                ))
            }
            val weeklyDays = week.filter { it.weeklyTarget != null }
            val target = weeklyDays.lastOrNull()?.weeklyTarget ?: continue
            val count = week.count(HabitDay::completed)
            add(HistoryItem(
                occurredAt = weeklyDays.last().date.startMillis(),
                type = ActivityType.HABIT, title = habit.name, subtitle = "$count de $target",
                status = when {
                    count >= target -> "Meta atingida"
                    monday.plusWeeks(1) <= today -> "Meta não atingida"
                    else -> "Em andamento"
                },
                habitPeriod = HabitPeriod(habit.id, monday, target, weeklyDays, today),
            ))
        }
    }
}

private fun LocalDate.startMillis(): Long = atStartOfDay(habitZone).toInstant().toEpochMilli()
