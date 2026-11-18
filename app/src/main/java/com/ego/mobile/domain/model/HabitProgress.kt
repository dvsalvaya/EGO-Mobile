package com.ego.mobile.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class HabitProgress(
    val completedThisWeek: Int,
    val weeklyTarget: Int?,
    val completedToday: Boolean,
    val canCompleteToday: Boolean,
) {
    val isWeeklyGoalReached: Boolean
        get() = weeklyTarget != null && completedThisWeek >= weeklyTarget

    val label: String?
        get() = weeklyTarget?.let { "$completedThisWeek/$it" }

    companion object {
        fun calculate(habit: Habit, occurrences: List<HabitOccurrence>, today: LocalDate): HabitProgress {
            val monday = today.with(DayOfWeek.MONDAY)
            val sunday = today.with(DayOfWeek.SUNDAY)
            val completedThisWeek = occurrences.count {
                it.status == HabitOccurrenceStatus.COMPLETED && it.date in monday..sunday
            }
            val completedToday = occurrences.any {
                it.date == today && it.status == HabitOccurrenceStatus.COMPLETED
            }
            val weeklyTarget = (habit.frequency as? HabitFrequency.TimesPerWeek)?.count
            val canComplete = habit.status == HabitStatus.ACTIVE && !completedToday &&
                habit.frequency.requiresOccurrenceOn(today) &&
                (weeklyTarget == null || completedThisWeek < weeklyTarget)
            return HabitProgress(completedThisWeek, weeklyTarget, completedToday, canComplete)
        }
    }
}
