package com.ego.mobile.feature.tasks

import androidx.lifecycle.ViewModelStore
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.usecase.DeleteTaskWithUndoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {
    @Test
    fun createTaskWithoutDeadlineAndRescheduleOverdueTask() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModelStore: ViewModelStore? = null
        try {
            val now = epoch(2026, 11, 16, 12, 0)
            val repository = FakeTaskRepository()
            val overdueId = repository.createTask(task("Vencida", dueAt = epoch(2026, 11, 15, 0, 0)))
            val viewModel = TasksViewModel(repository, clock = { now })
            viewModelStore = ViewModelStore().apply { put("tasks", viewModel) }
            runCurrent()

            viewModel.onRescheduleTask(overdueId, now + 60_000L)
            runCurrent()
            assertFalse(
                viewModel.tasksUiState.value.openTasks
                    .single { it.id == overdueId }
                    .isOverdue(now),
            )

            viewModel.onCreateTask("Sem prazo", null, null)
            runCurrent()
            val noDeadlineTask = viewModel.tasksUiState.value.openTasks.single { it.title == "Sem prazo" }
            assertEquals(null, noDeadlineTask.dueAt)
        } finally {
            viewModelStore?.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun overdueTasksAreShownAndCompletionMovesThemToCompletedFilter() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModelStore: ViewModelStore? = null
        try {
            val now = epoch(2026, 11, 16, 12, 0)
            val repository = FakeTaskRepository()
            val overdueId = repository.createTask(task("Vencida", dueAt = epoch(2026, 11, 15, 0, 0)))
            val noDueId = repository.createTask(task("Sem prazo"))
            val viewModel = TasksViewModel(repository, clock = { now })
            viewModelStore = ViewModelStore().apply { put("tasks", viewModel) }

            runCurrent()
            val initial = viewModel.tasksUiState.value
            assertEquals(overdueId, initial.openTasks.first().id)
            assertTrue(initial.openTasks.first().isOverdue(initial.currentTimeMillis))
            assertFalse(initial.openTasks.last().isOverdue(initial.currentTimeMillis))

            viewModel.onCompleteTask(overdueId)
            runCurrent()
            assertFalse(viewModel.tasksUiState.value.openTasks.any { it.id == overdueId })

            viewModel.setShowCompletedTasks(true)
            assertEquals(overdueId, viewModel.tasksUiState.value.visibleTasks.single().id)
            assertEquals(noDueId, viewModel.tasksUiState.value.openTasks.single().id)
        } finally {
            viewModelStore?.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun confirmedDeleteEmitsSnackbarEventAndCanBeUndone() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var viewModelStore: ViewModelStore? = null
        try {
            val now = epoch(2026, 11, 16, 12, 0)
            val repository = FakeTaskRepository()
            val taskId = repository.createTask(task("Restaurável"))
            val useCase = DeleteTaskWithUndoUseCase(repository, currentTimeMillis = { now })
            val viewModel = TasksViewModel(repository, deleteTaskWithUndoUseCase = useCase, clock = { now })
            viewModelStore = ViewModelStore().apply { put("tasks", viewModel) }

            runCurrent()
            val event = async { viewModel.events.first() }
            viewModel.onDeleteTask(taskId)
            runCurrent()

            assertEquals(TasksEvent.TaskDeleted("Restaurável"), event.await())
            assertTrue(viewModel.tasksUiState.value.openTasks.none { it.id == taskId })

            viewModel.undoDelete()
            runCurrent()
            assertTrue(viewModel.tasksUiState.value.openTasks.any { it.id == taskId })
        } finally {
            viewModelStore?.clear()
            Dispatchers.resetMain()
        }
    }

    private fun epoch(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        java.time.ZonedDateTime.of(
            java.time.LocalDateTime.of(year, month, day, hour, minute),
            java.time.ZoneId.of("America/Sao_Paulo"),
        ).toInstant().toEpochMilli()

    private fun task(title: String, dueAt: Long? = null) = Task(
        title = title,
        dueAt = dueAt,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    private class FakeTaskRepository : TaskRepository {
        private val tasks = linkedMapOf<Long, Task>()
        private val state = MutableStateFlow<List<Task>>(emptyList())
        private var nextId = 1L

        private fun emit() {
            state.value = tasks.values.toList()
        }

        override suspend fun createTask(task: Task): Long {
            val id = if (task.id == 0L) nextId++ else task.id
            tasks[id] = task.copy(id = id)
            emit()
            return id
        }

        override suspend fun updateTask(task: Task) {
            tasks[task.id] = task
            emit()
        }

        override suspend fun getTaskById(id: Long): Task? = tasks[id]

        override fun getOpenTasks(): Flow<List<Task>> =
            state.map { it.filter { task -> task.status == TaskStatus.OPEN } }

        override fun getCompletedTasks(): Flow<List<Task>> =
            state.map { it.filter { task -> task.status == TaskStatus.COMPLETED } }

        override suspend fun completeTask(id: Long, completedAt: Long): Result<Unit> =
            tasks[id]?.let { task ->
                updateTask(task.copy(status = TaskStatus.COMPLETED, completedAt = completedAt))
                Result.success(Unit)
            } ?: Result.failure(IllegalArgumentException("Task not found."))

        override suspend fun rescheduleTask(id: Long, newDueAt: Long): Result<Unit> =
            tasks[id]?.let { task ->
                updateTask(task.copy(dueAt = newDueAt))
                Result.success(Unit)
            } ?: Result.failure(IllegalArgumentException("Task not found."))

        override suspend fun deleteTask(id: Long): Result<Task> {
            val task = tasks.remove(id)
                ?: return Result.failure(IllegalArgumentException("Task not found."))
            emit()
            return Result.success(task)
        }

        override suspend fun restoreTask(task: Task): Result<Unit> {
            tasks[task.id] = task
            emit()
            return Result.success(Unit)
        }
    }
}
