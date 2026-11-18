package com.ego.mobile.feature.habits

import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitCalendar
import com.ego.mobile.domain.usecase.observeHabitOccurrenceSnapshot
import java.time.LocalDate
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrence
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeHabitRepository : HabitRepository {
    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())
    private val internalHabits = mutableListOf<Habit>()

    override fun observeHabitCalendars(): Flow<List<HabitCalendar>> = observeHabitOccurrenceSnapshot().map { snapshot ->
        snapshot.habits.map { HabitCalendar(it, emptyList(), snapshot.occurrencesByHabit[it.id].orEmpty()) }
    }

    override suspend fun correctOccurrence(habitId: Long, date: LocalDate, completed: Boolean): Result<Unit> =
        error("Not used in these tests")

    override suspend fun saveHabit(habit: Habit): Long {
        val nextId = if (internalHabits.isEmpty()) 1L else internalHabits.maxOf { it.id } + 1L
        val saved = habit.copy(id = nextId)
        internalHabits.add(saved)
        habitsFlow.value = internalHabits.toList()
        return saved.id
    }

    override suspend fun updateHabit(habit: Habit) {
        val index = internalHabits.indexOfFirst { it.id == habit.id }
        if (index >= 0) {
            internalHabits[index] = habit
            habitsFlow.value = internalHabits.toList()
        }
    }

    override suspend fun deleteHabit(habit: Habit) {
        internalHabits.removeIf { it.id == habit.id }
        habitsFlow.value = internalHabits.toList()
    }

    override suspend fun getHabitById(id: Long): Habit? = internalHabits.firstOrNull { it.id == id }

    override fun observeAllHabits(): Flow<List<Habit>> = habitsFlow

    override suspend fun completeOccurrence(habitId: Long, dateCivil: String, notes: String?): Result<Unit> {
        val habitsIndex = internalHabits.indexOfFirst { it.id == habitId }
        if (habitsIndex < 0) return Result.failure(IllegalArgumentException("Habit not found."))
        val updated = internalHabits[habitsIndex].copy(status = HabitStatus.ACTIVE, updatedAt = System.currentTimeMillis())
        internalHabits[habitsIndex] = updated
        habitsFlow.value = internalHabits.toList()
        return Result.success(Unit)
    }

    override suspend fun undoOccurrence(habitId: Long, dateCivil: String): Result<Unit> = Result.success(Unit)

    override suspend fun pauseHabit(habitId: Long): Result<Unit> {
        val habitsIndex = internalHabits.indexOfFirst { it.id == habitId }
        if (habitsIndex < 0) return Result.failure(IllegalArgumentException("Habit not found."))
        val updated = internalHabits[habitsIndex].copy(status = HabitStatus.PAUSED, updatedAt = System.currentTimeMillis())
        internalHabits[habitsIndex] = updated
        habitsFlow.value = internalHabits.toList()
        return Result.success(Unit)
    }

    override suspend fun resumeHabit(habitId: Long): Result<Unit> {
        val habitsIndex = internalHabits.indexOfFirst { it.id == habitId }
        if (habitsIndex < 0) return Result.failure(IllegalArgumentException("Habit not found."))
        val updated = internalHabits[habitsIndex].copy(status = HabitStatus.ACTIVE, updatedAt = System.currentTimeMillis())
        internalHabits[habitsIndex] = updated
        habitsFlow.value = internalHabits.toList()
        return Result.success(Unit)
    }

    override suspend fun archiveHabit(habitId: Long): Result<Unit> {
        val index = internalHabits.indexOfFirst { it.id == habitId }
        if (index < 0) return Result.failure(IllegalArgumentException("Habit not found."))
        internalHabits[index] = internalHabits[index].copy(status = HabitStatus.ARCHIVED)
        habitsFlow.value = internalHabits.toList()
        return Result.success(Unit)
    }

    override suspend fun getOccurrenceForDate(habitId: Long, dateCivil: String): HabitOccurrence? = null

    override fun observeOccurrencesForHabit(habitId: Long): Flow<List<HabitOccurrence>> = flowOf(emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observes habits split into active and paused sections`() = runTest(dispatcher) {
        val repository = FakeHabitRepository()
        val viewModel = HabitsViewModel(repository)

        repository.saveHabit(
            Habit(
                name = "Leitura",
                frequency = HabitFrequency.Daily,
                status = HabitStatus.ACTIVE,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
        )
        repository.saveHabit(
            Habit(
                name = "Meditação",
                frequency = HabitFrequency.Daily,
                status = HabitStatus.PAUSED,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
        )

        runCurrent()
        val state = viewModel.uiState.value
        assertEquals(1, state.activeHabits.size)
        assertEquals(1, state.pausedHabits.size)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `quick complete keeps the habit active and clears validation errors`() = runTest(dispatcher) {
        val repository = FakeHabitRepository()
        val viewModel = HabitsViewModel(repository)

        val id = repository.saveHabit(
            Habit(
                name = "Caminhada",
                frequency = HabitFrequency.Daily,
                status = HabitStatus.ACTIVE,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
        )

        runCurrent()
        viewModel.onQuickComplete(id)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.activeHabits.first().status == HabitStatus.ACTIVE)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `invalid name and description are reported without saving`() = runTest(dispatcher) {
        val repository = FakeHabitRepository()
        val viewModel = HabitsViewModel(repository)
        runCurrent()

        viewModel.onCreateHabit(" ", null, HabitFrequency.Daily)
        runCurrent()
        assertEquals("Habit name is required.", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.activeHabits.isEmpty())

        viewModel.onCreateHabit("Valid name", "D".repeat(251), HabitFrequency.Daily)
        runCurrent()
        assertEquals("Habit description must be 250 characters or less.", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.activeHabits.isEmpty())

        viewModel.onCreateHabit("N".repeat(81), null, HabitFrequency.Daily)
        runCurrent()
        assertEquals("Habit name must be 1 to 80 characters.", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.activeHabits.isEmpty())
    }
}
