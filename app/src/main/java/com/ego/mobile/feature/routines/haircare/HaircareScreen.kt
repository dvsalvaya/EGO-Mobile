package com.ego.mobile.feature.routines.haircare

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ego.mobile.domain.model.HaircareOccurrence
import com.ego.mobile.domain.model.HaircareOccurrenceStatus
import com.ego.mobile.domain.model.HaircarePresetStep
import com.ego.mobile.domain.model.HaircareStepType
import com.ego.mobile.feature.routines.haircare.components.CurrentStepCard
import com.ego.mobile.feature.routines.haircare.components.HaircareHistoryList
import com.ego.mobile.feature.routines.haircare.components.RescheduleStepDialog
import com.ego.mobile.feature.routines.haircare.components.HaircareCalendar
import com.ego.mobile.feature.common.EmptyState as CommonEmptyState
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HaircareScreen(
    viewModel: HaircareViewModel = rememberHaircareViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var reschedulingStepId by remember { mutableStateOf<Long?>(null) }
    var reschedulingDate by remember { mutableStateOf<String?>(null) }
    var showCustomRoutineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HaircareEvent.RoutineCreated -> snackbarHostState.showSnackbar("Rotina salva.")
            }
        }
    }
    LaunchedEffect(uiState) {
        val message = when (val state = uiState) {
            is HaircareUiState.ActiveRoutine -> state.errorMessage
            is HaircareUiState.NoRoutineConfigured -> state.errorMessage
            is HaircareUiState.Error -> state.message
            HaircareUiState.Loading -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                HaircareUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HaircareUiState.Error -> Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error,
                )
                is HaircareUiState.NoRoutineConfigured -> NoRoutineScreen(
                    onAdoptPreset = { viewModel.adoptStandardPreset() },
                    onCreateCustom = { showCustomRoutineDialog = true },
                )
                is HaircareUiState.ActiveRoutine -> ActiveRoutineScreen(
                    state = state,
                    onRecordExecution = { id -> viewModel.recordExecution(id) },
                    onReschedule = { step ->
                        reschedulingStepId = step.id
                        reschedulingDate = step.scheduledDate
                    },
                    onPreviousMonth = viewModel::showPreviousMonth,
                    onNextMonth = viewModel::showNextMonth,
                    onSelectDate = viewModel::selectDate,
                )
            }
        }
    }

    if (showCustomRoutineDialog) {
        CustomRoutineDialog(
            onDismiss = { showCustomRoutineDialog = false },
            onCreate = { name, steps ->
                showCustomRoutineDialog = false
                viewModel.createCustomRoutine(name, steps)
            },
        )
    }

    val stepId = reschedulingStepId
    val initialDate = reschedulingDate
    if (stepId != null && initialDate != null) {
        RescheduleStepDialog(
            initialDate = initialDate,
            today = LocalDate.now(ZoneId.of("America/Sao_Paulo")),
            onDismiss = {
                reschedulingStepId = null
                reschedulingDate = null
            },
            onConfirm = { date ->
                viewModel.rescheduleStep(stepId, date)
                reschedulingStepId = null
                reschedulingDate = null
            },
        )
    }
}

@Composable
private fun NoRoutineScreen(
    onAdoptPreset: () -> Unit,
    onCreateCustom: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CommonEmptyState(
            title = "Cuidados capilares",
            message = "Monte uma rotina flexível e registre cada etapa quando realmente acontecer.",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onAdoptPreset, modifier = Modifier.fillMaxWidth()) {
            Text("Começar com Cronograma Capilar Básico")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onCreateCustom, modifier = Modifier.fillMaxWidth()) {
            Text("Criar rotina personalizada")
        }
    }
}

@Composable
private fun ActiveRoutineScreen(
    state: HaircareUiState.ActiveRoutine,
    onRecordExecution: (Long) -> Unit,
    onReschedule: (HaircareOccurrence) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .testTag("haircare_screen_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    text = state.routine.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                state.routine.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Text("Calendário capilar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            HaircareCalendar(
                month = state.selectedMonth,
                selectedDate = state.selectedDate,
                occurrences = state.calendarOccurrences,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onSelectDate = onSelectDate,
            )
        }
        item {
            Text("Etapas de ${state.selectedDate.dayOfMonth}/${state.selectedDate.monthValue}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
        val selectedDayOccurrences = state.calendarOccurrences.filter { it.scheduledDate == state.selectedDate.toString() }
        if (selectedDayOccurrences.isEmpty()) {
            item { Text("Nenhuma etapa neste dia.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(selectedDayOccurrences, key = { "calendar-${it.id}" }) { occurrence ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(occurrence.stepTitle, style = MaterialTheme.typography.bodyMedium)
                        Text("${occurrence.stepType.displayName} · ${if (occurrence.status == HaircareOccurrenceStatus.COMPLETED) "Concluída" else if (occurrence.status == HaircareOccurrenceStatus.RESCHEDULED) "Reagendada" else "Pendente"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (occurrence.status == HaircareOccurrenceStatus.PENDING || occurrence.status == HaircareOccurrenceStatus.RESCHEDULED) {
                        Column(horizontalAlignment = Alignment.End) {
                            TextButton(onClick = { onRecordExecution(occurrence.id) }) { Text("Registrar execução") }
                            TextButton(onClick = { onReschedule(occurrence) }) { Text("Reagendar") }
                        }
                    }
                }
            }
        }
        item {
            val currentStep = state.currentStep
            if (currentStep == null) {
                CommonEmptyState(
                    title = "Tudo em dia",
                    message = "Não há etapas pendentes nesta rotina.",
                    isCard = true,
                )
            } else {
                CurrentStepCard(
                    occurrence = currentStep,
                    onRecordExecution = { onRecordExecution(currentStep.id) },
                    onReschedule = { onReschedule(currentStep) },
                )
            }
        }
        if (state.upcomingSteps.isNotEmpty()) {
            item {
                Text(
                    text = "Próximas etapas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.upcomingSteps, key = { it.id }) { step ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(step.stepTitle, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${step.stepType.displayName} · ${step.scheduledDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (step.status == HaircareOccurrenceStatus.RESCHEDULED) {
                            Text("Reagendada", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        HaircareHistoryList(state.history)
    }
}

@Composable
private fun CustomRoutineDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<HaircarePresetStep>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("2") }
    var steps by remember { mutableStateOf(listOf(HaircareStepType.HIDRATACAO)) }
    val intervalDays = interval.toIntOrNull()
    val canCreate = name.isNotBlank() && intervalDays != null && intervalDays > 0 && steps.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova rotina personalizada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da rotina") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit) },
                    label = { Text("Dias entre etapas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                steps.forEachIndexed { index, stepType ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StepTypeSelector(
                            selected = stepType,
                            onSelect = { selected ->
                                steps = steps.toMutableList().also { it[index] = selected }
                            },
                        )
                        TextButton(
                            enabled = steps.size > 1,
                            onClick = { steps = steps.filterIndexed { stepIndex, _ -> stepIndex != index } },
                        ) {
                            Text("Remover")
                        }
                    }
                }
                TextButton(
                    onClick = { steps = steps + HaircareStepType.HIDRATACAO },
                ) {
                    Text("Adicionar etapa")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canCreate,
                onClick = {
                    val days = intervalDays ?: return@Button
                    onCreate(
                        name.trim(),
                        steps.mapIndexed { index, type ->
                            HaircarePresetStep(
                                stepType = type,
                                stepTitle = type.displayName,
                                dayOffset = index * days,
                            )
                        },
                    )
                },
            ) {
                Text("Criar rotina")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun StepTypeSelector(
    selected: HaircareStepType,
    onSelect: (HaircareStepType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box {
        TextButton(onClick = { expanded = true }) {
            Text(selected.displayName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            HaircareStepType.entries.forEach { stepType ->
                DropdownMenuItem(
                    text = { Text(stepType.displayName) },
                    onClick = {
                        onSelect(stepType)
                        expanded = false
                    },
                )
            }
        }
    }
}
