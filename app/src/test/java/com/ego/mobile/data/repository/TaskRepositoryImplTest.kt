package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.TaskDao
import com.ego.mobile.data.local.entity.TaskEntity
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeTaskDao : TaskDao {
    private val entities = linkedMapOf<Long, TaskEntity>()
    private val allTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    private var nextId = 1L

    private fun emit() {
        allTasks.value = entities.values.toList()
    }

    override suspend fun insert(task: TaskEntity): Long {
        val id = if (task.id == 0L) nextId++ else task.id
        entities[id] = task.copy(id = id)
        emit()
        return id
    }

    override suspend fun update(task: TaskEntity) {
        entities[task.id] = task
        emit()
    }

    override suspend fun delete(task: TaskEntity) {
        entities.remove(task.id)
        emit()
    }

    override suspend fun getById(id: Long): TaskEntity? = entities[id]

    override suspend fun complete(id: Long, completedAt: Long): Int {
        val task = entities[id]?.takeIf { it.status == TaskStatus.OPEN.name } ?: return 0
        update(
            task.copy(
                status = TaskStatus.COMPLETED.name,
                completedAt = completedAt,
                updatedAt = completedAt,
            ),
        )
        return 1
    }

    override suspend fun reschedule(id: Long, newDueAt: Long, updatedAt: Long): Int {
        val task = entities[id]?.takeIf { it.status == TaskStatus.OPEN.name } ?: return 0
        update(task.copy(dueAt = newDueAt, updatedAt = updatedAt))
        return 1
    }

    override suspend fun deleteById(id: Long): TaskEntity? {
        val task = entities.remove(id) ?: return null
        emit()
        return task
    }

    override fun getOpenTasks(): Flow<List<TaskEntity>> = allTasks.map { tasks ->
        tasks.filter { it.status == TaskStatus.OPEN.name }
            .sortedWith(compareBy<TaskEntity>({ it.dueAt == null }, { it.dueAt ?: Long.MAX_VALUE }))
    }

    override fun getCompletedTasks(): Flow<List<TaskEntity>> = allTasks.map { tasks ->
        tasks.filter { it.status == TaskStatus.COMPLETED.name }
            .sortedByDescending { it.completedAt }
    }
}

class TaskRepositoryImplTest {
    private lateinit var dao: FakeTaskDao
    private lateinit var repository: TaskRepositoryImpl
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        dao = FakeTaskDao()
        repository = TaskRepositoryImpl(dao, dispatcher)
    }

    @Test
    fun createTaskWithNoDueDateIsObservable() = runTest(dispatcher) {
        val taskId = repository.createTask(task("Sem prazo"))

        val activeTasks = repository.getOpenTasks().first()
        assertEquals(1, activeTasks.size)
        assertEquals(taskId, activeTasks.single().id)
        assertEquals(null, activeTasks.single().dueAt)
    }

    @Test
    fun completeTaskMovesItemFromOpenToCompleted() = runTest(dispatcher) {
        val taskId = repository.createTask(task("Concluir"))
        val completedAt = 50_000L

        assertTrue(repository.completeTask(taskId, completedAt).isSuccess)
        assertTrue(repository.getOpenTasks().first().isEmpty())
        val completed = repository.getCompletedTasks().first().single()
        assertEquals(TaskStatus.COMPLETED, completed.status)
        assertEquals(completedAt, completed.completedAt)
    }

    @Test
    fun rescheduleDeleteAndRestorePreserveTaskData() = runTest(dispatcher) {
        val taskId = repository.createTask(task("Reagendar", dueAt = 10L))
        val newDueAt = 80_000L

        assertTrue(repository.rescheduleTask(taskId, newDueAt).isSuccess)
        assertEquals(newDueAt, repository.getTaskById(taskId)?.dueAt)

        val deleted = repository.deleteTask(taskId)
        assertTrue(deleted.isSuccess)
        val snapshot = deleted.getOrThrow()
        assertFalse(repository.getOpenTasks().first().any { it.id == taskId })

        assertTrue(repository.restoreTask(snapshot).isSuccess)
        assertNotNull(repository.getTaskById(taskId))
        assertEquals(newDueAt, repository.getTaskById(taskId)?.dueAt)
    }

    @Test
    fun operationsOnMissingTaskReturnFailure() = runTest(dispatcher) {
        assertTrue(repository.completeTask(42L, 10L).isFailure)
        assertTrue(repository.rescheduleTask(42L, 10L).isFailure)
        assertTrue(repository.deleteTask(42L).isFailure)
    }

    private fun task(title: String, dueAt: Long? = null) = Task(
        title = title,
        dueAt = dueAt,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )
}
