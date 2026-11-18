package com.ego.mobile.feature.history

import androidx.lifecycle.ViewModelStore
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.usecase.FakeConsolidationHaircareRepository
import com.ego.mobile.domain.usecase.FakeConsolidationHabitRepository
import com.ego.mobile.domain.usecase.FakeConsolidationTaskRepository
import com.ego.mobile.domain.usecase.FakeConsolidationWorkoutRepository
import com.ego.mobile.domain.usecase.GetConsolidatedHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var habits: FakeConsolidationHabitRepository
    private lateinit var tasks: FakeConsolidationTaskRepository
    private lateinit var workouts: FakeConsolidationWorkoutRepository
    private lateinit var haircare: FakeConsolidationHaircareRepository
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        habits = FakeConsolidationHabitRepository()
        tasks = FakeConsolidationTaskRepository()
        workouts = FakeConsolidationWorkoutRepository()
        haircare = FakeConsolidationHaircareRepository()
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `shows consolidated history newest first and filters by activity type`() = runTest(dispatcher) {
        val habit = Habit(
            id = 1L,
            name = "Leitura",
            frequency = HabitFrequency.Daily,
            createdAt = 1L,
            updatedAt = 1L,
        )
        habits.habits.value = listOf(habit)
        habits.setOccurrences(
            habit.id,
            listOf(
                HabitOccurrence(
                    id = 10L,
                    habitId = habit.id,
                    date = LocalDate.of(2026, 11, 13),
                    status = HabitOccurrenceStatus.COMPLETED,
                    completedAt = 400L,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            ),
        )
        tasks.completedTasks.value = listOf(
            Task(
                id = 2L,
                title = "Relatório",
                status = TaskStatus.COMPLETED,
                createdAt = 1L,
                updatedAt = 1L,
                completedAt = 200L,
            ),
        )
        workouts.completedSessions.value = listOf(
            WorkoutSession(
                id = 3L,
                title = "Treino",
                startedAt = 1L,
                endedAt = 300L,
                status = WorkoutSessionStatus.COMPLETED,
            ),
        )
        haircare.completedOccurrences.value = listOf(
            HaircareOccurrence(
                id = 4L,
                routineId = 5L,
                stepType = HaircareStepType.NUTRICAO,
                stepTitle = "Nutrição",
                scheduledDate = "2026-11-13",
                executedAt = 100L,
                status = HaircareOccurrenceStatus.COMPLETED,
            ),
        )
        val viewModel = createViewModel()
        runCurrent()

        assertEquals(
            listOf(ActivityType.HABIT, ActivityType.WORKOUT, ActivityType.TASK, ActivityType.HAIRCARE),
            viewModel.uiState.value.items.map { it.type },
        )

        viewModel.setFilter(ActivityType.TASK)
        runCurrent()

        assertEquals(listOf("Relatório"), viewModel.uiState.value.visibleItems.map { it.title })
        assertEquals(ActivityType.TASK, viewModel.uiState.value.selectedFilter)

        viewModel.setFilter(null)
        runCurrent()
        assertEquals(4, viewModel.uiState.value.visibleItems.size)
    }

    @Test
    fun `retains selected filter when consolidated source updates`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.setFilter(ActivityType.TASK)
        runCurrent()
        tasks.completedTasks.value = listOf(
            Task(
                id = 6L,
                title = "Atualização",
                status = TaskStatus.COMPLETED,
                createdAt = 1L,
                updatedAt = 1L,
                completedAt = 100L,
            ),
        )
        runCurrent()

        assertEquals(ActivityType.TASK, viewModel.uiState.value.selectedFilter)
        assertEquals(listOf("Atualização"), viewModel.uiState.value.visibleItems.map { it.title })
    }

    private fun createViewModel() = HistoryViewModel(
        habitRepository = habits,
        getConsolidatedHistoryUseCase = GetConsolidatedHistoryUseCase(
            habitRepository = habits,
            taskRepository = tasks,
            workoutRepository = workouts,
            haircareRepository = haircare,
            dates = kotlinx.coroutines.flow.flowOf(LocalDate.of(2026, 11, 18)),
        ),
    ).also { viewModelStore.put("history", it) }
}
