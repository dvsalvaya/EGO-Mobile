package com.ego.mobile.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
import com.ego.mobile.feature.tasks.components.DeleteConfirmDialog
import com.ego.mobile.feature.tasks.components.RescheduleDialog
import com.ego.mobile.feature.tasks.components.TaskFormDialog
import com.ego.mobile.feature.tasks.components.TaskItem
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = rememberTasksViewModel(),
) {
    val uiState by viewModel.tasksUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showForm by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var taskToReschedule by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TasksEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${event.title}\" excluída.",
                        actionLabel = "Desfazer",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    } else {
                        viewModel.discardPendingUndo()
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        showForm = true
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Adicionar tarefa"
                    },
                ) {
                    Text("+")
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Tarefas", style = MaterialTheme.typography.headlineMedium)
                    TextButton(
                        onClick = { viewModel.setShowCompletedTasks(!uiState.showCompletedTasks) },
                        modifier = Modifier.align(Alignment.Start),
                    ) {
                        Text(
                            if (uiState.showCompletedTasks) "Exibir tarefas abertas"
                            else "Exibir concluídas (${uiState.completedTasks.size})",
                        )
                    }
                    uiState.errorMessage?.takeIf { uiState.visibleTasks.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                when {
                    uiState.isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    uiState.visibleTasks.isEmpty() && uiState.errorMessage != null -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        uiState.errorMessage?.let { message ->
                            Text(
                                text = message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    uiState.visibleTasks.isEmpty() -> Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (uiState.showCompletedTasks) {
                                "Nenhuma tarefa concluída"
                            } else {
                                "Nenhuma tarefa aberta"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (uiState.showCompletedTasks) {
                                "As tarefas concluídas aparecerão aqui."
                            } else {
                                "Crie uma tarefa para organizar seu dia. O prazo é opcional."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (!uiState.showCompletedTasks) {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    editingTask = null
                                    showForm = true
                                },
                            ) {
                                Text("Adicionar tarefa")
                            }
                        }
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.visibleTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                nowMillis = uiState.currentTimeMillis,
                                onComplete = { viewModel.onCompleteTask(task.id) },
                                onEdit = {
                                    editingTask = task
                                    showForm = true
                                },
                                onReschedule = { taskToReschedule = task },
                                onDelete = { taskToDelete = task },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        TaskFormDialog(
            task = editingTask,
            onDismiss = { showForm = false },
            onSave = { title, description, dueAt ->
                if (editingTask == null) {
                    viewModel.onCreateTask(title, description, dueAt)
                } else {
                    viewModel.onUpdateTask(
                        editingTask!!.copy(
                            title = title,
                            description = description,
                            dueAt = dueAt,
                        ),
                    )
                }
                showForm = false
            },
        )
    }

    taskToReschedule?.let { task ->
        RescheduleDialog(
            initialDate = task.dueAt
                ?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate() }
                ?: LocalDate.now(ZoneId.of("America/Sao_Paulo")),
            onDismiss = { taskToReschedule = null },
            onReschedule = { dueAt ->
                viewModel.onRescheduleTask(task.id, dueAt)
                taskToReschedule = null
            },
        )
    }

    taskToDelete?.let { task ->
        DeleteConfirmDialog(
            taskTitle = task.title,
            onDismiss = { taskToDelete = null },
            onConfirm = {
                viewModel.onDeleteTask(task.id)
                taskToDelete = null
            },
        )
    }
}
