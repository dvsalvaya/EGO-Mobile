package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitPlan
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import com.ego.mobile.domain.model.habitZone

/**
 * Domain tests for TASK-04: build a seven-day grid from TASK-13 habit calendars.
 *
 * These tests cover GetHabitDayGridUseCase which aggregates
 * HabitCalendar.days() per civil date into predicted/completed fractions, consumed by TASK-04.
 */
class HabitDayGridUseCaseTest {

    private val monday = LocalDate.of(2026, 11, 16)
    private val tuesday = monday.plusDays(1)
    private val wednesday = monday.plusDays(2)
    private val thursday = monday.plusDays(3)
    private val friday = monday.plusDays(4)
    private val saturday = monday.plusDays(5)
    private val sunday = monday.plusDays(6)

    private fun dailyHabit(id: Long, name: String = "H$id") = Habit(
        id = id, name = name, frequency = HabitFrequency.Daily, createdAt = 1L, updatedAt = 1L,
    )

    private fun completedOccurrence(habitId: Long, date: LocalDate) = HabitOccurrence(
        id = habitId * 100 + date.dayOfMonth,
        habitId = habitId,
        date = date,
        status = HabitOccurrenceStatus.COMPLETED,
        completedAt = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun useCase(calendars: List<HabitCalendar>, today: LocalDate) =
        GetHabitDayGridUseCase(
            habitRepository = FakeGridHabitRepository(calendars),
            today = { today },
        )

    // ---------------------------------------------------------------------------
    // 1. No habits: every day has 0 predicted and 0 completed
    // ---------------------------------------------------------------------------
    @Test
    fun `empty calendars produce seven days with zero predicted and zero completed`() = runTest {
        val grid = useCase(emptyList(), sunday).invoke().first()

        assertEquals(7, grid.days.size)
        assertEquals(monday, grid.days.first().date)
        assertEquals(sunday, grid.days.last().date)
        assertTrue(grid.days.all { it.predicted == 0 })
        assertTrue(grid.days.all { it.completed == 0 })
    }

    // ---------------------------------------------------------------------------
    // 2. Daily habit fully completed: 100% every day
    // ---------------------------------------------------------------------------
    @Test
    fun `daily habit fully completed produces predicted 1 and completed 1 for each day`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val occurrences = (0L..6L).map { offset -> completedOccurrence(1, monday.plusDays(offset)) }
        val calendar = HabitCalendar(habit, listOf(plan), occurrences)

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        grid.days.forEach { day ->
            assertEquals("predicted on ${day.date}", 1, day.predicted)
            assertEquals("completed on ${day.date}", 1, day.completed)
        }
    }

    // ---------------------------------------------------------------------------
    // 3. Daily habit with no completions
    // ---------------------------------------------------------------------------
    @Test
    fun `daily habit with no completions marks each day predicted but not completed`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit, listOf(plan), emptyList())

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        grid.days.forEach { day ->
            assertEquals("predicted on ${day.date}", 1, day.predicted)
            assertEquals("completed on ${day.date}", 0, day.completed)
        }
        assertTrue(grid.days.last().isToday)
        assertFalse(grid.days.first().isToday)
    }

    // ---------------------------------------------------------------------------
    // 4. WeeklyDays habit: only predicted on selected days
    // ---------------------------------------------------------------------------
    @Test
    fun `weekly-days habit only marks selected days as predicted`() = runTest {
        val habit = dailyHabit(1)
        val frequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), emptyList())

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        val predictedDates = grid.days.filter { it.predicted > 0 }.map { it.date }
        assertEquals(listOf(monday, wednesday, friday), predictedDates)
    }

    // ---------------------------------------------------------------------------
    // 5. TimesPerWeek with preferred days: after goal reached, later preferred
    //    days stop being predicted (ausência de previsões após atingir a meta)
    // ---------------------------------------------------------------------------
    @Test
    fun `times-per-week goal stops predicting after target is reached`() = runTest {
        val habit = dailyHabit(1)
        val frequency = HabitFrequency.TimesPerWeek(2, setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        // Complete on Monday and Wednesday → target reached → Friday should NOT be predicted
        val occurrences = listOf(
            completedOccurrence(1, monday),
            completedOccurrence(1, wednesday),
        )
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), occurrences)

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        // Monday and Wednesday: predicted and completed
        assertEquals(1, grid.days.single { it.date == monday }.predicted)
        assertEquals(1, grid.days.single { it.date == monday }.completed)
        assertEquals(1, grid.days.single { it.date == wednesday }.predicted)
        assertEquals(1, grid.days.single { it.date == wednesday }.completed)
        // Friday: goal was already reached → NOT predicted
        assertEquals(0, grid.days.single { it.date == friday }.predicted)
        // Other days (Tue, Thu, Sat, Sun): no prediction
        assertEquals(0, grid.days.single { it.date == tuesday }.predicted)
        assertEquals(0, grid.days.single { it.date == thursday }.predicted)
        assertEquals(0, grid.days.single { it.date == saturday }.predicted)
        assertEquals(0, grid.days.single { it.date == sunday }.predicted)
    }

    // ---------------------------------------------------------------------------
    // 6. TimesPerWeek WITHOUT preferred days: no daily prediction generated
    //    (metas semanais sem dias marcados não geram previsão diária para a grade)
    // ---------------------------------------------------------------------------
    @Test
    fun `times-per-week without preferred days generates no daily prediction`() = runTest {
        val habit = dailyHabit(1)
        val frequency = HabitFrequency.TimesPerWeek(3) // no preferred days
        val plan = HabitPlan(monday, frequency, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit.copy(frequency = frequency), listOf(plan), emptyList())

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        assertTrue("No daily prediction for open weekly goal without days", grid.days.all { it.predicted == 0 })
    }

    // ---------------------------------------------------------------------------
    // 7. Historical correction reflects in the grid
    // ---------------------------------------------------------------------------
    @Test
    fun `historical correction on a past day updates completed count for that date`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val fakeRepo = MutableGridHabitRepository(HabitCalendar(habit, listOf(plan), emptyList()))
        val useCase = GetHabitDayGridUseCase(fakeRepo, today = { sunday })

        // Before correction
        val before = useCase().first()
        assertEquals(0, before.days.single { it.date == monday }.completed)

        // After correction: mark Monday as completed
        fakeRepo.updateCalendar(
            HabitCalendar(habit, listOf(plan), listOf(completedOccurrence(1, monday)))
        )
        val after = useCase().first()
        assertEquals(1, after.days.single { it.date == monday }.completed)
        assertEquals(1, after.days.single { it.date == monday }.predicted)
    }

    // ---------------------------------------------------------------------------
    // 8. Two habits: fractions aggregate per day
    // ---------------------------------------------------------------------------
    @Test
    fun `two habits aggregate predicted and completed per day`() = runTest {
        val h1 = dailyHabit(1)
        val h2 = dailyHabit(2)
        val plan1 = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val plan2 = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        // h1 completed all week, h2 completed only monday
        val occ1 = (0L..6L).map { offset -> completedOccurrence(1, monday.plusDays(offset)) }
        val occ2 = listOf(completedOccurrence(2, monday))

        val calendars = listOf(
            HabitCalendar(h1, listOf(plan1), occ1),
            HabitCalendar(h2, listOf(plan2), occ2),
        )

        val grid = useCase(calendars, sunday).invoke().first()

        val mon = grid.days.single { it.date == monday }
        assertEquals(2, mon.predicted)
        assertEquals(2, mon.completed)

        val tue = grid.days.single { it.date == tuesday }
        assertEquals(2, tue.predicted)
        assertEquals(1, tue.completed)
    }

    // ---------------------------------------------------------------------------
    // 9. Paused habit does not generate prediction
    // ---------------------------------------------------------------------------
    @Test
    fun `paused habit does not contribute to predicted count`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.PAUSED)
        val calendar = HabitCalendar(habit, listOf(plan), emptyList())

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        assertTrue(grid.days.all { it.predicted == 0 })
    }

    // ---------------------------------------------------------------------------
    // 10. Frequency change mid-week: old expected dates preserved, new from effective date
    // ---------------------------------------------------------------------------
    @Test
    fun `frequency change mid-week preserves old plan predictions`() = runTest {
        val habit = dailyHabit(1)
        // Started as WeeklyDays Mon+Wed, changed on Thursday to Daily
        val planOld = HabitPlan(monday, HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)), HabitStatus.ACTIVE)
        val planNew = HabitPlan(thursday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit.copy(frequency = HabitFrequency.Daily), listOf(planOld, planNew), emptyList())

        val grid = useCase(listOf(calendar), sunday).invoke().first()

        // Before frequency change: only Mon and Wed predicted
        assertEquals(1, grid.days.single { it.date == monday }.predicted)
        assertEquals(0, grid.days.single { it.date == tuesday }.predicted)
        assertEquals(1, grid.days.single { it.date == wednesday }.predicted)
        // From Thursday onwards: daily
        assertEquals(1, grid.days.single { it.date == thursday }.predicted)
        assertEquals(1, grid.days.single { it.date == friday }.predicted)
        assertEquals(1, grid.days.single { it.date == saturday }.predicted)
        assertEquals(1, grid.days.single { it.date == sunday }.predicted)
    }

    // ---------------------------------------------------------------------------
    // 11. Today field set correctly on the last day of the grid
    // ---------------------------------------------------------------------------
    @Test
    fun `only today is marked isToday in the grid`() = runTest {
        val grid = useCase(emptyList(), wednesday).invoke().first()

        assertEquals(7, grid.days.size)
        val todayDays = grid.days.filter { it.isToday }
        assertEquals(1, todayDays.size)
        assertEquals(wednesday, todayDays.single().date)
    }

    @Test
    fun `controlled clock anchors the grid to Sao Paulo civil date`() = runTest {
        val today = LocalDate.of(2026, 11, 18)
        val habit = dailyHabit(1)
        val plan = HabitPlan(today.minusDays(2), HabitFrequency.Daily, HabitStatus.ACTIVE)
        val uc = GetHabitDayGridUseCase(
            habitRepository = FakeGridHabitRepository(listOf(HabitCalendar(habit, listOf(plan), emptyList()))),
            clock = Clock.fixed(Instant.parse("2026-11-19T02:30:00Z"), habitZone),
        )

        val grid = uc().first()

        assertEquals(today.minusDays(6), grid.days.first().date)
        assertEquals(today, grid.days.last().date)
        assertEquals(1, grid.days.last().predicted)
        assertTrue(grid.days.last().isToday)
    }

    // ---------------------------------------------------------------------------
    // 12. Idempotency: calling twice with same data produces same result
    // ---------------------------------------------------------------------------
    @Test
    fun `grid is deterministic given same data`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val occurrences = listOf(completedOccurrence(1, monday), completedOccurrence(1, tuesday))
        val calendar = HabitCalendar(habit, listOf(plan), occurrences)
        val uc = useCase(listOf(calendar), sunday)

        val g1 = uc.invoke().first()
        val g2 = uc.invoke().first()

        assertEquals(g1.days, g2.days)
    }

    // ---------------------------------------------------------------------------
    // 13. Virada de semana: second emission when today changes reflects closure
    //     Completion on the last day of a weekly-day plan is captured
    // ---------------------------------------------------------------------------
    @Test
    fun `grid updates when today moves forward`() = runTest {
        val habit = dailyHabit(1)
        val plan = HabitPlan(monday, HabitFrequency.Daily, HabitStatus.ACTIVE)
        val calendar = HabitCalendar(habit, listOf(plan), emptyList())
        val datesFlow = MutableStateFlow(tuesday)
        val uc = GetHabitDayGridUseCase(
            habitRepository = FakeGridHabitRepository(listOf(calendar)),
            dates = datesFlow,
        )

        val day1 = uc().first()
        // Grid ends on Tuesday; first day is last-Tuesday-6 = Wednesday of previous week
        assertEquals(tuesday, day1.days.last().date)
        assertTrue(day1.days.last().isToday)

        datesFlow.value = wednesday
        val day2 = uc().first()
        assertEquals(wednesday, day2.days.last().date)
        assertTrue(day2.days.last().isToday)
    }
}

// ---------------------------------------------------------------------------
// Helpers: minimal HabitRepository implementations for grid tests
// ---------------------------------------------------------------------------

/** Returns a fixed list of [HabitCalendar]s from [observeHabitCalendars]. */
internal class FakeGridHabitRepository(
    private val calendars: List<HabitCalendar>,
) : HabitRepository {
    override fun observeHabitCalendars(): Flow<List<HabitCalendar>> = flowOf(calendars)
    override fun observeAllHabits() = flowOf(emptyList<Habit>())
    override suspend fun saveHabit(habit: Habit): Long = error("not used")
    override suspend fun updateHabit(habit: Habit) = error("not used")
    override suspend fun deleteHabit(habit: Habit) = error("not used")
    override suspend fun getHabitById(id: Long): Habit? = null
    override suspend fun correctOccurrence(habitId: Long, date: LocalDate, completed: Boolean) =
        error("not used")
    override suspend fun completeOccurrence(habitId: Long, dateCivil: String, notes: String?) =
        error("not used")
    override suspend fun undoOccurrence(habitId: Long, dateCivil: String) = error("not used")
    override suspend fun pauseHabit(habitId: Long) = error("not used")
    override suspend fun resumeHabit(habitId: Long) = error("not used")
    override suspend fun archiveHabit(habitId: Long) = error("not used")
    override suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence? = null
    override fun observeOccurrencesForHabit(habitId: Long) = flowOf(emptyList<HabitOccurrence>())
}

/** Mutable repository that allows updating the calendar after first emission. */
internal class MutableGridHabitRepository(initial: HabitCalendar) : HabitRepository {
    private val flow = MutableStateFlow(listOf(initial))

    override fun observeHabitCalendars(): Flow<List<HabitCalendar>> = flow

    fun updateCalendar(calendar: HabitCalendar) { flow.value = listOf(calendar) }

    override fun observeAllHabits() = flowOf(emptyList<Habit>())
    override suspend fun saveHabit(habit: Habit): Long = error("not used")
    override suspend fun updateHabit(habit: Habit) = error("not used")
    override suspend fun deleteHabit(habit: Habit) = error("not used")
    override suspend fun getHabitById(id: Long): Habit? = null
    override suspend fun correctOccurrence(habitId: Long, date: LocalDate, completed: Boolean) =
        error("not used")
    override suspend fun completeOccurrence(habitId: Long, dateCivil: String, notes: String?) =
        error("not used")
    override suspend fun undoOccurrence(habitId: Long, dateCivil: String) = error("not used")
    override suspend fun pauseHabit(habitId: Long) = error("not used")
    override suspend fun resumeHabit(habitId: Long) = error("not used")
    override suspend fun archiveHabit(habitId: Long) = error("not used")
    override suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence? = null
    override fun observeOccurrencesForHabit(habitId: Long) = flowOf(emptyList<HabitOccurrence>())
}
