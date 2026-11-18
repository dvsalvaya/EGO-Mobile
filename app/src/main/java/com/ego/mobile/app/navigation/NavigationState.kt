package com.ego.mobile.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import com.ego.mobile.domain.model.ActivityType

class NavigationState(
    initialDestination: AppDestination = AppDestination.HOME,
    initialIsHistoryOpen: Boolean = false,
) {
    var currentDestination: AppDestination by mutableStateOf(initialDestination)
        private set

    var isHistoryOpen: Boolean by mutableStateOf(initialIsHistoryOpen)
        private set

    fun navigateTo(destination: AppDestination) {
        currentDestination = destination
        isHistoryOpen = false
    }

    fun openHistory() {
        isHistoryOpen = true
    }

    fun closeHistory() {
        isHistoryOpen = false
    }

    fun navigateToRoutine(type: ActivityType) {
        currentDestination = if (type == ActivityType.HAIRCARE) {
            AppDestination.HAIRCARE
        } else {
            AppDestination.WORKOUTS
        }
        isHistoryOpen = false
    }

    fun handleBack(): Boolean {
        return if (isHistoryOpen) {
            isHistoryOpen = false
            true
        } else if (currentDestination != AppDestination.HOME) {
            currentDestination = AppDestination.HOME
            true
        } else {
            false
        }
    }

    companion object {
        val Saver: Saver<NavigationState, List<Int>> = Saver(
            save = { listOf(it.currentDestination.ordinal, if (it.isHistoryOpen) 1 else 0) },
            restore = { values ->
                NavigationState(
                    initialDestination = AppDestination.entries.getOrElse(values[0]) { AppDestination.HOME },
                    initialIsHistoryOpen = values.getOrElse(1) { 0 } == 1,
                )
            },
        )
    }
}
