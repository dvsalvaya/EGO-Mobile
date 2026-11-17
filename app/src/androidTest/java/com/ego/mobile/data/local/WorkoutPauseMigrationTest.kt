package com.ego.mobile.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutPauseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EgoDatabase::class.java,
    )

    @Test
    fun migrate5to6PreservesExistingDataAndAddsPauseColumns() {
        val database = helper.createDatabase(DATABASE_NAME, 5)
        insertLegacyData(database)
        database.close()

        val migrated = helper.runMigrationsAndValidate(DATABASE_NAME, 6, false, MIGRATION_5_6)

        migrated.query("SELECT title, status, endedAt FROM workout_sessions WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Treino antigo", cursor.getString(0))
            assertEquals("COMPLETED", cursor.getString(1))
            assertEquals(5_000L, cursor.getLong(2))
        }
        migrated.query("SELECT pausedAt, pausedDurationMillis FROM workout_sessions WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
            assertEquals(0L, cursor.getLong(1))
        }
        migrated.query("SELECT name FROM workout_exercises WHERE sessionId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Supino", cursor.getString(0))
        }
        migrated.query("SELECT name FROM habits WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Leitura", cursor.getString(0))
        }
        migrated.close()
    }

    private fun insertLegacyData(database: SupportSQLiteDatabase) {
        database.execSQL(
            "INSERT INTO habits (id, name, frequency, status, createdAt, updatedAt) " +
                "VALUES (1, 'Leitura', 'DAILY', 'ACTIVE', 100, 100)",
        )
        database.execSQL(
            "INSERT INTO workout_sessions (id, title, startedAt, endedAt, status, notes) " +
                "VALUES (1, 'Treino antigo', 1000, 5000, 'COMPLETED', NULL)",
        )
        database.execSQL(
            "INSERT INTO workout_exercises " +
                "(id, sessionId, name, orderIndex, setsCount, reps, weightKg, notes) " +
                "VALUES (1, 1, 'Supino', 0, 3, '10', 80.0, NULL)",
        )
    }

    private companion object {
        const val DATABASE_NAME = "workout_pause_migration_test.db"
    }
}
