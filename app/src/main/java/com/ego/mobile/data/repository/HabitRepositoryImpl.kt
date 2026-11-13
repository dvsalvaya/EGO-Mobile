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
        val existing = habitOccurrenceDao.getByHabitAndDate(habitId, dateCivil)
        val occurrence = (existing?.toDomain() ?: HabitOccurrence(
            habitId = habitId,
            date = LocalDate.parse(dateCivil),
            status = HabitOccurrenceStatus.PENDING,
            createdAt = now,
            updatedAt = now,
        )).copy(
            status = HabitOccurrenceStatus.COMPLETED,
            completedAt = now,
            notes = notes,
            updatedAt = now,
        )

        if (existing == null) {
            habitOccurrenceDao.insert(occurrence.toEntity())
        } else {
            habitOccurrenceDao.update(occurrence.toEntity())
        }

        Result.success(Unit)
    }

    override suspend fun pauseHabit(habitId: Long): Result<Unit> = withContext(ioDispatcher) {
        val habit = habitDao.getById(habitId) ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        val updated = habit.toDomain().copy(status = HabitStatus.PAUSED, updatedAt = System.currentTimeMillis())
        habitDao.update(updated.toEntity())
        Result.success(Unit)
    }

    override suspend fun resumeHabit(habitId: Long): Result<Unit> = withContext(ioDispatcher) {
        val habit = habitDao.getById(habitId) ?: return@withContext Result.failure(IllegalArgumentException("Habit not found."))
        val updated = habit.toDomain().copy(status = HabitStatus.ACTIVE, updatedAt = System.currentTimeMillis())
        habitDao.update(updated.toEntity())
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
