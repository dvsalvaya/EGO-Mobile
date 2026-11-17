package com.ego.mobile.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Progresso do hábito no período aplicável, usado por Home, Hábitos e conclusão rápida. */
data class HabitProgress(
    val habitId: Long,
    val completed: Int,
    val target: Int,
    val isComplete: Boolean,
    val isCompletedToday: Boolean,
    /** Rótulo curto para a UI: "Concluído hoje" ou "2/3". */
    val label: String,
    val canComplete: Boolean,
)

object HabitProgressRules {

    fun weekBounds(date: LocalDate): Pair<LocalDate, LocalDate> {
        val start = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return start to start.plusDays(6)
    }

    fun progressFor(
        habit: Habit,
        occurrences: List<HabitOccurrence>,
        today: LocalDate,
    ): HabitProgress {
        val completedToday = occurrences.any {
            it.date == today && it.status == HabitOccurrenceStatus.COMPLETED
        }
        val (weekStart, weekEnd) = weekBounds(today)

        val (completed, target) = when (val frequency = habit.frequency) {
            is HabitFrequency.TimesPerWeek -> {
                val inWeek = occurrences.count { occurrence ->
                    occurrence.status == HabitOccurrenceStatus.COMPLETED &&
                        occurrence.date in weekStart..weekEnd
                }
                inWeek to frequency.count
            }
            else -> {
                val value = if (completedToday) 1 else 0
                value to 1
            }
        }

        val isComplete = completed >= target
        return HabitProgress(
            habitId = habit.id,
            completed = completed,
            target = target,
            isComplete = isComplete,
            isCompletedToday = completedToday,
            label = if (isComplete && target == 1) {
                "Concluído hoje"
            } else {
                "$completed/$target"
            },
            canComplete = !isComplete,
        )
    }
}
