package com.ego.mobile.domain.usecase

import com.ego.mobile.data.repository.FakeHabitDao
import com.ego.mobile.data.repository.FakeHabitOccurrenceDao
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.domain.model.habitZone
import kotlinx.coroutines.flow.flowOf
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.model.HistoryItem
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
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
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryUseCaseTest {
    @Test
    fun reopeningHistoryClosesEachElapsedDailyDateExactlyOnce() = runTest {
        val dao = FakeHabitDao()
        val occurrences = FakeHabitOccurrenceDao()
        val habits = HabitRepositoryImpl(
            dao, UnconfinedTestDispatcher(testScheduler), occurrences,
        )
        val today = LocalDate.of(2026, 11, 18)
        val created = today.minusDays(2).atStartOfDay(habitZone).toInstant().toEpochMilli()
        habits.saveHabit(Habit(name = "Leitura", frequency = HabitFrequency.Daily, createdAt = created, updatedAt = created))
        val history = GetConsolidatedHistoryUseCase(
            habits, FakeConsolidationTaskRepository(), FakeConsolidationWorkoutRepository(),
            FakeConsolidationHaircareRepository(), dates = flowOf(today),
        )

        val rows = history().first()
        assertEquals(listOf("Não fiz", "Não fiz"), rows.map { it.status })
        assertEquals(2, rows.distinctBy { it.occurredAt }.size)
        assertEquals(rows, history().first())
    }

    @Test
    fun `merges completed activities from every repository in descending execution time`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val haircare = FakeConsolidationHaircareRepository()
        val habit = Habit(
            id = 1L,
            name = "Leitura arquivada",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ARCHIVED,
            createdAt = 1L,
            updatedAt = 1L,
        )
        habits.habits.value = listOf(habit)
        habits.setOccurrences(
            habit.id,
            listOf(
                HabitOccurrence(
                    id = 11L,
                    habitId = habit.id,
                    date = LocalDate.of(2026, 11, 12),
                    status = HabitOccurrenceStatus.COMPLETED,
                    completedAt = 100L,
                    notes = "20 páginas",
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
                HabitOccurrence(
                    id = 12L,
                    habitId = habit.id,
                    date = LocalDate.of(2026, 11, 13),
                    status = HabitOccurrenceStatus.PENDING,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
        )
        tasks.completedTasks.value = listOf(
            Task(
                id = 2L,
                title = "Entregar relatório",
                status = TaskStatus.COMPLETED,
                createdAt = 1L,
                updatedAt = 1L,
                completedAt = 300L,
                description = "Enviar por e-mail",
            ),
            Task(
                id = 3L,
                title = "Ainda aberta",
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        workouts.completedSessions.value = listOf(
            WorkoutSession(
                id = 4L,
                title = "Treino de força",
                startedAt = 350L,
                endedAt = 400L,
                status = WorkoutSessionStatus.COMPLETED,
            ),
        )
        haircare.completedOccurrences.value = listOf(
            HaircareOccurrence(
                id = 5L,
                routineId = 6L,
                stepType = HaircareStepType.NUTRICAO,
                stepTitle = "Etapa de nutrição",
                scheduledDate = "2026-11-13",
                executedAt = 500L,
                status = HaircareOccurrenceStatus.COMPLETED,
                notes = "Máscara",
            ),
            HaircareOccurrence(
                id = 7L,
                routineId = 6L,
                stepType = HaircareStepType.HIDRATACAO,
                stepTitle = "Pendente",
                scheduledDate = "2026-11-13",
                status = HaircareOccurrenceStatus.PENDING,
            ),
        )

        val history = GetConsolidatedHistoryUseCase(
            habitRepository = habits,
            taskRepository = tasks,
            workoutRepository = workouts,
            haircareRepository = haircare,
        ).invoke().first()

        assertEquals(
            listOf(ActivityType.HAIRCARE, ActivityType.WORKOUT, ActivityType.TASK, ActivityType.HABIT),
            history.map(HistoryItem::type),
        )
        assertEquals(listOf(500L, 400L, 300L, 100L), history.map(HistoryItem::occurredAt))
        assertEquals("Etapa de nutrição", history.first().title)
        assertEquals("Nutrição · Máscara", history.first().subtitle)
        assertEquals("Enviar por e-mail", history[2].subtitle)
        assertEquals("20 páginas", history.last().subtitle)
    }

    @Test
    fun `updates consolidated history when a repository emits new completed records`() = runTest {
        val habits = FakeConsolidationHabitRepository()
        val tasks = FakeConsolidationTaskRepository()
        val workouts = FakeConsolidationWorkoutRepository()
        val haircare = FakeConsolidationHaircareRepository()
        val useCase = GetConsolidatedHistoryUseCase(
            habitRepository = habits,
            taskRepository = tasks,
            workoutRepository = workouts,
            haircareRepository = haircare,
        )
        val emissions = mutableListOf<List<HistoryItem>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().take(5).toList(emissions)
        }
        runCurrent()
        tasks.completedTasks.value = listOf(
            Task(
                id = 1L,
                title = "Tarefa",
                status = TaskStatus.COMPLETED,
                createdAt = 1L,
                updatedAt = 1L,
                completedAt = 100L,
            ),
        )
        runCurrent()
        workouts.completedSessions.value = listOf(
            WorkoutSession(
                id = 2L,
                title = "Treino",
                startedAt = 1L,
                endedAt = 200L,
                status = WorkoutSessionStatus.COMPLETED,
            ),
        )
        runCurrent()
        haircare.completedOccurrences.value = listOf(
            HaircareOccurrence(
                id = 3L,
                routineId = 4L,
                stepType = HaircareStepType.HIDRATACAO,
                stepTitle = "Hidratação",
                scheduledDate = "2026-11-13",
                executedAt = 300L,
                status = HaircareOccurrenceStatus.COMPLETED,
            ),
        )
        runCurrent()
        val habit = Habit(
            id = 5L,
            name = "Leitura",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1L,
            updatedAt = 1L,
        )
        habits.setOccurrences(
            habit.id,
            listOf(
                HabitOccurrence(
                    id = 6L,
                    habitId = habit.id,
                    date = LocalDate.of(2026, 11, 13),
                    status = HabitOccurrenceStatus.COMPLETED,
                    completedAt = 400L,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
        )
        habits.habits.value = listOf(habit)
        runCurrent()

        assertEquals(5, emissions.size)
        assertEquals(listOf(0, 1, 2, 3, 4), emissions.map { it.size })
        assertEquals(
            listOf(ActivityType.HABIT, ActivityType.HAIRCARE, ActivityType.WORKOUT, ActivityType.TASK),
            emissions.last().map(HistoryItem::type),
        )
    }
}
