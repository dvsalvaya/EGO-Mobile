package com.ego.mobile.feature.routines.workouts

import androidx.lifecycle.ViewModelStore
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeWorkoutRepository : WorkoutRepository {
    private val activeSessionFlow = MutableStateFlow<WorkoutSession?>(null)
    private val completedSessionsFlow = MutableStateFlow<List<WorkoutSession>>(emptyList())
    private var nextExerciseId = 1L
    var loadError: Throwable? = null

    override fun getActiveSession(): Flow<WorkoutSession?> = loadError?.let { error ->
        flow { throw error }
    } ?: activeSessionFlow

    override suspend fun getActiveSessionSync(): WorkoutSession? = activeSessionFlow.value

    override fun getCompletedSessions(): Flow<List<WorkoutSession>> = completedSessionsFlow

    override fun getSessionById(sessionId: Long): Flow<WorkoutSession?> {
        return activeSessionFlow.map { if (it?.id == sessionId) it else null }
    }

    override suspend fun startSession(title: String, startedAt: Long): Result<WorkoutSession> {
        val existing = activeSessionFlow.value
        if (existing != null) return Result.success(existing)
        val created = WorkoutSession(
            id = 1L,
            title = title,
            startedAt = startedAt,
            status = WorkoutSessionStatus.IN_PROGRESS,
        )
        activeSessionFlow.value = created
        return Result.success(created)
    }

    override suspend fun pauseSession(sessionId: Long): Result<Unit> {
        val current = activeSessionFlow.value ?: return Result.failure(IllegalStateException("No session"))
        activeSessionFlow.value = current.transitionTo(WorkoutSessionStatus.PAUSED)
        return Result.success(Unit)
    }

    override suspend fun resumeSession(sessionId: Long): Result<Unit> {
        val current = activeSessionFlow.value ?: return Result.failure(IllegalStateException("No session"))
        activeSessionFlow.value = current.transitionTo(WorkoutSessionStatus.IN_PROGRESS)
        return Result.success(Unit)
    }

    override suspend fun completeSession(sessionId: Long, endedAt: Long): Result<Unit> {
        val current = activeSessionFlow.value ?: return Result.failure(IllegalStateException("No session"))
        val completed = current.transitionTo(WorkoutSessionStatus.COMPLETED, timestamp = endedAt)
        activeSessionFlow.value = null
        completedSessionsFlow.value = listOf(completed) + completedSessionsFlow.value
        return Result.success(Unit)
    }

    override suspend fun cancelSession(sessionId: Long): Result<Unit> {
        activeSessionFlow.value = null
        return Result.success(Unit)
    }

    override suspend fun deleteSession(sessionId: Long): Result<Unit> {
        activeSessionFlow.value = null
        return Result.success(Unit)
    }

    override suspend fun addExercise(exercise: WorkoutExercise): Result<Long> {
        val current = activeSessionFlow.value ?: return Result.failure(IllegalStateException("No session"))
        val id = nextExerciseId++
        val newEx = exercise.copy(id = id)
        activeSessionFlow.value = current.copy(exercises = current.exercises + newEx)
        return Result.success(id)
    }

    override suspend fun updateExercise(exercise: WorkoutExercise): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun removeExercise(exerciseId: Long): Result<Unit> {
        val current = activeSessionFlow.value ?: return Result.failure(IllegalStateException("No session"))
        activeSessionFlow.value = current.copy(exercises = current.exercises.filter { it.id != exerciseId })
        return Result.success(Unit)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeWorkoutRepository
    private lateinit var viewModelStore: ViewModelStore
    private var currentTime = 1000L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeWorkoutRepository()
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = WorkoutViewModel(repository, clock = { currentTime }).also {
        viewModelStore.put("workout", it)
    }

    @Test
    fun `starts session and updates uiState with active session`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        try {
            runCurrent()

            assertNull(viewModel.uiState.value.activeSession)
            assertFalse(viewModel.uiState.value.hasActiveSession)

            viewModel.startSession("Treino Costas")
            runCurrent()

            val active = viewModel.uiState.value.activeSession
            assertNotNull(active)
            assertEquals("Treino Costas", active?.title)
            assertTrue(viewModel.uiState.value.hasActiveSession)
        } finally {
            viewModelStore.clear()
        }
    }

    @Test
    fun `load errors are exposed as a user-visible state message`() = runTest(testDispatcher) {
        repository.loadError = IllegalStateException("Falha ao carregar treinos.")
        val viewModel = createViewModel()

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Falha ao carregar treinos.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `adds exercise to active session`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        try {
            runCurrent()

            viewModel.startSession("Treino Pernas")
            runCurrent()

            viewModel.addExercise(
                name = "Agachamento Livre",
                setsCount = 4,
                reps = "8-10",
                weightKg = 100.0,
            )
            runCurrent()

            val active = viewModel.uiState.value.activeSession
            assertEquals(1, active?.exercises?.size)
            assertEquals("Agachamento Livre", active?.exercises?.first()?.name)
            assertEquals(4, active?.exercises?.first()?.setsCount)
        } finally {
            viewModelStore.clear()
        }
    }

    @Test
    fun `pause, resume and complete session flow updates uiState correctly`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        try {
            runCurrent()

            viewModel.startSession("Treino Peito")
            runCurrent()

            viewModel.pauseSession()
            runCurrent()
            assertEquals(WorkoutSessionStatus.PAUSED, viewModel.uiState.value.activeSession?.status)

            viewModel.resumeSession()
            runCurrent()
            assertEquals(WorkoutSessionStatus.IN_PROGRESS, viewModel.uiState.value.activeSession?.status)

            currentTime = 5000L
            viewModel.completeSession()
            runCurrent()

            assertNull(viewModel.uiState.value.activeSession)
            assertFalse(viewModel.uiState.value.hasActiveSession)
            assertEquals(1, viewModel.uiState.value.completedSessions.size)
            assertEquals("Treino Peito", viewModel.uiState.value.completedSessions.first().title)
        } finally {
            viewModelStore.clear()
        }
    }
}
