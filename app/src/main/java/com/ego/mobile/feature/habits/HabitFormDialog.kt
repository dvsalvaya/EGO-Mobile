package com.ego.mobile.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HabitFrequency
import java.time.DayOfWeek

private val week = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
)
private val initials = listOf("S", "T", "Q", "Q", "S", "S", "D")
private val fullDays = listOf("segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado", "domingo")

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HabitFormDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialFrequency: HabitFrequency = HabitFrequency.Daily,
    title: String = if (initialName.isBlank()) "Novo hábito" else "Editar hábito",
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, frequency: HabitFrequency) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var nameError by remember(initialName) { mutableStateOf<String?>(null) }
    var descriptionError by remember(initialDescription) { mutableStateOf<String?>(null) }
    var mode by remember(initialFrequency) { mutableStateOf(initialFrequency.mode()) }
    var count by remember(initialFrequency) { mutableStateOf((initialFrequency as? HabitFrequency.TimesPerWeek)?.count ?: 3) }
    var days by remember(initialFrequency) { mutableStateOf(initialFrequency.days()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = null }, label = { Text("Nome") },
                isError = nameError != null, supportingText = { Text(nameError ?: "${name.length}/80") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it; descriptionError = null },
                label = { Text("Descrição (opcional)") }, isError = descriptionError != null,
                supportingText = { Text(descriptionError ?: "${description.length}/250") }, modifier = Modifier.fillMaxWidth(),
            )
            Text("Frequência", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Todos os dias", "Dias da semana", "Vezes por semana").forEachIndexed { index, label ->
                    FilterChip(selected = mode == index, onClick = { mode = index }, label = { Text(label) })
                }
            }
            if (mode == 1) {
                Text("Dias escolhidos", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEachIndexed { index, day ->
                        FilterChip(
                            selected = day in days,
                            onClick = { days = if (day in days) days - day else days + day },
                            label = { Text(initials[index]) },
                            modifier = Modifier.semantics { contentDescription = "${fullDays[index]}${if (day in days) ", selecionado" else ", não selecionado"}" },
                        )
                    }
                }
            }
            if (mode == 2) {
                Text("Meta semanal: $count vezes", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(onClick = { count = (count - 1).coerceAtLeast(1) }) { Text("−") }
                    (1..7).forEach { value ->
                        FilterChip(selected = count == value, onClick = { count = value }, label = { Text(value.toString()) })
                    }
                }
                Text("Dias preferidos (opcional)", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEachIndexed { index, day ->
                        FilterChip(
                            selected = day in days,
                            onClick = { days = if (day in days) days - day else days + day },
                            label = { Text(initials[index]) },
                            modifier = Modifier.semantics { contentDescription = "${fullDays[index]}${if (day in days) ", selecionado" else ", não selecionado"}" },
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = {
                    val nextNameError = when { name.trim().isEmpty() -> "Informe um nome."; name.trim().length > 80 -> "Use no máximo 80 caracteres."; else -> null }
                    val nextDescriptionError = if (description.length > 250) "Use no máximo 250 caracteres." else null
                    val nextFrequencyError = if (mode == 1 && days.isEmpty()) "Escolha pelo menos um dia da semana." else null
                    nameError = nextNameError ?: nextFrequencyError
                    descriptionError = nextDescriptionError
                    if (nextNameError == null && nextDescriptionError == null && nextFrequencyError == null) {
                        val frequency = when (mode) {
                            1 -> HabitFrequency.WeeklyDays(days)
                            2 -> HabitFrequency.TimesPerWeek(count, days)
                            else -> HabitFrequency.Daily
                        }
                        onSave(name.trim(), description.trim().ifEmpty { null }, frequency)
                    }
                }, modifier = Modifier.weight(1f)) { Text("Salvar") }
            }
        }
    }
}

private fun HabitFrequency.mode(): Int = when (this) {
    HabitFrequency.Daily -> 0
    is HabitFrequency.WeeklyDays -> 1
    is HabitFrequency.TimesPerWeek -> 2
}

private fun HabitFrequency.days(): Set<DayOfWeek> = when (this) {
    HabitFrequency.Daily -> emptySet()
    is HabitFrequency.WeeklyDays -> days
    is HabitFrequency.TimesPerWeek -> preferredDays
}
