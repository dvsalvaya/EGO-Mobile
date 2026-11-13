package com.ego.mobile.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HabitFrequency
import java.time.DayOfWeek

@Composable
fun HabitFormDialog(
    initialName: String = "",
    initialDescription: String = "",
    initialFrequency: HabitFrequency = HabitFrequency.Daily,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, frequency: HabitFrequency) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var selectedFrequency by remember(initialFrequency) { mutableStateOf(initialFrequency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Novo hábito") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Frequência", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FrequencyChoiceChip(
                            label = "Diária",
                            selected = selectedFrequency is HabitFrequency.Daily,
                            onClick = { selectedFrequency = HabitFrequency.Daily },
                        )
                        FrequencyChoiceChip(
                            label = "Semanal",
                            selected = selectedFrequency is HabitFrequency.WeeklyDays,
                            onClick = { selectedFrequency = HabitFrequency.WeeklyDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)) },
                        )
                        FrequencyChoiceChip(
                            label = "3x/sem",
                            selected = selectedFrequency is HabitFrequency.TimesPerWeek,
                            onClick = { selectedFrequency = HabitFrequency.TimesPerWeek(3) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), description.trim().ifEmpty { null }, selectedFrequency)
                    }
                },
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun FrequencyChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        content = { Text(label) },
    )
}
