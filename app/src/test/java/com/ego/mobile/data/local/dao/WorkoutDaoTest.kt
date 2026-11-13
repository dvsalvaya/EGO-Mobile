package com.ego.mobile.data.local.dao

import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.local.relation.WorkoutSessionWithExercises
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FakeWorkoutDao : WorkoutDao {
    private val sessions = mutableMapOf<Long, WorkoutSessionEntity>()
    private val exercises = mutableMapOf<Long, WorkoutExerciseEntity>()

    private val sessionsFlow = MutableStateFlow<Map<Long, WorkoutSessionEntity>>(emptyMap())
    private val exercisesFlow = MutableStateFlow<Map<Long, WorkoutExerciseEntity>>(emptyMap())

    private var nextSessionId = 1L
    private var nextExerciseId = 1L

    private fun emit() {
        sessionsFlow.value = sessions.toMap()
        exercisesFlow.value = exercises.toMap()
    }

    override suspend fun insertSession(session: WorkoutSessionEntity): Long {
        val id = if (session.id == 0L) nextSessionId++ else session.id
        val entity = session.copy(id = id)
        sessions[id] = entity
        emit()
        return id
    }

    override suspend fun updateSession(session: WorkoutSessionEntity) {
        sessions[session.id] = session
        emit()
    }

    override suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity? {
        return sessions[sessionId]
    }

    override fun getActiveSession(): Flow<WorkoutSessionWithExercises?> {
        return sessionsFlow.map { sessionMap ->
            val active = sessionMap.values
                .filter { it.status == "IN_PROGRESS" || it.status == "PAUSED" }
                .maxByOrNull { it.startedAt }
            active?.let { session ->
                val sessionExercises = exercises.values
                    .filter { it.sessionId == session.id }
                    .sortedBy { it.orderIndex }
                WorkoutSessionWithExercises(session, sessionExercises)
            }
        }
    }

    override suspend fun getActiveSessionSync(): WorkoutSessionWithExercises? {
        val active = sessions.values
            .filter { it.status == "IN_PROGRESS" || it.status == "PAUSED" }
            .maxByOrNull { it.startedAt } ?: return null
        val sessionExercises = exercises.values
            .filter { it.sessionId == active.id }
            .sortedBy { it.orderIndex }
        return WorkoutSessionWithExercises(active, sessionExercises)
    }

    override fun getSessionWithExercises(sessionId: Long): Flow<WorkoutSessionWithExercises?> {
        return sessionsFlow.map {
            getSessionWithExercisesSync(sessionId)
        }
    }

    override suspend fun getSessionWithExercisesSync(sessionId: Long): WorkoutSessionWithExercises? {
        val session = sessions[sessionId] ?: return null
        val sessionExercises = exercises.values
            .filter { it.sessionId == sessionId }
            .sortedBy { it.orderIndex }
        return WorkoutSessionWithExercises(session, sessionExercises)
    }

    override fun getCompletedSessions(): Flow<List<WorkoutSessionWithExercises>> {
        return sessionsFlow.map { sessionMap ->
            sessionMap.values
                .filter { it.status == "COMPLETED" }
                .sortedByDescending { it.startedAt }
                .map { session ->
                    val sessionExercises = exercises.values
                        .filter { it.sessionId == session.id }
                        .sortedBy { it.orderIndex }
                    WorkoutSessionWithExercises(session, sessionExercises)
                }
        }
    }

    override suspend fun insertExercise(exercise: WorkoutExerciseEntity): Long {
        val id = if (exercise.id == 0L) nextExerciseId++ else exercise.id
        val entity = exercise.copy(id = id)
        exercises[id] = entity
        emit()
        return id
    }

    override suspend fun updateExercise(exercise: WorkoutExerciseEntity) {
        exercises[exercise.id] = exercise
        emit()
    }

    override suspend fun deleteExercise(exerciseId: Long) {
        exercises.remove(exerciseId)
        emit()
    }

    override fun getExercisesBySessionId(sessionId: Long): Flow<List<WorkoutExerciseEntity>> {
        return exercisesFlow.map {
            exercises.values.filter { it.sessionId == sessionId }.sortedBy { it.orderIndex }
        }
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessions.remove(sessionId)
        val iterator = exercises.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.sessionId == sessionId) {
                iterator.remove()
            }
        }
        emit()
    }
}

class WorkoutDaoTest {

    @Test
    fun `inserts session and retrieves as active session`() = runTest {
        val dao = FakeWorkoutDao()
        val sessionId = dao.insertSession(
            WorkoutSessionEntity(
                title = "Treino Pernas",
                startedAt = 1000L,
                status = "IN_PROGRESS",
            )
        )

        dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                name = "Agachamento",
                orderIndex = 0,
                setsCount = 4,
                reps = "10",
                weightKg = 80.0,
            )
        )

        val active = dao.getActiveSession().first()
        assertNotNull(active)
        assertEquals(sessionId, active?.session?.id)
        assertEquals("Treino Pernas", active?.session?.title)
        assertEquals(1, active?.exercises?.size)
        assertEquals("Agachamento", active?.exercises?.first()?.name)
    }

    @Test
    fun `pausing session keeps it active and completing clears active session`() = runTest {
        val dao = FakeWorkoutDao()
        val sessionId = dao.insertSession(
            WorkoutSessionEntity(
                title = "Treino Costas",
                startedAt = 2000L,
                status = "IN_PROGRESS",
            )
        )

        // Pausar
        var session = dao.getSessionById(sessionId)!!
        dao.updateSession(session.copy(status = "PAUSED"))

        var active = dao.getActiveSession().first()
        assertNotNull(active)
        assertEquals("PAUSED", active?.session?.status)

        // Concluir
        session = dao.getSessionById(sessionId)!!
        dao.updateSession(session.copy(status = "COMPLETED", endedAt = 4000L))

        active = dao.getActiveSession().first()
        assertNull(active)

        val completed = dao.getCompletedSessions().first()
        assertEquals(1, completed.size)
        assertEquals(sessionId, completed.first().session.id)
        assertEquals("COMPLETED", completed.first().session.status)
    }

    @Test
    fun `deleting exercise removes it from session`() = runTest {
        val dao = FakeWorkoutDao()
        val sessionId = dao.insertSession(
            WorkoutSessionEntity(
                title = "Treino Braço",
                startedAt = 1000L,
                status = "IN_PROGRESS",
            )
        )

        val exId = dao.insertExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                name = "Rosca Direta",
                orderIndex = 0,
                setsCount = 3,
                reps = "12",
            )
        )

        val beforeDelete = dao.getSessionWithExercises(sessionId).first()
        assertEquals(1, beforeDelete?.exercises?.size)

        dao.deleteExercise(exId)

        val afterDelete = dao.getSessionWithExercises(sessionId).first()
        assertEquals(0, afterDelete?.exercises?.size)
    }
}
