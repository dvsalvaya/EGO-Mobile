package com.ego.mobile.data.local.dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.local.MIGRATION_1_2
import com.ego.mobile.data.local.MIGRATION_2_3
import com.ego.mobile.data.local.MIGRATION_3_4
import com.ego.mobile.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {
    private lateinit var database: EgoDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun openAndCompletedFlowsSeparateAndOrderTasks() = runBlocking {
        val now = System.currentTimeMillis()
        val noDueId = dao.insert(task(title = "Sem prazo", dueAt = null, createdAt = now))
        val laterId = dao.insert(task(title = "Prazo futuro", dueAt = now + 60_000L, createdAt = now))
        val overdueId = dao.insert(task(title = "Atrasada", dueAt = now - 60_000L, createdAt = now))
        val completedId = dao.insert(
            task(
                title = "Concluída",
                dueAt = now - 120_000L,
                status = "COMPLETED",
                createdAt = now,
                completedAt = now,
            ),
        )

        assertEquals(listOf(overdueId, laterId, noDueId), dao.getOpenTasks().first().map { it.id })
        assertEquals(listOf(completedId), dao.getCompletedTasks().first().map { it.id })
        assertNull(dao.getById(Long.MAX_VALUE))

        assertEquals(1, dao.complete(laterId, now + 10_000L))
        assertEquals("COMPLETED", dao.getById(laterId)?.status)
        assertEquals(1, dao.reschedule(overdueId, now + 120_000L, now + 1_000L))
        assertEquals(now + 120_000L, dao.getById(overdueId)?.dueAt)

        val deleted = dao.deleteById(noDueId)
        assertEquals(noDueId, deleted?.id)
        assertNull(dao.getById(noDueId))
    }

    @Test
    fun migrationFromVersionTwoPreservesHabitDataAndAddsTasks() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "task_migration_test.db"
        context.deleteDatabase(databaseName)
        val legacyDatabase = SQLiteDatabase.openOrCreateDatabase(
            context.getDatabasePath(databaseName),
            null,
        )
        legacyDatabase.execSQL(
            "CREATE TABLE `habits` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `description` TEXT, `frequency` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)",
        )
        legacyDatabase.execSQL(
            "CREATE TABLE `habit_occurrences` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `habitId` INTEGER NOT NULL, " +
                "`dateCivil` TEXT NOT NULL, `status` TEXT NOT NULL, `completedAt` INTEGER, `notes` TEXT, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        legacyDatabase.execSQL(
            "CREATE UNIQUE INDEX `index_habit_occurrences_habitId_dateCivil` " +
                "ON `habit_occurrences` (`habitId`, `dateCivil`)",
        )
        legacyDatabase.execSQL(
            "INSERT INTO habits (id, name, frequency, status, createdAt, updatedAt) " +
                "VALUES (1, 'Hábito antigo', 'DAILY', 'ACTIVE', 1000, 1000)",
        )
        legacyDatabase.version = 2
        legacyDatabase.close()

        val migratedDatabase = Room.databaseBuilder(context, EgoDatabase::class.java, databaseName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
        try {
            assertEquals("Hábito antigo", migratedDatabase.habitDao().getById(1L)?.name)
            val newTaskId = migratedDatabase.taskDao().insert(task("Nova tarefa", dueAt = null, createdAt = 2_000L))
            assertEquals("Nova tarefa", migratedDatabase.taskDao().getById(newTaskId)?.title)
        } finally {
            migratedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun task(
        title: String,
        dueAt: Long?,
        status: String = "OPEN",
        createdAt: Long,
        completedAt: Long? = null,
    ) = TaskEntity(
        title = title,
        description = null,
        dueAt = dueAt,
        status = status,
        createdAt = createdAt,
        updatedAt = createdAt,
        completedAt = completedAt,
    )
}
