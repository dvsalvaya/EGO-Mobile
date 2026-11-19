package com.ego.mobile.feature.routines.haircare

import androidx.lifecycle.ViewModelStore
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircarePresetStep
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.repository.HaircareRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HaircareViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.of(2026, 11, 13)
    private lateinit var repository: FakeHaircareRepository
    private lateinit var viewModelStore: ViewModelStore

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeHaircareRepository()
        viewModelStore = ViewModelStore()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HaircareViewModel(
        repository = repository,
        clock = { 5_000L },
        today = { today },
    ).also { viewModelStore.put("haircare", it) }

    @Test
    fun `starts without configured routine`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        assertTrue(viewModel.uiState.value is HaircareUiState.NoRoutineConfigured)
    }

    @Test
    fun `adopts standard preset and exposes current and upcoming steps`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.adoptStandardPreset(today)
        runCurrent()

        val state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(HaircareRoutineSource.PRESET, state.routine.source)
        assertEquals(HaircareStepType.HIDRATACAO, state.currentStep?.stepType)
        assertEquals(today.toString(), state.currentStep?.scheduledDate)
        assertEquals(11, state.upcomingSteps.size)
    }

    @Test
    fun `creates custom routine and records execution in current state and history`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.createCustomRoutine(
            name = "Rotina personalizada",
            steps = listOf(
                HaircarePresetStep(HaircareStepType.NUTRICAO, "Nutrição", 0),
                HaircarePresetStep(HaircareStepType.RECONSTRUCAO, "Reconstrução", 3),
            ),
            startDate = today,
        )
        runCurrent()

        val current = (viewModel.uiState.value as HaircareUiState.ActiveRoutine).currentStep!!
        viewModel.recordExecution(current.id, "Óleo vegetal")
        runCurrent()

        val state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(HaircareRoutineSource.CUSTOM, state.routine.source)
        assertEquals(HaircareStepType.RECONSTRUCAO, state.currentStep?.stepType)
        assertEquals(HaircareOccurrenceStatus.COMPLETED, state.history.single().status)
        assertEquals(5_000L, state.history.single().executedAt)
        assertEquals("Óleo vegetal", state.history.single().notes)
    }

    @Test
    fun `reschedules current step and reflects the changed date`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.adoptStandardPreset(today)
        runCurrent()

        val current = (viewModel.uiState.value as HaircareUiState.ActiveRoutine).currentStep!!
        viewModel.rescheduleStep(current.id, "2026-11-20")
        runCurrent()

        val state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals("2026-11-15", state.currentStep?.scheduledDate)
        val rescheduled = state.upcomingSteps.first { it.id == current.id }
        assertEquals("2026-11-20", rescheduled.scheduledDate)
        assertEquals(HaircareOccurrenceStatus.RESCHEDULED, rescheduled.status)
    }

    @Test
    fun `calendar month navigation clamps selected day and filters occurrences`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.adoptStandardPreset(today)
        runCurrent()

        var state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(java.time.YearMonth.of(2026, 11), state.selectedMonth)
        assertTrue(state.calendarOccurrences.all { it.scheduledDate.startsWith("2026-11-") })

        viewModel.selectDate(LocalDate.of(2026, 11, 30))
        viewModel.showNextMonth()
        runCurrent()
        state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(java.time.YearMonth.of(2026, 12), state.selectedMonth)
        assertEquals(LocalDate.of(2026, 12, 30), state.selectedDate)

        viewModel.selectDate(LocalDate.of(2026, 12, 31))
        viewModel.showNextMonth()
        runCurrent()
        state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(LocalDate.of(2027, 1, 31), state.selectedDate)
        viewModel.showPreviousMonth()
        runCurrent()
        state = viewModel.uiState.value as HaircareUiState.ActiveRoutine
        assertEquals(LocalDate.of(2026, 12, 31), state.selectedDate)
    }
}

private class FakeHaircareRepository : HaircareRepository {
    private val activeRoutine = MutableStateFlow<HaircareRoutine?>(null)
    private val occurrences = mutableMapOf<Long, MutableStateFlow<List<HaircareOccurrence>>>()
    private val completed = MutableStateFlow<List<HaircareOccurrence>>(emptyList())
    private var nextRoutineId = 1L
    private var nextOccurrenceId = 1L

    override fun observeActiveRoutine(): Flow<HaircareRoutine?> = activeRoutine

    override fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrence>> =
        occurrences.getOrPut(routineId) { MutableStateFlow(emptyList()) }

    override fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrence?> =
        observeOccurrencesForRoutine(routineId).map { list ->
            list.filter {
                it.status == HaircareOccurrenceStatus.PENDING ||
                    it.status == HaircareOccurrenceStatus.RESCHEDULED
            }.minByOrNull { it.scheduledDate }
        }

    override fun observeCompletedOccurrences(): Flow<List<HaircareOccurrence>> = completed

    override suspend fun createRoutine(
        routine: HaircareRoutine,
        occurrences: List<HaircareOccurrence>,
    ): Result<Long> {
        val routineId = nextRoutineId++
        activeRoutine.value = routine.copy(id = routineId)
        val created = occurrences.map { it.copy(id = nextOccurrenceId++, routineId = routineId) }
        this.occurrences.getOrPut(routineId) { MutableStateFlow(emptyList()) }.value = created
        return Result.success(routineId)
    }

    override suspend fun recordExecution(
        occurrenceId: Long,
        timestamp: Long,
        notes: String?,
    ): Result<Unit> {
        val found = updateOccurrence(occurrenceId) {
            it.complete(timestamp, notes ?: it.notes)
        } ?: return Result.failure(IllegalArgumentException("Occurrence not found"))
        completed.value = (completed.value.filterNot { it.id == found.id } + found)
            .sortedByDescending { it.executedAt }
        return Result.success(Unit)
    }

    override suspend fun rescheduleStep(occurrenceId: Long, newDateCivil: String): Result<Unit> =
        if (updateOccurrence(occurrenceId) { it.reschedule(newDateCivil) } == null) {
            Result.failure(IllegalArgumentException("Occurrence not found"))
        } else {
            Result.success(Unit)
        }

    private fun updateOccurrence(
        occurrenceId: Long,
        transform: (HaircareOccurrence) -> HaircareOccurrence,
    ): HaircareOccurrence? {
        val state = occurrences.values.firstOrNull { flow ->
            flow.value.any { it.id == occurrenceId }
        } ?: return null
        val updated = state.value.map { occurrence ->
            if (occurrence.id == occurrenceId) transform(occurrence) else occurrence
        }
        state.value = updated
        return updated.first { it.id == occurrenceId }
    }
}
