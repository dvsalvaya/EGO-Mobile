package com.ego.mobile.app.navigation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.ego.mobile.core.ui.theme.EgoTheme
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private val database
        get() = EgoDatabaseProvider.getDatabase(context)

    @Before
    fun setUp() {
        context.deleteDatabase("ego_mobile.db")
        EgoDatabaseProvider.resetForTesting()
    }

    @After
    fun tearDown() {
        EgoDatabaseProvider.resetForTesting()
        context.deleteDatabase("ego_mobile.db")
    }

    @Test
    fun bottomBarDisplaysExactlyTheFiveApprovedDestinations() {
        val navigationState = NavigationState()

        composeRule.setContent {
            EgoTheme {
                AppNavigation(navigationState = navigationState)
            }
        }

        // AC-02: Exactly Início, Hábitos, Tarefas, Treinos, Cabelo
        listOf("Início", "Hábitos", "Tarefas", "Treinos", "Cabelo").forEach { label ->
            val tab = hasText(label) and hasClickAction()
            composeRule.onNode(tab).assertIsDisplayed()
        }

        // Histórico must NOT appear as bottom navigation item
        composeRule.onAllNodes(hasContentDescription("Histórico") and hasClickAction())
            .assertCountEquals(0)
    }

    @Test
    fun selectingDestinationsUpdatesContentAndMaintainsSelection() {
        val navigationState = NavigationState()

        composeRule.setContent {
            EgoTheme {
                AppNavigation(navigationState = navigationState)
            }
        }

        // Initially on Home
        assertEquals(AppDestination.HOME, navigationState.currentDestination)
        composeRule.onNode(hasText("Início") and hasClickAction()).assertIsSelected()

        // Navigate to Hábitos
        composeRule.onNode(hasText("Hábitos") and hasClickAction()).performClick()
        assertEquals(AppDestination.HABITS, navigationState.currentDestination)
        composeRule.onNode(hasText("Hábitos") and hasClickAction()).assertIsSelected()

        // Navigate to Tarefas
        composeRule.onNode(hasText("Tarefas") and hasClickAction()).performClick()
        assertEquals(AppDestination.TASKS, navigationState.currentDestination)
        composeRule.onNode(hasText("Tarefas") and hasClickAction()).assertIsSelected()

        // Navigate to Treinos
        composeRule.onNode(hasText("Treinos") and hasClickAction()).performClick()
        composeRule.waitForIdle()
        assertEquals(AppDestination.WORKOUTS, navigationState.currentDestination)
        assertTrue(composeRule.activity.onBackPressedDispatcher.hasEnabledCallbacks())
        composeRule.onNode(hasText("Treinos") and hasClickAction()).assertIsSelected()

        // Navigate to Cabelo
        composeRule.onNode(hasText("Cabelo") and hasClickAction()).performClick()
        assertEquals(AppDestination.HAIRCARE, navigationState.currentDestination)
        composeRule.onNode(hasText("Cabelo") and hasClickAction()).assertIsSelected()
    }

    @Test
    fun internalHistoryOpensFromHomeAndBackReturnsToHome() {
        val navigationState = NavigationState()

        composeRule.setContent {
            EgoTheme {
                AppNavigation(navigationState = navigationState)
            }
        }

        // Verify "Ver histórico" is available on Home
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Ver histórico").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Ver histórico").performClick()

        // History is open
        assertTrue(navigationState.isHistoryOpen)
        composeRule.onNodeWithText("Histórico").assertIsDisplayed()

        // Pressing back returns to Home
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertFalse(navigationState.isHistoryOpen)
        assertEquals(AppDestination.HOME, navigationState.currentDestination)
    }

    @Test
    fun backOnSecondaryDestinationReturnsToHome() {
        val navigationState = NavigationState()

        composeRule.setContent {
            EgoTheme {
                AppNavigation(navigationState = navigationState)
            }
        }

        composeRule.onNode(hasText("Treinos") and hasClickAction()).performClick()
        composeRule.waitForIdle()
        assertEquals(AppDestination.WORKOUTS, navigationState.currentDestination)

        composeRule.runOnUiThread {
            val handled = navigationState.handleBack()
            assertTrue(handled)
        }
        composeRule.waitForIdle()

        assertEquals(AppDestination.HOME, navigationState.currentDestination)
    }

    @Test
    fun switchingDestinationsPreservesActiveWorkoutSession() = runBlocking {
        val now = System.currentTimeMillis()
        database.workoutDao().insertSession(
            WorkoutSessionEntity(
                title = "Treino Ativo Teste",
                startedAt = now,
                endedAt = null,
                status = "IN_PROGRESS",
            ),
        )

        val navigationState = NavigationState()

        composeRule.setContent {
            EgoTheme {
                AppNavigation(navigationState = navigationState)
            }
        }

        // Go to Treinos
        composeRule.onNode(hasText("Treinos") and hasClickAction()).performClick()
        assertEquals(AppDestination.WORKOUTS, navigationState.currentDestination)

        // Switch away to Tarefas
        composeRule.onNode(hasText("Tarefas") and hasClickAction()).performClick()
        assertEquals(AppDestination.TASKS, navigationState.currentDestination)

        // Switch back to Treinos
        composeRule.onNode(hasText("Treinos") and hasClickAction()).performClick()
        assertEquals(AppDestination.WORKOUTS, navigationState.currentDestination)

        // Check active session still exists and is not duplicated
        val active = database.workoutDao().getActiveSessionSync()
        assertNotNull(active)
        assertEquals("Treino Ativo Teste", active?.session?.title)
        assertEquals("IN_PROGRESS", active?.session?.status)
    }
}
