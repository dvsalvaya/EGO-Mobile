package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
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
    val emptyCardHeight = if (LocalDensity.current.fontScale >= 1.25f) 300.dp else 230.dp
    Card(modifier = modifier.fillMaxWidth().then(if (tasks.isEmpty()) Modifier.height(emptyCardHeight) else Modifier).testTag("home_today_tasks_card")) {
        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TasksSummaryHeader(totalCount, onOpenAll)
                    CompactTasksEmptyState()
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TasksSummaryHeader(totalCount, onOpenAll)
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

@Composable
private fun TasksSummaryHeader(totalCount: Int, onOpenAll: () -> Unit) {
    Row(Modifier.fillMaxWidth().requiredHeightIn(max = 80.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionHeader(title = "Para hoje · Tarefas ($totalCount)", modifier = Modifier.weight(1f))
        androidx.compose.material3.TextButton(onClick = onOpenAll) { Text("Ver tudo") }
    }
}

@Composable
private fun CompactTasksEmptyState() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).requiredHeightIn(max = 140.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).semantics { contentDescription = "Sem pendências" }, contentAlignment = Alignment.Center) {
            Text("✓", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tudo em dia", style = MaterialTheme.typography.titleMedium)
            Text("Tarefas sem prazo e futuras ficam disponíveis na aba Tarefas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Long.toLocalDate() = Instant.ofEpochMilli(this)
    .atZone(ZoneId.of("America/Sao_Paulo"))
    .toLocalDate()

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
