package com.ego.mobile.feature.routines.workouts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.feature.common.EmptyState
import com.ego.mobile.domain.model.WorkoutExercise
import com.ego.mobile.domain.model.WorkoutSession
import com.ego.mobile.domain.model.WorkoutSessionStatus

@Composable
fun ActiveWorkoutView(
    session: WorkoutSession,
    elapsedTimeMillis: Long,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onComplete: () -> Unit,
    onDiscard: () -> Unit,
    onAddExerciseClick: () -> Unit,
    onRemoveExercise: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showCompleteConfirm by remember { mutableStateOf(false) }

    val isPaused = session.status == WorkoutSessionStatus.PAUSED

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Cabeçalho da Sessão Ativa
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (isPaused) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = if (isPaused) "PAUSADO" else "EM EXECUÇÃO",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tempo decorrido: ${formatDuration(elapsedTimeMillis)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Botões de Ação da Sessão
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isPaused) {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Retomar")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onPause,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Pausar")
                        }
                    }

                    Button(
                        onClick = { showCompleteConfirm = true },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Finalizar")
                    }

                    OutlinedButton(
                        onClick = { showDiscardConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Descartar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Exercícios
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Exercícios (${session.exercises.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Button(onClick = onAddExerciseClick) {
                Text("+ Adicionar")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (session.exercises.isEmpty()) {
            EmptyState(
                title = "Nenhum exercício registrado",
                message = "Adicione um exercício para registrar suas séries e cargas.",
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                actionLabel = "Adicionar exercício",
                onActionClick = onAddExerciseClick,
                isCard = true,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(session.exercises, key = { it.id }) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onDelete = { onRemoveExercise(exercise.id) },
                    )
                }
            }
        }
    }

    if (showCompleteConfirm) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirm = false },
            title = { Text("Finalizar Treino?") },
            text = { Text("Deseja concluir esta sessão de treino e salvá-la no seu histórico?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteConfirm = false
                        onComplete()
                    },
                ) {
                    Text("Sim, Finalizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirm = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Descartar Treino?") },
            text = { Text("Tem certeza de que deseja descartar este treino? Os registros não serão salvos no histórico.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        onDiscard()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Voltar")
                }
            },
        )
    }
}

@Composable
private fun ExerciseItem(
    exercise: WorkoutExercise,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                val details = buildString {
                    append("${exercise.setsCount} séries")
                    if (!exercise.reps.isNullOrBlank()) {
                        append(" × ${exercise.reps} reps")
                    }
                    if (exercise.weightKg != null && exercise.weightKg > 0) {
                        append(" • ${exercise.weightKg} kg")
                    }
                }

                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!exercise.notes.isNullOrBlank()) {
                    Text(
                        text = exercise.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Remover")
            }
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
