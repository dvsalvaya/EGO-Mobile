package com.ego.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement.Horizontal
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AnimatedVisibility
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.int.dp
import com.ego.mobile.app.theme.Colors
import com.ego.mobile.app.theme.Spacing
import com.ego.mobile.feature.habits.HabitFormDialog
import com.ego.mobile.feature.home.components.ConsistencyDayDot
import com.ego.mobile.feature.home.components.QuickActionHeader
import com.ego.mobile.feature.home.components.TodayHabitsSummaryCard
import com.ego.mobile.feature.home.components.TodayTasksSummaryCard
import com.ego.mobile.feature.tasks.components.TaskFormDialog
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.feature.routines.haircare.HaircareScreen
import com.ego.mobile.feature.routines.workouts.WorkoutScreen
import kotlinx.coroutines.flow.collectAsState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = rememberHomeViewModel(),
    onContinueRoutine: (/* ActivityType */) -> Unit = {},
    onNavigate: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showHabitForm by remember { mutableStateOf(false) }
    var showTaskForm by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.OpenHabitCreation -> showHabitForm = true
                HomeEvent.OpenTaskCreation -> showTaskForm = true
            }
        }
    }

    val errorMessage = when (uiState) {
        is HomeUiState.Content -> uiState.errorMessage
        else -> null
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Colors.background,
    ) { contentPadding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
            is HomeUiState.Error -> Column(
                modifier = Modifier.fillMaxSize().padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.message, color = Colors.error)
            }
            is HomeUiState.Content -> {
                val todaySummary = state.todaySummary
                val consistencyGrid = state.consistencyGrid
                val summary = state.summary

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    // Header: Saudação neutra
                    item {
                        HeaderGreeting()
                    }

                    // "Seu dia" - dois indicadores independentes
                    item {
                        TodayIndicators(
                            predictedHabits = todaySummary.predictedHabits,
                            openTasksToday = todaySummary.openTasksToday,
                            overdueTasks = todaySummary.overdueTasks,
                        )
                    }

                    // Ações rápidas
                    item {
                        QuickActionHeader(
                            onCreateHabit = { showHabitForm = true },
                            onCreateTask = { showTaskForm = true },
                        )
                    }

                    // "Para hoje"
                    item {
                        TodaySection(
                            habits = summary.todayHabits,
                            priorityTasks = summary.priorityTasks,
                            nowMillis = System.currentTimeMillis(),
                            onCompleteHabit = { habitId ->
                                viewModel.onCompleteHabit(habitId)
                            },
                            onCompleteTask = { taskId ->
                                viewModel.onCompleteTask(taskId)
                            },
                            onViewAllHabits = {
                                onNavigate("habits")
                            },
                            onViewAllTasks = {
                                onNavigate("tasks")
                            },
                        )
                    }

                    // "Sua consistência" - grade de 7 dias
                    item {
                        ConsistencyGrid(
                            dayIntensityMap = consistencyGrid.dayIntensityMap,
                        )
                    }

                    // Treinos e Cabelo na Home
                    summary.activeRoutine?.let { routine ->
                        item {
                            ActiveRoutineBanner(
                                routine = routine,
                                onContinue = { onContinueRoutine(routine.type) },
                            )
                        }
                    }

                    // Cabelo na Home (se houver etapa pendente para hoje)
                    // TODO: Implementar quando tiver dados de cabelo para hoje
                }
            }
        }
    }

    if (showHabitForm) {
        HabitFormDialog(
            onDismiss = { showHabitForm = false },
            onSave = { name, description, frequency ->
                viewModel.onCreateHabit(name, description, frequency)
                showHabitForm = false
            },
        )
    }
    if (showTaskForm) {
        TaskFormDialog(
            task = null,
            onDismiss = { showTaskForm = false },
            onSave = { title, description, dueAt ->
                viewModel.onCreateTask(title, description, dueAt)
                showTaskForm = false
            },
        )
    }
}

@Composable
private fun HeaderGreeting() {
    // Saudação neutra curta "Olá", sem nome de perfil, data ou botão de configurações
    Row(
        modifier = Modifier.padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Olá",
            style = MaterialTheme.typography.headlineSmall,
            color = Colors.onBackground,
        )
    }
}

@Composable
private fun TodayIndicators(
    predictedHabits: Int,
    openTasksToday: Int,
    overdueTasks: Int,
) {
    // Dois indicadores independentes, sem percentual agregado
    Column(
        modifier = Modifier.padding(Spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Indicador de hábitos
        HabitIndicator(
            label = "Hábitos",
            count = predictedHabits,
            // formato concluídos/previstos
            color = Colors.onSurface,
        )

        // Indicador de tarefas - separado
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TaskIndicator(
                label = "Tarefas com prazo",
                count = openTasksToday,
                color = if (overdueTasks > 0) Colors.error else Colors.onSurface,
            )
            if (overdueTasks > 0) {
                Text(
                    text = "${overdueTasks} Atrasada",
                    style = MaterialTheme.typography.bodySmall,
                    color = Colors.error,
                )
            }
        }
    }
}

@Composable
private fun HabitIndicator(
    label: String,
    count: Int,
    color: android.graphics.Color,
) {
    Text(
        text = "$label: $count",
        style = MaterialTheme.typography.bodyLarge,
        color = color,
    )
}

@Composable
private fun TaskIndicator(
    label: String,
    count: Int,
    color: android.graphics.Color,
) {
    Text(
        text = "$label: $count",
        style = MaterialTheme.typography.bodyLarge,
        color = color,
    )
}

@Composable
private fun TodaySection(
    habits: List</* HabitSummary */>,
    priorityTasks: List<TaskSummary>,
    nowMillis: Long,
    onCompleteHabit: (Long) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onViewAllHabits: () -> Unit,
    onViewAllTasks: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg, Spacing.xl, Spacing.lg, Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Hábitos previstos para hoje
        if (habits.isNotEmpty()) {
            Text(
                text = "Hábitos previstos",
                style = MaterialTheme.typography.titleLarge,
                color = Colors.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(Spacing.sm),
            ) {
                items(habits) { habit ->
                    TodayHabitItem(habit = habit, onComplete = onCompleteHabit)
                }
            }
        }

        // Tarefas abertas com prazo hoje e atrasadas
        if (priorityTasks.isNotEmpty()) {
            Text(
                text = "Para hoje",
                style = MaterialTheme.typography.titleLarge,
                color = Colors.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(Spacing.sm),
            ) {
                items(priorityTasks) { task ->
                    TodayTaskItem(
                        task = task.task,
                        isOverdue = task.isOverdue,
                        onComplete = { onCompleteTask(task.task.id) },
                        onViewDetails = {
                            // Abrir detalhes em Bottom Sheet
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayHabitItem(
    habit: /* HabitSummary */,
    onComplete: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = habit.habit.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
            )
            // Frequência ou progresso
            Text(
                text = "— ${habit.todayOccurrences} previsto(s) para hoje",
                style = MaterialTheme.typography.bodySmall,
                color = Colors.onSurfaceVariant,
            )
        }

        Button(
            onClick = { onComplete(habit.habit.id) },
            modifier = Modifier.semantics { contentDescription = "Concluir hábito: ${habit.habit.name}" },
        ) {
            Text("✓")
        }
    }
}

@Composable
private fun TodayTaskItem(
    task: Task,
    isOverdue: Boolean,
    onComplete: (Long) -> Unit,
    onViewDetails: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.font.FontWeight.SemiBold,
                overflow = TextOverflow.Elide,
            )
            if (task.dueAt != null) {
                Text(
                    text = "Prazo: ${java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault()).format(task.dueAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) Colors.error else Colors.onSurfaceVariant,
                )
            }
        }

        if (isOverdue) {
            Text(
                text = "Atrasada",
                style = MaterialTheme.typography.labelSmall,
                color = Colors.error,
            )
        } else {
            Button(
                onClick = onComplete,
                modifier = Modifier.semantics { contentDescription = "Concluir tarefa: ${task.title}" },
            ) {
                Text("✓")
            }
        }
    }
}

@Composable
private fun ConsistencyGrid(
    dayIntensityMap: Map<String, Int>,
) {
    // Grade com um ponto por cada um dos últimos sete dias, incluindo hoje, em America/Sao_Paulo
    // Contar somente eventos de conclusão
    // Níveis de intensidade: 0, 1, 2, 3+ atividades
    // Cada nível tem tonalidade neutra crescente e rótulo/descrição acessível

    val saoPaulo = java.time.ZoneId.of("America/Sao_Paulo")
    val now = java.time.LocalDate.now(saoPaulo)

    // Criar labels para cada nível
    val levelLabels = mapOf(
        0 to "0 atividade",
        1 to "1 atividade",
        2 to "2 atividades",
        3 to "3+ atividades",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Sua consistência",
            style = MaterialTheme.typography.titleLarge,
            color = Colors.onSurfaceVariant,
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(Spacing.sm),
        ) {
            // Um ponto para cada um dos últimos 7 dias
            for (i in 0..6) {
                val day = now.minusDays(i)
                val dayKey = day.toString()
                val intensity = dayIntensityMap[dayKey] ?: 0
                val level = if (intensity >= 3) 3 else intensity

                // Linha do dia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Ponto da consistência
                    ConsistencyDayDot(
                        level = level,
                        color = consistencyColorForLevel(level),
                    )

                    // Label do dia
                    Text(
                        text = day.format(java.time.format.TextStyle.ShortStyle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.onSurfaceVariant,
                    )

                    // Contagem
                    Text(
                        text = levelLabels[level] ?: intensity.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun consistencyColorForLevel(level: Int): android.graphics.Color {
    return when (level) {
        0 -> Colors.onSurfaceVariant
        1 -> Colors.primary
        2 -> Colors.primaryContainer
        else -> Colors.errorContainer
    }
}

@Composable
private fun ConsistencyDayDot(level: Int, color: android.graphics.Color) {
    val size = Spacing.lg
    val contentColor = when (level) {
        0 -> Colors.onSurfaceVariant
        else -> Colors.onPrimary
    }
    Box(
        modifier = Modifier
            .size(size)
            .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(size / 2)),
        content = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Today,
                contentDescription = null,
                tint = contentColor,
            )
        }
    )
}

@Composable
private fun ActiveRoutineBanner(
    routine: /* RoutineSummary */,
    onContinue: (/* ActivityType */) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Spacing.surface),
        colors = CardDefaults.cardColors(
            containerColor = Colors.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Sessão ativa",
                style = MaterialTheme.typography.titleMedium,
                color = Colors.onPrimary,
            )
            Text(
                text = "Continuar ${routine.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Colors.onPrimary,
                onClick = { onContinue(routine.type) },
                modifier = Modifier.clickable,
            )
        }
    }
}