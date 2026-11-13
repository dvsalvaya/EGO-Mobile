package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ResolvePendingOccurrenceUseCaseTest {

    @Test
    fun `marks a late occurrence as completed or skipped without collapsing to an implicit failure`() {
        val occurrence = HabitOccurrence(
            habitId = 7L,
            date = LocalDate.of(2026, 10, 28),
            status = HabitOccurrenceStatus.PENDING,
            createdAt = 1_000L,
            updatedAt = 1_000L,
        )

        val completed = ResolvePendingOccurrenceUseCase().invoke(
            occurrence = occurrence,
            resolution = PendingOccurrenceResolution.COMPLETE,
            notes = "Ajustei o dia atual",
            completedAt = 1_700L,
        )

        assertEquals(HabitOccurrenceStatus.COMPLETED, completed.status)
        assertEquals("Ajustei o dia atual", completed.notes)
        assertEquals(1_700L, completed.completedAt)

        val skipped = ResolvePendingOccurrenceUseCase().invoke(
            occurrence = occurrence,
            resolution = PendingOccurrenceResolution.SKIP,
            notes = "Feriado",
        )

        assertEquals(HabitOccurrenceStatus.SKIPPED, skipped.status)
        assertEquals("Feriado", skipped.notes)
        assertEquals(null, skipped.completedAt)

        val keptPending = ResolvePendingOccurrenceUseCase().invoke(
            occurrence = occurrence,
            resolution = PendingOccurrenceResolution.KEEP_PENDING,
        )

        assertEquals(HabitOccurrenceStatus.PENDING, keptPending.status)
    }
}
