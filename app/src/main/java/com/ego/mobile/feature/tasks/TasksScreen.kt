package com.ego.mobile.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.ego.mobile.app.theme.Colors
import com.ego.mobile.app.theme.Spacing
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

    // Classificar tarefas em Atrasadas, Hoje e Próximas
    val classifiedTasks = remember(uiState.visibleTasks) {
        val now = uiState.currentTimeMillis
        tasks ->
            tasks.sortedWith(compareBy<Task>({ !it.isOverdue(now) }, { it.dueAt ?: Long.MAX_VALUE }, { -it.createdAt }))
    }

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TasksEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "\"${event.title}\" excluída.",
                        actionLabel = "Desfazer",
                        duration = androidx.compose.material3.SnackbarDuration.Short,
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

    Scaffold(
        containerColor = Colors.background,
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Título da tela
            Text("Tarefas", style = MaterialTheme.typography.headlineMedium)

            // Ação: alternar conclusão
            TextButton(
                onClick = { viewModel.setShowCompletedTasks(!uiState.showCompletedTasks) },
                modifier = Modifier.align(Alignment.Start),
            ) {
                Text(
                    if (uiState.showCompletedTasks) "Exibir tarefas abertas"
                    else "Exibir concluídas (${uiState.completedTasks.size})",
                )
            }

            // Mensagem de erro
            uiState.errorMessage?.takeIf { uiState.visibleTasks.isNotEmpty() }?.let { error ->
                Text(
                    text = error,
                    color = Colors.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Estados de loading, vazio e conteúdo
            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                uiState.visibleTasks.isEmpty() && uiState.errorMessage != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(Spacing.lg),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = Colors.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                uiState.visibleTasks.isEmpty() -> Column(
                    modifier = Modifier.fillMaxSize().padding(Spacing.lg),
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
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        text = if (!uiState.showCompletedTasks) {
                            "Crie uma tarefa para organizar seu dia. O prazo é opcional."
                        } else {
                            "As tarefas concluídas aparecerão aqui."
                        },
                        color = Colors.onSurfaceVariant,
                    )
                    if (!uiState.showCompletedTasks) {
                        Spacer(Modifier.height(Spacing.md))
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
                        start = Spacing.md,
                        end = Spacing.md,
                        top = Spacing.xs,
                        bottom = SnackbarHostDefaults.Height,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    // Agrupar tarefas em Atrasadas, Hoje, Próximas, Sem prazo
                    // 1. Atrasadas: isOverdue(now) = true
                    // 2. Hoje: dueAt não nulo && isDueToday
                    // 3. Próximas: dueAt não nulo && devido futuro (não atrasado)
                    // 4. Sem prazo: dueAt nulo

                    // Agrupamento
                    val (overdueTasks, dueTodayTasks, futureTasks, noDueTasks) = classifyTasks(
                        classifiedTasks,
                        uiState.currentTimeMillis
                    )

                    // Seção: Atrasadas
                    if (overdueTasks.isNotEmpty()) {
                        item {
                            _TaskSectionHeader(title = "Atrasadas")
                        }
                        items(overdueTasks, key = { it.id }) { task ->
                            _TaskItem(
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

                    // Seção: Hoje
                    if (dueTodayTasks.isNotEmpty()) {
                        item {
                            _TaskSectionHeader(title = "Hoje")
                        }
                        items(dueTodayTasks, key = { it.id }) { task ->
                            _TaskItem(
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

                    // Seção: Próximas
                    if (futureTasks.isNotEmpty()) {
                        item {
                            _TaskSectionHeader(title = "Próximas")
                        }
                        items(futureTasks, key = { it.id }) { task ->
                            _TaskItem(
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

                    // Seção: Sem prazo (sempre ao final)
                    if (noDueTasks.isNotEmpty()) {
                        item {
                            _TaskSectionHeader(title = "Sem prazo")
                        }
                        items(noDueTasks, key = { it.id }) { task ->
                            _TaskItem(
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

        // Bottom Sheet para criação/edição
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

        // Diálogo de reagendamento
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

        // Diálogo de confirmação de exclusão
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
}

// Classifica tarefas em grupos
private fun classifyTasks(
    tasks: List<Task>,
    nowMillis: Long,
): Tuple4<List<Task>, List<Task>, List<Task>, List<Task>> {
    val overdue = mutableListOf<Task>() // dueAt < agora
    val dueToday = mutableListOf<Task>() // dueAt == hoje (mesmo instante)
    val future = mutableListOf<Task>() // dueAt > agora e dueAt < fim do dia atual
    val noDue = mutableListOf<Task>() // dueAt == null

    for (task in tasks) {
        if (task.dueAt == null) {
            noDue.add(task)
        } else if (task.isOverdue(nowMillis)) {
            overdue.add(task)
        } else {
            // Verificar se é hoje
            val dueDate = LocalDate.ofEpochDay(
                task.dueAt / 86400000L
                    .until(java.time.LocalDate.EPOCH, ChronoUnit.DAYS)
            ).withZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDate()
            val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))

            if (dueDate == today) {
                dueToday.add(task)
            } else {
                future.add(task)
            }
        }
    }

    return overdue.toList(), dueToday.toList(), future.toList(), noDue.toList()
}

@Composable
private fun _TaskSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
        color = Colors.onSurfaceVariant,
    )
    Spacer(Modifier.height(Spacing.xs))
}

@Composable
private fun _TaskItem(
    task: Task,
    nowMillis: Long,
    onComplete: (Long) -> Unit,
    onEdit: () -> Unit,
    onReschedule: (Task) -> Unit,
    onDelete: (Task) -> Unit,
) {
    var isOverdue by remember { mutableStateOf(task.isOverdue(nowMillis)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.sm, Spacing.xs, Spacing.sm, Spacing.sm),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Colors.errorContainer else Colors.surface,
        ),
        shape = RoundedCornerShape(Spacing.control),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Informações da tarefa
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                    overflow = androidx.compose.ui.text.TextOverflow.Elide,
                )
                if (task.dueAt != null) {
                    Text(
                        text = "Prazo: ${formatDate(task.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) Colors.error else Colors.onSurfaceVariant,
                    )
                }
                if (task.description != null && !task.description.isEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.onSurfaceVariant,
                        overflow = androidx.compose.ui.text.TextOverflow.Elide,
                    )
                }
            }

            // Ações e status
            val isOverdue = task.isOverdue(nowMillis)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                if (isOverdue) {
                    Text(
                        text = "Atrasada",
                        style = MaterialTheme.typography.labelSmall,
                        color = Colors.error,
                    )
                }

                Button(
                    onClick = onComplete,
                    modifier = Modifier.semantics {
                        contentDescription = "Concluir tarefa: ${task.title}"
                    },
                ) {
                    Text("✓")
                }

                OutlinedButton(onClick = onEdit) {
                    Text("Editar")
                }

                // Only show reschedule for tasks with due date that are not overdue
                if (task.dueAt != null && !isOverdue) {
                    OutlinedButton(onClick = { onReschedule(task) }) {
                        Text("Reagendar")
                    }
                }

                // Only show delete for open tasks
                if (task.status == TaskStatus.OPEN) {
                    androidx.compose.material3.IconButton(
                        onClick = { onDelete(task) },
                    ) {
                        androidx.compose.material3Icons.Icons.Default.Delete
                        // Or use appropriate icon
                    }
                }
            }
        }
    }
}

private fun formatDate(dueAt: Long): String {
    return java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(dueAt)
}