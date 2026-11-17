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
class WorkoutTemplateMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EgoDatabase::class.java,
    )

    @Test
    fun migratesFromVersionFivePreservingSessionsAndExercises() {
        val database = helper.createDatabase(DATABASE_NAME, 5)
        insertLegacyData(database)
        database.close()

        val migrated = helper.runMigrationsAndValidate(DATABASE_NAME, 6, false, MIGRATION_5_6)

        migrated.query("SELECT title, status FROM workout_sessions WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Treino antigo", cursor.getString(0))
            assertEquals("COMPLETED", cursor.getString(1))
            assertEquals(false, cursor.moveToNext())
        }
        migrated.query("SELECT name, weightKg FROM workout_exercises WHERE sessionId = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Supino", cursor.getString(0))
            assertEquals(80.0, cursor.getDouble(1), 0.01)
            assertEquals(false, cursor.moveToNext())
        }

        migrated.execSQL(
            "INSERT INTO training_models (name, createdAt, updatedAt) VALUES ('Treino A', 1, 1)",
        )
        val modelId = migrated.query("SELECT id FROM training_models WHERE name = 'Treino A'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }
        migrated.execSQL(
            "INSERT INTO training_models_exercises " +
                "(modelId, name, orderIndex, setsCount, reps, weightKg, notes) " +
                "VALUES ($modelId, 'Agachamento', 0, 4, '8', 100.0, NULL)",
        )
        migrated.query(
            "SELECT name, orderIndex, setsCount FROM training_models_exercises " +
                "WHERE modelId = $modelId ORDER BY orderIndex",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Agachamento", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals(4, cursor.getInt(2))
        }
        migrated.close()
    }

    private fun insertLegacyData(database: SupportSQLiteDatabase) {
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
        const val DATABASE_NAME = "workout_template_migration_test.db"
    }
}
