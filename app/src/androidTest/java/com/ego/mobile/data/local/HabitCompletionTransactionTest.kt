package com.ego.mobile.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.data.local.entity.HabitEntity
import com.ego.mobile.data.local.entity.HabitOccurrenceEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitCompletionTransactionTest {
    private lateinit var database: EgoDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            EgoDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `concurrent completions cannot exceed weekly target`() = runTest {
        val dao = database.habitDao()
        val habitId = dao.insert(
            HabitEntity(
                name = "Treino",
                description = null,
                frequency = "TIMES_PER_WEEK:1",
                status = "ACTIVE",
                createdAt = 1L,
                updatedAt = 1L,
            ),
        )
        val candidates = listOf("2026-11-18", "2026-11-19").map { date ->
            async {
                dao.completeOccurrenceIfEligible(
                    habitId = habitId,
                    dateCivil = date,
                    weekStart = "2026-11-16",
                    weekEnd = "2026-11-22",
                    expectedFrequency = "TIMES_PER_WEEK:1",
                    weeklyTarget = 1,
                    occurrence = HabitOccurrenceEntity(
                        habitId = habitId,
                        dateCivil = date,
                        status = "COMPLETED",
                        completedAt = 2L,
                        notes = null,
                        createdAt = 2L,
                        updatedAt = 2L,
                    ),
                )
            }
        }.awaitAll()

        assertEquals(1, candidates.count { it })
        assertEquals(1, dao.countCompletedInWeek(habitId, "2026-11-16", "2026-11-22"))
        assertTrue(candidates.any { !it })
    }
}
