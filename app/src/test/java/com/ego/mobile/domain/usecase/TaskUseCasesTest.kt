package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.test.runTest

class TaskUseCasesTest {

    @Test
    fun rescheduleTaskUpdatesDueAtAndClearsLateState() {
        val now = System.currentTimeMillis()
        val task = Task(
            title = "Tarefa vencida",
            dueAt = now - 86_400_000L,
            status = TaskStatus.OPEN,
            createdAt = now - 60_000L,
            updatedAt = now - 60_000L,
        )

        val newDueAt = now + 86_400_000L
        val updated = RescheduleTaskUseCase().invoke(task, newDueAt)

        assertEquals(newDueAt, updated.dueAt)
        assertFalse(updated.isOverdue(now))
    }

    @Test
    fun rescheduleRejectsInvalidTimestamp() {
        val task = sampleTask()
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            RescheduleTaskUseCase().invoke(task, 0L)
        }
    }

    @Test
    fun deletionCanBeUndoneUntilTheWindowExpires() = runTest {
        var now = 1_000L
        val repository = FakeTaskRepository().apply { tasks[7L] = sampleTask(id = 7L) }
        val useCase = DeleteTaskWithUndoUseCase(
            taskRepository = repository,
            undoWindowMillis = 500L,
            currentTimeMillis = { now },
        )

        val deletion = useCase(7L).getOrThrow()
        assertTrue(deletion.canUndo)
        assertNull(repository.tasks[7L])

        now = 1_499L
        assertTrue(useCase.undo().isSuccess)
        assertEquals("Excluir depois", repository.tasks[7L]?.title)
    }

    @Test
    fun deletionCannotBeUndoneAfterTheWindowExpires() = runTest {
        var now = 1_000L
        val repository = FakeTaskRepository().apply { tasks[7L] = sampleTask(id = 7L) }
        val useCase = DeleteTaskWithUndoUseCase(
            taskRepository = repository,
            undoWindowMillis = 500L,
            currentTimeMillis = { now },
        )

        useCase(7L).getOrThrow()
        now = 1_500L

        assertTrue(useCase.undo().isFailure)
        assertNull(repository.tasks[7L])
    }

    private fun sampleTask(id: Long = 0L) = Task(
        id = id,
        title = "Excluir depois",
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    private class FakeTaskRepository : TaskRepository {
        val tasks = mutableMapOf<Long, Task>()
        private val taskFlow = MutableStateFlow<List<Task>>(emptyList())
        private var nextId = 1L

        private fun emit() {
            taskFlow.value = tasks.values.toList()
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
            taskFlow.map { it.filter { task -> task.status == TaskStatus.OPEN } }

        override fun getCompletedTasks(): Flow<List<Task>> =
            taskFlow.map { it.filter { task -> task.status == TaskStatus.COMPLETED } }

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
