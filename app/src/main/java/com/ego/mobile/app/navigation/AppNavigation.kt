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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.ego.mobile.feature.habits.HabitsScreen
import com.ego.mobile.feature.history.HistoryScreen
import com.ego.mobile.feature.home.HomeScreen
import com.ego.mobile.feature.routines.RoutinesScreen
import com.ego.mobile.feature.tasks.TasksScreen

@Composable
fun AppNavigation() {
    val navigationState = rememberSaveable(saver = NavigationState.Saver) { NavigationState() }
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
