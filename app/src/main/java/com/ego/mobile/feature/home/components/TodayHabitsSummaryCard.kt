package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HomeSummary

@Composable
fun TodayHabitsSummaryCard(
    habits: List<HomeSummary.TodayHabit>,
    onComplete: (Long) -> Unit,
    onUndo: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Hábitos de hoje", style = MaterialTheme.typography.titleLarge)
            if (habits.isEmpty()) {
                Text(
                    "Nenhum hábito previsto para hoje.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                habits.forEach { habit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(habit.title, fontWeight = FontWeight.SemiBold)
                            if (habit.weeklyTarget != null) {
                                Text("Progresso da semana: ${habit.completedThisWeek}/${habit.weeklyTarget}")
                            }
                        }
                        if (habit.status == HabitOccurrenceStatus.COMPLETED) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Concluido hoje", style = MaterialTheme.typography.labelLarge)
                                Button(onClick = { onUndo(habit.habitId) }) { Text("Desfazer") }
                            }
                        } else if (habit.canComplete && habit.status == HabitOccurrenceStatus.PENDING) {
                            Button(
                                onClick = { onComplete(habit.habitId) },
                                modifier = Modifier.testTag("home_habit_complete_${habit.habitId}"),
                            ) {
                                Text("Concluir")
                            }
                        } else if (habit.weeklyTarget != null && habit.completedThisWeek >= habit.weeklyTarget) {
                            Text("Meta semanal atingida", style = MaterialTheme.typography.labelLarge)
                        } else {
                            Text(
                                text = if (habit.status == HabitOccurrenceStatus.COMPLETED) "Concluído" else "Ignorado",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
