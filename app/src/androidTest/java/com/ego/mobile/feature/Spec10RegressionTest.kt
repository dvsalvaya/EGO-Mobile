package com.ego.mobile.feature

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus
import com.ego.mobile.feature.habits.HabitFormDialog
import com.ego.mobile.feature.routines.workouts.components.WorkoutHistoryView
import org.junit.Rule
import org.junit.Test

class Spec10RegressionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun workoutHistoryRendersEmptyState() {
        composeRule.setContent {
            MaterialTheme { WorkoutHistoryView(completedSessions = emptyList()) }
        }
        composeRule.onNodeWithText("Histórico de Treinos").assertIsDisplayed()
        composeRule.onNodeWithText("Nenhum treino concluído ainda. Inicie seu primeiro treino acima!").assertIsDisplayed()
    }

    @Test
    fun workoutHistoryRendersOneSession() {
        val sessions = listOf(session(1L))
        composeRule.setContent {
            MaterialTheme { WorkoutHistoryView(completedSessions = sessions) }
        }
        composeRule.onNodeWithText("Histórico de Treinos").assertIsDisplayed()
        composeRule.onNodeWithText("Treino 1").assertIsDisplayed()
    }

    @Test
    fun workoutHistoryRendersManySessionsWithoutNestedScrollCrash() {
        val sessions = listOf(session(1L), session(2L), session(3L))
        composeRule.setContent {
            MaterialTheme { WorkoutHistoryView(completedSessions = sessions) }
        }
        sessions.forEach { composeRule.onNodeWithText(it.title).assertIsDisplayed() }
    }

    @Test
    fun habitFormShowsValidationErrorAndSelectedFrequency() {
        composeRule.setContent {
            MaterialTheme { HabitFormDialog(onDismiss = {}, onSave = { _, _, _ -> Unit }) }
        }

        composeRule.onNodeWithText("Diária").assertIsSelected()
        composeRule.onNodeWithText("3x/sem").performClick()
        composeRule.onNodeWithText("3x/sem").assertIsSelected()

        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Informe o nome do hábito.").assertIsDisplayed()

        composeRule.onNodeWithText("Nome").performTextInput("Leitura")
        composeRule.onNodeWithText("Salvar").performClick()
        composeRule.onNodeWithText("Informe o nome do hábito.").assertIsNotDisplayed()
    }

    private fun session(id: Long) = WorkoutSession(
        id = id,
        title = "Treino $id",
        startedAt = 1_000L,
        endedAt = 5_000L,
        status = WorkoutSessionStatus.COMPLETED,
        exercises = listOf(WorkoutExercise(name = "Supino", setsCount = 3)),
    )
}
