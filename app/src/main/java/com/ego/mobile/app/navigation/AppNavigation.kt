package com.ego.mobile.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ego.mobile.feature.habits.HabitsScreen
import com.ego.mobile.feature.history.HistoryScreen
import com.ego.mobile.feature.home.HomeScreen
import com.ego.mobile.feature.routines.haircare.HaircareScreen
import com.ego.mobile.feature.routines.workouts.WorkoutScreen
import com.ego.mobile.feature.tasks.TasksScreen

@Composable
fun AppNavigation(
    navigationState: NavigationState = rememberSaveable(saver = NavigationState.Saver) { NavigationState() },
) {
    val destination = navigationState.currentDestination
    val isHistoryOpen = navigationState.isHistoryOpen

    BackHandler(enabled = isHistoryOpen || destination != AppDestination.HOME) {
        navigationState.handleBack()
    }

    val screenContent: @Composable () -> Unit = when {
        isHistoryOpen -> {
            {
                HistoryScreen()
            }
        }
        else -> when (destination) {
            AppDestination.HOME -> {
                {
                    HomeScreen(
                        onContinueRoutine = { type ->
                            navigationState.navigateToRoutine(type)
                        },
                        onOpenHistory = {
                            navigationState.openHistory()
                        },
                    )
                }
            }
            AppDestination.HABITS -> { { HabitsScreen() } }
            AppDestination.TASKS -> { { TasksScreen() } }
            AppDestination.WORKOUTS -> { { WorkoutScreen() } }
            AppDestination.HAIRCARE -> { { HaircareScreen() } }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                AppDestination.entries.forEach { appDestination ->
                    NavigationBarItem(
                        selected = !isHistoryOpen && destination == appDestination,
                        onClick = { navigationState.navigateTo(appDestination) },
                        icon = {
                            Text(
                                text = appDestination.iconGlyph,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                        label = { Text(appDestination.title) },
                        alwaysShowLabel = true,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .semantics {
                                contentDescription = appDestination.title
                            },
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
