package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class UpdateHabitFrequencyUseCaseTest {

    @Test
    fun `updates frequency from the chosen effective date and preserves past history`() {
        val habit = Habit(
            name = "Leitura",
            frequency = HabitFrequency.Daily,
            createdAt = 1_000L,
            updatedAt = 1_000L,
        )

        val occurrences = listOf(
            HabitOccurrence(
                habitId = 42L,
                date = LocalDate.of(2026, 10, 26),
                status = HabitOccurrenceStatus.COMPLETED,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
            HabitOccurrence(
                habitId = 42L,
                date = LocalDate.of(2026, 10, 27),
                status = HabitOccurrenceStatus.PENDING,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
            HabitOccurrence(
                habitId = 42L,
                date = LocalDate.of(2026, 10, 28),
                status = HabitOccurrenceStatus.PENDING,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )

        val newFrequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        val effectiveDate = LocalDate.of(2026, 10, 28)

        val result = UpdateHabitFrequencyUseCase().invoke(
            habit = habit,
            newFrequency = newFrequency,
            effectiveDate = effectiveDate,
            existingOccurrences = occurrences,
        )

        assertEquals(newFrequency, result.habit.frequency)
        assertEquals(effectiveDate, result.effectiveDate)
        assertEquals(2, result.preservedOccurrencesCount)
        assertEquals(2, result.preservedHistoricalDates.size)
    }
}
