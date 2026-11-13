package com.ego.mobile.domain.repository

import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareRoutine
import kotlinx.coroutines.flow.Flow

interface HaircareRepository {
    fun observeActiveRoutine(): Flow<HaircareRoutine?>
    fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrence>>
    fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrence?>
    fun observeCompletedOccurrences(): Flow<List<HaircareOccurrence>>
    suspend fun createRoutine(
        routine: HaircareRoutine,
        occurrences: List<HaircareOccurrence>,
    ): Result<Long>
    suspend fun recordExecution(
        occurrenceId: Long,
        timestamp: Long,
        notes: String? = null,
    ): Result<Unit>
    suspend fun rescheduleStep(occurrenceId: Long, newDateCivil: String): Result<Unit>
}
