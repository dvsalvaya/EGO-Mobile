package com.ego.mobile.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class NavigationState(initialDestination: AppDestination = AppDestination.HOME) {
    var currentDestination: AppDestination by mutableStateOf(initialDestination)
        private set

    fun navigateTo(destination: AppDestination) {
        currentDestination = destination
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
