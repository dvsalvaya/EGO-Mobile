package com.ego.mobile.feature.routines.workouts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.ego.mobile.domain.model.WorkoutTemplate
import com.ego.mobile.domain.model.WorkoutTemplateExercise
import com.ego.mobile.domain.usecase.GetWorkoutStatisticsUseCase
import com.ego.mobile.feature.common.SectionHeader
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutTemplatesSection(
    templates: List<WorkoutTemplate>,
    hasActiveSession: Boolean,
    onStart: (Long) -> Unit,
    onSave: (WorkoutTemplate) -> Unit,
    onDelete: (Long) -> Unit,
) {
    var editing by remember { mutableStateOf<WorkoutTemplate?>(null) }
    var confirmDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Modelos de treino", action = {
            TextButton(onClick = { editing = WorkoutTemplate(name = "Novo modelo") }) { Text("Criar modelo") }
        })
        if (templates.isEmpty()) {
            Text("Nenhum modelo salvo. Crie um modelo para iniciar com seus exercícios.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else templates.forEach { template ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    Text(template.orderedExercises.joinToString(" • ") { "${it.name} · ${it.setsCount} séries" },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            enabled = !hasActiveSession,
                            onClick = { onStart(template.id) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) {
                            Text(if (hasActiveSession) "Treino ativo" else "Iniciar")
                        }
                        OutlinedButton(
                            onClick = { editing = template },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) { Text("Editar") }
                        TextButton(
                            onClick = { confirmDelete = template },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) { Text("Excluir") }
                    }
                }
            }
        }
    }
    editing?.let { template ->
        TemplateEditorDialog(template = template, onDismiss = { editing = null }) {
            onSave(it)
            editing = null
        }
    }
    confirmDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Excluir modelo?") },
            text = { Text("${template.name} será removido. Sessões já iniciadas permanecem no histórico.") },
            confirmButton = { TextButton(onClick = { onDelete(template.id); confirmDelete = null }) { Text("Excluir") } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TemplateEditorDialog(template: WorkoutTemplate, onDismiss: () -> Unit, onSave: (WorkoutTemplate) -> Unit) {
    var name by remember(template.id) { mutableStateOf(if (template.id == 0L) "" else template.name) }
    val exercises = remember(template.id) { mutableStateListOf<WorkoutTemplateExercise>().apply { addAll(template.orderedExercises) } }
    var exerciseName by remember(template.id) { mutableStateOf("") }
    var sets by remember(template.id) { mutableStateOf("3") }
    var reps by remember(template.id) { mutableStateOf("") }
    var weight by remember(template.id) { mutableStateOf("") }
    var notes by remember(template.id) { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(if (template.id == 0L) "Novo modelo" else "Editar modelo", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it; error = null }, label = { Text("Nome do modelo") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Text("Exercícios", style = MaterialTheme.typography.titleSmall)
                exercises.forEachIndexed { index, exercise ->
                    TextButton(onClick = { exercises.removeAt(index) }) { Text("${exercise.name} · ${exercise.setsCount} séries — remover") }
                }
                OutlinedTextField(exerciseName, { exerciseName = it }, label = { Text("Nome do exercício") }, singleLine = true)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(sets, { sets = it }, label = { Text("Séries") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(reps, { reps = it }, label = { Text("Repetições") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(weight, { weight = it }, label = { Text("Carga kg") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(notes, { notes = it }, label = { Text("Nota") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                TextButton(onClick = {
                    val count = sets.toIntOrNull()
                    val load = weight.replace(',', '.').toDoubleOrNull()
                    if (exerciseName.isBlank() || count == null || count <= 0 || (weight.isNotBlank() && (load == null || load < 0))) {
                        error = "Informe exercício, séries válidas e carga válida."
                    } else {
                        exercises.add(WorkoutTemplateExercise(name = exerciseName.trim(), orderIndex = exercises.size,
                            setsCount = count, reps = reps.ifBlank { null }, weightKg = load, notes = notes.ifBlank { null }))
                        exerciseName = ""; reps = ""; weight = ""; notes = ""; error = null
                    }
                }) { Text("Adicionar exercício") }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    TextButton(onClick = {
                        if (name.isBlank()) error = "Informe o nome do modelo." else onSave(template.copy(name = name.trim(), exercises = exercises.toList()))
                    }) { Text("Salvar") }
                }
            }
    }
}

@Composable
fun WorkoutStatisticsSection(stats: GetWorkoutStatisticsUseCase.Statistics) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Estatísticas")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                val count = stats.totalCompletedWorkouts
                Text("$count ${if (count == 1) "treino concluído" else "treinos concluídos"}", style = MaterialTheme.typography.titleMedium)
                Text("Duração média: ${formatAverageDuration(stats.averageDurationMinutes)}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        stats.exerciseLoads.forEach { exercise ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(exercise.exerciseName, style = MaterialTheme.typography.titleSmall)
                    if (exercise.points.isEmpty()) {
                        Text("Sem cargas comparáveis · ${exercise.sessionCount} sessões no histórico",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Evolução de carga", style = MaterialTheme.typography.bodySmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(exercise.points) { point ->
                                Column {
                                    Text("${formatWeight(point.weightKg)} kg", style = MaterialTheme.typography.labelLarge)
                                    Text(formatLoadDate(point.endedAt), style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatAverageDuration(minutes: Double): String {
    val totalMinutes = minutes.toInt()
    val hours = totalMinutes / 60
    val remainder = totalMinutes % 60
    return if (hours > 0) "$hours h ${remainder} min" else "$remainder min"
}

private fun formatWeight(weight: Double): String = String.format(Locale.getDefault(), "%.1f", weight).removeSuffix(",0").removeSuffix(".0")

private fun formatLoadDate(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis)
    .atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault()))
