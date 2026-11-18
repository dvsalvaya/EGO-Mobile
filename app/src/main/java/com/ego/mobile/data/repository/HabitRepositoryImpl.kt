package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.HabitDao
import com.ego.mobile.data.local.dao.HabitOccurrenceDao
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.DayOfWeek

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val habitOccurrenceDao: HabitOccurrenceDao? = null,
) : HabitRepository {

    override suspend fun saveHabit(habit: Habit): Long = withContext(ioDispatcher) {
        habitDao.insert(habit.toEntity())
    }

    override suspend fun updateHabit(habit: Habit) = withContext(ioDispatcher) {
        habitDao.update(habit.toEntity())
    }

    override suspend fun deleteHabit(habit: Habit) = withContext(ioDispatcher) {
        habitDao.delete(habit.toEntity())
    }

    override suspend fun getHabitById(id: Long): Habit? = withContext(ioDispatcher) {
        habitDao.getById(id)?.toDomain()
    }

    override fun observeAllHabits(): Flow<List<Habit>> = habitDao
        .getAll()
        .map { list -> list.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override suspend fun completeOccurrence(
        habitId: Long,
        dateCivil: String,
        notes: String?,
    ): Result<Unit> = withContext(ioDispatcher) {
        if (habitOccurrenceDao == null) {
            return@withContext Result.failure(IllegalStateException("Habit occurrence DAO is not configured."))
        }

        val now = System.currentTimeMillis()
        val habit = habitDao.getById(habitId)?.toDomain()
            ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        val date = LocalDate.parse(dateCivil)
        val existing = habitOccurrenceDao.getByHabitAndDate(habitId, dateCivil)
        val occurrence = (existing?.toDomain() ?: HabitOccurrence(
            habitId = habitId,
            date = date,
            status = HabitOccurrenceStatus.PENDING,
            createdAt = now,
            updatedAt = now,
        )).copy(
            status = HabitOccurrenceStatus.COMPLETED,
            completedAt = now,
            notes = notes,
            updatedAt = now,
        )

        val weekStart = date.with(DayOfWeek.MONDAY).toString()
        val weekEnd = date.with(DayOfWeek.SUNDAY).toString()
        val weeklyTarget = (habit.frequency as? com.ego.mobile.domain.model.HabitFrequency.TimesPerWeek)?.count
        val saved = habitDao.completeOccurrenceIfEligible(
            habitId = habitId,
            dateCivil = dateCivil,
            weekStart = weekStart,
            weekEnd = weekEnd,
            expectedFrequency = habit.frequency.toPersistedValue(),
            weeklyTarget = weeklyTarget,
            occurrence = occurrence.toEntity(),
        )
        if (saved) Result.success(Unit) else Result.failure(IllegalStateException("Habit is not eligible for completion."))
    }

    override suspend fun undoOccurrence(habitId: Long, dateCivil: String): Result<Unit> = withContext(ioDispatcher) {
        val changed = habitDao.undoOccurrence(habitId, dateCivil, System.currentTimeMillis())
        if (changed) Result.success(Unit) else Result.failure(IllegalStateException("No completed occurrence to undo."))
    }

    override suspend fun pauseHabit(habitId: Long): Result<Unit> = withContext(ioDispatcher) {
        val habit = habitDao.getById(habitId) ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        if (habit.status != HabitStatus.ACTIVE.name) {
            return@withContext Result.failure(IllegalStateException("Only active habits can be paused."))
        }
        val updated = habit.toDomain().copy(status = HabitStatus.PAUSED, updatedAt = System.currentTimeMillis())
        habitDao.update(updated.toEntity())
        Result.success(Unit)
    }

    override suspend fun resumeHabit(habitId: Long): Result<Unit> = withContext(ioDispatcher) {
        val habit = habitDao.getById(habitId) ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        if (habit.status != HabitStatus.PAUSED.name) {
            return@withContext Result.failure(IllegalStateException("Only paused habits can be resumed."))
        }
        val updated = habit.toDomain().copy(status = HabitStatus.ACTIVE, updatedAt = System.currentTimeMillis())
        habitDao.update(updated.toEntity())
        Result.success(Unit)
    }

    override suspend fun archiveHabit(habitId: Long): Result<Unit> = withContext(ioDispatcher) {
        val habit = habitDao.getById(habitId)?.toDomain()
            ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        habitDao.update(habit.copy(status = HabitStatus.ARCHIVED, updatedAt = System.currentTimeMillis()).toEntity())
        Result.success(Unit)
    }

    override suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence? = withContext(ioDispatcher) {
        habitOccurrenceDao?.getByHabitAndDate(habitId, dateCivil)?.toDomain()
    }

    override fun observeOccurrencesForHabit(habitId: Long): Flow<List<HabitOccurrence>> = habitOccurrenceDao
        ?.observeByHabitId(habitId)
        ?.map { list -> list.map { it.toDomain() } }
        ?.flowOn(ioDispatcher)
        ?: kotlinx.coroutines.flow.flowOf(emptyList())
}
