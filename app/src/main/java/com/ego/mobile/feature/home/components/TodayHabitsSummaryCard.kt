package com.ego.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
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
import com.ego.mobile.domain.model.HabitOccurrenceStatus
import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.feature.common.SectionHeader

@Composable
fun TodayHabitsSummaryCard(
    habits: List<HomeSummary.TodayHabit>,
    onComplete: (Long) -> Unit,
    onUndo: (Long) -> Unit = {},
    totalCount: Int = habits.size,
    onOpenAll: () -> Unit = {},
    onHabitSelected: (HomeSummary.TodayHabit) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val emptyCardHeight = if (LocalDensity.current.fontScale >= 1.25f) 300.dp else 230.dp
    Card(modifier = modifier.fillMaxWidth().then(if (habits.isEmpty()) Modifier.height(emptyCardHeight) else Modifier).testTag("home_today_habits_card")) {
        if (habits.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HabitsSummaryHeader(totalCount, onOpenAll)
                    CompactHabitsEmptyState()
                }
            }
        } else {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HabitsSummaryHeader(totalCount, onOpenAll)
                habits.forEach { habit ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onHabitSelected(habit) },
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

@Composable
private fun HabitsSummaryHeader(totalCount: Int, onOpenAll: () -> Unit) {
    Row(Modifier.fillMaxWidth().requiredHeightIn(max = 80.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionHeader(title = "Para hoje · Hábitos ($totalCount)", modifier = Modifier.weight(1f))
        androidx.compose.material3.TextButton(onClick = onOpenAll) { Text("Ver tudo") }
    }
}

@Composable
private fun CompactHabitsEmptyState() {
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
            Text("Quando houver hábitos previstos para hoje, eles aparecerão aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
