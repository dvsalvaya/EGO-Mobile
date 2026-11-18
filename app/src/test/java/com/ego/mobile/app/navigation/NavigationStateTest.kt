package com.ego.mobile.app.navigation

import com.ego.mobile.domain.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {

    @Test
    fun `starts at Home by default`() {
        val state = NavigationState()
        assertEquals(AppDestination.HOME, state.currentDestination)
        assertFalse(state.isHistoryOpen)
    }

    @Test
    fun `has exactly five destinations defined`() {
        val destinations = AppDestination.values()
        assertEquals(5, destinations.size)
        assertEquals(
            listOf("Início", "Hábitos", "Tarefas", "Treinos", "Cabelo"),
            destinations.map { it.title },
        )
    }

    @Test
    fun `navigating to another destination updates currentDestination and closes history`() {
        val state = NavigationState()
        state.openHistory()
        assertTrue(state.isHistoryOpen)

        state.navigateTo(AppDestination.HABITS)
        assertEquals(AppDestination.HABITS, state.currentDestination)
        assertFalse(state.isHistoryOpen)

        state.navigateTo(AppDestination.TASKS)
        assertEquals(AppDestination.TASKS, state.currentDestination)

        state.navigateTo(AppDestination.WORKOUTS)
        assertEquals(AppDestination.WORKOUTS, state.currentDestination)

        state.navigateTo(AppDestination.HAIRCARE)
        assertEquals(AppDestination.HAIRCARE, state.currentDestination)
    }

    @Test
    fun `selecting same destination is idempotent`() {
        val state = NavigationState()
        state.navigateTo(AppDestination.HABITS)
        state.navigateTo(AppDestination.HABITS)
        assertEquals(AppDestination.HABITS, state.currentDestination)
    }

    @Test
    fun `handleBack on secondary destination returns to Home and returns true`() {
        val secondaryDestinations = listOf(
            AppDestination.HABITS,
            AppDestination.TASKS,
            AppDestination.WORKOUTS,
            AppDestination.HAIRCARE,
        )

        for (dest in secondaryDestinations) {
            val state = NavigationState()
            state.navigateTo(dest)
            val handled = state.handleBack()
            assertTrue("handleBack should be true for $dest", handled)
            assertEquals(AppDestination.HOME, state.currentDestination)
        }
    }

    @Test
    fun `handleBack on Home returns false and stays on Home`() {
        val state = NavigationState()
        val handled = state.handleBack()
        assertFalse(handled)
        assertEquals(AppDestination.HOME, state.currentDestination)
    }

    @Test
    fun `openHistory opens history and handleBack closes history returning to home`() {
        val state = NavigationState()
        state.openHistory()
        assertTrue(state.isHistoryOpen)

        val handled = state.handleBack()
        assertTrue("handleBack should be handled when history is open", handled)
        assertFalse(state.isHistoryOpen)
        assertEquals(AppDestination.HOME, state.currentDestination)
    }

    @Test
    fun `closeHistory explicitly closes history without changing currentDestination`() {
        val state = NavigationState()
        state.navigateTo(AppDestination.HABITS)
        state.openHistory()
        assertTrue(state.isHistoryOpen)

        state.closeHistory()
        assertFalse(state.isHistoryOpen)
        assertEquals(AppDestination.HABITS, state.currentDestination)
    }

    @Test
    fun `navigateToRoutine directs to WORKOUTS or HAIRCARE and closes history`() {
        val state = NavigationState()
        state.openHistory()
        state.navigateToRoutine(ActivityType.WORKOUT)
        assertEquals(AppDestination.WORKOUTS, state.currentDestination)
        assertFalse(state.isHistoryOpen)

        state.openHistory()
        state.navigateToRoutine(ActivityType.HAIRCARE)
        assertEquals(AppDestination.HAIRCARE, state.currentDestination)
        assertFalse(state.isHistoryOpen)
    }

    @Test
    fun `saveable state restores selected destination and history open state`() {
        val state = NavigationState()
        state.navigateTo(AppDestination.WORKOUTS)
        state.openHistory()

        val restored = NavigationState.Saver.restore(
            listOf(
                AppDestination.WORKOUTS.ordinal,
                if (state.isHistoryOpen) 1 else 0,
            ),
        )

        assertEquals(AppDestination.WORKOUTS, restored?.currentDestination)
        assertTrue(restored?.isHistoryOpen == true)
    }
}
