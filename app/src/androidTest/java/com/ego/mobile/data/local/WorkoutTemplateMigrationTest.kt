package com.ego.mobile.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ego.mobile.data.local.mapper.toDomain
import com.ego.mobile.data.local.mapper.toEntity
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutTemplateMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @After
    fun cleanUp() {
        EgoDatabaseProvider.resetForTesting()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migratesFromVersionFivePreservingSessionsAndExercises() {
        createLegacyDatabase(5) { database ->
            insertLegacyData(database)
        }

        val migrated = openWithRoom()

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
    }

    @Test
    fun migratesVersionSixAndPreservesLegacySessionDurationData() {
        createLegacyDatabase(6) { database ->
            database.execSQL(
                "INSERT INTO workout_sessions (id, title, startedAt, endedAt, status, notes) " +
                    "VALUES (8, 'Treino legado', 1000, 9000, 'COMPLETED', 'nota')",
            )
        }

        val migrated = openWithRoom()
        migrated.query(
            "SELECT title, startedAt, endedAt, status, notes, pausedAt, pausedDurationMillis " +
                "FROM workout_sessions WHERE id = 8",
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Treino legado", cursor.getString(0))
            assertEquals(1000L, cursor.getLong(1))
            assertEquals(9000L, cursor.getLong(2))
            assertEquals("COMPLETED", cursor.getString(3))
            assertEquals("nota", cursor.getString(4))
            assertTrue(cursor.isNull(5))
            assertEquals(0L, cursor.getLong(6))
        }
    }

    @Test
    fun pausedSessionKeepsItsDurationAfterDatabaseReopens() = runBlocking {
        val dao = EgoDatabaseProvider.getDatabase(context, DATABASE_NAME).workoutDao()
        val paused = WorkoutSession(title = "Treino pausado", startedAt = 1_000L)
            .transitionTo(WorkoutSessionStatus.PAUSED, 3_000L)
        val sessionId = dao.insertSession(paused.toEntity())

        EgoDatabaseProvider.resetForTesting()
        val reopened = EgoDatabaseProvider.getDatabase(context, DATABASE_NAME).workoutDao()
        val session = checkNotNull(reopened.getSessionById(sessionId)).toDomain()
        assertEquals(WorkoutSessionStatus.PAUSED, session.status)
        assertEquals(2_000L, session.durationMillis(now = 7_000L))

        val completed = session.transitionTo(WorkoutSessionStatus.IN_PROGRESS, 7_000L)
            .transitionTo(WorkoutSessionStatus.COMPLETED, 9_000L)
        reopened.updateSession(completed.toEntity())
        val saved = checkNotNull(reopened.getSessionById(sessionId)).toDomain()
        assertEquals(4_000L, saved.durationMillis())
    }

    @Test
    fun migratesSevenWithoutInventingPastPlansOrChangingOccurrences() {
        createLegacyDatabase(7) { db ->
            db.execSQL(
                "INSERT INTO habits VALUES (10, 'Ler', NULL, 'TIMES_PER_WEEK:3', 'ACTIVE', 1, 2)",
            )
            db.execSQL(
                "INSERT INTO habit_occurrences VALUES (42, 10, '2020-01-03', 'COMPLETED', 123, 'livro', 7, 8)",
            )
            db.execSQL(
                "INSERT INTO workout_sessions (id,title,startedAt,status,pausedAt,pausedDurationMillis) " +
                    "VALUES (9,'Pausado',1000,'PAUSED',1300,50)",
            )
        }
        val migrated = openWithRoom()
        migrated.query("SELECT effectiveFrom, frequency FROM habit_plans WHERE habitId = 10").use {
            assertTrue(it.moveToFirst())
            assertEquals(java.time.LocalDate.now(com.ego.mobile.domain.model.habitZone).toString(), it.getString(0))
            assertEquals("TIMES_PER_WEEK:3", it.getString(1))
            assertEquals(false, it.moveToNext())
        }
        migrated.query("SELECT id,dateCivil,status,completedAt,notes,createdAt,updatedAt FROM habit_occurrences").use {
            assertTrue(it.moveToFirst())
            assertEquals(listOf("42","2020-01-03","COMPLETED","123","livro","7","8"), (0..6).map(it::getString))
            assertEquals(false, it.moveToNext())
        }
        migrated.query("SELECT pausedAt, pausedDurationMillis FROM workout_sessions WHERE id = 9").use {
            assertTrue(it.moveToFirst())
            assertEquals(1300L, it.getLong(0))
            assertEquals(50L, it.getLong(1))
        }
    }

    private fun createLegacyDatabase(version: Int, seed: (SQLiteDatabase) -> Unit) {
        EgoDatabaseProvider.resetForTesting()
        context.deleteDatabase(DATABASE_NAME)
        val schemaPath = "${EgoDatabase::class.java.canonicalName}/$version.json"
        val schema = instrumentation.context.assets.open(schemaPath).bufferedReader().use {
            JSONObject(it.readText()).getJSONObject("database")
        }
        context.openOrCreateDatabase(DATABASE_NAME, 0, null).use { database ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val tableName = entity.getString("tableName")
                database.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", tableName))
                val indices = entity.optJSONArray("indices") ?: JSONArray()
                for (j in 0 until indices.length()) {
                    val sql = indices.getJSONObject(j).getString("createSql")
                    database.execSQL(sql.replace("\${TABLE_NAME}", tableName))
                }
            }
            val queries = schema.getJSONArray("setupQueries")
            for (i in 0 until queries.length()) {
                database.execSQL(queries.getString(i))
            }
            seed(database)
            database.version = version
        }
    }

    private fun openWithRoom(): SupportSQLiteDatabase =
        EgoDatabaseProvider.getDatabase(context, DATABASE_NAME).openHelper.writableDatabase

    private fun insertLegacyData(database: SQLiteDatabase) {
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
