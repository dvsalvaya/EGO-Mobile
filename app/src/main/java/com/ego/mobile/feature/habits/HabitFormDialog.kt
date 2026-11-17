package com.ego.mobile.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, frequency: HabitFrequency) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var selectedFrequency by remember(initialFrequency) { mutableStateOf(initialFrequency) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        val trimmed = name.trim()
        nameError = when {
            trimmed.isEmpty() -> "Informe o nome do hábito."
            trimmed.length > 80 -> "O nome deve ter no máximo 80 caracteres."
            else -> null
        }
        descriptionError = if (description.length > 250) {
            "A descrição deve ter no máximo 250 caracteres."
        } else {
            null
        }
        return nameError == null && descriptionError == null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isEdit) "Editar hábito" else "Novo hábito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("Nome") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= 250) {
                            description = it
                            descriptionError = null
                        } else {
                            descriptionError = "A descrição deve ter no máximo 250 caracteres."
                        }
                    },
                    label = { Text("Descrição (opcional)") },
                    maxLines = 3,
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Frequência",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FrequencyChoiceChip(
                            label = "Diária",
                            selected = selectedFrequency is HabitFrequency.Daily,
                            onClick = { selectedFrequency = HabitFrequency.Daily },
                            modifier = Modifier.weight(1f),
                        )
                        FrequencyChoiceChip(
                            label = "Semanal",
                            selected = selectedFrequency is HabitFrequency.WeeklyDays,
                            onClick = {
                                selectedFrequency = HabitFrequency.WeeklyDays(
                                    setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                        FrequencyChoiceChip(
                            label = "3x/sem",
                            selected = selectedFrequency is HabitFrequency.TimesPerWeek,
                            onClick = { selectedFrequency = HabitFrequency.TimesPerWeek(3) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
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
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, maxLines = 1, softWrap = false) },
        modifier = modifier,
    )
}
