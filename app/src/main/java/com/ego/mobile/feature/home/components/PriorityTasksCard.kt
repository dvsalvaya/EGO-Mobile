package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
import com.ego.mobile.feature.common.EmptyState
import com.ego.mobile.feature.common.SectionHeader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PriorityTasksCard(
    tasks: List<Task>,
    nowMillis: Long,
    onComplete: (Long) -> Unit,
    totalCount: Int = tasks.size,
    onOpenAll: () -> Unit = {},
    onTaskSelected: (Task) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader(title = "Para hoje · Tarefas ($totalCount)")
                androidx.compose.material3.TextButton(onClick = onOpenAll) { Text("Ver tudo") }
            }
            if (tasks.isEmpty()) {
                EmptyState(
                    title = "Tudo em dia",
                    message = "Tarefas sem prazo e futuras ficam disponíveis na aba Tarefas.",
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Text("✓", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    iconContentDescription = "Sem pendências",
                )
            } else {
                tasks.forEach { task ->
                    val overdue = task.isOverdue(nowMillis)
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onTaskSelected(task) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(task.title, fontWeight = FontWeight.SemiBold)
                            when {
                                overdue -> Text(
                                    "Atrasada",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                task.dueAt != null -> Text(
                                    "Prazo: ${DATE_FORMATTER.format(task.dueAt.toLocalDate())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Button(onClick = { onComplete(task.id) }) {
                            Text("Concluir")
                        }
                    }
                }
            }
        }
    }
}

private fun Long.toLocalDate() = Instant.ofEpochMilli(this)
    .atZone(ZoneId.of("America/Sao_Paulo"))
    .toLocalDate()

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
