package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeSummaryUseCaseTest {
    private val today = LocalDate.of(2026, 11, 13)

    @Test
    fun `includes active habits scheduled today with current occurrence status`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val useCase = createUseCase(habits = habits)
        val daily = habit(id = 1L, name = "Ler")
        val friday = habit(id = 2L, name = "Caminhar", frequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.FRIDAY)))
        val paused = habit(id = 3L, name = "Meditar", status = HabitStatus.PAUSED)
        val sunday = habit(id = 4L, name = "Alongar", frequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.SUNDAY)))
        habits.habits.value = listOf(daily, friday, paused, sunday)
        habits.setOccurrences(
            daily.id,
            listOf(
                occurrence(
                    id = 21L,
                    habitId = daily.id,
                    date = today,
                    status = HabitOccurrenceStatus.COMPLETED,
                ),
            ),
        )

        val summary = useCase().first()

        assertEquals(listOf(1L, 2L), summary.todayHabits.map { it.habitId })
        assertEquals(
            listOf(HabitOccurrenceStatus.COMPLETED, HabitOccurrenceStatus.PENDING),
            summary.todayHabits.map { it.status },
        )
    }

    @Test
    fun `weekly times progress appears on any weekday and closes eligibility at target`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val weekly = habit(id = 10L, name = "Treinar", frequency = HabitFrequency.TimesPerWeek(2))
        habits.habits.value = listOf(weekly)
        habits.setOccurrences(
            weekly.id,
            listOf(occurrence(101L, weekly.id, today.minusDays(2), HabitOccurrenceStatus.COMPLETED)),
        )

        val incomplete = createUseCase(habits = habits).invoke().first().todayHabits.single()
        assertEquals(1, incomplete.completedThisWeek)
        assertEquals(2, incomplete.weeklyTarget)
        assertTrue(incomplete.canComplete)

        habits.setOccurrences(
            weekly.id,
            listOf(
                occurrence(101L, weekly.id, today.minusDays(2), HabitOccurrenceStatus.COMPLETED),
                occurrence(102L, weekly.id, today.minusDays(1), HabitOccurrenceStatus.COMPLETED),
            ),
        )
        val complete = createUseCase(habits = habits).invoke().first().todayHabits.single()
        assertEquals(2, complete.completedThisWeek)
        assertTrue(!complete.canComplete)
    }

    @Test
    fun `prioritizes dated open tasks and shows workout ahead of haircare`() = runTest {
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val haircare = FakeConsolidationHaircareRepository()
        val routine = HaircareRoutine(id = 8L, name = "Cronograma", createdAt = 1L, updatedAt = 1L)
        haircare.activeRoutine.value = routine
        haircare.setOccurrences(
            routine.id,
            listOf(
                HaircareOccurrence(
                    id = 81L,
                    routineId = routine.id,
                    stepType = HaircareStepType.NUTRICAO,
                    stepTitle = "Nutrição",
                    scheduledDate = today.toString(),
                ),
            ),
        )
        workouts.activeSession.value = WorkoutSession(
            id = 9L,
            title = "Treino A",
            startedAt = 1L,
            status = WorkoutSessionStatus.PAUSED,
        )
        val later = task(id = 2L, title = "Prazo futuro", dueAt = 300L)
        val sooner = task(id = 1L, title = "Prazo próximo", dueAt = 200L)
        val undated = task(id = 3L, title = "Sem prazo", dueAt = null)
        tasks.openTasks.value = listOf(undated, later, sooner)

        val summary = createUseCase(
            tasks = tasks,
            workouts = workouts,
            haircare = haircare,
        ).invoke().first()

        assertEquals(listOf(1L, 2L, 3L), summary.priorityTasks.map { it.id })
        assertEquals(ActivityType.WORKOUT, summary.activeRoutine?.type)
        assertEquals("Treino A", summary.activeRoutine?.title)
    }

    @Test
    fun `emits new summaries when each observed repository changes`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val haircare = FakeConsolidationHaircareRepository()
        val summaries = mutableListOf<HomeSummary>()
        val useCase = createUseCase(habits, tasks, workouts, haircare)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().take(5).toList(summaries)
        }
        runCurrent()
        habits.habits.value = listOf(habit(id = 1L, name = "Ler"))
        runCurrent()
        tasks.openTasks.value = listOf(task(id = 1L, title = "Tarefa", dueAt = 10L))
        runCurrent()
        val routine = HaircareRoutine(id = 2L, name = "Cronograma", createdAt = 1L, updatedAt = 1L)
        haircare.setOccurrences(
            routine.id,
            listOf(
                HaircareOccurrence(
                    id = 20L,
                    routineId = routine.id,
                    stepType = HaircareStepType.HIDRATACAO,
                    stepTitle = "Hidratação",
                    scheduledDate = today.toString(),
                ),
            ),
        )
        haircare.activeRoutine.value = routine
        runCurrent()
        workouts.activeSession.value = WorkoutSession(id = 3L, title = "Treino", startedAt = 1L)
        runCurrent()

        assertEquals(5, summaries.size)
        assertTrue(summaries[1].todayHabits.isNotEmpty())
        assertTrue(summaries[2].priorityTasks.isNotEmpty())
        assertEquals(ActivityType.HAIRCARE, summaries[3].activeRoutine?.type)
        assertEquals(ActivityType.WORKOUT, summaries[4].activeRoutine?.type)
    }

    private fun createUseCase(
        habits: FakeConsolidationHabitRepository = FakeConsolidationHabitRepository(),
        tasks: FakeConsolidationTaskRepository = FakeConsolidationTaskRepository(),
        workouts: FakeConsolidationWorkoutRepository = FakeConsolidationWorkoutRepository(),
        haircare: FakeConsolidationHaircareRepository = FakeConsolidationHaircareRepository(),
    ) = GetHomeSummaryUseCase(
        habitRepository = habits,
        taskRepository = tasks,
        workoutRepository = workouts,
        haircareRepository = haircare,
        today = { today },
    )

    private fun habit(
        id: Long,
        name: String,
        frequency: HabitFrequency = HabitFrequency.Daily,
        status: HabitStatus = HabitStatus.ACTIVE,
    ) = Habit(
        id = id,
        name = name,
        frequency = frequency,
        status = status,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun occurrence(
        id: Long,
        habitId: Long,
        date: LocalDate,
        status: HabitOccurrenceStatus,
    ) = HabitOccurrence(
        id = id,
        habitId = habitId,
        date = date,
        status = status,
        completedAt = if (status == HabitOccurrenceStatus.COMPLETED) 500L else null,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun task(id: Long, title: String, dueAt: Long?) = Task(
        id = id,
        title = title,
        dueAt = dueAt,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
