package com.ego.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.feature.habits.HabitFormDialog
import com.ego.mobile.feature.home.components.ActiveRoutineBanner
import com.ego.mobile.feature.home.components.PriorityTasksCard
import com.ego.mobile.feature.home.components.QuickActionHeader
import com.ego.mobile.feature.home.components.TodayHabitsSummaryCard
import com.ego.mobile.feature.tasks.components.TaskFormDialog

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = rememberHomeViewModel(),
    onContinueRoutine: (ActivityType) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showHabitForm by remember { mutableStateOf(false) }
    var showTaskForm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.OpenHabitCreation -> showHabitForm = true
                HomeEvent.OpenTaskCreation -> showTaskForm = true
            }
        }
    }

    val errorMessage = when (uiState) {
        HomeUiState.Loading -> null
        is HomeUiState.Content -> (uiState as HomeUiState.Content).errorMessage
        is HomeUiState.Error -> (uiState as HomeUiState.Error).message
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { contentPadding ->
        when (val state = uiState) {
            HomeUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is HomeUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    QuickActionHeader(
                        onCreateHabit = viewModel::onCreateHabitRequested,
                        onCreateTask = viewModel::onCreateTaskRequested,
                    )
                }
                state.summary.activeRoutine?.let { routine ->
                    item {
                        ActiveRoutineBanner(
                            routine = routine,
                            onContinue = { onContinueRoutine(routine.type) },
                        )
                    }
                }
                item {
                    TodayHabitsSummaryCard(
                        habits = state.summary.todayHabits,
                        onComplete = viewModel::onCompleteHabit,
                        onUndo = viewModel::onUndoHabit,
                    )
                }
                item {
                    PriorityTasksCard(
                        tasks = state.summary.priorityTasks,
                        nowMillis = System.currentTimeMillis(),
                        onComplete = viewModel::onCompleteTask,
                    )
                }
            }
        }
    }

    if (showHabitForm) {
        HabitFormDialog(
            onDismiss = { showHabitForm = false },
            onSave = { name, description, frequency ->
                viewModel.onCreateHabit(name, description, frequency)
                showHabitForm = false
            },
        )
    }
    if (showTaskForm) {
        TaskFormDialog(
            task = null,
            onDismiss = { showTaskForm = false },
            onSave = { title, description, dueAt ->
                viewModel.onCreateTask(title, description, dueAt)
                showTaskForm = false
            },
        )
    }
}
