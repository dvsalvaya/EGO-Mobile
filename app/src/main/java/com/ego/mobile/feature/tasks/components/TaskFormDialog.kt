package com.ego.mobile.feature.tasks.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.Task
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String?, dueAt: Long?) -> Unit,
) {
    var title by remember(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by remember(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var dueAt by remember(task?.id) { mutableLongStateOf(task?.dueAt ?: 0L) }
    var hasDueDate by remember(task?.id) { mutableStateOf(task?.dueAt != null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    val descriptionError = description.length > 300

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(if (task == null) "Nova tarefa" else "Editar tarefa", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(120); titleError = false },
                    label = { Text("Título") },
                    singleLine = true,
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Informe um título de 1 a 100 caracteres.") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(301) },
                    label = { Text("Descrição (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    isError = descriptionError,
                    supportingText = if (descriptionError) {
                        { Text("A descrição deve ter até 300 caracteres.") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (hasDueDate) {
                            "Prazo: ${formatDate(dueAt)}"
                        } else {
                            "Adicionar prazo (opcional)"
                        },
                    )
                }
            if (hasDueDate) {
                TextButton(
                        onClick = { hasDueDate = false },
                        modifier = Modifier.align(androidx.compose.ui.Alignment.End),
                ) {
                    Text("Remover prazo")
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(
                    onClick = {
                        titleError = title.trim().length !in 1..100
                        if (!titleError && !descriptionError) {
                            onSave(title.trim(), description.trim().takeIf(String::isNotEmpty), dueAt.takeIf { hasDueDate })
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Salvar") }
            }
        }
    }

    if (showDatePicker) {
        TaskDatePickerDialog(
            initialDate = if (hasDueDate) toLocalDate(dueAt) else null,
            onDismiss = { showDatePicker = false },
            onSelect = {
                dueAt = toDueAt(it)
                hasDueDate = true
                showDatePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskDatePickerDialog(
    initialDate: LocalDate?,
    dialogTitle: String = "Selecionar prazo",
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onSelect(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = state.selectedDateMillis != null,
            ) {
                Text("Selecionar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    ) {
        Column {
            Text(dialogTitle, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            DatePicker(state = state)
        }
    }
}

internal fun toDueAt(date: LocalDate): Long = date
    .atStartOfDay(ZoneId.of("America/Sao_Paulo"))
    .toInstant()
    .toEpochMilli()

internal fun toLocalDate(epochMillis: Long): LocalDate =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate()

private fun formatDate(epochMillis: Long): String =
    DATE_FORMATTER.format(toLocalDate(epochMillis))

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
