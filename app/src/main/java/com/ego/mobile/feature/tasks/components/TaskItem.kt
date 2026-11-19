package com.ego.mobile.feature.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    nowMillis: Long,
    onComplete: () -> Unit,
    onClick: () -> Unit,
) {
    val isOverdue = task.isOverdue(nowMillis)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .semantics { contentDescription = "Detalhes da tarefa: ${task.title}" },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = { if (it) onComplete() },
                enabled = task.status == TaskStatus.OPEN,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .semantics { contentDescription = "Concluir tarefa: ${task.title}" },
            )
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough
                    else TextDecoration.None,
                )
                when {
                    isOverdue -> Text(
                        text = "⚠ Atrasada · Prazo: ${formatDate(task.dueAt!!)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    task.dueAt != null -> Text(
                        text = "Prazo: ${formatDate(task.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> Text(
                        text = "Sem prazo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String = DATE_FORMATTER.format(
    Instant.ofEpochMilli(epochMillis).atZone(PRODUCT_ZONE),
)

private val PRODUCT_ZONE = ZoneId.of("America/Sao_Paulo")
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
