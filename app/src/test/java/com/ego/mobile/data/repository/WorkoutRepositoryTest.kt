package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.FakeWorkoutDao
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSessionStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryTest {

    private lateinit var dao: FakeWorkoutDao
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeWorkoutDao()
        repository = WorkoutRepositoryImpl(dao)
    }

    @Test
    fun `startSession creates active session and returns it`() = runTest {
        val result = repository.startSession(title = "Treino A", startedAt = 1000L)
        assertTrue(result.isSuccess)
        val session = result.getOrThrow()

        assertEquals("Treino A", session.title)
        assertEquals(WorkoutSessionStatus.IN_PROGRESS, session.status)
        assertEquals(1000L, session.startedAt)

        val active = repository.getActiveSession().first()
        assertNotNull(active)
        assertEquals(session.id, active?.id)
    }

    @Test
    fun `startSession is idempotent when active session already exists`() = runTest {
        val firstResult = repository.startSession(title = "Treino A", startedAt = 1000L)
        val firstSession = firstResult.getOrThrow()

        // Segunda tentativa com outro título enquanto a primeira ainda está ativa
        val secondResult = repository.startSession(title = "Treino B", startedAt = 2000L)
        val secondSession = secondResult.getOrThrow()

        // Deve retornar a mesma primeira sessão sem criar uma segunda
        assertEquals(firstSession.id, secondSession.id)
        assertEquals("Treino A", secondSession.title)

        // Verifica que só existe uma sessão
        val active = repository.getActiveSession().first()
        assertEquals(firstSession.id, active?.id)
    }

    @Test
    fun `addExercise, pause, resume, complete session flow`() = runTest {
        val session = repository.startSession(title = "Treino Superior", startedAt = 1000L).getOrThrow()

        // Adicionar exercício
        val exResult = repository.addExercise(
            WorkoutExercise(
                sessionId = session.id,
                name = "Supino Inclinado",
                orderIndex = 0,
                setsCount = 4,
                reps = "10",
                weightKg = 60.0,
            )
        )
        assertTrue(exResult.isSuccess)

        var active = repository.getActiveSession().first()
        assertEquals(1, active?.exercises?.size)
        assertEquals("Supino Inclinado", active?.exercises?.first()?.name)

        // Pausar
        val pauseResult = repository.pauseSession(session.id)
        assertTrue(pauseResult.isSuccess)
        active = repository.getActiveSession().first()
        assertEquals(WorkoutSessionStatus.PAUSED, active?.status)

        // Retomar
        val resumeResult = repository.resumeSession(session.id)
        assertTrue(resumeResult.isSuccess)
        active = repository.getActiveSession().first()
        assertEquals(WorkoutSessionStatus.IN_PROGRESS, active?.status)

        // Finalizar
        val completeResult = repository.completeSession(session.id, endedAt = 4000L)
        assertTrue(completeResult.isSuccess)

        // Não há mais sessão ativa
        active = repository.getActiveSession().first()
        assertNull(active)

        // Está no histórico
        val completed = repository.getCompletedSessions().first()
        assertEquals(1, completed.size)
        assertEquals(session.id, completed.first().id)
        assertEquals(WorkoutSessionStatus.COMPLETED, completed.first().status)
        assertEquals(4000L, completed.first().endedAt)
        assertEquals(1, completed.first().exercises.size)
    }

    @Test
    fun `removeExercise removes exercise from active session`() = runTest {
        val session = repository.startSession(title = "Treino Pernas", startedAt = 1000L).getOrThrow()
        val exId = repository.addExercise(
            WorkoutExercise(
                sessionId = session.id,
                name = "Leg Press",
                orderIndex = 0,
                setsCount = 3,
                reps = "15",
            )
        ).getOrThrow()

        var active = repository.getActiveSession().first()
        assertEquals(1, active?.exercises?.size)

        repository.removeExercise(exId)
        active = repository.getActiveSession().first()
        assertEquals(0, active?.exercises?.size)
    }

    @Test
    fun `cancelSession cancels active session and clears it from active flow`() = runTest {
        val session = repository.startSession(title = "Treino Cancelado", startedAt = 1000L).getOrThrow()
        repository.cancelSession(session.id)

        val active = repository.getActiveSession().first()
        assertNull(active)
    }
}
