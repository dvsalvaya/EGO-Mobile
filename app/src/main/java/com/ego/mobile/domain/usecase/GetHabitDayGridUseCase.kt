package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.HabitDayGrid
import com.ego.mobile.domain.model.HabitGridDay
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.Clock

/**
 * Provides the seven-day habit grid for the Home screen (and for TASK-04).
 *
 * For each of the last [HabitDayGrid.DAY_COUNT] civil dates ending today, the use case
 * aggregates—across all habit calendars—the number of predicted and completed occurrences.
 *
 * Rules (spec 8.1 / TASK-13):
 * - A day is *predicted* for a habit if [com.ego.mobile.domain.model.HabitCalendar.days]
 *   marks it as `expected`.
 * - [com.ego.mobile.domain.model.HabitFrequency.TimesPerWeek] without preferred days never
 *   marks any day as expected → contributes 0 to [HabitGridDay.predicted] for the grid.
 * - Once a weekly goal is reached, later preferred days within the same week are no longer
 *   expected → also contribute 0.
 * - [HabitGridDay.completed] counts habits with a COMPLETED occurrence on that date,
 *   regardless of prediction (a correction beyond the goal still counts).
 * - Today is marked [HabitGridDay.isToday] = true.
 *
 * The [dates] flow drives automatic updates when the civil date changes (day boundary).
 * Pass a fixed [today] lambda (or override [dates]) in tests.
 */
class GetHabitDayGridUseCase(
    private val habitRepository: HabitRepository,
    private val dates: Flow<LocalDate> = habitDates(),
) {
    /** Production use can inject a clock; tests can pin the civil date deterministically. */
    constructor(
        habitRepository: HabitRepository,
        clock: Clock,
    ) : this(
        habitRepository = habitRepository,
        dates = habitDates(clock),
    )

    /**
     * Convenience constructor for tests: accepts a fixed [today] lambda and uses a
     * single-emission [dates] flow internally.
     */
    constructor(
        habitRepository: HabitRepository,
        today: () -> LocalDate,
    ) : this(
        habitRepository = habitRepository,
        dates = flowOf(today()),
    )

    operator fun invoke(): Flow<HabitDayGrid> = combine(
        habitRepository.observeHabitCalendars(),
        dates,
    ) { calendars, today ->
        val lastDay = today
        val firstDay = lastDay.minusDays((HabitDayGrid.DAY_COUNT - 1).toLong())

        // Accumulate predicted and completed counts per civil date.
        // HabitCalendar.days() already handles:
        //   - plan-based expected flag per date (respects status)
        //   - TimesPerWeek goal tracking (expected = false after target reached)
        //   - TimesPerWeek without preferredDays (expected = false always)
        //   - Paused/archived plans (expected = false for inactive plans)
        val predictedByDate = mutableMapOf<LocalDate, Int>()
        val completedByDate = mutableMapOf<LocalDate, Int>()

        for (calendar in calendars) {
            for (day in calendar.days(firstDay, lastDay)) {
                if (day.date < firstDay || day.date > lastDay) continue
                if (day.expected) predictedByDate[day.date] = (predictedByDate[day.date] ?: 0) + 1
                if (day.occurrence?.status == HabitOccurrenceStatus.COMPLETED) {
                    completedByDate[day.date] = (completedByDate[day.date] ?: 0) + 1
                }
            }
        }

        HabitDayGrid(
            days = (0L until HabitDayGrid.DAY_COUNT).map { offset ->
                val date = firstDay.plusDays(offset)
                HabitGridDay(
                    date = date,
                    predicted = predictedByDate[date] ?: 0,
                    completed = completedByDate[date] ?: 0,
                    isToday = date == lastDay,
                )
            },
        )
    }
}
