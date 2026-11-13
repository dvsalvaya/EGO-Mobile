package com.ego.mobile.domain.repository

import com.ego.mobile.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun createTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun getTaskById(id: Long): Task?
    fun getOpenTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    suspend fun completeTask(id: Long, completedAt: Long): Result<Unit>
    suspend fun rescheduleTask(id: Long, newDueAt: Long): Result<Unit>
    suspend fun deleteTask(id: Long): Result<Task>
    suspend fun restoreTask(task: Task): Result<Unit>
}
