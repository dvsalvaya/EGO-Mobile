package com.ego.mobile.feature

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.feature.common.EmptyState
import com.ego.mobile.feature.common.SectionHeader
import com.ego.mobile.feature.habits.HabitsScreen
import com.ego.mobile.feature.habits.HabitsViewModel
import com.ego.mobile.feature.history.HistoryScreen
import com.ego.mobile.feature.history.HistoryViewModel
import com.ego.mobile.feature.routines.haircare.HaircareScreen
import com.ego.mobile.feature.routines.haircare.HaircareUiState
import com.ego.mobile.feature.routines.haircare.HaircareViewModel
import com.ego.mobile.feature.routines.workouts.WorkoutScreen
import com.ego.mobile.feature.routines.workouts.WorkoutViewModel
import com.ego.mobile.feature.tasks.TasksScreen
import com.ego.mobile.feature.tasks.TasksViewModel
import com.ego.mobile.domain.usecase.GetConsolidatedHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EmptyStateScreensTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EgoDatabase
    private val viewModelStore = ViewModelStore()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun habitsScreenShowsEmptyStateAndCreationAction() {
        val repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val viewModel = HabitsViewModel(repository)
        viewModelStore.put("habits", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HabitsScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.onNodeWithText("Nenhum hábito cadastrado").assertIsDisplayed()
        composeRule.onNodeWithText("Crie um hábito para começar a acompanhar sua rotina.").assertIsDisplayed()
        composeRule.onNodeWithText("Adicionar hábito").performClick()
        composeRule.onNodeWithText("Novo hábito").assertIsDisplayed()
    }

    @Test
    fun habitsScreenShowsLoadErrorsInsteadOfAnEmptyState() {
        val repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val failingRepository = object : HabitRepository by repository {
            override fun observeAllHabits(): Flow<List<Habit>> = flow {
                throw IllegalStateException("Falha ao carregar hábitos")
            }
        }
        val viewModel = HabitsViewModel(failingRepository)
        viewModelStore.put("habits-error", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HabitsScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { viewModel.uiState.value.errorMessage != null }
        composeRule.onNodeWithText("Falha ao carregar hábitos").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhum hábito cadastrado").assertDoesNotExist()
    }

    @Test
    fun tasksScreenShowsEmptyStateAndCreationAction() {
        val viewModel = TasksViewModel(TaskRepositoryImpl(database.taskDao()))
        viewModelStore.put("tasks", viewModel)
        composeRule.setContent {
            MaterialTheme {
                TasksScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.tasksUiState.value.isLoading }
        composeRule.onNodeWithText("Nenhuma tarefa aberta").assertIsDisplayed()
        composeRule.onNodeWithText("Crie uma tarefa para organizar seu dia. O prazo é opcional.").assertIsDisplayed()
        composeRule.onNodeWithText("Adicionar tarefa").performClick()
        composeRule.onNodeWithText("Título").assertIsDisplayed()
    }

    @Test
    fun tasksScreenShowsLoadErrorsInsteadOfAnEmptyState() {
        val repository = TaskRepositoryImpl(database.taskDao())
        val failingRepository = object : TaskRepository by repository {
            override fun getOpenTasks(): Flow<List<Task>> = flow {
                throw IllegalStateException("Falha ao carregar tarefas.")
            }
        }
        val viewModel = TasksViewModel(failingRepository)
        viewModelStore.put("tasks-error", viewModel)
        composeRule.setContent {
            MaterialTheme {
                TasksScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { viewModel.tasksUiState.value.errorMessage != null }
        composeRule.onNodeWithText("Falha ao carregar tarefas.").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma tarefa aberta").assertDoesNotExist()
    }

    @Test
    fun workoutScreenDisplaysValidationErrors() {
        val repository = WorkoutRepositoryImpl(database.workoutDao())
        runBlocking { repository.startSession("Teste").getOrThrow() }
        val viewModel = WorkoutViewModel(repository)
        viewModelStore.put("workout", viewModel)
        composeRule.setContent {
            MaterialTheme {
                WorkoutScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.waitUntil(5_000) { viewModel.uiState.value.hasActiveSession }
        composeRule.runOnIdle { viewModel.addExercise(name = "", setsCount = 0) }
        composeRule.waitUntil(5_000) { viewModel.uiState.value.errorMessage != null }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Nome do exercício é obrigatório").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Nome do exercício é obrigatório").assertIsDisplayed()
    }

    @Test
    fun workoutScreenShowsEmptyHistoryAndStartAction() {
        val viewModel = WorkoutViewModel(WorkoutRepositoryImpl(database.workoutDao()))
        viewModelStore.put("workout-empty", viewModel)
        composeRule.setContent {
            MaterialTheme {
                WorkoutScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.onNodeWithText("Nenhum treino concluído ainda. Inicie seu primeiro treino acima!")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar Treino").performClick()
        composeRule.onNodeWithText("Novo Treino").assertIsDisplayed()
    }

    @Test
    fun haircareScreenShowsNoRoutineActions() {
        val viewModel = HaircareViewModel(HaircareRepositoryImpl(database.haircareDao()))
        viewModelStore.put("haircare", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HaircareScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HaircareUiState.NoRoutineConfigured
        }
        composeRule.onNodeWithText("Monte uma rotina flexível e registre cada etapa quando realmente acontecer.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Começar com Cronograma Capilar Básico").assertIsDisplayed()
        composeRule.onNodeWithText("Criar rotina personalizada").performClick()
        composeRule.onNodeWithText("Nova rotina personalizada").assertIsDisplayed()
    }

    @Test
    fun haircareScreenCanShowTheSameOperationErrorAgain() {
        val viewModel = HaircareViewModel(HaircareRepositoryImpl(database.haircareDao()))
        viewModelStore.put("haircare-error", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HaircareScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) {
            viewModel.uiState.value is HaircareUiState.NoRoutineConfigured
        }
        composeRule.runOnIdle { viewModel.recordExecution(occurrenceId = 999L) }
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HaircareUiState.NoRoutineConfigured)?.errorMessage != null
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Haircare occurrence not found.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(7_000) {
            composeRule.onAllNodesWithText("Haircare occurrence not found.").fetchSemanticsNodes().isEmpty()
        }

        composeRule.runOnIdle { viewModel.recordExecution(occurrenceId = 999L) }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Haircare occurrence not found.").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun historyScreenShowsEmptyState() {
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        val viewModel = HistoryViewModel(
            GetConsolidatedHistoryUseCase(
                habitRepository,
                taskRepository,
                WorkoutRepositoryImpl(database.workoutDao()),
                HaircareRepositoryImpl(database.haircareDao()),
            ),
            habitRepository,
        )
        viewModelStore.put("history-empty", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HistoryScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.onNodeWithText("Nenhuma atividade registrada.").assertIsDisplayed()
    }

    @Test
    fun historyScreenShowsLoadErrorsInsteadOfAnEmptyState() {
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val failingHabitRepository = object : HabitRepository by habitRepository {
            override fun observeHabitCalendars(): Flow<List<com.ego.mobile.domain.model.HabitCalendar>> = flow {
                throw IllegalStateException("Falha ao carregar histórico.")
            }
        }
        val viewModel = HistoryViewModel(
            GetConsolidatedHistoryUseCase(
                failingHabitRepository,
                TaskRepositoryImpl(database.taskDao()),
                WorkoutRepositoryImpl(database.workoutDao()),
                HaircareRepositoryImpl(database.haircareDao()),
            ),
            habitRepository,
        )
        viewModelStore.put("history-error", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HistoryScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { viewModel.uiState.value.errorMessage != null }
        composeRule.onNodeWithText("Falha ao carregar histórico.").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhuma atividade registrada.").assertDoesNotExist()
    }

    @Test
    fun emptyStateComponentDisplaysContentActionAndMeetsMinimumTouchTarget() {
        var actionClicked = false
        composeRule.setContent {
            MaterialTheme {
                EmptyState(
                    title = "Nenhum item encontrado",
                    message = "Cadastre um novo item para começar.",
                    actionLabel = "Criar item",
                    onActionClick = { actionClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Nenhum item encontrado").assertIsDisplayed()
        composeRule.onNodeWithText("Cadastre um novo item para começar.").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Ícone de estado vazio").assertIsDisplayed()

        val actionNode = composeRule.onNodeWithText("Criar item")
        actionNode.assertIsDisplayed()
        val minimumTargetPx = with(composeRule.density) { 48.dp.toPx() }
        val bounds = actionNode.fetchSemanticsNode().boundsInRoot
        assertTrue("EmptyState action width must be >= 48dp", bounds.width >= minimumTargetPx)
        assertTrue("EmptyState action height must be >= 48dp", bounds.height >= minimumTargetPx)

        actionNode.performClick()
        composeRule.runOnIdle { assertTrue("EmptyState action callback should be triggered", actionClicked) }
    }

    @Test
    fun sectionHeaderDisplaysHierarchyAndActionWithTouchTarget() {
        var actionClicked = false
        composeRule.setContent {
            MaterialTheme {
                SectionHeader(
                    title = "Ativos",
                    subtitle = "Hábitos em andamento",
                    actionLabel = "Ver tudo",
                    onActionClick = { actionClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Ativos").assertIsDisplayed()
        composeRule.onNodeWithText("Hábitos em andamento").assertIsDisplayed()

        val actionNode = composeRule.onNodeWithText("Ver tudo")
        actionNode.assertIsDisplayed()
        val minimumTargetPx = with(composeRule.density) { 48.dp.toPx() }
        val bounds = actionNode.fetchSemanticsNode().boundsInRoot
        assertTrue("SectionHeader action width must be >= 48dp", bounds.width >= minimumTargetPx)
        assertTrue("SectionHeader action height must be >= 48dp", bounds.height >= minimumTargetPx)

        actionNode.performClick()
        composeRule.runOnIdle { assertTrue("SectionHeader action callback should be triggered", actionClicked) }
    }

    @Test
    fun historyScreenEmptyStateHasNoDirectCreationAction() {
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        val viewModel = HistoryViewModel(
            GetConsolidatedHistoryUseCase(
                habitRepository,
                taskRepository,
                WorkoutRepositoryImpl(database.workoutDao()),
                HaircareRepositoryImpl(database.haircareDao()),
            ),
        )
        viewModelStore.put("history-empty-action-check", viewModel)
        composeRule.setContent {
            MaterialTheme {
                HistoryScreen(viewModel)
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.onNodeWithText("Nenhuma atividade registrada.").assertIsDisplayed()
        composeRule.onAllNodesWithText("Adicionar").assertCountEquals(0)
        composeRule.onAllNodesWithText("Criar").assertCountEquals(0)
        composeRule.onAllNodesWithText("Novo").assertCountEquals(0)
    }
}
