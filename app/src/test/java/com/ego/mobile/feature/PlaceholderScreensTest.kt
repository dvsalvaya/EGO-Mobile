package com.ego.mobile.feature

import com.ego.mobile.app.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceholderScreensTest {

    @Test
    fun `all five destinations are exposed with the approved labels`() {
        val destinations = AppDestination.entries

        assertEquals(5, destinations.size)
        assertEquals(
            listOf("Início", "Hábitos", "Tarefas", "Treinos", "Cabelo"),
            destinations.map { it.title },
        )
    }

    @Test
    fun `home remains the default entry point for the app skeleton`() {
        assertEquals(AppDestination.HOME, AppDestination.entries.first())
        assertEquals("home", AppDestination.HOME.route)
    }
}
