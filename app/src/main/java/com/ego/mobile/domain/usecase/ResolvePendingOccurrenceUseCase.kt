package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus

class ResolvePendingOccurrenceUseCase {
    operator fun invoke(
        occurrence: HabitOccurrence,
        resolution: PendingOccurrenceResolution,
        notes: String? = null,
        completedAt: Long? = null,
    ): HabitOccurrence {
        require(notes == null || notes.length <= 250) {
            "Habit occurrence notes must be 250 characters or less."
        }

        return when (resolution) {
            PendingOccurrenceResolution.COMPLETE -> occurrence.copy(
                status = HabitOccurrenceStatus.COMPLETED,
                notes = notes,
                completedAt = completedAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
            PendingOccurrenceResolution.SKIP -> occurrence.copy(
                status = HabitOccurrenceStatus.SKIPPED,
                notes = notes,
                completedAt = null,
                updatedAt = System.currentTimeMillis(),
            )
            PendingOccurrenceResolution.KEEP_PENDING -> occurrence.copy(
                status = HabitOccurrenceStatus.PENDING,
                notes = notes,
                completedAt = null,
                updatedAt = System.currentTimeMillis(),
            )
        }
    }
}
