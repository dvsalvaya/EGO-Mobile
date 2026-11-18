package com.ego.mobile.feature.habits

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
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
        composeRule.onNodeWithText("Nome").assertIsDisplayed()
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

    @Test
    fun weeklyGoalFormSavesPreferredDaysAccessibly() {
        val submitted = java.util.concurrent.atomic.AtomicReference<Pair<String, HabitFrequency>?>(null)
        composeRule.setContent {
            MaterialTheme {
                HabitFormDialog(onDismiss = {}, onSave = { name, _, frequency -> submitted.set(name to frequency) })
            }
        }
        composeRule.onNodeWithText("Nome").performTextReplacement("Alongamento")
        composeRule.onNodeWithText("Vezes por semana").performClick()
        composeRule.waitUntil(3_000) { composeRule.onAllNodes(hasText("Meta semanal: 3 vezes")).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithContentDescription("terça-feira, não selecionado").performScrollTo().performClick()
        composeRule.onNodeWithContentDescription("quinta-feira, não selecionado").performScrollTo().performClick()
        composeRule.onNodeWithText("Alongamento").assertIsDisplayed()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Informe um nome.").assertDoesNotExist()
        assertEquals(
            "Alongamento" to HabitFrequency.TimesPerWeek(3, setOf(java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.THURSDAY)),
            submitted.get(),
        )
    }

    @Test
    fun changingPlanConfirmsHistoryPreservationAndKeepsTheOccurrence() {
        val habitId = runBlocking {
            val id = repository.saveHabit(Habit(name = "Leitura", frequency = HabitFrequency.Daily, createdAt = 1L, updatedAt = 1L))
            repository.completeOccurrence(id, today.toString(), null)
            id
        }
        showHabits()
        composeRule.onNodeWithText("Frequência").performScrollTo().performClick()
        composeRule.onNodeWithText("Vezes por semana").performClick()
        composeRule.onNodeWithContentDescription("terça-feira, não selecionado").performScrollTo().performClick()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("O novo plano vale a partir de hoje. O histórico e as ocorrências anteriores serão preservados.").assertIsDisplayed()
        composeRule.onNodeWithText("Aplicar a partir de hoje").performClick()

        composeRule.waitUntil(5_000) {
            runBlocking { repository.getHabitById(habitId)?.frequency == HabitFrequency.TimesPerWeek(3, setOf(java.time.DayOfWeek.TUESDAY)) }
        }
        val occurrence = runBlocking { repository.getOccurrenceForDate(habitId, today.toString()) }
        assertEquals(HabitOccurrenceStatus.COMPLETED, occurrence?.status)
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
