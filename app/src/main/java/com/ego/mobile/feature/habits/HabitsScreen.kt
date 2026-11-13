package com.ego.mobile.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = rememberHabitsViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.errorMessage != null && uiState.isEmpty) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            } else if (uiState.isEmpty && !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Nenhum hábito cadastrado",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Crie um hábito para começar a acompanhar sua rotina.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { isDialogOpen = true }) {
                        Text("Adicionar hábito")
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.activeHabits.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Ativos",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            items(uiState.activeHabits) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onQuickComplete = { viewModel.onQuickComplete(habit.id) },
                                    onPause = { viewModel.onPauseHabit(habit.id) },
                                    onResume = { viewModel.onResumeHabit(habit.id) },
                                )
                            }
                        }

                        if (uiState.pausedHabits.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Pausados",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            items(uiState.pausedHabits) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onQuickComplete = null,
                                    onPause = null,
                                    onResume = { viewModel.onResumeHabit(habit.id) },
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { isDialogOpen = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .semantics { contentDescription = "Adicionar hábito" },
            ) {
                Text("+")
            }
        }
    }

    if (isDialogOpen) {
        HabitFormDialog(
            onDismiss = { isDialogOpen = false },
            onSave = { name, description, frequency ->
                viewModel.onCreateHabit(name, description, frequency)
                isDialogOpen = false
            },
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onQuickComplete: (() -> Unit)?,
    onPause: (() -> Unit)?,
    onResume: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.status == HabitStatus.PAUSED) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (habit.status == HabitStatus.PAUSED) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "Pausado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatFrequency(habit.frequency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (habit.status == HabitStatus.ACTIVE && onQuickComplete != null) {
                    Button(
                        onClick = onQuickComplete,
                        modifier = Modifier.semantics {
                            contentDescription = "Concluir hábito: ${habit.name}"
                        },
                    ) {
                        Text("✓")
                    }
                } else if (habit.status == HabitStatus.PAUSED && onResume != null) {
                    OutlinedButton(onClick = onResume) {
                        Text("Retomar")
                    }
                } else if (habit.status == HabitStatus.ACTIVE && onPause != null) {
                    OutlinedButton(onClick = onPause) {
                        Text("Pausar")
                    }
                }
            }
        }
    }
}

private fun formatFrequency(frequency: HabitFrequency): String = when (frequency) {
    HabitFrequency.Daily -> "Diária"
    is HabitFrequency.WeeklyDays -> "Dias fixos (${frequency.days.size})"
    is HabitFrequency.TimesPerWeek -> "${frequency.count}x por semana"
}
