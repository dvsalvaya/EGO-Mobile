package com.ego.mobile.data.repository

import com.ego.mobile.data.local.dao.HaircareDao
import com.ego.mobile.data.local.entity.HaircareOccurrenceEntity
import com.ego.mobile.data.local.entity.HaircareRoutineEntity
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircareRoutine
import com.ego.mobile.domain.model.HaircareRoutineSource
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.domain.usecase.RecordHaircareExecutionUseCase
import com.ego.mobile.domain.usecase.RescheduleHaircareStepUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HaircareRepositoryTest {
    private lateinit var dao: FakeHaircareDao
    private lateinit var repository: HaircareRepositoryImpl
    private val today = LocalDate.of(2026, 11, 13)

    @Before
    fun setUp() {
        dao = FakeHaircareDao()
        repository = HaircareRepositoryImpl(dao, today = { today })
    }

    @Test
    fun `creating a routine saves its steps with generated routine id`() = runTest {
        val result = repository.createRoutine(
            HaircareRoutine(
                name = "Minha rotina",
                source = HaircareRoutineSource.CUSTOM,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
            listOf(
                occurrence("2026-11-14", orderIndex = 0),
                occurrence("2026-11-16", orderIndex = 1),
            ),
        )

        val routineId = result.getOrThrow()
        assertEquals(routineId, repository.observeActiveRoutine().first()?.id)
        assertEquals(
            listOf(routineId, routineId),
            repository.observeOccurrencesForRoutine(routineId).first().map { it.routineId },
        )
        assertEquals(
            "2026-11-14",
            repository.observeNextPendingOccurrence(routineId).first()?.scheduledDate,
        )
    }

    @Test
    fun `recording execution saves the actual timestamp and preserves existing notes`() = runTest {
        val occurrenceId = addOccurrence(
            occurrence("2026-11-14", notes = "Produto usado"),
        )

        RecordHaircareExecutionUseCase(repository)(occurrenceId, timestamp = 2_000L).getOrThrow()

        val completed = repository.observeCompletedOccurrences().first().single()
        assertEquals(HaircareOccurrenceStatus.COMPLETED, completed.status)
        assertEquals(2_000L, completed.executedAt)
        assertEquals("Produto usado", completed.notes)
    }

    @Test
    fun `rescheduling updates only the scheduled date and pending status`() = runTest {
        val occurrenceId = addOccurrence(
            occurrence("2026-11-14", notes = "Preservar"),
        )

        RescheduleHaircareStepUseCase(repository)(occurrenceId, "2026-11-20").getOrThrow()

        val updated = dao.getOccurrenceById(occurrenceId)!!.toDomain()
        assertEquals("2026-11-20", updated.scheduledDate)
        assertEquals(HaircareOccurrenceStatus.RESCHEDULED, updated.status)
        assertEquals("Preservar", updated.notes)
        assertEquals(null, updated.executedAt)
    }

    @Test
    fun `rescheduling rejects invalid dates and completed occurrences`() = runTest {
        val pendingId = addOccurrence(occurrence("2026-11-14"))
        val completedId = addOccurrence(
            occurrence(
                "2026-11-12",
                status = HaircareOccurrenceStatus.COMPLETED,
                executedAt = 1_500L,
            ),
        )

        assertTrue(repository.rescheduleStep(pendingId, "2026-02-30").isFailure)
        assertTrue(repository.rescheduleStep(completedId, "2026-11-20").isFailure)
    }

    @Test
    fun `execution rejects nonpositive timestamps and missing occurrences`() = runTest {
        val occurrenceId = addOccurrence(occurrence("2026-11-14"))

        assertTrue(repository.recordExecution(occurrenceId, timestamp = 0L).isFailure)
        assertTrue(repository.recordExecution(Long.MAX_VALUE, timestamp = 1_000L).isFailure)
    }

    private suspend fun addOccurrence(occurrence: HaircareOccurrence): Long {
        val routineId = dao.insertRoutine(
            HaircareRoutineEntity(
                name = "Cronograma",
                source = "CUSTOM",
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )
        return dao.insertOccurrence(occurrence.copy(routineId = routineId).toEntity())
    }

    private fun occurrence(
        scheduledDate: String,
        notes: String? = null,
        status: HaircareOccurrenceStatus = HaircareOccurrenceStatus.PENDING,
        executedAt: Long? = null,
        orderIndex: Int = 0,
    ) = HaircareOccurrence(
        routineId = 0L,
        stepType = HaircareStepType.HIDRATACAO,
        stepTitle = "Hidratação",
        scheduledDate = scheduledDate,
        status = status,
        executedAt = executedAt,
        notes = notes,
        orderIndex = orderIndex,
    )
}

private class FakeHaircareDao : HaircareDao {
    private val routines = mutableMapOf<Long, HaircareRoutineEntity>()
    private val occurrences = mutableMapOf<Long, HaircareOccurrenceEntity>()
    private val routineFlow = MutableStateFlow<List<HaircareRoutineEntity>>(emptyList())
    private val occurrenceFlow = MutableStateFlow<List<HaircareOccurrenceEntity>>(emptyList())
    private var nextRoutineId = 1L
    private var nextOccurrenceId = 1L

    private fun emit() {
        routineFlow.value = routines.values.toList()
        occurrenceFlow.value = occurrences.values.toList()
    }

    override suspend fun insertRoutine(routine: HaircareRoutineEntity): Long {
        val id = if (routine.id == 0L) nextRoutineId++ else routine.id
        routines[id] = routine.copy(id = id)
        emit()
        return id
    }

    override suspend fun updateRoutine(routine: HaircareRoutineEntity) {
        routines[routine.id] = routine
        emit()
    }

    override fun getActiveRoutine(): Flow<HaircareRoutineEntity?> =
        routineFlow.map { list -> list.maxByOrNull { it.id } }

    override suspend fun getRoutineById(routineId: Long): HaircareRoutineEntity? = routines[routineId]

    override suspend fun insertOccurrence(occurrence: HaircareOccurrenceEntity): Long {
        val id = if (occurrence.id == 0L) nextOccurrenceId++ else occurrence.id
        occurrences[id] = occurrence.copy(id = id)
        emit()
        return id
    }

    override suspend fun insertOccurrences(occurrences: List<HaircareOccurrenceEntity>): List<Long> =
        occurrences.map { insertOccurrence(it) }

    override suspend fun updateOccurrence(occurrence: HaircareOccurrenceEntity) {
        occurrences[occurrence.id] = occurrence
        emit()
    }

    override suspend fun getOccurrenceById(occurrenceId: Long): HaircareOccurrenceEntity? =
        occurrences[occurrenceId]

    override fun observeOccurrencesForRoutine(routineId: Long): Flow<List<HaircareOccurrenceEntity>> =
        occurrenceFlow.map { list -> list.filter { it.routineId == routineId } }

    override fun observeNextPendingOccurrence(routineId: Long): Flow<HaircareOccurrenceEntity?> =
        observeOccurrencesForRoutine(routineId).map { list ->
            list.filter { it.status == "PENDING" || it.status == "RESCHEDULED" }
                .minWithOrNull(compareBy<HaircareOccurrenceEntity> { it.scheduledDate }.thenBy { it.orderIndex })
        }

    override fun observeCompletedOccurrences(): Flow<List<HaircareOccurrenceEntity>> =
        occurrenceFlow.map { list ->
            list.filter { it.status == "COMPLETED" }.sortedByDescending { it.executedAt }
        }

    override suspend fun deleteRoutine(routineId: Long) {
        routines.remove(routineId)
        occurrences.entries.removeAll { it.value.routineId == routineId }
        emit()
    }
}

