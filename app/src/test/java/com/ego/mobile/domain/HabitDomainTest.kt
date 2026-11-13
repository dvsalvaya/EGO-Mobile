package com.ego.mobile.domain

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HabitDomainTest {

    @Test
    fun `daily frequency marks the expected dates as due`() {
        val frequency = HabitFrequency.Daily

        assertEquals(true, frequency.requiresOccurrenceOn(LocalDate.of(2026, 10, 28)))
        assertEquals(true, frequency.requiresOccurrenceOn(LocalDate.of(2026, 10, 29)))
    }

    @Test
    fun `weekly days frequency only triggers on configured weekdays`() {
        val frequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY))

        assertEquals(true, frequency.requiresOccurrenceOn(LocalDate.of(2026, 10, 26)))
        assertEquals(false, frequency.requiresOccurrenceOn(LocalDate.of(2026, 10, 27)))
        assertEquals(true, frequency.requiresOccurrenceOn(LocalDate.of(2026, 10, 29)))
    }

    @Test
    fun `habit name validation rejects blank values`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Habit(
                name = "   ",
                frequency = HabitFrequency.Daily,
                createdAt = 1000L,
                updatedAt = 1000L,
            )
        }

        assertEquals("Habit name is required.", ex.message)
    }

    @Test
    fun `habit description is optional but limited to a safe size`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            Habit(
                name = "Leitura",
                description = "x".repeat(251),
                frequency = HabitFrequency.Daily,
                createdAt = 1000L,
                updatedAt = 1000L,
            )
        }

        assertEquals("Habit description must be 250 characters or less.", ex.message)
    }
}