package com.ego.mobile.feature.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsBottomSheet(
    task: Task,
    nowMillis: Long,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onReschedule: () -> Unit,
    onDelete: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(task.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                when {
                    task.status == TaskStatus.COMPLETED -> "Concluída"
                    task.isOverdue(nowMillis) -> "⚠ Atrasada"
                    else -> "Em aberto"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (task.isOverdue(nowMillis)) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                task.dueAt?.let { "Prazo: ${formatDate(it)}" } ?: "Sem prazo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("task_details_due_date"),
            )
            task.completedAt?.let {
                Text(
                    "Concluída em ${formatDate(it)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            task.description?.takeIf(String::isNotBlank)?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
            if (task.status == TaskStatus.OPEN) {
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                    Text("Concluir")
                }
            }
            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
            if (task.status == TaskStatus.OPEN) {
                OutlinedButton(onClick = onReschedule, modifier = Modifier.fillMaxWidth()) { Text("Reagendar") }
            }
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Excluir") }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text("Fechar")
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String = DATE_FORMATTER.format(
    Instant.ofEpochMilli(epochMillis).atZone(PRODUCT_ZONE),
)

private val PRODUCT_ZONE = ZoneId.of("America/Sao_Paulo")
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
