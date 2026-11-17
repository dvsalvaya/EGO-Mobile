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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitStatus

private sealed interface HabitDialog {
    data object Create : HabitDialog
    data class Edit(val habit: Habit) : HabitDialog
    data class ChangeFrequency(val habit: Habit) : HabitDialog
}

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = rememberHabitsViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<HabitDialog?>(null) }
    var pendingArchive by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearOperationMessage()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.errorMessage != null && uiState.isEmpty -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(onClick = viewModel::clearError) { Text("Tentar novamente") }
                    }
                }
                uiState.isEmpty && !uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Nenhum hábito cadastrado",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Crie um hábito para começar a acompanhar sua rotina.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { dialog = HabitDialog.Create }) { Text("Adicionar hábito") }
                    }
                }
                else -> {
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
                                item { SectionTitle("Ativos") }
                                items(uiState.activeHabits) { habit ->
                                    HabitCard(
                                        habit = habit,
                                        progress = uiState.progressByHabit[habit.id],
                                        onQuickComplete = { viewModel.onQuickComplete(habit.id) },
                                        onUndo = { viewModel.onUndoCompletion(habit.id) },
                                        onPause = { viewModel.onPauseHabit(habit.id) },
                                        onResume = null,
                                        onEdit = { dialog = HabitDialog.Edit(habit) },
                                        onChangeFrequency = { dialog = HabitDialog.ChangeFrequency(habit) },
                                        onArchive = { pendingArchive = habit },
                                    )
                                }
                            }
                            if (uiState.pausedHabits.isNotEmpty()) {
                                item { SectionTitle("Pausados") }
                                items(uiState.pausedHabits) { habit ->
                                    HabitCard(
                                        habit = habit,
                                        progress = uiState.progressByHabit[habit.id],
                                        onQuickComplete = null,
                                        onUndo = null,
                                        onPause = null,
                                        onResume = { viewModel.onResumeHabit(habit.id) },
                                        onEdit = { dialog = HabitDialog.Edit(habit) },
                                        onChangeFrequency = { dialog = HabitDialog.ChangeFrequency(habit) },
                                        onArchive = { pendingArchive = habit },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { dialog = HabitDialog.Create },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .semantics { contentDescription = "Adicionar hábito" },
            ) { Text("+") }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    when (val current = dialog) {
        is HabitDialog.Create -> HabitFormDialog(
            onDismiss = { dialog = null },
            onSave = { name, description, frequency ->
                viewModel.onCreateHabit(name, description, frequency)
                dialog = null
            },
        )
        is HabitDialog.Edit -> HabitFormDialog(
            initialName = current.habit.name,
            initialDescription = current.habit.description.orEmpty(),
            initialFrequency = current.habit.frequency,
            isEdit = true,
            onDismiss = { dialog = null },
            onSave = { name, description, _ ->
                viewModel.onEditHabit(current.habit.id, name, description)
                dialog = null
            },
        )
        is HabitDialog.ChangeFrequency -> FrequencyChangeDialog(
            habit = current.habit,
            onDismiss = { dialog = null },
            onConfirm = { frequency ->
                viewModel.onUpdateFrequency(current.habit.id, frequency)
                dialog = null
            },
        )
        null -> Unit
    }

    pendingArchive?.let { habit ->
        AlertDialog(
            onDismissRequest = { pendingArchive = null },
            title = { Text("Arquivar hábito") },
            text = { Text("O hábito sairá das listas, mas seu histórico será preservado.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onArchiveHabit(habit.id)
                    pendingArchive = null
                }) { Text("Arquivar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingArchive = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun FrequencyChangeDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (HabitFrequency) -> Unit,
) {
    var selected by remember(habit.frequency) { mutableStateOf<HabitFrequency>(habit.frequency) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alterar frequência") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("As ocorrências anteriores serão preservadas; a nova frequência vale a partir de hoje.")
                FrequencyChoice(habit = habit, selected = selected, onSelect = { selected = it })
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selected) }) { Text("Confirmar") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun FrequencyChoice(
    habit: Habit,
    selected: HabitFrequency,
    onSelect: (HabitFrequency) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(
            "Diária" to HabitFrequency.Daily,
            "3x/sem" to HabitFrequency.TimesPerWeek(3),
            "5x/sem" to HabitFrequency.TimesPerWeek(5),
        ).forEach { (label, frequency) ->
            OutlinedButton(
                onClick = { onSelect(frequency) },
                modifier = Modifier.semantics { contentDescription = "Frequência $label para ${habit.name}" },
            ) { Text(text = label, maxLines = 1, softWrap = false) }
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    progress: HabitProgress?,
    onQuickComplete: (() -> Unit)?,
    onUndo: (() -> Unit)?,
    onPause: (() -> Unit)?,
    onResume: (() -> Unit)?,
    onEdit: () -> Unit,
    onChangeFrequency: () -> Unit,
    onArchive: () -> Unit,
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (!habit.description.isNullOrBlank()) {
                        Text(
                            habit.description,
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
                            "Pausado",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = listOfNotNull(formatFrequency(habit.frequency), progress?.label).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onQuickComplete != null && progress?.canComplete != false) {
                    Button(
                        onClick = onQuickComplete,
                        modifier = Modifier.semantics { contentDescription = "Concluir hábito: ${habit.name}" },
                    ) { Text("Concluir") }
                }
                if (onUndo != null && progress?.isCompletedToday == true) {
                    OutlinedButton(onClick = onUndo) { Text("Desfazer") }
                }
                TextButton(onClick = onEdit) { Text("Editar") }
                TextButton(onClick = onChangeFrequency) { Text("Frequência") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onPause != null) {
                    OutlinedButton(onClick = onPause) { Text("Pausar") }
                }
                if (onResume != null) {
                    OutlinedButton(onClick = onResume) { Text("Retomar") }
                }
                TextButton(onClick = onArchive) { Text("Arquivar") }
            }
        }
    }
}

private fun formatFrequency(frequency: HabitFrequency): String = when (frequency) {
    HabitFrequency.Daily -> "Diária"
    is HabitFrequency.WeeklyDays -> "Dias fixos (${frequency.days.size})"
    is HabitFrequency.TimesPerWeek -> "${frequency.count}x por semana"
}
