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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.feature.common.EmptyState
import java.time.DayOfWeek

@Composable
fun HabitsScreen(viewModel: HabitsViewModel = rememberHabitsViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var frequencyHabit by remember { mutableStateOf<Habit?>(null) }
    var selectedFrequency by remember { mutableStateOf<HabitFrequency?>(null) }
    var archivingHabit by remember { mutableStateOf<Habit?>(null) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                uiState.errorMessage?.let { message ->
                    Text(message, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.error)
                }
                if (uiState.isEmpty && !uiState.isLoading && uiState.errorMessage == null) {
                    EmptyState(
                        title = "Nenhum hábito cadastrado",
                        message = "Crie um hábito para começar a acompanhar sua rotina.",
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        actionLabel = "Adicionar hábito",
                        onActionClick = { isDialogOpen = true },
                    )
                } else {
                    LazyColumn(
                        Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.activeHabits.isNotEmpty()) {
                            item { Text("Ativos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                            items(uiState.activeHabits, key = { it.id }) { habit ->
                                HabitCard(
                                    habit = habit,
                                    progress = uiState.progressByHabitId[habit.id],
                                    onComplete = { viewModel.onQuickComplete(habit.id) },
                                    onUndo = { viewModel.onUndoCompletion(habit.id) },
                                    onPause = { viewModel.onPauseHabit(habit.id) },
                                    onResume = null,
                                    onEdit = { editingHabit = habit },
                                    onChangeFrequency = {
                                        frequencyHabit = habit
                                        selectedFrequency = habit.frequency
                                    },
                                    onArchive = { archivingHabit = habit },
                                )
                            }
                        }
                        if (uiState.pausedHabits.isNotEmpty()) {
                            item { Text("Pausados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                            items(uiState.pausedHabits, key = { it.id }) { habit ->
                                HabitCard(
                                    habit = habit,
                                    progress = uiState.progressByHabitId[habit.id],
                                    onComplete = null,
                                    onUndo = null,
                                    onPause = null,
                                    onResume = { viewModel.onResumeHabit(habit.id) },
                                    onEdit = { editingHabit = habit },
                                    onChangeFrequency = {
                                        frequencyHabit = habit
                                        selectedFrequency = habit.frequency
                                    },
                                    onArchive = { archivingHabit = habit },
                                )
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { editingHabit = null; isDialogOpen = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                    .semantics { contentDescription = "Adicionar hábito" },
            ) { Text("+") }
        }
    }

    if (isDialogOpen || editingHabit != null) {
        val target = editingHabit
        HabitFormDialog(
            initialName = target?.name.orEmpty(),
            initialDescription = target?.description.orEmpty(),
            initialFrequency = target?.frequency ?: HabitFrequency.Daily,
            onDismiss = { isDialogOpen = false; editingHabit = null },
            onSave = { name, description, frequency ->
                if (target == null) viewModel.onCreateHabit(name, description, frequency)
                else viewModel.onUpdateHabitDetails(target.id, name, description)
                isDialogOpen = false
                editingHabit = null
            },
        )
    }

    if (frequencyHabit != null) {
        val habit = frequencyHabit!!
        AlertDialog(
            onDismissRequest = { frequencyHabit = null },
            title = { Text("Alterar frequência") },
            text = {
                Column {
                    Text("As ocorrências anteriores serão preservadas. A nova frequência vale a partir de hoje.")
                    listOf(
                        HabitFrequency.Daily to "Diaria",
                        HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) to "Segunda, quarta e sexta",
                        HabitFrequency.TimesPerWeek(3) to "3 vezes por semana",
                    ).forEach { (frequency, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedFrequency == frequency,
                                onClick = { selectedFrequency = frequency },
                            )
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedFrequency?.let { viewModel.onUpdateFrequency(habit.id, it) }
                    frequencyHabit = null
                }) { Text("Aplicar a partir de hoje") }
            },
            dismissButton = { OutlinedButton(onClick = { frequencyHabit = null }) { Text("Cancelar alteracao") } },
        )
    }

    archivingHabit?.let { habit ->
        AlertDialog(
            onDismissRequest = { archivingHabit = null },
            title = { Text("Arquivar hábito?") },
            text = { Text("O histórico e as ocorrências serão preservados. O hábito deixará de aceitar novas conclusões.") },
            confirmButton = {
                Button(onClick = { viewModel.onArchiveHabit(habit.id); archivingHabit = null }) { Text("Arquivar") }
            },
            dismissButton = { OutlinedButton(onClick = { archivingHabit = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    progress: HabitProgress?,
    onComplete: (() -> Unit)?,
    onUndo: (() -> Unit)?,
    onPause: (() -> Unit)?,
    onResume: (() -> Unit)?,
    onEdit: () -> Unit,
    onChangeFrequency: () -> Unit,
    onArchive: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.status == HabitStatus.PAUSED) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            habit.description?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(formatFrequency(habit.frequency), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            when {
                progress?.weeklyTarget != null -> {
                    Text("Progresso da semana: ${progress.label}", style = MaterialTheme.typography.labelLarge)
                    if (progress.isWeeklyGoalReached) Text("Meta semanal atingida")
                }
                progress?.completedToday == true -> Text("Concluído hoje")
                habit.status == HabitStatus.PAUSED -> Text("Pausado")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onComplete != null && progress?.canCompleteToday != false) {
                    Button(onClick = onComplete) { Text("Concluir") }
                }
                if (onUndo != null && progress?.completedToday == true) {
                    OutlinedButton(onClick = onUndo) { Text("Desfazer") }
                }
                if (onPause != null) OutlinedButton(onClick = onPause) { Text("Pausar") }
                if (onResume != null) OutlinedButton(onClick = onResume) { Text("Retomar") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit) { Text("Editar") }
                OutlinedButton(onClick = onChangeFrequency) { Text("Frequencia") }
                OutlinedButton(onClick = onArchive) { Text("Arquivar") }
            }
        }
    }
}

private fun formatFrequency(frequency: HabitFrequency): String = when (frequency) {
    HabitFrequency.Daily -> "Diária"
    is HabitFrequency.WeeklyDays -> "Dias fixos (${frequency.days.size})"
    is HabitFrequency.TimesPerWeek -> "${frequency.count}x por semana"
}
