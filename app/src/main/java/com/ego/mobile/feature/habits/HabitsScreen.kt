package com.ego.mobile.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitProgress
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.feature.common.EmptyState
import java.time.Instant
import java.time.ZoneOffset

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitsScreen(viewModel: HabitsViewModel = rememberHabitsViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var archivingHabit by remember { mutableStateOf<Habit?>(null) }
    var pendingFrequencyChange by remember { mutableStateOf<PendingHabitEdit?>(null) }
    var detailHabit by remember { mutableStateOf<Habit?>(null) }
    var dateHabit by remember { mutableStateOf<Habit?>(null) }
    val datePickerState = androidx.compose.material3.rememberDatePickerState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hábitos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Button(onClick = { editingHabit = null; isDialogOpen = true }) { Text("Novo hábito") }
                }
                uiState.errorMessage?.let { message ->
                    Text(message, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.error)
                }
                if (uiState.isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.isEmpty && uiState.errorMessage == null) {
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
                        if (uiState.activeHabits.isEmpty() && uiState.pausedHabits.isNotEmpty()) {
                            item {
                                EmptyState(
                                    title = "Nenhum hábito ativo",
                                    message = "Retome um hábito pausado para voltar a acompanhar seu progresso.",
                                )
                            }
                        }
                        if (uiState.activeHabits.isNotEmpty()) {
                            item { Text("Ativos (${uiState.activeHabits.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                            items(uiState.activeHabits, key = { it.id }) { habit ->
                                HabitCard(
                                    habit = habit,
                                    progress = uiState.progressByHabitId[habit.id],
                                    onComplete = { viewModel.onQuickComplete(habit.id) },
                                    onUndo = { viewModel.onUndoCompletion(habit.id) },
                                    onPause = { viewModel.onPauseHabit(habit.id) },
                                    onResume = null,
                                    onEdit = { editingHabit = habit },
                                    onDetails = { detailHabit = habit },
                                    onChangeFrequency = { editingHabit = habit },
                                    onArchive = { archivingHabit = habit },
                                )
                            }
                        }
                        if (uiState.pausedHabits.isNotEmpty()) {
                            item { Text("Pausados (${uiState.pausedHabits.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                            items(uiState.pausedHabits, key = { it.id }) { habit ->
                                HabitCard(
                                    habit = habit,
                                    progress = uiState.progressByHabitId[habit.id],
                                    onComplete = null,
                                    onUndo = null,
                                    onPause = null,
                                    onResume = { viewModel.onResumeHabit(habit.id) },
                                    onEdit = { editingHabit = habit },
                                    onDetails = { detailHabit = habit },
                                    onChangeFrequency = { editingHabit = habit },
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
                if (target == null) {
                    viewModel.onCreateHabit(name, description, frequency)
                } else if (frequency != target.frequency) {
                    pendingFrequencyChange = PendingHabitEdit(target, name, description, frequency)
                } else {
                    viewModel.onUpdateHabitDetails(target.id, name, description)
                }
                isDialogOpen = false
                editingHabit = null
            },
        )
    }

    pendingFrequencyChange?.let { edit ->
        AlertDialog(
            onDismissRequest = { pendingFrequencyChange = null },
            title = { Text("Alterar plano do hábito?") },
            text = { Text("O novo plano vale a partir de hoje. O histórico e as ocorrências anteriores serão preservados.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onUpdateHabitDetails(edit.habit.id, edit.name, edit.description, edit.frequency)
                    pendingFrequencyChange = null
                }) { Text("Aplicar a partir de hoje") }
            },
            dismissButton = { OutlinedButton(onClick = { pendingFrequencyChange = null }) { Text("Cancelar") } },
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

    detailHabit?.let { habit ->
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { detailHabit = null }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(habit.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                habit.description?.takeIf(String::isNotBlank)?.let { Text(it) }
                Text(formatFrequency(habit.frequency), color = MaterialTheme.colorScheme.onSurfaceVariant)
                uiState.progressByHabitId[habit.id]?.let { progress ->
                    Text(progress.label ?: if (progress.completedToday) "Concluído hoje" else "Sem conclusão hoje", style = MaterialTheme.typography.titleMedium)
                    if (progress.isWeeklyGoalReached) Text("Meta semanal atingida")
                }
                if (uiState.progressByHabitId[habit.id]?.completedToday == true) {
                    OutlinedButton(onClick = { viewModel.onUndoCompletion(habit.id) }) { Text("Desfazer conclusão de hoje") }
                }
                Button(onClick = { detailHabit = null; editingHabit = habit }, modifier = Modifier.fillMaxWidth()) { Text("Editar") }
                OutlinedButton(onClick = {
                    if (habit.status == HabitStatus.ACTIVE) viewModel.onPauseHabit(habit.id) else viewModel.onResumeHabit(habit.id)
                    detailHabit = null
                }, modifier = Modifier.fillMaxWidth()) { Text(if (habit.status == HabitStatus.ACTIVE) "Pausar" else "Retomar") }
                OutlinedButton(onClick = { detailHabit = null; dateHabit = habit }, modifier = Modifier.fillMaxWidth()) { Text("Registrar conclusão anterior") }
                OutlinedButton(onClick = { detailHabit = null; archivingHabit = habit }, modifier = Modifier.fillMaxWidth()) { Text("Arquivar") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
    dateHabit?.let { habit ->
        DatePickerDialog(
            onDismissRequest = { dateHabit = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.onCompleteAt(habit.id, Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    dateHabit = null
                }) { Text("Registrar") }
            },
            dismissButton = { TextButton(onClick = { dateHabit = null }) { Text("Cancelar") } },
        ) { DatePicker(state = datePickerState) }
    }
}

private data class PendingHabitEdit(
    val habit: Habit,
    val name: String,
    val description: String?,
    val frequency: HabitFrequency,
)

@Composable
private fun HabitCard(
    habit: Habit,
    progress: HabitProgress?,
    onComplete: (() -> Unit)?,
    onUndo: (() -> Unit)?,
    onPause: (() -> Unit)?,
    onResume: (() -> Unit)?,
    onEdit: () -> Unit,
    onDetails: () -> Unit,
    onChangeFrequency: () -> Unit,
    onArchive: () -> Unit,
) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onDetails),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.status == HabitStatus.PAUSED) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onDetails))
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
                OutlinedButton(onClick = onChangeFrequency) { Text("Frequência") }
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
