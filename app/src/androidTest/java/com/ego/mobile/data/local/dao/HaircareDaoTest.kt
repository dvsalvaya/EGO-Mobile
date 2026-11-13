package com.ego.mobile.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.local.entity.HaircareOccurrenceEntity
import com.ego.mobile.data.local.entity.HaircareRoutineEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HaircareDaoTest {
    private lateinit var database: EgoDatabase
    private lateinit var dao: HaircareDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.haircareDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun batchInsertAndQueriesReturnNextPendingAndCompletedOccurrences() = runBlocking {
        val routineId = dao.insertRoutine(
            HaircareRoutineEntity(
                name = "Cronograma",
                source = "PRESET",
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )
        val occurrences = listOf(
            occurrence(routineId, "2026-05-10", status = "PENDING", orderIndex = 2),
            occurrence(routineId, "2026-05-05", status = "RESCHEDULED", orderIndex = 1),
            occurrence(
                routineId,
                "2026-05-03",
                status = "COMPLETED",
                executedAt = 100L,
                orderIndex = 0,
            ),
        )

        assertEquals(3, dao.insertOccurrences(occurrences).size)
        assertEquals("Cronograma", dao.getActiveRoutine().first()?.name)
        assertEquals("2026-05-05", dao.observeNextPendingOccurrence(routineId).first()?.scheduledDate)
        assertEquals(listOf(100L), dao.observeCompletedOccurrences().first().map { it.executedAt })
    }

    @Test
    fun completedOccurrencesAreOrderedByRealExecutionTimeAndRoutineDeleteCascades() = runBlocking {
        val routineId = dao.insertRoutine(
            HaircareRoutineEntity(
                name = "Personalizada",
                source = "CUSTOM",
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )
        val olderId = dao.insertOccurrence(
            occurrence(routineId, "2026-05-01", status = "COMPLETED", executedAt = 100L),
        )
        val newerId = dao.insertOccurrence(
            occurrence(routineId, "2026-05-02", status = "COMPLETED", executedAt = 200L),
        )

        assertEquals(listOf(newerId, olderId), dao.observeCompletedOccurrences().first().map { it.id })
        assertNull(dao.observeNextPendingOccurrence(routineId).first())

        dao.deleteRoutine(routineId)

        assertEquals(emptyList<Long>(), dao.observeCompletedOccurrences().first().map { it.id })
    }

    private fun occurrence(
        routineId: Long,
        scheduledDate: String,
        status: String,
        executedAt: Long? = null,
        orderIndex: Int = 0,
    ) = HaircareOccurrenceEntity(
        routineId = routineId,
        stepType = "HIDRATACAO",
        stepTitle = "Hidratação",
        scheduledDate = scheduledDate,
        executedAt = executedAt,
        status = status,
        orderIndex = orderIndex,
    )
}
