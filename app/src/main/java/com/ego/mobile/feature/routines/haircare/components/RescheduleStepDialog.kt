package com.ego.mobile.feature.routines.haircare.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleStepDialog(
    initialDate: String,
    today: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialMillis = remember(initialDate) {
        LocalDate.parse(initialDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = FutureSelectableDates(today),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                    val date = Instant.ofEpochMilli(selectedMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()
                    onConfirm(date.toString())
                },
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class FutureSelectableDates(
    private val today: LocalDate,
) : androidx.compose.material3.SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
        return date.isAfter(today)
    }
}
