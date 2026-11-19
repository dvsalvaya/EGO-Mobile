package com.ego.mobile.feature.tasks

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.domain.model.Task
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TasksGroupingDetailsTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EgoDatabase
    private lateinit var viewModel: TasksViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries().build()
        val zone = ZoneId.of("America/Sao_Paulo")
        val now = LocalDate.of(2026, 11, 18).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val repository = TaskRepositoryImpl(database.taskDao())
        runBlocking {
            repository.createTask(task("Atrasada", LocalDate.of(2026, 11, 17).atStartOfDay(zone).toInstant().toEpochMilli()))
            repository.createTask(task("Hoje", LocalDate.of(2026, 11, 18).atStartOfDay(zone).toInstant().toEpochMilli()))
            repository.createTask(task("Próxima", LocalDate.of(2026, 11, 19).atStartOfDay(zone).toInstant().toEpochMilli()))
            repository.createTask(task("Sem prazo", null))
        }
        viewModel = TasksViewModel(repository, clock = { now })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun rendersEachDeadlineGroupAndUndatedTasksAtEndOfUpcoming() {
        composeRule.setContent { MaterialTheme { TasksScreen(viewModel) } }

        composeRule.waitUntil(5_000) { !viewModel.tasksUiState.value.isLoading }
        composeRule.onNodeWithText("Atrasadas").assertIsDisplayed()
        composeRule.onAllNodesWithText("Hoje").assertCountEquals(2)
        composeRule.onNodeWithText("Próximas").assertIsDisplayed()
        composeRule.onNodeWithText("Atrasada").assertIsDisplayed()
        composeRule.onNodeWithText("Próxima").assertIsDisplayed()
        composeRule.onNodeWithText("Sem prazo").assertIsDisplayed()
    }

    @Test
    fun tappingTaskOpensDetailsWithDescriptionAndPrimaryCompletionAction() {
        runBlocking { TaskRepositoryImpl(database.taskDao()).createTask(task("Detalhar", null, "Descrição completa")) }
        composeRule.setContent { MaterialTheme { TasksScreen(viewModel) } }
        composeRule.waitUntil(5_000) { !viewModel.tasksUiState.value.isLoading }

        composeRule.onNodeWithContentDescription("Detalhes da tarefa: Detalhar").performClick()
        composeRule.onNodeWithText("Descrição completa").assertIsDisplayed()
        composeRule.onAllNodesWithText("Sem prazo").assertCountEquals(2)
        composeRule.onNodeWithText("Concluir").assertIsDisplayed()
        composeRule.onNodeWithText("Editar").assertIsDisplayed()
        composeRule.onNodeWithText("Reagendar").assertIsDisplayed()
        composeRule.onNodeWithText("Excluir").assertIsDisplayed()
    }

    private fun task(title: String, dueAt: Long?, description: String? = null) = Task(
        title = title, dueAt = dueAt, description = description,
        createdAt = 1_000L, updatedAt = 1_000L,
    )
}
