package com.ego.mobile.feature.home

import androidx.lifecycle.ViewModelStore
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.usecase.FakeConsolidationHaircareRepository
import com.ego.mobile.domain.usecase.FakeConsolidationHabitRepository
import com.ego.mobile.domain.usecase.FakeConsolidationTaskRepository
import com.ego.mobile.domain.usecase.FakeConsolidationWorkoutRepository
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 11, 13)
    private lateinit var habits: FakeConsolidationHabitRepository
    private lateinit var tasks: FakeConsolidationTaskRepository
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        habits = FakeConsolidationHabitRepository()
        tasks = FakeConsolidationTaskRepository()
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `emits summary content from the reactive use case`() = runTest(dispatcher) {
        habits.habits.value = listOf(
            Habit(
                id = 4L,
                name = "Leitura",
                frequency = HabitFrequency.Daily,
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val viewModel = createViewModel()
        runCurrent()

        val state = viewModel.uiState.value as HomeUiState.Content
        assertEquals("Leitura", state.summary.todayHabits.single().title)
        assertTrue(state.errorMessage == null)
    }

    @Test
    fun `quick completion dispatches to habit and task repositories`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onCompleteHabit(7L)
        viewModel.onCompleteTask(12L)
        runCurrent()

        assertEquals(listOf(7L to today.toString()), habits.completionCalls.map { it.habitId to it.dateCivil })
        assertEquals(listOf(12L to 9_000L), tasks.completionCalls.map { it.taskId to it.completedAt })
    }

    @Test
    fun `creation shortcuts emit one-shot form requests`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onCreateHabitRequested()
        viewModel.onCreateTaskRequested()
        runCurrent()

        assertEquals(
            listOf(HomeEvent.OpenHabitCreation, HomeEvent.OpenTaskCreation),
            viewModel.events.take(2).toList(),
        )
    }

    @Test
    fun `completion failure is exposed in the content state`() = runTest(dispatcher) {
        tasks.completionResult = Result.failure(IllegalStateException("Falha ao concluir"))
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onCompleteTask(12L)
        runCurrent()

        assertEquals("Falha ao concluir", (viewModel.uiState.value as HomeUiState.Content).errorMessage)
    }

    @Test
    fun `summary follows changes from the use case`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        tasks.openTasks.value = listOf(
            Task(
                id = 3L,
                title = "Revisar documento",
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        runCurrent()

        val state = viewModel.uiState.value as HomeUiState.Content
        assertEquals("Revisar documento", state.summary.priorityTasks.single().title)
    }

    private fun createViewModel() = HomeViewModel(
        getHomeSummaryUseCase = GetHomeSummaryUseCase(
            habitRepository = habits,
            taskRepository = tasks,
            workoutRepository = FakeConsolidationWorkoutRepository(),
            haircareRepository = FakeConsolidationHaircareRepository(),
            today = { today },
        ),
        habitRepository = habits,
        taskRepository = tasks,
        clock = { 9_000L },
        today = { today },
    ).also { viewModelStore.put("home", it) }
}
