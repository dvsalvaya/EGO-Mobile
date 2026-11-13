package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitOccurrence
import java.time.LocalDate

class UpdateHabitFrequencyUseCase {
    data class Result(
        val habit: Habit,
        val effectiveDate: LocalDate,
        val preservedOccurrencesCount: Int,
        val preservedHistoricalDates: Set<LocalDate>,
    )

    operator fun invoke(
        habit: Habit,
        newFrequency: com.ego.mobile.domain.model.HabitFrequency,
        effectiveDate: LocalDate,
        existingOccurrences: List<HabitOccurrence>,
    ): Result {
        val preservedHistoricalDates = existingOccurrences
            .filter { it.date.isBefore(effectiveDate) }
            .map { it.date }
            .toSet()

        val updatedHabit = habit.copy(
            frequency = newFrequency,
            updatedAt = System.currentTimeMillis(),
        )

        return Result(
            habit = updatedHabit,
            effectiveDate = effectiveDate,
            preservedOccurrencesCount = preservedHistoricalDates.size,
            preservedHistoricalDates = preservedHistoricalDates,
        )
    }
}
