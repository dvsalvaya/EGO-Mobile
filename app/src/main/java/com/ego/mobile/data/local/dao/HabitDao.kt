package com.ego.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitCalendarEntities
import com.ego.mobile.data.local.entity.HabitPlanEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import com.ego.mobile.domain.model.HabitFrequency
import java.time.LocalDate
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

    @Transaction
    @Query("SELECT * FROM habits ORDER BY id")
    fun observeCalendars(): Flow<List<HabitCalendarEntities>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putPlan(plan: HabitPlanEntity)

    @Query("SELECT * FROM habit_plans WHERE habitId = :habitId AND effectiveFrom <= :date ORDER BY effectiveFrom DESC LIMIT 1")
    suspend fun planOn(habitId: Long, date: String): HabitPlanEntity?

    @Transaction
    suspend fun saveWithPlan(habit: HabitEntity, date: String): Long {
        val previous = if (habit.id == 0L) null else getById(habit.id)
        val id = if (previous == null) insert(habit) else {
            update(habit)
            habit.id
        }
        if (previous?.frequency != habit.frequency || previous.status != habit.status) {
            putPlan(HabitPlanEntity(id, date, habit.frequency, habit.status))
        }
        return id
    }

    @Transaction
    suspend fun updateWithPlan(habit: HabitEntity, date: String) {
        if (getById(habit.id) != null) saveWithPlan(habit, date)
    }

    @Transaction
    suspend fun changeStatus(id: Long, expected: String?, status: String, date: String, now: Long): Boolean {
        val habit = getById(id) ?: return false
        if (expected != null && habit.status != expected) return false
        saveWithPlan(habit.copy(status = status, updatedAt = now), date)
        return true
    }

    @Transaction
    suspend fun correctOccurrence(habitId: Long, date: String, completed: Boolean, occurredAt: Long, now: Long): Boolean {
        val existing = getOccurrence(habitId, date)
        if (existing == null) {
            val plan = planOn(habitId, date) ?: return false
            val frequency = HabitFrequency.fromPersistedValue(plan.frequency)
            if (plan.status != "ACTIVE" || !frequency.requiresOccurrenceOn(LocalDate.parse(date))) return false
        }
        val status = if (completed) "COMPLETED" else "SKIPPED"
        if (existing?.status == status) return true
        val occurrence = existing ?: HabitOccurrenceEntity(
            habitId = habitId, dateCivil = date, status = status, createdAt = now, updatedAt = now,
        )
        val corrected = occurrence.copy(status = status, completedAt = occurredAt.takeIf { completed }, updatedAt = now)
        if (existing == null) insertOccurrence(corrected) else updateOccurrence(corrected)
        return true
    }

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
