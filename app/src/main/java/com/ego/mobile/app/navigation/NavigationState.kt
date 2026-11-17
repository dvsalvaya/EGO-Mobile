package com.ego.mobile.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ego.mobile.domain.model.ActivityType

class NavigationState(
    initialDestination: AppDestination = AppDestination.HOME,
    initialRoutinesTab: Int = 0,
) {
    var currentDestination: AppDestination by mutableStateOf(initialDestination)
        private set
    var routinesInitialTab: Int by mutableStateOf(initialRoutinesTab)
        private set

    fun navigateTo(destination: AppDestination) {
        currentDestination = destination
    }

    fun navigateToRoutine(type: ActivityType) {
        routinesInitialTab = if (type == ActivityType.HAIRCARE) 1 else 0
        currentDestination = AppDestination.ROUTINES
    }

    fun handleBack(): Boolean {
        return if (currentDestination != AppDestination.HOME) {
            currentDestination = AppDestination.HOME
            true
        } else {
            false
        }
    }
}