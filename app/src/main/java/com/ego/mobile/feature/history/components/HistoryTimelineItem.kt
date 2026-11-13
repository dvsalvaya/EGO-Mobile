package com.ego.mobile.feature.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HistoryItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryTimelineItem(
    item: HistoryItem,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (item.type) {
                    ActivityType.HABIT -> "H"
                    ActivityType.TASK -> "T"
                    ActivityType.WORKOUT -> "W"
                    ActivityType.HAIRCARE -> "C"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                item.subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = "${typeLabel(item.type)} · ${DATE_TIME_FORMATTER.format(item.occurredAt.toLocalDateTime())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (item.status == "COMPLETED") "Concluído" else item.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun typeLabel(type: ActivityType): String = when (type) {
    ActivityType.HABIT -> "Hábito"
    ActivityType.TASK -> "Tarefa"
    ActivityType.WORKOUT -> "Treino"
    ActivityType.HAIRCARE -> "Cuidado capilar"
}

private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this)
    .atZone(ZoneId.of("America/Sao_Paulo"))
    .toLocalDateTime()

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy · HH:mm",
    Locale.forLanguageTag("pt-BR"),
)
