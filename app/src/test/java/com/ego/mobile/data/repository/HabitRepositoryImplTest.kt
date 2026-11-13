package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.HabitDao
import com.ego.mobile.data.local.dao.HabitOccurrenceDao
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HabitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FakeHabitDao : HabitDao {
    val items = mutableMapOf<Long, HabitEntity>()
    private val flow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private var nextId = 1L

    private fun emit() {
        flow.value = items.values.sortedByDescending { it.createdAt }
    }

    override suspend fun insert(habit: HabitEntity): Long {
        val id = if (habit.id == 0L) nextId++ else habit.id
        val stored = habit.copy(id = id)
        items[id] = stored
        emit()
        return id
    }

    override suspend fun update(habit: HabitEntity) {
        items[habit.id] = habit
        emit()
    }

    override suspend fun delete(habit: HabitEntity) {
        items.remove(habit.id)
        emit()
    }

    override suspend fun getById(id: Long): HabitEntity? {
        return items[id]
    }

    override fun getAll(): Flow<List<HabitEntity>> {
        return flow
    }
}

class FakeHabitOccurrenceDao : HabitOccurrenceDao {
    val items = mutableMapOf<Long, HabitOccurrenceEntity>()
    private val flow = MutableStateFlow<List<HabitOccurrenceEntity>>(emptyList())
    private var nextId = 1L

    private fun emit() {
        flow.value = items.values.sortedBy { it.dateCivil }
    }

    override suspend fun insert(occurrence: HabitOccurrenceEntity): Long {
        val id = if (occurrence.id == 0L) nextId++ else occurrence.id
        items[id] = occurrence.copy(id = id)
        emit()
        return id
    }

    override suspend fun update(occurrence: HabitOccurrenceEntity) {
        items[occurrence.id] = occurrence
        emit()
    }

    override suspend fun getByHabitAndDate(habitId: Long, dateCivil: String): HabitOccurrenceEntity? {
        return items.values.firstOrNull { it.habitId == habitId && it.dateCivil == dateCivil }
    }

    override fun observeByHabitId(habitId: Long): Flow<List<HabitOccurrenceEntity>> {
        return flow.map { list -> list.filter { it.habitId == habitId } }
    }

    override suspend fun getByHabitAndDateRange(
        habitId: Long,
        startDate: String,
        endDate: String,
    ): List<HabitOccurrenceEntity> {
        return items.values
            .filter { it.habitId == habitId && it.dateCivil >= startDate && it.dateCivil <= endDate }
            .sortedBy { it.dateCivil }
    }
}

class HabitRepositoryImplTest {

    private lateinit var fakeDao: FakeHabitDao
    private lateinit var fakeOccurrenceDao: FakeHabitOccurrenceDao
    private lateinit var repository: HabitRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        fakeDao = FakeHabitDao()
        fakeOccurrenceDao = FakeHabitOccurrenceDao()
        repository = HabitRepositoryImpl(fakeDao, testDispatcher, fakeOccurrenceDao)
    }

    @Test
    fun saveHabitStoresEntityAndReturnsId() = runTest(testDispatcher) {
        val habit = Habit(
            name = "Leitura matinal",
            description = "15 minutos",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L
        )

        val id = repository.saveHabit(habit)
        assertEquals(1L, id)

        val retrieved = repository.getHabitById(id)
        assertNotNull(retrieved)
        assertEquals("Leitura matinal", retrieved?.name)
        assertEquals(HabitStatus.ACTIVE, retrieved?.status)
    }

    @Test
    fun updateHabitModifiesStoredEntity() = runTest(testDispatcher) {
        val habit = Habit(
            name = "Caminhada",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val id = repository.saveHabit(habit)

        val updated = habit.copy(id = id, status = HabitStatus.PAUSED, updatedAt = 1500L)
        repository.updateHabit(updated)

        val retrieved = repository.getHabitById(id)
        assertEquals(HabitStatus.PAUSED, retrieved?.status)
        assertEquals(1500L, retrieved?.updatedAt)
    }

    @Test
    fun deleteHabitRemovesEntity() = runTest(testDispatcher) {
        val habit = Habit(
            name = "Alongamento",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val id = repository.saveHabit(habit)
        repository.deleteHabit(habit.copy(id = id))

        val retrieved = repository.getHabitById(id)
        assertNull(retrieved)
    }

    @Test
    fun observeAllHabitsMapsEntitiesToDomainList() = runTest(testDispatcher) {
        val h1 = Habit(name = "Hábito 1", frequency = HabitFrequency.Daily, createdAt = 1000L, updatedAt = 1000L)
        val h2 = Habit(name = "Hábito 2", frequency = HabitFrequency.Daily, createdAt = 2000L, updatedAt = 2000L)

        repository.saveHabit(h1)
        repository.saveHabit(h2)

        val list = repository.observeAllHabits().first()
        assertEquals(2, list.size)
        assertEquals("Hábito 2", list[0].name)
        assertEquals("Hábito 1", list[1].name)
    }

    @Test
    fun completeOccurrenceMarksTheDayAsCompleted() = runTest(testDispatcher) {
        val habit = Habit(
            name = "Leitura",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val habitId = repository.saveHabit(habit)

        val result = repository.completeOccurrence(habitId, "2026-10-28", "Concluído")

        assertEquals(true, result.isSuccess)
        val occurrence = repository.getOccurrenceForDate(habitId, "2026-10-28")
        assertEquals(HabitOccurrenceStatus.COMPLETED, occurrence?.status)
        assertEquals("Concluído", occurrence?.notes)
    }

    @Test
    fun pauseHabitAndResumeHabitToggleStatus() = runTest(testDispatcher) {
        val habit = Habit(
            name = "Meditar",
            frequency = HabitFrequency.Daily,
            status = HabitStatus.ACTIVE,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val habitId = repository.saveHabit(habit)

        repository.pauseHabit(habitId)
        assertEquals(HabitStatus.PAUSED, repository.getHabitById(habitId)?.status)

        repository.resumeHabit(habitId)
        assertEquals(HabitStatus.ACTIVE, repository.getHabitById(habitId)?.status)
    }
}
