package com.ego.mobile.data.repository

import android.database.SQLException
import com.ego.mobile.data.local.dao.HaircareDao
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.repository.HaircareRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.ZoneId

class HaircareRepositoryImpl(
    private val haircareDao: HaircareDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) : HaircareRepository {

    override fun observeActiveRoutine(): Flow<HaircareRoutine?> = haircareDao
        .getActiveRoutine()
        .map { it?.toDomain() }
        .flowOn(ioDispatcher)

    override fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrence>> = haircareDao
        .observeOccurrencesForRoutine(routineId)
        .map { occurrences -> occurrences.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrence?> = haircareDao
        .observeNextPendingOccurrence(routineId)
        .map { it?.toDomain() }
        .flowOn(ioDispatcher)

    override fun observeCompletedOccurrences(): Flow<List<HaircareOccurrence>> = haircareDao
        .observeCompletedOccurrences()
        .map { occurrences -> occurrences.map { it.toDomain() } }
        .flowOn(ioDispatcher)

    override suspend fun createRoutine(
        routine: HaircareRoutine,
        occurrences: List<HaircareOccurrence>,
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            Result.success(
                haircareDao.insertRoutineWithOccurrences(
                    routine.toEntity(),
                    occurrences.map { it.toEntity() },
                ),
            )
        } catch (error: IllegalArgumentException) {
            Result.failure(error)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }

    override suspend fun recordExecution(
        occurrenceId: Long,
        timestamp: Long,
        notes: String?,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            require(timestamp > 0L) { "Execution timestamp must be positive." }
            require(notes == null || notes.length <= 250) { "Notes must be 250 characters or less." }
            val entity = haircareDao.getOccurrenceById(occurrenceId)
                ?: throw IllegalArgumentException("Haircare occurrence not found.")
            val occurrence = entity.toDomain()
            require(
                occurrence.status == HaircareOccurrenceStatus.PENDING ||
                    occurrence.status == HaircareOccurrenceStatus.RESCHEDULED,
            ) { "Only pending haircare occurrences can be completed." }

            haircareDao.updateOccurrence(
                occurrence.complete(timestamp, notes ?: occurrence.notes).toEntity(),
            )
            Result.success(Unit)
        } catch (error: IllegalArgumentException) {
            Result.failure(error)
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }

    override suspend fun rescheduleStep(
        occurrenceId: Long,
        newDateCivil: String,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val newDate = LocalDate.parse(newDateCivil)
            require(newDate.toString() == newDateCivil) { "Date must use YYYY-MM-DD format." }
            require(newDate.isAfter(today())) { "Rescheduled date must be in the future." }
            val entity = haircareDao.getOccurrenceById(occurrenceId)
                ?: throw IllegalArgumentException("Haircare occurrence not found.")
            val occurrence = entity.toDomain()
            require(
                occurrence.status == HaircareOccurrenceStatus.PENDING ||
                    occurrence.status == HaircareOccurrenceStatus.RESCHEDULED,
            ) { "Only pending haircare occurrences can be rescheduled." }

            haircareDao.updateOccurrence(occurrence.reschedule(newDateCivil).toEntity())
            Result.success(Unit)
        } catch (error: IllegalArgumentException) {
            Result.failure(error)
        } catch (error: DateTimeException) {
            Result.failure(IllegalArgumentException("Date must use YYYY-MM-DD format.", error))
        } catch (error: SQLException) {
            Result.failure(error)
        }
    }
}
