package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.HabitDao
import com.ego.mobile.data.local.dao.HabitOccurrenceDao
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitPlan
import com.ego.mobile.domain.model.habitZone
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
import java.time.Clock
import java.time.Instant

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val habitOccurrenceDao: HabitOccurrenceDao? = null,
    private val clock: Clock = Clock.system(habitZone),
) : HabitRepository {

    override suspend fun saveHabit(habit: Habit): Long = withContext(ioDispatcher) {
        val start = if (habit.id == 0L) Instant.ofEpochMilli(habit.createdAt) else clock.instant()
        habitDao.saveWithPlan(habit.toEntity(), start.atZone(clock.zone).toLocalDate().toString())
    }

    override suspend fun updateHabit(habit: Habit) = withContext(ioDispatcher) {
        habitDao.updateWithPlan(habit.toEntity(), LocalDate.now(clock).toString())
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

    override fun observeHabitCalendars(): Flow<List<HabitCalendar>> = habitDao.observeCalendars().map { records ->
        records.map { record ->
            HabitCalendar(
                habit = record.habit.toDomain(),
                plans = record.plans.map { plan ->
                    HabitPlan(
                        LocalDate.parse(plan.effectiveFrom),
                        HabitFrequency.fromPersistedValue(plan.frequency),
                        HabitStatus.valueOf(plan.status),
                    )
                },
                occurrences = record.occurrences.map { it.toDomain() },
            )
        }
    }.flowOn(ioDispatcher)

    override suspend fun correctOccurrence(habitId: Long, date: LocalDate, completed: Boolean): Result<Unit> = withContext(ioDispatcher) {
        val now = clock.instant()
        if (date > now.atZone(clock.zone).toLocalDate()) {
            return@withContext Result.failure(IllegalArgumentException("A data ainda não ocorreu."))
        }
        val changed = habitDao.correctOccurrence(
            habitId, date.toString(), completed, date.atStartOfDay(clock.zone).toInstant().toEpochMilli(), now.toEpochMilli(),
        )
        if (changed) Result.success(Unit) else Result.failure(IllegalArgumentException("Data fora do período do hábito."))
    }

    override suspend fun completeOccurrence(
        habitId: Long,
        dateCivil: String,
        notes: String?,
    ): Result<Unit> = withContext(ioDispatcher) {
        if (habitOccurrenceDao == null) {
            return@withContext Result.failure(IllegalStateException("Habit occurrence DAO is not configured."))
        }

        val now = clock.millis()
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
        val weeklyTarget = (habit.frequency as? HabitFrequency.TimesPerWeek)?.count
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
        val changed = habitDao.undoOccurrence(habitId, dateCivil, clock.millis())
        if (changed) Result.success(Unit) else Result.failure(IllegalStateException("No completed occurrence to undo."))
    }

    override suspend fun pauseHabit(habitId: Long) = changeStatus(habitId, HabitStatus.ACTIVE, HabitStatus.PAUSED)
    override suspend fun resumeHabit(habitId: Long) = changeStatus(habitId, HabitStatus.PAUSED, HabitStatus.ACTIVE)
    override suspend fun archiveHabit(habitId: Long) = changeStatus(habitId, null, HabitStatus.ARCHIVED)

    private suspend fun changeStatus(habitId: Long, expected: HabitStatus?, status: HabitStatus): Result<Unit> =
        withContext(ioDispatcher) {
            val now = clock.instant()
            val changed = habitDao.changeStatus(
                habitId, expected?.name, status.name, now.atZone(clock.zone).toLocalDate().toString(), now.toEpochMilli(),
            )
            if (changed) Result.success(Unit) else Result.failure(IllegalStateException("O hábito não está disponível para esta ação."))
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
