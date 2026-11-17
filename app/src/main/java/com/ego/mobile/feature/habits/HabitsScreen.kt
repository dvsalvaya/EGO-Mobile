package com.ego.mobile.feature.habits

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ego.mobile.app.theme.Colors
import com.ego.mobile.app.theme.Spacing
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
        color = Colors.background,
    ) { Box(modifier = Modifier.fillMaxSize()) {
        // Vazio: ícone discreto, explicação e ação "Criar hábito"
        if (uiState.isEmpty && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Nenhum hábito cadastrado",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Colors.onBackground,
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = "Crie um hábito para começar a acompanhar sua rotina.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Colors.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                Button(onClick = { isDialogOpen = true }) {
                    Text("Adicionar hábito")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Seções Ativos e Pausados - listas compactas
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier
                            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        color = Colors.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                if (uiState.activeHabits.isNotEmpty()) {
                    // Seção: Ativos
                    Text(
                        text = "Ativos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                        color = Colors.onSurface,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        items(uiState.activeHabits) { habit ->
                            HabitCard(
                                habit = habit,
                                onQuickComplete = { viewModel.onQuickComplete(habit.id) },
                                onPause = { viewModel.onPauseHabit(habit.id) },
                                onResume = { viewModel.onResumeHabit(habit.id) },
                            )
                        }
                    }
                }

                if (uiState.pausedHabits.isNotEmpty()) {
                    // Seção: Pausados
                    Text(
                        text = "Pausados",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                        color = Colors.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
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

        // Floating Action Button para criar novo hábito
        FloatingActionButton(
            onClick = { isDialogOpen = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg)
                .semantics { contentDescription = "Adicionar hábito" },
        ) {
            Text("+")
        }
    } }

    if (isDialogOpen) {
        // Bottom Sheet para criação/edição de hábito
        // Usar Bottom Sheet conforme padrão Spec 9
        androidx.compose.material3.BottomSheetScaffold(
            scaffoldState = androidx.compose.material3 rememberBottomSheetScaffold(),
        ) { sheetContent ->
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Colors.surface,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    HabitFormDialog(
                        onDismiss = { isDialogOpen = false },
                        onSave = { name, description, frequency ->
                            viewModel.onCreateHabit(name, description, frequency)
                            isDialogOpen = false
                        },
                    )
                }
            }
        } { sheetContent ->
            // Conteúdo do Bottom Sheet será substituído
        }
    }
}

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
                Colors.surfaceVariant
            } else {
                Colors.surface
            },
        ),
        shape = RoundedCornerShape(Spacing.control),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
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
                        fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                    )
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Colors.onSurfaceVariant,
                        )
                    }
                }

                if (habit.status == HabitStatus.PAUSED) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Colors.errorContainer,
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = Spacing.xs, vertical = Spacing.sm),
                    ) {
                        Text(
                            text = "Pausado",
                            style = MaterialTheme.typography.labelMedium,
                            color = Colors.onErrorContainer,
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
                    color = Colors.onSurfaceVariant,
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