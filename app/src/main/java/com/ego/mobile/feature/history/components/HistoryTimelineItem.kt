package com.ego.mobile.feature.history.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HistoryItem
import com.ego.mobile.domain.model.HabitPeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import com.ego.mobile.domain.model.habitZone
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryTimelineItem(
    item: HistoryItem,
    onCorrect: (Long, LocalDate, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val period = item.habitPeriod
    var showDetails by rememberSaveable(period?.habitId, period?.start, period?.weeklyTarget) { mutableStateOf(false) }
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
                    text = if (period?.weeklyTarget != null) {
                        "Semana de ${DATE.format(period.start)} a ${DATE.format(period.start.plusDays(6))}"
                    } else if (period != null) {
                        "Hábito · ${recordDate(period.start, period.days.single().occurrence?.completedAt)}"
                    } else {
                        "${typeLabel(item.type)} · ${DATE_TIME_FORMATTER.format(item.occurredAt.toLocalDateTime())}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (item.status == "COMPLETED") "Concluído" else item.status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (period != null) {
                    TextButton(onClick = { showDetails = true }) { Text("Ver registro") }
                }
            }
        }
    }
    if (showDetails && period != null) {
        HabitRecordDialog(item.title, period, onCorrect, onDismiss = { showDetails = false })
    }
}

@Composable
private fun HabitRecordDialog(
    title: String,
    period: HabitPeriod,
    onCorrect: (Long, LocalDate, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (day in period.days) {
                    val status = when {
                        day.completed -> "Concluído"
                        day.missedOn(period.today) -> "Não fiz"
                        !day.expected -> "Fora do plano"
                        else -> "Em andamento"
                    }
                    Text("${recordDate(day.date, day.occurrence?.completedAt)} · $status")
                    day.occurrence?.notes?.takeIf(String::isNotBlank)?.let { Text(it) }
                    TextButton(onClick = { onCorrect(period.habitId, day.date, !day.completed) }) {
                        Text(if (day.completed) "Não fiz" else "Fiz nesse dia")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

private val DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun recordDate(date: LocalDate, completedAt: Long?): String {
    val time = completedAt?.toLocalDateTime()
    return if (time?.toLocalDate() == date && time.toLocalTime() != LocalTime.MIDNIGHT) {
        DATE_TIME_FORMATTER.format(time)
    } else {
        DATE.format(date)
    }
}

private fun typeLabel(type: ActivityType): String = when (type) {
    ActivityType.HABIT -> "Hábito"
    ActivityType.TASK -> "Tarefa"
    ActivityType.WORKOUT -> "Treino"
    ActivityType.HAIRCARE -> "Cuidado capilar"
}

private fun Long.toLocalDateTime() = Instant.ofEpochMilli(this)
    .atZone(habitZone)
    .toLocalDateTime()

private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
    "dd/MM/yyyy · HH:mm",
    Locale.forLanguageTag("pt-BR"),
)
