package com.ego.mobile.domain

import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutDomainTest {

    @Test
    fun `session title cannot be blank`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            WorkoutSession(
                title = "   ",
                startedAt = 1000L,
            )
        }
        assertTrue(ex.message.orEmpty().contains("title", ignoreCase = true))
    }

    @Test
    fun `session startedAt must be positive`() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            WorkoutSession(
                title = "Treino A",
                startedAt = 0L,
            )
        }
        assertTrue(ex.message.orEmpty().contains("startedAt", ignoreCase = true))
    }

    @Test
    fun `valid status transitions work as expected`() {
        val session = WorkoutSession(
            title = "Treino de Peito",
            startedAt = 1000L,
            status = WorkoutSessionStatus.IN_PROGRESS,
        )

        val paused = session.transitionTo(WorkoutSessionStatus.PAUSED)
        assertEquals(WorkoutSessionStatus.PAUSED, paused.status)

        val resumed = paused.transitionTo(WorkoutSessionStatus.IN_PROGRESS)
        assertEquals(WorkoutSessionStatus.IN_PROGRESS, resumed.status)

        val completed = resumed.transitionTo(WorkoutSessionStatus.COMPLETED, timestamp = 5000L)
        assertEquals(WorkoutSessionStatus.COMPLETED, completed.status)
        assertEquals(5000L, completed.endedAt)
    }

    @Test
    fun `terminal status cannot transition to any other status`() {
        val completedSession = WorkoutSession(
            title = "Treino Finalizado",
            startedAt = 1000L,
            endedAt = 3000L,
            status = WorkoutSessionStatus.COMPLETED,
        )

        assertFalse(completedSession.canTransitionTo(WorkoutSessionStatus.IN_PROGRESS))
        assertFalse(completedSession.canTransitionTo(WorkoutSessionStatus.PAUSED))

        assertThrows(IllegalStateException::class.java) {
            completedSession.transitionTo(WorkoutSessionStatus.PAUSED)
        }

        val cancelledSession = WorkoutSession(
            title = "Treino Cancelado",
            startedAt = 1000L,
            endedAt = 2000L,
            status = WorkoutSessionStatus.CANCELLED,
        )

        assertFalse(cancelledSession.canTransitionTo(WorkoutSessionStatus.IN_PROGRESS))
        assertThrows(IllegalStateException::class.java) {
            cancelledSession.transitionTo(WorkoutSessionStatus.IN_PROGRESS)
        }
    }

    @Test
    fun `durationMillis calculates elapsed time correctly`() {
        val completed = WorkoutSession(
            title = "Treino Concluído",
            startedAt = 1000L,
            endedAt = 4500L,
            status = WorkoutSessionStatus.COMPLETED,
        )
        assertEquals(3500L, completed.durationMillis(now = 10000L))

        val inProgress = WorkoutSession(
            title = "Treino em Curso",
            startedAt = 2000L,
            status = WorkoutSessionStatus.IN_PROGRESS,
        )
        assertEquals(3000L, inProgress.durationMillis(now = 5000L))
    }

    @Test
    fun `paused time is excluded from duration across multiple pause resume cycles`() {
        val started = WorkoutSession(title = "Treino", startedAt = 1_000L)

        val paused = started.transitionTo(WorkoutSessionStatus.PAUSED, timestamp = 3_000L)
        assertEquals(2_000L, paused.durationMillis(now = 30_000L))

        val resumed = paused.transitionTo(WorkoutSessionStatus.IN_PROGRESS, timestamp = 8_000L)
        assertEquals(5_000L, resumed.pausedDurationMillis)
        assertEquals(6_000L, resumed.durationMillis(now = 12_000L))

        val pausedAgain = resumed.transitionTo(WorkoutSessionStatus.PAUSED, timestamp = 14_000L)
        assertEquals(8_000L, pausedAgain.durationMillis(now = 40_000L))

        val resumedAgain = pausedAgain.transitionTo(WorkoutSessionStatus.IN_PROGRESS, timestamp = 18_000L)
        assertEquals(9_000L, resumedAgain.pausedDurationMillis)
        assertEquals(12_000L, resumedAgain.durationMillis(now = 22_000L))

        val completed = resumedAgain.transitionTo(WorkoutSessionStatus.COMPLETED, timestamp = 25_000L)
        assertEquals(15_000L, completed.durationMillis())
    }

    @Test
    fun `completing while paused excludes the open pause interval`() {
        val paused = WorkoutSession(title = "Treino", startedAt = 1_000L)
            .transitionTo(WorkoutSessionStatus.PAUSED, timestamp = 4_000L)

        val completed = paused.transitionTo(WorkoutSessionStatus.COMPLETED, timestamp = 10_000L)

        assertEquals(6_000L, completed.pausedDurationMillis)
        assertEquals(3_000L, completed.durationMillis())
    }

    @Test
    fun `exercise validation requires non blank name and max 80 chars`() {
        val blankEx = assertThrows(IllegalArgumentException::class.java) {
            WorkoutExercise(
                name = "  ",
                setsCount = 3,
            )
        }
        assertTrue(blankEx.message.orEmpty().contains("name", ignoreCase = true))

        val longNameEx = assertThrows(IllegalArgumentException::class.java) {
            WorkoutExercise(
                name = "A".repeat(81),
                setsCount = 3,
            )
        }
        assertTrue(longNameEx.message.orEmpty().contains("80", ignoreCase = true))
    }

    @Test
    fun `exercise setsCount must be greater than zero`() {
        val zeroSetsEx = assertThrows(IllegalArgumentException::class.java) {
            WorkoutExercise(
                name = "Supino",
                setsCount = 0,
            )
        }
        assertTrue(zeroSetsEx.message.orEmpty().contains("sets", ignoreCase = true))

        val negativeSetsEx = assertThrows(IllegalArgumentException::class.java) {
            WorkoutExercise(
                name = "Supino",
                setsCount = -1,
            )
        }
        assertTrue(negativeSetsEx.message.orEmpty().contains("sets", ignoreCase = true))
    }

    @Test
    fun `exercise weightKg cannot be negative`() {
        val negativeWeightEx = assertThrows(IllegalArgumentException::class.java) {
            WorkoutExercise(
                name = "Supino",
                setsCount = 3,
                weightKg = -5.0,
            )
        }
        assertTrue(negativeWeightEx.message.orEmpty().contains("weight", ignoreCase = true))
    }

    @Test
    fun `valid exercise creation`() {
        val exercise = WorkoutExercise(
            sessionId = 1L,
            name = "Supino Reto",
            orderIndex = 0,
            setsCount = 4,
            reps = "8-10",
            weightKg = 70.0,
            notes = "Aquecer antes",
        )
        assertEquals("Supino Reto", exercise.name)
        assertEquals(4, exercise.setsCount)
        assertEquals("8-10", exercise.reps)
        assertEquals(70.0, exercise.weightKg)
    }
}
