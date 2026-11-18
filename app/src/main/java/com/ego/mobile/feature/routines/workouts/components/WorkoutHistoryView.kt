package com.ego.mobile.feature.routines.workouts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.feature.common.EmptyState
import com.ego.mobile.feature.common.SectionHeader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WorkoutHistoryView(
    completedSessions: List<WorkoutSession>,
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        SectionHeader(title = "Histórico de Treinos")

        Spacer(modifier = Modifier.height(12.dp))

        if (completedSessions.isEmpty()) {
            EmptyState(
                title = "Nenhum treino concluído ainda",
                message = "Nenhum treino concluído ainda. Inicie seu primeiro treino acima!",
                actionLabel = "Iniciar Treino",
                onActionClick = onStartWorkout,
                isCard = true,
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                completedSessions.forEach { session ->
                    HistoricalSessionItem(session = session)
                }
            }
        }
    }
}

@Composable
private fun HistoricalSessionItem(session: WorkoutSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = formatDateTime(session.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val durationFormatted = formatDuration(session.durationMillis())
            val exercisesCount = session.exercises.size
            val exerciseText = if (exercisesCount == 1) "1 exercício" else "$exercisesCount exercícios"

            Text(
                text = "Duração: $durationFormatted • $exerciseText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (session.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                val exerciseNames = session.exercises.joinToString(", ") { it.name }
                Text(
                    text = exerciseNames,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                )
            }
        }
    }
}

private fun formatDateTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
