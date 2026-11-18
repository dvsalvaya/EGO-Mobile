package com.ego.mobile.feature.routines.workouts

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.MainActivity
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkoutRegressionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var database: EgoDatabase

    @Before
    fun createInMemoryDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeInMemoryDatabase() {
        database.close()
    }

    @Test
    fun selectedDestinationSurvivesActivityRecreation() {
        composeRule.onNodeWithText("Tarefas").performClick()
        composeRule.onNodeWithText("Nenhuma tarefa aberta").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Nenhuma tarefa aberta").assertIsDisplayed()
    }

    @Test
    fun workoutHistoryRendersSeveralCompletedRecordsInsideWorkoutScreen() {
        runBlocking {
            repeat(3) { index ->
                val sessionId = database.workoutDao().insertSession(
                    WorkoutSessionEntity(
                        title = "Treino de regressão ${index + 1}",
                        startedAt = 1_700_000_000_000L + index * 10_000L,
                        endedAt = 1_700_000_060_000L + index * 10_000L,
                        status = "COMPLETED",
                    ),
                )
                database.workoutDao().insertExercise(
                    WorkoutExerciseEntity(
                        sessionId = sessionId,
                        name = "Exercício ${index + 1}",
                        orderIndex = 0,
                        setsCount = 3,
                    ),
                )
            }
        }
        val viewModel = WorkoutViewModel(WorkoutRepositoryImpl(database.workoutDao()))

        composeRule.activity.runOnUiThread {
            composeRule.activity.setContent {
                MaterialTheme {
                    WorkoutScreen(viewModel = viewModel)
                }
            }
        }

        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        composeRule.onNodeWithText("Histórico de Treinos").assertIsDisplayed()
        repeat(3) { index ->
            composeRule.onNodeWithText("Treino de regressão ${index + 1}").assertIsDisplayed()
            composeRule.onNodeWithText("Exercício ${index + 1}").assertIsDisplayed()
        }
    }
}
