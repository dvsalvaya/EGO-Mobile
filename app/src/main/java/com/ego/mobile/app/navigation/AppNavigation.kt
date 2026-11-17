package com.ego.mobile.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ego.mobile.feature.habits.HabitsScreen
import com.ego.mobile.feature.history.HistoryScreen
import com.ego.mobile.feature.home.HomeScreen
import com.ego.mobile.feature.routines.RoutinesScreen
import com.ego.mobile.feature.tasks.TasksScreen

@Composable
private fun rememberNavigationState(): NavigationState {
    var savedDestination by rememberSaveable { mutableStateOf(AppDestination.HOME.name) }
    var savedRoutinesTab by rememberSaveable { mutableStateOf(0) }
    val navigationState = remember {
        NavigationState(
            initialDestination = runCatching { AppDestination.valueOf(savedDestination) }
                .getOrDefault(AppDestination.HOME),
            initialRoutinesTab = savedRoutinesTab,
        )
    }
    LaunchedEffect(navigationState.currentDestination) {
        savedDestination = navigationState.currentDestination.name
    }
    LaunchedEffect(navigationState.routinesInitialTab) {
        savedRoutinesTab = navigationState.routinesInitialTab
    }
    return navigationState
}

@Composable
fun AppNavigation() {
    val navigationState = rememberNavigationState()
    val destination = navigationState.currentDestination

    BackHandler(enabled = destination != AppDestination.HOME) {
        navigationState.navigateTo(AppDestination.HOME)
    }

    val screenContent: @Composable () -> Unit = when (destination) {
        AppDestination.HOME -> {
            {
                HomeScreen(
                    onContinueRoutine = { type ->
                        navigationState.navigateToRoutine(type)
                    },
                )
            }
        }
        AppDestination.HABITS -> { { HabitsScreen() } }
        AppDestination.TASKS -> { { TasksScreen() } }
        AppDestination.ROUTINES -> { { RoutinesScreen(navigationState.routinesInitialTab) } }
        AppDestination.HISTORY -> { { HistoryScreen() } }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                AppDestination.entries.forEach { appDestination ->
                    NavigationBarItem(
                        selected = destination == appDestination,
                        onClick = { navigationState.navigateTo(appDestination) },
                        icon = {
                            Text(
                                text = appDestination.iconGlyph,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        label = { Text(appDestination.title) },
                        alwaysShowLabel = true,
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            screenContent()
        }
    }
}

