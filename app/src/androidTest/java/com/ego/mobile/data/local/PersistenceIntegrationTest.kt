package com.ego.mobile.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ego.mobile.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceIntegrationTest {

    private val dbName = "test_persistence_ego.db"
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun dataPersistsAcrossDatabaseRecreation() = runTest {
        // Passo 1: Abrir primeira instância e salvar dados
        val db1 = Room.databaseBuilder(context, EgoDatabase::class.java, dbName).build()
        val habitDao1 = db1.habitDao()

        val habit = HabitEntity(
            name = "Prática de violão",
            description = "Escalas e repertório",
            frequency = "DAILY",
            status = "ACTIVE",
            createdAt = 1700000000L,
            updatedAt = 1700000000L
        )

        val insertedId = habitDao1.insert(habit)
        val listBeforeClose = habitDao1.getAll().first()
        assertEquals(1, listBeforeClose.size)

        // Passo 2: Fechar conexão
        db1.close()

        // Passo 3: Abrir nova instância sobre o mesmo arquivo de banco (simula nova sessão)
        val db2 = Room.databaseBuilder(context, EgoDatabase::class.java, dbName).build()
        val habitDao2 = db2.habitDao()

        val retrieved = habitDao2.getById(insertedId)
        assertNotNull("Registro deve persistir após reabertura", retrieved)
        assertEquals("Prática de violão", retrieved?.name)
        assertEquals("Escalas e repertório", retrieved?.description)
        assertEquals("ACTIVE", retrieved?.status)

        val listAfterReopen = habitDao2.getAll().first()
        assertEquals(1, listAfterReopen.size)
        assertEquals(insertedId, listAfterReopen[0].id)

        db2.close()
    }
}
