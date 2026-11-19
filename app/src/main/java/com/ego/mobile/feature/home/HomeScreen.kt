package com.ego.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.ego.mobile.domain.model.ActivityType
import com.ego.mobile.domain.model.HabitDayGrid
import com.ego.mobile.domain.model.HomeSummary
import com.ego.mobile.domain.model.Task
import com.ego.mobile.feature.habits.HabitFormDialog
import com.ego.mobile.feature.home.components.ActiveRoutineBanner
import com.ego.mobile.feature.home.components.HabitWeekGrid
import com.ego.mobile.feature.home.components.PriorityTasksCard
import com.ego.mobile.feature.home.components.QuickActionHeader
import com.ego.mobile.feature.home.components.TodayHabitsSummaryCard
import com.ego.mobile.feature.tasks.components.TaskFormDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = rememberHomeViewModel(),
    onContinueRoutine: (ActivityType) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("home_preferences", 0) }
    var name by rememberSaveable { mutableStateOf(preferences.getString("name", null)?.ifBlank { null } ?: "Chefe") }
    var nameDraft by remember { mutableStateOf(name) }
    var showNameDialog by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var showHabitForm by remember { mutableStateOf(false) }
    var showTaskForm by remember { mutableStateOf(false) }
    var selectedHabit by remember { mutableStateOf<HomeSummary.TodayHabit?>(null) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.OpenHabitCreation -> showHabitForm = true
                HomeEvent.OpenTaskCreation -> showTaskForm = true
                is HomeEvent.HabitCompleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Hábito concluído",
                        actionLabel = "Desfazer",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.onUndoHabit(event.habitId)
                }
            }
        }
    }

    val errorMessage = when (val state = uiState) {
        HomeUiState.Loading -> null
        is HomeUiState.Content -> state.errorMessage
        is HomeUiState.Error -> state.message
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showCreateMenu = true },
                    modifier = Modifier.semantics { contentDescription = "Criar" },
                ) { Text("+") }
                DropdownMenu(expanded = showCreateMenu, onDismissRequest = { showCreateMenu = false }) {
                    DropdownMenuItem(text = { Text("Novo hábito") }, onClick = { showCreateMenu = false; showHabitForm = true })
                    DropdownMenuItem(text = { Text("Nova tarefa") }, onClick = { showCreateMenu = false; showTaskForm = true })
                }
            }
        },
    ) { contentPadding ->
        when (val state = uiState) {
            HomeUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { Text(state.message, color = MaterialTheme.colorScheme.error) }
            is HomeUiState.Content -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(contentPadding).testTag("home_list"),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    QuickActionHeader(
                        greeting = "${greetingForHour(Instant.now().atZone(ZoneId.of("America/Sao_Paulo")).hour)}, $name",
                        onEditName = { nameDraft = if (name == "Chefe") "" else name; showNameDialog = true },
                        onOpenHistory = onOpenHistory,
                    )
                }
                state.summary.activeWorkout?.let { routine -> item {
                    ActiveRoutineBanner(routine = routine, onContinue = { onContinueRoutine(routine.type) })
                } }
                state.summary.haircareRoutine?.takeIf {
                    haircareDateIsDue(it.subtitle, LocalDate.now(ZoneId.of("America/Sao_Paulo")))
                }?.let { routine -> item {
                    ActiveRoutineBanner(routine = routine, onContinue = { onContinueRoutine(routine.type) })
                } }
                item {
                    if (state.summary.todayHabitsTotal == 0 && state.summary.priorityTasksTotal == 0) {
                        Text("Comece pelo + para criar seu primeiro hábito ou tarefa.", style = MaterialTheme.typography.bodyMedium)
                    }
                    TodayHabitsSummaryCard(
                        habits = state.summary.todayHabits.filter { it.status != com.ego.mobile.domain.model.HabitOccurrenceStatus.COMPLETED },
                        totalCount = state.summary.todayHabitsTotal,
                        onComplete = viewModel::onCompleteHabit,
                        onUndo = viewModel::onUndoHabit,
                        onOpenAll = onOpenHabits,
                        onHabitSelected = { selectedHabit = it },
                    )
                }
                item {
                    PriorityTasksCard(
                        tasks = state.summary.priorityTasks,
                        totalCount = state.summary.priorityTasksTotal,
                        nowMillis = System.currentTimeMillis(),
                        onComplete = viewModel::onCompleteTask,
                        onOpenAll = onOpenTasks,
                        onTaskSelected = { selectedTask = it },
                    )
                }
                item { HabitWeekGrid(state.dayGrid) }
            }
        }
    }

    if (showHabitForm) HabitFormDialog(
        onDismiss = { showHabitForm = false },
        onSave = { name, description, frequency -> viewModel.onCreateHabit(name, description, frequency); showHabitForm = false },
    )
    if (showTaskForm) TaskFormDialog(
        task = null,
        onDismiss = { showTaskForm = false },
        onSave = { title, description, dueAt -> viewModel.onCreateTask(title, description, dueAt); showTaskForm = false },
    )
    if (showNameDialog) AlertDialog(
        onDismissRequest = { showNameDialog = false },
        title = { Text("Como devemos chamar você?") },
        text = { OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it.take(40) }, label = { Text("Nome") }, singleLine = true) },
        confirmButton = {
            TextButton(onClick = {
                name = nameDraft.trim().ifEmpty { "Chefe" }
                preferences.edit().putString("name", name).apply()
                showNameDialog = false
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("Cancelar") } },
    )
    selectedHabit?.let { habit -> AlertDialog(
        onDismissRequest = { selectedHabit = null },
        title = { Text(habit.title) },
        text = { Text(if (habit.weeklyTarget != null) "Progresso nesta semana: ${habit.completedThisWeek}/${habit.weeklyTarget}" else "Hábito previsto para hoje.") },
        confirmButton = {
            TextButton(onClick = { if (habit.status == com.ego.mobile.domain.model.HabitOccurrenceStatus.COMPLETED) viewModel.onUndoHabit(habit.habitId) else viewModel.onCompleteHabit(habit.habitId); selectedHabit = null }) {
                Text(if (habit.status == com.ego.mobile.domain.model.HabitOccurrenceStatus.COMPLETED) "Desfazer" else "Concluir")
            }
        },
        dismissButton = { TextButton(onClick = { selectedHabit = null }) { Text("Fechar") } },
    ) }
    selectedTask?.let { task -> AlertDialog(
        onDismissRequest = { selectedTask = null },
        title = { Text(task.title) },
        text = { Text(task.description?.takeIf(String::isNotBlank) ?: "Tarefa com prazo ${task.dueAt?.let(::formatDueDate) ?: "hoje"}.") },
        confirmButton = { TextButton(onClick = { viewModel.onCompleteTask(task.id); selectedTask = null }) { Text("Concluir") } },
        dismissButton = { TextButton(onClick = { selectedTask = null }) { Text("Fechar") } },
    ) }
}

internal fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Bom dia"
    in 12..17 -> "Boa tarde"
    else -> "Boa noite"
}

private fun haircareDateIsDue(subtitle: String?, today: LocalDate): Boolean {
    val date = subtitle?.substringAfter("·", "")?.trim()?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
    return !date.isAfter(today)
}

private fun formatDueDate(epoch: Long): String = Instant.ofEpochMilli(epoch)
    .atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate().toString()
