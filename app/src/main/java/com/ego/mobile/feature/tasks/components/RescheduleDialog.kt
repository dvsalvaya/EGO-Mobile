package com.ego.mobile.feature.tasks.components

import androidx.compose.runtime.Composable
import java.time.LocalDate

@Composable
fun RescheduleDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onReschedule: (Long) -> Unit,
) {
    TaskDatePickerDialog(
        initialDate = initialDate,
        dialogTitle = "Reagendar tarefa",
        onDismiss = onDismiss,
        onSelect = { onReschedule(toDueAt(it)) },
    )
}
