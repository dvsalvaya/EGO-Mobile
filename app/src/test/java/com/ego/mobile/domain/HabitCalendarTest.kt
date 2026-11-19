package com.ego.mobile.domain

import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitPlan
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.usecase.history
import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitCalendarTest {
    private val monday = LocalDate.of(2025, 12, 29)
    private val habit = Habit(id = 1, name = "Ler", frequency = HabitFrequency.Daily, createdAt = 1, updatedAt = 1)

    @Test
    fun weeklyGoalWithoutPreferencesClosesAtIsoMondayAcrossNewYear() {
        val plan = HabitPlan(monday, HabitFrequency.TimesPerWeek(3), HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit, listOf(plan), emptyList())
        val sunday = monday.plusDays(6)
        val open = calendar.history(sunday).single()
        assertEquals("Em andamento", open.status)
        assertEquals(emptyList<LocalDate>(), calendar.days(monday, sunday).filter { it.expected }.map { it.date })
        val rows = calendar.history(sunday.plusDays(1))
        assertEquals(listOf("Meta não atingida", "Em andamento"), rows.map { it.status })
        assertEquals(listOf(monday, monday.plusWeeks(1)), rows.map { it.habitPeriod!!.start })
        assertEquals("0 de 3", rows.first().subtitle)
    }

    @Test
    fun midweekEditsAndPauseKeepPreviousDailyAndWeeklyExpectations() {
        val friday = monday.plusDays(4)
        val calendar = HabitCalendar(habit, listOf(
            HabitPlan(monday, HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY)), HabitStatus.ACTIVE),
            HabitPlan(monday.plusDays(2), HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.WEDNESDAY)), HabitStatus.ACTIVE),
            HabitPlan(friday, HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)), HabitStatus.ACTIVE),
            HabitPlan(friday.plusDays(1), HabitFrequency.Daily, HabitStatus.PAUSED),
        ), emptyList())
        val expected = calendar.days(monday.minusDays(3), monday.plusDays(6)).filter { it.expected }
        assertEquals(listOf(monday, monday.plusDays(2), friday), expected.map { it.date })
        assertEquals(emptyList<Any>(), calendar.history(monday))
        val closed = calendar.history(monday.plusWeeks(1))
        assertEquals(listOf("Não fiz", "Meta não atingida"), closed.map { it.status })
    }

    @Test
    fun weeklyGoalRoundTripsItsPreferencesAndStillReadsLegacyGoals() {
        val encoded = "TIMES_PER_WEEK:3:MONDAY,WEDNESDAY,FRIDAY"
        assertEquals(encoded, HabitFrequency.fromPersistedValue(encoded).toPersistedValue())
        assertEquals(HabitFrequency.TimesPerWeek(3), HabitFrequency.fromPersistedValue("TIMES_PER_WEEK:3"))
    }

    // -------------------------------------------------------------------------
    // TASK-13: Fechar períodos, preservar previsões e corrigir histórico
    // -------------------------------------------------------------------------

    /**
     * Preferred days count as predictions only while the weekly goal is still open.
     * After reaching the target, later preferred days are NOT predicted.
     */
    @Test
    fun preferredDaysAreNotPredictedAfterWeeklyGoalIsReached() {
        val frequency = HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        // Complete on Monday and Wednesday → target reached → Friday should NOT be expected
        val occ = listOf(
            HabitOccurrence(1, 1, monday, HabitOccurrenceStatus.COMPLETED, createdAt = 1, updatedAt = 1),
            HabitOccurrence(2, 1, monday.plusDays(2), HabitOccurrenceStatus.COMPLETED, createdAt = 1, updatedAt = 1),
        )
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), occ)

        val days = calendar.days(monday, monday.plusDays(6))
        // Monday and Wednesday: expected = true (goal not yet reached at that point)
        assertTrue(days.single { it.date == monday }.expected)
        assertTrue(days.single { it.date == monday.plusDays(2) }.expected)
        // Friday: goal was already reached → NOT expected
        val friday = days.firstOrNull { it.date == monday.plusDays(4) }
        assertTrue("Friday should not be expected after goal reached", friday == null || !friday.expected)
    }

    /**
     * A correction beyond the weekly goal is accepted and reflected in the history subtitle.
     */
    @Test
    fun correctionBeyondWeeklyGoalAcceptedAndReflectedInHistory() {
        val frequency = HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        // Three completions with target=2 → "3 de 2"
        val occ = listOf(
            HabitOccurrence(1, 1, monday, HabitOccurrenceStatus.COMPLETED, createdAt = 1, updatedAt = 1),
            HabitOccurrence(2, 1, monday.plusDays(2), HabitOccurrenceStatus.COMPLETED, createdAt = 1, updatedAt = 1),
            HabitOccurrence(3, 1, monday.plusDays(4), HabitOccurrenceStatus.COMPLETED, createdAt = 1, updatedAt = 1),
        )
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), occ)

        val nextMonday = monday.plusWeeks(1)
        val rows = calendar.history(nextMonday)
        val completedWeek = rows.single { it.habitPeriod?.start == monday }
        assertEquals("Meta atingida", completedWeek.status)
        assertEquals("3 de 2", completedWeek.subtitle)
    }

    /**
     * A weekly goal without preferred days never generates daily predictions.
     */
    @Test
    fun weeklyGoalWithoutPreferredDaysProducesNoDailyPredictions() {
        val frequency = HabitFrequency.TimesPerWeek(3) // no preferred days
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), emptyList())

        val days = calendar.days(monday, monday.plusDays(6))
        // WeeklyTarget days are always included in days(), but expected is false for all
        assertTrue("No expected daily predictions", days.all { !it.expected })
    }

    /**
     * Day boundary: a daily habit has a "Não fiz" row for each elapsed date without completion.
     */
    @Test
    fun dailyHabitClosesEachPastDayAsMissedWhenNoCompletion() {
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit, listOf(plan), emptyList())
        val sunday = monday.plusDays(6)

        val rows = calendar.history(sunday)

        // All 6 elapsed days (Mon-Sat) should be "Não fiz"; Sunday is still open → 0 rows from open day
        assertEquals(6, rows.size)
        assertTrue(rows.all { it.status == "Não fiz" })
        // Distinct dates
        assertEquals(6, rows.map { it.occurredAt }.distinct().size)
    }

    /**
     * Frequency change mid-week: old plan's expected days are preserved, new plan applies from effectiveFrom.
     */
    @Test
    fun frequencyChangeMidWeekPreservesOldPredictions() {
        val friday = monday.plusDays(4)
        val calendar = HabitCalendar(
            habit,
            listOf(
                HabitPlan(monday, HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)), HabitStatus.ACTIVE),
                HabitPlan(friday, HabitFrequency.Daily, HabitStatus.ACTIVE),
            ),
            emptyList(),
        )

        val days = calendar.days(monday, monday.plusDays(6))
        val expectedDates = days.filter { it.expected }.map { it.date }
        // Mon and Wed from old plan, Fri-Sun from new plan
        assertEquals(listOf(monday, monday.plusDays(2), friday, friday.plusDays(1), friday.plusDays(2)), expectedDates)
    }
}
