package com.ego.mobile.domain

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitProgressRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HabitProgressRulesTest {

    @Test
    fun `week bounds use ISO monday to sunday`() {
        assertEquals(LocalDate.of(2026, 11, 16) to LocalDate.of(2026, 11, 22), HabitProgressRules.weekBounds(LocalDate.of(2026, 11, 16)))
        assertEquals(LocalDate.of(2026, 11, 16) to LocalDate.of(2026, 11, 22), HabitProgressRules.weekBounds(LocalDate.of(2026, 11, 22)))
        assertEquals(LocalDate.of(2026, 11, 23) to LocalDate.of(2026, 11, 29), HabitProgressRules.weekBounds(LocalDate.of(2026, 11, 23)))
    }

    @Test
    fun `times per week counts completions on any days and blocks after target`() {
        val habit = habit(id = 1L, frequency = HabitFrequency.TimesPerWeek(3))
        val dates = listOf(
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 11, 19),
            LocalDate.of(2026, 11, 22),
        )

        val partial = HabitProgressRules.progressFor(habit, occurrences(1L, dates.take(2)), LocalDate.of(2026, 11, 20))
        assertEquals(2, partial.completed)
        assertEquals(3, partial.target)
        assertEquals("2/3", partial.label)
        assertTrue(partial.canComplete)

        val complete = HabitProgressRules.progressFor(habit, occurrences(1L, dates), LocalDate.of(2026, 11, 22))
        assertEquals(3, complete.completed)
        assertTrue(complete.isComplete)
        assertEquals("3/3", complete.label)
        assertFalse(complete.canComplete)
    }

    @Test
    fun `weekly count restarts in the next week without discarding history`() {
        val habit = habit(id = 1L, frequency = HabitFrequency.TimesPerWeek(3))
        val previousWeek = occurrences(1L, listOf(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 17)))

        val progress = HabitProgressRules.progressFor(habit, previousWeek, LocalDate.of(2026, 11, 23))

        assertEquals(0, progress.completed)
        assertEquals("0/3", progress.label)
        assertTrue(progress.canComplete)
    }

    @Test
    fun `daily habit reports completion of today`() {
        val habit = habit(id = 2L, frequency = HabitFrequency.Daily)
        val today = LocalDate.of(2026, 11, 16)

        val pending = HabitProgressRules.progressFor(habit, emptyList(), today)
        assertFalse(pending.isCompletedToday)
        assertTrue(pending.canComplete)

        val done = HabitProgressRules.progressFor(habit, occurrences(2L, listOf(today)), today)
        assertTrue(done.isCompletedToday)
        assertEquals("Concluído hoje", done.label)
        assertFalse(done.canComplete)
    }

    private fun habit(id: Long, frequency: HabitFrequency) = Habit(
        id = id,
        name = "Hábito $id",
        frequency = frequency,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun occurrences(habitId: Long, dates: List<LocalDate>) = dates.map { date ->
        HabitOccurrence(
            id = date.toEpochDay(),
            habitId = habitId,
            date = date,
            status = HabitOccurrenceStatus.COMPLETED,
            completedAt = 1L,
            createdAt = 1L,
            updatedAt = 1L,
        )
    }
}
