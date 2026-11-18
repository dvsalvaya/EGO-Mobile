package com.ego.mobile.feature.history

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.HaircareRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.usecase.GetConsolidatedHistoryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class HistoryScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: EgoDatabase
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        viewModel = HistoryViewModel(
            GetConsolidatedHistoryUseCase(
                habitRepository,
                taskRepository,
                WorkoutRepositoryImpl(database.workoutDao()),
                HaircareRepositoryImpl(database.haircareDao()),
            ),
            habitRepository,
        )
        runBlocking {
            val now = System.currentTimeMillis()
            val habitId = habitRepository.saveHabit(
                Habit(
                    name = "Meditar",
                    frequency = HabitFrequency.Daily,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            habitRepository.completeOccurrence(
                habitId = habitId,
                dateCivil = LocalDate.now(ZoneId.of("America/Sao_Paulo")).toString(),
            ).getOrThrow()

            val taskId = taskRepository.createTask(
                Task(
                    title = "Revisar proposta",
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            taskRepository.completeTask(taskId, now + 1_000L).getOrThrow()
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun correctsTheDisplayedDayInBothDirections() {
        composeRule.setContent { MaterialTheme { HistoryScreen(viewModel) } }
        composeRule.waitUntil(5_000) { viewModel.uiState.value.items.any { it.title == "Meditar" } }
        composeRule.onNodeWithText("Hábitos").performClick()
        composeRule.onNodeWithText("Ver registro").performClick()
        composeRule.onNodeWithText("Não fiz").performClick()
        composeRule.waitUntil(5_000) { viewModel.uiState.value.visibleItems.singleOrNull()?.status == "Não fiz" }
        composeRule.onNodeWithText("Fiz nesse dia").performClick()
        composeRule.waitUntil(5_000) { viewModel.uiState.value.visibleItems.singleOrNull()?.status == "COMPLETED" }
        composeRule.onNodeWithText("Fechar").performClick()
        composeRule.onNodeWithText("Concluído").assertIsDisplayed()
    }

    @Test
    fun showsTimelineAndFiltersByActivityType() {
        composeRule.setContent {
            MaterialTheme {
                HistoryScreen(viewModel = viewModel)
            }
        }
        composeRule.waitUntil(5_000) {
            !viewModel.uiState.value.isLoading
        }

        composeRule.onNodeWithText("Histórico").assertIsDisplayed()
        composeRule.onNodeWithText("Revisar proposta").assertIsDisplayed()
        composeRule.onNodeWithText("Meditar").assertIsDisplayed()

        composeRule.onNodeWithText("Hábitos").performClick()
        composeRule.onNodeWithText("Meditar").assertIsDisplayed()
        composeRule.onAllNodesWithText("Revisar proposta").assertCountEquals(0)

        composeRule.onNodeWithText("Tarefas").performClick()
        composeRule.onNodeWithText("Revisar proposta").assertIsDisplayed()
        composeRule.onAllNodesWithText("Meditar").assertCountEquals(0)

        composeRule.onNodeWithText("Todas").performClick()
        composeRule.onNodeWithText("Meditar").assertIsDisplayed()
        composeRule.onNodeWithText("Revisar proposta").assertIsDisplayed()
    }
}
