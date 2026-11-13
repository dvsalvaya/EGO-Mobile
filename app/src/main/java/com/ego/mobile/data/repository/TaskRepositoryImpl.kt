package com.ego.mobile.data.repository

import android.database.SQLException
import com.ego.mobile.data.local.dao.TaskDao
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : TaskRepository {
    override suspend fun createTask(task: Task): Long =
        withContext(ioDispatcher) { taskDao.insert(task.toEntity()) }

    override suspend fun updateTask(task: Task) =
        withContext(ioDispatcher) { taskDao.update(task.toEntity()) }

    override suspend fun getTaskById(id: Long): Task? =
        withContext(ioDispatcher) { taskDao.getById(id)?.toDomain() }

    override fun getOpenTasks(): Flow<List<Task>> = taskDao
        .getOpenTasks()
        .map { tasks -> tasks.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override fun getCompletedTasks(): Flow<List<Task>> = taskDao
        .getCompletedTasks()
        .map { tasks -> tasks.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override suspend fun completeTask(id: Long, completedAt: Long): Result<Unit> = withContext(ioDispatcher) {
        if (completedAt <= 0L) {
            return@withContext Result.failure(IllegalArgumentException("Completion timestamp must be positive."))
        }
        try {
            if (taskDao.complete(id, completedAt) == 0) {
                return@withContext Result.failure(IllegalArgumentException("Open task not found."))
            }
            Result.success(Unit)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }

    override suspend fun rescheduleTask(id: Long, newDueAt: Long): Result<Unit> = withContext(ioDispatcher) {
        if (newDueAt <= 0L) {
            return@withContext Result.failure(IllegalArgumentException("Due date must be a positive timestamp."))
        }
        try {
            if (taskDao.reschedule(id, newDueAt, System.currentTimeMillis()) == 0) {
                return@withContext Result.failure(IllegalArgumentException("Open task not found."))
            }
            Result.success(Unit)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }

    override suspend fun deleteTask(id: Long): Result<Task> = withContext(ioDispatcher) {
        try {
            val task = taskDao.deleteById(id)?.toDomain()
                ?: return@withContext Result.failure(IllegalArgumentException("Task not found."))
            Result.success(task)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }

    override suspend fun restoreTask(task: com.ego.mobile.domain.model.Task): Result<Unit> = withContext(ioDispatcher) {
        try {
            taskDao.insert(task.toEntity())
            Result.success(Unit)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }
}
