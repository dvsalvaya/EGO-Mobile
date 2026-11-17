package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.feature.home.HomeUiState
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus

@Composable
fun PriorityTasksCard(
    tasks: List<HomeUiState.TaskSummary>,
    nowMillis: Long,
    onComplete: (Long) -> Unit,
) {
    if (tasks.isEmpty()) {
        // Não mostrar nada se não há tarefas
        return
    }

    // Agrupar tarefas: Atrasadas, Hoje, Próximas, Sem prazo
    val (overdueTasks, dueTodayTasks, futureTasks, noDueTasks) = classifyTasks(tasks, nowMillis)

    // Seção: Atrasadas
    if (overdueTasks.isNotEmpty()) {
        Text(
            text = "Atrasadas (${overdueTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(4.dp))
        tasksInSection(overdueTasks) { task ->
            TaskItemInSection(
                task = task,
                isOverdue = task.isOverdue(nowMillis),
                onComplete = onComplete,
            )
        }
    }

    // Seção: Hoje
    if (dueTodayTasks.isNotEmpty()) {
        if (overdueTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = "Hoje (${dueTodayTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        tasksInSection(dueTodayTasks) { task ->
            TaskItemInSection(
                task = task,
                isOverdue = false,
                onComplete = onComplete,
            )
        }
    }

    // Seção: Próximas
    if (futureTasks.isNotEmpty()) {
        if (dueTodayTasks.isNotEmpty() || overdueTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = "Próximas (${futureTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        tasksInSection(futureTasks) { task ->
            TaskItemInSection(
                task = task,
                isOverdue = false,
                onComplete = onComplete,
            )
        }
    }

    // Seção: Sem prazo
    if (noDueTasks.isNotEmpty()) {
        if (futureTasks.isNotEmpty() || dueTodayTasks.isNotEmpty() || overdueTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = "Sem prazo (${noDueTasks.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        tasksInSection(noDueTasks) { task ->
            TaskItemInSection(
                task = task,
                isOverdue = false,
                showDueDate = false,
                onComplete = onComplete,
            )
        }
    }
}

private fun tasksInSection(
    tasks: List<HomeUiState.TaskSummary>,
    showDueDate: Boolean = true,
    onComplete: (Long) -> Unit,
) {
    items(tasks) { task ->
        TaskItemInSection(
            task = task,
            isOverdue = task.isOverdue,
            showDueDate = showDueDate,
            onComplete = onComplete,
        )
    }
}

private fun classifyTasks(
    tasks: List<HomeUiState.TaskSummary>,
    nowMillis: Long,
): Tuple4<List<HomeUiState.TaskSummary>, List<HomeUiState.TaskSummary>, List<HomeUiState.TaskSummary>, List<HomeUiState.TaskSummary>> {
    val overdue = mutableListOf<HomeUiState.TaskSummary>()
    val dueToday = mutableListOf<HomeUiState.TaskSummary>()
    val future = mutableListOf<HomeUiState.TaskSummary>()
    val noDue = mutableListOf<HomeUiState.TaskSummary>()

    for (task in tasks) {
        if (task.task.dueAt == null) {
            noDue.add(task)
        } else if (task.isOverdue) {
            overdue.add(task)
        } else {
            // Verificar se é hoje
            val dueDate = LocalDate.ofEpochDay(
                task.task.dueAt / 86400000L
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
private fun TaskItemInSection(
    task: HomeUiState.TaskSummary,
    isOverdue: Boolean,
    showDueDate: Boolean = true,
    onComplete: (Long) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Título e prazo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = task.task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                    overflow = androidx.compose.ui.text.TextOverflow.Elide,
                )
                if (showDueDate && task.task.dueAt != null) {
                    Text(
                        text = "Prazo: ${formatDate(task.task.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Status e ações
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                if (isOverdue) {
                    Text(
                        text = "Atrasada",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = { onComplete(task.task.id) },
                    modifier = Modifier.semantics {
                        contentDescription = "Concluir tarefa: ${task.task.title}"
                    },
                ) {
                    Text("✓")
                }
            }
        }
    }
}

private fun formatDate(dueAt: Long): String {
    return java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(dueAt)
}