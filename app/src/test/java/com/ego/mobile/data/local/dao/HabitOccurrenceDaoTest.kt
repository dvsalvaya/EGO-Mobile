package com.ego.mobile.data.local.dao

import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeHabitOccurrenceDao : HabitOccurrenceDao {
    private val items = mutableMapOf<Long, HabitOccurrenceEntity>()
    private val state = MutableStateFlow<List<HabitOccurrenceEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(occurrence: HabitOccurrenceEntity): Long {
        val inserted = occurrence.copy(id = if (occurrence.id == 0L) nextId++ else occurrence.id)
        items[inserted.id] = inserted
        state.value = items.values.sortedBy { it.dateCivil }
        return inserted.id
    }

    override suspend fun update(occurrence: HabitOccurrenceEntity) {
        items[occurrence.id] = occurrence
        state.value = items.values.sortedBy { it.dateCivil }
    }

    override suspend fun getByHabitAndDate(habitId: Long, dateCivil: String): HabitOccurrenceEntity? {
        return items.values.firstOrNull { it.habitId == habitId && it.dateCivil == dateCivil }
    }

    override suspend fun deleteByHabitAndDate(habitId: Long, dateCivil: String) {
        items.entries.removeIf { it.value.habitId == habitId && it.value.dateCivil == dateCivil }
        state.value = items.values.sortedBy { it.dateCivil }
    }

    override fun observeByHabitId(habitId: Long): Flow<List<HabitOccurrenceEntity>> {
        return state.map { list -> list.filter { it.habitId == habitId } }
    }

    override suspend fun getByHabitAndDateRange(
        habitId: Long,
        startDate: String,
        endDate: String,
    ): List<HabitOccurrenceEntity> {
        return items.values.filter { it.habitId == habitId && it.dateCivil >= startDate && it.dateCivil <= endDate }
    }
}

class HabitOccurrenceDaoTest {

    @Test
    fun insertsAndReadsHabitOccurrenceByDate() = runTest {
        val dao = FakeHabitOccurrenceDao()
        val habitId = 1L
        val occurrence = HabitOccurrenceEntity(
            habitId = habitId,
            dateCivil = "2026-10-28",
            status = "PENDING",
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        val insertedId = dao.insert(occurrence)
        val byDate = dao.getByHabitAndDate(habitId, "2026-10-28")
        val all = dao.observeByHabitId(habitId).first()

        assertEquals(1L, insertedId)
        assertEquals("PENDING", byDate?.status)
        assertEquals(1, all.size)
    }
}