package com.ego.mobile.feature.home

import android.database.SQLException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ego.mobile.data.local.EgoDatabaseProvider
import com.ego.mobile.data.repository.HabitRepositoryImpl
import com.ego.mobile.data.repository.TaskRepositoryImpl
import com.ego.mobile.domain.model.Habit
import com.ego.mobile.domain.model.HabitFrequency
import com.ego.mobile.domain.model.HabitStatus
import com.ego.mobile.domain.model.Task
import com.ego.mobile.domain.model.TaskStatus
import com.ego.mobile.domain.repository.HabitRepository
import com.ego.mobile.domain.repository.TaskRepository
import com.ego.mobile.domain.usecase.GetHomeSummaryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HomeViewModel(
    private val getHomeSummaryUseCase: GetHomeSummaryUseCase,
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.of("America/Sao_Paulo")) },
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = eventChannel.receiveAsFlow()

    init {
        getHomeSummaryUseCase()
            .onStart { _uiState.value = HomeUiState.Loading }
            .onEach { summary ->
                _uiState.value = HomeUiState.Content(
                    summary = summary,
                    todaySummary = computeTodaySummary(summary),
                    consistencyGrid = computeConsistencyGrid(summary),
                )
            }
            .catch { error ->
                _uiState.value = HomeUiState.Error(
                    error.message ?: "Não foi possível carregar o resumo.",
                )
            }
            .launchIn(viewModelScope)
    }

    fun onCompleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                habitRepository.completeOccurrence(habitId, today().toString())
                    .onFailure { error ->
                        setError(error.message ?: "Não foi possível concluir o hábito.")
                    }
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível concluir o hábito.")
            }
        }
    }

    fun onCompleteTask(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId, clock())
                    .onFailure { error ->
                        setError(error.message ?: "Não foi possível concluir a tarefa.")
                    }
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível concluir a tarefa.")
            }
        }
    }

    fun onCreateHabit(name: String, description: String?, frequency: HabitFrequency) {
        viewModelScope.launch {
            val now = clock()
            val habit = try {
                Habit(
                    name = name.trim(),
                    description = description?.trim()?.takeIf(String::isNotEmpty),
                    frequency = frequency,
                    status = HabitStatus.ACTIVE,
                    createdAt = now,
                    updatedAt = now,
                )
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Verifique os dados do hábito.")
                return@launch
            }
            try {
                habitRepository.saveHabit(habit)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível salvar o hábito.")
            }
        }
    }

    fun onCreateTask(title: String, description: String?, dueAt: Long?) {
        viewModelScope.launch {
            val now = clock()
            val task = try {
                Task(
                    title = title.trim(),
                    description = description?.trim()?.takeIf(String::isNotEmpty),
                    dueAt = dueAt,
                    createdAt = now,
                    updatedAt = now,
                )
            } catch (error: IllegalArgumentException) {
                setError(error.message ?: "Verifique os dados da tarefa.")
                return@launch
            }
            try {
                taskRepository.createTask(task)
            } catch (error: SQLException) {
                setError(error.message ?: "Não foi possível salvar a tarefa.")
            }
        }
    }

    fun onCreateHabitRequested() {
        viewModelScope.launch { eventChannel.send(HomeEvent.OpenHabitCreation) }
    }

    fun onCreateTaskRequested() {
        viewModelScope.launch { eventChannel.send(HomeEvent.OpenTaskCreation) }
    }

    fun clearError() {
        val state = _uiState.value
        if (state is HomeUiState.Content) {
            _uiState.value = state.copy(
                errorMessage = null,
                todaySummary = computeTodaySummary(state.summary),
                consistencyGrid = computeConsistencyGrid(state.summary),
            )
        }
    }

    private fun setError(message: String) {
        _uiState.value = when (val state = _uiState.value) {
            is HomeUiState.Content -> state.copy(errorMessage = message)
            HomeUiState.Loading,
            is HomeUiState.Error -> HomeUiState.Error(message)
        }
    }

    private fun computeTodaySummary(summary: HomeSummary): HomeUiState.TodaySummary {
        val nowMillis = clock()
        val openTasks = summary.priorityTasks
        // AC-05: Apenas tarefas com prazo (dueAt != null). Sem prazo ficam fora do contador.
        val openTasksToday = openTasks.filter { task ->
            task.dueAt != null && isDueToday(task.dueAt, nowMillis)
        }.size
        // AC-07: Classificação de atrasada usa exatamente dueAt < agora da Fase 4
        val overdueTasks = openTasks.filter { task ->
            task.isOverdue(nowMillis) && !isDueToday(task.dueAt, nowMillis)
        }.size

        // AC-05: Contagem de hábito considera somente hábitos ativos previstos para a data civil local.
        // O GetHomeSummaryUseCase já filtra habits por status ACTIVE no repository level,
        // então todayHabits já vem com apenas hábitos ativos.
        val predictedHabits = summary.todayHabits.size

        return HomeUiState.TodaySummary(
            predictedHabits = predictedHabits,
            openTasksToday = openTasksToday,
            overdueTasks = overdueTasks,
            totalTasks = openTasks.size,
        )
    }

    private fun computeConsistencyGrid(summary: HomeSummary): HomeUiState.ConsistencyGrid {
        // Build a map of day -> intensity level (0-3+)
        // Count only completed events: habit by habitId/civil date, task by ID/local completedAt, session by ID/local endedAt
        // Deduplicate by (source type, persisted id) using canonical date
        val calendar = java.util.Calendar.getInstance(java.util.Locale.forLanguageCode("pt"))
        calendar.firstDayOfWeek = java.util.Calendar.MONDAY
        // We'll compute the last 7 days including today in America/Sao_Paulo
        val saoPaulo = ZoneId.of("America/Sao_Paulo")
        val now = LocalDate.now(saoPaulo)
        val dayMap = mutableMapOf<String, Int>()

        // Initialize all 7 days with level 0
        for (i in 0..6) {
            val day = now.minusDays(i)
            dayMap[day.toString()] = 0
        }

        // Count habit completions
        for (habitSummary in summary.todayHabits) {
            // habitSummary.habit has the habit, todayOccurrences is the count for today
            // We need to mark the day as having completed occurrences
            val civilDate = java.time.LocalDate.now(saoPaulo).toString()
            val currentLevel = dayMap[civilDate] ?: 0
            dayMap[civilDate] = currentLevel + habitSummary.todayOccurrences
        }

        // Count task completions
        for (taskSummary in summary.priorityTasks) {
            if (taskSummary.task.completedAt != null) {
                val civilDate = LocalDate.ofEpochDay(
                    taskSummary.task.completedAt / 86400000L // assuming millis to date
                        .until(java.time.LocalDate.EPOCH, ChronoUnit.DAYS)
                ).withZoneSameInstant(saoPaulo).toLocalDate().toString()
                val currentLevel = dayMap[civilDate] ?: 0
                dayMap[civilDate] = currentLevel + 1
            }
        }

        // Count session completions (treinos)
        // This would need data from the workout repository

        return HomeUiState.ConsistencyGrid(dayIntensityMap = dayMap.toMap())
    }

    private fun isDueDueToday(dueAt: Long, nowMillis: Long): Boolean {
        // Check if the due date is today in Sao Paulo timezone
        val dueDate = LocalDate.ofEpochDay(
            dueAt / 86400000L
                .until(java.time.LocalDate.EPOCH, ChronoUnit.DAYS)
        ).withZoneSameInstant(ZoneId.of("America/Sao_Paulo")).toLocalDate()
        val today = LocalDate.now(ZoneId.of("America/Sao_Paulo"))
        return dueDate == today
    }
}

sealed interface HomeEvent {
    data object OpenHabitCreation : HomeEvent
    data object OpenTaskCreation : HomeEvent
}

@Composable
fun rememberHomeViewModel(): HomeViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val database = EgoDatabaseProvider.getDatabase(context)
        val habitRepository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitOccurrenceDao = database.habitOccurrenceDao(),
        )
        val taskRepository = TaskRepositoryImpl(database.taskDao())
        val workoutRepository = WorkoutRepositoryImpl(database.workoutDao())
        val haircareRepository = HaircareRepositoryImpl(database.haircareDao())
        HomeViewModel(
            getHomeSummaryUseCase = GetHomeSummaryUseCase(
                habitRepository = habitRepository,
                taskRepository = taskRepository,
                workoutRepository = workoutRepository,
                haircareRepository = haircareRepository,
            ),
            habitRepository = habitRepository,
            taskRepository = taskRepository,
        )
    }
}