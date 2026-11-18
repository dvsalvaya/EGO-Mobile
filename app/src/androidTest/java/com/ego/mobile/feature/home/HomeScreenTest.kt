package com.ego.mobile.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EgoDatabase
    private lateinit var viewModel: HomeViewModel
    private lateinit var taskRepository: TaskRepositoryImpl
    private lateinit var workoutRepository: WorkoutRepositoryImpl
    private var continuedRoutine = false

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val habitRepository = HabitRepositoryImpl(database.habitDao(), habitOccurrenceDao = database.habitOccurrenceDao())
        taskRepository = TaskRepositoryImpl(database.taskDao())
        workoutRepository = WorkoutRepositoryImpl(database.workoutDao())
        viewModel = HomeViewModel(
            getHomeSummaryUseCase = GetHomeSummaryUseCase(
                habitRepository,
                taskRepository,
                workoutRepository,
                HaircareRepositoryImpl(database.haircareDao()),
                today = { LocalDate.of(2026, 11, 13) },
            ),
            habitRepository = habitRepository,
            taskRepository = taskRepository,
            clock = { 1_000L },
            today = { LocalDate.of(2026, 11, 13) },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun showsQuickActionsAndTodaySummarySections() {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(viewModel = viewModel)
            }
        }

        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HomeUiState.Content
        }
        composeRule.onNodeWithText("Novo hábito").assertIsDisplayed()
        composeRule.onNodeWithText("Nova tarefa").assertIsDisplayed()
        composeRule.onNodeWithText("Hábitos de hoje").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhum hábito previsto para hoje.").assertIsDisplayed()
        composeRule.onNodeWithText("Tarefas prioritárias").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa em aberto.").assertIsDisplayed()
    }

    @Test
    fun quickActionsOpenTheirCreationForms() {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HomeUiState.Content
        }

        composeRule.onNodeWithText("Novo hábito").performClick()
        composeRule.onNodeWithText("Frequência").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.onNodeWithText("Nova tarefa").performClick()
        composeRule.onNodeWithText("Título").assertIsDisplayed()
    }

    @Test
    fun createsHabitAndTaskFromTheHomeShortcuts() {
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HomeUiState.Content
        }

        composeRule.onNodeWithText("Novo hábito").performClick()
        composeRule.onNodeWithText("Nome").performTextInput("Meditar")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.todayHabits?.any { it.title == "Meditar" } == true
        }

        val habitId = (viewModel.uiState.value as HomeUiState.Content).summary.todayHabits
            .first { it.title == "Meditar" }.habitId
        composeRule.onNodeWithTag("home_habit_complete_$habitId").performClick()
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.todayHabits?.firstOrNull { it.habitId == habitId }
                ?.status == com.ego.mobile.domain.model.HabitOccurrenceStatus.COMPLETED
        }

        composeRule.onNodeWithText("Nova tarefa").performClick()
        composeRule.onNodeWithText("Título").performTextInput("Revisar proposta")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.priorityTasks?.any { it.title == "Revisar proposta" } == true
        }
    }

    @Test
    fun habitFormKeepsDialogOpenAndShowsInlineErrorsForInvalidInput() {
        composeRule.setContent {
            MaterialTheme { HomeScreen(viewModel = viewModel) }
        }
        composeRule.waitUntil(5_000) { viewModel.uiState.value is HomeUiState.Content }
        composeRule.onNodeWithText("Novo hÃ¡bito").performClick()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Informe um nome.").assertIsDisplayed()
        composeRule.onNodeWithText("FrequÃªncia").assertIsDisplayed()

        composeRule.onNodeWithText("Nome").performTextInput("M".repeat(81))
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Use no maximo 80 caracteres.").assertIsDisplayed()
        composeRule.onNodeWithText("FrequÃªncia").assertIsDisplayed()
    }

    @Test
    fun overdueTaskAndActiveWorkoutAreClearlyActionable() {
        runBlocking {
            taskRepository.createTask(
                com.ego.mobile.domain.model.Task(
                    title = "Enviar proposta",
                    dueAt = 1L,
                    createdAt = 1L,
                    updatedAt = 1L,
                ),
            )
            assertTrue(workoutRepository.startSession("Treino de força", startedAt = 1L).isSuccess)
        }
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onContinueRoutine = { continuedRoutine = true },
                )
            }
        }
        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HomeUiState.Content &&
                (viewModel.uiState.value as HomeUiState.Content).summary.priorityTasks.isNotEmpty()
        }

        composeRule.onNodeWithText("Atrasada").assertIsDisplayed()
        composeRule.onNodeWithText("Enviar proposta").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar treino").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar treino").performClick()
        composeRule.runOnIdle { assertTrue(continuedRoutine) }
    }
}
