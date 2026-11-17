package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.feature.home.HomeUiState
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitStatus

@Composable
fun TodayHabitsSummaryCard(
    habits: List<HomeUiState.HabitSummary>,
    onComplete: (Long) -> Unit,
) {
    if (habits.isEmpty()) {
        // Se não há hábitos previstos para hoje, mostrar mensagem
        Text(
            text = "Nenhum hábito previsto para hoje",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    // Grid compacto de hábitos previstos para hoje
    var rowIndex by remember { mutableStateOf(0) }
    // We'll render them in a simple column with row management
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(habits) { habitSummary ->
            TodayHabitSummaryItem(
                habit = habitSummary.habit,
                todayOccurrences = habitSummary.todayOccurrences,
                onComplete = onComplete,
            )
        }
    }
}

@Composable
private fun TodayHabitSummaryItem(
    habit: Habit,
    todayOccurrences: Int,
    onComplete: (Long) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.status == HabitStatus.PAUSED) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Nome e progresso do hábito
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                    overflow = androidx.compose.ui.text.TextOverflow.Elide,
                )
                Text(
                    text = "— ${todayOccurrences} previsto(s) para hoje",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Botão de conclusão rápida
            Button(
                onClick = { onComplete(habit.id) },
                modifier = Modifier.semantics {
                    contentDescription = "Concluir hábito: ${habit.name}"
                },
            ) {
                Text("✓")
            }
        }
    }
}