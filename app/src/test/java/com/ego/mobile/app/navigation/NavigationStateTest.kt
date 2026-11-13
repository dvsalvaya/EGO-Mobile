package com.ego.mobile.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationStateTest {

    @Test
    fun `starts at Home by default`() {
        val state = NavigationState()
        assertEquals(AppDestination.HOME, state.currentDestination)
    }

    @Test
    fun `has exactly five destinations defined`() {
        val destinations = AppDestination.values()
        assertEquals(5, destinations.size)
        assertEquals(
            listOf("Início", "Hábitos", "Tarefas", "Rotinas", "Histórico"),
            destinations.map { it.title }
        )
    }

    @Test
    fun `navigating to another destination updates currentDestination`() {
        val state = NavigationState()
        state.navigateTo(AppDestination.HABITS)
        assertEquals(AppDestination.HABITS, state.currentDestination)

        state.navigateTo(AppDestination.TASKS)
        assertEquals(AppDestination.TASKS, state.currentDestination)

        state.navigateTo(AppDestination.ROUTINES)
        assertEquals(AppDestination.ROUTINES, state.currentDestination)

        state.navigateTo(AppDestination.HISTORY)
        assertEquals(AppDestination.HISTORY, state.currentDestination)
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
            AppDestination.ROUTINES,
            AppDestination.HISTORY
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
}
