package com.ego.mobile.feature.routines.haircare.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HaircareOccurrence
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val executionDateFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
private val haircareZone = ZoneId.of("America/Sao_Paulo")

fun LazyListScope.HaircareHistoryList(history: List<HaircareOccurrence>) {
    if (history.isEmpty()) return

    item(key = "haircare_history_heading") {
        Text(
            text = "Histórico de cuidados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
    items(
        items = history.sortedByDescending { it.executedAt ?: Long.MIN_VALUE },
        key = { "haircare_history_${it.id}" },
    ) { occurrence ->
        HaircareHistoryCard(occurrence)
    }
}

@Composable
private fun HaircareHistoryCard(occurrence: HaircareOccurrence) {
    val executedAt = occurrence.executedAt?.let { timestamp ->
        Instant.ofEpochMilli(timestamp)
            .atZone(haircareZone)
            .format(executionDateFormatter)
    } ?: "data indisponível"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = occurrence.stepType.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = occurrence.stepTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Executada em $executedAt",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            occurrence.notes?.takeIf(String::isNotBlank)?.let { notes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notas: $notes",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
