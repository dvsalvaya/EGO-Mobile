package com.ego.mobile.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import com.ego.mobile.domain.usecase.GetHabitDayGridUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
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
    private lateinit var habitRepository: HabitRepositoryImpl
    private lateinit var taskRepository: TaskRepositoryImpl
    private lateinit var workoutRepository: WorkoutRepositoryImpl
    private var continuedRoutine = false

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitRepository = HabitRepositoryImpl(database.habitDao(), habitOccurrenceDao = database.habitOccurrenceDao())
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
            getHabitDayGridUseCase = GetHabitDayGridUseCase(habitRepository, today = { LocalDate.of(2026, 11, 13) }),
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
        composeRule.onNodeWithContentDescription("Criar").assertIsDisplayed()
        composeRule.onNodeWithTag("home_list").performScrollToNode(hasText("Consistência dos hábitos"))
        composeRule.onNodeWithText("Consistência dos hábitos").assertIsDisplayed()
        composeRule.onAllNodesWithText("Ver tudo").assertCountEquals(2)
        composeRule.onAllNodesWithText("Tudo em dia").assertCountEquals(2)
    }

    @Test
    fun emptyTodayCardsStayCompactAndWeekGridRemainsScrollable() {
        composeRule.setContent { MaterialTheme { HomeScreen(viewModel = viewModel) } }
        composeRule.waitUntil(5_000) { viewModel.uiState.value is HomeUiState.Content }

        val maxCardHeightDp = if (composeRule.density.fontScale >= 1.25f) 310.dp else 240.dp
        val maxCardHeightPx = with(composeRule.density) { maxCardHeightDp.toPx() }
        listOf("home_today_habits_card", "home_today_tasks_card").forEach { tag ->
            val cardHeight = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.height
            assertTrue("$tag should stay below $maxCardHeightDp, was ${cardHeight / composeRule.density.density}dp", cardHeight <= maxCardHeightPx)
        }
        composeRule.onAllNodesWithText("Tudo em dia").assertCountEquals(2)
        composeRule.onNodeWithText("Quando houver hábitos previstos para hoje, eles aparecerão aqui.").performScrollTo()
        composeRule.onNodeWithText("Quando houver hábitos previstos para hoje, eles aparecerão aqui.").assertIsDisplayed()
        composeRule.onNodeWithText("Tarefas sem prazo e futuras ficam disponíveis na aba Tarefas.").performScrollTo()
        composeRule.onNodeWithText("Tarefas sem prazo e futuras ficam disponíveis na aba Tarefas.").assertIsDisplayed()
        composeRule.onNodeWithTag("home_list").performScrollToNode(hasText("Consistência dos hábitos"))
        composeRule.onAllNodes(hasContentDescription("hábitos concluídos", substring = true), useUnmergedTree = true)
            .assertCountEquals(7)
    }

    @Test
    fun habitGridExposesSevenDatedAccessibleDaysAndCreateHasLargeTouchTarget() {
        composeRule.setContent { MaterialTheme { HomeScreen(viewModel = viewModel) } }
        composeRule.waitUntil(5_000) { viewModel.uiState.value is HomeUiState.Content }
        composeRule.onNodeWithTag("home_list").performScrollToNode(hasText("Consistência dos hábitos"))

        composeRule.onAllNodes(hasContentDescription("hábitos concluídos", substring = true), useUnmergedTree = true)
            .assertCountEquals(7)
        val createButton = composeRule.onNodeWithContentDescription("Criar")
        val minimumPx = with(composeRule.density) { 48.dp.toPx() }
        val bounds = createButton.fetchSemanticsNode().boundsInRoot
        assertTrue("Create target height should be at least 48dp", bounds.height >= minimumPx)
        assertTrue("Create target width should be at least 48dp", bounds.width >= minimumPx)
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

        openCreate("Novo hábito")
        composeRule.onNodeWithText("Frequência").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelar").performClick()
        openCreate("Nova tarefa")
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

        openCreate("Novo hábito")
        composeRule.onNodeWithText("Nome").performTextInput("Meditar")
        composeRule.onNodeWithText("Salvar").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { habitRepository.observeAllHabits().first().any { it.name == "Meditar" } }
        }

        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.todayHabits?.any { it.title == "Meditar" } == true
        }

        val habitId = (viewModel.uiState.value as HomeUiState.Content).summary.todayHabits
            .first { it.title == "Meditar" }.habitId
        composeRule.onNodeWithTag("home_habit_complete_$habitId").performClick()
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.todayHabits?.none { it.habitId == habitId } == true
        }
        composeRule.onNodeWithText("Hábito concluído").assertIsDisplayed()
        composeRule.onNodeWithText("Desfazer").performClick()
        composeRule.waitUntil(5_000) {
            (viewModel.uiState.value as? HomeUiState.Content)
                ?.summary?.todayHabits?.any { it.habitId == habitId } == true
        }

        openCreate("Nova tarefa")
        composeRule.onNodeWithText("Título").performTextInput("Revisar proposta")
        composeRule.onNodeWithText("Salvar").performScrollTo().performClick()
        composeRule.waitUntil(5_000) {
            runBlocking { taskRepository.getOpenTasks().first().any { it.title == "Revisar proposta" } }
        }
    }

    @Test
    fun habitFormKeepsDialogOpenAndShowsInlineErrorsForInvalidInput() {
        composeRule.setContent {
            MaterialTheme { HomeScreen(viewModel = viewModel) }
        }
        composeRule.waitUntil(5_000) { viewModel.uiState.value is HomeUiState.Content }
        openCreate("Novo hábito")
        composeRule.onNodeWithText("Salvar").performScrollTo().performClick()
        composeRule.onNodeWithText("Informe um nome.").assertIsDisplayed()
        composeRule.onNodeWithText("Frequência").assertIsDisplayed()

        composeRule.onNodeWithText("Nome").performTextInput("M".repeat(81))
        composeRule.onNodeWithText("Salvar").performScrollTo().performClick()
        composeRule.onNodeWithText("Use no máximo 80 caracteres.").performScrollTo()
        composeRule.onNodeWithText("Use no máximo 80 caracteres.").assertIsDisplayed()
        composeRule.onNodeWithText("Frequência").assertIsDisplayed()
    }

    private fun openCreate(label: String) {
        composeRule.onNodeWithContentDescription("Criar").performClick()
        composeRule.onNodeWithText(label).performClick()
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

        composeRule.onNodeWithTag("home_list").performScrollToNode(hasText("Atrasada"))
        composeRule.onNodeWithText("Atrasada").assertIsDisplayed()
        composeRule.onNodeWithText("Enviar proposta").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar treino").assertIsDisplayed()
        composeRule.onNodeWithText("Continuar treino").performClick()
        composeRule.runOnIdle { assertTrue(continuedRoutine) }
    }
}
