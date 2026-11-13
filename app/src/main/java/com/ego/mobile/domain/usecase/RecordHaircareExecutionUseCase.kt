package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.repository.HaircareRepository

class RecordHaircareExecutionUseCase(
    private val haircareRepository: HaircareRepository,
) {
    suspend operator fun invoke(
        occurrenceId: Long,
        timestamp: Long = System.currentTimeMillis(),
        notes: String? = null,
    ) = haircareRepository.recordExecution(occurrenceId, timestamp, notes)
}
