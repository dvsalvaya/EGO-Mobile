package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TaskEntity?

    @Query(
        "UPDATE tasks SET status = 'COMPLETED', completedAt = :completedAt, updatedAt = :completedAt " +
            "WHERE id = :id AND status = 'OPEN'",
    )
    suspend fun complete(id: Long, completedAt: Long): Int

    @Query("UPDATE tasks SET dueAt = :newDueAt, updatedAt = :updatedAt WHERE id = :id AND status = 'OPEN'")
    suspend fun reschedule(id: Long, newDueAt: Long, updatedAt: Long): Int

    @Transaction
    suspend fun deleteById(id: Long): TaskEntity? {
        val task = getById(id) ?: return null
        delete(task)
        return task
    }

    @Query(
        "SELECT * FROM tasks WHERE status = 'OPEN' " +
            "ORDER BY CASE WHEN dueAt IS NULL THEN 1 ELSE 0 END ASC, dueAt ASC, createdAt DESC",
    )
    fun getOpenTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completedAt DESC, updatedAt DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
}
