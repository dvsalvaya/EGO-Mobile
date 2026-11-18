package com.ego.mobile.feature.habits

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HabitStabilizationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val store = ViewModelStore()
    private lateinit var database: EgoDatabase
    private lateinit var repository: HabitRepositoryImpl
    private val today = LocalDate.of(2026, 11, 18)

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            EgoDatabase::class.java,
        ).build()
        repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
    }

    @After
    fun tearDown() {
        store.clear()
        database.close()
    }

    @Test
    fun invalidHabitFormShowsFeedbackAndKeepsTheFormOpen() {
        showHabits()
        composeRule.onNodeWithText("Adicionar hábito").performClick()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Informe um nome.").assertIsDisplayed()

        composeRule.onNodeWithText("Nome").performTextReplacement("A".repeat(81))
        composeRule.onNodeWithText("Descrição (opcional)").performTextReplacement("D".repeat(251))
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Novo hábito").assertIsDisplayed()

        composeRule.onNodeWithText("Nome").performTextReplacement("Leitura")
        composeRule.onNodeWithText("Descrição (opcional)").performTextReplacement("Diária")
        composeRule.onNodeWithText("Salvar").performClick()
        awaitText("Leitura")
    }

    @Test
    fun habitCanBeCompletedUndonePausedResumedAndEdited() {
        val habitId = runBlocking {
            repository.saveHabit(
                Habit(
                    name = "Leitura",
                    frequency = HabitFrequency.Daily,
                    createdAt = 1_000L,
                    updatedAt = 1_000L,
                ),
            )
        }
        showHabits()

        composeRule.onNodeWithText("Concluir").performClick()
        composeRule.waitUntil(5_000) {
            runBlocking {
                repository.getOccurrenceForDate(habitId, today.toString())?.status == HabitOccurrenceStatus.COMPLETED
            }
        }
        awaitText("Desfazer")
        composeRule.onNodeWithText("Desfazer").performClick()
        awaitText("Concluir")

        composeRule.onNodeWithText("Pausar").performClick()
        awaitText("Retomar")
        composeRule.onNodeWithText("Retomar").performClick()
        awaitText("Concluir")

        composeRule.onNodeWithText("Editar").performScrollTo().performClick()
        composeRule.onNodeWithText("Nome").performTextReplacement("Leitura diária")
        composeRule.onNodeWithText("Salvar").performClick()
        awaitText("Leitura diária")
    }

    private fun awaitText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }

    private fun showHabits() {
        val viewModel = HabitsViewModel(repository, today = { today })
        store.put("habits", viewModel)
        composeRule.setContent {
            MaterialTheme { HabitsScreen(viewModel) }
        }
        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
    }
}
