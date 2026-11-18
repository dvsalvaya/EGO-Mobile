package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_occurrences WHERE habitId = :habitId AND dateCivil = :dateCivil LIMIT 1")
    suspend fun getOccurrence(habitId: Long, dateCivil: String): HabitOccurrenceEntity?

    @Query("SELECT COUNT(*) FROM habit_occurrences WHERE habitId = :habitId AND status = 'COMPLETED' AND dateCivil BETWEEN :weekStart AND :weekEnd")
    suspend fun countCompletedInWeek(habitId: Long, weekStart: String, weekEnd: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: HabitOccurrenceEntity): Long

    @Update
    suspend fun updateOccurrence(occurrence: HabitOccurrenceEntity)

    @Transaction
    suspend fun completeOccurrenceIfEligible(
        habitId: Long,
        dateCivil: String,
        weekStart: String,
        weekEnd: String,
        expectedFrequency: String,
        weeklyTarget: Int?,
        occurrence: HabitOccurrenceEntity,
    ): Boolean {
        val habit = getById(habitId) ?: return false
        if (habit.status != "ACTIVE" || habit.frequency != expectedFrequency) return false
        val existing = getOccurrence(habitId, dateCivil)
        if (existing?.status == "COMPLETED") return false
        if (weeklyTarget != null && countCompletedInWeek(habitId, weekStart, weekEnd) >= weeklyTarget) return false
        if (existing == null) insertOccurrence(occurrence) else updateOccurrence(
            occurrence.copy(id = existing.id, createdAt = existing.createdAt),
        )
        return true
    }

    @Transaction
    suspend fun undoOccurrence(habitId: Long, dateCivil: String, updatedAt: Long): Boolean {
        val habit = getById(habitId) ?: return false
        if (habit.status != "ACTIVE") return false
        val occurrence = getOccurrence(habitId, dateCivil) ?: return false
        if (occurrence.status != "COMPLETED") return false
        updateOccurrence(occurrence.copy(status = "PENDING", completedAt = null, updatedAt = updatedAt))
        return true
    }
}
