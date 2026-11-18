package com.ego.mobile.domain.usecase

import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
class GetWorkoutStatisticsUseCaseTest {

    private val repository = FakeWorkoutRepository()
    private val useCase = GetWorkoutStatisticsUseCase(repository)

    @Test
    fun `returns zeros when no completed sessions exist`() = runTest {
        val statistics = useCase()
        assertEquals(0, statistics.totalCompletedWorkouts)
        assertEquals(0.0, statistics.averageDurationMinutes, 0.01)
        assertEquals(0, statistics.exerciseLoads.size)
    }

    @Test
    fun `calculates statistics from completed sessions`() = runTest {
        val session1 = WorkoutSession(
            id = 1L,
            title = "Treino A",
            startedAt = 1000L,
            endedAt = 3000L,
            status = WorkoutSessionStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    sessionId = 1L,
                    name = "Supino",
                    orderIndex = 0,
                    setsCount = 3,
                    reps = "10",
                    weightKg = 80.0,
                    notes = null,
                ),
                WorkoutExercise(
                    id = 2L,
                    sessionId = 1L,
                    name = "Agachamento",
                    orderIndex = 1,
                    setsCount = 4,
                    reps = "8",
                    weightKg = 100.0,
                    notes = null,
                )
            )
        )

        val session2 = WorkoutSession(
            id = 2L,
            title = "Treino B",
            startedAt = 4000L,
            endedAt = 6000L,
            status = WorkoutSessionStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 3L,
                    sessionId = 2L,
                    name = "Supino",
                    orderIndex = 0,
                    setsCount = 3,
                    reps = "6",
                    weightKg = 85.0,
                    notes = null,
                ),
                WorkoutExercise(
                    id = 4L,
                    sessionId = 2L,
                    name = "Supino",
                    orderIndex = 1,
                    setsCount = 3,
                    reps = "8",
                    weightKg = 75.0,
                    notes = null,
                ),
                WorkoutExercise(
                    id = 5L,
                    sessionId = 2L,
                    name = "Remada",
                    orderIndex = 1,
                    setsCount = 3,
                    reps = "10",
                    weightKg = null,
                    notes = null,
                )
            )
        )

        repository.completedSessions.value = listOf(session1, session2)

        val statistics = useCase()

        // Total de treinos concluídos
        assertEquals(2, statistics.totalCompletedWorkouts)

        // Duração média: (2000ms + 2000ms) / 2 = 2000ms ≈ 0,0333 minuto.
        assertEquals(2000.0 / 60_000.0, statistics.averageDurationMinutes, 0.0001)

        // Estatísticas por exercício
        // Supino: max load = 85.0kg (de session2), appeared in 2 sessions
        // Agachamento: max load = 100.0kg (de session1), appeared in 1 session
        // Remada: weightKg is null, should be skipped
        assertEquals(3, statistics.exerciseLoads.size)

        val supino = statistics.exerciseLoads.find { it.exerciseName == "Supino" }!!
        assertEquals(85.0, supino.maxLoadKg!!, 0.01)
        assertEquals(2, supino.sessionCount)

        val agachamento = statistics.exerciseLoads.find { it.exerciseName == "Agachamento" }!!
        assertEquals(100.0, agachamento.maxLoadKg!!, 0.01)
        assertEquals(1, agachamento.sessionCount)

        val remada = statistics.exerciseLoads.find { it.exerciseName == "Remada" }
        assertEquals(null, remada?.maxLoadKg)
        // Remada aparece em uma sessão concluída, mas não tem carga comparável.
        assertEquals(1, remada?.sessionCount ?: 0)
    }

    @Test
    fun `average duration excludes persisted pause intervals`() {
        val pausedSession = WorkoutSession(
            id = 15L,
            title = "Treino com pausas",
            startedAt = 1_000L,
            endedAt = 13_000L,
            status = WorkoutSessionStatus.COMPLETED,
            pausedDurationMillis = 5_000L,
        )

        val statistics = useCase.fromSessions(listOf(pausedSession))

        assertEquals(8_000.0 / 60_000.0, statistics.averageDurationMinutes, 0.0001)
    }

    @Test
    fun `ignores non-completed sessions`() = runTest {
        val sessionInProgress = WorkoutSession(
            id = 1L,
            title = "Treino A",
            startedAt = 1000L,
            status = WorkoutSessionStatus.IN_PROGRESS,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    sessionId = 1L,
                    name = "Supino",
                    orderIndex = 0,
                    setsCount = 3,
                    reps = "10",
                    weightKg = 80.0,
                    notes = null,
                )
            )
        )

        val sessionPaused = WorkoutSession(
            id = 2L,
            title = "Treino B",
            startedAt = 2000L,
            endedAt = 4000L,
            status = WorkoutSessionStatus.PAUSED,
            exercises = listOf(
                WorkoutExercise(
                    id = 2L,
                    sessionId = 2L,
                    name = "Agachamento",
                    orderIndex = 0,
                    setsCount = 4,
                    reps = "8",
                    weightKg = 100.0,
                    notes = null,
                )
            )
        )

        repository.completedSessions.value = listOf(sessionInProgress, sessionPaused)

        val statistics = useCase()

        assertEquals(0, statistics.totalCompletedWorkouts)
        assertEquals(0.0, statistics.averageDurationMinutes, 0.01)
        assertEquals(0, statistics.exerciseLoads.size)
    }

    @Test
    fun `sessionsWithoutWeightAreIncludedInCountButNotInMaxLoad`() = runTest {
        val session = WorkoutSession(
            id = 1L,
            title = "Treino A",
            startedAt = 1000L,
            endedAt = 3000L,
            status = WorkoutSessionStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    sessionId = 1L,
                    name = "Supino",
                    orderIndex = 0,
                    setsCount = 3,
                    reps = "10",
                    weightKg = null,
                    notes = null,
                )
            )
        )

        repository.completedSessions.value = listOf(session)

        val statistics = useCase()

        assertEquals(1, statistics.totalCompletedWorkouts)
        assertEquals(1, statistics.exerciseLoads.size)
        val supino = statistics.exerciseLoads.first()
        assertEquals(null, supino.maxLoadKg)
        assertEquals(1, supino.sessionCount)
    }

    @Test
    fun `only_completed_sessions_integrate_statistics`() = runTest {
        val completed = WorkoutSession(
            id = 1L,
            title = "Treino Concluído",
            startedAt = 1000L,
            endedAt = 5000L,
            status = WorkoutSessionStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    sessionId = 1L,
                    name = "Supino",
                    orderIndex = 0,
                    setsCount = 3,
                    reps = "10",
                    weightKg = 80.0,
                    notes = null,
                )
            )
        )

        val incomplete = WorkoutSession(
            id = 2L,
            title = "Treino Incompleto",
            startedAt = 6000L,
            status = WorkoutSessionStatus.IN_PROGRESS,
            exercises = listOf(
                WorkoutExercise(
                    id = 2L,
                    sessionId = 2L,
                    name = "Agachamento",
                    orderIndex = 0,
                    setsCount = 4,
                    reps = "8",
                    weightKg = 120.0,
                    notes = null,
                )
            )
        )

        repository.completedSessions.value = listOf(completed, incomplete)

        val statistics = useCase()

        assertEquals(1, statistics.totalCompletedWorkouts)
        assertEquals(1, statistics.exerciseLoads.size)
        val supino = statistics.exerciseLoads.first()
        assertEquals(80.0, supino.maxLoadKg!!, 0.01)
        assertEquals(1, supino.sessionCount)
    }

    @Test
    fun `groups exercise names after trim ignoring case and orders load points chronologically`() = runTest {
        val first = completedSession(
            id = 1L,
            endedAt = 1_000L,
            startedAt = 1L,
            exerciseName = "  Supino ",
            weightKg = 80.0,
            reps = "10",
        )
        val second = completedSession(
            id = 2L,
            endedAt = 2_000L,
            startedAt = 1_000L,
            exerciseName = "supino",
            weightKg = 85.0,
            reps = "5x5",
        )
        repository.completedSessions.value = listOf(second, first)

        val load = useCase().exerciseLoads.single()

        assertEquals("Supino", load.exerciseName)
        assertEquals(85.0, load.maxLoadKg!!, 0.01)
        assertEquals(2, load.sessionCount)
        assertEquals(listOf(1L, 2L), load.points.map { it.sessionId })
        assertEquals(listOf(80.0, 85.0), load.points.map { it.weightKg })
    }

    @Test
    fun `uses the highest positive weight per session and skips incomparable points`() = runTest {
        val session = WorkoutSession(
            id = 7L,
            title = "Treino",
            startedAt = 1L,
            endedAt = 1_000L,
            status = WorkoutSessionStatus.COMPLETED,
            exercises = listOf(
                exercise(name = "Supino", orderIndex = 0, weightKg = 0.0),
                exercise(name = "Supino", orderIndex = 1, weightKg = 70.0),
                exercise(name = "Supino", orderIndex = 2, weightKg = 65.0, reps = "100"),
                exercise(name = "Remada", orderIndex = 3, weightKg = null),
            ),
        )
        repository.completedSessions.value = listOf(session)

        val loads = useCase().exerciseLoads
        val supino = loads.single { it.exerciseName == "Supino" }
        val remada = loads.single { it.exerciseName == "Remada" }

        assertEquals(70.0, supino.maxLoadKg!!, 0.01)
        assertEquals(listOf(70.0), supino.points.map { it.weightKg })
        assertEquals(null, remada.maxLoadKg)
        assertTrue(remada.points.isEmpty())
        assertEquals(1, remada.sessionCount)
    }

    private fun completedSession(
        id: Long,
        startedAt: Long,
        endedAt: Long,
        exerciseName: String,
        weightKg: Double,
        reps: String,
    ) = WorkoutSession(
        id = id,
        title = "Treino $id",
        startedAt = startedAt,
        endedAt = endedAt,
        status = WorkoutSessionStatus.COMPLETED,
        exercises = listOf(exercise(name = exerciseName, weightKg = weightKg, reps = reps)),
    )

    private fun exercise(
        name: String,
        orderIndex: Int = 0,
        weightKg: Double? = null,
        reps: String? = null,
    ) = WorkoutExercise(
        name = name,
        orderIndex = orderIndex,
        setsCount = 3,
        weightKg = weightKg,
        reps = reps,
    )

    private class FakeWorkoutRepository : WorkoutRepository {
        val completedSessions = MutableStateFlow<List<WorkoutSession>>(emptyList())

        override fun getActiveSession(): Flow<WorkoutSession?> = TODO("Not used by this test")
        override suspend fun getActiveSessionSync(): WorkoutSession? = TODO("Not used by this test")
        override fun getCompletedSessions(): Flow<List<WorkoutSession>> = completedSessions
        override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> = TODO("Not used by this test")
        override suspend fun startSession(title: String, startedAt: Long): Result<WorkoutSession> = TODO("Not used by this test")
        override suspend fun pauseSession(sessionId: Long): Result<Unit> = TODO("Not used by this test")
        override suspend fun resumeSession(sessionId: Long): Result<Unit> = TODO("Not used by this test")
        override suspend fun completeSession(sessionId: Long, endedAt: Long): Result<Unit> = TODO("Not used by this test")
        override suspend fun cancelSession(sessionId: Long): Result<Unit> = TODO("Not used by this test")
        override suspend fun deleteSession(sessionId: Long): Result<Unit> = TODO("Not used by this test")
        override suspend fun addExercise(exercise: WorkoutExercise): Result<Long> = TODO("Not used by this test")
        override suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit> = TODO("Not used by this test")
        override suspend fun removeExercise(exerciseId: Long): Result<Unit> = TODO("Not used by this test")
    }
}
