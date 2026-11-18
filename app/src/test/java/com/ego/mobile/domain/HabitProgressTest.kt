package com.ego.mobile.domain

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitProgressTest {
    @Test
    fun `weekly count accepts any days and uses ISO week boundaries`() {
        val habit = habit(HabitFrequency.TimesPerWeek(3))
        val today = LocalDate.parse("2026-11-20") // Friday
        val events = listOf(
            occurrence("2026-11-16"),
            occurrence("2026-11-18"),
            occurrence("2026-11-19"),
            occurrence("2026-11-15"), // Sunday from the previous ISO week
        )

        val progress = HabitProgress.calculate(habit, events, today)
        assertEquals(3, progress.completedThisWeek)
        assertEquals("3/3", progress.label)
        assertTrue(progress.isWeeklyGoalReached)
        assertFalse(progress.canCompleteToday)
    }

    @Test
    fun `weekly goal one remains eligible on Sunday after a Monday completion`() {
        val habit = habit(HabitFrequency.TimesPerWeek(2))
        val progress = HabitProgress.calculate(habit, listOf(occurrence("2026-11-16")), LocalDate.parse("2026-11-22"))
        assertEquals(1, progress.completedThisWeek)
        assertFalse(progress.isWeeklyGoalReached)
        assertTrue(progress.canCompleteToday)
    }

    @Test
    fun `daily completion and paused status are reflected in eligibility`() {
        val today = LocalDate.parse("2026-11-18")
        val daily = habit(HabitFrequency.Daily)
        val completed = HabitProgress.calculate(daily, listOf(occurrence(today.toString())), today)
        assertTrue(completed.completedToday)
        assertFalse(completed.canCompleteToday)
        assertFalse(HabitProgress.calculate(daily.copy(status = HabitStatus.PAUSED), emptyList(), today).canCompleteToday)
    }

    private fun habit(frequency: HabitFrequency) = Habit(
        id = 4L,
        name = "Treino",
        frequency = frequency,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun occurrence(date: String) = HabitOccurrence(
        habitId = 4L,
        date = LocalDate.parse(date),
        status = HabitOccurrenceStatus.COMPLETED,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
