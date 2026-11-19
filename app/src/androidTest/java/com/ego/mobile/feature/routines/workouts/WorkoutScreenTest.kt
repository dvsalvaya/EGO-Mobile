package com.ego.mobile.feature.routines.workouts

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.room.Room
import com.ego.mobile.MainActivity
import com.ego.mobile.data.local.EgoDatabase
import com.ego.mobile.data.local.entity.WorkoutExerciseEntity
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import com.ego.mobile.data.repository.WorkoutRepositoryImpl
import com.ego.mobile.data.repository.WorkoutTemplateRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WorkoutScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private lateinit var database: EgoDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, EgoDatabase::class.java)
            .allowMainThreadQueries().build()
    }

    @After fun tearDown() = database.close()

    @Test fun templateAndLoadEvolutionAreShownAlongsideHistory() {
        runBlocking {
            val dao = database.trainingModelDao()
            val templateRepository = WorkoutTemplateRepositoryImpl(
                dao, database.trainingModelExerciseDao(), WorkoutRepositoryImpl(database.workoutDao()),
            )
            templateRepository.saveTemplate(com.ego.mobile.domain.model.WorkoutTemplate(
                name = "Força A",
                exercises = listOf(com.ego.mobile.domain.model.WorkoutTemplateExercise(
                    name = "Supino", orderIndex = 0, setsCount = 3, reps = "8-10", weightKg = 40.0,
                )),
            )).getOrThrow()
            val id = database.workoutDao().insertSession(WorkoutSessionEntity(
                title = "Treino concluído", startedAt = 1_700_000_000_000L,
                endedAt = 1_700_000_600_000L, status = "COMPLETED",
            ))
            database.workoutDao().insertExercise(WorkoutExerciseEntity(
                sessionId = id, name = "Supino", orderIndex = 0, setsCount = 3, weightKg = 50.0,
            ))
        }
        val workoutRepository = WorkoutRepositoryImpl(database.workoutDao())
        val viewModel = WorkoutViewModel(workoutRepository, templateRepository = WorkoutTemplateRepositoryImpl(
            database.trainingModelDao(), database.trainingModelExerciseDao(), workoutRepository,
        ))
        composeRule.activity.runOnUiThread {
            composeRule.activity.setContent { MaterialTheme { WorkoutScreen(viewModel) } }
        }
        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        listOf("Modelos de treino", "Força A", "Estatísticas", "1 treino concluído", "Evolução de carga", "50 kg", "Histórico de Treinos").forEach {
            composeRule.onNodeWithTag("workout_overview").performScrollToNode(hasText(it))
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
        composeRule.onNodeWithTag("workout_overview").performScrollToNode(hasText("Força A"))
        composeRule.onNodeWithText("Iniciar", substring = false).performClick()
        composeRule.waitUntil(5_000) { viewModel.uiState.value.activeSession?.exercises?.size == 1 }
        assertEquals("Força A", viewModel.uiState.value.activeSession?.title)
        assertEquals("Supino", viewModel.uiState.value.activeSession?.exercises?.single()?.name)
        runBlocking {
            val saved = WorkoutTemplateRepositoryImpl(
                database.trainingModelDao(), database.trainingModelExerciseDao(), WorkoutRepositoryImpl(database.workoutDao()),
            ).observeTemplates().first()
            assertEquals(40.0, saved.single().exercises.single().weightKg!!, 0.0)
            assertEquals("Força A", database.workoutDao().getActiveSessionSync()?.session?.title)
        }
    }
}
