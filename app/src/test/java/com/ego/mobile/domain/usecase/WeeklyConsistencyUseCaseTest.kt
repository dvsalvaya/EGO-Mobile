package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.ConsistencyEvent
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.model.WeeklyConsistency
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyConsistencyUseCaseTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val today = LocalDate.of(2026, 11, 13)

    @Test
    fun `emits seven local days ending today with level zero when nothing was completed`() = runTest {
        val useCase = createUseCase()

        val grid = useCase().first()

        assertEquals(7, grid.days.size)
        assertEquals(today.minusDays(6), grid.days.first().date)
        assertEquals(today, grid.days.last().date)
        assertEquals(List(7) { 0 }, grid.days.map { it.level })
    }

    @Test
    fun `counts habit task and workout completions with Sao Paulo civil dates`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        habits.habits.value = listOf(habit(id = 1L))
        habits.setOccurrences(1L, listOf(completedOccurrence(id = 10L, habitId = 1L, date = today)))
        tasks.completedTasks.value = listOf(completedTask(id = 20L, completedAt = epochUtc(2026, 11, 12, 14, 0)))
        workouts.completedSessions.value = listOf(
            completedSession(id = 30L, endedAt = epochUtc(2026, 11, 11, 14, 0)),
        )

        val grid = createUseCase(habits, tasks, workouts).invoke().first()

        assertEquals(1, grid.days.single { it.date == today }.count)
        assertEquals(1, grid.days.single { it.date == today.minusDays(1) }.count)
        assertEquals(1, grid.days.single { it.date == today.minusDays(2) }.count)
    }

    @Test
    fun `groups three or more activities into the maximum level without a general score`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        habits.habits.value = listOf(habit(id = 1L), habit(id = 2L))
        habits.setOccurrences(1L, listOf(completedOccurrence(id = 1L, habitId = 1L, date = today)))
        habits.setOccurrences(2L, listOf(completedOccurrence(id = 2L, habitId = 2L, date = today)))
        tasks.completedTasks.value = listOf(
            completedTask(id = 3L, completedAt = epochUtc(2026, 11, 13, 12, 0)),
            completedTask(id = 4L, completedAt = epochUtc(2026, 11, 13, 13, 0)),
        )
        workouts.completedSessions.value = listOf(
            completedSession(id = 5L, endedAt = epochUtc(2026, 11, 13, 14, 0)),
        )

        val grid = createUseCase(habits, tasks, workouts).invoke().first()

        val day = grid.days.last()
        assertEquals(5, day.count)
        assertEquals(3, day.level)
        assertEquals(3, grid.levelByCivilDate[today.toString()])
    }

    @Test
    fun `deduplicates the same activity presented more than once`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val occurrence = completedOccurrence(id = 7L, habitId = 1L, date = today)
        val task = completedTask(id = 8L, completedAt = epochUtc(2026, 11, 13, 12, 0))
        val session = completedSession(id = 9L, endedAt = epochUtc(2026, 11, 13, 13, 0))
        habits.habits.value = listOf(habit(id = 1L))
        habits.setOccurrences(1L, listOf(occurrence, occurrence.copy()))
        tasks.completedTasks.value = listOf(task, task.copy())
        workouts.completedSessions.value = listOf(session, session.copy())

        val grid = createUseCase(habits, tasks, workouts).invoke().first()

        assertEquals(3, grid.days.last().count)
        assertEquals(3, grid.days.last().level)
    }

    @Test
    fun `same persisted id from different source types does not collide`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        habits.habits.value = listOf(habit(id = 1L))
        habits.setOccurrences(1L, listOf(completedOccurrence(id = 55L, habitId = 1L, date = today)))
        tasks.completedTasks.value = listOf(completedTask(id = 55L, completedAt = epochUtc(2026, 11, 13, 12, 0)))
        workouts.completedSessions.value = listOf(
            completedSession(id = 55L, endedAt = epochUtc(2026, 11, 13, 13, 0)),
        )

        val grid = createUseCase(habits, tasks, workouts).invoke().first()

        assertEquals(3, grid.days.last().count)
    }

    @Test
    fun `ignores pending occurrences open tasks active sessions and events outside the window`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val oldDate = today.minusDays(8)
        habits.habits.value = listOf(habit(id = 1L))
        habits.setOccurrences(
            1L,
            listOf(
                completedOccurrence(id = 1L, habitId = 1L, date = oldDate),
                HabitOccurrence(
                    id = 2L,
                    habitId = 1L,
                    date = today,
                    status = HabitOccurrenceStatus.PENDING,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
        )
        tasks.completedTasks.value = emptyList()
        tasks.openTasks.value = listOf(
            Task(id = 3L, title = "Aberta", createdAt = 1L, updatedAt = 1L),
        )
        workouts.completedSessions.value = listOf(
            completedSession(id = 4L, endedAt = epochUtc(2026, 11, 13, 12, 0)),
        )
        workouts.activeSession.value = WorkoutSession(id = 5L, title = "Ativa", startedAt = 1L)

        val grid = createUseCase(habits, tasks, workouts).invoke().first()

        assertEquals(1, grid.days.sumOf { it.count })
        assertEquals(1, grid.days.last().count)
        assertEquals(0, grid.days.first().count)
    }

    @Test
    fun `habit uses its civil date even when completion timestamp falls on another local day`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        habits.habits.value = listOf(habit(id = 1L))
        habits.setOccurrences(
            1L,
            listOf(completedOccurrence(id = 1L, habitId = 1L, date = today, completedAt = epochUtc(2026, 11, 10, 23, 0))),
        )

        val grid = createUseCase(habits = habits).invoke().first()

        assertEquals(1, grid.days.last().count)
        assertEquals(0, grid.days.single { it.date == today.minusDays(3) }.count)
    }

    @Test
    fun `pure aggregation deduplicates by source type and id and clamps levels`() {
        val events = listOf(
            ConsistencyEvent(ActivityType.HABIT, 1L, today),
            ConsistencyEvent(ActivityType.HABIT, 1L, today),
            ConsistencyEvent(ActivityType.TASK, 1L, today),
            ConsistencyEvent(ActivityType.WORKOUT, 1L, today),
            ConsistencyEvent(ActivityType.HAIRCARE, 9L, today),
        )

        val grid = aggregateWeeklyConsistency(events, today)

        assertEquals(7, grid.days.size)
        assertEquals(3, grid.days.last().count)
        assertEquals(3, grid.days.last().level)
        assertEquals(WeeklyConsistency.empty(today).days.dropLast(1), grid.days.dropLast(1))
    }

    private fun createUseCase(
        habits: FakeConsolidationHabitRepository = FakeConsolidationHabitRepository(),
        tasks: FakeConsolidationTaskRepository = FakeConsolidationTaskRepository(),
        workouts: FakeConsolidationWorkoutRepository = FakeConsolidationWorkoutRepository(),
    ) = GetWeeklyConsistencyUseCase(
        habitRepository = habits,
        taskRepository = tasks,
        workoutRepository = workouts,
        zoneId = zone,
        today = { today },
    )

    private fun habit(id: Long) = Habit(
        id = id,
        name = "Hábito $id",
        frequency = HabitFrequency.Daily,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun completedOccurrence(
        id: Long,
        habitId: Long,
        date: LocalDate,
        completedAt: Long = epochUtc(2026, 11, 13, 12, 0),
    ) = HabitOccurrence(
        id = id,
        habitId = habitId,
        date = date,
        status = HabitOccurrenceStatus.COMPLETED,
        completedAt = completedAt,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun completedTask(id: Long, completedAt: Long) = Task(
        id = id,
        title = "Tarefa $id",
        status = TaskStatus.COMPLETED,
        completedAt = completedAt,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun completedSession(id: Long, endedAt: Long) = WorkoutSession(
        id = id,
        title = "Treino $id",
        startedAt = endedAt - 3_600_000L,
        endedAt = endedAt,
        status = WorkoutSessionStatus.COMPLETED,
    )

    private fun epochUtc(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), ZoneOffset.UTC).toInstant().toEpochMilli()
}
